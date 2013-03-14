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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * Source builder that creates form data classes for an ITableField. The generated form data element stores the data in
 * ordinary java beans (which are implementing {@link RuntimeClasses#AbstractTableRowData}.
 * 
 * @since 3.8.2
 */
public class TableFieldBeanSourceBuilder extends SourceBuilderWithProperties {
  private static final String TABLE_COLUMN_PROPERTY_CONSTANT_NAME_PREFIX = "PROP_";
  private static final String TABLE_ROW_BEAN_BANE_SUFFIX = "RowData";
  private static final String STRING_SIGNATURE = Signature.createTypeSignature("String", false);

  private final IType iTable = TypeUtility.getType(RuntimeClasses.ITable);
  private final IType iTableField = TypeUtility.getType(RuntimeClasses.ITableField);
  private final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);

  /** counter used for sorting method sub-builders. */
  private int m_methodSortCounter = 0;

  /** list of additional imports required by the generated code. */
  private final List<String> m_additionalImports = new ArrayList<String>();

  public TableFieldBeanSourceBuilder(IType tableField, ITypeHierarchy hierarchy) {
    super(tableField);
    // find table
    IType table = findTable(tableField, hierarchy);
    if (!TypeUtility.exists(table)) {
      addAbstractMethodImplementations();
    }
    else if (hierarchy.contains(table)) {
      visitTable(table, hierarchy);
    }
  }

  @Override
  public String createSource(IImportValidator validator) throws JavaModelException {
    for (String additionalImport : m_additionalImports) {
      validator.addImport(additionalImport);
    }
    return super.createSource(validator);
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
        IType superType = hierarchy.getSuperclass(tableField);
        if (TypeUtility.exists(superType) && hierarchy.isSubtype(iTableField, superType)) {
          ITypeHierarchy superTypeHierarchy = TypeUtility.getLocalTypeHierarchy(superType);
          return findTable(superType, superTypeHierarchy);
        }
      }
    }
    return null;
  }

  protected void addAbstractMethodImplementations() {
    // createRow
    String simpleTableRowDataName = RuntimeClasses.AbstractTableRowData.substring(RuntimeClasses.AbstractTableRowData.lastIndexOf(".") + 1);
    MethodSourceBuilder createRow = addMethodOverride("createRow");
    createRow.setReturnSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTableRowData, true));
    createRow.setSimpleBody("return new " + simpleTableRowDataName + "() {private static final long serialVersionUID = 1L;};");

    // getRowType
    MethodSourceBuilder getRowType = addMethodOverride("getRowType");
    getRowType.setReturnSignature(Signature.createTypeSignature(Class.class.getName() + "<? extends " + RuntimeClasses.AbstractTableRowData + ">", true));
    getRowType.setSimpleBody("return " + simpleTableRowDataName + ".class;");
  }

  protected void visitTable(IType table, ITypeHierarchy hierarchy) {
    final IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(iColumn, hierarchy), ScoutTypeComparators.getOrderAnnotationComparator());

    // table bean
    String tableRowBeanName = getTableRowBeanName(table);
    String tableBeanSignature = Signature.createTypeSignature(tableRowBeanName, false);
    String tableBeanArraySignature = Signature.createArraySignature(tableBeanSignature, 1);

    TypeSourceBuilder tableBeanBuilder = new TypeSourceBuilder(NL);
    tableBeanBuilder.setElementName(tableRowBeanName);
    tableBeanBuilder.setSuperTypeSignature(getTableRowDataSuperClassSignature(table, hierarchy));
    tableBeanBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
    addBuilder(tableBeanBuilder, CATEGORY_TYPE_FIELD);

    for (int i = 0; i < columns.length; i++) {
      IType column = columns[i];
      try {
        if (ScoutTypeUtility.isReplaceAnnotationPresent(column)) {
          // replaced columns already have a column data
          continue;
        }

        String fieldNameWithoutSuffix = FormDataUtility.getFieldNameWithoutSuffix(column.getElementName());
        String upperColName = FormDataUtility.getBeanName(fieldNameWithoutSuffix, true);
        String constantColName = TABLE_COLUMN_PROPERTY_CONSTANT_NAME_PREFIX + FormDataUtility.getConstantName(upperColName);
        String lowerColName = FormDataUtility.getBeanName(fieldNameWithoutSuffix, false);
        String methodParameterName = FormDataUtility.getValidMethodParameterName(lowerColName);
        String propertyName = "m_" + methodParameterName;
        String colSignature = getColumnSignature(column, hierarchy);

        // property constant
        FieldSourceBuilder propConstant = new FieldSourceBuilder();
        propConstant.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
        propConstant.setElementName(constantColName);
        propConstant.setSignature(STRING_SIGNATURE);
        propConstant.setAssignment("\"" + lowerColName + "\"");
        tableBeanBuilder.addBuilder(propConstant, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 1, i, 0, propConstant));

        // property
        FieldSourceBuilder prop = new FieldSourceBuilder();
        prop.setElementName(propertyName);
        prop.setSignature(colSignature);
        tableBeanBuilder.addBuilder(prop, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 2, i, 0, prop));

        // getter
        MethodSourceBuilder propGetter = new MethodSourceBuilder(NL);
        propGetter.setSimpleBody("return " + propertyName + ";");
        propGetter.setElementName("get" + upperColName);
        propGetter.setReturnSignature(colSignature);
        tableBeanBuilder.addBuilder(propGetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 3, i, 1, propGetter));

        // setter
        MethodSourceBuilder propSetter = new MethodSourceBuilder(NL);
        propSetter.setElementName("set" + upperColName);
        propSetter.addParameter(new MethodParameter(colSignature, methodParameterName));
        propSetter.setSimpleBody(propertyName + " = " + methodParameterName + ";");
        tableBeanBuilder.addBuilder(propSetter, new CompositeObject(CATEGORY_TYPE_TABLE_COLUMN, 3, i, 2, propSetter));
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not add column '" + column.getFullyQualifiedName() + "' to form data.", e);
      }
    }

    // getRows
    MethodSourceBuilder getRows = addMethodOverride("getRows");
    getRows.setReturnSignature(tableBeanArraySignature);
    getRows.setSimpleBody("return (" + tableRowBeanName + "[]) super.getRows();");

    // setRows
    MethodSourceBuilder setRows = new MethodSourceBuilder(NL);
    setRows.setElementName("setRows");
    setRows.addParameter(new MethodParameter(tableBeanArraySignature, "rows"));
    setRows.setSimpleBody("super.setRows(rows);");
    addMethodBuilder(setRows);

    // addRow
    MethodSourceBuilder addRow = addMethodOverride("addRow");
    addRow.setReturnSignature(tableBeanSignature);
    addRow.setSimpleBody("return (" + tableRowBeanName + ") super.addRow();");

    // addRowWithRowState
    MethodSourceBuilder addRowWithRowState = addMethodOverride("addRow");
    addRowWithRowState.setReturnSignature(tableBeanSignature);
    addRowWithRowState.addParameter(new MethodParameter(Signature.SIG_INT, "rowState"));
    addRowWithRowState.setSimpleBody("return (" + tableRowBeanName + ") super.addRow(rowState);");

    // rowAt
    MethodSourceBuilder rowAt = addMethodOverride("rowAt");
    rowAt.setReturnSignature(tableBeanSignature);
    rowAt.addParameter(new MethodParameter(Signature.SIG_INT, "idx"));
    rowAt.setSimpleBody("return (" + tableRowBeanName + ") super.rowAt(idx);");

    // createRow
    MethodSourceBuilder createRow = addMethodOverride("createRow");
    createRow.setReturnSignature(tableBeanSignature);
    createRow.setSimpleBody("return new " + tableRowBeanName + "();");

    // getRowType
    MethodSourceBuilder getRowType = addMethodOverride("getRowType");
    getRowType.setReturnSignature(Signature.createTypeSignature(Class.class.getName() + "<? extends " + RuntimeClasses.AbstractTableRowData + ">", true));
    getRowType.setSimpleBody("return " + tableRowBeanName + ".class;");
  }

  protected String getTableRowBeanName(IType table) {
    String tableRowBeanName = table.getElementName();
    if (iTable.equals(table) || "table".equalsIgnoreCase(tableRowBeanName)) {
      // use table fields name
      tableRowBeanName = FormDataUtility.getFieldNameWithoutSuffix(table.getDeclaringType().getElementName());
    }
    tableRowBeanName = tableRowBeanName + TABLE_ROW_BEAN_BANE_SUFFIX;
    return tableRowBeanName;
  }

  /**
   * Creates, adds and returns a {@link MethodSourceBuilder} with the given name. The {@link Override} annotation
   * builder is already attached. Note: parametrs, return type and the method body must be set by the caller.
   * 
   * @param methodName
   * @return
   */
  private MethodSourceBuilder addMethodOverride(String methodName) {
    MethodSourceBuilder method = new MethodSourceBuilder(NL);
    method.setElementName(methodName);
    method.addAnnotation(new AnnotationSourceBuilder(Signature.createTypeSignature(Override.class.getName(), true)));
    addMethodBuilder(method);
    return method;
  }

  /**
   * Adds the given method builder to this {@link TableFieldBeanSourceBuilder} and ensures, that the generated methods
   * are sorted in the same order they are added.
   * 
   * @param methodBuilder
   */
  private void addMethodBuilder(MethodSourceBuilder methodBuilder) {
    addBuilder(methodBuilder, new CompositeObject(CATEGORY_MEHTOD, m_methodSortCounter++, methodBuilder.getElementName(), methodBuilder));
  }

  /**
   * Computes the given column's type signature that is also the type of the property generated in the table row data.
   * 
   * @param type
   * @param columnHierarchy
   * @return
   * @throws JavaModelException
   */
  private String getColumnSignature(IType type, ITypeHierarchy columnHierarchy) throws JavaModelException {
    if (type == null || Object.class.getName().equals(type.getFullyQualifiedName())) {
      return null;
    }
    IType superType = columnHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        String superTypeSig = type.getSuperclassTypeSignature();
        return SignatureUtility.getResolvedSignature(Signature.getTypeArguments(superTypeSig)[0], type);
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
   * Computes the super class signature of the AbstractTableRowData bean created by this builder.
   * 
   * @param table
   * @param hierarchy
   * @return
   */
  private String getTableRowDataSuperClassSignature(IType table, ITypeHierarchy hierarchy) {
    try {
      IType parentTable = hierarchy.getSuperclass(table);
      if (TypeUtility.exists(parentTable)) {
        IType declaringType = parentTable.getDeclaringType();
        if (TypeUtility.exists(declaringType)) {
          IType formDataType = ScoutTypeUtility.getFormDataType(declaringType, hierarchy);
          String parentTableRowBeanName = getTableRowBeanName(parentTable);
          IType parentTableBeanData = formDataType.getType(parentTableRowBeanName);
          if (TypeUtility.exists(parentTableBeanData)) {
            m_additionalImports.add(formDataType.getFullyQualifiedName());
            return Signature.createTypeSignature(parentTableBeanData.getTypeQualifiedName(), false);
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("error while computing super class signature for [" + table + "]", e);
    }
    return Signature.createTypeSignature(RuntimeClasses.AbstractTableRowData, true);
  }
}
