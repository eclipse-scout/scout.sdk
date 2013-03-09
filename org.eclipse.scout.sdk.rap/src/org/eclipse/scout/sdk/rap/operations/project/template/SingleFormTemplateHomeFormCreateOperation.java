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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.annotations.InjectFieldTo;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.field.ButtonFieldNewOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateMobileClientPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link SingleFormTemplateHomeFormCreateOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 08.03.2013
 */
public class SingleFormTemplateHomeFormCreateOperation extends AbstractScoutProjectNewOperation {

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID) && SingleFormTemplateOperation.TEMPLATE_ID.equals(getTemplateName());
  }

  @Override
  public void init() {
  }

  @Override
  public String getOperationName() {
    return "Apply single form template...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    IScoutBundle mobileClient = bundleGraph.getBundle(getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class));
    IScoutBundle client = bundleGraph.getBundle(getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class));
    String desktopFormFqn = client.getPackageName(".ui.forms") + "." + SingleFormTemplateOperation.FORM_NAME;

    createHomeForm(mobileClient, TypeUtility.getType(desktopFormFqn), monitor, workingCopyManager);
  }

  private void createHomeForm(IScoutBundle mobileClient, IType desktopForm, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutTypeNewOperation homeFormOp = new ScoutTypeNewOperation(MobileDesktopExtensionInstallOperation.MOBILE_HOME_FORM_NAME,
        mobileClient.getPackageName(".ui.forms"), mobileClient);
    homeFormOp.setSuperTypeSignature(SignatureCache.createTypeSignature(desktopForm.getFullyQualifiedName()));
    homeFormOp.validate();
    homeFormOp.run(monitor, workingCopyManager);
    IType homeForm = homeFormOp.getCreatedType();

    // constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(homeForm, false);
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    constructorOp.setSimpleBody("  super();");
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);

    // logoff button
    ButtonFieldNewOperation logoutButtonOp = new ButtonFieldNewOperation(homeForm, false);
    logoutButtonOp.setNlsEntry(mobileClient.getNlsProject().getEntry("Logoff"));
    logoutButtonOp.setTypeName("LogoutButton");
    logoutButtonOp.validate();
    logoutButtonOp.run(monitor, workingCopyManager);
    IType logoutButton = logoutButtonOp.getCreatedButton();

    // execclickaction
    MethodOverrideOperation execClickAction = new MethodOverrideOperation(logoutButton, "execClickAction", false);
    execClickAction.setSimpleBody("ClientJob.getCurrentSession().stopSession();");
    execClickAction.validate();
    execClickAction.run(monitor, workingCopyManager);

    // InjectFieldTo annotation
    AnnotationCreateOperation injectFieldTo = new AnnotationCreateOperation(logoutButton, SignatureCache.createTypeSignature(InjectFieldTo.class.getName()));
    injectFieldTo.addParameter(desktopForm.getElementName() + ".MainBox.class");
    injectFieldTo.validate();
    injectFieldTo.run(monitor, workingCopyManager);
  }
}
