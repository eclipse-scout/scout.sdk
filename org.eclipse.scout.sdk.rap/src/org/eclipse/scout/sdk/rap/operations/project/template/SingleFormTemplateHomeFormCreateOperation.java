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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.template.SingleFormTemplateOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateMobileClientPluginOperation;
import org.eclipse.scout.sdk.rap.operations.project.CreateUiRapPluginOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;

/**
 * <h3>{@link SingleFormTemplateHomeFormCreateOperation}</h3>
 *
 * @author Matthias Villiger
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
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    IScoutBundle mobileClient = bundleGraph.getBundle(getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class));
    IScoutBundle client = bundleGraph.getBundle(getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class));
    String desktopFormFqn = client.getPackageName(".ui.forms") + "." + SingleFormTemplateOperation.FORM_NAME;

    createHomeForm(mobileClient, TypeUtility.getType(desktopFormFqn), monitor, workingCopyManager);
  }

  private void createHomeForm(IScoutBundle mobileClient, IType desktopForm, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String packageName = mobileClient.getPackageName(".ui.forms");
    PrimaryTypeNewOperation homeFormOp = new PrimaryTypeNewOperation("MobileHomeForm", packageName, mobileClient.getJavaProject());
    homeFormOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    homeFormOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    homeFormOp.setFlags(Flags.AccPublic);
    homeFormOp.setSuperTypeSignature(SignatureCache.createTypeSignature(SignatureCache.createTypeSignature(desktopForm.getFullyQualifiedName())));

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(homeFormOp.getElementName());
    constructorBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    constructorBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    constructorBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // LogoutButton type
    ITypeSourceBuilder logoutButtonBuilder = new TypeSourceBuilder("LogoutButton");
    logoutButtonBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IButton, mobileClient.getJavaProject()));
    logoutButtonBuilder.setFlags(Flags.AccPublic);
    logoutButtonBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(SdkProperties.ORDER_ANNOTATION_VALUE_STEP));
    homeFormOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormFieldKey(logoutButtonBuilder, SdkProperties.ORDER_ANNOTATION_VALUE_STEP), logoutButtonBuilder);

    // getConfiguredLabel method
    IMethodSourceBuilder getConfiguredLabelBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(logoutButtonBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
    getConfiguredLabelBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(mobileClient.getNlsProject().getEntry("Logoff")));
    logoutButtonBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelBuilder), getConfiguredLabelBuilder);
    // execClickAction method
    IMethodSourceBuilder execClickActionBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(logoutButtonBuilder, "execClickAction");
    execClickActionBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append(validator.getTypeName(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.ClientJob")));
        source.append(".getCurrentSession().stopSession();");
      }
    });
    logoutButtonBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execClickActionBuilder), execClickActionBuilder);
    logoutButtonBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createInjectFieldTo(desktopForm.getElementName() + ".MainBox.class"));
    // end LogoutButton type

    // LogOutButton getter
    IMethodSourceBuilder getLogoutButtonBuilder = MethodSourceBuilderFactory.createFieldGetterSourceBuilder(SignatureCache.createTypeSignature(homeFormOp.getPackageName() + "." + homeFormOp.getElementName() + "." + logoutButtonBuilder.getElementName()));
    homeFormOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodFormFieldGetterKey(getLogoutButtonBuilder), getLogoutButtonBuilder);

    homeFormOp.setFormatSource(true);
    homeFormOp.validate();
    homeFormOp.run(monitor, workingCopyManager);
  }
}
