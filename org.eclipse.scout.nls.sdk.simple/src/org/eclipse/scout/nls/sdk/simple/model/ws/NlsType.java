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
package org.eclipse.scout.nls.sdk.simple.model.ws;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.sdk.util.ScoutSdkUtilCore;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.resources.WeakResourceChangeListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h4>NlsType</h4>
 */
public class NlsType implements INlsType {

  public static final char FOLDER_SEGMENT_SEPARATOR = '/';
  public static final String RESOURCE_BUNDLE_FIELD_NAME = "RESOURCE_BUNDLE_NAME";
  public static final String PROP_TRANSLATION_FOLDER_NAME = "translationFolderName";
  public static final String PROP_TRANSLATION_FILE_PREFIX = "translationFilePrefix";
  public static final String PROP_SUPER_TYPE = "superType";

  private static final Pattern REGEX_RESOURCE_BUNDLE_FIELD = Pattern.compile(RESOURCE_BUNDLE_FIELD_NAME + "\\s*=\\s*\\\"([^\\\"]*)\\\"\\s*\\;", Pattern.DOTALL);

  protected IResourceChangeListener m_nlsResourceChangeListener;
  protected final IType m_type;
  protected final BasicPropertySupport m_propertySupport;

  public NlsType(IType type) {
    m_propertySupport = new BasicPropertySupport(this);
    m_type = type;
    if (!m_type.isReadOnly()) {
      m_nlsResourceChangeListener = new P_NlsResourceChangeListener();
      ResourcesPlugin.getWorkspace().addResourceChangeListener(new WeakResourceChangeListener(m_nlsResourceChangeListener), IResourceChangeEvent.POST_CHANGE);
    }
    reload();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  protected String getBundleValue() throws JavaModelException {
    IField field = m_type.getField(RESOURCE_BUNDLE_FIELD_NAME);
    if (TypeUtility.exists(field)) {
      Matcher matcher = REGEX_RESOURCE_BUNDLE_FIELD.matcher(field.getSource());
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  protected void loadSuperTypeHierarchy() throws JavaModelException {
    ITypeHierarchy typeHierarchy = ScoutSdkUtilCore.getHierarchyCache().getSuperHierarchy(m_type);
    IType firstType = typeHierarchy.getSuperclass(m_type);
    if (TypeUtility.exists(firstType)) {
      if (!DynamicNls.class.getName().equals(firstType.getFullyQualifiedName())) {
        m_propertySupport.setProperty(PROP_SUPER_TYPE, firstType);
      }
    }
  }

  public void reload() {
    try {
      String bundleValue = getBundleValue();
      if (bundleValue != null) {
        String[] splitedValue = bundleValue.split("\\.");
        String filePrefix = splitedValue[splitedValue.length - 1];
        StringBuilder folderName = new StringBuilder();
        for (int i = 0; i < splitedValue.length - 1; i++) {
          folderName.append(FOLDER_SEGMENT_SEPARATOR).append(splitedValue[i]);
        }
        setTranslationFolderName(folderName.toString());
        setTranslationFilePrefix(filePrefix);
      }

      loadSuperTypeHierarchy();
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
      if (TypeUtility.exists(m_type)) {
        final IResource resourceToFind = m_type.getResource();
        if (ResourceUtility.exists(resourceToFind)) {
          IResourceDelta delta = event.getDelta();
          try {
            List<IResource> allResources = ResourceUtility.getAllResources(delta, new IResourceFilter() {
              @Override
              public boolean accept(IResourceProxy resource) {
                return resourceToFind.equals(resource.requestResource());
              }
            });

            if (!allResources.isEmpty()) {
              reload();
            }
          }
          catch (CoreException e) {
            NlsCore.logWarning(e);
          }
        }
      }
    }
  } // end class P_NlsFileChangeListener
}
