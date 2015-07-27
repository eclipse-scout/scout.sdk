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
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.FieldFilters;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.TypeFilters;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.SdkColumnCommand;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.model.ScoutTypeComparators;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.PropertyMap;

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
  private final ILookupEnvironment m_lookpEnvironment;

  public TableRowDataTypeSourceBuilder(String elementName, IType columnContainer, IType modelType, ILookupEnvironment lookupEnv) {
    super(elementName);
    m_columnContainer = columnContainer;
    m_modelType = modelType;
    m_lookpEnvironment = lookupEnv;
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    setup();
    super.createSource(source, lineDelimiter, context, validator);
  }

  private void setup() {
    // row data super type
    IType rowDataSuperClassType = null;
    String rowDataSuperClassSig = computeTableRowDataSuperClassSignature();
    if (rowDataSuperClassSig != null) {
      rowDataSuperClassType = m_lookpEnvironment.findType(SignatureUtils.toFullyQualifiedName(Signature.getTypeErasure(rowDataSuperClassSig)));
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
      addInterfaceSignature(Signature.createTypeSignature(Serializable.class.getName()));
    }

    // serialVersionUidBuilder
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // get all columns
    Set<IType> columns = getColumns(getColumnContainer(), rowDataSuperClassType);

    // visit columns
    int i = 0;
    for (IType column : columns) {
      String columnBeanName = getColumnBeanName(column);
      String constantColName = columnBeanName;
      if (CoreUtils.isReservedJavaKeyword(constantColName)) {
        constantColName += "_";
      }
      IFieldSourceBuilder constantFieldBuilder = new FieldSourceBuilder(constantColName);
      constantFieldBuilder.setFlags(ROW_DATA_FIELD_FLAGS);
      constantFieldBuilder.setSignature(Signature.createTypeSignature(String.class.getName()));
      constantFieldBuilder.setValue("\"" + columnBeanName + "\"");
      addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_CONSTANT + 1, i, columnBeanName), constantFieldBuilder);

      // member
      IFieldSourceBuilder memberFieldBuilder = new FieldSourceBuilder("m_" + columnBeanName);
      memberFieldBuilder.setFlags(Flags.AccPrivate);

      // try to find the column value type with the local hierarchy first.
      String columnValueTypeSignature = DtoUtils.getColumnValueTypeSignature(column);
      memberFieldBuilder.setSignature(columnValueTypeSignature);
      addSortedFieldSourceBuilder(new CompositeObject(SortedMemberKeyFactory.FIELD_MEMBER + 1, i, columnBeanName), memberFieldBuilder);

      // getter
      IMethodSourceBuilder getterBuilder = MethodSourceBuilderFactory.createGetter(memberFieldBuilder);
      addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 1, getterBuilder), getterBuilder);

      // setter
      IMethodSourceBuilder setterBuilder = MethodSourceBuilderFactory.createSetter(memberFieldBuilder);
      addSortedMethodSourceBuilder(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 2, setterBuilder), setterBuilder);

      i++;
    }
  }

  protected static String getColumnBeanName(IType column) {
    return CoreUtils.ensureStartWithLowerCase(DtoUtils.removeFieldSuffix(column.getSimpleName()));
  }

  protected static Set<IType> getColumns(IType declaringType, IType rowDataSuperType) {

    // the declaring type is a column itself
    if (CoreUtils.isInstanceOf(declaringType, IRuntimeClasses.IColumn)) {
      Set<IType> result = new HashSet<>(1);
      result.add(declaringType);
      return result;
    }

    // the declaring type is a IPageWithTableExtension -> search the inner table extension
    if (CoreUtils.isInstanceOf(declaringType, IRuntimeClasses.IPageWithTableExtension)) {
      IType tableExtension = CoreUtils.findInnerTypeInSuperHierarchy(declaringType, TypeFilters.getSubtypeFilter(IRuntimeClasses.ITableExtension));
      if (tableExtension != null) {
        declaringType = tableExtension; // switch to the table as column holder
      }
    }

    // the declaring type holds columns
    TreeSet<IType> allColumnsUpTheHierarchy = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator());
    // do not re-use the fieldHierarchy for the subtype filter!
    IFilter<IType> filter = TypeFilters.getMultiFilterAnd(TypeFilters.getSubtypeFilter(IRuntimeClasses.IColumn), new IFilter<IType>() {
      @Override
      public boolean evaluate(IType type) {
        SdkColumnCommand command = DtoUtils.findColumnDataSdkColumnCommand(type);
        return command == null || command == SdkColumnCommand.CREATE;
      }
    });

    // collect all columns that exist in the table and all of its super classes
    IType curTableType = declaringType;
    while (curTableType != null) {
      List<IType> columns = CoreUtils.getInnerTypes(curTableType, filter);
      allColumnsUpTheHierarchy.addAll(columns);
      curTableType = curTableType.getSuperClass();
    }

    if (rowDataSuperType == null) {
      // no need to filter the columns of the super classes
      return allColumnsUpTheHierarchy;
    }

    // collect all columns that exist in the row data and all of its super classes
    Set<String> usedColumnBeanNames = new HashSet<>();
    IType currentRowDataSuperType = rowDataSuperType;
    while (currentRowDataSuperType != null && !IRuntimeClasses.AbstractTableRowData.equals(currentRowDataSuperType.getName())) {
      List<IField> columnFields = CoreUtils.getFields(currentRowDataSuperType, FieldFilters.getFlagsFilter(ROW_DATA_FIELD_FLAGS));
      for (IField column : columnFields) {
        Object val = column.getValue();
        if (val instanceof String) {
          usedColumnBeanNames.add(val.toString());
        }
      }
      currentRowDataSuperType = currentRowDataSuperType.getSuperClass();
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
    }

    return allColumnsUpTheHierarchy;
  }

  protected String computeTableRowDataSuperClassSignature() {
    ITypeSourceBuilder surroundingTableBeanSourceBuilder = getParentTypeSourceBuilder();
    if (surroundingTableBeanSourceBuilder == null) {
      // row data extension. no super class
      return null;
    }

    String superTypeOfSurroundingTableBeanSourceBuilder = surroundingTableBeanSourceBuilder.getSuperTypeSignature();
    if (!Signature.createTypeSignature(IRuntimeClasses.AbstractTablePageData).equals(superTypeOfSurroundingTableBeanSourceBuilder) && !Signature.createTypeSignature(IRuntimeClasses.AbstractTableFieldBeanData).equals(superTypeOfSurroundingTableBeanSourceBuilder)) {
      // use the row data in the super page data.
      IType superType = m_lookpEnvironment.findType(SignatureUtils.toFullyQualifiedName(superTypeOfSurroundingTableBeanSourceBuilder));

      IType innerType = CoreUtils.getInnerType(superType, TypeFilters.getSubtypeFilter(IRuntimeClasses.AbstractTableRowData));
      if (innerType != null) {
        return SignatureUtils.getResolvedSignature(innerType);
      }
    }
    return Signature.createTypeSignature(IRuntimeClasses.AbstractTableRowData);
  }

  public IType getColumnContainer() {
    return m_columnContainer;
  }

  public IType getModelType() {
    return m_modelType;
  }
}
