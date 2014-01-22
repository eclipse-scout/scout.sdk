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
package org.eclipse.scout.sdk.operation.project.template;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.form.FormNewOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceMethod;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;

/**
 * <h3>{@link SingleFormTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class SingleFormTemplateOperation extends AbstractScoutProjectNewOperation {

  public static final String TEMPLATE_ID = "ID_SINGLE_FORM_TEMPLATE";

  public static final String FORM_NAME = "DesktopForm";

  @Override
  public String getOperationName() {
    return "Apply single form template...";
  }

  @Override
  public boolean isRelevant() {
    return TEMPLATE_ID.equals(getTemplateName());
  }

  @Override
  public void init() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    final IScoutBundle client = bundleGraph.getBundle(getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class));
    final IScoutBundle server = bundleGraph.getBundle(getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class));
    final IScoutBundle shared = bundleGraph.getBundle(getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class));

    if (shared == null || client == null) {
      // the projects could not be found. maybe the target platform could not be applied successfully
      ScoutSdk.logWarning("Single Form Template could not applied because the scout bundles could not be found. Check that the target platform is valid and contains the scout runtime.");
      return;
    }

    // form data
    PrimaryTypeNewOperation formDataOp = new PrimaryTypeNewOperation(FORM_NAME + "Data", shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES), shared.getJavaProject());
    formDataOp.setFlags(Flags.AccPublic);
    formDataOp.setSuperTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractFormData));
    formDataOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    formDataOp.validate();
    formDataOp.run(monitor, workingCopyManager);
    IType formData = formDataOp.getCreatedType();

    final String formDataSignature = SignatureCache.createTypeSignature(formData.getFullyQualifiedName());

    // process service
    final IType serviceInterface;
    if (server != null) {
      serviceInterface = createProcessService(client, shared, server, formData, monitor, workingCopyManager);
    }
    else {
      serviceInterface = null;
    }

    // form
    FormNewOperation formOp = new FormNewOperation(FORM_NAME, client.getPackageName(".ui.forms"), client.getJavaProject());
    formOp.setFormDataSignature(formDataSignature);
    formOp.setCreateButtonCancel(false);
    formOp.setCreateButtonOk(false);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IForm, client.getJavaProject()));
    formOp.setFormatSource(true);
    // getConfiguredIconId method
    final ScoutIconDesc icon = client.getIconProvider().getIcon("eclipse_scout");
    if (icon != null) {
      IMethodSourceBuilder getConfiguredIconSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(formOp.getSourceBuilder(), "getConfiguredIconId");
      getConfiguredIconSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String iconRef = validator.getTypeName(SignatureCache.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName()));
          source.append("  return ").append(iconRef).append(".").append(icon.getConstantField().getElementName()).append(";");
        }
      });
      formOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredIconSourceBuilder), getConfiguredIconSourceBuilder);
    }
    // getConfiguredDisplayHint method
    IMethodSourceBuilder getConfiguredDisplayHintBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(formOp.getSourceBuilder(), "getConfiguredDisplayHint");
    getConfiguredDisplayHintBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return DISPLAY_HINT_VIEW;"));
    formOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredDisplayHintBuilder), getConfiguredDisplayHintBuilder);

    // getConfiguredAskIfNeedSave method
    IMethodSourceBuilder getConfiguredAskIfNeedSaveBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(formOp.getSourceBuilder(), "getConfiguredAskIfNeedSave");
    getConfiguredAskIfNeedSaveBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return false;"));
    formOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredAskIfNeedSaveBuilder), getConfiguredAskIfNeedSaveBuilder);

    // getConfiguredDisplayViewId method
    IMethodSourceBuilder getConfiguredDisplayViewIdBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(formOp.getSourceBuilder(), "getConfiguredDisplayViewId");
    getConfiguredDisplayViewIdBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return VIEW_ID_CENTER;"));
    formOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredDisplayViewIdBuilder), getConfiguredDisplayViewIdBuilder);

    // form handler
    ITypeSourceBuilder viewHandlerBuilder = new TypeSourceBuilder("ViewHandler");
    viewHandlerBuilder.setFlags(Flags.AccPublic);
    viewHandlerBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IFormHandler, client.getJavaProject()));
    formOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormHandlerKey(viewHandlerBuilder), viewHandlerBuilder);

    // execLoad method
    if (server != null && serviceInterface != null) {
      IMethodSourceBuilder execLoadSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(viewHandlerBuilder, "execLoad");
      execLoadSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String serviceInterfaceName = validator.getTypeName(SignatureCache.createTypeSignature(serviceInterface.getFullyQualifiedName()));
          source.append(serviceInterfaceName).append(" service = ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.SERVICES))).append(".getService(").append(serviceInterfaceName).append(".class);").append(lineDelimiter);
          String formDataRef = validator.getTypeName(formDataSignature);
          source.append(formDataRef).append(" formData = new ").append(formDataRef).append("();").append(lineDelimiter);
          source.append("exportFormData(formData);").append(lineDelimiter);
          source.append("formData = service.load(formData);").append(lineDelimiter);
          source.append("importFormData(formData);").append(lineDelimiter);
        }
      });
      viewHandlerBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execLoadSourceBuilder), execLoadSourceBuilder);
    }

    // start view method
    final String handlerFqn = formOp.getPackageName() + "." + formOp.getElementName() + "." + viewHandlerBuilder.getElementName();
    IMethodSourceBuilder startHandlerMethodBuilder = new MethodSourceBuilder("start" + SdkProperties.TYPE_NAME_VIEW_HANDLER_PREFIX);
    startHandlerMethodBuilder.setFlags(Flags.AccPublic);
    startHandlerMethodBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    startHandlerMethodBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    startHandlerMethodBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    startHandlerMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("startInternal(new ").append(validator.getTypeName(SignatureCache.createTypeSignature(handlerFqn))).append("());");
      }
    });
    formOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodStartFormKey(startHandlerMethodBuilder), startHandlerMethodBuilder);

    formOp.run(monitor, workingCopyManager);
    final IType form = formOp.getCreatedType();
    workingCopyManager.reconcile(form.getCompilationUnit(), monitor);

    // form data
    FormDataDtoUpdateOperation formDataUpdateOp = new FormDataDtoUpdateOperation(form);
    formDataOp.validate();
    formDataUpdateOp.run(monitor, workingCopyManager);

    // desktop
    IType desktopType = TypeUtility.getType(client.getDefaultPackage(IDefaultTargetPackage.CLIENT_DESKTOP) + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation("execOpened", desktopType, false);
      execOpenOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("//If it is a mobile or tablet device, the DesktopExtension in the mobile plugin takes care of starting the correct forms.\n");
          source.append("if (!").append(validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.UserAgentUtility)));
          source.append(".isDesktopDevice()) {").append(lineDelimiter);
          source.append("  return;").append(lineDelimiter);
          source.append("}").append(lineDelimiter);
          String treeFormRef = validator.getTypeName(SignatureCache.createTypeSignature(form.getFullyQualifiedName()));
          source.append(treeFormRef).append(" desktopForm = new ").append(treeFormRef).append("();").append(lineDelimiter);
          ScoutIconDesc icn = client.getIconProvider().getIcon("eclipse_scout");
          if (icn != null) {
            String iconsRef = validator.getTypeName(SignatureCache.createTypeSignature(icn.getConstantField().getDeclaringType().getFullyQualifiedName()));
            source.append("desktopForm.setIconId(").append(iconsRef).append(".").append(icn.getConstantField().getElementName()).append(");").append(lineDelimiter);
          }
          source.append("desktopForm.startView();");
        }
      });
      execOpenOp.setSibling(desktopType.getType("FileMenu"));
      execOpenOp.validate();
      execOpenOp.run(monitor, workingCopyManager);
    }
  }

  private IType createProcessService(IScoutBundle client, IScoutBundle shared, IScoutBundle server, IType formData, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws IllegalArgumentException, CoreException {
    ProcessServiceNewOperation serviceOp = new ProcessServiceNewOperation("DesktopService");
    serviceOp.addProxyRegistrationProject(client.getJavaProject());
    serviceOp.addServiceRegistration(new ServiceRegistrationDescription(server.getJavaProject()));
    serviceOp.setImplementationProject(server.getJavaProject());
    serviceOp.setImplementationPackageName(server.getDefaultPackage(IDefaultTargetPackage.SERVER_SERVICES));
    serviceOp.setInterfaceProject(shared.getJavaProject());
    serviceOp.setInterfacePackageName(shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES));
    // load method
    ServiceMethod loadMethod = new ServiceMethod("load", serviceOp.getInterfacePackageName() + "." + serviceOp.getInterfaceName());
    String formDataSignature = SignatureCache.createTypeSignature(formData.getFullyQualifiedName());
    loadMethod.setReturnTypeSignature(formDataSignature);
    loadMethod.addParameter(new MethodParameter("formData", formDataSignature));
    loadMethod.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append(ScoutUtility.getCommentAutoGeneratedMethodStub()).append(lineDelimiter);
        source.append("return formData;");
      }
    });
    serviceOp.addServiceMethodBuilder(loadMethod);
    serviceOp.validate();
    serviceOp.run(monitor, workingCopyManager);

    IType serviceInterface = serviceOp.getCreatedServiceInterface();
    return serviceInterface;

  }

}
