/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.structured;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMember;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.structured.IStructuredType.Categories;

/**
 * <h3>{@link Wellformer}</h3>
 *
 * @since 5.2.0
 */
public class Wellformer {

  protected static final Pattern EMPTY_COMMENT_REGEX = Pattern.compile("/\\*\\*[/*\\s]+", Pattern.DOTALL | Pattern.MULTILINE);
  private static final Pattern LEADING_SPACES_REGEX = Pattern.compile("\\s*$");
  private static final Pattern TRAILING_SPACES_REGEX = Pattern.compile("^\\s*");

  private final String m_lineDelimiter;
  private final boolean m_recursive;

  public Wellformer(String lineDelimiter, boolean recursive) {
    m_lineDelimiter = lineDelimiter;
    m_recursive = recursive;
  }

  protected static List<IJavaElement> getChildren(IType t) {
    var fields = t.fields().stream().toList();
    var innerTypes = t.innerTypes().stream().toList();
    var methods = t.methods().stream().toList();
    List<IJavaElement> result = new ArrayList<>(fields.size() + innerTypes.size() + methods.size());
    result.addAll(fields);
    result.addAll(methods);
    result.addAll(innerTypes);
    return result;
  }

  protected void appendFields(Iterable<IField> fields, StringBuilder builder) {
    for (var f : fields) {
      builder.append(m_lineDelimiter);
      appendMemberSource(f, builder);
    }
  }

  protected void appendMemberSource(IMember m, StringBuilder builder) {
    if (m.source().isEmpty()) {
      return;
    }

    var source = m.source().orElseThrow().asCharSequence();
    if (m.javaDoc().isPresent()) {
      var javaDoc = m.javaDoc().orElseThrow();
      if (EMPTY_COMMENT_REGEX.matcher(javaDoc.asCharSequence()).matches()) {
        // workaround for a bug in the javadoc formatter. See bug 491387 for details.
        var javaDocEndRel = javaDoc.length() + 1 + m_lineDelimiter.length();
        builder.append("/**").append(m_lineDelimiter).append(" *").append(m_lineDelimiter).append(" */"); // default empty comment
        builder.append(source.subSequence(javaDocEndRel, source.length()));
        return;
      }
    }
    builder.append(source);
  }

  protected void appendMethods(Iterable<IMethod> methods, StringBuilder builder) {
    for (var m : methods) {
      builder.append(m_lineDelimiter);
      appendMemberSource(m, builder);
    }
  }

  protected void appendTypes(Iterable<IType> types, StringBuilder builder, boolean recursive) {
    for (var t : types) {
      if (recursive) {
        builder.append(m_lineDelimiter).append(m_lineDelimiter);
        buildSource(t, builder);
      }
      else {
        builder.append(m_lineDelimiter);
        appendMemberSource(t, builder);
      }
    }
  }

  public boolean buildSource(IType type, StringBuilder builder) {
    if (type.source().isEmpty()) {
      return false;
    }

    var typeSource = type.source().orElseThrow();
    var src = typeSource.asCharSequence();
    var children = getChildren(type);
    if (children.isEmpty()) {
      builder.append(src);
    }
    else {
      var start = Integer.MAX_VALUE;
      var end = -1;
      for (var e : children) {
        if (e.source().isPresent()) {
          var eRange = e.source().orElseThrow();
          start = Math.min(start, (eRange.start() - typeSource.start()));
          end = Math.max(end, (eRange.start() + eRange.length() - typeSource.start()));
        }
      }
      if (start > end) {
        builder.append(src);
      }
      else {
        var classHeader = src.subSequence(0, start);
        // remove leading spaces
        classHeader = LEADING_SPACES_REGEX.matcher(classHeader).replaceAll("");
        builder.append(classHeader).append(m_lineDelimiter);

        var structureHelper = StructuredType.of(type);
        appendFields(structureHelper.getElements(Categories.FIELD_LOGGER, IField.class), builder);
        appendFields(structureHelper.getElements(Categories.FIELD_STATIC, IField.class), builder);
        appendFields(structureHelper.getElements(Categories.FIELD_MEMBER, IField.class), builder);
        appendFields(structureHelper.getElements(Categories.FIELD_UNKNOWN, IField.class), builder);
        appendTypes(structureHelper.getElements(Categories.ENUM, IType.class), builder, false);
        // methods
        appendMethods(structureHelper.getElements(Categories.METHOD_CONSTRUCTOR, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_CONFIG_PROPERTY, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_CONFIG_EXEC, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_FORM_DATA_BEAN, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_OVERRIDDEN, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_START_HANDLER, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_INNER_TYPE_GETTER, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_LOCAL_BEAN, IMethod.class), builder);
        appendMethods(structureHelper.getElements(Categories.METHOD_UNCATEGORIZED, IMethod.class), builder);
        // types
        appendTypes(structureHelper.getElements(Categories.TYPE_FORM_FIELD, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_COLUMN, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_CODE, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_FORM, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_TABLE, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_TREE, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_CALENDAR, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_CALENDAR_ITEM_PROVIDER, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_WIZARD, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_WIZARD_STEP, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_MENU, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_VIEW_BUTTON, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_KEYSTROKE, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_FORM_HANDLER, IType.class), builder, m_recursive);
        appendTypes(structureHelper.getElements(Categories.TYPE_UNCATEGORIZED, IType.class), builder, false);

        var classTail = src.subSequence(end, src.length());
        // remove trailing spaces
        classTail = TRAILING_SPACES_REGEX.matcher(classTail).replaceAll("");
        builder.append(m_lineDelimiter);
        builder.append(classTail);
      }
    }
    return true;
  }
}
