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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>FormHandlerNewOperation</h3> ...
 */
public class OutlineToolbuttonNewOperation extends ToolbuttonNewOperation {

  private IType m_outlineType;
  private boolean m_fomatSource;

  public OutlineToolbuttonNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public OutlineToolbuttonNewOperation(IType declaringType, boolean formatSource) {
    super(declaringType, false);
    m_fomatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "new outline tool button...";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    if (TypeUtility.exists(getOutlineType())) {
      ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(getCreatedToolButton(), false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          String outlineRef = validator.getSimpleTypeRef(Signature.createTypeSignature(getOutlineType().getFullyQualifiedName(), true));
          sourceBuilder.append("super(Desktop.this, " + outlineRef + ".class);");
          return sourceBuilder.toString();
        }
      };
      constructorOp.setMethodFlags(Flags.AccPublic);
      constructorOp.validate();
      constructorOp.run(monitor, workingCopyManager);
    }
    if (m_fomatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedToolButton(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  @Override
  public INlsEntry getNlsEntry() {
    return null;
  }

  public void setOutlineType(IType outlineType) {
    m_outlineType = outlineType;
  }

  public IType getOutlineType() {
    return m_outlineType;
  }

  @Override
  public boolean isFormatSource() {
    return m_fomatSource;
  }

  @Override
  public void setFormatSource(boolean formatSource) {
    m_fomatSource = formatSource;
  }

}
