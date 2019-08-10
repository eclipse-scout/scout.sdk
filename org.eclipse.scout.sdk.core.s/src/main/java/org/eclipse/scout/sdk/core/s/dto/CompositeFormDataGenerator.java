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

import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
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
    // step into inner form field
    compositeType.innerTypes()
        .withFlags(Flags.AccPublic)
        .withInstanceOf(IScoutRuntimeTypes.IFormField).stream()
        .forEach(this::processFormField);

    // step into formFieldMenus (special menu that may contain more form fields)
    compositeType.innerTypes()
        .withFlags(Flags.AccPublic)
        .withInstanceOf(IScoutRuntimeTypes.IFormFieldMenu).stream()
        .forEach(this::processInnerTypes);

    // step into inner extensions
    compositeType.innerTypes()
        .withInstanceOf(IScoutRuntimeTypes.ICompositeFieldExtension).stream()
        .forEach(this::processInnerTypes);

    // step into inner table extensions of table-fields
    compositeType.innerTypes()
        .withRecursiveInnerTypes(true)
        .withInstanceOf(IScoutRuntimeTypes.ITableExtension).stream()
        .forEach(this::processTableExtension);
  }

  protected void processFormField(IType formField) {
    FormDataAnnotationDescriptor fieldAnnotation = FormDataAnnotationDescriptor.of(formField);
    if (FormDataAnnotationDescriptor.isIgnore(fieldAnnotation)) {
      return;
    }

    boolean fieldExtendsTemplateField = false;
    boolean isCompositeField = formField.isInstanceOf(IScoutRuntimeTypes.ICompositeField);
    if (FormDataAnnotationDescriptor.isCreate(fieldAnnotation)) {
      IType formDataType = fieldAnnotation.getFormDataType();
      String formDataTypeName;
      if (formDataType == null) {
        formDataTypeName = removeFieldSuffix(formField.elementName());
      }
      else {
        formDataTypeName = formDataType.elementName();
      }

      ITypeGenerator<?> dtoGenerator;
      if (fieldAnnotation.getSuperType().isInstanceOf(IScoutRuntimeTypes.AbstractTableFieldBeanData)) {
        // fill table bean
        dtoGenerator = new TableFieldDataGenerator<>(formField, fieldAnnotation, targetEnvironment());
      }
      else if (isCompositeField && !formField.isInstanceOf(IScoutRuntimeTypes.IValueField)) {
        // field extends a field template.
        fieldExtendsTemplateField = true;
        dtoGenerator = new CompositeFormDataGenerator<>(formField, fieldAnnotation, targetEnvironment());
      }
      else {

        dtoGenerator = new FormDataGenerator<>(formField, fieldAnnotation, targetEnvironment());

        // special case if a property has the same name as a form field -> show warning
        methods()
            .filter(msb -> (PropertyBean.getterPrefixFor(msb.returnType().get()) + formDataTypeName).equals(msb.elementName().get()))
            .findAny()
            .ifPresent(msb -> dtoGenerator.withComment(b -> b.appendTodo("Duplicate names '" + formDataTypeName + "'. Rename property or form field.")));
      }

      // Scout RT requires the first char to be upper-case for a getter.
      // See org.eclipse.scout.rt.platform.reflect.FastBeanUtility.BEAN_METHOD_PAT.
      String methodName = Strings.ensureStartWithUpperCase(formDataTypeName);
      withType(dtoGenerator
          .withElementName(formDataTypeName))
              .withMethod(ScoutMethodGenerator.create()
                  .asPublic()
                  .withElementName(PropertyBean.GETTER_PREFIX + methodName)
                  .withReturnType(formDataTypeName)
                  .withBody(b -> b.returnClause().appendGetFieldByClass(formDataTypeName).semicolon()));
    }

    if (isCompositeField && !fieldExtendsTemplateField) {
      processInnerTypes(formField);
    }
  }

  protected void processTableExtension(IType tableExtension) {
    withType(new TableRowDataGenerator<>(tableExtension, tableExtension, targetEnvironment())
        .withElementName(getRowDataName(tableExtension.elementName()))
        .withExtendsAnnotationIfNecessary(tableExtension));
  }
}
