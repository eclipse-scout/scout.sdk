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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>MenuNewOperation</h3> ...
 */
public class MenuNewOperation implements IOperation {
  static final String FORM_NAME = "form";

  // in members
  private final IType m_declaringType;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_formToOpen;
  private IType m_formHandler;
  private boolean m_formatSource;
  // out members
  private IType m_createdMenu;

  public MenuNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public MenuNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation menuNewOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    menuNewOp.setSuperTypeSignature(getSuperTypeSignature());
    menuNewOp.setTypeModifiers(Flags.AccPublic);
    menuNewOp.setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IMenu));
    menuNewOp.setSibling(getSibling());
    menuNewOp.validate();
    menuNewOp.run(monitor, workingCopyManager);
    m_createdMenu = menuNewOp.getCreatedType();
    if (getNlsEntry() != null) {
      // text
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedMenu(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }

    boolean isNewFormHandler = getFormHandler() != null && getFormHandler().getElementName().matches("^New.*");
    if (isNewFormHandler) {
      MethodOverrideOperation getConfiguredEmptySpaceActionOp = new MethodOverrideOperation(getCreatedMenu(), "getConfiguredEmptySpaceAction");
      getConfiguredEmptySpaceActionOp.setSimpleBody("return true;");
      getConfiguredEmptySpaceActionOp.validate();
      getConfiguredEmptySpaceActionOp.run(monitor, workingCopyManager);

      MethodOverrideOperation getConfiguredSingleSelectionActionOp = new MethodOverrideOperation(getCreatedMenu(), "getConfiguredSingleSelectionAction");
      getConfiguredSingleSelectionActionOp.setSimpleBody("return false;");
      getConfiguredSingleSelectionActionOp.validate();
      getConfiguredSingleSelectionActionOp.run(monitor, workingCopyManager);
    }
    createExecActionMethod(m_createdMenu, isNewFormHandler, monitor, workingCopyManager);

    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedMenu(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  private IMethod createExecActionMethod(IType menu, final boolean isNewFormHandler, IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    IMethod execActionMethod = null;
    if (getFormToOpen() != null) {
      MethodOverrideOperation execActionOp = new MethodOverrideOperation(menu, "execAction", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getCreatedMenu().getCompilationUnit());

          StringBuilder sourceBuilder = new StringBuilder();
          String formTypeName = SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(getFormToOpen().getFullyQualifiedName()), getDeclaringType(), validator);
          sourceBuilder.append(formTypeName + " " + FORM_NAME + " = new " + formTypeName + "();\n");
          if (getFormHandler() != null) {
            IType table = TypeUtility.getAncestor(getCreatedMenu(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.ITable), hierarchy));
            if (!isNewFormHandler && TypeUtility.exists(table)) {
              createFormParameterSource(getFormToOpen(), getFormHandler(), table, sourceBuilder, validator);
            }
            createStartFormSource(getFormToOpen(), getFormHandler(), sourceBuilder, validator);
          }
          else {
            sourceBuilder.append(ScoutUtility.getCommentBlock("start form here.") + "\n");
          }
          createReloadPage(getFormToOpen(), getFormHandler(), hierarchy, sourceBuilder, validator);
          return sourceBuilder.toString();
        }
      };
      execActionOp.validate();
      execActionOp.run(monitor, manager);
      execActionMethod = execActionOp.getCreatedMethod();
    }
    return execActionMethod;
  }

  public IType getCreatedMenu() {
    return m_createdMenu;
  }

  private void createFormParameterSource(IType form, IType formHandler, IType table, StringBuilder builder, IImportValidator validator) {
    IType[] columns = ScoutTypeUtility.getPrimaryKeyColumns(table);
    for (IType col : columns) {
      // find method on form
      String colPropName = col.getElementName().replaceAll("^(.*)Column$", "$1");
      IMethod writeMethodOnForm = TypeUtility.getMethod(form, "set" + colPropName);
      if (TypeUtility.exists(writeMethodOnForm)) {
        builder.append(FORM_NAME + "." + writeMethodOnForm.getElementName() + "(get" + col.getElementName() + "().getSelectedValue());\n");
      }
    }
  }

  private void createStartFormSource(IType form, IType formHandler, StringBuilder builder, IImportValidator validator) {
    String startMethodName = formHandler.getElementName().replaceAll("^(.*)Handler$", "start$1");
    IMethod startMethod = TypeUtility.getMethod(form, startMethodName);
    if (TypeUtility.exists(startMethod)) {
      builder.append(FORM_NAME + "." + startMethod.getElementName() + "();");
    }
  }

  private void createReloadPage(IType form, IType formHandler, ITypeHierarchy hierarchy, StringBuilder builder, IImportValidator validator) {
    IType pageWithTable = TypeUtility.getAncestor(getDeclaringType(), TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IPageWithTable), hierarchy));
    if (TypeUtility.exists(pageWithTable)) {
      builder.append("\n" + FORM_NAME + ".waitFor();\n");
      builder.append("if (" + FORM_NAME + ".isFormStored()) {\n");
      builder.append("reloadPage();\n");
      builder.append("}");
    }
  }

  @Override
  public String getOperationName() {
    return "New menu";
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

  public IType getFormToOpen() {
    return m_formToOpen;
  }

  public void setFormToOpen(IType bcType) {
    m_formToOpen = bcType;

  }

  public void setFormHandler(IType formHandler) {
    m_formHandler = formHandler;
  }

  public IType getFormHandler() {
    return m_formHandler;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }
}
