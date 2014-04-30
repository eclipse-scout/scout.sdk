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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.jdt.type.InnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>FormHandlerNewOperation</h3> ...
 */
public class FormHandlerNewOperation extends InnerTypeNewOperation {

  private IMethod m_createdStartMethod;

  public FormHandlerNewOperation(String typeName, IType declaringType) {
    this(typeName, declaringType, true);
  }

  public FormHandlerNewOperation(String typeName, IType declaringType, boolean formatSource) {
    super(typeName, declaringType);
    setFormatSource(formatSource);
    // defaults
    setFlags(Flags.AccPublic);
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IFormHandler, getDeclaringType().getJavaProject()));
  }

  @Override
  public String getOperationName() {
    return "new form handler...";
  }

  @Override
  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    super.createType(monitor, workingCopyManager);
    // start method
    createStartMethod(getCreatedType(), monitor, workingCopyManager);
  }

  protected void createStartMethod(final IType formHandler, IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {

    // find form
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(formHandler.getCompilationUnit());
    IType form = TypeUtility.getAncestor(formHandler, TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IForm), hierarchy),
        TypeFilters.getTopLevelTypeFilter()));

    String handlerName = getElementName();

    if (TypeUtility.exists(form) && handlerName != null && handlerName.length() > 1) {
      String startMethodName = handlerName.replaceFirst(SdkProperties.SUFFIX_FORM_HANDLER + "\\b", "");
      if (startMethodName.length() > 1) {
        startMethodName = NamingUtility.ensureStartWithUpperCase(startMethodName);
        startMethodName = "start" + startMethodName;
      }

      IMethodBodySourceBuilder bodyBuilder = new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("startInternal(new ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(formHandler.getFullyQualifiedName())));
          source.append("());");
        }
      };
      IStructuredType sourceHelper = ScoutTypeUtility.createStructuredForm(form);
      IJavaElement sibling = sourceHelper.getSiblingMethodStartHandler(startMethodName);
      if (sibling == null && getCreatedType().getDeclaringType().equals(form)) {
        sibling = getCreatedType();
      }

      IMethod methodToOverride = TypeUtility.findMethodInSuperClassHierarchy(form, MethodFilters.getNameFilter(startMethodName));
      if (TypeUtility.exists(methodToOverride)) {
        MethodOverrideOperation startMethodOp = new MethodOverrideOperation(methodToOverride, form);
        startMethodOp.setMethodBodySourceBuilder(bodyBuilder);
        startMethodOp.setSibling(sibling);
        startMethodOp.validate();
        startMethodOp.run(monitor, manager);
        m_createdStartMethod = startMethodOp.getCreatedMethod();
      }
      else {
        MethodNewOperation startMethodOp = new MethodNewOperation(startMethodName, form);
        startMethodOp.setFlags(Flags.AccPublic);
        startMethodOp.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
        startMethodOp.setSibling(sibling);
        startMethodOp.setReturnTypeSignature(Signature.SIG_VOID);
        startMethodOp.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
        startMethodOp.setMethodBodySourceBuilder(bodyBuilder);
        startMethodOp.setSibling(sibling);
        startMethodOp.validate();
        startMethodOp.run(monitor, manager);
        m_createdStartMethod = startMethodOp.getCreatedMethod();
      }
    }
  }

  public IMethod getCreatedStartMethod() {
    return m_createdStartMethod;
  }
}
