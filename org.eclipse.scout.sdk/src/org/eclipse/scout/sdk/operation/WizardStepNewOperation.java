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
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>WizardStepNewOperation</h3>
 */
public class WizardStepNewOperation implements IOperation {

  // in member
  private final IType m_declaringType;
  private final String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IType m_form;
  private IType m_formHandler;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out member
  private IType m_createdWizardStep;

  public WizardStepNewOperation(String typeName, IType declaringType) {
    this(typeName, declaringType, false);
  }

  public WizardStepNewOperation(String typeName, IType declaringType, boolean formatSource) {
    m_typeName = typeName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IWizardStep, getDeclaringType().getJavaProject());
  }

  @Override
  public String getOperationName() {
    return "New Wizard step";
  }

  @Override
  public void validate() {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IType iWizardStep = TypeUtility.getType(IRuntimeClasses.IWizardStep);
    OrderedInnerTypeNewOperation wizardStepOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    wizardStepOp.setFlags(Flags.AccPublic);
    wizardStepOp.setSuperTypeSignature(getSuperTypeSignature());
    wizardStepOp.setOrderDefinitionType(iWizardStep);
    wizardStepOp.setSibling(getSibling());
    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(wizardStepOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsTextMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      wizardStepOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsTextMethodBuilder), nlsTextMethodBuilder);
    }
    // form
    if (getForm() != null) {
      String superTypeFqn = SignatureUtility.getFullyQualifiedName(getSuperTypeSignature());
      if (CompareUtility.equals(superTypeFqn, IRuntimeClasses.AbstractWizardStep)) {
        // update generic in supertype signature
        StringBuilder superTypeSigBuilder = new StringBuilder(superTypeFqn);
        superTypeSigBuilder.append(Signature.C_GENERIC_START).append(getForm().getFullyQualifiedName()).append(Signature.C_GENERIC_END);
        wizardStepOp.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeSigBuilder.toString()));
        // execActivate method
        IMethodSourceBuilder execActivateBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(wizardStepOp.getSourceBuilder(), "execActivate");
        execActivateBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            String formRefName = validator.getTypeName(SignatureCache.createTypeSignature(getForm().getFullyQualifiedName()));
            source.append(formRefName).append(" form = getForm();").append(lineDelimiter);
            source.append("if(form == null){").append(lineDelimiter);
            source.append("form = new ").append(formRefName).append("();").append(lineDelimiter);
            source.append("// start the form by executing the form handler").append(lineDelimiter);
            if (getFormHandler() != null) {
              source.append("form.startWizardStep(this,").append(validator.getTypeName(SignatureCache.createTypeSignature(getFormHandler().getFullyQualifiedName()))).append(".class);").append(lineDelimiter);
            }
            else {
              source.append(ScoutUtility.getCommentBlock("start the form (e.g. form.startWizardStep(this, MyForm.ModifyHandler.class);")).append(lineDelimiter);
            }
            source.append("// Register the form on the step").append(lineDelimiter);
            source.append("setForm(form);").append(lineDelimiter);
            source.append("}").append(lineDelimiter);
            source.append("// Set the form on the wizard").append(lineDelimiter);
            source.append("// This will automatically display it as inner form of the wizard container form").append(lineDelimiter);
            source.append("getWizard().setWizardForm(form);");
          }
        });
        wizardStepOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execActivateBuilder), execActivateBuilder);
        // execDeactivate method
        IMethodSourceBuilder execDeactivateBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(wizardStepOp.getSourceBuilder(), "execDeactivate");
        execDeactivateBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
          @Override
          public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
            String formRefName = validator.getTypeName(SignatureCache.createTypeSignature(getForm().getFullyQualifiedName()));
            source.append("// Save the form if the user clicks next").append(lineDelimiter);
            source.append("if (stepKind == STEP_NEXT){").append(lineDelimiter);
            source.append(formRefName).append(" form = getForm();").append(lineDelimiter);
            source.append("if (form != null) {").append(lineDelimiter);
            source.append("form.doSave();").append(lineDelimiter);
            source.append("}").append(lineDelimiter);
            source.append("}");
          }
        });
        wizardStepOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execDeactivateBuilder), execDeactivateBuilder);

      }
    }

    wizardStepOp.validate();
    wizardStepOp.run(monitor, workingCopyManager);
    setCreatedWizardStep(wizardStepOp.getCreatedType());
    // getter on declaring type
    InnerTypeGetterCreateOperation getterOp = new InnerTypeGetterCreateOperation(getCreatedWizardStep(), getDeclaringType(), false);
    IStructuredType structuredType = ScoutTypeUtility.createStructuredWizard(getDeclaringType());
    getterOp.setSibling(structuredType.getSiblingMethodFieldGetter(getterOp.getElementName()));
    getterOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        String wizardStepRef = validator.getTypeName(SignatureCache.createTypeSignature(getCreatedWizardStep().getFullyQualifiedName()));
        source.append("return getStep(" + wizardStepRef + ".class);");
      }
    });
    getterOp.validate();
    getterOp.run(monitor, workingCopyManager);

    if (m_formatSource) {
      JavaElementFormatOperation foramtOp = new JavaElementFormatOperation(getDeclaringType().getCompilationUnit(), true);
      foramtOp.validate();
      foramtOp.run(monitor, workingCopyManager);
    }
  }

  protected void setCreatedWizardStep(IType createdWizardStep) {
    m_createdWizardStep = createdWizardStep;
  }

  public IType getCreatedWizardStep() {
    return m_createdWizardStep;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setForm(IType form) {
    m_form = form;
  }

  public IType getForm() {
    return m_form;
  }

  public void setFormHandler(IType formHandler) {
    m_formHandler = formHandler;
  }

  public IType getFormHandler() {
    return m_formHandler;
  }
}
