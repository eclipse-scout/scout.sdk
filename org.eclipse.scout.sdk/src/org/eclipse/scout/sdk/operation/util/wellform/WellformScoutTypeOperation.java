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
package org.eclipse.scout.sdk.operation.util.wellform;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.text.edits.ReplaceEdit;

/**
 *
 */
public class WellformScoutTypeOperation implements IOperation {

  private static final Pattern LEADING_SPACES_REGEX = Pattern.compile("\\s*$");
  private static final Pattern TRAILING_SPACES_REGEX = Pattern.compile("^\\s*");

  private final Set<IType> m_types;
  private final boolean m_recursive;
  private String m_lineDelimiter;

  public WellformScoutTypeOperation(IType type, boolean recursive) {
    this(CollectionUtility.hashSet(type), recursive);
  }

  public WellformScoutTypeOperation(Set<IType> types, boolean recursive) {
    m_types = CollectionUtility.hashSet(types);
    m_recursive = recursive;
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform ");
    if (m_types.size() > 0) {
      int i = 0;
      builder.append('\'');
      for (IType t : m_types) {
        builder.append(t.getElementName());
        if (i < 2) {
          builder.append(", ");
        }
        else if (i == 2) {
          break;
        }
        i++;
      }
      builder.append('\'');
    }
    builder.append("...");
    return builder.toString();
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IType t : m_types) {
      if (monitor.isCanceled()) {
        return;
      }
      monitor.beginTask("wellformX '" + t.getElementName() + "'", IProgressMonitor.UNKNOWN);
      wellformType(t, monitor, workingCopyManager);
      monitor.done();
    }
  }

  protected void wellformType(IType type, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws JavaModelException {
    workingCopyManager.register(type.getCompilationUnit(), monitor);
    IBuffer icuBuffer = type.getCompilationUnit().getBuffer();
    Document sourceDoc = new Document(icuBuffer.getContents());
    m_lineDelimiter = sourceDoc.getDefaultLineDelimiter();
    ISourceRange typeRange = type.getSourceRange();
    StringBuilder sourceBuilder = new StringBuilder();
    buildSource(type, sourceBuilder);
    ReplaceEdit edit = new ReplaceEdit(typeRange.getOffset(), typeRange.getLength(), sourceBuilder.toString());
    // format
    try {
      edit.apply(sourceDoc);
      SourceRange range = new SourceRange(0, sourceDoc.getLength());
      SourceFormatOperation op = new SourceFormatOperation(type.getJavaProject(), sourceDoc, range);
      op.run(monitor, workingCopyManager);
      icuBuffer.setContents(sourceDoc.get());
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform type '" + type.getFullyQualifiedName() + "'.", e);
    }
  }

  protected void buildSource(IType type, StringBuilder builder) throws JavaModelException {
    IJavaElement[] children = type.getChildren();
    String typeSource = type.getSource();
    if (children == null || children.length == 0) {
      builder.append(typeSource);
    }
    else {
      ISourceRange typeRange = type.getSourceRange();
      int start = Integer.MAX_VALUE;
      int end = -1;
      for (IJavaElement e : children) {
        if (e instanceof ISourceReference) {
          ISourceRange eRange = ((ISourceReference) e).getSourceRange();
          start = Math.min(start, (eRange.getOffset() - typeRange.getOffset()));
          end = Math.max(end, (eRange.getOffset() + eRange.getLength() - typeRange.getOffset()));
        }
      }
      if (start > end) {
        builder.append(typeSource);
      }
      else {

        String classHeader = typeSource.substring(0, start);
        // remove leading spaces
        classHeader = LEADING_SPACES_REGEX.matcher(classHeader).replaceAll("");
        builder.append(classHeader + m_lineDelimiter);

        IStructuredType structureHelper = ScoutTypeUtility.createStructuredType(type);
        appendFields(structureHelper.getElements(CATEGORIES.FIELD_LOGGER, IField.class), builder);
        appendFields(structureHelper.getElements(CATEGORIES.FIELD_STATIC, IField.class), builder);
        appendFields(structureHelper.getElements(CATEGORIES.FIELD_MEMBER, IField.class), builder);
        appendFields(structureHelper.getElements(CATEGORIES.FIELD_UNKNOWN, IField.class), builder);
        appendTypes(structureHelper.getElements(CATEGORIES.ENUM, IType.class), builder, false);
        // methods
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_CONSTRUCTOR, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_CONFIG_PROPERTY, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_CONFIG_EXEC, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_FORM_DATA_BEAN, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_OVERRIDDEN, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_START_HANDLER, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_INNER_TYPE_GETTER, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_LOCAL_BEAN, IMethod.class), builder);
        appendMethods(structureHelper.getElements(CATEGORIES.METHOD_UNCATEGORIZED, IMethod.class), builder);
        // types
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_FORM_FIELD, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_COLUMN, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_CODE, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_FORM, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_TABLE, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_ACTIVITY_MAP, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_TREE, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_CALENDAR, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_WIZARD, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_WIZARD_STEP, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_MENU, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_VIEW_BUTTON, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_TOOL_BUTTON, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_KEYSTROKE, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_FORM_HANDLER, IType.class), builder, isRecursive());
        appendTypes(structureHelper.getElements(CATEGORIES.TYPE_UNCATEGORIZED, IType.class), builder, false);

        String classTail = typeSource.substring(end);
        // remove trailing spaces
        classTail = TRAILING_SPACES_REGEX.matcher(classTail).replaceAll("");
        builder.append(classTail);
      }
    }
  }

  protected void appendFields(List<IField> fields, StringBuilder builder) throws JavaModelException {
    for (IField f : fields) {
      builder.append(m_lineDelimiter + f.getSource());
    }
  }

  protected void appendMethods(List<IMethod> methods, StringBuilder builder) throws JavaModelException {
    for (IMethod m : methods) {
      builder.append(m_lineDelimiter + m.getSource());
    }
  }

  protected void appendTypes(List<IType> types, StringBuilder builder, boolean recursive) throws JavaModelException {
    for (IType t : types) {
      if (recursive) {
        builder.append(m_lineDelimiter + m_lineDelimiter);
        buildSource(t, builder);
      }
      else {
        builder.append(m_lineDelimiter + t.getSource());
      }
    }
  }

  public boolean isRecursive() {
    return m_recursive;
  }
}
