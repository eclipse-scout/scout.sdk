/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class CompositePrimaryTypeSourceBuilder extends SourceBuilderWithProperties {
  final IType iFormField = TypeUtility.getType(RuntimeClasses.IFormField);
  final IType iTableField = TypeUtility.getType(RuntimeClasses.ITableField);
  final IType iComposerField = TypeUtility.getType(RuntimeClasses.IComposerField);
  final IType iCompositeField = TypeUtility.getType(RuntimeClasses.ICompositeField);
  final IType iRadioButtonGroup = TypeUtility.getType(RuntimeClasses.IRadioButtonGroup);

  public CompositePrimaryTypeSourceBuilder(IType type) {
    this(type, TypeUtility.getLocalTypeHierarchy(type));
  }

  public CompositePrimaryTypeSourceBuilder(IType type, ITypeHierarchy formFieldHierarchy) {
    super(type);
    visitFormFields(type, formFieldHierarchy);
  }

  protected void visitFormFields(IType declaringType, ITypeHierarchy formFieldHierarchy) {
    try {
      if (declaringType.getTypes().length > 0) {
        if (formFieldHierarchy == null) {
          formFieldHierarchy = TypeUtility.getLocalTypeHierarchy(declaringType);
        }
        ITypeFilter formFieldFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getSubtypeFilter(iFormField, formFieldHierarchy));//, TypeFilters.getClassFilter());
        for (IType t : TypeUtility.getInnerTypes(declaringType, formFieldFilter, ScoutTypeComparators.getOrderAnnotationComparator())) {
          try {
            addFormField(t, formFieldHierarchy);
          }
          catch (JavaModelException e) {
            ScoutSdk.logError("could not add form field '" + declaringType.getElementName() + "' to form data.", e);
          }
        }
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("error during visiting type '" + declaringType.getElementName() + "'", e);
    }
  }

  protected void addFormField(IType formField, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    FormDataAnnotation formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(formField, formFieldHierarchy);
    if (formDataAnnotation != null) {
      if (FormDataAnnotation.isCreate(formDataAnnotation)) {
        String formDataElementName = FormDataUtility.getBeanName(FormDataUtility.getFieldNameWithoutSuffix(formField.getElementName()), true);
        String superTypeSignature = formDataAnnotation.getSuperTypeSignature();
        if (formDataAnnotation.getGenericOrdinal() >= 0) {
          IType superType = TypeUtility.getTypeBySignature(superTypeSignature);
          if (TypeUtility.isGenericType(superType)) {
            String genericTypeSig = org.eclipse.scout.sdk.operation.form.formdata.FormDataUtility.computeFormFieldGenericType(formField, formFieldHierarchy);
            if (genericTypeSig != null) {
              superTypeSignature = superTypeSignature.replaceAll("\\;$", "<" + genericTypeSig + ">;");
            }
          }
        }
        ITypeSourceBuilder builder = FormDataUtility.getInnerTypeFormDataSourceBuilder(superTypeSignature, formField, formFieldHierarchy);
        builder.setElementName(formDataElementName);
        builder.setSuperTypeSignature(superTypeSignature);
        builder.setFlags(Flags.AccPublic | Flags.AccStatic);
        addBuilder(builder, CATEGORY_TYPE_FIELD);
        MethodSourceBuilder getterBuilder = new MethodSourceBuilder();
        getterBuilder.setElementName("get" + formDataElementName);
        getterBuilder.setReturnSignature(Signature.createTypeSignature(formDataElementName, false));
        getterBuilder.setSimpleBody("return getFieldByClass(" + formDataElementName + ".class);");
        addBuilder(getterBuilder, CATEGORY_METHOD_FIELD_GETTER);
      }
      else if (FormDataAnnotation.isIgnore(formDataAnnotation)) {
        return;
      }
    }
    // visit children
    if (formFieldHierarchy.isSubtype(iCompositeField, formField)) {
      visitFormFields(formField, formFieldHierarchy);
    }
  }
}
