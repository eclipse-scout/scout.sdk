/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.ExtendsAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.ReplaceAnnotation;
import org.eclipse.scout.sdk.core.s.apidef.IScoutAnnotationApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AbstractDtoGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public abstract class AbstractDtoGenerator<TYPE extends AbstractDtoGenerator<TYPE>> extends TypeGenerator<TYPE> {

  public static final String FORMDATA_CLASSID_SUFFIX = "-formdata";
  private final IType m_modelType;
  private final IJavaEnvironment m_targetEnvironment;
  private final IScoutApi m_scoutApi;
  private IJavaSourceBuilder<?> m_currentBuilder; // only set during build

  protected AbstractDtoGenerator(IType modelType, IJavaEnvironment targetEnvironment) {
    m_modelType = Ensure.notNull(modelType);
    m_targetEnvironment = Ensure.notNull(targetEnvironment);
    m_scoutApi = modelType.javaEnvironment().requireApi(IScoutApi.class);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    m_currentBuilder = builder;
    try {
      setupBuilder();
      super.build(builder);
    }
    finally {
      m_currentBuilder = null;
    }
  }

  public IScoutApi scoutApi() {
    return m_scoutApi;
  }

  protected IJavaSourceBuilder<?> currentBuilder() {
    return m_currentBuilder;
  }

  protected void copyAnnotations() {
    copyAnnotations(modelType(), this, targetEnvironment());
  }

  private static void copyAnnotations(IAnnotatable annotationOwner, ITypeGenerator<?> target, IJavaEnvironment targetEnv) {
    var scoutApi = annotationOwner.javaEnvironment().requireApi(IScoutApi.class);
    annotationOwner.annotations().stream()
        .filter(a -> isAnnotationDtoRelevant(a.type(), scoutApi))
        .filter(a -> targetEnv.exists(a.type()))
        .map(AbstractDtoGenerator::toDtoAnnotationGenerator)
        .forEach(target::withAnnotation);
  }

  /**
   * Override original ClassId value
   */
  private static IAnnotationGenerator<?> toDtoAnnotationGenerator(IAnnotation a) {
    var result = a.toWorkingCopy();
    var classIdApi = a.javaEnvironment().requireApi(IScoutApi.class).ClassId();
    if (classIdApi.fqn().equals(a.type().name())) {
      var valueElementName = classIdApi.valueElementName();
      var id = a.element(valueElementName).orElseThrow().value().as(String.class);
      result.withElement(valueElementName, b -> b.stringLiteral(id + FORMDATA_CLASSID_SUFFIX));
    }
    return result;
  }

  private static boolean isAnnotationDtoRelevant(IType annotationType, IScoutAnnotationApi scoutApi) {
    if (annotationType == null) {
      return false;
    }
    var elementName = annotationType.name();
    var isDtoAnnotation = elementName.equals(scoutApi.FormData().fqn())
        || elementName.equals(scoutApi.PageData().fqn())
        || elementName.equals(scoutApi.Data().fqn());
    return !isDtoAnnotation
        && !elementName.equals(scoutApi.Order().fqn())
        && annotationType.annotations().withName(scoutApi.DtoRelevant().fqn()).existsAny();
  }

  /**
   * Adds all interfaces as specified in the given {@link FormDataAnnotationDescriptor}.<br>
   * For all methods that also exist in the interfaces added and {@link Override} annotation is added as well.
   *
   * @param formDataAnnotation
   *          The {@link FormDataAnnotationDescriptor} holding the interfaces.
   */
  protected TYPE withAdditionalInterfaces(FormDataAnnotationDescriptor formDataAnnotation) {
    var interfaces = formDataAnnotation.getInterfaces();
    if (interfaces.isEmpty()) {
      return thisInstance();
    }

    var javaEnvironment = targetEnvironment();
    var allSuperInterfaceMethods = interfaces.stream()
        .filter(javaEnvironment::exists)
        .peek(ifcType -> withInterface(ifcType.reference()))
        .flatMap(ifcType -> ifcType.methods().withSuperTypes(true).stream())
        .map(IMethod::identifier)
        .collect(toSet());

    methods()
        .filter(msb -> allSuperInterfaceMethods.contains(msb.identifier(currentBuilder().context())))
        .forEach(msb -> msb.withAnnotation(AnnotationGenerator.createOverride()));
    return thisInstance();
  }

  protected TYPE withExtendsAnnotationIfNecessary(IType element) {
    var extendedTypeOpt = getExtendedType(element);
    if (extendedTypeOpt.isEmpty()) {
      return thisInstance();
    }
    var extendedType = extendedTypeOpt.orElseThrow();
    var primaryType = extendedType.primary();

    Optional<IType> extendedDto = Optional.empty();
    var api = scoutApi();
    if (primaryType.isInstanceOf(api.IForm()) || primaryType.isInstanceOf(api.IFormField())) {
      var declaring = extendedType.declaringType();
      if (extendedType.isInstanceOf(api.ITable()) && declaring.isPresent()) {
        var tableFieldDto = getFormDataType(declaring.orElseThrow());
        extendedDto = tableFieldDto.flatMap(dto -> dto.innerTypes().withInstanceOf(api.AbstractTableRowData()).first());
      }
      else {
        extendedDto = findDtoForForm(primaryType);
      }
    }
    else if (primaryType.isInstanceOf(api.IExtension())) {
      extendedDto = findDtoForPage(primaryType);
    }
    else if (primaryType.isInstanceOf(api.IPageWithTable())) {
      var pageDto = findDtoForPage(primaryType);
      extendedDto = pageDto.flatMap(dto -> dto.innerTypes().withInstanceOf(api.AbstractTableRowData()).first());
    }

    return extendedDto
        .map(t -> withAnnotation(ScoutAnnotationGenerator.createExtends(t.reference())))
        .orElseGet(this::thisInstance);
  }

  /**
   * Gets the form data type that is referenced in the form data annotation of the given form.<br>
   * If the annotation does not exist or points to an inexistent form data type, null is returned.
   *
   * @param form
   *          the form for which the form data should be returned.
   * @return the form data type or null if it could not be found.
   */
  private static Optional<IType> findDtoForForm(IType form) {
    if (form == null) {
      return Optional.empty();
    }
    var a = FormDataAnnotationDescriptor.of(form);
    return Optional.ofNullable(a.getFormDataType());
  }

  /**
   * Gets the page data type that is referenced in the page data annotation of the given page type.<br>
   * If the annotation does not exist or points to an inexistent page data type, null is returned.
   *
   * @param page
   *          the page for which the page data should be returned.
   * @return the page data class or null.
   */
  private static Optional<IType> findDtoForPage(IType page) {
    if (page == null) {
      return Optional.empty();
    }
    return DataAnnotationDescriptor.of(page)
        .map(DataAnnotationDescriptor::getDataType);
  }

  protected static String getRowDataName(String base) {
    if (Strings.isBlank(base)) {
      return ISdkConstants.SUFFIX_ROW_DATA;
    }

    var result = new StringBuilder(base.length() + ISdkConstants.SUFFIX_ROW_DATA.length());
    var suffixes = new String[]{"PageData", "FieldData", ISdkConstants.SUFFIX_DTO};
    Arrays.stream(suffixes)
        .filter(base::endsWith)
        .findFirst()
        .ifPresent(suffix -> result.append(base, 0, base.length() - suffix.length()));
    if (result.length() < 1) {
      // has none of the suffixes
      result.append(base);
    }
    return result.append(ISdkConstants.SUFFIX_ROW_DATA).toString();
  }

  protected static String removeFieldSuffix(String fieldName) {
    if (fieldName.endsWith(ISdkConstants.SUFFIX_FORM_FIELD)) {
      return fieldName.substring(0, fieldName.length() - ISdkConstants.SUFFIX_FORM_FIELD.length());
    }
    if (fieldName.endsWith(ISdkConstants.SUFFIX_BUTTON)) {
      return fieldName.substring(0, fieldName.length() - ISdkConstants.SUFFIX_BUTTON.length());
    }
    if (fieldName.endsWith(ISdkConstants.SUFFIX_COLUMN)) {
      return fieldName.substring(0, fieldName.length() - ISdkConstants.SUFFIX_COLUMN.length());
    }
    if (fieldName.endsWith(ISdkConstants.SUFFIX_OUTLINE_PAGE)) {
      return fieldName.substring(0, fieldName.length() - ISdkConstants.SUFFIX_OUTLINE_PAGE.length());
    }
    return fieldName;
  }

  /**
   * @return Returns an {@link Optional} with the form field data/form data for the given form field/form.
   * @since 3.8.2
   */
  private static Optional<IType> getFormDataType(IType modelType) {
    var primaryType = getFormFieldDataPrimaryTypeRec(modelType);
    if (primaryType == null) {
      return Optional.empty();
    }

    var isPrimaryType = modelType.declaringType().isEmpty();
    if (isPrimaryType) {
      // model type is a primary type (form, template) and we have a corresponding DTO type.
      return Optional.of(primaryType);
    }

    // check if the primary type itself is the correct type
    var formDataName = removeFieldSuffix(modelType.elementName());
    if (primaryType.elementName().equals(formDataName)) {
      return Optional.of(primaryType);
    }

    // search field data within form data
    return primaryType.innerTypes().withRecursiveInnerTypes(true).withSimpleName(formDataName).first();
  }

  private static Optional<IType> computeDtoGenericType(IType contextType, IType annotationOwnerType, int genericOrdinal) {
    if (contextType == null || Object.class.getName().equals(contextType.name()) || annotationOwnerType == null) {
      return Optional.empty();
    }

    if (annotationOwnerType.typeArguments().count() <= genericOrdinal) {
      // cannot be found in arguments. check parameters
      var param = annotationOwnerType.typeParameters()
          .skip(genericOrdinal)
          .findAny()
          .orElseThrow(() -> new SdkException("Invalid genericOrdinal value on class '{}': {}.", annotationOwnerType.name(), genericOrdinal));
      return param.bounds().findAny();
    }

    return annotationOwnerType.resolveTypeParamValue(genericOrdinal)
        .flatMap(Stream::findFirst);
  }

  /**
   * @return Returns the form field data/form data for the given form field/form or {@code null} if it does not have
   *         one. The method walks recursively through the list of declaring classes until it has reached a primary
   *         type.
   * @since 3.8.2
   */
  private static IType getFormFieldDataPrimaryTypeRec(IType recursiveDeclaringType) {
    while (true) {
      if (recursiveDeclaringType == null) {
        return null;
      }

      var formDataAnnotation = FormDataAnnotationDescriptor.of(recursiveDeclaringType);
      if (FormDataAnnotationDescriptor.isIgnore(formDataAnnotation)) {
        return null;
      }

      var declaringType = recursiveDeclaringType.declaringType();
      if (declaringType.isEmpty()) {
        // primary type
        if (FormDataAnnotationDescriptor.isCreate(formDataAnnotation) || FormDataAnnotationDescriptor.isSdkCommandUse(formDataAnnotation)) {
          return formDataAnnotation.getFormDataType();
        }
        return null;
      }

      recursiveDeclaringType = declaringType.orElseThrow();
    }
  }

  protected static String computeSuperTypeForFormData(IType modelType, FormDataAnnotationDescriptor formDataAnnotation) {
    // handle replace
    if (modelType.annotations().withManagedWrapper(ReplaceAnnotation.class).existsAny()) {
      var replaced = modelType.superClass()
          .flatMap(AbstractDtoGenerator::getFormDataType)
          .map(IType::reference);
      if (replaced.isPresent()) {
        return replaced.orElseThrow();
      }
    }
    return computeSuperTypeForFormDataIgnoringReplace(modelType, formDataAnnotation);
  }

  private static String computeSuperTypeForFormDataIgnoringReplace(IType formField, FormDataAnnotationDescriptor formDataAnnotation) {
    var superType = formDataAnnotation.getSuperType();
    if (superType == null) {
      return null;
    }

    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      var genericOrdinalDefinitionType = formDataAnnotation.getGenericOrdinalDefinitionType();
      if (genericOrdinalDefinitionType != null && superType.hasTypeParameters()) {
        var genericType = computeDtoGenericType(formField, genericOrdinalDefinitionType, formDataAnnotation.getGenericOrdinal());
        if (genericType.isPresent()) {
          return genericType
              .map(IType::reference)
              .map(fqn -> superType.name() + JavaTypes.C_GENERIC_START + fqn + JavaTypes.C_GENERIC_END)
              .orElseThrow();
        }
      }
    }
    return superType.reference();
  }

  private static Optional<IType> findExtendsAnnotationValue(IType element) {
    return element.superTypes().withSuperInterfaces(false).stream()
        .flatMap(curType -> curType.annotations().withManagedWrapper(ExtendsAnnotation.class).first().stream())
        .map(ExtendsAnnotation::value)
        .findAny();
  }

  /**
   * Gets the {@link IType} the given model type extends or {@code null} if none.<br>
   * The given modelType must be an IExtension or must have an @Extends annotation.
   *
   * @param modelType
   *          The extension whose owner should be returned.
   * @return The owner of the given extension or null.
   */
  private Optional<IType> getExtendedType(IType modelType) {
    if (modelType == null) {
      return Optional.empty();
    }

    // 1. try to read from @Extends annotation
    var extendsValue = findExtendsAnnotationValue(modelType);
    if (extendsValue.isPresent()) {
      return extendsValue;
    }

    // 2. try to read from generic
    var iExtension = scoutApi().IExtension();
    if (modelType.isInstanceOf(iExtension)) {
      var owner = modelType.resolveTypeParamValue(iExtension.ownerTypeParamIndex(), iExtension.fqn());
      if (owner.isPresent()) {
        return owner.orElseThrow().findFirst();
      }
    }

    // 3. try in declaring type
    return modelType
        .declaringType()
        .flatMap(this::getExtendedType);
  }

  protected void setupBuilder() {
    // flags
    asPublic()
        .withSuperClass(computeSuperType())
        .withField(FieldGenerator.createSerialVersionUid());
    if (declaringGenerator().orElse(null) instanceof ITypeGenerator) {
      asStatic();
    }
    if (isAbstract(modelType().flags())) {
      asAbstract();
    }

    // copy annotations over to the DTO
    copyAnnotations();
  }

  protected TYPE withReplaceIfNecessary() {
    // add replace annotation to DTO if replace annotation is present on the model
    if (modelType().annotations().withManagedWrapper(ReplaceAnnotation.class).existsAny()) {
      withAnnotation(ScoutAnnotationGenerator.createReplace());
    }
    return thisInstance();
  }

  protected abstract String computeSuperType();

  public IType modelType() {
    return m_modelType;
  }

  protected static boolean hasDtoAnnotation(IAnnotatable method, IScoutAnnotationApi scoutApi) {
    var formDataFqn = scoutApi.FormData().fqn();
    var data = scoutApi.Data().fqn();
    return method.annotations().withName(formDataFqn).existsAny() || method.annotations().withName(data).existsAny();
  }

  protected TYPE withPropertyDtos() {
    var scoutApi = scoutApi();
    PropertyBean.of(modelType())
        .filter(bean -> bean.readMethod().isPresent() && bean.writeMethod().isPresent())
        .filter(bean -> hasDtoAnnotation(bean.readMethod().orElseThrow(), scoutApi) || hasDtoAnnotation(bean.writeMethod().orElseThrow(), scoutApi))
        .sorted(comparing(PropertyBean::name).thenComparing(PropertyBean::toString))
        .forEach(this::addPropertyDto);
    return thisInstance();
  }

  @SuppressWarnings("squid:UnusedPrivateMethod") // used as method-reference
  private void addPropertyDto(PropertyBean desc) {
    var lowerCaseBeanName = Introspector.decapitalize(desc.name());
    var upperCaseBeanName = Strings.capitalize(desc.name()).toString();
    var propName = upperCaseBeanName + ISdkConstants.SUFFIX_DTO_PROPERTY;
    var propDataType = desc.type().reference();
    var propDataTypeBoxed = JavaTypes.boxPrimitive(propDataType);

    // property class
    var abstractPropertyDataApi = scoutApi().AbstractPropertyData();
    var propertyTypeBuilder = TypeGenerator.create()
        .asPublic()
        .asStatic()
        .withElementName(propName)
        .withSuperClass(abstractPropertyDataApi.fqn() + JavaTypes.C_GENERIC_START + propDataTypeBoxed + JavaTypes.C_GENERIC_END)
        .withField(FieldGenerator.createSerialVersionUid());
    copyAnnotations(desc.readMethod().orElseThrow(), propertyTypeBuilder, targetEnvironment());

    var getterName = PropertyBean.GETTER_PREFIX + propName;
    this
        .withType(propertyTypeBuilder, DtoMemberSortObjectFactory.forTypeFormDataProperty(propName))
        .withMethod(ScoutMethodGenerator.create() // getter
            .asPublic()
            .withElementName(getterName)
            .withReturnType(propName)
            .withBody(b -> b.returnClause().appendGetPropertyByClass(propName).semicolon()),
            DtoMemberSortObjectFactory.forMethodFormDataProperty(upperCaseBeanName))
        .withMethod(MethodGenerator.create() // legacy getter
            .asPublic()
            .withElementName(PropertyBean.getterPrefixFor(propDataType) + upperCaseBeanName)
            .withComment(b -> b.appendJavaDocComment("access method for property " + upperCaseBeanName + JavaTypes.C_DOT))
            .withReturnType(propDataType)
            .withBody(b -> {
              var suffix = "()." + abstractPropertyDataApi.getValueMethodName() + "()";
              b.returnClause().append(getterName).append(suffix);
              if (JavaTypes.isPrimitive(propDataType)) {
                b.append(" == ").nullLiteral().append(" ? ").appendDefaultValueOf(propDataTypeBoxed).append(" : ").append(getterName).append(suffix);
              }
              b.semicolon();
            }),
            DtoMemberSortObjectFactory.forMethodFormDataPropertyLegacy(upperCaseBeanName))
        .withMethod(MethodGenerator.create() // legacy setter
            .asPublic()
            .withElementName(PropertyBean.SETTER_PREFIX + upperCaseBeanName)
            .withComment(b -> b.appendJavaDocComment("access method for property " + upperCaseBeanName + JavaTypes.C_DOT))
            .withReturnType(JavaTypes._void)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(lowerCaseBeanName)
                .withDataType(propDataType))
            .withBody(b -> b.append(getterName).parenthesisOpen().parenthesisClose()
                .dot().append(abstractPropertyDataApi.setValueMethodName()).parenthesisOpen().appendParameterName(0).parenthesisClose().semicolon()),
            DtoMemberSortObjectFactory.forMethodFormDataPropertyLegacy(upperCaseBeanName));
  }

  public IJavaEnvironment targetEnvironment() {
    return m_targetEnvironment;
  }
}
