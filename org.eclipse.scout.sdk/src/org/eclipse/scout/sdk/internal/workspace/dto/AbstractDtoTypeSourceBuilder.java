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
package org.eclipse.scout.sdk.internal.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutPropertyBeanFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractDtoTypeSourceBuilder}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractDtoTypeSourceBuilder extends TypeSourceBuilder {

  private IType m_modelType;
  private ITypeHierarchy m_localTypeHierarchy;

  public AbstractDtoTypeSourceBuilder(IType modelType, String elementName) {
    this(modelType, elementName, true);
  }

  /**
   * @param elementName
   */
  public AbstractDtoTypeSourceBuilder(IType modelType, String elementName, boolean setup) {
    super(elementName);
    m_modelType = modelType;
    m_localTypeHierarchy = TypeUtility.getLocalTypeHierarchy(modelType);
    if (setup) {
      setup();
    }
  }

  protected void setup() {
    setupBuilder();
    createContent();
  }

  /**
   *
   */
  protected void setupBuilder() {
    // flags
    int flags = Flags.AccPublic;
    try {
      if (Flags.isAbstract(getModelType().getFlags())) {
        flags |= Flags.AccAbstract;
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not determ abstract flag of '" + getModelType().getFullyQualifiedName() + "'.", e);
    }
    setFlags(flags);
    try {
      setSuperTypeSignature(computeSuperTypeSignature());
    }
    catch (CoreException e) {
      ScoutSdk.logError("could not calculate super type for '" + getElementName() + "'.", e);
    }
  }

  /**
   *
   */
  protected void createContent() {

    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // serial version uid
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

  }

  /**
   * @return
   */
  protected abstract String computeSuperTypeSignature() throws CoreException;

  public IType getModelType() {
    return m_modelType;
  }

  public ITypeHierarchy getLocalTypeHierarchy() {
    return m_localTypeHierarchy;
  }

  protected void collectProperties() {
    IPropertyBean[] beanPropertyDescriptors = TypeUtility.getPropertyBeans(getModelType(), ScoutPropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    if (beanPropertyDescriptors != null) {
      for (IPropertyBean desc : beanPropertyDescriptors) {
        try {
          if (desc.getReadMethod() != null || desc.getWriteMethod() != null) {
            if (FormDataAnnotation.isCreate(ScoutTypeUtility.findFormDataAnnotation(desc.getReadMethod())) &&
                FormDataAnnotation.isCreate(ScoutTypeUtility.findFormDataAnnotation(desc.getWriteMethod()))) {
              String beanName = ScoutUtility.ensureValidParameterName(desc.getBeanName());
              String lowerCaseBeanName = ScoutUtility.ensureStartWithLowerCase(beanName);
              final String upperCaseBeanName = ScoutUtility.ensureStartWithUpperCase(beanName);

              String propName = upperCaseBeanName + "Property";
              String resolvedSignature = SignatureUtility.getResolvedSignature(desc.getBeanSignature(), desc.getDeclaringType());
              String unboxedSignature = ScoutUtility.unboxPrimitiveSignature(resolvedSignature);

              // property class
              TypeSourceBuilder propertyTypeBuilder = new TypeSourceBuilder(propName);
              propertyTypeBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
              String superTypeSig = SignatureCache.createTypeSignature(RuntimeClasses.AbstractPropertyData);
              superTypeSig = superTypeSig.replaceAll("\\;$", "<" + unboxedSignature + ">;");
              propertyTypeBuilder.setSuperTypeSignature(superTypeSig);
              IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
              propertyTypeBuilder.addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
              IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(propName);
              propertyTypeBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
              addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormDataPropertyKey(propertyTypeBuilder), propertyTypeBuilder);

              // getter
              IMethodSourceBuilder propertyGetterBuilder = new MethodSourceBuilder("get" + propName);
              propertyGetterBuilder.setFlags(Flags.AccPublic);
              propertyGetterBuilder.setReturnTypeSignature(Signature.createTypeSignature(propName, false));
              propertyGetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return getPropertyByClass(" + propName + ".class);"));
              addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(propertyGetterBuilder), propertyGetterBuilder);

              // legacy getter
              IMethodSourceBuilder legacyPropertyGetterBuilder = new MethodSourceBuilder((Signature.SIG_BOOLEAN.equals(resolvedSignature) ? "is" : "get") + upperCaseBeanName);
              legacyPropertyGetterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("access method for property " + upperCaseBeanName + "."));
              legacyPropertyGetterBuilder.setFlags(Flags.AccPublic);
              legacyPropertyGetterBuilder.setReturnTypeSignature(resolvedSignature);
              legacyPropertyGetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody(getLegacyGetterMethodBody(resolvedSignature, propName)));
              addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertyGetterBuilder), legacyPropertyGetterBuilder);

              // legacy setter
              IMethodSourceBuilder legacyPropertySetterBuilder = new MethodSourceBuilder("set" + upperCaseBeanName);
              legacyPropertySetterBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createCustomCommentBuilder("access method for property " + upperCaseBeanName + "."));
              legacyPropertySetterBuilder.setFlags(Flags.AccPublic);
              legacyPropertySetterBuilder.setReturnTypeSignature(Signature.SIG_VOID);
              legacyPropertySetterBuilder.addParameter(new MethodParameter(lowerCaseBeanName, resolvedSignature));
              legacyPropertySetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("get" + propName + "().setValue(" + lowerCaseBeanName + ");"));
              addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertySetterBuilder), legacyPropertySetterBuilder);

            }
          }
        }
        catch (CoreException e) {
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
