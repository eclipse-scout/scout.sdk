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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 * <h3>{@link TableFieldSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.02.2011
 */
public class TableFieldSourceBuilder extends SourceBuilderWithProperties {
  private final IType iTable = ScoutSdk.getType(RuntimeClasses.ITable);
  private final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);
  private final IType m_tableField;

  public TableFieldSourceBuilder(IType tableField, ITypeHierarchy hierarchy) {
    super(tableField);
    m_tableField = tableField;
    // find table
    IType table = findTable(tableField, hierarchy);
    if (TypeUtility.exists(table)) {
      visitTable(table, hierarchy);
    }
  }

  private IType findTable(IType tableField, ITypeHierarchy hierarchy) {
    if (TypeUtility.exists(tableField)) {
      IType[] tables = TypeUtility.getInnerTypes(tableField, TypeFilters.getSubtypeFilter(iTable, hierarchy), null);
      if (tables.length > 0) {
        if (tables.length > 1) {
          ScoutSdk.logWarning("table field '" + tableField.getFullyQualifiedName() + "' contatins more than one table! Taking first for formdata creation.");
        }
        return tables[0];
      }
      else {
        return findTable(hierarchy.getSuperclass(tableField), hierarchy);
      }
    }
    return null;
  }

  protected void visitTable(IType table, ITypeHierarchy hierarchy) {

    final IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(iColumn, hierarchy), TypeComparators.getOrderAnnotationComparator());
    final String[] colunmSignatures = new String[columns.length];
    for (int i = 0; i < columns.length; i++) {
      try {
        IType column = columns[i];

        String upperColName = FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(column.getElementName()), true);
        String lowerColName = FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(column.getElementName()), false);
        String methodParameterName = FormDataUtility.getValidMethodParameterName(lowerColName);
        final String colSignature = getColumnSignature(column, hierarchy);
        colunmSignatures[i] = colSignature;
        // setter
        MethodSourceBuilder columnSetter = new MethodSourceBuilder();
        columnSetter.setElementName("set" + upperColName);
        columnSetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
        columnSetter.addParameter(new MethodParameter(colSignature, methodParameterName));
        columnSetter.setSimpleBody("setValueInternal(row, " + i + ", " + methodParameterName + ");");
        addBuilder(columnSetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 1, i, 1, columnSetter));
        // getter
        MethodSourceBuilder columnGetter = new MethodSourceBuilder() {
          @Override
          protected String createMethodBody(IImportValidator validator) {
            StringBuilder getterBody = new StringBuilder();
            getterBody.append("return ");
            if (!colSignature.equals("Ljava.lang.Object;")) {
              String simpleRef = ScoutSdkUtility.getSimpleTypeRefName(colSignature, validator);
              getterBody.append("(" + simpleRef + ") ");
            }
            getterBody.append("getValueInternal(row, 1);");
            return getterBody.toString();
          }
        };
        columnGetter.setElementName("get" + upperColName);
        columnGetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
        columnGetter.setReturnSignature(colSignature);
        addBuilder(columnGetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 1, i, 2, columnSetter));
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not add column '" + columns[i].getFullyQualifiedName() + "' to form data.", e);
      }

    }
    // gobal getter
    MethodSourceBuilder globalGetter = new MethodSourceBuilder() {
      @Override
      protected String createMethodBody(IImportValidator validator) {
        StringBuilder builder = new StringBuilder();
        builder.append("  switch(column){\n");
        for (int i = 0; i < columns.length; i++) {
          builder.append("    case " + i + ":\n return get");
          builder.append(FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(columns[i].getElementName()), true));
          builder.append("(row);\n");
        }
        builder.append("    default: return null;\n");
        builder.append("  }");
        return builder.toString();
      }
    };
    globalGetter.setElementName("getValueAt");
    globalGetter.addAnnotation(new AnnotationSourceBuilder(Signature.createTypeSignature(Override.class.getName(), true)));
    globalGetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
    globalGetter.addParameter(new MethodParameter(Signature.SIG_INT, "column"));
    globalGetter.setReturnSignature(Signature.createTypeSignature(Object.class.getName(), true));
    addBuilder(globalGetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 2, globalGetter.getElementName(), globalGetter));

    // gobal setter
    MethodSourceBuilder globalSetter = new MethodSourceBuilder() {
      @Override
      protected String createMethodBody(IImportValidator validator) {
        StringBuilder builder = new StringBuilder();
        builder.append("  switch(column){\n");
        for (int i = 0; i < columns.length; i++) {
          builder.append("    case " + i + ": set");
          builder.append(FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(columns[i].getElementName()), true));
          builder.append("(row,");
          if (!colunmSignatures[i].equals("Ljava.lang.Object;")) {
            String simpleRef = ScoutSdkUtility.getSimpleTypeRefName(colunmSignatures[i], validator);
            builder.append("(" + simpleRef + ") ");
          }
          builder.append("value); break;\n");
        }
        builder.append("  }");
        return builder.toString();
      }
    };
    globalSetter.setElementName("setValueAt");
    globalSetter.addAnnotation(new AnnotationSourceBuilder(Signature.createTypeSignature(Override.class.getName(), true)));
    globalSetter.addParameter(new MethodParameter(Signature.SIG_INT, "row"));
    globalSetter.addParameter(new MethodParameter(Signature.SIG_INT, "column"));
    globalSetter.addParameter(new MethodParameter(Signature.createTypeSignature(Object.class.getName(), true), "value"));
    addBuilder(globalSetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 2, globalSetter.getElementName(), globalSetter));

    // column count
    MethodSourceBuilder columnCount = new MethodSourceBuilder();
    columnCount.setElementName("getColumnCount");
    columnCount.addAnnotation(new AnnotationSourceBuilder(Signature.createTypeSignature(Override.class.getName(), true)));
    columnCount.setReturnSignature(Signature.SIG_INT);
    columnCount.setSimpleBody("return " + columns.length + ";");
    addBuilder(columnCount, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 2, columnCount.getElementName(), columnCount));

  }

  private String getColumnSignature(IType type, ITypeHierarchy columnHierarchy) throws JavaModelException {
    if (type == null || type.getFullyQualifiedName().equals(Object.class.getName())) {
      return null;
    }
    IType superType = columnHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        String superTypeSig = type.getSuperclassTypeSignature();
        return ScoutSdkUtility.getResolvedSignature(Signature.getTypeArguments(superTypeSig)[0], type);
      }
      else {
        return getColumnSignature(superType, columnHierarchy);
      }
    }
    else {
      return null;
    }
  }

  /**
   * @return the tableField
   */
  public IType getTableField() {
    return m_tableField;
  }
}
