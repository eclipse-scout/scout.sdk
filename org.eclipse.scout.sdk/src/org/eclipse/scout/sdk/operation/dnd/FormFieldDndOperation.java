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
package org.eclipse.scout.sdk.operation.dnd;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class FormFieldDndOperation extends AbstractTypeDndOperation {

  private final IType iCompositeField = TypeUtility.getType(IRuntimeClasses.ICompositeField);

  /**
   * @param typeToMove
   * @param targetDeclaringType
   * @param typeCategory
   */
  public FormFieldDndOperation(IType typeToMove, IType targetDeclaringType, String newName, int mode) {
    super(typeToMove, targetDeclaringType, newName, TypeUtility.getType(IRuntimeClasses.IFormField), mode);
  }

  @Override
  protected IType createNewType(String source, List<String> fqImports, IJavaElement sibling, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    IType newFormField = super.createNewType(source, fqImports, sibling, monitor, manager);
    // getter
    ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(newFormField.getCompilationUnit());
    IType form = TypeUtility.getAncestor(newFormField, TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IForm), hierarchy),
        TypeFilters.getPrimaryTypeFilter()));

    if (TypeUtility.exists(form)) {
      IStructuredType structuredForm = ScoutTypeUtility.createStructuredForm(form, hierarchy);
      TreeMap<CompositeObject, IJavaElement> siblings = new TreeMap<CompositeObject, IJavaElement>();
      for (IJavaElement e : structuredForm.getElements(CATEGORIES.METHOD_INNER_TYPE_GETTER)) {
        siblings.put(new CompositeObject(1, e.getElementName()), e);
      }
      IJavaElement getterSibling = structuredForm.getSibling(CATEGORIES.METHOD_INNER_TYPE_GETTER);
      siblings.put(new CompositeObject(2, ""), getterSibling);
      createFormFieldGetter(newFormField, form, siblings, hierarchy, monitor, manager);
    }
    return newFormField;
  }

  @Override
  protected void deleteType(IType type, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ITypeHierarchy superTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(getType());
    if (superTypeHierarchy.contains(iCompositeField)) {
      BoxDeleteOperation deleteOp = new BoxDeleteOperation(getType());
      deleteOp.validate();
      deleteOp.run(monitor, workingCopyManager);
    }
    else {
      FormFieldDeleteOperation deleteOp = new FormFieldDeleteOperation(getType(), false);
      deleteOp.validate();
      deleteOp.run(monitor, workingCopyManager);
    }
  }

  protected void createFormFieldGetter(IType type, IType formType, TreeMap<CompositeObject, IJavaElement> siblings, ITypeHierarchy hierarchy, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    if (TypeUtility.exists(type)) {
      InnerTypeGetterCreateOperation op = new InnerTypeGetterCreateOperation(type, formType, true);
      CompositeObject key = new CompositeObject(1, op.getElementName());
      for (Entry<CompositeObject, IJavaElement> entry : siblings.entrySet()) {
        if (entry.getKey().compareTo(key) > 0) {
          op.setSibling(entry.getValue());
          break;
        }
      }
      op.validate();
      op.run(monitor, manager);
      siblings.put(key, op.getCreatedMethod());

      // visit children
      if (hierarchy.isSubtype(iCompositeField, type)) {
        Set<IType> innerFields = TypeUtility.getInnerTypes(type, TypeFilters.getSubtypeFilter(getOrderDefinitionType(), hierarchy));
        for (IType t : innerFields) {
          createFormFieldGetter(t, formType, siblings, hierarchy, monitor, manager);
        }
      }
    }
  }
}
