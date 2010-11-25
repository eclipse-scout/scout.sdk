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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.OrderAnnotationsUpdateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 *
 */
public class AbstractCopyInnerTypeOperation implements IOperation, IFieldPosition {

  private final String m_targetTypeName;
  private final IType m_typeToCopy;
  private final IType m_targetDeclaringType;
  private int m_position;
  private IType m_positionType;
  private double m_orderNr;
  private IJavaElement m_sibling;
  private final IType m_orderDefinitionType;
  private IType m_copiedType;

  public AbstractCopyInnerTypeOperation(String targetFieldName, IType typeToCopy, IType targetDeclaringType, IType orderDefinitionType) {
    m_targetTypeName = targetFieldName;
    m_typeToCopy = typeToCopy;
    m_targetDeclaringType = targetDeclaringType;
    m_orderDefinitionType = orderDefinitionType;
  }

  @Override
  public String getOperationName() {
    return "create a copy of '" + getTypeToCopy().getElementName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTargetTypeName())) {
      throw new IllegalArgumentException("type name can not be null.");
    }
    if (getTypeToCopy() == null) {
      throw new IllegalArgumentException("type to copy can not be null.");
    }
    if (getTargetDeclaringType() == null) {
      throw new IllegalArgumentException("target declaring type copy can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    List<String> imports = new ArrayList<String>();
    for (IImportDeclaration imp : getTypeToCopy().getCompilationUnit().getImports()) {
      if (imp.getElementName().startsWith(getTypeToCopy().getFullyQualifiedName())) {
        imports.add(imp.getElementName());
      }
    }
    updateOrderNumbers(monitor, workingCopyManager);
    // source
    Document fieldSourceDoc = new Document(getTypeToCopy().getSource());
    MultiTextEdit multiEdit = new MultiTextEdit();
    Matcher renameMatcher = Pattern.compile("[\\s]{1}(" + getTypeToCopy().getElementName() + ")[\\s\\{]{1}", Pattern.MULTILINE).matcher(fieldSourceDoc.get());
    while (renameMatcher.find()) {
      ReplaceEdit edit = new ReplaceEdit(renameMatcher.start(1), renameMatcher.end(1) - renameMatcher.start(1), getTargetTypeName());
      multiEdit.addChild(edit);
    }
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
    final String finalSource = fieldSourceDoc.get();
    final String[] finalImports = imports.toArray(new String[imports.size()]);
    InnerTypeNewOperation fieldCopyOp = new InnerTypeNewOperation(getTargetTypeName(), getTargetDeclaringType()) {
      @Override
      public String createSource(IImportValidator validator) throws JavaModelException {
        for (String imp : finalImports) {
          validator.addImport(imp);
        }
        return finalSource;
      }
    };
    fieldCopyOp.setSibling(m_sibling);
    fieldCopyOp.setFormatSource(true);
    fieldCopyOp.run(monitor, workingCopyManager);
    m_copiedType = fieldCopyOp.getCreatedType();

  }

  protected void updateOrderNumbers(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    m_orderNr = -1.0;
    m_sibling = null;
    OrderAnnotationsUpdateOperation orderAnnotationOp = new OrderAnnotationsUpdateOperation(getTargetDeclaringType());
    ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getTargetDeclaringType());
    IType[] innerTypes = TypeUtility.getInnerTypes(getTargetDeclaringType(), TypeFilters.getSubtypeFilter(m_orderDefinitionType, hierarchy), TypeComparators.getOrderAnnotationComparator());
//    IJavaElement[] orderedFormFields = targetStructuredType.getElements(CATEGORIES.TYPE_FORM_FIELD);
    double tempOrderNr = 10.0;
    if (innerTypes.length == 0) {
      m_orderNr = tempOrderNr;
      m_sibling = null;//targetStructuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD);
      return;
    }
    else if (getPosition() == FIRST) {
      m_orderNr = tempOrderNr;
      tempOrderNr += 10.0;
      m_sibling = innerTypes[0];
    }
    for (int i = 0; i < innerTypes.length; i++) {
      if (innerTypes[i].equals(getPositionType())) {
        switch (getPosition()) {
          case BEFORE:
            m_orderNr = tempOrderNr;
            tempOrderNr += 10.0;
            orderAnnotationOp.addOrderAnnotation((IType) innerTypes[i], tempOrderNr);
            m_sibling = innerTypes[i];
            break;
          case AFTER:
            tempOrderNr += 10.0;
            orderAnnotationOp.addOrderAnnotation((IType) innerTypes[i], tempOrderNr);
            m_orderNr = tempOrderNr;
            if (innerTypes.length > i + 1) {
              m_sibling = innerTypes[i + 1];
            }
            break;
        }
      }
      else {
        orderAnnotationOp.addOrderAnnotation((IType) innerTypes[i], tempOrderNr);
      }
      tempOrderNr += 10.0;
    }
    if (getPosition() == LAST) {
      m_orderNr = tempOrderNr;
    }
    orderAnnotationOp.validate();
    orderAnnotationOp.run(monitor, manager);
  }

  public String getTargetTypeName() {
    return m_targetTypeName;
  }

  public IType getTypeToCopy() {
    return m_typeToCopy;
  }

  public IType getTargetDeclaringType() {
    return m_targetDeclaringType;
  }

  public IType getOrderDefinitionType() {
    return m_orderDefinitionType;
  }

  public int getPosition() {
    return m_position;
  }

  public void setPosition(int position) {
    m_position = position;
  }

  public IType getPositionType() {
    return m_positionType;
  }

  public void setPositionType(IType positionType) {
    m_positionType = positionType;
  }

  public IType getCopiedType() {
    return m_copiedType;
  }
}
