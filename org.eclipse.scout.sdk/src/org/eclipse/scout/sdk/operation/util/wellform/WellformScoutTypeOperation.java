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
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.SourceRange;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.text.edits.ReplaceEdit;

/**
 *
 */
public class WellformScoutTypeOperation implements IOperation {

  private final IType[] m_types;
  private final boolean m_recursive;
  private String m_lineDelimiter;

  public WellformScoutTypeOperation(IType type, boolean recursive) {
    this(new IType[]{type}, recursive);
  }

  public WellformScoutTypeOperation(IType[] types, boolean recursive) {
    m_types = types;
    m_recursive = recursive;
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform");
    IType[] types = getScoutTypes();
    if (types.length <= 3) {
      builder.append("'");
      for (int i = 0; i < types.length; i++) {
        builder.append(types[i].getElementName());
        if (i < types.length - 1) {
          builder.append(", ");
        }
      }
      builder.append("'");

    }
    builder.append("...");
    return builder.toString();
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (IType t : getScoutTypes()) {
      if (monitor.isCanceled()) {
        return;
      }
      monitor.beginTask("wellformX '" + t.getElementName() + "'", IProgressMonitor.UNKNOWN);
      wellformType(t, monitor, workingCopyManager);
      monitor.done();
    }
  }

  protected void wellformType(IType type, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws JavaModelException {
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
        classHeader = classHeader.replaceAll("\\s*$", "");
        builder.append(classHeader + m_lineDelimiter);
        IStructuredType structureHelper = SdkTypeUtility.createStructuredType(type);
        append(structureHelper.getElements(CATEGORIES.FIELD_LOGGER, IField.class), builder);
        append(structureHelper.getElements(CATEGORIES.FIELD_STATIC, IField.class), builder);
        append(structureHelper.getElements(CATEGORIES.FIELD_MEMBER, IField.class), builder);
        append(structureHelper.getElements(CATEGORIES.FIELD_UNKNOWN, IField.class), builder);
        append(structureHelper.getElements(CATEGORIES.ENUM, IType.class), builder, false);
        // methods
        append(structureHelper.getElements(CATEGORIES.METHOD_CONSTRUCTOR, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_CONFIG_PROPERTY, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_CONFIG_EXEC, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_FORM_DATA_BEAN, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_OVERRIDDEN, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_START_HANDLER, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_INNER_TYPE_GETTER, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_LOCAL_BEAN, IMethod.class), builder);
        append(structureHelper.getElements(CATEGORIES.METHOD_UNCATEGORIZED, IMethod.class), builder);
        // types
        append(structureHelper.getElements(CATEGORIES.TYPE_FORM_FIELD, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_COLUMN, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_CODE, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_FORM, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_TABLE, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_ACTIVITY_MAP, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_TREE, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_CALENDAR, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_CALENDAR_ITEM_PROVIDER, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_WIZARD, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_WIZARD_STEP, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_MENU, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_VIEW_BUTTON, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_TOOL_BUTTON, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_KEYSTROKE, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_FORM_HANDLER, IType.class), builder, isRecursive());
        append(structureHelper.getElements(CATEGORIES.TYPE_UNCATEGORIZED, IType.class), builder, false);

        String classTail = typeSource.substring(end);
        // remove tailing spaces
        classTail = classTail.replaceAll("^\\s*", "");
        builder.append(classTail);
      }
    }
  }

  protected void append(IField[] fields, StringBuilder builder) throws JavaModelException {
    for (IField f : fields) {
      builder.append(m_lineDelimiter + f.getSource());
    }
  }

  protected void append(IMethod[] methods, StringBuilder builder) throws JavaModelException {
    for (IMethod m : methods) {
      builder.append(m_lineDelimiter + m.getSource());
    }
  }

  protected void append(IType[] types, StringBuilder builder, boolean recursive) throws JavaModelException {
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

  public IType[] getScoutTypes() {
    return m_types;
  }

  public boolean isRecursive() {
    return m_recursive;
  }

}
