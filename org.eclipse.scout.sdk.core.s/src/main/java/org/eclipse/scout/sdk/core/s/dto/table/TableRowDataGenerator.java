/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.dto.table;

import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

import java.beans.Introspector;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.ColumnDataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.ColumnDataAnnotation.SdkColumnCommand;
import org.eclipse.scout.sdk.core.s.dto.AbstractDtoGenerator;
import org.eclipse.scout.sdk.core.s.util.ScoutTypeComparators;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link TableRowDataGenerator}</h3>
 *
 * @since 4.1.0 2014-11-19
 */
public class TableRowDataGenerator<TYPE extends TableRowDataGenerator<TYPE>> extends AbstractDtoGenerator<TYPE> {

  protected static final int ROW_DATA_FIELD_FLAGS = Flags.AccPublic | Flags.AccFinal | Flags.AccStatic;

  private final IType m_columnContainer;

  /**
   * @param columnContainer
   *          e.g. ITable or ITableExtension or IColumn
   * @param modelType
   *          e.g. IPageWithTable, ITableField, ITableExtension
   * @param targetEnv
   *          the {@link IJavaEnvironment} in which the row data is generated.
   */
  public TableRowDataGenerator(IType columnContainer, IType modelType, IJavaEnvironment targetEnv) {
    super(modelType, targetEnv);
    m_columnContainer = Ensure.notNull(columnContainer);
  }

  @Override
  protected void copyAnnotations() {
    // not necessary for row data
  }

  protected static Optional<String> getColumnValueType(IType columnContainer) {
    if (columnContainer == null) {
      return Optional.empty();
    }

    return columnContainer.resolveTypeParamValue(IScoutRuntimeTypes.TYPE_PARAM_COLUMN__VALUE_TYPE, IScoutRuntimeTypes.IColumn)
        .flatMap(Stream::findFirst) // only use first
        .map(IType::reference);
  }

  protected static String getColumnBeanName(IJavaElement column) {
    return Introspector.decapitalize(removeFieldSuffix(column.elementName()));
  }

  protected static Stream<IType> findColumnsToAdd(IType columnContainer, IType rowDataSuperType) {
    // the declaring type is a column itself
    if (columnContainer.isInstanceOf(IScoutRuntimeTypes.IColumn)) {
      return Stream.of(columnContainer);
    }

    // the declaring type is a IPageWithTableExtension -> search the inner table extension
    if (columnContainer.isInstanceOf(IScoutRuntimeTypes.IPageWithTableExtension)) {
      Optional<IType> tableExtension = columnContainer.innerTypes()
          .withSuperClasses(true)
          .withInstanceOf(IScoutRuntimeTypes.ITableExtension)
          .first();
      columnContainer = tableExtension.orElse(columnContainer); // switch to the table as column holder
    }

    // the declaring type is now the IType holding the columns: collect all columns in the model
    Stream<IType> allModelColumns = modelColumnsIn(columnContainer);
    if (rowDataSuperType == null) {
      // no need to filter the columns of the super classes
      return allModelColumns;
    }

    // collect all columns that exist in the row data and all of its super classes
    Set<String> usedColumnBeanNames = columnBeanNamesInRowData(rowDataSuperType);
    return allModelColumns
        .filter(column -> !usedColumnBeanNames.contains(getColumnBeanName(column)));
  }

  /**
   * @return A {@link Set} with all column bean names of the given rowData (including its super classes).
   */
  protected static Set<String> columnBeanNamesInRowData(IType rowDataSuperType) {
    Set<String> usedColumnBeanNames = new HashSet<>();
    Optional<IType> currentRowDataSuperType = Optional.of(rowDataSuperType);
    while (currentRowDataSuperType.isPresent() && !IScoutRuntimeTypes.AbstractTableRowData.equals(currentRowDataSuperType.get().name())) {
      IType curSuperType = currentRowDataSuperType.get();
      curSuperType.fields().withFlags(ROW_DATA_FIELD_FLAGS).stream()
          .map(IField::constantValue)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .filter(val -> val.type() == MetaValueType.String)
          .map(val -> val.as(String.class))
          .forEach(usedColumnBeanNames::add);
      currentRowDataSuperType = curSuperType.superClass();
    }
    return usedColumnBeanNames;
  }

