/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.util.JavaTypes.arrayMarker;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.dto.table.TableRowDataGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link AbstractTableBeanGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public abstract class AbstractTableBeanGenerator<TYPE extends AbstractTableBeanGenerator<TYPE>> extends AbstractDtoGenerator<TYPE> {

  public static final String ROW_STATE_PARAM_NAME = "rowState";
  public static final String ROW_INDEX_PARAM_NAME = "index";
  public static final String ROWS_PARAM_NAME = "rows";

  protected AbstractTableBeanGenerator(IType modelType, IJavaEnvironment targetEnv) {
    super(modelType, targetEnv);
  }

  @Override
  @SuppressWarnings("squid:S2201")
  protected void setupBuilder() {
    super.setupBuilder();

    modelType().innerTypes()
        .withSuperClasses(true)
        .withInstanceOf(scoutApi().ITable())
        .first()
        .map(this::withTableBeanContent)
        .orElseGet(this::withAbstractMethodImplementations);

    withReplaceIfNecessary();
  }

  protected TYPE withTableBeanContent(IType table) {
    var rowDataName = getRowDataName(removeFieldSuffix(modelType().elementName()));
    var tableRowArray = rowDataName + arrayMarker();
    var abstractTableFieldBeanDataApi = scoutApi().AbstractTableFieldBeanData();
    var rowAtMethodName = abstractTableFieldBeanDataApi.rowAtMethodName();
    var setRowsMethodName = abstractTableFieldBeanDataApi.setRowsMethodName();
    var createRowMethodName = abstractTableFieldBeanDataApi.createRowMethodName();
    var iTableBeanHolderApi = scoutApi().ITableBeanHolder();
    var addRowMethodName = iTableBeanHolderApi.addRowMethodName();
    var getRowTypeMethodName = iTableBeanHolderApi.getRowTypeMethodName();
    var getRowsMethodName = iTableBeanHolderApi.getRowsMethodName();

    var createRow = MethodGenerator.create()
        .asPublic()
        .withReturnType(rowDataName)
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementName(createRowMethodName)
        .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
            .superClause().dot().append(rowAtMethodName).parenthesisOpen().append(ROW_INDEX_PARAM_NAME).parenthesisClose().semicolon());
    if (isAbstract(table.flags()) || isAbstract(modelType().flags())) {
      createRow.asAbstract();
    }
    else {
      createRow.withBody(b -> b.returnClause().appendNew().ref(rowDataName).parenthesisOpen().parenthesisClose().semicolon());
    }

    return this
        .withType(new TableRowDataGenerator<>(table, modelType(), targetEnvironment()) // inner row data class
            .withElementName(rowDataName),
            DtoMemberSortObjectFactory.forTypeTableRowData(rowDataName))
        .withMethod(MethodGenerator.create() // addRow()
            .asPublic()
            .withReturnType(rowDataName)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(addRowMethodName)
            .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                .superClause().dot().append(addRowMethodName).parenthesisOpen().parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(addRowMethodName))
        .withMethod(MethodGenerator.create() // addRow(int state)
            .asPublic()
            .withReturnType(rowDataName)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(addRowMethodName)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(ROW_STATE_PARAM_NAME)
                .withDataType(JavaTypes._int))
            .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                .superClause().dot().append(addRowMethodName).parenthesisOpen().append(ROW_STATE_PARAM_NAME).parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(addRowMethodName))
        .withMethod(createRow, DtoMemberSortObjectFactory.forMethodTableData(createRow.elementName().orElseThrow())) // createRow
        .withMethod(MethodGenerator.create() // getRowType
            .asPublic()
            .withReturnType(Class.class.getName() + JavaTypes.C_GENERIC_START + JavaTypes.C_QUESTION_MARK + ' ' + JavaTypes.EXTENDS + ' ' + scoutApi().AbstractTableRowData().fqn() + JavaTypes.C_GENERIC_END)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(getRowTypeMethodName)
            .withBody(b -> b.returnClause().classLiteral(rowDataName).semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(getRowTypeMethodName))
        .withMethod(MethodGenerator.create() // getRows
            .asPublic()
            .withReturnType(tableRowArray)
            .withElementName(getRowsMethodName)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withBody(b -> b.returnClause().parenthesisOpen().ref(tableRowArray).parenthesisClose().space()
                .superClause().dot().append(getRowsMethodName).parenthesisOpen().parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(getRowsMethodName))
        .withMethod(MethodGenerator.create() // rowAt
            .asPublic()
            .withReturnType(rowDataName)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(rowAtMethodName)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(ROW_INDEX_PARAM_NAME)
                .withDataType(JavaTypes._int))
            .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                .superClause().dot().append(rowAtMethodName).parenthesisOpen().append(ROW_INDEX_PARAM_NAME).parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(rowAtMethodName))
        .withMethod(MethodGenerator.create() // setRows
            .asPublic()
            .withReturnType(JavaTypes._void)
            .withElementName(setRowsMethodName)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(ROWS_PARAM_NAME)
                .withDataType(tableRowArray))
            .withBody(IMethodBodyBuilder::appendSuperCall),
            DtoMemberSortObjectFactory.forMethodTableData(setRowsMethodName));
  }

  protected TYPE withAbstractMethodImplementations() {
    var scoutApi = scoutApi();
    var getRowTypeMethodName = scoutApi.ITableBeanHolder().getRowTypeMethodName();
    var createRowMethodName = scoutApi.AbstractTableFieldBeanData().createRowMethodName();
    var abstractTableRowDataFqn = scoutApi.AbstractTableRowData().fqn();
    return this
        .withMethod(MethodGenerator.create()
            .withAnnotation(AnnotationGenerator.createOverride())
            .asPublic()
            .withReturnType(abstractTableRowDataFqn)
            .withElementName(createRowMethodName)
            .withBody(b -> b.returnClause().appendNew().ref(abstractTableRowDataFqn).parenthesisOpen().parenthesisClose().space().blockStart().nl()
                .append(FieldGenerator.createSerialVersionUid()).nl().blockEnd().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(createRowMethodName))
        .withMethod(MethodGenerator.create()
            .withAnnotation(AnnotationGenerator.createOverride())
            .asPublic()
            .withReturnType(Class.class.getName() + JavaTypes.C_GENERIC_START + JavaTypes.C_QUESTION_MARK + ' ' + JavaTypes.EXTENDS + ' ' + abstractTableRowDataFqn + JavaTypes.C_GENERIC_END)
            .withElementName(getRowTypeMethodName)
            .withBody(b -> b.returnClause().classLiteral(abstractTableRowDataFqn).semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(getRowTypeMethodName));
  }
}
