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
package org.eclipse.scout.sdk.operation.page;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodUpdateContentOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.InsertEdit;

public abstract class AbstractPageOperation implements IOperation {

  private static final Pattern REGEX_LIST_ADD_ENTRY_POINT = Pattern.compile("([a-zA-Z0-9\\_\\-]*)\\.add\\(.+\\)\\;", Pattern.MULTILINE);
  private static final String CHILD_PAGE_VAR_NAME = "childPage";

  private IType m_holderType;

  protected void addToHolder(IType page, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getHolderType() != null) {
      IType iOutline = TypeUtility.getType(IRuntimeClasses.IOutline);
      IType iPageWithNodes = TypeUtility.getType(IRuntimeClasses.IPageWithNodes);
      IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);

      ITypeHierarchy superTypeHierarchy = getHolderType().newSupertypeHierarchy(monitor);
      if (superTypeHierarchy.contains(iOutline)) {
        addToOutline(page, getHolderType(), monitor, workingCopyManager);
      }
      else if (superTypeHierarchy.contains(iPageWithNodes)) {
        addToPageWithNodes(page, getHolderType(), monitor, workingCopyManager);
      }
      else if (superTypeHierarchy.contains(iPageWithTable)) {
        addToPageWithTable(page, getHolderType(), monitor, workingCopyManager);
      }
    }
  }

  private String getChildPageAddSource(IType pageType, String listName, String lineDelimiter, IImportValidator validator) {
    String pageRef = validator.getTypeName(SignatureCache.createTypeSignature(pageType.getFullyQualifiedName()));
    String varName = NamingUtility.ensureStartWithLowerCase(pageType.getElementName());

    StringBuilder bodyBuilder = new StringBuilder();
    bodyBuilder.append(SdkProperties.TAB).append(SdkProperties.TAB).append(pageRef).append(" ").append(varName).append(" = new ").append(pageRef).append("();").append(lineDelimiter);
    bodyBuilder.append(SdkProperties.TAB).append(SdkProperties.TAB).append(listName).append(".add(").append(varName).append(");");

    return bodyBuilder.toString();
  }

  private void addPageToList(Document methodBody, IType pageTypeToAdd, IMethod methodToUpdate, IImportValidator validator) {
    // find entry point and list variable name
    Matcher matcher = REGEX_LIST_ADD_ENTRY_POINT.matcher(methodBody.get());
    int index = -1;
    String listVarName = null;
    while (matcher.find()) {
      index = matcher.end();
      listVarName = matcher.group(1);
    }

    // add our page if we could find an entry point.
    if (index > 0) {
      String lineDelimiter = ResourceUtility.getLineSeparator(methodBody);
      InsertEdit edit = new InsertEdit(index, lineDelimiter + getChildPageAddSource(pageTypeToAdd, listVarName, lineDelimiter, validator));
      try {
        edit.apply(methodBody);
      }
      catch (Exception e) {
        ScoutSdk.logError("Could not update method '" + methodToUpdate.getElementName() + "' in type '" + methodToUpdate.getDeclaringType().getFullyQualifiedName() + "'.", e);
      }
    }
    else {
      ScoutSdk.logWarning("Could find insert position for an additional page in method '" + methodToUpdate.getElementName() + "' in type '" + methodToUpdate.getDeclaringType().getFullyQualifiedName() + "'.");
    }
  }

  private void addToOutline(final IType pageType, IType outlineType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String methodName = "execCreateChildPages";
    IMethod childPagesMethod = TypeUtility.getMethod(outlineType, methodName);
    if (TypeUtility.exists(childPagesMethod)) {
      MethodUpdateContentOperation updateContentOp = new MethodUpdateContentOperation(childPagesMethod) {
        @Override
        protected void updateMethodBody(Document methodBody, IImportValidator validator) throws CoreException {
          addPageToList(methodBody, pageType, getMethod(), validator);
        }
      };
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(methodName, outlineType);
      overrideOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append(getChildPageAddSource(pageType, "pageList", lineDelimiter, validator));
        }
      });
      IStructuredType structuredType = ScoutTypeUtility.createStructuredOutline(outlineType);
      overrideOp.setSibling(structuredType.getSiblingMethodConfigExec(methodName));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
    }
  }

  private void addToPageWithNodes(final IType pageType, IType pageWithNodes, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String methodName = "execCreateChildPages";
    IMethod childPagesMethod = TypeUtility.getMethod(pageWithNodes, methodName);
    if (TypeUtility.exists(childPagesMethod)) {
      MethodUpdateContentOperation updateContentOp = new MethodUpdateContentOperation(childPagesMethod) {
        @Override
        protected void updateMethodBody(Document methodBody, IImportValidator validator) throws CoreException {
          addPageToList(methodBody, pageType, getMethod(), validator);
        }
      };
      updateContentOp.setFormatSource(true);
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(methodName, pageWithNodes, true);
      overrideOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String pageRef = validator.getTypeName(SignatureCache.createTypeSignature(pageType.getFullyQualifiedName()));
          String varName = NamingUtility.ensureStartWithLowerCase(pageType.getElementName());
          source.append(pageRef).append(" ").append(varName).append(" = new ").append(pageRef).append("();").append(lineDelimiter);
          source.append("pageList.add(").append(varName).append(");").append(lineDelimiter);
        }
      });
      IStructuredType structuredType = ScoutTypeUtility.createStructuredPageWithNodes(pageWithNodes);
      overrideOp.setSibling(structuredType.getSiblingMethodConfigExec(methodName));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
    }
  }

  private void addToPageWithTable(final IType pageType, final IType pageWithTable, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String methodName = "execCreateChildPage";
    IMethod childPagesMethod = TypeUtility.getMethod(pageWithTable, methodName);
    if (TypeUtility.exists(childPagesMethod)) {
      MethodUpdateContentOperation updateContentOp = new MethodUpdateContentOperation(childPagesMethod) {
        @Override
        protected void createMethodBody(StringBuilder sourceBuilder, String lineDelimiter, IImportValidator validator, String originalBody) throws JavaModelException {
          String pageRef = validator.getTypeName(SignatureCache.createTypeSignature(pageType.getFullyQualifiedName()));
          sourceBuilder.append(pageRef).append(" ").append(CHILD_PAGE_VAR_NAME).append(" = new ").append(pageRef).append("();").append(lineDelimiter);
          createPageParameterSource(pageType, pageWithTable, validator, sourceBuilder);
          sourceBuilder.append("return ").append(CHILD_PAGE_VAR_NAME).append(";");
        }
      };
      updateContentOp.setFormatSource(true);
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(methodName, pageWithTable, true);
      overrideOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String pageRef = validator.getTypeName(SignatureCache.createTypeSignature(pageType.getFullyQualifiedName()));
          source.append(pageRef).append(" ").append(CHILD_PAGE_VAR_NAME).append(" = new ").append(pageRef).append("();").append(lineDelimiter);
          createPageParameterSource(pageType, pageWithTable, validator, source);
          source.append("return ").append(CHILD_PAGE_VAR_NAME).append(";");
        }
      });
      IStructuredType structuredType = ScoutTypeUtility.createStructuredPageWithTable(pageWithTable);
      overrideOp.setSibling(structuredType.getSiblingMethodConfigExec(methodName));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
    }
  }

  private void createPageParameterSource(IType page, IType pageWithTable, IImportValidator validator, StringBuilder builder) {
    if (TypeUtility.exists(page) && TypeUtility.exists(pageWithTable)) {
      IType[] tables = ScoutTypeUtility.getTables(pageWithTable);
      if (tables != null && tables.length > 0 && TypeUtility.exists(tables[0])) {
        IType[] columns = ScoutTypeUtility.getPrimaryKeyColumns(tables[0]);
        for (IType col : columns) {
          // find method on form
          String colPropName = col.getElementName().replaceAll("^(.*)Column$", "$1");
          IMethod writeMethodOnChildPage = TypeUtility.getMethod(page, "set" + colPropName);
          if (TypeUtility.exists(writeMethodOnChildPage)) {
            builder.append(CHILD_PAGE_VAR_NAME);
            builder.append(".");
            builder.append(writeMethodOnChildPage.getElementName());
            builder.append("(getTable().get");
            builder.append(col.getElementName());
            builder.append("().getValue(row));\n");
          }
        }
      }
    }
  }

  public void setHolderType(IType holderType) {
    m_holderType = holderType;
  }

  public IType getHolderType() {
    return m_holderType;
  }
}
