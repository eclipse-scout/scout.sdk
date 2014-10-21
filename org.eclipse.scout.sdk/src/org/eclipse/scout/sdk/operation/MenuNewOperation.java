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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>MenuNewOperation</h3>
 */
public class MenuNewOperation implements IOperation {
  static final String FORM_NAME = "form";

  // in members
  private final String m_typeName;
  private final IType m_declaringType;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_formToOpen;
  private IType m_formHandler;
  private boolean m_formatSource;
  // out members
  private IType m_createdMenu;

  public MenuNewOperation(String menuName, IType declaringType) {
    this(menuName, declaringType, true);
  }

  public MenuNewOperation(String menuName, IType declaringType, boolean formatSource) {
    m_typeName = menuName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
  }

  @Override
  public void validate() {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type cannot be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation menuNewOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType());
    menuNewOp.setSuperTypeSignature(getSuperTypeSignature());
    menuNewOp.setFlags(Flags.AccPublic);
    menuNewOp.setOrderDefinitionType(TypeUtility.getType(IRuntimeClasses.IMenu));
    menuNewOp.setSibling(getSibling());
    // getConfiguredLabel method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(menuNewOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      menuNewOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }
    final boolean isNewFormHandler = getFormHandler() != null && getFormHandler().getElementName().matches("^New.*");
    final ITypeHierarchy superTypeHierarchy = ScoutTypeUtility.getSupertypeHierarchy(getDeclaringType());
    if (isNewFormHandler) {
      // getConfiguredMenuTypes
      IMethodSourceBuilder getConfiguredMenuTypes = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(menuNewOp.getSourceBuilder(), "getConfiguredMenuTypes");
      getConfiguredMenuTypes.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String collUtilityName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.CollectionUtility));
          String iMenuTypeName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.IMenuType));
          String tableMenuTypeName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.TableMenuType));
          String treeMenuTypeName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.TreeMenuType));
          String valueFieldMenuTypeName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.ValueFieldMenuType));

          source.append("return ").append(collUtilityName).append(".").append(Signature.C_GENERIC_START).append(iMenuTypeName).append(Signature.C_GENERIC_END).append(" hashSet(");
          if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITable))) {
            source.append(tableMenuTypeName).append(".EmptySpace");
          }
          else if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.ITree))) {
            source.append(treeMenuTypeName).append(".EmptySpace");
          }
          else if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IValueField))) {
            source.append(valueFieldMenuTypeName).append(".NotNull");
          }
          source.append(");");
        }
      });
      menuNewOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredMenuTypes), getConfiguredMenuTypes);
    }
    if (getFormToOpen() != null) {
      //     execAction method
      IMethodSourceBuilder execActionBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(menuNewOp.getSourceBuilder(), "execAction");
      execActionBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getDeclaringType().getCompilationUnit());
          String formTypeName = validator.getTypeName(SignatureCache.createTypeSignature(getFormToOpen().getFullyQualifiedName()));
          source.append(formTypeName + " ").append(FORM_NAME).append(" = new ").append(formTypeName).append("();").append(lineDelimiter);
          if (getFormHandler() != null) {
            IType table = TypeUtility.getAncestor(getDeclaringType(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.ITable), hierarchy));
            if (!isNewFormHandler && TypeUtility.exists(table)) {
              Set<IType> columns = ScoutTypeUtility.getPrimaryKeyColumns(table);
              for (IType col : columns) {
                // find method on form
                String colPropName = col.getElementName().replaceAll("^(.*)Column$", "$1");
                IMethod writeMethodOnForm = TypeUtility.getMethod(getFormToOpen(), "set" + colPropName);
                if (TypeUtility.exists(writeMethodOnForm)) {
                  source.append(FORM_NAME + "." + writeMethodOnForm.getElementName() + "(get" + col.getElementName() + "().getSelectedValue());").append(lineDelimiter);
                }
              }
            }
            String startMethodName = getFormHandler().getElementName().replaceAll("^(.*)Handler$", "start$1");
            IMethod startMethod = TypeUtility.getMethod(getFormToOpen(), startMethodName);
            if (TypeUtility.exists(startMethod)) {
              source.append(FORM_NAME + "." + startMethod.getElementName() + "();");
            }
          }
          else {
            source.append(ScoutUtility.getCommentBlock("start form here.")).append(lineDelimiter);
          }
          IType pageWithTable = TypeUtility.getAncestor(getDeclaringType(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IPageWithTable), hierarchy));
          if (TypeUtility.exists(pageWithTable)) {
            source.append("\n" + FORM_NAME + ".waitFor();").append(lineDelimiter);
            source.append("if (" + FORM_NAME + ".isFormStored()) {").append(lineDelimiter);
            source.append("reloadPage();").append(lineDelimiter);
            source.append("}");
          }
        }
      });
      menuNewOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execActionBuilder), execActionBuilder);
    }

    menuNewOp.setFormatSource(isFormatSource());
    menuNewOp.validate();
    menuNewOp.run(monitor, workingCopyManager);
    m_createdMenu = menuNewOp.getCreatedType();

  }

  public IType getCreatedMenu() {
    return m_createdMenu;
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

  public void setFormToOpen(IType formToOpenType) {
    m_formToOpen = formToOpenType;
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
