/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.dto.sourcebuilder;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IPropertyBean;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.IFilter;

/**
 * <h3>{@link AbstractDtoTypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractDtoTypeSourceBuilder extends TypeSourceBuilder implements IDtoSourceBuilder {

  private static final Pattern ENDING_SEMICOLON_PATTERN = Pattern.compile("\\;$");
  protected static final String SIG_FOR_IS_METHOD_NAME = ISignatureConstants.SIG_BOOLEAN;

  private final IType m_modelType;
  private final IJavaEnvironment m_env;

  public AbstractDtoTypeSourceBuilder(IType modelType, String typeName, IJavaEnvironment env) {
    this(modelType, typeName, env, true);
  }

  /**
   * @param elementName
   */
  public AbstractDtoTypeSourceBuilder(IType modelType, String typeName, IJavaEnvironment env, boolean setup) {
    super(typeName);
    m_modelType = modelType;
    m_env = env;
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
    if (Flags.isAbstract(getModelType().flags())) {
      flags |= Flags.AccAbstract;
    }
    setFlags(flags);
    setSuperTypeSignature(computeSuperTypeSignature());
  }

  protected void createContent() {
    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructor(getElementName());
    addSortedMethod(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // serial version uid
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedField(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // copy annotations over to the DTO
    copyAnnotations(getModelType(), getModelType(), this, getJavaEnvironment());
  }

  /**
   * @return
   */
  protected abstract String computeSuperTypeSignature();

  /**
   * Override default {@link IMetaValue#createAptSource(IImportCollector)} for strings of ClassId annotations
   */
  protected static void filterAnnotationValues(IAnnotationSourceBuilder builder, IAnnotation a) {
    if (IScoutRuntimeTypes.ClassId.equals(a.type().name())) {
      String id = a.element("value").value().get(String.class);
      id += "-formdata";
      builder.putElement("value", CoreUtils.toStringLiteral(id));
    }
  }

  protected static void copyAnnotations(IAnnotatable annotationOwner, final IType declaringType, ITypeSourceBuilder sourceBuilder, final IJavaEnvironment env) {
    List<IAnnotation> annotations = annotationOwner.annotations().list();
    for (IAnnotation a : annotations) {
      final IAnnotation annotation = a;
      final IType annotationDeclarationType = annotation.type();
      final String elementName = annotationDeclarationType.name();

      boolean mustCopyAnnotation = !IScoutRuntimeTypes.FormData.equals(elementName)
          && !IScoutRuntimeTypes.Order.equals(elementName)
          && !IScoutRuntimeTypes.PageData.equals(elementName)
          && !IScoutRuntimeTypes.Data.equals(elementName)
          && isAnnotationDtoRelevant(annotationDeclarationType)
          && CoreUtils.isOnClasspath(env, annotationDeclarationType);
      if (mustCopyAnnotation) {
        AnnotationSourceBuilder asb = new AnnotationSourceBuilder(a);
        filterAnnotationValues(asb, a);
        sourceBuilder.addAnnotation(asb);
      }
    }
  }

  protected static boolean isAnnotationDtoRelevant(IType annotationType) {
    if (annotationType == null) {
      return false;
    }
    return annotationType.annotations().withName(IScoutRuntimeTypes.DtoRelevant).existsAny();
  }

  public IType getModelType() {
    return m_modelType;
  }

  protected void collectProperties() {
    List<IPropertyBean> beanPropertyDescriptors = CoreUtils.getPropertyBeans(getModelType(), DTO_PROPERTY_FILTER, BEAN_NAME_COMPARATOR);
    for (IPropertyBean desc : beanPropertyDescriptors) {
      String beanName = CoreUtils.ensureValidParameterName(desc.name());
      String lowerCaseBeanName = CoreUtils.ensureStartWithLowerCase(beanName);
      final String upperCaseBeanName = CoreUtils.ensureStartWithUpperCase(beanName);

      String propName = upperCaseBeanName + "Property";
      String resolvedSignature = SignatureUtils.getTypeSignature(desc.type());
      String unboxedSignature = SignatureUtils.boxPrimitiveSignature(resolvedSignature);

      // property class
      TypeSourceBuilder propertyTypeBuilder = new TypeSourceBuilder(propName);
      propertyTypeBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
      String superTypeSig = Signature.createTypeSignature(IScoutRuntimeTypes.AbstractPropertyData);
      superTypeSig = ENDING_SEMICOLON_PATTERN.matcher(superTypeSig).replaceAll(ISignatureConstants.C_GENERIC_START + Matcher.quoteReplacement(unboxedSignature) + ISignatureConstants.C_GENERIC_END + ISignatureConstants.C_SEMICOLON);
      propertyTypeBuilder.setSuperTypeSignature(superTypeSig);
      IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
      propertyTypeBuilder.addSortedField(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
      IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructor(propName);
      propertyTypeBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
      addSortedType(SortedMemberKeyFactory.createTypeFormDataPropertyKey(propertyTypeBuilder), propertyTypeBuilder);

      // copy annotations over to the DTO
      IMethod propertyMethod = desc.readMethod();
      if (propertyMethod == null) {
        propertyMethod = desc.writeMethod();
      }
      if (propertyMethod != null) {
        copyAnnotations(propertyMethod, propertyMethod.declaringType(), propertyTypeBuilder, getJavaEnvironment());
      }

      // getter
      IMethodSourceBuilder propertyGetterBuilder = new MethodSourceBuilder("get" + propName);
      propertyGetterBuilder.setFlags(Flags.AccPublic);
      propertyGetterBuilder.setReturnTypeSignature(Signature.createTypeSignature(propName, false));
      propertyGetterBuilder.setBody(new RawSourceBuilder("return getPropertyByClass(" + propName + ".class);"));
      addSortedMethod(SortedMemberKeyFactory.createMethodPropertyKey(propertyGetterBuilder), propertyGetterBuilder);

      // legacy getter
      IMethodSourceBuilder legacyPropertyGetterBuilder = new MethodSourceBuilder((SIG_FOR_IS_METHOD_NAME.equals(resolvedSignature) ? "is" : "get") + upperCaseBeanName);
      legacyPropertyGetterBuilder.setComment(CommentSourceBuilderFactory.createCustomCommentBuilder("access method for property " + upperCaseBeanName + "."));
      legacyPropertyGetterBuilder.setFlags(Flags.AccPublic);
      legacyPropertyGetterBuilder.setReturnTypeSignature(resolvedSignature);
      legacyPropertyGetterBuilder.setBody(new RawSourceBuilder(getLegacyGetterMethodBody(resolvedSignature, propName)));
      addSortedMethod(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertyGetterBuilder), legacyPropertyGetterBuilder);

      // legacy setter
      IMethodSourceBuilder legacyPropertySetterBuilder = new MethodSourceBuilder("set" + upperCaseBeanName);
      legacyPropertySetterBuilder.setComment(CommentSourceBuilderFactory.createCustomCommentBuilder("access method for property " + upperCaseBeanName + "."));
      legacyPropertySetterBuilder.setFlags(Flags.AccPublic);
      legacyPropertySetterBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
      legacyPropertySetterBuilder.addParameter(new MethodParameterSourceBuilder(lowerCaseBeanName, resolvedSignature));
      legacyPropertySetterBuilder.setBody(new RawSourceBuilder("get" + propName + "().setValue(" + lowerCaseBeanName + ");"));
      addSortedMethod(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertySetterBuilder), legacyPropertySetterBuilder);
    }
  }

  private static String getLegacyGetterMethodBody(String propertySignature, String propertyName) {
    String nonArraySig = propertySignature;

    StringBuilder source = new StringBuilder();
    source.append("return ");
    if (ISignatureConstants.SIG_BOOLEAN.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (false) : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_BYTE.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_CHAR.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? ('\u0000') : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_DOUBLE.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0.0d) : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_FLOAT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0.0f) : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_INT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_LONG.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0L) : (get" + propertyName + "().getValue());");
    }
    else if (ISignatureConstants.SIG_SHORT.equals(nonArraySig)) {
      source.append("(get" + propertyName + "().getValue() == null) ? (0) : (get" + propertyName + "().getValue());");
    }
    else {
      source.append("get" + propertyName + "().getValue();");
    }
    return source.toString();
  }

  @Override
  public IJavaEnvironment getJavaEnvironment() {
    return m_env;
  }

  protected static final IFilter<IPropertyBean> DTO_PROPERTY_FILTER = new IFilter<IPropertyBean>() {
    @Override
    public boolean evaluate(IPropertyBean property) {
      // read and write method must exist
      boolean readAndWriteMethodsExist = property.readMethod() != null && property.writeMethod() != null;
      if (!readAndWriteMethodsExist) {
        return false;
      }

      // @FormData or @Data annotation must exist
      boolean isReadMethodDtoRelevant = property.readMethod().annotations().withName(IScoutRuntimeTypes.FormData).existsAny()
          || property.readMethod().annotations().withName(IScoutRuntimeTypes.Data).existsAny();
      if (!isReadMethodDtoRelevant) {
        return false;
      }

      return property.writeMethod().annotations().withName(IScoutRuntimeTypes.FormData).existsAny()
          || property.writeMethod().annotations().withName(IScoutRuntimeTypes.Data).existsAny();
    }
  };

  protected static final Comparator<IPropertyBean> BEAN_NAME_COMPARATOR = new Comparator<IPropertyBean>() {
    @Override
    public int compare(IPropertyBean p1, IPropertyBean p2) {
      if (p1 == null && p2 == null) {
        return 0;
      }
      else if (p1 == null) {
        return 1;
      }
      else if (p2 == null) {
        return -1;
      }
      CompositeObject m1c = new CompositeObject(p1.name(), p1);
      CompositeObject m2c = new CompositeObject(p2.name(), p2);
      return m1c.compareTo(m2c);
    }
  };
}
