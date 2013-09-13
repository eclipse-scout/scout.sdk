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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link OutlineToolbuttonNewOperation}</h3> ...
 * 
 * @author aho
 * @since 3.9.0 05.04.2013
 */
public class OutlineToolbuttonNewOperation extends ToolbuttonNewOperation {

  private IType m_outlineType;

  public OutlineToolbuttonNewOperation(String outlineButtonElementName, IType declaringType) {
    this(outlineButtonElementName, declaringType, false);
  }

  public OutlineToolbuttonNewOperation(String outlineButtonElementName, IType declaringType, boolean formatSource) {
    super(outlineButtonElementName, declaringType, false);
    setSuperTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractOutlineViewButton));

  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getOutlineType())) {
      throw new IllegalArgumentException("Outline type does not exist!");
    }

    super.validate();
  }

  @Override
  public String getOperationName() {
    return "new outline tool button...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (TypeUtility.exists(getOutlineType())) {
      IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
      constructorBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("super(Desktop.this, ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(getOutlineType().getFullyQualifiedName())));
          source.append(".class);");
        }
      });
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    }
    super.run(monitor, workingCopyManager);
  }

  public void setOutlineType(IType outlineType) {
    m_outlineType = outlineType;
  }

  public IType getOutlineType() {
    return m_outlineType;
  }

}
