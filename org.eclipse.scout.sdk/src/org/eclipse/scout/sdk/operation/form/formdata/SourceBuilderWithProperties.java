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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.util.ScoutSignature;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;
import org.eclipse.scout.sdk.workspace.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.workspace.type.PropertyBeanFilters;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>{@link SourceBuilderWithProperties}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.02.2011
 */
public class SourceBuilderWithProperties extends TypeSourceBuilder {

  public SourceBuilderWithProperties(IType type) {
    visitProperties(type);
  }

  protected void visitProperties(IType type) {
    IPropertyBean[] beanPropertyDescriptors = TypeUtility.getPropertyBeans(type, PropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    if (beanPropertyDescriptors != null) {
      for (IPropertyBean desc : beanPropertyDescriptors) {
        try {
          if (desc.getReadMethod() != null || desc.getWriteMethod() != null) {
            if (FormDataAnnotation.isCreate(SdkTypeUtility.findFormDataAnnotation(desc.getReadMethod())) &&
                FormDataAnnotation.isCreate(SdkTypeUtility.findFormDataAnnotation(desc.getWriteMethod()))) {
              String beanName = FormDataUtility.getValidMethodParameterName(desc.getBeanName());
              String lowerCaseBeanName = FormDataUtility.getBeanName(beanName, false);
              String upperCaseBeanName = FormDataUtility.getBeanName(beanName, true);

              String propName = upperCaseBeanName + "Property";
              String resolvedSignature = ScoutSignature.getResolvedSignature(desc.getBeanSignature(), desc.getDeclaringType());
              String unboxedSignature = FormDataUtility.unboxPrimitiveSignature(resolvedSignature);
              // property class
              TypeSourceBuilder propertyBuilder = new TypeSourceBuilder();
              propertyBuilder.setElementName(propName);

              String superTypeSig = Signature.createTypeSignature(RuntimeClasses.AbstractPropertyData, true);
              superTypeSig = superTypeSig.replaceAll("\\;$", "<" + unboxedSignature + ">;");
              propertyBuilder.setSuperTypeSignature(superTypeSig);
              addBuilder(propertyBuilder, CATEGORY_TYPE_PROPERTY);
              // getter
              MethodSourceBuilder getterBuilder = new MethodSourceBuilder();
              getterBuilder.setElementName("get" + propName);
              getterBuilder.setReturnSignature(Signature.createTypeSignature(propName, false));
              getterBuilder.setSimpleBody("return getPropertyByClass(" + propName + ".class);");
              addBuilder(getterBuilder, new CompositeObject(CATEGORY_METHOD_PROPERTY, lowerCaseBeanName, 1, getterBuilder));

              // legacy getter
              MethodSourceBuilder legacyGetter = new MethodSourceBuilder();
              legacyGetter.setJavaDoc(" /** " + ScoutUtility.NL + "   * access method for property " + upperCaseBeanName + "." + ScoutUtility.NL + "*/");
              legacyGetter.setElementName((Signature.SIG_BOOLEAN.equals(resolvedSignature) ? "is" : "get") + upperCaseBeanName);
              legacyGetter.setReturnSignature(resolvedSignature);
              legacyGetter.setSimpleBody(getLegacyGetterMethodBody(resolvedSignature, propName));
              addBuilder(legacyGetter, new CompositeObject(CATEGORY_METHOD_PROPERTY, lowerCaseBeanName, 2, legacyGetter));

              // legacy setter
              MethodSourceBuilder legacySetter = new MethodSourceBuilder();
              legacySetter.setJavaDoc(" /** " + ScoutUtility.NL + "   * access method for property " + upperCaseBeanName + "." + ScoutUtility.NL + "*/");
              legacySetter.setElementName("set" + upperCaseBeanName);
              legacySetter.addParameter(new MethodParameter(resolvedSignature, lowerCaseBeanName));
              legacySetter.setSimpleBody("get" + propName + "().setValue(" + lowerCaseBeanName + ");");
              addBuilder(legacySetter, new CompositeObject(CATEGORY_METHOD_PROPERTY, lowerCaseBeanName, 3, legacySetter));
            }
          }
        }
        catch (JavaModelException e) {
          ScoutSdk.logError("could append property to form data '" + getElementName() + "'.", e);
        }
      }
    }
  }

  private String getLegacyGetterMethodBody(String propertySignature, String propertyName) {
    String nonArraySig = propertySignature;

    StringBuilder source = new StringBuilder();
    source.append("return ");
    if (Signature.SIG_BOOLEAN.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (false) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_BYTE.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_CHAR.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? ('\u0000') : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_DOUBLE.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0.0d) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_FLOAT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0.0f) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_INT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_LONG.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0L) : (get" + propertyName + "().getValue());");
    }
    else if (Signature.SIG_SHORT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else {
      source.append("get" + propertyName + "().getValue();");
    }
    return source.toString();
  }
}
