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
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.ColumnDataAnnotation.SdkColumnCommand;
import org.eclipse.scout.sdk.core.s.model.ScoutTypeComparators;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
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
import org.eclipse.scout.sdk.core.util.FieldFilters;
import org.eclipse.scout.sdk.core.util.Filters;
import org.eclipse.scout.sdk.core.util.IFilter;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.TypeFilters;

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
  private final IJavaEnvironment m_lookpEnvironment;

  public TableRowDataTypeSourceBuilder(String elementName, IType columnContainer, IType modelType, IJavaEnvironment env) {
    super(elementName);
    m_columnContainer = columnContainer;
    m_modelType = modelType;
    m_lookpEnvironment = env;
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
    if (getDeclaringElement() instanceof ITypeSourceBuilder) {
      flags |= Flags.AccStatic;
    }
    if (Flags.isAbstract(getColumnContainer().flags()) || Flags.isAbstract(getModelType().flags())) {
      flags |= Flags.AccAbstract;
    }
    setFlags(flags);
    setSuperTypeSignature(rowDataSuperClassSig);
    if (rowDataSuperClassSig == null) {
      addInterfaceSignature(Signature.createTypeSignature(Serializable.class.getName()));
    }

    // serialVersionUidBuilder
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedField(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructor(getElementName());
    addSortedMethod(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

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
      constantFieldBuilder.setValue(new RawSourceBuilder(CoreUtils.toStringLiteral(columnBeanName)));
      addSortedField(new CompositeObject(SortedMemberKeyFactory.FIELD_CONSTANT + 1, i, columnBeanName), constantFieldBuilder);

      // member
      IFieldSourceBuilder memberFieldBuilder = new FieldSourceBuilder("m_" + columnBeanName);
      memberFieldBuilder.setFlags(Flags.AccPrivate);

      // try to find the column value type with the local hierarchy first.
      String columnValueTypeSignature = DtoUtils.getColumnValueTypeSignature(column);
      memberFieldBuilder.setSignature(columnValueTypeSignature);
      addSortedField(new CompositeObject(SortedMemberKeyFactory.FIELD_MEMBER + 1, i, columnBeanName), memberFieldBuilder);

      // getter
      IMethodSourceBuilder getterBuilder = MethodSourceBuilderFactory.createGetter(memberFieldBuilder);
      addSortedMethod(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 1, getterBuilder), getterBuilder);

      // setter
      IMethodSourceBuilder setterBuilder = MethodSourceBuilderFactory.createSetter(memberFieldBuilder);
      addSortedMethod(new CompositeObject(SortedMemberKeyFactory.METHOD_PROPERTY_ACCESS, i, 2, setterBuilder), setterBuilder);

      i++;
    }
  }

  protected static String getColumnBeanName(IType column) {
    return CoreUtils.ensureStartWithLowerCase(DtoUtils.removeFieldSuffix(column.elementName()));
  }

  protected static Set<IType> getColumns(IType declaringType, IType rowDataSuperType) {

    // the declaring type is a column itself
    if (declaringType.isInstanceOf(IScoutRuntimeTypes.IColumn)) {
      Set<IType> result = new HashSet<>(1);
      result.add(declaringType);
      return result;
    }

    // the declaring type is a IPageWithTableExtension -> search the inner table extension
    if (declaringType.isInstanceOf(IScoutRuntimeTypes.IPageWithTableExtension)) {
      IType tableExtension = CoreUtils.findInnerTypeInSuperHierarchy(declaringType, TypeFilters.instanceOf(IScoutRuntimeTypes.ITableExtension));
      if (tableExtension != null) {
        declaringType = tableExtension; // switch to the table as column holder
      }
    }

    // the declaring type holds columns
    TreeSet<IType> allColumnsUpTheHierarchy = new TreeSet<>(ScoutTypeComparators.getOrderAnnotationComparator());
    // do not re-use the fieldHierarchy for the subtype filter!
    IFilter<IType> filter = Filters.and(TypeFilters.instanceOf(IScoutRuntimeTypes.IColumn), new IFilter<IType>() {
      @Override
      public boolean evaluate(IType type) {
        SdkColumnCommand cmd = DtoUtils.getColumnDataSdkColumnCommand(type);
        return cmd == null || cmd.equals(SdkColumnCommand.CREATE);
      }
    });

    // collect all columns that exist in the table and all of its super classes
    IType curTableType = declaringType;
    while (curTableType != null) {
      List<IType> columns = curTableType.innerTypes().withFilter(filter).list();
      allColumnsUpTheHierarchy.addAll(columns);
      curTableType = curTableType.superClass();
    }

    if (rowDataSuperType == null) {
      // no need to filter the columns of the super classes
      return allColumnsUpTheHierarchy;
    }

    // collect all columns that exist in the row data and all of its super classes
    Set<String> usedColumnBeanNames = new HashSet<>();
    IType currentRowDataSuperType = rowDataSuperType;
    while (currentRowDataSuperType != null && !IScoutRuntimeTypes.AbstractTableRowData.equals(currentRowDataSuperType.name())) {
      List<IField> columnFields = currentRowDataSuperType.fields().withFilter(FieldFilters.flags(ROW_DATA_FIELD_FLAGS)).list();
      for (IField column : columnFields) {
        IMetaValue val = column.constantValue();
        if (val != null && val.type() == MetaValueType.String) {
          usedColumnBeanNames.add(val.get(String.class));
        }
      }
      currentRowDataSuperType = currentRowDataSuperType.superClass();
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
    if (!(getDeclaringElement() instanceof ITypeSourceBuilder)) {
      // row data extension. no super class
      return null;
    }

    ITypeSourceBuilder surroundingTableBeanSourceBuilder = (ITypeSourceBuilder) getDeclaringElement();
    String superTypeOfSurroundingTableBeanSourceBuilder = surroundingTableBeanSourceBuilder.getSuperTypeSignature();
    if (!Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTablePageData).equals(superTypeOfSurroundingTableBeanSourceBuilder)
        && !Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTableFieldBeanData).equals(superTypeOfSurroundingTableBeanSourceBuilder)) {
      // use the row data in the super page data.
      IType superType = m_lookpEnvironment.findType(SignatureUtils.toFullyQualifiedName(superTypeOfSurroundingTableBeanSourceBuilder));

      IType innerType = superType.innerTypes().withInstanceOf(IScoutRuntimeTypes.AbstractTableRowData).first();
      if (innerType != null) {
        return SignatureUtils.getTypeSignature(innerType);
      }
    }
    return Signature.createTypeSignature(IScoutRuntimeTypes.AbstractTableRowData);
  }

  public IType getColumnContainer() {
    return m_columnContainer;
  }

  public IType getModelType() {
    return m_modelType;
  }
}
