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
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.OrderAnnotationsUpdateOperation;
import org.eclipse.scout.sdk.operation.form.field.IFieldPosition;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 *
 */
public abstract class AbstractTypeDndOperation implements IOperation, IFieldPosition {

  public static final int MODE_COPY = 1;
  public static final int MODE_MOVE = 2;
  private final IType m_type;
  private final IType m_targetDeclaringType;
  private final CATEGORIES m_typeCategory;
  private int m_position;
  private IType m_positionType;
  private int m_mode;
  private String m_newTypeName;
  // out members
  private IType m_newType;

  public AbstractTypeDndOperation(IType type, IType targetDeclaringType, String newTypeName, CATEGORIES typeCategory, int mode) {
    m_type = type;
    m_targetDeclaringType = targetDeclaringType;
    m_newTypeName = newTypeName;
    m_typeCategory = typeCategory;
    m_mode = mode;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getTargetDeclaringType() == null) {
      throw new IllegalArgumentException("declaring field must not be null.");
    }
    if (getType() == null) {
      throw new IllegalArgumentException("type must be diffrent from null.");
    }
    if (getPosition() == 0) {
      throw new IllegalArgumentException("the location must be defined.");
    }
    if (getPosition() < LAST && getPositionType() == null) {
      throw new IllegalArgumentException("if the location is BEFORE or AFTER the target type must be defined.");
    }
  }

  @Override
  public String getOperationName() {
    return "DND " + getType().getElementName() + "...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find sibling
    IStructuredType structuredType = ScoutTypeUtility.createStructuredType(getTargetDeclaringType());
    TypePosition position = updateOrderNumbers(structuredType, monitor, workingCopyManager);
    String typeSimpleName = getNewTypeName();
    List<String> imports = new ArrayList<String>();
    if (!getTargetDeclaringType().equals(getType().getDeclaringType())) {
      String normalizedTypeElementName = getType().getFullyQualifiedName().replace('$', '.');
      String normalizedTargetTypeElementName = getTargetDeclaringType().getFullyQualifiedName().replace('$', '.');
      for (IImportDeclaration imp : getType().getCompilationUnit().getImports()) {
        String normalizedImport = imp.getElementName().replace('$', '.');
        if (normalizedImport.startsWith(normalizedTypeElementName)) {
          imports.add(normalizedTargetTypeElementName + "." + typeSimpleName + normalizedImport.replaceAll("^" + normalizedTypeElementName, ""));
        }
      }
    }

    Document fieldSourceDoc = new Document(getType().getSource());
    MultiTextEdit multiEdit = new MultiTextEdit();
    // type name
    if (!getType().getElementName().equals(getNewTypeName())) {
      Matcher renameMatcher = Pattern.compile("[\\s]{1}(" + getType().getElementName() + ")[\\s\\{]{1}", Pattern.MULTILINE).matcher(fieldSourceDoc.get());
      while (renameMatcher.find()) {
        ReplaceEdit edit = new ReplaceEdit(renameMatcher.start(1), renameMatcher.end(1) - renameMatcher.start(1), getNewTypeName());
        multiEdit.addChild(edit);
      }
    }

    // order nr
    Matcher orderMatcher = Pattern.compile("@Order\\(([0-9\\.]*)\\)").matcher(fieldSourceDoc.get());
    if (orderMatcher.find()) {
      ReplaceEdit edit = new ReplaceEdit(orderMatcher.start(1), orderMatcher.end(1) - orderMatcher.start(1), "" + position.orderNr);
      multiEdit.addChild(edit);
    }
    try {
      multiEdit.apply(fieldSourceDoc);
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not create new source.", e);
      return;
    }

    // when the parent is unchanged and the sibling is the field itself: nothing changed -> do not move / copy
    if (!(getType().equals(position.sibling) && getTargetDeclaringType().equals(getType().getDeclaringType()))) {
      if (getMode() == MODE_MOVE) {
        // delete old
        deleteType(getType(), monitor, workingCopyManager);
        structuredType = ScoutTypeUtility.createStructuredType(getTargetDeclaringType());
      }
      // create new
      m_newType = createNewType(getTargetDeclaringType(), typeSimpleName, fieldSourceDoc.get(), imports.toArray(new String[imports.size()]), position.sibling, structuredType, monitor, workingCopyManager);
    }
    // format
    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(TypeUtility.getToplevelType(getTargetDeclaringType()), true);
    formatOp.validate();
    formatOp.run(monitor, workingCopyManager);
  }

  protected void deleteType(IType type, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JavaElementDeleteOperation op = new JavaElementDeleteOperation();
    op.addMember(type);
    op.run(monitor, workingCopyManager);
  }

  protected IType createNewType(IType declaringType, String simpleName, final String source, final String[] fqImports, IJavaElement sibling, IStructuredType structuredType, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    InnerTypeNewOperation fieldCopyOp = new InnerTypeNewOperation(simpleName, getTargetDeclaringType()) {
      @Override
      public String createSource(IImportValidator validator) throws JavaModelException {
        for (String imp : fqImports) {
          validator.addImport(imp);
        }
        return source;
      }
    };
    fieldCopyOp.setSibling(sibling);
    fieldCopyOp.setFormatSource(false);
    fieldCopyOp.run(monitor, manager);
    return fieldCopyOp.getCreatedType();
  }

  protected TypePosition updateOrderNumbers(IStructuredType targetStructuredType, IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    TypePosition position = new TypePosition();
    OrderAnnotationsUpdateOperation orderAnnotationOp = new OrderAnnotationsUpdateOperation(getTargetDeclaringType());

    IJavaElement[] orderedTypes = targetStructuredType.getElements(getTypeCategory());
    double tempOrderNr = 10.0;
    if (orderedTypes.length == 0) {
      position.orderNr = tempOrderNr;
      position.sibling = targetStructuredType.getSibling(getTypeCategory());
      return position;
    }
    else if (getPosition() == FIRST) {
      position.orderNr = tempOrderNr;
      tempOrderNr += 10.0;
      position.sibling = orderedTypes[0];
    }
    for (int i = 0; i < orderedTypes.length; i++) {
      if (getMode() == MODE_MOVE && getType().equals(orderedTypes[i])) {
        continue;
      }
      if (orderedTypes[i].equals(getPositionType())) {
        switch (getPosition()) {
          case BEFORE:
            position.orderNr = tempOrderNr;
            tempOrderNr += 10.0;
            orderAnnotationOp.addOrderAnnotation((IType) orderedTypes[i], tempOrderNr);
            position.sibling = orderedTypes[i];
            break;
          case AFTER:
            orderAnnotationOp.addOrderAnnotation((IType) orderedTypes[i], tempOrderNr);
            tempOrderNr += 10.0;
            position.orderNr = tempOrderNr;
            if (orderedTypes.length > i + 1) {
              position.sibling = orderedTypes[i + 1];
            }
            break;
        }
      }
      else {
        orderAnnotationOp.addOrderAnnotation((IType) orderedTypes[i], tempOrderNr);
      }
      tempOrderNr += 10.0;
    }
    if (getPosition() == LAST) {
      position.orderNr = tempOrderNr;
    }
    if (position.sibling == null) {
      position.sibling = targetStructuredType.getSibling(getTypeCategory());
    }
    orderAnnotationOp.validate();
    orderAnnotationOp.run(monitor, manager);
    return position;
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

  public IType getNewType() {
    return m_newType;
  }

  public void setNewType(IType newType) {
    m_newType = newType;
  }

  public IType getType() {
    return m_type;
  }

  public IType getTargetDeclaringType() {
    return m_targetDeclaringType;
  }

  public CATEGORIES getTypeCategory() {
    return m_typeCategory;
  }

  public int getMode() {
    return m_mode;
  }

  public String getNewTypeName() {
    return m_newTypeName;
  }

  public class TypePosition {
    public double orderNr = -1.0;
    public IJavaElement sibling;
  }
}
