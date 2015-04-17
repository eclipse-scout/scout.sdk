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
package org.eclipse.scout.sdk.internal.workspace.dto.pagedata;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.FieldFilters;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link TableRowDataTypeSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 19.11.2014
 */
public class TableRowDataTypeSourceBuilder extends TypeSourceBuilder {

  protected static final int ROW_DATA_FIELD_FLAGS = Flags.AccPublic | Flags.AccFinal | Flags.AccStatic;

  private final IType m_columnContainer; // e.g. ITable or ITableExtension
  private final IType m_modelType; // e.g. IPageWithTable, ITableField, ITableExtension
  private final ITypeHierarchy m_modelLocalHierarchy;
  private final IProgressMonitor m_monitor;

  public TableRowDataTypeSourceBuilder(String elementName, IType columnContainer, IType modelType, ITypeHierarchy modelLocalHierarchy, IProgressMonitor monitor) throws CoreException {
    super(elementName);
    m_columnContainer = columnContainer;
    m_modelType = modelType;
    m_modelLocalHierarchy = modelLocalHierarchy;
    m_monitor = monitor;
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    setup(getMonitor());
    super.createSource(source, lineDelimiter, ownerProject, validator);
  }

  private void setup(IProgressMonitor monitor) throws CoreException {
    // row data super type
    IType rowDataSuperClassType = null;
    String rowDataSuperClassSig = computeTableRowDataSuperClassSignature();
    if (rowDataSuperClassSig != null) {
      rowDataSuperClassType = TypeUtility.getTypeBySignature(rowDataSuperClassSig);
    }

    // row data class flags
    int flags = Flags.AccPublic;
    if (getParentTypeSourceBuilder() != null) {
      flags |= Flags.AccStatic;
    }
    if (Flags.isAbstract(getColumnContainer().getFlags()) || Flags.isAbstract(getModelType().getFlags())) {
      flags |= Flags.AccAbstract;
    }
    setFlags(flags);
    setSuperTypeSignature(rowDataSuperClassSig);
    if (rowDataSuperClassSig == null) {
      addInterfaceSignature(SignatureCache.createTypeSignature(Serializable.class.getName()));
    }

    // serialVersionUidBuilder
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    if (monitor.isCanceled()) {
      return;
    }

    // get all columns
    Set<IType> columns = getColumns(getColumnContainer(), rowDataSuperClassType, getModelLocalHierarchy(), monitor);
    if (monitor.isCanceled()) {
      return;
    }

    // visit columns
    int i = 0;
    for (IType column : columns) {
      String columnBeanName = getColumnBeanName(column);
      String constantColName = columnBeanName;
      if (NamingUtility.isReservedJavaKeyword(constantColName)) {
        constantColName += "_";
      }
      IFieldSourceBuilder constantFieldBuilder = new FieldSourceBuilder(constantColName);
      constantFieldBuilder.setFlags(ROW_DATA_FIELD_FLAGS);
      constantFieldBuilder.setSignature(SignatureCache.createTypeSignature(String.class.getName()));
      constantFieldBuilder.setValue("\"" + columnBeanName + "\"");
      addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_CONSTANT + 1, i, columnBeanName), constantFieldBuilder);

      // member
      IFieldSourceBuilder memberFieldBuilder = new FieldSourceBuilder("m_" + columnBeanName);
      memberFieldBuilder.setFlags(Flags.AccPrivate);

