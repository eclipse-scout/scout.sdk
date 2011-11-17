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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.form.FormHandlerNewOperation;
import org.eclipse.scout.sdk.operation.form.FormNewOperation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.method.MethodCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link SingleFormTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public class SingleFormTemplateOperation implements IOperation {

  IScoutProject m_scoutProject;

  public SingleFormTemplateOperation(IScoutProject scoutProject) {
    m_scoutProject = scoutProject;
  }

  @Override
  public String getOperationName() {
    return "Applay single form tempalte...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getScoutProject() == null) {
      throw new IllegalArgumentException("scout project must not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String formName = "DesktopForm";
    IScoutBundle sharedBundle = getScoutProject().getSharedBundle();

    ScoutTypeNewOperation formDataOp = new ScoutTypeNewOperation(formName + "Data", sharedBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS), sharedBundle);
    formDataOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormData, true));
    formDataOp.run(monitor, workingCopyManager);
    final IType formData = formDataOp.getCreatedType();
    ManifestExportPackageOperation expFormDataPackage = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD, new IPackageFragment[]{formData.getPackageFragment()}, false);
    expFormDataPackage.validate();
    expFormDataPackage.run(monitor, workingCopyManager);
    String formDataSignature = Signature.createTypeSignature(formData.getFullyQualifiedName(), true);
    // form
    FormNewOperation formOp = new FormNewOperation();
    formOp.setFormDataSignature(formDataSignature);
    formOp.setClientBundle(getScoutProject().getClientBundle());
    formOp.setCreateButtonCancel(false);
    formOp.setCreateButtonOk(false);
    formOp.setSuperType(Signature.createTypeSignature(RuntimeClasses.AbstractForm, true));
    formOp.setTypeName("DesktopForm");
    formOp.run(monitor, workingCopyManager);
    final IType form = formOp.getCreatedFormType();
    workingCopyManager.reconcile(form.getCompilationUnit(), monitor);

    final ScoutIconDesc icon = getScoutProject().getIconProvider().getIcon("eclipse_scout");
    if (icon != null) {
      MethodOverrideOperation iconIdOverrideOp = new MethodOverrideOperation(form, "getConfiguredIconId", true) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String iconRef = validator.getSimpleTypeRef(Signature.createTypeSignature(icon.getConstantField().getDeclaringType().getFullyQualifiedName(), true));
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
    serviceOp.setClientServiceRegistryBundles(new IScoutBundle[]{getScoutProject().getClientBundle()});
    serviceOp.setServerServiceRegistryBundles(new IScoutBundle[]{getScoutProject().getServerBundle()});
    serviceOp.setServiceImplementationBundle(getScoutProject().getServerBundle());
    serviceOp.setServiceImplementationName("DesktopProcessService");
    serviceOp.setServiceInterfaceBundle(sharedBundle);
    serviceOp.setServiceInterfaceName("IDesktopProcessService");
    serviceOp.run(monitor, workingCopyManager);
    final IType serviceInterface = serviceOp.getCreatedServiceInterface();

    // process service load method
    if (TypeUtility.exists(serviceInterface)) { /* service interface can be null on a client only project */
      workingCopyManager.reconcile(serviceInterface.getCompilationUnit(), monitor);
      MethodCreateOperation loadInterfaceOp = new MethodCreateOperation(serviceInterface, "load");
      loadInterfaceOp.addExceptionSignature(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
      loadInterfaceOp.setMethodFlags(Flags.AccInterface);
      loadInterfaceOp.setReturnTypeSignature(Signature.createTypeSignature(formData.getFullyQualifiedName(), true));
      loadInterfaceOp.setParameterNames(new String[]{"formData"});
      loadInterfaceOp.setParameterSignatures(new String[]{Signature.createTypeSignature(formData.getFullyQualifiedName(), true)});
      loadInterfaceOp.setFormatSource(true);
      loadInterfaceOp.validate();
      loadInterfaceOp.run(monitor, workingCopyManager);

      workingCopyManager.reconcile(serviceOp.getCreatedServiceImplementation().getCompilationUnit(), monitor);
      MethodCreateOperation loadMethodOp = new MethodCreateOperation(serviceOp.getCreatedServiceImplementation(), "load");
      loadMethodOp.addAnnotation(AnnotationCreateOperation.OVERRIDE_OPERATION);
      loadMethodOp.setMethodFlags(Flags.AccPublic);
      loadMethodOp.setReturnTypeSignature(Signature.createTypeSignature(formData.getFullyQualifiedName(), true));
      loadMethodOp.setParameterNames(new String[]{"formData"});
      loadMethodOp.setParameterSignatures(new String[]{Signature.createTypeSignature(formData.getFullyQualifiedName(), true)});
      loadMethodOp.addExceptionSignature(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
      loadMethodOp.setSimpleBody(ScoutUtility.getCommentAutoGeneratedMethodStub() + "\nreturn formData;\n");
      loadMethodOp.setFormatSource(true);
      loadMethodOp.validate();
      loadMethodOp.run(monitor, workingCopyManager);
    }

    // form handler
    FormHandlerNewOperation handlerOp = new FormHandlerNewOperation(form);
    handlerOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormHandler, true));
    handlerOp.setTypeName("ViewHandler");
    handlerOp.setFormatSource(true);
    handlerOp.validate();
    handlerOp.run(monitor, workingCopyManager);
    IType handler = handlerOp.getCreatedHandler();

    workingCopyManager.reconcile(handler.getCompilationUnit(), monitor);

    MethodOverrideOperation execLoadOp = new MethodOverrideOperation(handler, "execLoad", true) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder builder = new StringBuilder();
        if (TypeUtility.exists(serviceInterface)) { /* service interface can be null on a client only project */
          String servicesRef = validator.getSimpleTypeRef(Signature.createTypeSignature(RuntimeClasses.SERVICES, true));
          String serviceRef = validator.getSimpleTypeRef(Signature.createTypeSignature(serviceInterface.getFullyQualifiedName(), true));
          String formDataRef = validator.getSimpleTypeRef(Signature.createTypeSignature(formData.getFullyQualifiedName(), true));
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
    IType desktopType = TypeUtility.getType(getScoutProject().getClientBundle().getBundleName() + IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_DESKTOP + ".Desktop");
    if (TypeUtility.exists(desktopType)) {
      MethodOverrideOperation execOpenOp = new MethodOverrideOperation(desktopType, "execOpened") {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          sourceBuilder.append("// dektop form\n");
          String treeFormRef = validator.getSimpleTypeRef(Signature.createTypeSignature(form.getFullyQualifiedName(), true));
          sourceBuilder.append(treeFormRef + " desktopForm = new " + treeFormRef + "();\n");
          ScoutIconDesc icn = getScoutProject().getIconProvider().getIcon("eclipse_scout");
          if (icn != null) {
            String iconsRef = validator.getSimpleTypeRef(Signature.createTypeSignature(icn.getConstantField().getDeclaringType().getFullyQualifiedName(), true));
            sourceBuilder.append("desktopForm.setIconId(" + iconsRef + "." + icn.getConstantField().getElementName() + ");\n");
          }
          sourceBuilder.append("desktopForm.startView();");
          return sourceBuilder.toString();
        }
      };
      execOpenOp.setSibling(desktopType.getType("FileMenu"));
      execOpenOp.setFormatSource(true);
      execOpenOp.validate();
      execOpenOp.run(monitor, workingCopyManager);
    }
  }

  /**
   * @return the scoutProject
   */
  public IScoutProject getScoutProject() {
    return m_scoutProject;
  }

}
