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
package org.eclipse.scout.sdk.operation.form;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.MethodCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>FormHandlerNewOperation</h3> ...
 */
public class FormHandlerNewOperation implements IOperation {
  final IType iFormHandler = TypeUtility.getType(RuntimeClasses.IFormHandler);
  final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
  // in members
  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private String m_superTypeSignature;

  // op members
  private IJavaElement m_sibling;
  private IJavaElement m_startMethodSibling;

  // out members
  private IType m_createdHandler;

  private String m_startMethodName;

  public FormHandlerNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public FormHandlerNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    m_superTypeSignature = Signature.createTypeSignature(RuntimeClasses.AbstractFormHandler, true);
  }

  @Override
  public String getOperationName() {
    return "new form handler...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("typeName is null or empty.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getDeclaringType().getCompilationUnit(), monitor);
    // create handler
    InnerTypeNewOperation formHandlerOp = new InnerTypeNewOperation(getTypeName(), getDeclaringType());
    formHandlerOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractFormHandler, true));
    formHandlerOp.setTypeModifiers(Flags.AccPublic);
    formHandlerOp.setSibling(m_sibling);
    formHandlerOp.validate();
    formHandlerOp.run(monitor, workingCopyManager);
    m_createdHandler = formHandlerOp.getCreatedType();

    // start method
    String nameKey = getTypeName();
    nameKey = nameKey.replaceFirst(SdkProperties.SUFFIX_FORM_HANDLER + "\\b", "");
    if (!StringUtility.isNullOrEmpty(getStartMethodName())) {
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(m_createdHandler.getCompilationUnit());
      IType form = TypeUtility.getAncestor(m_createdHandler, TypeFilters.getSubtypeFilter(iForm, hierarchy));
      IType superType = hierarchy.getSuperclass(form);
      IMethod methodToOverride = null;
      while (superType != null && !TypeUtility.exists(methodToOverride)) {
        methodToOverride = TypeUtility.getMethod(superType, getStartMethodName());
        superType = hierarchy.getSuperclass(superType);
      }
      if (TypeUtility.exists(methodToOverride)) {
        MethodOverrideOperation startMethodOp = new MethodOverrideOperation(form, getStartMethodName()) {
          @Override
          protected String createMethodBody(IImportValidator validator) throws JavaModelException {
            StringBuilder source = new StringBuilder();
            source.append("startInternal(new ");
            source.append(SignatureUtility.getTypeReference(Signature.createTypeSignature(getCreatedHandler().getFullyQualifiedName(), true), validator));
            source.append("());");
            return source.toString();
          }
        };
        startMethodOp.setSibling(getStartMethodSibling());
        startMethodOp.validate();
        startMethodOp.run(monitor, workingCopyManager);
      }
      else {
        MethodCreateOperation startMethodOp = new MethodCreateOperation(form, getStartMethodName()) {
          @Override
          protected String createMethodBody(IImportValidator validator) throws JavaModelException {
            StringBuilder source = new StringBuilder();
            source.append("startInternal(new ");
            source.append(SignatureUtility.getTypeReference(Signature.createTypeSignature(getCreatedHandler().getFullyQualifiedName(), true), validator));
            source.append("());");
            return source.toString();
          }
        };
        startMethodOp.setFormatSource(true);
        startMethodOp.setReturnTypeSignature(Signature.SIG_VOID);
        startMethodOp.addExceptionSignature(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
        startMethodOp.setSibling(getStartMethodSibling());

        startMethodOp.validate();
        startMethodOp.run(monitor, workingCopyManager);
      }
    }
    else {
      ScoutSdk.logWarning("could not determ start method name for handler '" + getCreatedHandler().getElementName() + "'.");
    }
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(m_createdHandler, true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public IType getCreatedHandler() {
    return m_createdHandler;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
    if (typeName != null && typeName.length() > 1) {
      String startMethodName = typeName.replaceFirst(SdkProperties.SUFFIX_FORM_HANDLER + "\\b", "");
      if (startMethodName.length() > 1) {
        startMethodName = Character.toUpperCase(startMethodName.charAt(0)) + startMethodName.substring(1);
        startMethodName = "start" + startMethodName;
      }
      m_startMethodName = startMethodName;
    }
  }

  public String getStartMethodName() {
    return m_startMethodName;
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

  public IJavaElement getStartMethodSibling() {
    return m_startMethodSibling;
  }

  public void setStartMethodSibling(IJavaElement startMethodSibling) {
    m_startMethodSibling = startMethodSibling;
  }

}
