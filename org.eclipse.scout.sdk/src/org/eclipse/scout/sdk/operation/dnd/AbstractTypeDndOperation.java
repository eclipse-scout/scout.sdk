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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.IFieldPosition;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
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
  private final IType m_orderDefinitionType;
  private final int m_mode;

  private int m_position;
  private IType m_positionType;
  private String m_newTypeName;

  // out members
  private IType m_newType;

  public AbstractTypeDndOperation(IType type, IType targetDeclaringType, String newTypeName, IType orderDefinitionType, int mode) {
    m_type = type;
    m_targetDeclaringType = targetDeclaringType;
    m_newTypeName = newTypeName;
    m_orderDefinitionType = orderDefinitionType;
    m_mode = mode;
  }

  @Override
  public void validate() {
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
    return "DnD " + getType().getElementName() + "...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    ITypeHierarchy typeHierarchy = TypeUtility.getLocalTypeHierarchy(getTargetDeclaringType());
    IJavaElement sibling = findSibling(typeHierarchy);

    // collect new imports
    List<String> imports = new ArrayList<>();
    String normalizedTypeElementName = null;
    String normalizedTargetTypeElementName = null;
    if (!getTargetDeclaringType().equals(getType().getDeclaringType())) {
      normalizedTypeElementName = getType().getFullyQualifiedName().replace('$', '.');
      normalizedTargetTypeElementName = getTargetDeclaringType().getFullyQualifiedName().replace('$', '.');
    }
    for (IImportDeclaration imp : getType().getCompilationUnit().getImports()) {
      String normalizedImport = imp.getElementName().replace('$', '.');
      if (normalizedTypeElementName != null && normalizedImport.startsWith(normalizedTypeElementName)) {
        imports.add(normalizedTargetTypeElementName + "." + getNewTypeName() + normalizedImport.replaceAll("^" + normalizedTypeElementName, ""));
      }
      else {
        imports.add(normalizedImport);
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

    // order
    Pattern pat = Pattern.compile("@(?:" + Signature.getSimpleName(IRuntimeClasses.Order) + "|" + IRuntimeClasses.Order + ")\\(((?:[\\-]?)(?:[0-9\\.]{1,200}))(?:[dfDF]?)\\)");
    Matcher orderMatcher = pat.matcher(fieldSourceDoc.get());
    if (orderMatcher.find()) {
      double order = ScoutTypeUtility.getOrderNr(getTargetDeclaringType(), getOrderDefinitionType(), sibling, typeHierarchy);
      ReplaceEdit edit = new ReplaceEdit(orderMatcher.start(1), orderMatcher.end(1) - orderMatcher.start(1), "" + order);
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
    if (!(getType().equals(sibling) && getTargetDeclaringType().equals(getType().getDeclaringType()))) {
      if (getMode() == MODE_MOVE) {
        // delete old
        deleteType(getType(), monitor, workingCopyManager);
      }
      // create new
      m_newType = createNewType(fieldSourceDoc.get(), imports, sibling, monitor, workingCopyManager);
    }

    // format
    JavaElementFormatOperation formatOp = new JavaElementFormatOperation(TypeUtility.getPrimaryType(getTargetDeclaringType()), true);
    formatOp.validate();
    formatOp.run(monitor, workingCopyManager);
  }

  protected void deleteType(IType type, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JavaElementDeleteOperation op = new JavaElementDeleteOperation();
    op.addMember(type);
    op.run(monitor, workingCopyManager);
  }

  protected IType createNewType(final String source, final List<String> fqImports, IJavaElement sibling, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    TypeSourceBuilder typeSourceBuilder = new TypeSourceBuilder(getNewTypeName()) {
      @Override
      public void createSource(StringBuilder sourcebuilder, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        for (String imp : fqImports) {
          validator.addImport(imp);
        }
        sourcebuilder.append(source);
      }
    };

    OrderedInnerTypeNewOperation fieldCopyOp = new OrderedInnerTypeNewOperation(typeSourceBuilder, getTargetDeclaringType());
    fieldCopyOp.setOrderDefinitionType(getOrderDefinitionType());
    fieldCopyOp.setSibling(sibling);
    fieldCopyOp.setFormatSource(false);
    fieldCopyOp.run(monitor, manager);
    return fieldCopyOp.getCreatedType();
  }

  protected IJavaElement findSibling(ITypeHierarchy typeHierarchy) {
    Set<IType> innerTypes = TypeUtility.getInnerTypes(getTargetDeclaringType(), TypeFilters.getSubtypeFilter(getOrderDefinitionType(), typeHierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
    if (innerTypes.size() < 1) {
      return null;
    }
    else if (getPosition() == FIRST) {
      return CollectionUtility.firstElement(innerTypes);
    }
    else if (getPosition() == LAST) {
      return null;
    }

    Iterator<IType> it = innerTypes.iterator();
    while (it.hasNext()) {
      IType t = it.next();
      if (getMode() == MODE_MOVE && getType().equals(t)) {
        continue;
      }
      if (t.equals(getPositionType())) {
        if (getPosition() == BEFORE) {
          return t;
        }
        else {
          if (it.hasNext()) {
            return it.next();
          }
          else {
            return null;
          }
        }
      }
    }
    return null; // position type not found
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

  public IType getType() {
    return m_type;
  }

  public IType getTargetDeclaringType() {
    return m_targetDeclaringType;
  }

  public int getMode() {
    return m_mode;
  }

  public String getNewTypeName() {
    return m_newTypeName;
  }

  public IType getOrderDefinitionType() {
    return m_orderDefinitionType;
  }
}
