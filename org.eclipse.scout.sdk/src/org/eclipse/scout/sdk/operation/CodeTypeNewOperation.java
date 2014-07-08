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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CodeTypeNewOperation extends PrimaryTypeNewOperation {

  private String m_nextCodeId;
  private INlsEntry m_nlsEntry;

  /**
   * @param typeName
   * @param packageName
   * @param project
   * @throws JavaModelException
   */
  public CodeTypeNewOperation(String typeName, String packageName, IJavaProject project) throws JavaModelException {
    super(typeName, packageName, project);
    // defaults
    setFlags(Flags.AccPublic);
    setPackageExportPolicy(ExportPolicy.ADD_PACKAGE);
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    setFormatSource(true);
  }

  @Override
  public String getOperationName() {
    return "New Code Type '" + getElementName() + "'...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // serial version UID
    addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    // field ID
    FieldSourceBuilder idFieldBuilder = new FieldSourceBuilder("ID") {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        super.createSource(source, lineDelimiter, ownerProject, validator);
        if (StringUtility.isNullOrEmpty(getNextCodeId())) {
          source.append(ScoutUtility.getCommentBlock("Auto-generated value"));
        }
      }
    };

    if (StringUtility.isNullOrEmpty(getNextCodeId())) {
      idFieldBuilder.setValue("null");
    }
    else {
      idFieldBuilder.setValue(getNextCodeId());
    }
    idFieldBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesFieldCommentBuilder());
    idFieldBuilder.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);

    IMethodSourceBuilder getIdSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), "getId");
    getIdSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return ID;"));

    idFieldBuilder.setSignature(getIdSourceBuilder.getReturnTypeSignature());
    addFieldSourceBuilder(idFieldBuilder);
    // constructor
    IMethodSourceBuilder constructorSourceBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    constructorSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodCommentBuilder());
    constructorSourceBuilder.addExceptionSignature(SignatureCache.createTypeSignature(IRuntimeClasses.ProcessingException));
    constructorSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorSourceBuilder), constructorSourceBuilder);

    // nls
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsSourceBuilder), nlsSourceBuilder);
    }
    // get id method

    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getIdSourceBuilder), getIdSourceBuilder);

    super.run(monitor, workingCopyManager);
  }

  public void setNextCodeId(String nextCodeId) {
    m_nextCodeId = nextCodeId;
  }

  public String getNextCodeId() {
    return m_nextCodeId;
  }

  public void setNlsEntry(INlsEntry nlsKey) {
    m_nlsEntry = nlsKey;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

}
