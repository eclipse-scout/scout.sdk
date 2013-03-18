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
package org.eclipse.scout.sdk.operation.project.add;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.FillServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.IScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.ScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.osgi.framework.Constants;

/**
 * <h3>{@link ScoutProjectAddOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 02.03.2012
 */
public class ScoutProjectAddOperation extends ScoutProjectNewOperation {

  public final static String PROP_EXISTING_BUNDLE = "parentScoutBundle";

  private final IScoutBundle m_project;

  public ScoutProjectAddOperation(IScoutBundle element) {
    m_project = element;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_project == null) {
      throw new IllegalArgumentException("null project not allowed");
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  protected String computeExecutionEnvironment() {
    if (!m_project.isBinary()) {
      PluginModelHelper pmh = new PluginModelHelper(m_project.getProject());
      String execEnv = pmh.Manifest.getEntry(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT);
      if (execEnv != null) {
        return execEnv;
      }
    }
    return super.computeExecutionEnvironment();
  }

  @Override
  protected void putInitialProperties() {
    super.putInitialProperties();

    String name = getProperties().getProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME_POSTFIX, String.class);
    if (!StringUtility.hasText(name) || name.trim().length() < 2) {
      name = getProperties().getProperty(IScoutProjectNewOperation.PROP_PROJECT_NAME, String.class);
    }
    int lastDotPos = name.lastIndexOf('.');
    name = Character.toUpperCase(name.charAt(lastDotPos + 1)) + name.substring(lastDotPos + 2);

    getProperties().setProperty(PROP_EXISTING_BUNDLE, m_project);
    getProperties().setProperty(FillClientPluginOperation.PROP_INSTALL_CLIENT_SESSION, false);
    getProperties().setProperty(FillClientPluginOperation.PROP_INSTALL_DESKTOP_EXT, true);
    getProperties().setProperty(CreateClientPluginOperation.PROP_INSTALL_ICONS, false);
    getProperties().setProperty(CreateSharedPluginOperation.PROP_TEXT_SERVICE_NAME, name);
    getProperties().setProperty(CreateServerPluginOperation.PROP_INSTALL_HTML_RESOURCES, false);
    getProperties().setProperty(CreateServerPluginOperation.PROP_INSTALL_PRODUCTS, false);
    getProperties().setProperty(FillServerPluginOperation.PROP_INSTALL_ACCESS_CONTROL_SVC_CLASS, false);
    getProperties().setProperty(FillServerPluginOperation.PROP_INSTALL_SERVER_APP_CLASS, false);
    getProperties().setProperty(FillServerPluginOperation.PROP_INSTALL_SERVER_SESSION_CLASS, false);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    super.run(monitor, workingCopyManager);
    postProcess(monitor, workingCopyManager);
  }

  private void postProcess(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (isNodeChecked(CreateServerPluginOperation.BUNDLE_ID)) {
      // clear server plugin.xml
      String serverPluginName = getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class);
      IJavaProject server = getCreatedBundle(serverPluginName);
      PluginModelHelper pmh = new PluginModelHelper(server.getProject());
      pmh.PluginXml.removeSimpleExtension(IRuntimeClasses.EXTENSION_POINT_SERVICES, IRuntimeClasses.EXTENSION_ELEMENT_SERVICE);
      pmh.PluginXml.removeExtensionPoint(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS);
      pmh.PluginXml.removeExtensionPoint(IRuntimeClasses.EXTENSION_POINT_SERVLET_FILTERS);
      pmh.PluginXml.removeExtensionPoint("org.eclipse.core.runtime.applications");
      pmh.PluginXml.removeExtensionPoint(IRuntimeClasses.EXTENSION_POINT_PRODUCTS);
      pmh.save();
    }
    if (isNodeChecked(CreateSharedPluginOperation.BUNDLE_ID)) {
      // remove all the icon fields in the Icons class
      String sharedPluginName = getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class);
      IJavaProject shared = getCreatedBundle(sharedPluginName);
      IType icons = shared.findType(shared.getElementName() + ".Icons");
      if (TypeUtility.exists(icons)) {
        JavaElementDeleteOperation op = new JavaElementDeleteOperation();
        for (IField f : icons.getFields()) {
          if ((f.getFlags() & Flags.AccPublic) != 0) {
            op.addMember(f);
          }
        }
        op.validate();
        op.run(monitor, workingCopyManager);
      }
    }
    if (isNodeChecked(CreateClientPluginOperation.BUNDLE_ID)) {
      // register the desktop extension
      String clientPluginName = getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class);
      IJavaProject server = getCreatedBundle(clientPluginName);
      PluginModelHelper pmh = new PluginModelHelper(server.getProject());
      Map<String, String> props = new HashMap<String, String>(2);
      props.put("active", "true");
      props.put("class", clientPluginName + ".ui.desktop.DesktopExtension");
      pmh.PluginXml.addSimpleExtension(IRuntimeClasses.EXTENSION_POINT_DESKTOP_EXTENSIONS, IRuntimeClasses.EXTENSION_ELEMENT_DESKTOP_EXTENSION, props);
      pmh.save();
    }

    for (IJavaProject p : getCreatedBundlesList()) {
      String type = RuntimeBundles.getBundleType(p.getProject());
      IScoutBundle parent = m_project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(type), true);
      if (parent != null && !parent.getSymbolicName().equals(p.getElementName())) {
        PluginModelHelper pmh = new PluginModelHelper(p.getProject());
        pmh.Manifest.addDependency(parent.getSymbolicName(), true);
        pmh.save();
      }
    }
  }
}