  /**
   * @return A {@link Stream} with all columns of the given model type (including all columns in the super classes).
   */
  protected static Stream<IType> modelColumnsIn(IType modelType) {
    // collect all columns that exist in the table and all of its super classes
    return modelType.innerTypes()
        .withInstanceOf(IScoutRuntimeTypes.IColumn)
        .withSuperClasses(true).stream()
        .filter(type -> ColumnDataAnnotation.sdkColumnCommandOf(type).orElse(SdkColumnCommand.CREATE) == SdkColumnCommand.CREATE)
        .sorted(ScoutTypeComparators.orderAnnotationComparator(false));
  }

  @Override
  protected void setupBuilder() {
    super.setupBuilder();

    // row data class flags
    int flags;
    if (declaringGenerator().orElse(null) instanceof ITypeGenerator) {
      flags = Flags.AccPublic | Flags.AccStatic;
    }
    else {
      flags = Flags.AccPublic;
    }
    if (isAbstract(columnContainer().flags()) || isAbstract(modelType().flags())) {
      flags |= Flags.AccAbstract;
    }
    withFlags(flags);

    // super interface (for extensions)
    if (!superClass().isPresent()) {
      withInterface(Serializable.class.getName());
    }

    // find our super type
    Optional<IType> rowDataSuperClassType = superClass()
        .map(JavaTypes::erasure)
        .flatMap(targetEnvironment()::findType);

    // add all columns that do not exist in the super row data
    findColumnsToAdd(columnContainer(), rowDataSuperClassType.orElse(null))
        .forEach(this::withDtoTypeForColumn);
  }

  protected TYPE withDtoTypeForColumn(IType column) {
    String columnBeanName = getColumnBeanName(column);
    String columnValueType = getColumnValueType(column).orElseGet(() -> {
      SdkLog.warning("Column '{}' has no value type.", column.name());
      return Object.class.getName();
    });

    IFieldGenerator<?> memberFieldGenerator = FieldGenerator.create()
        .asPrivate()
        .withElementName("m_" + columnBeanName)
        .withDataType(columnValueType);

    return withField(FieldGenerator.create() // constant
        .withElementName(columnBeanName)
        .withFlags(ROW_DATA_FIELD_FLAGS)
        .withDataType(String.class.getName())
        .withValue(b -> b.stringLiteral(columnBeanName)))
            .withField(memberFieldGenerator)
            .withMethod(MethodGenerator.createGetter(memberFieldGenerator))
            .withMethod(MethodGenerator.createSetter(memberFieldGenerator.elementName().get(), columnValueType, Flags.AccPublic, "new"));
  }

  @Override
  protected String computeSuperType() {
    if (m_columnContainer.isInstanceOf(IScoutRuntimeTypes.IExtension) || !(declaringGenerator().orElse(null) instanceof ITypeGenerator)) {
      // row data extension. no super class
      return null;
    }

    Supplier<String> defaultSuperClass = () -> IScoutRuntimeTypes.AbstractTableRowData;
    ITypeGenerator<?> surroundingTableBeanSourceBuilder = (ITypeGenerator<?>) declaringGenerator().get();
    String superTypeOfSurroundingTableBeanGenerator = surroundingTableBeanSourceBuilder.superClass().get();
    if (IScoutRuntimeTypes.AbstractTablePageData.equals(superTypeOfSurroundingTableBeanGenerator)
        || IScoutRuntimeTypes.AbstractTableFieldBeanData.equals(superTypeOfSurroundingTableBeanGenerator)) {
      return defaultSuperClass.get();
    }

    // use the row data in the super page data.
    return targetEnvironment().findType(superTypeOfSurroundingTableBeanGenerator)
        .flatMap(s -> s.innerTypes()
            .withInstanceOf(IScoutRuntimeTypes.AbstractTableRowData)
            .first())
        .map(IType::reference)
        .orElseGet(defaultSuperClass);
  }

  /**
   * @return ITable or ITableExtension or IColumn
   */
  public IType columnContainer() {
    return m_columnContainer;
  }

  /**
   * @return IPageWithTable, ITableField, ITableExtension
   */
  @Override
  @SuppressWarnings("squid:S1185")
  public IType modelType() {
    return super.modelType();
  }
}
