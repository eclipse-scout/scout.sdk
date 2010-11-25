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
package org.eclipse.scout.nls.sdk.operations.desc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtHandler;
import org.eclipse.scout.nls.sdk.internal.model.PropertyBasedModel;

public class NewNlsFileOperationDesc extends PropertyBasedModel {
  public static final String PROP_PLUGIN = "plugin";
  public static final String PROP_FILE_NAME = "fileName";
  public static final String PROP_NLS_CLASS_PACKAGE = "nlsClassPackage";
  public static final String PROP_SRC_CONTAINER = "srcContainer";
  public static final String PROP_PACKAGE = "package";
  public static final String PROP_CLASS_NAME = "className";
  public static final String PROP_BINDING_DYNAMIC = "dynamicBinding";
  public static final String PROP_PARENT_PLUGIN = "parentPlugin";
  public static final String PROP_PARENT_FILE = "parentFile";
  public static final String PROP_TRANSLATION_FOLDER = "translationFolder";
  public static final String PROP_TRANSLATION_FILE = "translationFile";

  public NewNlsFileOperationDesc() {

  }

  public void setPlugin(IProject value) {
    setProperty(PROP_PLUGIN, value);
    try {
      if (value != null) {

        IJavaProject jp = JavaCore.create(value);
        List<IClasspathEntry> possibleEntries = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : NlsJdtHandler.getSourceLocations(jp)) {
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
      // TODO: handle exception
      NlsCore.logWarning(e);
      setSourceContainer(null);
    }
    setPackage(null);
  }

  public IProject getPlugin() {
    return (IProject) getProperty(PROP_PLUGIN);
  }

  public void setFileName(String value) {
    setPropertyString(PROP_FILE_NAME, value);
  }

  public String getFileName() {
    return getPropertyString(PROP_FILE_NAME);
  }

  public void setSourceContainer(IPath value) {
    setProperty(PROP_SRC_CONTAINER, value);
  }

  public IPath getSourceContainer() {
    return (IPath) getProperty(PROP_SRC_CONTAINER);
  }

  public void setPackage(String value) {
    setPropertyString(PROP_PACKAGE, value);
  }

  public String getPackage() {
    return getPropertyString(PROP_PACKAGE);
  }

  public void setClassName(String input) {
    if (input == null || input.equals("")) {
      input = null;
    }
    setPropertyString(PROP_CLASS_NAME, input);
  }

  public String getClassName() {
    return getPropertyString(PROP_CLASS_NAME);
  }

  public void setFileTypeDynamic(boolean b) {
    setPropertyBool(PROP_BINDING_DYNAMIC, b);
  }

  public boolean isFileTypeDynamic() {
    return getPropertyBool(PROP_BINDING_DYNAMIC);
  }

  public void setParentPlugin(IPluginModelBase model) {
    setProperty(PROP_PARENT_PLUGIN, model);

  }

  public IPluginModelBase getParentPlugin() {
    return (IPluginModelBase) getProperty(PROP_PARENT_PLUGIN);
  }

  public void setParentFile(IFile file) {
    setProperty(PROP_PARENT_FILE, file);
  }

  public IFile getParentFile() {
    return (IFile) getProperty(PROP_PARENT_FILE);
  }

  public void setTranslationFolder(String folder) {
    setPropertyString(PROP_TRANSLATION_FOLDER, folder);
  }

  public String getTranslationFolder() {
    return getPropertyString(PROP_TRANSLATION_FOLDER);
  }

  public void setTranlationFileName(String filename) {
    setPropertyString(PROP_TRANSLATION_FILE, filename);
  }

  public String getTranlationFileName() {
    return getPropertyString(PROP_TRANSLATION_FILE);
  }

}
