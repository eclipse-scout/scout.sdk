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
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataTypeSourceBuilder;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.table.TableFieldBeanFormDataSourceBuilder;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.TypeFilters;

/**
 * <h3>{@link CompositeFormDataTypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public class CompositeFormDataTypeSourceBuilder extends FormDataTypeSourceBuilder {

  public CompositeFormDataTypeSourceBuilder(IType modelType, FormDataAnnotation formDataAnnotation, String typeName, IJavaEnvironment env) {
    super(modelType, formDataAnnotation, typeName, env);
  }

  @Override
  protected void createContent() {
    super.createContent();
    createCompositeFieldFormData(getModelType());
  }

  private void createCompositeFieldFormData(IType compositeType) {
    List<IType> innerTypes = CoreUtils.getInnerTypes(compositeType, TypeFilters.subtypeOf(IRuntimeClasses.IFormField));
    for (IType formField : innerTypes) {
      boolean fieldExtendsTemplateField = false;

      if (Flags.isPublic(formField.getFlags())) {
        FormDataAnnotation fieldAnnotation = DtoUtils.findFormDataAnnotation(formField);

        if (FormDataAnnotation.isCreate(fieldAnnotation)) {
          IType formDataType = fieldAnnotation.getFormDataType();
          String formDataTypeName = null;
          if (formDataType == null) {
            formDataTypeName = DtoUtils.removeFieldSuffix(formField.getSimpleName());
          }
          else {
            formDataTypeName = formDataType.getSimpleName();
          }

          ITypeSourceBuilder fieldSourceBuilder = null;
          if (CoreUtils.isInstanceOf(fieldAnnotation.getSuperType(), IRuntimeClasses.AbstractTableFieldBeanData)) {
            // fill table bean
            fieldSourceBuilder = new TableFieldBeanFormDataSourceBuilder(formField, fieldAnnotation, formDataTypeName, getJavaEnvironment());
          }
          else if (CoreUtils.isInstanceOf(formField, IRuntimeClasses.ICompositeField) && !CoreUtils.isInstanceOf(formField, IRuntimeClasses.IValueField)) {
            // field extends a field template.
            fieldExtendsTemplateField = true;
            fieldSourceBuilder = new CompositeFormDataTypeSourceBuilder(formField, fieldAnnotation, formDataTypeName, getJavaEnvironment());
          }
          else {
            fieldSourceBuilder = new FormDataTypeSourceBuilder(formField, fieldAnnotation, formDataTypeName, getJavaEnvironment());

            // special case if a boolean (primitive!) property has the same name as a form field -> show warning
            for (IMethodSourceBuilder msb : getMethods()) {
              if (SIG_FOR_IS_METHOD_NAME.equals(msb.getReturnTypeSignature()) && ("is" + formDataTypeName).equals(msb.getElementName())) {
                fieldSourceBuilder.setComment(CommentSourceBuilderFactory.createCustomCommentBuilder("TODO [everyone] Duplicate names '" + formDataTypeName + "'. Rename property or form field."));
                break;
              }
            }
          }
          fieldSourceBuilder.setFlags(fieldSourceBuilder.getFlags() | Flags.AccStatic);
          addSortedType(SortedMemberKeyFactory.createTypeFormDataPropertyKey(fieldSourceBuilder), fieldSourceBuilder);

          // add interfaces specified on the formdata annotation
          DtoUtils.addFormDataAdditionalInterfaces(fieldAnnotation, fieldSourceBuilder, getJavaEnvironment());

          // getter for field
          String methodName = CoreUtils.ensureStartWithUpperCase(formDataTypeName); // Scout RT requires the first char to be upper-case for a getter. See org.eclipse.scout.commons.beans.FastBeanUtility.BEAN_METHOD_PAT.
          IMethodSourceBuilder getterBuilder = new MethodSourceBuilder("get" + methodName);
          getterBuilder.setFlags(Flags.AccPublic);
          getterBuilder.setReturnTypeSignature(Signature.createTypeSignature(formDataTypeName, false));
          getterBuilder.setBody(new RawSourceBuilder("return getFieldByClass(" + formDataTypeName + ".class);"));
          addSortedMethod(SortedMemberKeyFactory.createMethodPropertyKey(getterBuilder), getterBuilder);
        }
        else if (FormDataAnnotation.isIgnore(fieldAnnotation)) {
          continue;
        }

        if (CoreUtils.isInstanceOf(formField, IRuntimeClasses.ICompositeField) && !fieldExtendsTemplateField) {
          createCompositeFieldFormData(formField);
        }
      }
    }

    // step into extensions
    for (IType formFieldExtension : CoreUtils.getInnerTypes(compositeType, TypeFilters.subtypeOf(IRuntimeClasses.ICompositeFieldExtension))) {
      createCompositeFieldFormData(formFieldExtension);
    }
  }
}
