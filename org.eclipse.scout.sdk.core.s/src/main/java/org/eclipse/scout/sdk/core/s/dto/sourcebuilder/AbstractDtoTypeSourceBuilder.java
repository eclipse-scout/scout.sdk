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

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IAnnotation;
import org.eclipse.scout.sdk.core.model.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IPropertyBean;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodParameterDescription;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AbstractDtoTypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractDtoTypeSourceBuilder extends TypeSourceBuilder {

  private static final Pattern ENDING_SEMICOLON_PATTERN = Pattern.compile("\\;$");
  protected static final String SIG_FOR_IS_METHOD_NAME = Signature.SIG_BOOLEAN;

  private final IType m_modelType;
  private final ILookupEnvironment m_lookupEnv;

  public AbstractDtoTypeSourceBuilder(IType modelType, String typeName, ILookupEnvironment lookupEnv) {
    this(modelType, typeName, lookupEnv, true);
  }

  /**
   * @param elementName
   */
  public AbstractDtoTypeSourceBuilder(IType modelType, String typeName, ILookupEnvironment lookupEnv, boolean setup) {
    super(typeName);
    m_modelType = modelType;
    m_lookupEnv = lookupEnv;
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
    if (Flags.isAbstract(getModelType().getFlags())) {
      flags |= Flags.AccAbstract;
    }
    setFlags(flags);
    setSuperTypeSignature(computeSuperTypeSignature());
  }

  protected void createContent() {
    // constructor
    IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getElementName());
    addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);

    // serial version uid
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // copy annotations over to the DTO
    copyAnnotations(getModelType(), getModelType(), this, getLookupEnvironment());
  }

  /**
   * @return
   */
  protected abstract String computeSuperTypeSignature();

  protected static String getAnnotationValueSource(IAnnotationValue v, IImportValidator validator, int numValues) {
    StringBuilder sb = new StringBuilder();
    if (numValues > 1 || !"value".equals(v.getName())) {
      sb.append(v.getName()).append(" = ");
    }
    appendAnnotationSource(v, validator, sb);
    return sb.toString();
  }

  protected static void appendAnnotationSource(IAnnotationValue v, IImportValidator validator, StringBuilder sb) {
    switch (v.getValueType()) {
      case Char:
        sb.append('\'').append(((Character) v.getValue()).charValue()).append('\'');
        break;
      case Byte:
      case Int:
      case Short:
        sb.append(((Number) v.getValue()).intValue());
        break;
      case Bool:
        sb.append(((Boolean) v.getValue()).booleanValue());
        break;
      case Long:
        sb.append(((Number) v.getValue()).longValue()).append('L');
        break;
      case Double:
        sb.append(((Number) v.getValue()).doubleValue());
        break;
      case Float:
        sb.append(((Number) v.getValue()).floatValue()).append('f');
        break;
      case String:
        boolean isClassId = IRuntimeClasses.ClassId.equals(v.getOwnerAnnotation().getType().getName());
        String val = v.getValue().toString();
        if (isClassId) {
          val += "-formdata";
        }
        sb.append(CoreUtils.toStringLiteral(val));
        break;
      case Type:
        IType t = (IType) v.getValue();
        sb.append(validator.getTypeName(SignatureUtils.getResolvedSignature(t))).append(".class");
        break;
      case Array:
        IAnnotationValue[] arr = (IAnnotationValue[]) v.getValue();
        sb.append('{');
        if (arr.length > 0) {
          appendAnnotationSource(arr[0], validator, sb);
          for (int i = 1; i < arr.length; i++) {
            sb.append(", ");
            appendAnnotationSource(arr[i], validator, sb);
          }
        }
        sb.append('}');
        break;
    }
  }

  protected static void copyAnnotations(IAnnotatable annotationOwner, final IType declaringType, ITypeSourceBuilder sourceBuilder, final ILookupEnvironment lookupEnv) {
    Set<IAnnotation> annotations = annotationOwner.getAnnotations();
    for (IAnnotation a : annotations) {
      final IAnnotation annotation = a;
      final IType annotationDeclarationType = annotation.getType();
      final String elementName = annotationDeclarationType.getName();

      if (!IRuntimeClasses.FormData.equals(elementName)
          && !IRuntimeClasses.Order.equals(elementName)
          && !IRuntimeClasses.PageData.equals(elementName)
          && !IRuntimeClasses.Data.equals(elementName)) {

        if (isAnnotationDtoRelevant(annotationDeclarationType)) {
          if (CoreUtils.isOnClasspath(annotationDeclarationType, lookupEnv)) {
            AnnotationSourceBuilder asb = new AnnotationSourceBuilder(Signature.createTypeSignature(elementName)) {
              @Override
              public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
                // copy over all params
                Collection<IAnnotationValue> annotValues = annotation.getValues().values();
                for (IAnnotationValue v : annotValues) {
                  String param = getAnnotationValueSource(v, validator, annotValues.size());
                  if (StringUtils.isNotEmpty(param)) {
                    super.addParameter(param);
                  }
                }
                super.createSource(source, lineDelimiter, context, validator);
              }
            };
            sourceBuilder.addAnnotationSourceBuilder(asb);
          }
        }
      }
    }
  }

  protected static boolean isAnnotationDtoRelevant(IType annotation) {
    if (annotation == null) {
      return false;
    }
    IAnnotation dtoReleventAnnotation = CoreUtils.getAnnotation(annotation, IRuntimeClasses.DtoRelevant);
    return dtoReleventAnnotation != null;
  }

  public IType getModelType() {
    return m_modelType;
  }

  protected void collectProperties() {
    Set<? extends IPropertyBean> beanPropertyDescriptors = CoreUtils.getPropertyBeans(getModelType(), DTO_PROPERTY_FILTER, BEAN_NAME_COMPARATOR);
    for (IPropertyBean desc : beanPropertyDescriptors) {
      String beanName = CoreUtils.ensureValidParameterName(desc.getBeanName());
      String lowerCaseBeanName = CoreUtils.ensureStartWithLowerCase(beanName);
      final String upperCaseBeanName = CoreUtils.ensureStartWithUpperCase(beanName);

      String propName = upperCaseBeanName + "Property";
      String resolvedSignature = SignatureUtils.getResolvedSignature(desc.getBeanType());
      String unboxedSignature = SignatureUtils.unboxPrimitiveSignature(resolvedSignature);

      // property class
      TypeSourceBuilder propertyTypeBuilder = new TypeSourceBuilder(propName);
      propertyTypeBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
      String superTypeSig = Signature.createTypeSignature(IRuntimeClasses.AbstractPropertyData);
      superTypeSig = ENDING_SEMICOLON_PATTERN.matcher(superTypeSig).replaceAll(Signature.C_GENERIC_START + Matcher.quoteReplacement(unboxedSignature) + Signature.C_GENERIC_END + Signature.C_SEMICOLON);
      propertyTypeBuilder.setSuperTypeSignature(superTypeSig);
      IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
      propertyTypeBuilder.addSortedFieldSourceBuilder(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
      IMethodSourceBuilder constructorBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(propName);
      propertyTypeBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodConstructorKey(constructorBuilder), constructorBuilder);
      addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeFormDataPropertyKey(propertyTypeBuilder), propertyTypeBuilder);

      // copy annotations over to the DTO
      IMethod propertyMethod = desc.getReadMethod();
      if (propertyMethod == null) {
        propertyMethod = desc.getWriteMethod();
      }
      if (propertyMethod != null) {
        copyAnnotations(propertyMethod, propertyMethod.getDeclaringType(), propertyTypeBuilder, getLookupEnvironment());
      }

      // getter
      IMethodSourceBuilder propertyGetterBuilder = new MethodSourceBuilder("get" + propName);
      propertyGetterBuilder.setFlags(Flags.AccPublic);
      propertyGetterBuilder.setReturnTypeSignature(Signature.createTypeSignature(propName, false));
      propertyGetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return getPropertyByClass(" + propName + ".class);"));
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(propertyGetterBuilder), propertyGetterBuilder);

      // legacy getter
      IMethodSourceBuilder legacyPropertyGetterBuilder = new MethodSourceBuilder((SIG_FOR_IS_METHOD_NAME.equals(resolvedSignature) ? "is" : "get") + upperCaseBeanName);
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
      legacyPropertySetterBuilder.addParameter(new MethodParameterDescription(lowerCaseBeanName, resolvedSignature));
      legacyPropertySetterBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("get" + propName + "().setValue(" + lowerCaseBeanName + ");"));
      addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodPropertyKey(legacyPropertySetterBuilder), legacyPropertySetterBuilder);
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

  public ILookupEnvironment getLookupEnvironment() {
    return m_lookupEnv;
  }

  protected static final Predicate<IPropertyBean> DTO_PROPERTY_FILTER = new Predicate<IPropertyBean>() {
    @Override
    public boolean evaluate(IPropertyBean property) {
      // read and write method must exist
      boolean readAndWriteMethodsExist = property.getReadMethod() != null && property.getWriteMethod() != null;
      if (!readAndWriteMethodsExist) {
        return false;
      }

      // @FormData or @Data annotation must exist
      boolean isReadMethodDtoRelevant = CoreUtils.getAnnotation(property.getReadMethod(), IRuntimeClasses.FormData) != null || CoreUtils.getAnnotation(property.getReadMethod(), IRuntimeClasses.Data) != null;
      if (!isReadMethodDtoRelevant) {
        return false;
      }
      return CoreUtils.getAnnotation(property.getWriteMethod(), IRuntimeClasses.FormData) != null || CoreUtils.getAnnotation(property.getWriteMethod(), IRuntimeClasses.Data) != null;
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
      CompositeObject m1c = new CompositeObject(p1.getBeanName(), p1);
      CompositeObject m2c = new CompositeObject(p2.getBeanName(), p2);
      return m1c.compareTo(m2c);
    }
  };
}
