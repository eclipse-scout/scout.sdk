/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
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

  public static final String ADD_ROW_METHOD_NAME = "addRow";
  public static final String GET_ROW_TYPE_METHOD_NAME = "getRowType";
  public static final String GET_ROWS_METHOD_NAME = "getRows";
  public static final String ROW_AT_METHOD_NAME = "rowAt";
  public static final String SET_ROWS_METHOD_NAME = "setRows";
  public static final String CREATE_ROW_METHOD_NAME = "createRow";

  protected AbstractTableBeanGenerator(IType modelType, IJavaEnvironment targetEnv) {
    super(modelType, targetEnv);
  }

  @Override
  @SuppressWarnings("squid:S2201")
  protected void setupBuilder() {
    super.setupBuilder();

    modelType().innerTypes()
        .withSuperClasses(true)
        .withInstanceOf(IScoutRuntimeTypes.ITable)
        .first()
        .map(this::withTableBeanContent)
        .orElseGet(this::withAbstractMethodImplementations);

    withReplaceIfNecessary();
  }

  protected TYPE withTableBeanContent(IType table) {
    String rowDataName = getRowDataName(removeFieldSuffix(modelType().elementName()));
    String tableRowArray = rowDataName + "[]";

    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createRow = MethodGenerator.create()
        .asPublic()
        .withReturnType(rowDataName)
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementName(CREATE_ROW_METHOD_NAME)
        .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
            .superClause().dotSign().append(ROW_AT_METHOD_NAME).parenthesisOpen().append(ROW_INDEX_PARAM_NAME).parenthesisClose().semicolon());
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
            .withElementName(ADD_ROW_METHOD_NAME)
            .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                .superClause().dotSign().append(ADD_ROW_METHOD_NAME).parenthesisOpen().parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(ADD_ROW_METHOD_NAME))
        .withMethod(MethodGenerator.create() // addRow(int state)
            .asPublic()
            .withReturnType(rowDataName)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(ADD_ROW_METHOD_NAME)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(ROW_STATE_PARAM_NAME)
                .withDataType(JavaTypes._int))
            .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                .superClause().dotSign().append(ADD_ROW_METHOD_NAME).parenthesisOpen().append(ROW_STATE_PARAM_NAME).parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(ADD_ROW_METHOD_NAME))
        .withMethod(createRow, DtoMemberSortObjectFactory.forMethodTableData(createRow.elementName().get())) // createRow
        .withMethod(MethodGenerator.create() // getRowType
            .asPublic()
            .withReturnType(Class.class.getName() + JavaTypes.C_GENERIC_START + JavaTypes.C_QUESTION_MARK + ' ' + JavaTypes.EXTENDS + ' ' + IScoutRuntimeTypes.AbstractTableRowData + JavaTypes.C_GENERIC_END)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(GET_ROW_TYPE_METHOD_NAME)
            .withBody(b -> b.returnClause().classLiteral(rowDataName).semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(GET_ROW_TYPE_METHOD_NAME))
        .withMethod(MethodGenerator.create() // getRows
            .asPublic()
            .withReturnType(tableRowArray)
            .withElementName(GET_ROWS_METHOD_NAME)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withBody(b -> b.returnClause().parenthesisOpen().ref(tableRowArray).parenthesisClose().space()
                .superClause().dotSign().append(GET_ROWS_METHOD_NAME).parenthesisOpen().parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(GET_ROWS_METHOD_NAME))
        .withMethod(MethodGenerator.create() // rowAt
            .asPublic()
            .withReturnType(rowDataName)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withElementName(ROW_AT_METHOD_NAME)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(ROW_INDEX_PARAM_NAME)
                .withDataType(JavaTypes._int))
            .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                .superClause().dotSign().append(ROW_AT_METHOD_NAME).parenthesisOpen().append(ROW_INDEX_PARAM_NAME).parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(ROW_AT_METHOD_NAME))
        .withMethod(MethodGenerator.create() // setRows
            .asPublic()
            .withReturnType(JavaTypes._void)
            .withElementName(SET_ROWS_METHOD_NAME)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(ROWS_PARAM_NAME)
                .withDataType(tableRowArray))
            .withBody(IMethodBodyBuilder::appendSuperCall),
            DtoMemberSortObjectFactory.forMethodTableData(SET_ROWS_METHOD_NAME));
  }

  protected TYPE withAbstractMethodImplementations() {
    return this
        .withMethod(MethodGenerator.create()
            .withAnnotation(AnnotationGenerator.createOverride())
            .asPublic()
            .withReturnType(IScoutRuntimeTypes.AbstractTableRowData)
            .withElementName(CREATE_ROW_METHOD_NAME)
            .withBody(b -> b.returnClause().appendNew().ref(IScoutRuntimeTypes.AbstractTableRowData).parenthesisOpen().parenthesisClose().space().blockStart().nl()
                .append(FieldGenerator.createSerialVersionUid()).nl().blockEnd().semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(CREATE_ROW_METHOD_NAME))
        .withMethod(MethodGenerator.create()
            .withAnnotation(AnnotationGenerator.createOverride())
            .asPublic()
            .withReturnType(Class.class.getName() + JavaTypes.C_GENERIC_START + JavaTypes.C_QUESTION_MARK + ' ' + JavaTypes.EXTENDS + ' ' + IScoutRuntimeTypes.AbstractTableRowData + JavaTypes.C_GENERIC_END)
            .withElementName(GET_ROW_TYPE_METHOD_NAME)
            .withBody(b -> b.returnClause().classLiteral(IScoutRuntimeTypes.AbstractTableRowData).semicolon()),
            DtoMemberSortObjectFactory.forMethodTableData(GET_ROW_TYPE_METHOD_NAME));
  }
}
