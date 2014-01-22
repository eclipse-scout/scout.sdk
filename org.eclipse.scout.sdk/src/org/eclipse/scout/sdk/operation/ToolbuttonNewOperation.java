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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>ToolbuttonNewOperation</h3> ...
 */
public class ToolbuttonNewOperation extends OrderedInnerTypeNewOperation {

  private INlsEntry m_nlsEntry;

  public ToolbuttonNewOperation(String toolbuttonElementName, IType declaringType) {
    this(toolbuttonElementName, declaringType, false);
  }

  public ToolbuttonNewOperation(String toolbuttonElementName, IType declaringType, boolean formatSource) {
    super(toolbuttonElementName, declaringType, formatSource);
    // defaults
    setOrderDefinitionType(TypeUtility.getType(IRuntimeClasses.IToolButton));
    setFlags(Flags.AccPublic);
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IToolButton, declaringType.getJavaProject()));
  }

  @Override
  public String getOperationName() {
    return "new tool button [" + getElementName() + "]...";
  }

  @Override
  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextGetterBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsTextGetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      getSourceBuilder().addMethodSourceBuilder(nlsTextGetterBuilder);
    }
    super.createType(monitor, workingCopyManager);
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }
}
