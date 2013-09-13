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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
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
public class LocalLookupCallNewOperation implements IOperation {
  // in members
  private String m_lookupCallName;
  private String m_packageName;
  private IJavaProject m_javaProject;
  private String m_lookupCallSuperTypeSignature;
  private boolean m_formatSource;

  //out members
  private IType m_createdLookupCall;

  public LocalLookupCallNewOperation(String lookupCallName, String packageName, IJavaProject javaProject) {
    m_lookupCallName = lookupCallName;
    m_packageName = packageName;
    m_javaProject = javaProject;

  }

  @Override
  public String getOperationName() {
    return "New Local LookupCall '" + getLookupCallName() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      throw new IllegalArgumentException("package can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    // lookup call
    PrimaryTypeNewOperation lookupCallOp = new PrimaryTypeNewOperation(getLookupCallName(), getPackageName(), getJavaProject());
    lookupCallOp.setFlags(Flags.AccPublic);
    lookupCallOp.setSuperTypeSignature(getLookupCallSuperTypeSignature());
    lookupCallOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    // serial version uid
    lookupCallOp.addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    // execCreateLookupRows method
    IMethodSourceBuilder execCreateLookupRowsBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(lookupCallOp.getSourceBuilder(), "execCreateLookupRows");
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
    lookupCallOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodExecKey(execCreateLookupRowsBuilder), execCreateLookupRowsBuilder);
    lookupCallOp.setFormatSource(isFormatSource());
    lookupCallOp.validate();
    lookupCallOp.run(monitor, workingCopyManager);
    m_createdLookupCall = lookupCallOp.getCreatedType();

  }

  public void setJavaProject(IJavaProject javaProject) {
    m_javaProject = javaProject;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }

  public String getLookupCallName() {
    return m_lookupCallName;
  }

  public void setLookupCallName(String lookupCallName) {
    m_lookupCallName = lookupCallName;
  }

  public void setLookupCallSuperTypeSignature(String lookupCallSuperTypeSignature) {
    m_lookupCallSuperTypeSignature = lookupCallSuperTypeSignature;
  }

  public String getLookupCallSuperTypeSignature() {
    return m_lookupCallSuperTypeSignature;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public IType getCreatedLookupCall() {
    return m_createdLookupCall;
  }
}
