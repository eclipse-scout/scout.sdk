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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.FormHandlerNewOperation;
import org.eclipse.scout.sdk.operation.form.FormNewOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.method.MethodCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateClientPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link SingleFormTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class SingleFormTemplateOperation extends AbstractScoutProjectNewOperation {

  public final static String TEMPLATE_ID = "ID_SINGLE_FORM_TEMPLATE";

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
    String formName = "DesktopForm";

    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    final IScoutBundle client = bundleGraph.getBundle(getProperties().getProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, String.class));
    final IScoutBundle server = bundleGraph.getBundle(getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class));
    final IScoutBundle shared = bundleGraph.getBundle(getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class));

    // formdata
    ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(formName + "Data", shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES), shared);
    formDataOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractFormData));
    formDataOp.run(monitor, workingCopyManager);
    IType formData = formDataOp.getCreatedType();

    // export formdata package
    ManifestExportPackageOperation expFormDataPackage = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD, new IPackageFragment[]{formData.getPackageFragment()}, false);
    expFormDataPackage.validate();
    expFormDataPackage.run(monitor, workingCopyManager);
    String formDataSignature = SignatureCache.createTypeSignature(formData.getFullyQualifiedName());

    // form
    FormNewOperation formOp = new FormNewOperation();
    formOp.setPackage(client.getPackageName(".ui.forms"));
    formOp.setFormDataSignature(formDataSignature);
    formOp.setClientBundle(client);
    formOp.setCreateButtonCancel(false);
    formOp.setCreateButtonOk(false);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, client.getJavaProject()));
    formOp.setTypeName(formName);
    formOp.setFormatSource(false);
    formOp.run(monitor, workingCopyManager);
    final IType form = formOp.getCreatedFormType();
    workingCopyManager.reconcile(form.getCompilationUnit(), monitor);

    final ScoutIconDesc icon = client.getIconProvider().getIcon("eclipse_scout");
    if (icon != null) {
      MethodOverrideOperation iconIdOverrideOp = new MethodOverrideOperation(form, "getConfiguredIconId", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String iconRef = validator.getTypeName(SignatureCache.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName()));
          return "  return " + iconRef + "." + icon.getConstantField().getElementName() + ";";
        }
      };
      iconIdOverrideOp.run(monitor, workingCopyManager);
    }

    MethodOverrideOperation displayHintOp = new MethodOverrideOperation(form, "getConfiguredDisplayHint", false);
    displayHintOp.setSimpleBody("  return DISPLAY_HINT_VIEW;");
    displayHintOp.validate();
    displayHintOp.run(monitor, workingCopyManager);

    ConfigPropertyMethodUpdateOperation askIfSaveNeedMethodOp = new ConfigPropertyMethodUpdateOperation(form, "getConfiguredAskIfNeedSave", "  return false;", false);
    askIfSaveNeedMethodOp.run(monitor, workingCopyManager);

    ConfigPropertyMethodUpdateOperation displayViewIdMethodOp = new ConfigPropertyMethodUpdateOperation(form, "getConfiguredDisplayViewId", "  return VIEW_ID_CENTER;", false);
    displayViewIdMethodOp.run(monitor, workingCopyManager);

    WellformScoutTypeOperation wellformFormOp = new WellformScoutTypeOperation(form, true);
    wellformFormOp.validate();
    wellformFormOp.run(monitor, workingCopyManager);

    // process service
    ProcessServiceNewOperation serviceOp = new ProcessServiceNewOperation();
    serviceOp.setClientServiceRegistryBundles(new IScoutBundle[]{client});
    serviceOp.setServerServiceRegistryBundles(new IScoutBundle[]{server});
    serviceOp.setServiceImplementationBundle(server);
    serviceOp.setServicePackageName(server.getDefaultPackage(IDefaultTargetPackage.SERVER_SERVICES));
    serviceOp.setServiceImplementationName("DesktopService");
    serviceOp.setServiceInterfaceBundle(shared);
    serviceOp.setServiceInterfacePackageName(shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SERVICES));
    serviceOp.setPermissionPackageName(shared.getDefaultPackage(IDefaultTargetPackage.SHARED_SECURITY));
    serviceOp.setServiceInterfaceName("IDesktopService");
    serviceOp.run(monitor, workingCopyManager);
    final IType serviceInterface = serviceOp.getCreatedServiceInterface();

    // process service load method
    if (TypeUtility.exists(serviceInterface)) { /* service interface can be null on a client only project */
      workingCopyManager.reconcile(serviceInterface.getCompilationUnit(), monitor);
      MethodCreateOperation loadInterfaceOp = new MethodCreateOperation(serviceInterface, "load");
      loadInterfaceOp.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
      loadInterfaceOp.setMethodFlags(Flags.AccInterface);
      loadInterfaceOp.setReturnTypeSignature(SignatureCache.createTypeSignature(formData.getFullyQualifiedName()));
      loadInterfaceOp.setParameterNames(new String[]{"formData"});
      loadInterfaceOp.setParameterSignatures(new String[]{SignatureCache.createTypeSignature(formData.getFullyQualifiedName())});
      loadInterfaceOp.setFormatSource(false);
      loadInterfaceOp.validate();
      loadInterfaceOp.run(monitor, workingCopyManager);

      workingCopyManager.reconcile(serviceOp.getCreatedServiceImplementation().getCompilationUnit(), monitor);
      MethodCreateOperation loadMethodOp = new MethodCreateOperation(serviceOp.getCreatedServiceImplementation(), "load");
      loadMethodOp.addAnnotation(AnnotationCreateOperation.OVERRIDE_OPERATION);
      loadMethodOp.setMethodFlags(Flags.AccPublic);
      loadMethodOp.setReturnTypeSignature(SignatureCache.createTypeSignature(formData.getFullyQualifiedName()));
      loadMethodOp.setParameterNames(new String[]{"formData"});
      loadMethodOp.setParameterSignatures(new String[]{SignatureCache.createTypeSignature(formData.getFullyQualifiedName())});
      loadMethodOp.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
      loadMethodOp.setSimpleBody(ScoutUtility.getCommentAutoGeneratedMethodStub() + "\nreturn formData;\n");
      loadMethodOp.setFormatSource(false);
      loadMethodOp.validate();
      loadMethodOp.run(monitor, workingCopyManager);
    }

    // form handler
    FormHandlerNewOperation handlerOp = new FormHandlerNewOperation(form);
    handlerOp.setTypeName("ViewHandler");
    handlerOp.setFormatSource(false);
    handlerOp.validate();
    handlerOp.run(monitor, workingCopyManager);
    IType handler = handlerOp.getCreatedHandler();

    workingCopyManager.reconcile(handler.getCompilationUnit(), monitor);

    final String fqnFormData = formData.getFullyQualifiedName();
    MethodOverrideOperation execLoadOp = new MethodOverrideOperation(handler, "execLoad", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder builder = new StringBuilder();
        if (TypeUtility.exists(serviceInterface)) { /* service interface can be null on a client only project */
          String servicesRef = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.SERVICES));
          String serviceRef = validator.getTypeName(SignatureCache.createTypeSignature(serviceInterface.getFullyQualifiedName()));
          String formDataRef = validator.getTypeName(SignatureCache.createTypeSignature(fqnFormData));
          builder.append(serviceRef + " service = " + servicesRef + ".getService(" + serviceRef + ".class);\n");
          builder.append(formDataRef + " formData = new " + formDataRef + "();\n");
          builder.append("exportFormData(formData);\n");
          builder.append("formData = service.load(formData);\n");
          builder.append("importFormData(formData);\n");
        }
        return builder.toString();
      }
    };
    execLoadOp.validate();
    execLoadOp.run(monitor, workingCopyManager);

    // formdata
    FormDataUpdateOperation formDataUpdateOp = new FormDataUpdateOperation(form);
    formDataUpdateOp.run(monitor, workingCopyManager);

    // desktop
    IType desktopType = TypeUtility.getType(client.getDefaultPackage(IDefaultTargetPackage.CLIENT_DESKTOP) + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation(desktopType, "execOpened", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          sourceBuilder.append("// desktop form\n");
          String treeFormRef = validator.getTypeName(SignatureCache.createTypeSignature(form.getFullyQualifiedName()));
          sourceBuilder.append(treeFormRef + " desktopForm = new " + treeFormRef + "();\n");
          ScoutIconDesc icn = client.getIconProvider().getIcon("eclipse_scout");
          if (icn != null) {
            String iconsRef = validator.getTypeName(SignatureCache.createTypeSignature(icn.getConstantField().getDeclaringType().getFullyQualifiedName()));
            sourceBuilder.append("desktopForm.setIconId(" + iconsRef + "." + icn.getConstantField().getElementName() + ");\n");
          }
          sourceBuilder.append("desktopForm.startView();");
          return sourceBuilder.toString();
        }
      };
      execOpenOp.setSibling(desktopType.getType("FileMenu"));
      execOpenOp.validate();
      execOpenOp.run(monitor, workingCopyManager);
    }
  }
}
