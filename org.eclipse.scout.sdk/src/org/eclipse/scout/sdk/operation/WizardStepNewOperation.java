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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>WizardStepNewOperation</h3> ...
 */
public class WizardStepNewOperation implements IOperation {

  final IType iWizardStep = TypeUtility.getType(RuntimeClasses.IWizardStep);

  // in member
  private final IType m_declaringType;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out member
  private IType m_createdWizardStep;

  public WizardStepNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public WizardStepNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = Signature.createTypeSignature(RuntimeClasses.AbstractWizardStep, true);
  }

  @Override
  public String getOperationName() {
    return "New Wizard step";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation wizardStepOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    wizardStepOp.setOrderDefinitionType(iWizardStep);
    wizardStepOp.setSibling(getSibling());
    wizardStepOp.setSuperTypeSignature(getSuperTypeSignature());
    wizardStepOp.setTypeModifiers(Flags.AccPublic);
    wizardStepOp.validate();
    wizardStepOp.run(monitor, workingCopyManager);
    m_createdWizardStep = wizardStepOp.getCreatedType();
    // getter on declaring type
    InnerTypeGetterCreateOperation getterOp = new InnerTypeGetterCreateOperation(getCreatedWizardStep(), getDeclaringType(), true) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        String wizardStepRef = validator.getSimpleTypeRef(Signature.createTypeSignature(getField().getFullyQualifiedName(), true));
        StringBuilder source = new StringBuilder();
        source.append("return getStep(" + wizardStepRef + ".class);");
        return source.toString();
      }
    };
    getterOp.validate();
    getterOp.run(monitor, workingCopyManager);

    // nls entry
    if (getNlsEntry() != null) {
      // text
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedWizardStep(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TITLE, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }
    if (m_formatSource) {
      JavaElementFormatOperation foramtOp = new JavaElementFormatOperation(getCreatedWizardStep(), true);
      foramtOp.validate();
      foramtOp.run(monitor, workingCopyManager);
    }
  }

  public void setCreatedWizardStep(IType createdWizardStep) {
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

  public void setTypeName(String typeName) {
    m_typeName = typeName;
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

}
