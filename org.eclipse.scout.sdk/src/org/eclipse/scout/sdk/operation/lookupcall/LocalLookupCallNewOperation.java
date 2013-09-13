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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class LocalLookupCallNewOperation extends PrimaryTypeNewOperation {

  public LocalLookupCallNewOperation(String lookupCallName, String packageName, IJavaProject javaProject) throws JavaModelException {
    super(lookupCallName, packageName, javaProject);

    // defaults
    setFlags(Flags.AccPublic);
    setPackageExportPolicy(ExportPolicy.AddPackage);
    setFormatSource(true);
    getCompilationUnitNewOp().setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());

  }

  @Override
  public String getOperationName() {
    return "New Local LookupCall '" + getElementName() + "'";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    // serial version uid
    addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    // execCreateLookupRows method
    IMethodSourceBuilder execCreateLookupRowsBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), "execCreateLookupRows");
    execCreateLookupRowsBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        String refLookupRow = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.LookupRow));
        String refArrayList = validator.getTypeName(SignatureCache.createTypeSignature(ArrayList.class.getName()));
        source.append(refArrayList).append("<").append(refLookupRow).append("> rows = new ").append(refArrayList).append("<").append(refLookupRow).append(">();").append(lineDelimiter);
        source.append("  ").append(ScoutUtility.getCommentBlock("create lookup rows here.")).append(lineDelimiter);
        source.append("  return rows;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execCreateLookupRowsBuilder), execCreateLookupRowsBuilder);

    super.run(monitor, workingCopyManager);

  }
}
