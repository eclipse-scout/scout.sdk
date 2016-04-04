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
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
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
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
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
 * @since 3.10.0 2013-08-27
 */
public abstract class AbstractDtoTypeSourceBuilder extends TypeSourceBuilder implements IDtoSourceBuilder {

  private static final Pattern ENDING_SEMICOLON_PATTERN = Pattern.compile("\\;$");

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
    // serial version uid
    IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
    addSortedField(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);

    // copy annotations over to the DTO
    copyAnnotations(getModelType(), this, getJavaEnvironment());

    // add replace annotation to DTO if replace annotation is present on the model
    if (getModelType().annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
      addAnnotation(ScoutAnnotationSourceBuilderFactory.createReplace());
    }
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

  protected static void copyAnnotations(IAnnotatable annotationOwner, ITypeSourceBuilder sourceBuilder, final IJavaEnvironment env) {
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
      String boxedSignature = SignatureUtils.boxPrimitiveSignature(resolvedSignature);

      // property class
      TypeSourceBuilder propertyTypeBuilder = new TypeSourceBuilder(propName);
      propertyTypeBuilder.setFlags(Flags.AccPublic | Flags.AccStatic);
      String superTypeSig = Signature.createTypeSignature(IScoutRuntimeTypes.AbstractPropertyData);
      superTypeSig = ENDING_SEMICOLON_PATTERN.matcher(superTypeSig).replaceAll(ISignatureConstants.C_GENERIC_START + Matcher.quoteReplacement(boxedSignature) + ISignatureConstants.C_GENERIC_END + ISignatureConstants.C_SEMICOLON);
      propertyTypeBuilder.setSuperTypeSignature(superTypeSig);
      IFieldSourceBuilder serialVersionUidBuilder = FieldSourceBuilderFactory.createSerialVersionUidBuilder();
      propertyTypeBuilder.addSortedField(SortedMemberKeyFactory.createFieldSerialVersionUidKey(serialVersionUidBuilder), serialVersionUidBuilder);
      addSortedType(SortedMemberKeyFactory.createTypeFormDataPropertyKey(propertyTypeBuilder), propertyTypeBuilder);

      // copy annotations over to the DTO
      IMethod propertyMethod = desc.readMethod();
      if (propertyMethod == null) {
        propertyMethod = desc.writeMethod();
      }
      if (propertyMethod != null) {
        copyAnnotations(propertyMethod, propertyTypeBuilder, getJavaEnvironment());
      }

      // getter
      IMethodSourceBuilder propertyGetterBuilder = new MethodSourceBuilder("get" + propName);
      propertyGetterBuilder.setFlags(Flags.AccPublic);
      propertyGetterBuilder.setReturnTypeSignature(Signature.createTypeSignature(propName, false));
      propertyGetterBuilder.setBody(new RawSourceBuilder(new StringBuilder("return getPropertyByClass(").append(propName).append(SuffixConstants.SUFFIX_class).append(");").toString()));
      addSortedMethod(SortedMemberKeyFactory.createMethodPropertyKey(propertyGetterBuilder), propertyGetterBuilder);

      // legacy getter
      IMethodSourceBuilder legacyPropertyGetterBuilder = new MethodSourceBuilder(CoreUtils.getGetterMethodPrefix(resolvedSignature) + upperCaseBeanName);
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
    String suffix = "().getValue()";
    StringBuilder source = new StringBuilder("return get");
    source.append(propertyName).append(suffix);
    if (Signature.getTypeSignatureKind(propertySignature) == ISignatureConstants.BASE_TYPE_SIGNATURE) {
      source.append(" == null ? ");
      source.append(CoreUtils.getDefaultValueOf(propertySignature));
      source.append(" : get").append(propertyName).append(suffix);
    }
    source.append(';');
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
