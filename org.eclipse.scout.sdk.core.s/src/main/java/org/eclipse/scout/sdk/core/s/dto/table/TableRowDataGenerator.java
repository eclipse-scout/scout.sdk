/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto.table;

import static org.eclipse.scout.sdk.core.java.model.api.Flags.isAbstract;

import java.beans.Introspector;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.dto.AbstractDtoGenerator;
import org.eclipse.scout.sdk.core.s.java.annotation.ColumnDataAnnotation;
import org.eclipse.scout.sdk.core.s.java.annotation.ColumnDataAnnotation.SdkColumnCommand;
import org.eclipse.scout.sdk.core.s.util.ScoutTypeComparators;
import org.eclipse.scout.sdk.core.util.Ensure;

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

  protected Optional<String> getColumnValueType(IType columnContainer) {
    if (columnContainer == null) {
      return Optional.empty();
    }
    var iColumn = scoutApi().IColumn();
    return columnContainer.resolveTypeParamValue(iColumn.valueTypeParamIndex(), iColumn.fqn())
        .flatMap(Stream::findFirst) // only use first
        .map(IType::reference);
  }

  protected static String getColumnBeanName(IJavaElement column) {
    return Introspector.decapitalize(removeFieldSuffix(column.elementName()));
  }

  protected Stream<IType> findColumnsToAdd(IType columnContainer, IType rowDataSuperType) {
    var scoutApi = scoutApi();
    // the declaring type is a column itself
    if (columnContainer.isInstanceOf(scoutApi.IColumn())) {
      return Stream.of(columnContainer);
    }

    // the declaring type is a IPageWithTableExtension -> search the inner table extension
    if (columnContainer.isInstanceOf(scoutApi.IPageWithTableExtension())) {
      var tableExtension = columnContainer.innerTypes()
          .withSuperClasses(true)
          .withInstanceOf(scoutApi.ITableExtension())
          .first();
      columnContainer = tableExtension.orElse(columnContainer); // switch to the table as column holder
    }

    // the declaring type is now the IType holding the columns: collect all columns in the model
    var allModelColumns = modelColumnsIn(columnContainer);
    if (rowDataSuperType == null) {
      // no need to filter the columns of the super classes
      return allModelColumns;
    }

    // collect all columns that exist in the row data and all of its super classes
    var usedColumnBeanNames = columnBeanNamesInRowData(rowDataSuperType);
    return allModelColumns
        .filter(column -> !usedColumnBeanNames.contains(getColumnBeanName(column)));
  }

  /**
   * @return A {@link Set} with all column bean names of the given rowData (including its super classes).
   */
  protected Set<String> columnBeanNamesInRowData(IType rowDataSuperType) {
    Set<String> usedColumnBeanNames = new HashSet<>();
    var currentRowDataSuperType = Optional.of(rowDataSuperType);
    var abstractTableRowDataFqn = scoutApi().AbstractTableRowData().fqn();
    while (currentRowDataSuperType.isPresent() && !abstractTableRowDataFqn.equals(currentRowDataSuperType.orElseThrow().name())) {
      var curSuperType = currentRowDataSuperType.orElseThrow();
      curSuperType.fields().withFlags(ROW_DATA_FIELD_FLAGS).stream()
          .map(IField::constantValue)
          .flatMap(Optional::stream)
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
  protected Stream<IType> modelColumnsIn(IType modelType) {
    // collect all columns that exist in the table and all of its super classes
    return modelType.innerTypes()
        .withInstanceOf(scoutApi().IColumn())
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
    var superClass = superClassFunc().map(af -> af.apply(currentBuilder().context()));
    if (superClass.isEmpty()) {
      withInterface(Serializable.class.getName());
    }

    // find our super type
    var rowDataSuperClassType = superClass
        .map(JavaTypes::erasure)
        .flatMap(targetEnvironment()::findType);

    // add all columns that do not exist in the super row data
    findColumnsToAdd(columnContainer(), rowDataSuperClassType.orElse(null))
        .forEach(this::withDtoTypeForColumn);
  }

  protected TYPE withDtoTypeForColumn(IType column) {
    var columnBeanName = getColumnBeanName(column);
    var columnValueType = getColumnValueType(column).orElseGet(() -> {
      SdkLog.warning("Column '{}' has no value type.", column.name());
      return Object.class.getName();
    });

    var memberFieldGenerator = FieldGenerator.create()
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
            .withMethod(MethodGenerator.createSetter(memberFieldGenerator.elementName().orElseThrow(), columnValueType, Flags.AccPublic, "new"));
  }

  @Override
  protected String computeSuperType() {
    var scoutApi = scoutApi();
    if (m_columnContainer.isInstanceOf(scoutApi.IExtension()) || !(declaringGenerator().orElse(null) instanceof ITypeGenerator)) {
      // row data extension. no super class
      return null;
    }

    var superTypeOfSurroundingTableBeanGenerator = declaringGenerator()
        .map(jeg -> (ITypeGenerator<?>) jeg)
        .flatMap(ITypeGenerator::superClassFunc)
        .map(af -> af.apply(currentBuilder().context()))
        .orElseThrow();
    if (superTypeOfSurroundingTableBeanGenerator.equals(scoutApi.AbstractTablePageData().fqn())
        || superTypeOfSurroundingTableBeanGenerator.equals(scoutApi.AbstractTableFieldBeanData().fqn())) {
      return defaultSuperClass();
    }

    // use the row data in the super page data.
    return targetEnvironment().findType(superTypeOfSurroundingTableBeanGenerator)
        .flatMap(s -> s.innerTypes()
            .withInstanceOf(scoutApi.AbstractTableRowData())
            .first())
        .map(IType::reference)
        .orElseGet(this::defaultSuperClass);
  }

  protected String defaultSuperClass() {
    return scoutApi().AbstractTableRowData().fqn();
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
  @SuppressWarnings({"squid:S1185", "RedundantMethodOverride"}) // method is overridden for javadoc
  public IType modelType() {
    return super.modelType();
  }
}
