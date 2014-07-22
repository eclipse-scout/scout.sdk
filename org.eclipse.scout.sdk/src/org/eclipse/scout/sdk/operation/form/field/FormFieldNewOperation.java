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
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormFieldNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 06.03.2013
 */
public class FormFieldNewOperation extends OrderedInnerTypeNewOperation {

  private IMethod m_createdFieldGetterMethod;

  /**
   * @param name
   * @param declaringType
   */
  public FormFieldNewOperation(String name, IType declaringType) {
    super(name, declaringType);
    setOrderDefinitionType(TypeUtility.getType(IRuntimeClasses.IFormField));
    setFlags(Flags.AccPublic);
  }

  @Override
  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.createType(monitor, workingCopyManager);
    createFormFieldGetter(monitor, workingCopyManager);
  }

  protected void createFormFieldGetter(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {

    IType declaringType = TypeUtility.getType(getDeclaringType().getFullyQualifiedName());
    IType createdType = TypeUtility.getType(getCreatedType().getFullyQualifiedName());
    setCreatedType(createdType);
    setDeclaringType(declaringType);

    // find form
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy();
    IType form = TypeUtility.getAncestor(declaringType, TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IForm), hierarchy),
        TypeFilters.getPrimaryTypeFilter()));

    if (TypeUtility.exists(form)) {
      InnerTypeGetterCreateOperation getterMethodOp = new InnerTypeGetterCreateOperation(createdType, form, false /* do not pass the formatting in -> performance */);
      IStructuredType sourceHelper = ScoutTypeUtility.createStructuredForm(form, hierarchy);
      IJavaElement sibling = sourceHelper.getSiblingMethodFieldGetter("get" + getElementName());
      if (sibling == null && createdType.getDeclaringType().equals(form)) {
        sibling = createdType;
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
