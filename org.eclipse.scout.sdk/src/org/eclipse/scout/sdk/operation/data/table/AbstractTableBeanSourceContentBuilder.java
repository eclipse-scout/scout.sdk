/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.data.table;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.form.formdata.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.FieldSourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUtility;
import org.eclipse.scout.sdk.operation.form.formdata.ITypeSourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.MethodParameter;
import org.eclipse.scout.sdk.operation.form.formdata.MethodSourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.TableFieldBeanSourceBuilder;
import org.eclipse.scout.sdk.operation.form.formdata.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractTableBeanSourceContentBuilder}</h3>
 * 
 * @since 3.10.0-M1
 */
public abstract class AbstractTableBeanSourceContentBuilder {

  private static final String TABLE_COLUMN_PROPERTY_CONSTANT_NAME_SUFFIX = "_";
  private static final String TABLE_ROW_BEAN_BANE_SUFFIX = "RowData";
  private static final String STRING_SIGNATURE = Signature.createTypeSignature("String", false);

  private final IType iTable = TypeUtility.getType(RuntimeClasses.ITable);
  private final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);

  private final IType m_tableContainerType;
  private final ITypeHierarchy m_hierarchy;
  private final IType m_tableContainerStopType;
  protected final String NL;

  /** counter used for sorting method sub-builders. */
  private int m_methodSortCounter = 0;

  public AbstractTableBeanSourceContentBuilder(IType tableContainerType, ITypeHierarchy hierarchy, String nl, IType tableContainerStopType) {
    m_tableContainerType = tableContainerType;
    m_hierarchy = hierarchy;
    NL = nl;
    m_tableContainerStopType = tableContainerStopType;
  }

  protected IType getTableContainerType() {
    return m_tableContainerType;
  }

  protected ITypeHierarchy getHierarchy() {
    return m_hierarchy;
  }

  protected IType getTableContainerStopType() {
    return m_tableContainerStopType;
  }

  /**
   * @param tableFieldBeanSourceBuilder
   * @param validator
   */
  public void addContentBuilders(ITypeSourceBuilder parentBuilder, IImportValidator validator) {
    IType table = findTable(getTableContainerType(), getTableContainerStopType(), getHierarchy());
    if (!TypeUtility.exists(table)) {
      addAbstractMethodImplementations(parentBuilder);
    }
    else if (getTableContainerType().equals(table.getDeclaringType())) {
      visitTable(table, getHierarchy(), parentBuilder, validator);
    }
  }

  protected IType findTable(IType tableContainerType, IType tableContainerStopType, ITypeHierarchy hierarchy) {
    if (TypeUtility.exists(tableContainerType)) {
      IType[] tables = TypeUtility.getInnerTypes(tableContainerType, TypeFilters.getSubtypeFilter(iTable, hierarchy), null);
      if (tables.length > 0) {
        if (tables.length > 1) {
          ScoutSdk.logWarning("Found more than one table within '" + tableContainerType.getFullyQualifiedName() + "'! Using first one.");
        }
        return tables[0];
      }
      else {
        IType superType = hierarchy.getSuperclass(tableContainerType);
        if (TypeUtility.exists(superType) && hierarchy.isSubtype(tableContainerStopType, superType)) {
          ITypeHierarchy superTypeHierarchy = TypeUtility.getLocalTypeHierarchy(superType);
          return findTable(superType, tableContainerStopType, superTypeHierarchy);
        }
      }
    }
    return null;
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

  protected void addAbstractMethodImplementations(ITypeSourceBuilder parentBuilder) {
    // createRow
    String simpleTableRowDataName = RuntimeClasses.AbstractTableRowData.substring(RuntimeClasses.AbstractTableRowData.lastIndexOf(".") + 1);
    MethodSourceBuilder createRow = addMethodOverride(parentBuilder, "createRow");
    createRow.setReturnSignature(Signature.createTypeSignature(RuntimeClasses.AbstractTableRowData, true));
    createRow.setSimpleBody("return new " + simpleTableRowDataName + "() {private static final long serialVersionUID = 1L;};");

    // getRowType
    MethodSourceBuilder getRowType = addMethodOverride(parentBuilder, "getRowType");
    getRowType.setReturnSignature(Signature.createTypeSignature(Class.class.getName() + "<? extends " + RuntimeClasses.AbstractTableRowData + ">", true));
    getRowType.setSimpleBody("return " + simpleTableRowDataName + ".class;");
  }

  protected void visitTable(IType table, ITypeHierarchy hierarchy, ITypeSourceBuilder parentBuilder, IImportValidator validator) {
    final IType[] columns = TypeUtility.getInnerTypes(table, TypeFilters.getSubtypeFilter(iColumn, hierarchy), ScoutTypeComparators.getOrderAnnotationComparator());

    // table bean
    String tableRowBeanName = getTableRowBeanName(table);
    String tableBeanSignature = Signature.createTypeSignature(tableRowBeanName, false);
    String tableBeanArraySignature = Signature.createArraySignature(tableBeanSignature, 1);

    TypeSourceBuilder tableBeanBuilder = new TypeSourceBuilder(NL);
    tableBeanBuilder.setElementName(tableRowBeanName);
    tableBeanBuilder.setSuperTypeSignature(getTableRowDataSuperClassSignature(validator, table, hierarchy));
    tableBeanBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
    parentBuilder.addBuilder(tableBeanBuilder, ITypeSourceBuilder.CATEGORY_TYPE_FIELD);

    for (int i = 0; i < columns.length; i++) {
      IType column = columns[i];
      try {
        if (ScoutTypeUtility.isReplaceAnnotationPresent(column)) {
          // replaced columns already have a column data
          continue;
        }

        String fieldNameWithoutSuffix = FormDataUtility.getFieldNameWithoutSuffix(column.getElementName());
        String upperColName = FormDataUtility.getBeanName(fieldNameWithoutSuffix, true);
        String lowerColName = FormDataUtility.getBeanName(fieldNameWithoutSuffix, false);
        String methodParameterName = FormDataUtility.getValidMethodParameterName(lowerColName);
        String propertyName = "m_" + methodParameterName;
        String colSignature = getColumnSignature(column, hierarchy);

        // property constant
        FieldSourceBuilder propConstant = new FieldSourceBuilder();
        propConstant.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
        String constantColName = lowerColName;
        if (FormDataUtility.isReservedJavaKeyword(constantColName)) {
          constantColName += TABLE_COLUMN_PROPERTY_CONSTANT_NAME_SUFFIX;
        }
        propConstant.setElementName(constantColName);
        propConstant.setSignature(STRING_SIGNATURE);
        propConstant.setAssignment("\"" + lowerColName + "\"");
        tableBeanBuilder.addBuilder(propConstant, new CompositeObject(ITypeSourceBuilder.CATEGORY_TYPE_TABLE_COLUMN, 1, i, 0, propConstant));

        // property
        FieldSourceBuilder prop = new FieldSourceBuilder();
        prop.setElementName(propertyName);
        prop.setSignature(colSignature);
        tableBeanBuilder.addBuilder(prop, new CompositeObject(ITypeSourceBuilder.CATEGORY_TYPE_TABLE_COLUMN, 2, i, 0, prop));

        // getter
        MethodSourceBuilder propGetter = new MethodSourceBuilder(NL);
        propGetter.setSimpleBody("return " + propertyName + ";");
        propGetter.setElementName("get" + upperColName);
        propGetter.setReturnSignature(colSignature);
        tableBeanBuilder.addBuilder(propGetter, new CompositeObject(ITypeSourceBuilder.CATEGORY_TYPE_TABLE_COLUMN, 3, i, 1, propGetter));

        // setter
        MethodSourceBuilder propSetter = new MethodSourceBuilder(NL);
        propSetter.setElementName("set" + upperColName);
        propSetter.addParameter(new MethodParameter(colSignature, methodParameterName));
        propSetter.setSimpleBody(propertyName + " = " + methodParameterName + ";");
        tableBeanBuilder.addBuilder(propSetter, new CompositeObject(ITypeSourceBuilder.CATEGORY_TYPE_TABLE_COLUMN, 3, i, 2, propSetter));
      }
      catch (JavaModelException e) {
        ScoutSdk.logWarning("could not add column '" + column.getFullyQualifiedName() + "' to form data.", e);
      }
    }

    // getRows
    MethodSourceBuilder getRows = addMethodOverride(parentBuilder, "getRows");
    getRows.setReturnSignature(tableBeanArraySignature);
    getRows.setSimpleBody("return (" + tableRowBeanName + "[]) super.getRows();");

    // setRows
    MethodSourceBuilder setRows = new MethodSourceBuilder(NL);
    setRows.setElementName("setRows");
    setRows.addParameter(new MethodParameter(tableBeanArraySignature, "rows"));
    setRows.setSimpleBody("super.setRows(rows);");
    addMethodBuilder(parentBuilder, setRows);

    // addRow
    MethodSourceBuilder addRow = addMethodOverride(parentBuilder, "addRow");
    addRow.setReturnSignature(tableBeanSignature);
    addRow.setSimpleBody("return (" + tableRowBeanName + ") super.addRow();");

    // addRowWithRowState
    MethodSourceBuilder addRowWithRowState = addMethodOverride(parentBuilder, "addRow");
    addRowWithRowState.setReturnSignature(tableBeanSignature);
    addRowWithRowState.addParameter(new MethodParameter(Signature.SIG_INT, "rowState"));
    addRowWithRowState.setSimpleBody("return (" + tableRowBeanName + ") super.addRow(rowState);");

    // rowAt
    MethodSourceBuilder rowAt = addMethodOverride(parentBuilder, "rowAt");
    rowAt.setReturnSignature(tableBeanSignature);
    rowAt.addParameter(new MethodParameter(Signature.SIG_INT, "idx"));
    rowAt.setSimpleBody("return (" + tableRowBeanName + ") super.rowAt(idx);");

    // createRow
    MethodSourceBuilder createRow = addMethodOverride(parentBuilder, "createRow");
    createRow.setReturnSignature(tableBeanSignature);
    createRow.setSimpleBody("return new " + tableRowBeanName + "();");

    // getRowType
    MethodSourceBuilder getRowType = addMethodOverride(parentBuilder, "getRowType");
    getRowType.setReturnSignature(Signature.createTypeSignature(Class.class.getName() + "<? extends " + RuntimeClasses.AbstractTableRowData + ">", true));
    getRowType.setSimpleBody("return " + tableRowBeanName + ".class;");
  }

  /**
   * Creates, adds and returns a {@link MethodSourceBuilder} with the given name. The {@link Override} annotation
   * builder is already attached. Note: parameters, return type and the method body must be set by the caller.
   * 
   * @param methodName
   * @return
   */
  private MethodSourceBuilder addMethodOverride(ITypeSourceBuilder parentBuilder, String methodName) {
    MethodSourceBuilder method = new MethodSourceBuilder(NL);
    method.setElementName(methodName);
    method.addAnnotation(new AnnotationSourceBuilder(Signature.createTypeSignature(Override.class.getName(), true)));
    addMethodBuilder(parentBuilder, method);
    return method;
  }

  /**
   * Adds the given method builder to this {@link TableFieldBeanSourceBuilder} and ensures, that the generated methods
   * are sorted in the same order they are added.
   * 
   * @param methodBuilder
   */
  private void addMethodBuilder(ITypeSourceBuilder parentBuilder, MethodSourceBuilder methodBuilder) {
    parentBuilder.addBuilder(methodBuilder, new CompositeObject(ITypeSourceBuilder.CATEGORY_MEHTOD, m_methodSortCounter++, methodBuilder.getElementName(), methodBuilder));
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
   * Computes the super class signature of the {@link IRuntimeClasses#AbstractTableRowData} bean created by this
   * builder.
   * 
   * @param validator
   * @param table
   * @param hierarchy
   * @return
   */
  protected abstract String getTableRowDataSuperClassSignature(IImportValidator validator, IType table, ITypeHierarchy hierarchy);
}
