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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.operation.form.util.FormDataUtility;
import org.eclipse.scout.sdk.operation.form.util.FormDataUtility.FormDataAnnotationDesc;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 *
 */
public class FormDataSourceBuilder extends TypeSourceBuilder {
  final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);
  final IType iTableField = ScoutSdk.getType(RuntimeClasses.ITableField);
  final IType iComposerField = ScoutSdk.getType(RuntimeClasses.IComposerField);
  final IType iCompositeField = ScoutSdk.getType(RuntimeClasses.ICompositeField);

  public FormDataSourceBuilder(IType type) {

  }

  public FormDataSourceBuilder(IType type, ITypeHierarchy formFieldHierarchy) {

  }

  protected void visitFormFields(IType declaringType, ITypeHierarchy formFieldHierarchy) {
    ITypeFilter formFieldFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getSubtypeFilter(iFormField, formFieldHierarchy), TypeFilters.getClassFilter());
    for (IType t : TypeUtility.getInnerTypes(declaringType, formFieldFilter, TypeComparators.getOrderAnnotationComparator())) {
      try {
        addFormField(t, formFieldHierarchy);
      }
      catch (JavaModelException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  protected void addFormField(IType formField, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    FormDataAnnotationDesc desc = FormDataUtility.parseFormDataAnnotation(formField, formFieldHierarchy);
    if (desc != null) {
      String formDataElementName = FormDataUtility.getBeanName(FormDataUtility.getFieldIdWithoutSuffix(formField.getElementName()), true);
      if (desc.usingFlag && !StringUtility.isNullOrEmpty(desc.fullyQualifiedName)) {
        TypeSourceBuilder builder = new TypeSourceBuilder();
        builder.setElementName(formDataElementName);
        builder.setSuperTypeSignature(Signature.createTypeSignature(desc.fullyQualifiedName, true));
        addBuilder(builder, CATEGORY_TYPE_FIELD);
        MethodSourceBuilder getterBuilder = new MethodSourceBuilder();
        getterBuilder.setElementName("get" + formDataElementName);
        getterBuilder.setReturnSignature(Signature.createTypeSignature(formDataElementName, false));
        getterBuilder.setSimpleBody("return getFieldByClass(" + formDataElementName + ".class)");
        addBuilder(getterBuilder, CATEGORY_METHOD_FIELD_GETTER);
        return;
      }
      else {
        TypeSourceBuilder builder = null;
        if (formFieldHierarchy.isSubtype(iTableField, formField)) {

        }
        else if (formFieldHierarchy.isSubtype(iComposerField, formField)) {

        }
        else {
          builder = new TypeSourceBuilder();
          builder.setElementName(formDataElementName);
          String superTypeSignature = Signature.createTypeSignature(RuntimeClasses.AbstractFormFieldData, true);
          String genericTypeSig = createFormDataSignatureFor(formField, formFieldHierarchy);
          if (genericTypeSig != null) {
            String superSig = Signature.createTypeSignature(RuntimeClasses.AbstractValueFieldData, true);
            superTypeSignature = superSig.replaceAll("\\;$", "<" + genericTypeSig + ">;");
          }
          builder.setSuperTypeSignature(superTypeSignature);
          addBuilder(builder, CATEGORY_TYPE_FIELD);
        }
        if (builder != null) {
          if (desc.externalFlag) {
            addExternalBuilder(builder);
          }
          MethodSourceBuilder getterBuilder = new MethodSourceBuilder();
          getterBuilder.setElementName("get" + formDataElementName);
          getterBuilder.setReturnSignature(Signature.createTypeSignature(formDataElementName, false));
          getterBuilder.setSimpleBody("return getFieldByClass(" + formDataElementName + ".class)");
          addBuilder(getterBuilder, CATEGORY_METHOD_FIELD_GETTER);
          if (formFieldHierarchy.isSubtype(iCompositeField, formField)) {
            visitFormFields(formField, formFieldHierarchy);
          }
        }
      }
    }
  }

  private String createFormDataSignatureFor(IType type, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    if (type == null || type.getFullyQualifiedName().equals(Object.class.getName())) {
      return null;
    }
    IType superType = formFieldHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        // compute generic parameter type by merging all super type generic parameter declarations
        List<GenericSignatureMapping> signatureMapping = new ArrayList<GenericSignatureMapping>();
        IType currentType = type;
        IType currentSuperType = superType;
        while (currentSuperType != null) {
          if (TypeUtility.isGenericType(currentSuperType)) {
            String superTypeGenericParameterName = currentSuperType.getTypeParameters()[0].getElementName();
            String currentSuperTypeSig = currentType.getSuperclassTypeSignature();
            String superTypeGenericParameterSignature = ScoutSdkUtility.getResolvedSignature(Signature.getTypeArguments(currentSuperTypeSig)[0], currentType);
            signatureMapping.add(0, new GenericSignatureMapping(superTypeGenericParameterName, superTypeGenericParameterSignature));
            currentType = currentSuperType;

            currentSuperType = formFieldHierarchy.getSuperclass(currentSuperType);
          }
          else {
            break;
          }
        }

        String signature = signatureMapping.get(0).getSuperTypeGenericParameterSignature();
        for (int i = 1; i < signatureMapping.size(); i++) {
          String replacement = signatureMapping.get(i).getSuperTypeGenericParameterSignature();
          replacement = replacement.substring(0, replacement.length() - 1);
          signature = signature.replaceAll("[T,L,Q]" + signatureMapping.get(i).getSuperTypeGenericParameterName(), replacement);
        }
        return ScoutSdkUtility.getResolvedSignature(signature, type);
      }
      else {
        return createFormDataSignatureFor(superType, formFieldHierarchy);
      }
    }
    else {
      return null;
    }
  }

  private static class GenericSignatureMapping {
    private final String m_superTypeGenericParameterName;
    private final String m_superTypeGenericParameterSignature;

    public GenericSignatureMapping(String superTypeGenericParameterName, String superTypeGenericParameterSignature) {
      m_superTypeGenericParameterName = superTypeGenericParameterName;
      m_superTypeGenericParameterSignature = superTypeGenericParameterSignature;
    }

    public String getSuperTypeGenericParameterName() {
      return m_superTypeGenericParameterName;
    }

    public String getSuperTypeGenericParameterSignature() {
      return m_superTypeGenericParameterSignature;
    }
  }
}
