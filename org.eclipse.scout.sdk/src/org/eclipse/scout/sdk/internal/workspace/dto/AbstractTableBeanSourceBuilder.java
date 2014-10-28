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
package org.eclipse.scout.sdk.internal.workspace.dto;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.FieldFilters;
import org.eclipse.scout.sdk.util.type.IMethodFilter;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link AbstractTableBeanSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractTableBeanSourceBuilder extends AbstractTableSourceBuilder {

  protected static final int ROW_DATA_FIELD_FLAGS = Flags.AccPublic | Flags.AccFinal | Flags.AccStatic;
  protected static final String ABS_TABLE_ROW_DATA_SIMPLE_NAME = Signature.getSimpleName(IRuntimeClasses.AbstractTableRowData);

  /**
   * @param elementName
   */
  public AbstractTableBeanSourceBuilder(IType modelType, String elementName, boolean setup, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    super(modelType, elementName, setup, derivedCu, monitor);
  }

  @Override
  protected void createContent(IProgressMonitor monitor) {
    super.createContent(monitor);
    try {
      IType table = DtoUtility.findTable(getModelType(), getLocalTypeHierarchy());
      if (TypeUtility.exists(table)) {
        visitTableBean(table, getLocalTypeHierarchy(), monitor);
      }
      else {
        addAbstractMethodImplementations();
      }
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not build form data for '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
  }

  protected Set<IType> getColumns(IType table, IType rowDataSuperType, final ITypeHierarchy fieldHierarchy, IProgressMonitor monitor) throws JavaModelException {
    // collect all columns that exist in the table and all of its super classes
    TreeSet<IType> allColumnsUpTheHierarchy = new TreeSet<IType>(ScoutTypeComparators.getOrderAnnotationComparator());
    ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IColumn)), new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        SdkColumnCommand command = ScoutTypeUtility.findColumnDataSdkColumnCommand(type, fieldHierarchy);
        return command == null || command == SdkColumnCommand.CREATE;
      }
    });
    IType curTableType = table;
    do {
      Set<IType> columns = TypeUtility.getInnerTypes(curTableType, filter);
      for (IType column : columns) {
        allColumnsUpTheHierarchy.add(column);
      }
      curTableType = fieldHierarchy.getSuperclass(curTableType);
      if (monitor.isCanceled()) {
        return null;
      }
    }
    while (TypeUtility.exists(curTableType));

    // collect all columns that exist in the row data and all of its super classes
    HashSet<String> usedColumnBeanNames = new HashSet<String>();
    IType currentRowDataSuperType = rowDataSuperType;
    ITypeHierarchy rowDataHierarchy = TypeUtility.getSupertypeHierarchy(rowDataSuperType);
    if (!IRuntimeClasses.AbstractTableRowData.equals(currentRowDataSuperType.getFullyQualifiedName())) {
      do {
        Set<IField> columnFields = TypeUtility.getFields(currentRowDataSuperType, FieldFilters.getFlagsFilter(ROW_DATA_FIELD_FLAGS));
        for (IField column : columnFields) {
          Object val = TypeUtility.getFieldConstant(column);
          if (val instanceof String) {
            usedColumnBeanNames.add(val.toString());
          }
        }
        currentRowDataSuperType = rowDataHierarchy.getSuperclass(currentRowDataSuperType);
        if (monitor.isCanceled()) {
          return null;
        }
      }
      while (TypeUtility.exists(currentRowDataSuperType) && !IRuntimeClasses.AbstractTableRowData.equals(currentRowDataSuperType.getFullyQualifiedName()));
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

  protected String getColumnBeanName(IType column) {
    return NamingUtility.ensureStartWithLowerCase(ScoutUtility.removeFieldSuffix(column.getElementName()));
  }

  protected void visitTableBean(IType table, ITypeHierarchy fieldHierarchy, IProgressMonitor monitor) throws CoreException {
    // row data super type
    String rowDataSuperClassSig = getTableRowDataSuperClassSignature(table);
    IType rowDataSuperClassType = TypeUtility.getTypeBySignature(rowDataSuperClassSig);

    // row data class name
    String rowDataName = getElementName().replaceAll("(PageData|FieldData|Data)$", "") + "RowData";
    ITypeSourceBuilder tableRowDataBuilder = new TypeSourceBuilder(rowDataName);

    // row data class flags
    int flags = Flags.AccPublic | Flags.AccStatic;
    boolean isAbstract = Flags.isAbstract(table.getFlags()) || Flags.isAbstract(getModelType().getFlags());
    if (isAbstract) {
      flags |= Flags.AccAbstract;
    }
    tableRowDataBuilder.setFlags(flags);
    tableRowDataBuilder.setSuperTypeSignature(rowDataSuperClassSig);

    // serialVersionUidBuilder
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    tableRowDataBuilder.addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(rowDataName);
    tableRowDataBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
    if (monitor.isCanceled()) {
      return;
    }

    // get all columns
    Set<IType> columns = getColumns(table, rowDataSuperClassType, fieldHierarchy, monitor);
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
      tableRowDataBuilder.addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_CONSTANT + 1, i, columnBeanName), constantFieldBuilder);

      // member
      IFieldSourceBuilder memberFieldBuilder = new FieldSourceBuilder("m_" + columnBeanName);
      memberFieldBuilder.setFlags(Flags.AccPrivate);
      memberFieldBuilder.setSignature(getColumnSignature(column, TypeUtility.getSupertypeHierarchy(column)));
      tableRowDataBuilder.addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_MEMBER + 1, i, columnBeanName), memberFieldBuilder);

      // getter
      IMethodSourceBuilder getterBuilder = MethodSourceBuilderFactory.createGetter(memberFieldBuilder);
      tableRowDataBuilder.addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 1, getterBuilder), getterBuilder);

      // setter
      IMethodSourceBuilder setterBuilder = MethodSourceBuilderFactory.createSetter(memberFieldBuilder);
      tableRowDataBuilder.addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 2, setterBuilder), setterBuilder);

      i++;
      if (monitor.isCanceled()) {
        return;
      }
    }
    addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableRowDataBuilder), tableRowDataBuilder);

    // row access methods
    final String tableRowSignature = Signature.createTypeSignature(tableRowDataBuilder.getElementName(), false);
    // getRows
    IMethodSourceBuilder getRowsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRows", new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        // choose the narrowed overload from the abstract super class instead of the method defined in the interface
        return candidate.getReturnType().contains(ABS_TABLE_ROW_DATA_SIMPLE_NAME);
      }
    });
    getRowsMethodBuilder.setReturnTypeSignature(Signature.createArraySignature(tableRowSignature, 1));
    getRowsMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(Signature.createArraySignature(tableRowSignature, 1), validator)).append(") super.getRows();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowsMethodBuilder), getRowsMethodBuilder);

    // setRows
    IMethodSourceBuilder setRowsMethodBuilder = new MethodSourceBuilder("setRows");
    setRowsMethodBuilder.setFlags(Flags.AccPublic);
    setRowsMethodBuilder.setReturnTypeSignature(Signature.SIG_VOID);
    setRowsMethodBuilder.addParameter(new MethodParameter("rows", Signature.createArraySignature(tableRowSignature, 1)));
    setRowsMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("super.setRows(rows);");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(setRowsMethodBuilder), setRowsMethodBuilder);

    // addRow
    final String addRowMethodName = "addRow";
    IMethodSourceBuilder addRowMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "addRow", new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        // choose the narrowed overload from the abstract super class instead of the method defined in the interface
        return candidate.getParameters().length == 0 && candidate.getReturnType().contains(ABS_TABLE_ROW_DATA_SIMPLE_NAME);
      }
    });
    addRowMethodBuilder.setReturnTypeSignature(tableRowSignature);
    addRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(") super.addRow();");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(addRowMethodBuilder), addRowMethodBuilder);
    if (monitor.isCanceled()) {
      return;
    }

    // addRow(int state)
    IMethodSourceBuilder addRowWithStateMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "addRow", new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) throws CoreException {
        if (addRowMethodName.equals(candidate.getElementName())) {
          return candidate.getParameters().length == 1;
        }
        return false;
      }
    });
    addRowWithStateMethodBuilder.setReturnTypeSignature(tableRowSignature);
    addRowWithStateMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(") super.addRow(");
        source.append(methodBuilder.getParameters().get(0).getName()).append(");");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(addRowWithStateMethodBuilder), addRowWithStateMethodBuilder);

    // rowAt
    IMethodSourceBuilder rowAtMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "rowAt");
    rowAtMethodBuilder.setReturnTypeSignature(tableRowSignature);
    rowAtMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return (").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(") super.rowAt(").append(methodBuilder.getParameters().get(0).getName()).append(");");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(rowAtMethodBuilder), rowAtMethodBuilder);

    // createRow
    IMethodSourceBuilder createRowMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "createRow");
    createRowMethodBuilder.setReturnTypeSignature(tableRowSignature);
    if (isAbstract) {
      createRowMethodBuilder.setFlags(createRowMethodBuilder.getFlags() | Flags.AccAbstract);
    }
    else {
      createRowMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("return new ").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append("();");
        }
      });
    }
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowMethodBuilder), createRowMethodBuilder);

    // getRowType
    IMethodSourceBuilder getRowTypeMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRowType");
    getRowTypeMethodBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(SignatureUtility.getTypeReference(tableRowSignature, validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeMethodBuilder), getRowTypeMethodBuilder);
  }

  protected String getTableRowDataSuperClassSignature(IType table) throws CoreException {
    if (!SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTablePageData).equals(getSuperTypeSignature())
        && !SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableFieldBeanData).equals(getSuperTypeSignature())) {
      // use the row data in the super page data.
      IType superType = TypeUtility.getTypeBySignature(getSuperTypeSignature());
      IType[] rowDatas = superType.getTypes();
      IType abstractTableRowData = TypeUtility.getType(IRuntimeClasses.AbstractTableRowData);

      // search the row data in the super type
      for (IType t : rowDatas) {
        if (TypeUtility.getSupertypeHierarchy(t).contains(abstractTableRowData)) {
          return SignatureCache.createTypeSignature(t.getFullyQualifiedName());
        }
      }
    }
    return SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData);
  }

  /**
   * Gets the row data type that is used within the given table data.
   *
   * @param tableData
   *          the table data that contains the row data. the type must exist.
   * @return the type that is referenced in the
   *         org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData#getRowType() method of
   *         the given table data.
   * @throws CoreException
   */
  protected IType getTableRowDataType(IType tableData) throws CoreException {
    IMethod getRowTypeMethod = ScoutTypeUtility.getMethod(tableData, "getRowType");
    return PropertyMethodSourceUtility.parseReturnParameterClass(PropertyMethodSourceUtility.getMethodReturnValue(getRowTypeMethod), getRowTypeMethod);
  }

  protected void addAbstractMethodImplementations() throws CoreException {
    // createRow
    IMethodSourceBuilder createRowSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "createRow");

    createRowSourceBuilder.setReturnTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData));
    createRowSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return new ").append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData), validator));
        source.append("(){").append(lineDelimiter).append("private static final long serialVersionUID = 1L;").append(lineDelimiter).append("};");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(createRowSourceBuilder), createRowSourceBuilder);

    IMethodSourceBuilder getRowTypeSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(this, "getRowType");
    getRowTypeSourceBuilder.setReturnTypeSignature(SignatureCache.createTypeSignature(Class.class.getName() + Signature.C_GENERIC_START + "? extends " + IRuntimeClasses.AbstractTableRowData + Signature.C_GENERIC_END));
    getRowTypeSourceBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return ").append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractTableRowData), validator)).append(".class;");
      }
    });
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getRowTypeSourceBuilder), getRowTypeSourceBuilder);
  }

}
