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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.MethodUpdateContentOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.InsertEdit;

public abstract class AbstractPageOperation implements IOperation {

  final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);
  final IType iPage = TypeUtility.getType(RuntimeClasses.IPage);
  final IType iPageWithNodes = TypeUtility.getType(RuntimeClasses.IPageWithNodes);
  final IType iPageWithTable = TypeUtility.getType(RuntimeClasses.IPageWithTable);

  private IType m_holderType;

  protected void addToHolder(IType page, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getHolderType() != null) {
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

  private void addToOutline(final IType pageType, IType outlineType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String methodName = "execCreateChildPages";
    IMethod childPagesMethod = TypeUtility.getMethod(outlineType, methodName);
    if (TypeUtility.exists(childPagesMethod)) {
      MethodUpdateContentOperation updateContentOp = new MethodUpdateContentOperation(childPagesMethod) {
        @Override
        protected void updateMethodBody(Document methodBody, IImportValidator validator) throws CoreException {
          Matcher matcher = Pattern.compile("([a-zA-Z0-9\\_\\-]*)\\.add\\(.+\\)\\;", Pattern.MULTILINE).matcher(methodBody.get());
          int index = -1;
          String listName = null;
          while (matcher.find()) {
            index = matcher.end();
            listName = matcher.group(1);
          }
          if (index > 0) {
            InsertEdit edit = new InsertEdit(index, "\n" + listName + ".add(new " + validator.getTypeName(Signature.createTypeSignature(pageType.getFullyQualifiedName(), true)) + "());");
            try {
              edit.apply(methodBody);
            }
            catch (Exception e) {
              ScoutSdk.logError("could not update method '" + getMethod().getElementName() + "' in type '" + getMethod().getDeclaringType().getFullyQualifiedName() + "'.", e);
            }
          }
          else {
            ScoutSdk.logWarning("could find insert position for an additional page in method '" + getMethod().getElementName() + "' in type '" + getMethod().getDeclaringType().getFullyQualifiedName() + "'.");
          }
        }
      };
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(outlineType, methodName) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String pageRef = validator.getTypeName(Signature.createTypeSignature(pageType.getFullyQualifiedName(), true));
          String varName = Character.toLowerCase(pageType.getElementName().charAt(0)) + pageType.getElementName().substring(1);
          StringBuilder bodyBuilder = new StringBuilder();
          bodyBuilder.append(pageRef + " " + varName + " = new " + pageRef + "();\n" + SdkProperties.TAB + "pageList.add(" + varName + ");\n");
          return bodyBuilder.toString();
        }
      };
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
        protected String createMethodBody(String originalBody, IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder(originalBody);
          String pageRef = validator.getTypeName(Signature.createTypeSignature(pageType.getFullyQualifiedName(), true));
          String varName = Character.toLowerCase(pageType.getElementName().charAt(0)) + pageType.getElementName().substring(1);
          sourceBuilder.append(pageRef + " " + varName + " = new " + pageRef + "();\n" + SdkProperties.TAB + "pageList.add(" + varName + ");\n");
          return sourceBuilder.toString();
        }
      };
      updateContentOp.setFormatSource(true);
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(pageWithNodes, methodName) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String pageRef = validator.getTypeName(Signature.createTypeSignature(pageType.getFullyQualifiedName(), true));
          String varName = Character.toLowerCase(pageType.getElementName().charAt(0)) + pageType.getElementName().substring(1);
          StringBuilder bodyBuilder = new StringBuilder();
          bodyBuilder.append(pageRef + " " + varName + " = new " + pageRef + "();\n" + SdkProperties.TAB + "pageList.add(" + varName + ");\n");
          return bodyBuilder.toString();
        }
      };
      IStructuredType structuredType = ScoutTypeUtility.createStructuredPageWithNodes(pageWithNodes);
      overrideOp.setSibling(structuredType.getSiblingMethodConfigExec(methodName));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
    }
  }

  private void addToPageWithTable(final IType pageType, IType pageWithTable, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String methodName = "execCreateChildPage";
    IMethod childPagesMethod = TypeUtility.getMethod(pageWithTable, methodName);
    if (TypeUtility.exists(childPagesMethod)) {
      MethodUpdateContentOperation updateContentOp = new MethodUpdateContentOperation(childPagesMethod) {
        @Override
        protected String createMethodBody(String originalBody, IImportValidator validator) throws JavaModelException {
          StringBuilder b = new StringBuilder();
          String pageRef = validator.getTypeName(Signature.createTypeSignature(pageType.getFullyQualifiedName(), true));
          b.append(pageRef + " childPage=new " + pageRef + "();\n");
          b.append(ScoutUtility.getCommentBlock("propagate the selected row to the childPage.") + "\n");
          b.append("return childPage;\n");
          return b.toString();
        }
      };
      updateContentOp.setFormatSource(true);
      updateContentOp.validate();
      updateContentOp.run(monitor, workingCopyManager);
    }
    else {
      MethodOverrideOperation overrideOp = new MethodOverrideOperation(pageWithTable, methodName) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          String pageRef = validator.getTypeName(Signature.createTypeSignature(pageType.getFullyQualifiedName(), true));
          StringBuilder b = new StringBuilder();
          b.append(pageRef + " childPage=new " + pageRef + "();\n");
          b.append(ScoutUtility.getCommentBlock("propagate the selected row to the childPage.") + "\n");
          b.append("return childPage;\n");
          return b.toString();
        }
      };
      IStructuredType structuredType = ScoutTypeUtility.createStructuredPageWithTable(pageWithTable);
      overrideOp.setSibling(structuredType.getSiblingMethodConfigExec(methodName));
      overrideOp.validate();
      overrideOp.run(monitor, workingCopyManager);
    }

  }

  public void setHolderType(IType holderType) {
    m_holderType = holderType;
  }

  public IType getHolderType() {
    return m_holderType;
  }
}
