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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutSignature;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

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
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation menuNewOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    menuNewOp.setSuperTypeSignature(getSuperTypeSignature());
    menuNewOp.setTypeModifiers(Flags.AccPublic);
    menuNewOp.setOrderDefinitionType(ScoutSdk.getType(RuntimeClasses.IMenu));
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

    if (getFormHandler() != null && getFormHandler().getElementName().matches("^New.*")) {
      MethodOverrideOperation getConfiguredEmptySpaceActionOp = new MethodOverrideOperation(getCreatedMenu(), "getConfiguredEmptySpaceAction");
      getConfiguredEmptySpaceActionOp.setSimpleBody("return true;");
      getConfiguredEmptySpaceActionOp.validate();
      getConfiguredEmptySpaceActionOp.run(monitor, workingCopyManager);

      MethodOverrideOperation getConfiguredSingleSelectionActionOp = new MethodOverrideOperation(getCreatedMenu(), "getConfiguredSingleSelectionAction");
      getConfiguredSingleSelectionActionOp.setSimpleBody("return false;");
      getConfiguredSingleSelectionActionOp.validate();
      getConfiguredSingleSelectionActionOp.run(monitor, workingCopyManager);
    }
    createExecActionMethod(m_createdMenu, monitor, workingCopyManager);

    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedMenu(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  private IMethod createExecActionMethod(IType menu, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    IMethod execActionMethod = null;
    if (getFormToOpen() != null) {
      MethodOverrideOperation execActionOp = new MethodOverrideOperation(menu, "execAction", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getCreatedMenu().getCompilationUnit());

          StringBuilder sourceBuilder = new StringBuilder();
          String formTypeName = ScoutSignature.getTypeReference(Signature.createTypeSignature(getFormToOpen().getFullyQualifiedName(), true), getDeclaringType(), validator);
          sourceBuilder.append(formTypeName + " " + FORM_NAME + " = new " + formTypeName + "();\n");
          if (getFormHandler() != null) {
            IType table = TypeUtility.getAncestor(getCreatedMenu(), TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.ITable), hierarchy));
            if (TypeUtility.exists(table)) {
              createFormParameterSource(getFormToOpen(), getFormHandler(), table, hierarchy, sourceBuilder, validator);
              createStartFormSource(getFormToOpen(), getFormHandler(), table, sourceBuilder, validator);
            }
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

  private void createFormParameterSource(IType form, IType formHandler, IType table, ITypeHierarchy hierarchy, StringBuilder builder, IImportValidator validator) {
    IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.IColumn), hierarchy));
    for (IType col : columns) {
      try {
        IMethod primKeyMethod = TypeUtility.getMethod(col, "getConfiguredPrimaryKey");
        if (TypeUtility.exists(primKeyMethod)) {
          String isPrimaryKey = PropertyMethodSourceUtilities.getMethodReturnValue(primKeyMethod);
          if (Boolean.valueOf(isPrimaryKey)) {
            // find method on form
            String colPropName = col.getElementName().replaceAll("^(.*)Column$", "$1");
            IMethod writeMethodOnForm = TypeUtility.getMethod(form, "set" + colPropName);
            if (TypeUtility.exists(writeMethodOnForm)) {
              builder.append(FORM_NAME + "." + writeMethodOnForm.getElementName() + "(get" + col.getElementName() + "().getSelectedValue());\n");
            }
          }
        }
      }
      catch (CoreException e) {
        ScoutSdk.logError("cold not parse column '" + col.getFullyQualifiedName() + "' for primary key.", e);
      }
    }
  }

  private void createStartFormSource(IType form, IType formHandler, IType table, StringBuilder builder, IImportValidator validator) {
    String startMethodName = formHandler.getElementName().replaceAll("^(.*)Handler$", "start$1");
    IMethod startMethod = TypeUtility.getMethod(form, startMethodName);
    if (TypeUtility.exists(startMethod)) {
      builder.append(FORM_NAME + "." + startMethod.getElementName() + "();");
    }
  }

  private void createReloadPage(IType form, IType formHandler, ITypeHierarchy hierarchy, StringBuilder builder, IImportValidator validator) {
    IType pageWithTable = TypeUtility.getAncestor(getDeclaringType(), TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.IPageWithTable), hierarchy));
    if (TypeUtility.exists(pageWithTable)) {
      builder.append("\n" + FORM_NAME + ".waitFor();\n");
      builder.append("if (" + FORM_NAME + ".isFormStored()) {\n");
      builder.append("reloadPage();\n");
      builder.append("}");
    }
  }

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
