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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.OrderAnnotationsUpdateOperation;
import org.eclipse.scout.sdk.operation.method.FieldGetterCreateOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

public class FormFieldMoveOperation implements IOperation, IFieldPosition {

  private IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  private IType iCompositeField = ScoutSdk.getType(RuntimeClasses.ICompositeField);
  /**
   * e.g. the group box holding the moved field
   */
  private final IType m_fieldToMove;
  private final IType m_targetDeclaringType;
  private int m_position;
  private IType m_positionField;
  // out members
  private IType m_movedField;
  // local members
  private double m_orderNr;
  private IJavaElement m_sibling;

  public FormFieldMoveOperation(IType fieldToMove, IType targetDeclaringType) {
    m_fieldToMove = fieldToMove;
    m_targetDeclaringType = targetDeclaringType;
  }

  @Override
  public String getOperationName() {
    return "Move field '" + getFieldToMove().getElementName() + "'.";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getTargetDeclaringType() == null) {
      throw new IllegalArgumentException("declaring field must not be null.");
    }
    if (getFieldToMove() == null) {
      throw new IllegalArgumentException("field to move must be diffrent from null.");
    }
    if (getPosition() == 0) {
      throw new IllegalArgumentException("the location must be defined.");
    }
    if (getPosition() < LAST && getPositionField() == null) {
      throw new IllegalArgumentException("if the location is BEFORE or AFTER the target field must be defined.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find sibling
    IStructuredType structuredType = SdkTypeUtility.createStructuredCompositeField(getTargetDeclaringType());
    updateOrderNumbers(structuredType, monitor, workingCopyManager);
    String fieldSimpleName = getFieldToMove().getElementName();
    List<String> imports = new ArrayList<String>();
    if (!getTargetDeclaringType().equals(getFieldToMove().getDeclaringType())) {
      String normalizedFieldToMoveElementName = getFieldToMove().getFullyQualifiedName().replace('$', '.');
      String normalizedTargetFieldElementName = getTargetDeclaringType().getFullyQualifiedName().replace('$', '.');
      for (IImportDeclaration imp : getFieldToMove().getCompilationUnit().getImports()) {
        String normalizedImport = imp.getElementName().replace('$', '.');
        if (normalizedImport.startsWith(normalizedFieldToMoveElementName)) {
          imports.add(normalizedTargetFieldElementName + "." + fieldSimpleName + normalizedImport.replaceAll("^" + normalizedFieldToMoveElementName, ""));
          imp.delete(true, monitor);
        }
      }
    }

    Document fieldSourceDoc = new Document(getFieldToMove().getSource());
    MultiTextEdit multiEdit = new MultiTextEdit();
    Matcher orderMatcher = Pattern.compile("@Order\\(([0-9\\.]*)\\)").matcher(fieldSourceDoc.get());
    if (orderMatcher.find()) {
      ReplaceEdit edit = new ReplaceEdit(orderMatcher.start(1), orderMatcher.end(1) - orderMatcher.start(1), "" + m_orderNr);
      multiEdit.addChild(edit);
    }
    try {
      multiEdit.apply(fieldSourceDoc);
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not create new source.", e);
      return;
    }
    // delete old
    ITypeHierarchy superTypeHierarchy = getFieldToMove().newSupertypeHierarchy(monitor);
    if (superTypeHierarchy.contains(iCompositeField)) {
      BoxDeleteOperation deleteOp = new BoxDeleteOperation(getFieldToMove());
      deleteOp.validate();
      deleteOp.run(monitor, workingCopyManager);
    }
    else {
      FormFieldDeleteOperation deleteOp = new FormFieldDeleteOperation(getFieldToMove(), true);
      deleteOp.validate();
      deleteOp.run(monitor, workingCopyManager);
    }

    // create new
    final String finalSource = fieldSourceDoc.get();
    final String[] finalImports = imports.toArray(new String[imports.size()]);
    OrderedInnerTypeNewOperation fieldCopyOp = new OrderedInnerTypeNewOperation(fieldSimpleName, getTargetDeclaringType()) {
      @Override
      public String createSource(IImportValidator validator) throws JavaModelException {
        for (String imp : finalImports) {
          validator.addImport(imp);
        }
        return finalSource;
      }
    };
    fieldCopyOp.setSibling(m_sibling);
    fieldCopyOp.setOrderDefinitionType(iFormField);
    fieldCopyOp.setFormatSource(true);
    fieldCopyOp.run(monitor, workingCopyManager);
    m_movedField = fieldCopyOp.getCreatedType();

    // getter
    org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getMovedField().getCompilationUnit());
    IType form = TypeUtility.getAncestor(getMovedField(), TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getToplevelTypeFilter()));

