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
package org.eclipse.scout.sdk.operation.form.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.util.ScoutSignature;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public class TableFieldDataSourceBuilder extends AbstractSourceBuilder {
  private final IType iTableField = ScoutSdk.getType(RuntimeClasses.ITableField);
  private final IType iTable = ScoutSdk.getType(RuntimeClasses.ITable);
  private final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);

  private final IType m_uiFieldType;
  private List<String> m_columnSimpleNames;
  private List<String> m_columnTypeSignatures;
  private final boolean m_isImplementation;
  private final ITypeHierarchy m_formFieldHierarchy;

  public TableFieldDataSourceBuilder(String typeSimpleName, IType uiFieldType, ITypeHierarchy formFieldHierarchy, ISourceBuilder parentBuilder, boolean isImplementation) {
    this(typeSimpleName, uiFieldType, formFieldHierarchy, parentBuilder.getImportValidator(), isImplementation);
  }

  public TableFieldDataSourceBuilder(String typeSimpleName, IType uiFieldType, ITypeHierarchy formFieldHierarchy, IImportValidator importValidator, boolean isImplementation) {
    super(typeSimpleName, Signature.createTypeSignature(RuntimeClasses.AbstractTableFieldData, true), Flags.AccPublic, importValidator);
    m_uiFieldType = uiFieldType;
    m_formFieldHierarchy = formFieldHierarchy;
    m_isImplementation = isImplementation;
    m_columnTypeSignatures = new ArrayList<String>();
    m_columnSimpleNames = new ArrayList<String>();
  }

  private boolean visitTableField(IType fieldType) throws JavaModelException {
    ITypeFilter filter = TypeFilters.getClassFilter();
    for (IType table : TypeUtility.getInnerTypes(fieldType, filter, TypeComparators.getOrderAnnotationComparator())) {
      TreeMap<CompositeObject, P_TableColumn> columns = new TreeMap<CompositeObject, P_TableColumn>();
      visitTable(table, columns);

      for (P_TableColumn column : columns.values()) {
        m_columnSimpleNames.add(FormDataUtility.getFieldIdWithoutSuffix(column.getColumnType().getElementName()));
        m_columnTypeSignatures.add(createFormDataSignatureFor(column.getColumnType(), column.getTypeHierarchy()));
      }
      return true;
    }
    return false;
  }

  /**
   * Collects all columns by going up the dependency graph as long as it contains an <code>ITable</code>.
   * 
   * @param table
   * @param columns
   *          tree map that collects table columns.
   * @throws JavaModelException
   */
  private void visitTable(IType table, TreeMap<CompositeObject, P_TableColumn> columns) throws JavaModelException {
    org.eclipse.jdt.core.ITypeHierarchy superTypeHierarchy = table.newSupertypeHierarchy(null);
    if (superTypeHierarchy.contains(iTable)) {
      ITypeHierarchy columnHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iColumn).combinedTypeHierarchy(table);
      ITypeFilter columnFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getSubtypeFilter(iColumn, columnHierarchy), TypeFilters.getClassFilter());
      for (IType columnType : TypeUtility.getInnerTypes(table, columnFilter, TypeComparators.getOrderAnnotationComparator())) {
        columns.put(new CompositeObject(ScoutSdkUtility.getOrderAnnotation(columnType), columnType.getElementName()), new P_TableColumn(columnType, columnHierarchy));
      }
      visitTable(superTypeHierarchy.getSuperclass(table), columns);
    }
  }

  @Override
  public void build() throws CoreException {
    addBeanPropertiesFrom(m_uiFieldType, m_isImplementation);
    if (m_isImplementation) {
      // columns
      IType currentType = m_uiFieldType;

      while (TypeUtility.exists(currentType) && m_formFieldHierarchy.isSubtype(iTableField, currentType)) {
        if (visitTableField(currentType)) {
          break;
        }
        currentType = m_formFieldHierarchy.getSuperclass(currentType);
      }
      //
      StringBuilder sb = new StringBuilder();
      sb.append("@Override\n");
      sb.append("public int getColumnCount(){\n");
      sb.append("  return " + m_columnSimpleNames.size() + ";\n");
      sb.append("}");
      addPropertyMethod(sb.toString());
      //
      for (int i = 0; i < m_columnSimpleNames.size(); i++) {
        String columnSimpleName = m_columnSimpleNames.get(i);
        String colNameUpper = FormDataUtility.getBeanName(columnSimpleName, true);
        String colNameLower = FormDataUtility.getBeanName(columnSimpleName, false);
        String gen = m_columnTypeSignatures.get(i);
        String genSimpleName = ScoutSignature.getTypeReference(m_columnTypeSignatures.get(i), null, getImportValidator());
        // GETTER
        sb = new StringBuilder();
        sb.append("public " + genSimpleName + " get" + colNameUpper + "(int row){\n");
        sb.append("  return (" + genSimpleName + ")getValueInternal(row," + i + ");\n");
        sb.append("}");
        addPropertyMethod(sb.toString());
        // SETTER
        sb = new StringBuilder();
        sb.append("public void set" + colNameUpper + "(int row, " + genSimpleName + " " + colNameLower + "){\n");
        sb.append("  setValueInternal(row," + i + "," + colNameLower + ");\n");
        sb.append("}");
        addPropertyMethod(sb.toString());
      }
      // load/store methods
      StringBuilder getterBuf = new StringBuilder();
      getterBuf.append("@Override\n");
      getterBuf.append("public Object getValueAt(int row, int column){\n");
      getterBuf.append("  switch(column){\n");

      StringBuilder setterBuf = new StringBuilder();
      setterBuf.append("@Override\n");
      setterBuf.append("public void setValueAt(int row, int column, Object value) {\n");
      setterBuf.append("  switch(column){\n");
      for (int i = 0; i < m_columnSimpleNames.size(); i++) {
        String columnSimpleName = m_columnSimpleNames.get(i);
        String colNameUpper = FormDataUtility.getBeanName(columnSimpleName, true);
        String gen = m_columnTypeSignatures.get(i);
        String genSimpleName = ScoutSignature.getTypeReference(gen, null, getImportValidator());
        //
        getterBuf.append("    case " + i + ": return get" + colNameUpper + "(row);\n");
        //
        setterBuf.append("    case " + i + ": set" + colNameUpper + "(row,(" + genSimpleName + ")value); break;\n");
      }
      getterBuf.append("    default: return null;\n");
      getterBuf.append("  }\n");
      getterBuf.append("}");
      addPropertyMethod(getterBuf.toString());
      //
      setterBuf.append("  }\n");
      setterBuf.append("}");
      addPropertyMethod(setterBuf.toString());
    }
  }

  public void appendToParent(ISourceBuilder parentBuilder) throws CoreException {
    //add type
    parentBuilder.addInnerClass(createDocumentText());
    //add type getter
    parentBuilder.addFieldGetterMethod("public " + getSimpleTypeName() + " get" + getSimpleTypeName() + "(){\n" + ScoutIdeProperties.TAB + "return getFieldByClass(" + getSimpleTypeName() + ".class);\n}");
  }

  private String createFormDataSignatureFor(IType type, ITypeHierarchy columnHierarchy) throws JavaModelException {
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
        return createFormDataSignatureFor(superType, columnHierarchy);
      }
    }
    else {
      return null;
    }
  }

  private final static class P_TableColumn {
    private final IType m_columnType;
    private final ITypeHierarchy m_typeHierarchy;

    public P_TableColumn(IType columnType, ITypeHierarchy typeHierarchy) {
      m_columnType = columnType;
      m_typeHierarchy = typeHierarchy;
    }

    public IType getColumnType() {
      return m_columnType;
    }

    public ITypeHierarchy getTypeHierarchy() {
      return m_typeHierarchy;
    }
  }
}