      // try to find the column value type with the local hierarchy first.
      String columnValueTypeSignature = ScoutTypeUtility.getColumnValueTypeSignature(column, getColumnContainer(), getModelLocalHierarchy());
      if (columnValueTypeSignature == null) {
        // this cannot find anything, in case the column is inherited from a parent table. try again with super hierarchy
        columnValueTypeSignature = ScoutTypeUtility.getColumnValueTypeSignature(column, getColumnContainer(), TypeUtility.getSupertypeHierarchy(column));
      }
      memberFieldBuilder.setSignature(columnValueTypeSignature);
      addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_MEMBER + 1, i, columnBeanName), memberFieldBuilder);

      // getter
      IMethodSourceBuilder getterBuilder = MethodSourceBuilderFactory.createGetter(memberFieldBuilder);
      addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 1, getterBuilder), getterBuilder);

      // setter
      IMethodSourceBuilder setterBuilder = MethodSourceBuilderFactory.createSetter(memberFieldBuilder);
      addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 2, setterBuilder), setterBuilder);

      i++;
      if (monitor.isCanceled()) {
        return;
      }
    }
  }

  protected static String getColumnBeanName(IType column) {
    return NamingUtility.ensureStartWithLowerCase(ScoutUtility.removeFieldSuffix(column.getElementName()));
  }

  protected static Set<IType> getColumns(IType declaringType, IType rowDataSuperType, ITypeHierarchy modelLocalHierarchy, IProgressMonitor monitor) throws JavaModelException {

    final ITypeHierarchy fieldHierarchy = TypeUtility.getSupertypeHierarchy(declaringType);

    // the declaring type is a column itself
    if (fieldHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.IColumn), declaringType)) {
      return CollectionUtility.hashSet(declaringType);
    }

    // the declaring type is a IPageWithTableExtension -> search the inner table extension
    if (fieldHierarchy.isSubtype(TypeUtility.getType(IRuntimeClasses.IPageWithTableExtension), declaringType)) {
      Set<IType> innerTableExtensions = TypeUtility.getInnerTypesOrdered(declaringType, TypeUtility.getType(IRuntimeClasses.ITableExtension), ScoutTypeComparators.getSourceRangeComparator(), modelLocalHierarchy);
      IType tableExtension = CollectionUtility.firstElement(innerTableExtensions);
      if (TypeUtility.exists(tableExtension)) {
        declaringType = tableExtension; // switch to the table as column holder
      }
    }

    // the declaring type holds columns
    TreeSet<IType> allColumnsUpTheHierarchy = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator());
    // do not re-use the fieldHierarchy for the subtype filter!
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IColumn)), new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        try {
          SdkColumnCommand command = ScoutTypeUtility.findColumnDataSdkColumnCommand(type, TypeUtility.getSupertypeHierarchy(type));
          return command == null || command == SdkColumnCommand.CREATE;
        }
        catch (JavaModelException e) {
          ScoutSdk.logError("Unable to find column data annotation for element '" + type.getFullyQualifiedName() + "'.", e);
          return false;
        }
      }
    });

    // collect all columns that exist in the table and all of its super classes
    Deque<IType> superClassStack = fieldHierarchy.getSuperClassStack(declaringType);
    for (IType curTableType : superClassStack) {
      Set<IType> columns = TypeUtility.getInnerTypes(curTableType, filter);
      allColumnsUpTheHierarchy.addAll(columns);
      if (monitor.isCanceled()) {
        return null;
      }
    }

    if (rowDataSuperType == null) {
      // no need to filter the columns of the super classes
      return allColumnsUpTheHierarchy;
    }

    // collect all columns that exist in the row data and all of its super classes
    Set<String> usedColumnBeanNames = new HashSet<>();
    ITypeHierarchy rowDataHierarchy = TypeUtility.getSupertypeHierarchy(rowDataSuperType);
    Deque<IType> rowDataSuperClasses = rowDataHierarchy.getSuperClassStack(rowDataSuperType, true, IRuntimeClasses.AbstractTableRowData);
    for (IType currentRowDataSuperType : rowDataSuperClasses) {
      Set<IField> columnFields = TypeUtility.getFields(currentRowDataSuperType, FieldFilters.getFlagsFilter(ROW_DATA_FIELD_FLAGS));
      for (IField column : columnFields) {
        if (monitor.isCanceled()) {
          return null;
        }

        Object val = TypeUtility.getFieldConstant(column);
        if (val instanceof String) {
          usedColumnBeanNames.add(val.toString());
        }
      }
    }

    // filter the already existing columns out
    Iterator<IType> allColumnsIterator = allColumnsUpTheHierarchy.iterator();
    while (allColumnsIterator.hasNext()) {
      IType col = allColumnsIterator.next();
      String beanName = getColumnBeanName(col);
      if (usedColumnBeanNames.contains(beanName)) {
        // the current column is already in a row data of our parent -> we don't need it for us: remove
        allColumnsIterator.remove();
      }
      if (monitor.isCanceled()) {
        return null;
      }
    }

    return allColumnsUpTheHierarchy;
  }

  protected String computeTableRowDataSuperClassSignature() throws CoreException {
    ITypeSourceBuilder surroundingTableBeanSourceBuilder = getParentTypeSourceBuilder();
    if (surroundingTableBeanSourceBuilder == null) {
      // row data extension. no super class
      return null;
    }

    String superTypeOfSurroundingTableBeanSourceBuilder = surroundingTableBeanSourceBuilder.getSuperTypeSignature();
    if (!SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTablePageData).equals(superTypeOfSurroundingTableBeanSourceBuilder)
        && !SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableFieldBeanData).equals(superTypeOfSurroundingTableBeanSourceBuilder)) {
      // use the row data in the super page data.
      IType superType = TypeUtility.getTypeBySignature(superTypeOfSurroundingTableBeanSourceBuilder);
      IType abstractTableRowData = TypeUtility.getType(IRuntimeClasses.AbstractTableRowData);

      IType rowDataInSuperTableBeanData = CollectionUtility.firstElement(TypeUtility.getInnerTypes(superType, TypeFilters.getSubtypeFilter(abstractTableRowData)));
      if (TypeUtility.exists(rowDataInSuperTableBeanData)) {
        return SignatureCache.createTypeSignature(rowDataInSuperTableBeanData.getFullyQualifiedName());
      }
    }
    return SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData);
  }

  public IType getColumnContainer() {
    return m_columnContainer;
  }

  public IType getModelType() {
    return m_modelType;
  }

  public ITypeHierarchy getModelLocalHierarchy() {
    return m_modelLocalHierarchy;
  }

  public IProgressMonitor getMonitor() {
    return m_monitor;
  }
}
