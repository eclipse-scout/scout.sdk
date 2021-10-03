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

import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.dto.form.FormDataGenerator;
import org.eclipse.scout.sdk.core.s.dto.table.TableFieldDataGenerator;
import org.eclipse.scout.sdk.core.s.dto.table.TableRowDataGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link CompositeFormDataGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public class CompositeFormDataGenerator<TYPE extends CompositeFormDataGenerator<TYPE>> extends FormDataGenerator<TYPE> {

  public CompositeFormDataGenerator(IType modelType, FormDataAnnotationDescriptor formDataAnnotation, IJavaEnvironment targetEnv) {
    super(modelType, formDataAnnotation, targetEnv);
  }

  @Override
  protected void setupBuilder() {
    processInnerTypes(modelType());
    super.setupBuilder();
  }

  protected void processInnerTypes(IType compositeType) {
    var scoutApi = scoutApi();
    // step into inner form field
    compositeType.innerTypes()
        .withFlags(Flags.AccPublic)
        .withInstanceOf(scoutApi.IFormField()).stream()
        .forEach(this::processFormField);

    // step into formFieldMenus (special menu that may contain more form fields)
    compositeType.innerTypes()
        .withFlags(Flags.AccPublic)
        .withInstanceOf(scoutApi.IFormFieldMenu()).stream()
        .forEach(this::processInnerTypes);

    // step into inner extensions
    compositeType.innerTypes()
        .withInstanceOf(scoutApi.ICompositeFieldExtension()).stream()
        .forEach(this::processInnerTypes);

    // step into inner table extensions of table-fields
    compositeType.innerTypes()
        .withRecursiveInnerTypes(true)
        .withInstanceOf(scoutApi.ITableExtension()).stream()
        .forEach(this::processTableExtension);
  }

  protected void processFormField(IType formField) {
    var scoutApi = scoutApi();
    var fieldAnnotation = FormDataAnnotationDescriptor.of(formField);
    if (FormDataAnnotationDescriptor.isIgnore(fieldAnnotation)) {
      return;
    }

    var fieldExtendsTemplateField = false;
    var isCompositeField = formField.isInstanceOf(scoutApi.ICompositeField());
    if (FormDataAnnotationDescriptor.isCreate(fieldAnnotation)) {
      var formDataType = fieldAnnotation.getFormDataType();
      String formDataTypeName;
      if (formDataType == null) {
        formDataTypeName = removeFieldSuffix(formField.elementName());
      }
      else {
        formDataTypeName = formDataType.elementName();
      }

      ITypeGenerator<?> dtoGenerator;
      if (fieldAnnotation.getSuperType().isInstanceOf(scoutApi.AbstractTableFieldBeanData())) {
        // fill table bean
        dtoGenerator = new TableFieldDataGenerator<>(formField, fieldAnnotation, targetEnvironment());
      }
      else if (isCompositeField && !formField.isInstanceOf(scoutApi.IValueField())) {
        // field extends a field template.
        fieldExtendsTemplateField = true;
        dtoGenerator = new CompositeFormDataGenerator<>(formField, fieldAnnotation, targetEnvironment());
      }
      else {

        dtoGenerator = new FormDataGenerator<>(formField, fieldAnnotation, targetEnvironment());

        // special case if a property has the same name as a form field -> show warning
        methods()
            .filter(msb -> hasSimilarNameAs(msb, formDataTypeName))
            .findAny()
            .ifPresent(msb -> dtoGenerator.withComment(b -> b.appendTodo("Duplicate names '" + formDataTypeName + "'. Rename property or form field.")));
      }

      // Scout RT requires the first char to be upper-case for a getter.
      // See org.eclipse.scout.rt.platform.reflect.FastBeanUtility.BEAN_METHOD_PAT.
      var methodName = Strings.capitalize(formDataTypeName).toString();
      this
          .withType(dtoGenerator.withElementName(formDataTypeName), DtoMemberSortObjectFactory.forTypeFormDataFormField(formDataTypeName))
          .withMethod(ScoutMethodGenerator.create()
              .asPublic()
              .withElementName(PropertyBean.GETTER_PREFIX + methodName)
              .withReturnType(formDataTypeName)
              .withBody(b -> b.returnClause().appendGetFieldByClass(formDataTypeName).semicolon()),
              DtoMemberSortObjectFactory.forMethodFormDataFormField(methodName));
    }

    if (isCompositeField && !fieldExtendsTemplateField) {
      processInnerTypes(formField);
    }
  }

  /**
   * @return {@code true} if the given {@link IMethodGenerator} has the same method name as a getter for the given
   *         formDataTypeName would have.
   */
  protected boolean hasSimilarNameAs(IMethodGenerator<?, ?> msb, String formDataTypeName) {
    var dataType = msb.returnType()
        .flatMap(af -> af.apply(this.targetEnvironment()))
        .orElseThrow();
    var name = PropertyBean.getterPrefixFor(dataType) + formDataTypeName;
    return name.equals(msb.elementName().orElseThrow());
  }

  protected void processTableExtension(IType tableExtension) {
    withType(new TableRowDataGenerator<>(tableExtension, tableExtension, targetEnvironment())
        .withElementName(getRowDataName(tableExtension.elementName()))
        .withExtendsAnnotationIfNecessary(tableExtension),
        DtoMemberSortObjectFactory.forTypeTableRowData(tableExtension.elementName()));
  }
}
