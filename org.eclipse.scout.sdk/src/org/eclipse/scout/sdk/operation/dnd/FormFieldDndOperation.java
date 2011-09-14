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

import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class FormFieldDndOperation extends AbstractTypeDndOperation {

  final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  final IType iCompositeField = ScoutSdk.getType(RuntimeClasses.ICompositeField);

  /**
   * @param typeToMove
   * @param targetDeclaringType
   * @param typeCategory
   */
  public FormFieldDndOperation(IType typeToMove, IType targetDeclaringType, String newName, int mode) {
    super(typeToMove, targetDeclaringType, newName, CATEGORIES.TYPE_FORM_FIELD, mode);
  }

  @Override
  protected IType createNewType(IType declaringType, String simpleName, String source, String[] fqImports, IJavaElement sibling, IStructuredType structuredType, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    IType newFormField = super.createNewType(declaringType, simpleName, source, fqImports, sibling, structuredType, monitor, manager);
//    OrganizeImportOperation impOp = new OrganizeImportOperation(declaringType.getCompilationUnit());
//    impOp.validate();
//    impOp.run(monitor, manager);
    // getter
    org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(newFormField.getCompilationUnit());
    IType form = TypeUtility.getAncestor(newFormField, TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getToplevelTypeFilter()));

    if (TypeUtility.exists(form)) {
      IStructuredType structuredForm = SdkTypeUtility.createStructuredForm(form);
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
  protected void deleteType(IType type, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ITypeHierarchy superTypeHierarchy = getType().newSupertypeHierarchy(monitor);
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

  protected void createFormFieldGetter(IType type, IType formType, TreeMap<CompositeObject, IJavaElement> siblings, org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy hierarchy, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    if (TypeUtility.exists(type)) {
//      if (hierarchy.isSubtype(iFormField, type)) {
      InnerTypeGetterCreateOperation op = new InnerTypeGetterCreateOperation(type, formType, true);
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
//      }
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
