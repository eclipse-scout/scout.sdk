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
package org.eclipse.scout.sdk.operation.form.field.composer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.InnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>WizardStepNewOperation</h3>
 */
public class ComposerAttributeNewOperation extends InnerTypeNewOperation {

  // in member
  private INlsEntry m_nlsEntry;

  public ComposerAttributeNewOperation(String attributeName, IType declaringType) {
    this(attributeName, declaringType, true);
  }

  public ComposerAttributeNewOperation(String attributeName, IType declaringType, boolean formatSource) {
    super(attributeName, declaringType);
    setFormatSource(formatSource);

    // default values
    setFlags(Flags.AccPublic);
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IDataModelAttribute, getDeclaringType().getJavaProject()));
    setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
  }

  @Override
  public String getOperationName() {
    return "New composer attribute '" + getElementName() + "'...";
  }

  @Override
  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // serial version uid
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsTextMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsTextMethodBuilder), nlsTextMethodBuilder);
    }
    super.createType(monitor, workingCopyManager);
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }
}
