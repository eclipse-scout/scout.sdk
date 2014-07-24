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
package org.eclipse.scout.nls.sdk.simple.operations;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;

public class NewNlsFileOperationDesc {
  public static final String PROP_PLUGIN = "plugin";
  public static final String PROP_FILE_NAME = "fileName";

  public static final String PROP_SRC_CONTAINER = "srcContainer";
  public static final String PROP_PACKAGE = "package";
  public static final String PROP_CLASS_NAME = "className";

  public static final String PROP_PARENT_PLUGIN = "parentPlugin";
  public static final String PROP_PARENT_FILE = "parentFile";

  public static final String PROP_TRANSLATION_FOLDER = "translationFolder";
  public static final String PROP_TRANSLATION_FILE = "translationFile";

  private final BasicPropertySupport m_propertySupport = new BasicPropertySupport(this);

  public void setPlugin(IProject value) {
    m_propertySupport.setProperty(PROP_PLUGIN, value);
    try {
      if (value != null) {
        IJavaProject jp = JavaCore.create(value);
        List<IClasspathEntry> possibleEntries = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : NlsJdtUtility.getSourceLocations(jp)) {
          if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
            possibleEntries.add(entry);
          }
        }
        if (possibleEntries.size() > 0) {
          // set first as default
          setSourceContainer(possibleEntries.get(0).getPath());
        }
        else {
          setSourceContainer(null);
        }
      }
      else {
        setSourceContainer(null);
      }
    }
    catch (JavaModelException e) {
      NlsCore.logWarning(e);
      setSourceContainer(null);
    }
    setPackage(null);
  }

  public Map<String, Object> getPropertiesMap() {
    return m_propertySupport.getPropertiesMap();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  public IProject getPlugin() {
    return (IProject) m_propertySupport.getProperty(PROP_PLUGIN);
  }

  public void setFileName(String value) {
    m_propertySupport.setPropertyString(PROP_FILE_NAME, value);
  }

  public String getFileName() {
    return m_propertySupport.getPropertyString(PROP_FILE_NAME);
  }

  public void setSourceContainer(IPath value) {
    m_propertySupport.setProperty(PROP_SRC_CONTAINER, value);
  }

  public IPath getSourceContainer() {
    return (IPath) m_propertySupport.getProperty(PROP_SRC_CONTAINER);
  }

  public void setPackage(String value) {
    m_propertySupport.setPropertyString(PROP_PACKAGE, value);
  }

  public String getPackage() {
    return m_propertySupport.getPropertyString(PROP_PACKAGE);
  }

  public void setClassName(String input) {
    if (!StringUtility.hasText(input)) {
      input = null;
    }
    m_propertySupport.setPropertyString(PROP_CLASS_NAME, input);
  }

  public String getClassName() {
    return m_propertySupport.getPropertyString(PROP_CLASS_NAME);
  }

  public void setParentPlugin(IPluginModelBase model) {
    m_propertySupport.setProperty(PROP_PARENT_PLUGIN, model);
  }

  public IPluginModelBase getParentPlugin() {
    return (IPluginModelBase) m_propertySupport.getProperty(PROP_PARENT_PLUGIN);
  }

  public void setParentFile(IFile file) {
    m_propertySupport.setProperty(PROP_PARENT_FILE, file);
  }

  public IFile getParentFile() {
    return (IFile) m_propertySupport.getProperty(PROP_PARENT_FILE);
  }

  public void setTranslationFolder(String folder) {
    m_propertySupport.setPropertyString(PROP_TRANSLATION_FOLDER, folder);
  }

  public String getTranslationFolder() {
    return m_propertySupport.getPropertyString(PROP_TRANSLATION_FOLDER);
  }

  public void setTranlationFileName(String filename) {
    m_propertySupport.setPropertyString(PROP_TRANSLATION_FILE, filename);
  }

  public String getTranlationFileName() {
    return m_propertySupport.getPropertyString(PROP_TRANSLATION_FILE);
  }
}
