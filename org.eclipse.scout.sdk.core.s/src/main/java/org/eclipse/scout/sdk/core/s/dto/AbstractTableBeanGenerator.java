/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.SortedMemberEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.dto.table.TableRowDataGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;

import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

/**
 * <h3>{@link AbstractTableBeanGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public abstract class AbstractTableBeanGenerator<TYPE extends AbstractTableBeanGenerator<TYPE>> extends AbstractDtoGenerator<TYPE> {

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
    String rowStateParamName = "rowState";
    String rowIndexParamName = "index";

    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createRow = MethodGenerator.create()
        .asPublic()
        .withReturnType(rowDataName)
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementName("createRow")
        .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
            .superClause().dotSign().append("rowAt").parenthesisOpen().append(rowIndexParamName).parenthesisClose().semicolon());
    if (isAbstract(table.flags()) || isAbstract(modelType().flags())) {
      createRow.asAbstract();
    }
    else {
      createRow.withBody(b -> b.returnClause().appendNew().ref(rowDataName).parenthesisOpen().parenthesisClose().semicolon());
    }

    return withType(new TableRowDataGenerator<>(table, modelType(), targetEnvironment()) // inner row data class
        .withElementName(rowDataName))
            .withMethod(MethodGenerator.create() // addRow()
                .asPublic()
                .withReturnType(rowDataName)
                .withAnnotation(AnnotationGenerator.createOverride())
                .withElementName("addRow")
                .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                    .superClause().dotSign().append("addRow").parenthesisOpen().parenthesisClose().semicolon()),
                SortedMemberEntry.createDefaultMethodPos(1))
            .withMethod(MethodGenerator.create() // addRow(int state)
                .asPublic()
                .withReturnType(rowDataName)
                .withAnnotation(AnnotationGenerator.createOverride())
                .withElementName("addRow")
                .withParameter(MethodParameterGenerator.create()
                    .withElementName(rowStateParamName)
                    .withDataType(JavaTypes._int))
                .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                    .superClause().dotSign().append("addRow").parenthesisOpen().append(rowStateParamName).parenthesisClose().semicolon()),
                SortedMemberEntry.createDefaultMethodPos(2))
            .withMethod(createRow, SortedMemberEntry.createDefaultMethodPos(3)) // createRow
            .withMethod(MethodGenerator.create() // getRowType
                .asPublic()
                .withReturnType(Class.class.getName() + "<? extends " + IScoutRuntimeTypes.AbstractTableRowData + JavaTypes.C_GENERIC_END)
                .withAnnotation(AnnotationGenerator.createOverride())
                .withElementName("getRowType")
                .withBody(b -> b.returnClause().classLiteral(rowDataName).semicolon()), SortedMemberEntry.createDefaultMethodPos(4))
            .withMethod(MethodGenerator.create() // getRows
                .asPublic()
                .withReturnType(tableRowArray)
                .withElementName("getRows")
                .withAnnotation(AnnotationGenerator.createOverride())
                .withBody(b -> b.returnClause().parenthesisOpen().ref(tableRowArray).parenthesisClose().space()
                    .superClause().dotSign().append("getRows").parenthesisOpen().parenthesisClose().semicolon()),
                SortedMemberEntry.createDefaultMethodPos(5))
            .withMethod(MethodGenerator.create() // rowAt
                .asPublic()
                .withReturnType(rowDataName)
                .withAnnotation(AnnotationGenerator.createOverride())
                .withElementName("rowAt")
                .withParameter(MethodParameterGenerator.create()
                    .withElementName(rowIndexParamName)
                    .withDataType(JavaTypes._int))
                .withBody(b -> b.returnClause().parenthesisOpen().ref(rowDataName).parenthesisClose().space()
                    .superClause().dotSign().append("rowAt").parenthesisOpen().append(rowIndexParamName).parenthesisClose().semicolon()),
                SortedMemberEntry.createDefaultMethodPos(6))
            .withMethod(MethodGenerator.create() // setRows
                .asPublic()
                .withReturnType(JavaTypes._void)
                .withElementName("setRows")
                .withParameter(MethodParameterGenerator.create()
                    .withElementName("rows")
                    .withDataType(tableRowArray))
                .withBody(IMethodBodyBuilder::appendSuperCall), SortedMemberEntry.createDefaultMethodPos(7));
  }

  protected TYPE withAbstractMethodImplementations() {
    return withMethod(MethodGenerator.create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asPublic()
        .withReturnType(IScoutRuntimeTypes.AbstractTableRowData)
        .withElementName("createRow")
        .withBody(b -> b.returnClause().appendNew().ref(IScoutRuntimeTypes.AbstractTableRowData).parenthesisOpen().parenthesisClose().space().blockStart().nl()
            .append(FieldGenerator.createSerialVersionUid()).nl().blockEnd().semicolon()))
                .withMethod(MethodGenerator.create()
                    .withAnnotation(AnnotationGenerator.createOverride())
                    .asPublic()
                    .withReturnType(Class.class.getName() + "<? extends " + IScoutRuntimeTypes.AbstractTableRowData + JavaTypes.C_GENERIC_END)
                    .withElementName("getRowType")
                    .withBody(b -> b.returnClause().classLiteral(IScoutRuntimeTypes.AbstractTableRowData).semicolon()));
  }

}
