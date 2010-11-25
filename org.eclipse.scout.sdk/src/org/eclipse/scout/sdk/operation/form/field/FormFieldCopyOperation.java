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

import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.method.FieldGetterCreateOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 *
 */
public class FormFieldCopyOperation extends AbstractCopyInnerTypeOperation {
  final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  final IType iCompositeField = ScoutSdk.getType(RuntimeClasses.ICompositeField);

  public FormFieldCopyOperation(String copyName, IType fieldToCopy, IType targetDeclaringField) {
    super(copyName, fieldToCopy, targetDeclaringField, ScoutSdk.getType(RuntimeClasses.IFormField));
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    super.run(monitor, workingCopyManager);
    // getter
    ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getTargetDeclaringType().getCompilationUnit());
    IType form = TypeUtility.getAncestor(getCopiedType(), TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getToplevelTypeFilter()));
    if (TypeUtility.exists(form)) {
      IStructuredType structuredForm = SdkTypeUtility.createStructuredForm(form);
      TreeMap<CompositeObject, IJavaElement> siblings = new TreeMap<CompositeObject, IJavaElement>();
      IJavaElement sibling = structuredForm.getSibling(CATEGORIES.METHOD_INNER_TYPE_GETTER);
      siblings.put(new CompositeObject(2, ""), sibling);
      // find form
      createFormFieldGetter(getCopiedType(), form, siblings, hierarchy, monitor, workingCopyManager);
    }
  }

  protected void createFormFieldGetter(IType type, IType formType, TreeMap<CompositeObject, IJavaElement> siblings, ITypeHierarchy hierarchy, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    if (TypeUtility.exists(type)) {
      if (hierarchy.isSubtype(iFormField, type)) {
        FieldGetterCreateOperation op = new FieldGetterCreateOperation(type, formType, true);
        CompositeObject key = new CompositeObject(1, op.getMethodName());
        for (Entry<CompositeObject, IJavaElement> entry : siblings.entrySet()) {
          if (entry.getKey().compareTo(key) > 0) {
            op.setSibling(entry.getValue());
            break;
          }
        }
        op.validate();
        op.run(monitor, manager);
        siblings.put(key, op.getCreatedMethod());
      }
      // visit children
      if (hierarchy.isSubtype(iCompositeField, type)) {
        IType[] innerFields = TypeUtility.getInnerTypes(type, TypeFilters.getSubtypeFilter(iFormField, hierarchy));
        for (IType t : innerFields) {
          createFormFieldGetter(t, formType, siblings, hierarchy, monitor, manager);
        }
      }
    }
  }

}
