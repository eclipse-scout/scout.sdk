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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutFileLocator;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.graphics.ImageData;

public class ScoutProjectIcons implements IIconProvider {

  protected static String[] PREDEFINED_EXTENSIONS = new String[]{"png", "ico", "gif"};
  protected static String[] PREDEFINED_SUB_FOLDERS = new String[]{"resources/icons/internal/", "resources/icons/"};

  private HashMap<String, ScoutIconDesc> m_cachedIcons;
  private Object cacheLock = new Object();

  final IType abstractIcons = TypeUtility.getType(RuntimeClasses.AbstractIcons);
  private final IScoutProject m_scoutProject;

  private IPrimaryTypeTypeHierarchy m_iconsHierarchy;
  private String[] m_baseUrls;

  public ScoutProjectIcons(IScoutProject scoutProject) {
    m_scoutProject = scoutProject;
    m_iconsHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractIcons);
    m_iconsHierarchy.addHierarchyListener(new ITypeHierarchyChangedListener() {
      @Override
      public void handleEvent(int eventType, IType type) {
        clearCache();
      }
    });

  }

  @Override
  public ScoutIconDesc[] getIcons() {
    cache();
    return m_cachedIcons.values().toArray(new ScoutIconDesc[m_cachedIcons.size()]);
  }

  @Override
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
        m_baseUrls = buildBaseUrls();
        Map<String, ScoutIconDesc> collector = new HashMap<String, ScoutIconDesc>();
        collectIconNames(collector);
        for (ScoutIconDesc desc : collector.values()) {
          findIconInProject(desc);
        }
        m_cachedIcons = new HashMap<String, ScoutIconDesc>(collector);// collector.toArray(new BCIcon[collector.size()]);
      }
    }
  }

  private String[] buildBaseUrls() {
    List<String> projects = new ArrayList<String>();
    IScoutBundle clientP = m_scoutProject.getClientBundle();
    while (clientP != null) {
      projects.add(clientP.getBundleName());
      if (clientP.getScoutProject().getParentProject() != null) {
        clientP = clientP.getScoutProject().getParentProject().getClientBundle();
      }
      else {
        clientP = null;
      }
    }
    projects.add(RuntimeClasses.ScoutClientBundleId);
    projects.add(RuntimeClasses.ScoutUiSwtBundleId);
    projects.add(RuntimeClasses.ScoutUiSwingBundleId);
    // invert order
    String[] result = projects.toArray(new String[projects.size()]);
    Arrays.sort(result, Collections.reverseOrder());
    return result;
  }

  protected void collectIconNames(Map<String, ScoutIconDesc> collector) {
    // find best match shared bundle
    IScoutProject project = m_scoutProject;
    IScoutBundle sharedBundle = null;
    while (sharedBundle == null && project != null) {
      sharedBundle = project.getSharedBundle();
      project = project.getParentProject();
    }
    if (sharedBundle != null) {
      IType[] iconTypes = m_iconsHierarchy.getAllSubtypes(abstractIcons, ScoutTypeFilters.getInScoutBundles(sharedBundle));
      if (iconTypes.length > 0) {
        if (TypeUtility.exists(iconTypes[0])) {
          try {
            collectIconNamesOfType(iconTypes[0], collector);
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  protected void collectIconNamesOfType(IType iconType, Map<String, ScoutIconDesc> collector) throws JavaModelException, IllegalArgumentException {
    if (TypeUtility.exists(iconType)) {
      boolean inherited = ScoutTypeUtility.getScoutProject(iconType) == m_scoutProject;
      for (IField field : iconType.getFields()) {
        if (Flags.isPublic(field.getFlags()) && field.getSource() != null && field.getSource().contains(" String ")) {
          String source = field.getSource();
          String iconDeclaration = ScoutUtility.removeComments(source);
          String iconSimpleName = null;
          try {
            iconSimpleName = Regex.getFieldDeclarationRightHandSide(iconDeclaration);
            collector.put(iconSimpleName, new ScoutIconDesc(field.getElementName(), Regex.getIconSimpleName(iconSimpleName), field, inherited));
          }
          catch (Exception e) {
            ScoutSdk.logError(e);
          }
        }
      }
      collectIconNamesOfType(m_iconsHierarchy.getSuperclass(iconType), collector);
    }
  }

  protected void findIconInProject(ScoutIconDesc desc) {
    if (desc == null || desc.getIconName() == null) return;

    for (String baseUrl : m_baseUrls) {
      for (String ext : PREDEFINED_EXTENSIONS) {
        for (String folder : PREDEFINED_SUB_FOLDERS) {
          String imgPath = folder + desc.getIconName() + "." + ext;
          InputStream is = null;
          try {
            is = ScoutFileLocator.resolve(baseUrl, imgPath);
            if (is != null) {
              ImageDescriptor desc1 = ImageDescriptor.createFromImageData(new ImageData(is));
              if (desc1 != null) {
                desc.setImgDesc(desc1);
                return;
              }
            }
          }
          finally {
            if (is != null) {
              try {
                is.close();
              }
              catch (IOException e) {
                ScoutSdk.logWarning("could not close input stream of file '" + baseUrl + "" + imgPath + "'.", e);
              }
            }
          }
        }
      }
    }
  }

//  protected void findIconInProject1(IScoutProject project, ScoutIconDesc desc) {
//    if (project == null) {
//      return;
//    }
//    for (String ext : PREDEFINED_EXTENSIONS) {
//      Path iconPath = new Path(ScoutIdeProperties.ICON_PATH + desc.getIconName() + "." + ext);
//      if (project.getClientBundle() != null) {
//        if (project.getClientBundle().getProject().getResourceAttributes().isReadOnly()) {
//          URL[] imgUrl = FileLocator.findEntries(Platform.getBundle(project.getClientBundle().getBundleName()), iconPath, null);
//          if (imgUrl.length > 0) {
//            desc.setImgDesc(ImageDescriptor.createFromURL(imgUrl[imgUrl.length - 1]));
//            return;
//          }
//        }
//        else {
//          project.getClientBundle().getProject().getFile(iconPath);
//          try {
//            IResource iconResource = project.getClientBundle().getProject().getFile(iconPath);
//            if (iconResource instanceof IFile && iconResource.exists()) {
//              ImageData data = new ImageData(((IFile) iconResource).getContents());
//              desc.setImgDesc(ImageDescriptor.createFromImageData(data));
//              return;
//            }
//          }
//          catch (CoreException e) {
//            ScoutSdk.logInfo("could not find icon '" + iconPath + "'");
//            return;
//          }
//        }
//      }
//    }
//    findIconInProject1(project.getParentProject(), desc);
//  }

//  protected void findIconsInProject(IScoutProject project,  Map<String, ScoutIconDesc> collector){
//    IType[] iconTypes = iconsHierarchy.getAllSubtypes(abstractIcons, TypeFilters.getInScoutBundles(project.getSharedBundle()));
//    if (iconTypes.length > 0) {
//      if (TypeUtility.exists(iconTypes[0])) {
//
//      }
//    }
//  }
//
//  private void collectIconsRect(IType iconType, ITypeHierarchy iconsHierarchy, Map<String, ScoutIconDesc> collector, boolean inherited) {
//      if (TypeUtility.exists(iconType)) {
//        try {
//
//          for (IField field : iconType.getFields()) {
//            if (Flags.isPublic(field.getFlags()) && field.getSource() != null && field.getSource().contains(" String ")) {
//              String source = field.getSource();
//              String iconDeclaration = ScoutUtility.removeComments(source);
//              String iconSimpleName = Regex.getFieldDeclarationRightHandSide(iconDeclaration);
//              ScoutIconDesc createIcon = createIcon(project, iconSimpleName, field, inherited);
//              if (createIcon != null) {
//                collector.put(iconSimpleName, createIcon);
//              }
//            }
//          }
//        }
//        catch (JavaModelException e) {
//          ScoutSdk.logWarning("error during parsing icon fields of '" + iconTypes[0] + "'.");
//        }
//        collectIconsRect(project.getParentProject(), iconsHierarchy, collector, true);
//      }
//    }
//    else {
//      ScoutSdk.logWarning("no icon support found for scout project '" + m_scoutProject.getProjectName() + "'.");
//    }
//  }
//
////  private void collectIconsRec(IType iconType, ITypeHierarchy superTypeHierarchy, Map<String, ScoutIconDesc> collector, boolean inherited) throws JavaModelException {
////    if (iconType == null || !iconType.exists()) {
////      return;
////    }
////    else {
////      for (IField field : iconType.getFields()) {
////        if (Flags.isPublic(field.getFlags()) && field.getSource() != null && field.getSource().contains(" String ")) {
////          String source = field.getSource();
////          String iconDeclaration = ScoutUtility.removeComments(source);
////          String iconSimpleName = Regex.getFieldDeclarationRightHandSide(iconDeclaration);
////          ScoutIconDesc createIcon = createIcon(iconSimpleName, field, inherited);
////          if (createIcon != null) {
////            collector.put(iconSimpleName, createIcon);
////          }
////        }
////      }
////      collectIconsRec(superTypeHierarchy.getSuperclass(iconType), superTypeHierarchy, collector, true);
////    }
////  }
//
//  private ScoutIconDesc createIcon(IScoutProject project, String constant, IField element, boolean inherited) {
//    String iconName = Regex.getIconSimpleName(constant);
//    ImageDescriptor img = null;
//    // loop all possible extension
//    for (String ext : PREDEFINED_EXTENSIONS) {
//      Path fullyPath = new Path(ScoutIdeProperties.ICON_PATH + iconName + "." + ext);
//      try {
//        img = getImage(fullyPath, project, element);
//        if (img != null) {
//          break;
//        }
//
//      }
//      catch (Exception e) {
//        ScoutSdk.logWarning("could not load image '" + fullyPath.toString() + "'", e);
//      }
//    }
//    if (img == null) {
//      ScoutSdk.logWarning("could not find icon '" + iconName + "' in project '" + element.getJavaProject().getProject().getName() + "'");
//    }
//    ScoutIconDesc icon = new ScoutIconDesc(element.getElementName(), iconName, img, element, inherited);
//    return icon;
//  }
//
//  private ImageDescriptor getImage(Path iconPath, IScoutProject project, IJavaElement element) throws CoreException, IOException {
////    IResource resource = element.getResource();
//    ImageDescriptor img = null;
//    if (project.getClientBundle() != null) {
//      if (project.getClientBundle().getProject().getResourceAttributes().isReadOnly()) {
//        URL[] imgUrl = FileLocator.findEntries(Platform.getBundle(project.getClientBundle().getBundleName()), iconPath, null);
//        if (imgUrl.length > 0) {
//          img = ImageDescriptor.createFromURL(imgUrl[imgUrl.length - 1]);
//        }
//      }
//      else {
//        project.getClientBundle().getProject().getFile(iconPath);
//        try {
//          IResource iconResource = project.getClientBundle().getProject().getFile(iconPath);
//          if (iconResource instanceof IFile) {
//            ImageData data = new ImageData(((IFile) iconResource).getContents());
//            img = ImageDescriptor.createFromImageData(data);
//          }
//        }
//        catch (CoreException e) {
//          ScoutSdk.logInfo("could not find icon '" + iconPath + "'");
//          return null;
//        }
//      }
//    }
////    if (resource == null || element.isReadOnly()) {
////      // ResourcesPlugin.getWorkspace().getRoot()
////      project.getClientBundle().getProject().g
////      URL imgUrl = FileLocator.find(Platform.getBundle(getPluginId(element)), iconPath, null);
////
////      if (imgUrl != null) {
////        img = ImageDescriptor.createFromURL(imgUrl);
////      }
////    }
////    else if (resource instanceof IFile) {
////      IFile file = (IFile) resource;
////      try {
////        IResource iconResource = file.getProject().getFile(iconPath);
////        if (iconResource instanceof IFile) {
////          ImageData data = new ImageData(((IFile) iconResource).getContents());
////          img = ImageDescriptor.createFromImageData(data);
////        }
////      }
////      catch (CoreException e) {
////        ScoutSdk.logInfo("could not find icon '" + iconPath + "'");
////        return null;
////      }
////    }
//    return img;
//  }

}
