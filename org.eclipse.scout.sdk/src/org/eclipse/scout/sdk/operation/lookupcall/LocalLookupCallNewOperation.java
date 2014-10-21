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
package org.eclipse.scout.sdk.operation.lookupcall;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class LocalLookupCallNewOperation extends PrimaryTypeNewOperation {

  public LocalLookupCallNewOperation(String lookupCallName, String packageName, IJavaProject javaProject) throws JavaModelException {
    super(lookupCallName, packageName, javaProject);

    // defaults
    setFlags(Flags.AccPublic);
    setPackageExportPolicy(ExportPolicy.ADD_PACKAGE);
    setFormatSource(true);
    getCompilationUnitNewOp().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
  }

  @Override
  public String getOperationName() {
    return "New Local LookupCall '" + getElementName() + "'";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    // serial version uid
    addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());

    if (TypeUtility.getSupertypeHierarchy(TypeUtility.getTypeBySignature(getSuperTypeSignature())).contains(TypeUtility.getType(IRuntimeClasses.CodeLookupCall))) {
      // CodeLookupCalls have different content

      IMethodSourceBuilder constructor = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName(), Flags.AccPublic);
      constructor.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("super(null); ").append(ScoutUtility.getCommentBlock("Change parameter to the desired code type class."));
        }
      });
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(constructor), constructor);
    }
    else {
      // execCreateLookupRows method
      IMethodSourceBuilder execCreateLookupRowsBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), "execCreateLookupRows");
      execCreateLookupRowsBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          String refLookupRow = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.ILookupRow));
          String refArrayList = validator.getTypeName(SignatureCache.createTypeSignature(ArrayList.class.getName()));
          String refList = validator.getTypeName(SignatureCache.createTypeSignature(List.class.getName()));

          String refGenericType = null;
          String[] args = Signature.getTypeArguments(getSuperTypeSignature());
          if (args != null && args.length > 0) {
            refGenericType = validator.getTypeName(args[0]);
          }
          else {
            refGenericType = validator.getTypeName(SignatureCache.createTypeSignature(Object.class.getName()));
          }

          source.append(refList).append(Signature.C_GENERIC_START).append("? extends ").append(refLookupRow).append(Signature.C_GENERIC_START).append(refGenericType).append(Signature.C_GENERIC_END).append(Signature.C_GENERIC_END).append(" rows = new ");
          source.append(refArrayList).append(Signature.C_GENERIC_START).append(refLookupRow).append(Signature.C_GENERIC_START).append(refGenericType).append(Signature.C_GENERIC_END).append(Signature.C_GENERIC_END).append("();").append(lineDelimiter);
          source.append("  ").append(ScoutUtility.getCommentBlock("Create lookup rows here.")).append(lineDelimiter);
          source.append("  return rows;");
        }
      });
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execCreateLookupRowsBuilder), execCreateLookupRowsBuilder);
    }

    super.run(monitor, workingCopyManager);
  }
}
