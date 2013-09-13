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
package org.eclipse.scout.sdk.operation.form.field;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormFieldNewOperationNew}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 06.03.2013
 */
public class FormFieldNewOperationNew extends OrderedInnerTypeNewOperation {

  private IMethod m_createdFieldGetterMethod;

  /**
   * @param name
   * @param declaringType
   */
  public FormFieldNewOperationNew(String name, IType declaringType) {
    super(name, declaringType);
    setOrderDefinitionType(TypeUtility.getType(RuntimeClasses.IFormField));
    setFlags(Flags.AccPublic);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    super.run(monitor, workingCopyManager);
    createFormFieldGetter(monitor, workingCopyManager);
  }

  protected void createFormFieldGetter(IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    // find form
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(getDeclaringType().getCompilationUnit());
    IType form = TypeUtility.getAncestor(getDeclaringType(), TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getTopLevelTypeFilter()));

    if (TypeUtility.exists(form)) {
      InnerTypeGetterCreateOperation getterMethodOp = new InnerTypeGetterCreateOperation(getCreatedType(), form, isFormatSource());
      IStructuredType sourceHelper = ScoutTypeUtility.createStructuredForm(form);
      IJavaElement sibling = sourceHelper.getSiblingMethodFieldGetter("get" + getElementName());
      if (sibling == null && getCreatedType().getDeclaringType().equals(form)) {
        sibling = getCreatedType();
      }
      getterMethodOp.setSibling(sibling);
      getterMethodOp.validate();
      getterMethodOp.run(monitor, manager);
      m_createdFieldGetterMethod = getterMethodOp.getCreatedMethod();
    }
  }

  public IMethod getCreatedFieldGetterMethod() {
    return m_createdFieldGetterMethod;
  }

}