    if (TypeUtility.exists(form)) {

      IStructuredType structuredForm = SdkTypeUtility.createStructuredForm(form);
      TreeMap<CompositeObject, IJavaElement> siblings = new TreeMap<CompositeObject, IJavaElement>();
      IJavaElement sibling = structuredForm.getSibling(CATEGORIES.METHOD_INNER_TYPE_GETTER);
      siblings.put(new CompositeObject(2, ""), sibling);
      createFormFieldGetter(m_movedField, form, siblings, hierarchy, monitor, workingCopyManager);
    }

  }

  protected void updateOrderNumbers(IStructuredType targetStructuredType, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    m_orderNr = -1.0;
    m_sibling = null;
    OrderAnnotationsUpdateOperation orderAnnotationOp = new OrderAnnotationsUpdateOperation(getTargetDeclaringType());

    IJavaElement[] orderedFormFields = targetStructuredType.getElements(CATEGORIES.TYPE_FORM_FIELD);
    double tempOrderNr = 10.0;
    if (orderedFormFields.length == 0) {
      m_orderNr = tempOrderNr;
      m_sibling = targetStructuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD);
      return;
    }
    else if (getPosition() == FIRST) {
      m_orderNr = tempOrderNr;
      tempOrderNr += 10.0;
      m_sibling = orderedFormFields[0];
    }
    for (int i = 0; i < orderedFormFields.length; i++) {
      if (orderedFormFields[i].equals(getPositionField())) {
        switch (getPosition()) {
          case BEFORE:
            m_orderNr = tempOrderNr;
            tempOrderNr += 10.0;
            orderAnnotationOp.addOrderAnnotation((IType) orderedFormFields[i], tempOrderNr);
            m_sibling = orderedFormFields[i];
            break;
          case AFTER:
            tempOrderNr += 10.0;
            orderAnnotationOp.addOrderAnnotation((IType) orderedFormFields[i], tempOrderNr);
            m_orderNr = tempOrderNr;
            if (orderedFormFields.length > i + 1) {
              m_sibling = orderedFormFields[i + 1];
            }
            break;
        }
      }
      else {
        orderAnnotationOp.addOrderAnnotation((IType) orderedFormFields[i], tempOrderNr);
      }
      tempOrderNr += 10.0;
    }
    if (getPosition() == LAST) {
      m_orderNr = tempOrderNr;
    }
    if (m_sibling == null) {
      m_sibling = targetStructuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD);
    }
    orderAnnotationOp.validate();
    orderAnnotationOp.run(monitor, manager);
  }

  protected void createFormFieldGetter(IType type, IType formType, TreeMap<CompositeObject, IJavaElement> siblings, org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy hierarchy, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
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

  public void setMovedField(IType movedField) {
    m_movedField = movedField;
  }

  public IType getMovedField() {
    return m_movedField;
  }

  public void setPosition(int position) {
    m_position = position;
  }

  public int getPosition() {
    return m_position;
  }

  public void setPositionField(IType positionField) {
    m_positionField = positionField;
  }

  public IType getPositionField() {
    return m_positionField;
  }

  public IType getFieldToMove() {
    return m_fieldToMove;
  }

  public IType getTargetDeclaringType() {
    return m_targetDeclaringType;
  }

}
