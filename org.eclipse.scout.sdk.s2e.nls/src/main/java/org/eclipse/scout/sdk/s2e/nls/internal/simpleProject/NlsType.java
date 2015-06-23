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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleProject;

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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.util.BasicPropertySupport;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.WeakResourceChangeListener;

/**
 * <h4>NlsType</h4>
 */
public class NlsType implements INlsType {

  public static final String DYNAMIC_NLS_NAME = "org.eclipse.scout.commons.nls.DynamicNls";
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
    if (JdtUtils.exists(field)) {
      Matcher matcher = REGEX_RESOURCE_BUNDLE_FIELD.matcher(field.getSource());
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  protected void loadSuperTypeHierarchy() throws JavaModelException {
    ITypeHierarchy superTypeHierarchy = m_type.newSupertypeHierarchy(null);
    IType firstType = superTypeHierarchy.getSuperclass(m_type);
    if (JdtUtils.exists(firstType)) {
      String superTypeFqn = firstType.getFullyQualifiedName();
      if (!DYNAMIC_NLS_NAME.equals(superTypeFqn) && !Object.class.getName().equals(superTypeFqn)) {
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
      NlsCore.logError("could not reload NLS type '" + m_type.getFullyQualifiedName() + "'.", e);
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

  private class P_NlsResourceChangeListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (!JdtUtils.exists(m_type)) {
        return;
      }

      final IResource resourceToFind = m_type.getResource();
      if (resourceToFind == null || !resourceToFind.exists()) {
        return;
      }

      try {
        final boolean[] resourceFound = new boolean[1];
        IResourceDelta delta = event.getDelta();
        delta.accept(new IResourceDeltaVisitor() {
          @Override
          public boolean visit(IResourceDelta d) throws CoreException {
            if (resourceFound[0]) {
              return false;
            }

            resourceFound[0] = resourceToFind.equals(d.getResource());
            return !resourceFound[0];
          }
        });

        if (resourceFound[0]) {
          reload();
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }
  }// end class P_NlsResourceChangeListener
}
