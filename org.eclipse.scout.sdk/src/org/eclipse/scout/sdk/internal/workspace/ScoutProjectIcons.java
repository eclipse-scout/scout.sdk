/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.swt.graphics.ImageData;

public class ScoutProjectIcons implements IIconProvider {

  protected static String[] PREDEFINED_EXTENSIONS = new String[]{"ico", "gif", "png"};

  private IType m_projectIconsType;
  private HashMap<String, ScoutIconDesc> m_cachedIcons;
  private Object cacheLock = new Object();

//  private IScoutTypeListener m_iconsTypeListener;

  public ScoutProjectIcons(IType iconsType) {
    m_projectIconsType = iconsType;
//    m_iconsTypeListener = new IScoutTypeListener() {
//      @Override
//      public void innerTypeChanged(IScoutType type, IScoutType innerType, int eventType, IJavaElement modification) {
//
//      }
//
//      @Override
//      public void typeChanged(IScoutType type, int eventType, IJavaElement modification) {
//        clearCache();
//      }
//    };
//    iconsType.addWeakScoutTypeListener(m_iconsTypeListener);
  }

  private String getPluginId(IJavaElement type) {
    if (type == null) {
      return null;
    }
    else if (type.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
      return type.getElementName();
    }
    else if (type.getElementType() == IJavaElement.JAVA_PROJECT) {
      return type.getElementName();
    }
    else {
      return getPluginId(type.getParent());
    }
  }

  public ScoutIconDesc[] getIcons() {
    cache();
    return m_cachedIcons.values().toArray(new ScoutIconDesc[m_cachedIcons.size()]);
  }

  public ScoutIconDesc getIcon(String key) {
    cache();
    return m_cachedIcons.get(key);
  }

  // internal cache methods

  private void clearCache() {
    if (m_cachedIcons != null) {
      m_cachedIcons.clear();
    }
    m_cachedIcons = null;
  }

  protected void cache() {
    synchronized (cacheLock) {
      if (m_cachedIcons == null) {
        Map<String, ScoutIconDesc> collector = new HashMap<String, ScoutIconDesc>();
        try {
          ITypeHierarchy superTypeHierarchy = m_projectIconsType.newSupertypeHierarchy(null);
          collectIconsRec(m_projectIconsType, superTypeHierarchy, collector, false);
        }
        catch (JavaModelException e) {
          ScoutSdk.logWarning("could not load icons of '" + m_projectIconsType.getFullyQualifiedName() + "'", e);
        }

        m_cachedIcons = new HashMap<String, ScoutIconDesc>(collector);// collector.toArray(new BCIcon[collector.size()]);
      }
    }
  }

  private void collectIconsRec(IType iconType, ITypeHierarchy superTypeHierarchy, Map<String, ScoutIconDesc> collector, boolean inherited) throws JavaModelException {
    if (iconType == null || !iconType.exists()) {
      return;
    }
    else {
      for (IField field : iconType.getFields()) {
        if (Flags.isPublic(field.getFlags()) && field.getSource() != null && field.getSource().contains(" String ")) {
          String source = field.getSource();
          String iconDeclaration = ScoutUtility.removeComments(source);
          String iconSimpleName = Regex.getFieldDeclarationRightHandSide(iconDeclaration);
          ScoutIconDesc createIcon = createIcon(iconSimpleName, field, inherited);
          if (createIcon != null) {
            collector.put(iconSimpleName, createIcon);
          }
        }
      }
      collectIconsRec(superTypeHierarchy.getSuperclass(iconType), superTypeHierarchy, collector, true);
    }
  }

  private ScoutIconDesc createIcon(String constant, IField element, boolean inherited) {
    String iconName = Regex.getIconSimpleName(constant);
    ImageDescriptor img = null;
    // loop all possible extension
    for (String ext : PREDEFINED_EXTENSIONS) {
      Path fullyPath = new Path(ScoutIdeProperties.ICON_PATH + iconName + "." + ext);
      try {
        img = getImage(fullyPath, element);
        if (img != null) {
          break;
        }

      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not load image '" + fullyPath.toString() + "'", e);
      }
    }
    if (img == null) {
      ScoutSdk.logWarning("could not find icon '" + iconName + "' in project '" + element.getJavaProject().getProject().getName() + "'");
    }
    ScoutIconDesc icon = new ScoutIconDesc(element.getElementName(), iconName, img, element, inherited);
    return icon;
  }

  private ImageDescriptor getImage(Path iconPath, IJavaElement element) throws CoreException, IOException {
    IResource resource = element.getResource();
    ImageDescriptor img = null;
    if (resource == null || element.isReadOnly()) {
      // ResourcesPlugin.getWorkspace().getRoot()

      URL imgUrl = FileLocator.find(Platform.getBundle(getPluginId(element)), iconPath, null);

      if (imgUrl != null) {
        img = ImageDescriptor.createFromURL(imgUrl);
      }
    }
    else if (resource instanceof IFile) {
      IFile file = (IFile) resource;
      try {
        IResource iconResource = file.getProject().getFile(iconPath);
        if (iconResource instanceof IFile) {
          ImageData data = new ImageData(((IFile) iconResource).getContents());
          img = ImageDescriptor.createFromImageData(data);
        }
      }
      catch (CoreException e) {
        ScoutSdk.logInfo("could not find icon '" + iconPath + "'");
        return null;
      }
    }
    return img;
  }

  public IType getIconsType() {
    return m_projectIconsType;
  }
}
