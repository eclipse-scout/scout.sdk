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
package org.eclipse.scout.nls.sdk.internal.model.workspace;

import java.beans.PropertyChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.INlsConstants;
import org.eclipse.scout.nls.sdk.model.workspace.INlsType;

/**
 * <h4>NlsType</h4>
 */
public class NlsType implements INlsType {

  public static final String PROP_TRANSLATION_FOLDER_NAME = "translationFolderName";
  public static final String PROP_TRANSLATION_FILE_PREFIX = "translationFilePrefix";
  public static final String PROP_SUPER_TYPE = "superType";

  private final IType m_type;
  private IType[] m_superTypes;
  private BasicPropertySupport m_propertySupport;

  public NlsType(IType type) {
    m_propertySupport = new BasicPropertySupport(this);
    m_type = type;
    if (!m_type.isReadOnly()) {
      ResourcesPlugin.getWorkspace().addResourceChangeListener(new P_NlsResourceChangeListener(), IResourceChangeEvent.POST_CHANGE);
    }
    reload();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  public void reload() {
    try {
      IField field = m_type.getField(INlsConstants.RESOURCE_BUNDLE_FIELD_NAME);
      if (field != null && field.exists()) {
        int flags = field.getFlags();
        int refFlags = Flags.AccPublic | Flags.AccStatic;
        if ((refFlags & flags) == refFlags) {
          Matcher matcher = Pattern.compile(INlsConstants.RESOURCE_BUNDLE_FIELD_NAME + "\\s*=\\s*\\\"([^\\\"]*)\\\"\\s*\\;", Pattern.DOTALL).matcher(field.getSource());
          if (matcher.find()) {
            String bundleValue = matcher.group(1);
            String[] splitedValue = bundleValue.split("\\.");
            String filePrefix = splitedValue[splitedValue.length - 1];
            String folderName = "";
            for (int i = 0; i < splitedValue.length - 1; i++) {
              folderName = folderName + "/" + splitedValue[i];
            }
            setTranslationFolderName(folderName);
            setTranslationFilePrefix(filePrefix);
          }

        }
      }
      ITypeHierarchy typeHierarchy = m_type.newTypeHierarchy(new NullProgressMonitor());
      m_superTypes = typeHierarchy.getAllSuperclasses(m_type);
//      IType superclass = typeHierarchy.getSuperclass(m_type);
      // String superClassPluginId = PluginRegistry.findModel(superclass.getJavaProject().getProject()).getPluginBase().getId();
      if (m_superTypes.length > 0 && m_superTypes[0].exists() && !m_superTypes[0].getFullyQualifiedName().equals(DynamicNls.class.getName())) {
        m_propertySupport.setProperty(PROP_SUPER_TYPE, m_superTypes[0]);
      }
    }
    catch (JavaModelException e) {
      NlsCore.logError("could not reload NLS type '" + m_type.getFullyQualifiedName() + "'");
    }
  }

  @Override
  public IType getType() {
    return m_type;
  }

  protected void setTranslationFolderName(String name) {
    m_propertySupport.setPropertyString(PROP_TRANSLATION_FOLDER_NAME, name);
  }

  @Override
  public String getTranslationsFolderName() {
    return m_propertySupport.getPropertyString(PROP_TRANSLATION_FOLDER_NAME);
  }

  protected void setTranslationFilePrefix(String filePrefix) {
    m_propertySupport.setPropertyString(PROP_TRANSLATION_FILE_PREFIX, filePrefix);
  }

  @Override
  public String getTranslationsPrefix() {
    return m_propertySupport.getPropertyString(PROP_TRANSLATION_FILE_PREFIX);
  }

  public IType getSuperType() {
    return (IType) m_propertySupport.getProperty(PROP_SUPER_TYPE);
  }

  @Override
  public IType[] getAllSuperclasses() {
    return m_superTypes;
  }

  /**
   * commodity
   * 
   * @return
   */
  public IJavaProject getJavaProject() {
    if (m_type == null) {
      return null;
    }
    return m_type.getJavaProject();
  }

  public String getHostPluginId() {
    String hostPluginId = PluginRegistry.findModel(m_type.getJavaProject().getProject()).getPluginBase().getId();
    return hostPluginId;
  }

  private class P_NlsResourceChangeListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta resDelta) {
              IResource resource = resDelta.getResource();
              if (resource != null && resource.equals(m_type.getResource())) {
                if (m_type.getResource().exists()) {
                  reload();
                }
              }
              return true;
            }
          });
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }
  } // end class P_NlsFileChangeListener
}
