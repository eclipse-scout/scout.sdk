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
package org.eclipse.scout.sdk.rap.operations.project.template;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.template.OutlineTemplateOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateMobileClientPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link MobileDesktopExtensionInstallOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 08.03.2013
 */
public class MobileDesktopExtensionInstallOperation extends AbstractScoutProjectNewOperation {

  public final static String MOBILE_HOME_FORM_NAME = "MobileHomeForm";

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID) &&
        (OutlineTemplateOperation.TEMPLATE_ID.equals(getTemplateName()) || SingleFormTemplateOperation.TEMPLATE_ID.equals(getTemplateName()));
  }

  @Override
  public void init() {
  }

  @Override
  public String getOperationName() {
    return "Apply mobile desktop extension...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    IScoutBundle mobileClient = bundleGraph.getBundle(getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class));

    String pck = mobileClient.getDefaultPackage(IDefaultTargetPackage.CLIENT_DESKTOP);
    final String desktopExtensionName = "DesktopExtension";
    ScoutTypeNewOperation desktopExtensionOp = new ScoutTypeNewOperation(desktopExtensionName, pck, mobileClient) {
      @Override
      protected void createContent(StringBuilder source, IImportValidator validator) {
        source.append("private " + MOBILE_HOME_FORM_NAME + " m_homeForm;\n");
        source.append("private boolean m_active;\n\n");
        source.append("public " + desktopExtensionName + "() {\n");
        source.append("  setActive(!UserAgentUtility.isDesktopDevice());\n");
        source.append("}\n\n");
        source.append("public boolean isActive() {\n");
        source.append("  return m_active;\n");
        source.append("}\n\n");
        source.append("public void setActive(boolean active) {\n");
        source.append("  m_active = active;\n");
        source.append("}");
      }
    };
    desktopExtensionOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IDesktopExtension, mobileClient));
    desktopExtensionOp.validate();
    desktopExtensionOp.run(monitor, workingCopyManager);
    IType desktopExtension = desktopExtensionOp.getCreatedType();

    // execGuiAttached
    MethodOverrideOperation execGuiAttached = new MethodOverrideOperation(desktopExtension, "execGuiAttached", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("if (!isActive()) {\n");
        body.append("  return super.execGuiAttached();\n");
        body.append("}\n\n");
        body.append("if (m_homeForm == null) {\n");
        body.append("  m_homeForm = new " + MOBILE_HOME_FORM_NAME + "();\n");
        body.append("  m_homeForm.startView();\n");
        body.append("}\n");
        body.append("return ContributionCommand.Continue;");
        return body.toString();
      }
    };
    execGuiAttached.validate();
    execGuiAttached.run(monitor, workingCopyManager);

    // execGuiDetached
    MethodOverrideOperation execGuiDetached = new MethodOverrideOperation(desktopExtension, "execGuiDetached", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder body = new StringBuilder();
        body.append("if (!isActive()) {\n");
        body.append("  return super.execGuiDetached();\n");
        body.append("}\n\n");
        body.append("if (m_homeForm != null) {\n");
        body.append("  m_homeForm.doClose();\n");
        body.append("}\n");
        body.append("return ContributionCommand.Continue;");
        return body.toString();
      }
    };
    execGuiDetached.validate();
    execGuiDetached.run(monitor, workingCopyManager);

    registerDesktopExtension(desktopExtension, mobileClient, monitor, workingCopyManager);
  }

  private void registerDesktopExtension(IType desktopExtension, IScoutBundle mobileClient, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    HashMap<String, String> properties = new HashMap<String, String>(2);
    properties.put("active", "true");
    properties.put("class", desktopExtension.getFullyQualifiedName());
    PluginModelHelper pmh = new PluginModelHelper(mobileClient.getSymbolicName());
    pmh.PluginXml.addSimpleExtension(RuntimeClasses.EXTENSION_POINT_DESKTOP_EXTENSIONS, RuntimeClasses.EXTENSION_ELEMENT_DESKTOP_EXTENSION, properties);
    pmh.save();
  }
}
