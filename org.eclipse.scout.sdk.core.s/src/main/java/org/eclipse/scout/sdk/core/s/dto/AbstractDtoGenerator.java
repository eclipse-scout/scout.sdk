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

import java.beans.Introspector;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.ExtendsAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;

/**
 * <h3>{@link AbstractDtoGenerator}</h3>
 *
 * @since 3.10.0 2013-08-27
 */
public abstract class AbstractDtoGenerator<TYPE extends AbstractDtoGenerator<TYPE>> extends TypeGenerator<TYPE> {

  private final IType m_modelType;
  private final IJavaEnvironment m_targetEnvironment;
  private boolean m_setupExecuted;

  protected AbstractDtoGenerator(IType modelType, IJavaEnvironment targetEnvironment) {
    m_modelType = Ensure.notNull(modelType);
    m_targetEnvironment = Ensure.notNull(targetEnvironment);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    if (!m_setupExecuted) {
      setupBuilder();
      m_setupExecuted = true;
    }
    super.build(builder);
  }

  protected void copyAnnotations() {
    copyAnnotations(modelType(), this, targetEnvironment());
  }

  private static void copyAnnotations(IAnnotatable annotationOwner, ITypeGenerator<?> target, IJavaEnvironment targetEnv) {
    annotationOwner.annotations().stream()
        .filter(a -> isAnnotationDtoRelevant(a.type()))
        .filter(a -> targetEnv.exists(a.type()))
        .map(AbstractDtoGenerator::toDtoAnnotationGenerator)
        .forEach(target::withAnnotation);
  }

  /**
   * Override original ClassId value
   */
  private static IAnnotationGenerator<?> toDtoAnnotationGenerator(IAnnotation a) {
    IAnnotationGenerator<?> result = a.toWorkingCopy();
    if (IScoutRuntimeTypes.ClassId.equals(a.type().name())) {
      String id = a.element("value").get().value().as(String.class);
      result.withElement("value", b -> b.stringLiteral(id + "-formdata"));
    }
    return result;
  }

  private static boolean isAnnotationDtoRelevant(IType annotationType) {
    if (annotationType == null) {
      return false;
    }
    String elementName = annotationType.name();
    boolean isDtoAnnotation =
        IScoutRuntimeTypes.FormData.equals(elementName)
            || IScoutRuntimeTypes.PageData.equals(elementName)
            || IScoutRuntimeTypes.Data.equals(elementName);
    return !isDtoAnnotation && !IScoutRuntimeTypes.Order.equals(elementName) && annotationType.annotations().withName(IScoutRuntimeTypes.DtoRelevant).existsAny();
  }

  /**
   * Adds all interfaces as specified in the given {@link FormDataAnnotationDescriptor}.<br>
   * For all methods that also exist in the interfaces added and {@link Override} annotation is added as well.
   *
   * @param formDataAnnotation
   *          The {@link FormDataAnnotationDescriptor} holding the interfaces.
   */
  protected TYPE withAdditionalInterfaces(FormDataAnnotationDescriptor formDataAnnotation) {
    Set<IType> interfaces = formDataAnnotation.getInterfaces();
    if (interfaces.isEmpty()) {
      return currentInstance();
    }

    Set<String> allSuperInterfaceMethods = interfaces.stream()
        .filter(targetEnvironment()::exists)
        .peek(ifcType -> withInterface(ifcType.reference()))
        .flatMap(ifcType -> ifcType.superTypes().withSuperClasses(false).stream())
        .flatMap(superIfc -> superIfc.methods().stream())
        .map(IMethod::identifier)
        .collect(toSet());

    methods()
        .filter(msb -> allSuperInterfaceMethods.contains(msb.identifier()))
        .forEach(msb -> msb.withAnnotation(AnnotationGenerator.createOverride()));
    return currentInstance();
  }

  protected TYPE withExtendsAnnotationIfNecessary(IType element) {
    Optional<IType> extendedTypeOpt = getExtendedType(element);
    if (!extendedTypeOpt.isPresent()) {
      return currentInstance();
    }
    IType extendedType = extendedTypeOpt.get();
    IType primaryType = extendedType.primary();

    Optional<IType> extendedDto = Optional.empty();
    if (primaryType.isInstanceOf(IScoutRuntimeTypes.IForm) || primaryType.isInstanceOf(IScoutRuntimeTypes.IFormField)) {
      Optional<IType> declaring = extendedType.declaringType();
      if (extendedType.isInstanceOf(IScoutRuntimeTypes.ITable) && declaring.isPresent()) {
        Optional<IType> tableFieldDto = getFormDataType(declaring.get());
        extendedDto = tableFieldDto.flatMap(dto -> dto.innerTypes().withInstanceOf(IScoutRuntimeTypes.AbstractTableRowData).first());
      }
      else {
        extendedDto = findDtoForForm(primaryType);
      }
    }
    else if (primaryType.isInstanceOf(IScoutRuntimeTypes.IExtension)) {
      extendedDto = findDtoForPage(primaryType);
    }
    else if (primaryType.isInstanceOf(IScoutRuntimeTypes.IPageWithTable)) {
      Optional<IType> pageDto = findDtoForPage(primaryType);
      extendedDto = pageDto.flatMap(dto -> dto.innerTypes().withInstanceOf(IScoutRuntimeTypes.AbstractTableRowData).first());
    }

    return extendedDto
        .map(t -> withAnnotation(ScoutAnnotationGenerator.createExtends(t.reference())))
        .orElseGet(this::currentInstance);
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
    FormDataAnnotationDescriptor a = FormDataAnnotationDescriptor.of(form);
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
    String rowDataSuffix = "RowData";
    if (Strings.isBlank(base)) {
      return rowDataSuffix;
    }

    StringBuilder result = new StringBuilder(base.length() + rowDataSuffix.length());
    String[] suffixes = {"PageData", "FieldData", "Data"};
    for (String suffix : suffixes) {
      if (base.endsWith(suffix)) {
        result.append(base, 0, base.length() - suffix.length());
        break;
      }
    }
    if (result.length() < 1) {
      // has none of the suffixes
      result.append(base);
    }
    return result.append(rowDataSuffix).toString();
  }

  protected static String removeFieldSuffix(String fieldName) {
    if (fieldName.endsWith(ISdkProperties.SUFFIX_FORM_FIELD)) {
      return fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_FORM_FIELD.length());
    }
    if (fieldName.endsWith(ISdkProperties.SUFFIX_BUTTON)) {
      return fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_BUTTON.length());
    }
    if (fieldName.endsWith(ISdkProperties.SUFFIX_TABLE_COLUMN)) {
      return fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_TABLE_COLUMN.length());
    }
    if (fieldName.endsWith(ISdkProperties.SUFFIX_OUTLINE_PAGE)) {
      return fieldName.substring(0, fieldName.length() - ISdkProperties.SUFFIX_OUTLINE_PAGE.length());
    }
    return fieldName;
  }

  /**
   * @return Returns an {@link Optional} with the form field data/form data for the given form field/form.
   * @since 3.8.2
   */
  private static Optional<IType> getFormDataType(IType modelType) {
    IType primaryType = getFormFieldDataPrimaryTypeRec(modelType);
    if (primaryType == null) {
      return Optional.empty();
    }

    boolean isPrimaryType = !modelType.declaringType().isPresent();
    if (isPrimaryType) {
      // model type is a primary type (form, template) and we have a corresponding DTO type.
      return Optional.of(primaryType);
    }

    // check if the primary type itself is the correct type
    String formDataName = removeFieldSuffix(modelType.elementName());
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
      ITypeParameter param = annotationOwnerType.typeParameters()
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

      FormDataAnnotationDescriptor formDataAnnotation = FormDataAnnotationDescriptor.of(recursiveDeclaringType);
      if (FormDataAnnotationDescriptor.isIgnore(formDataAnnotation)) {
        return null;
      }

      Optional<IType> declaringType = recursiveDeclaringType.declaringType();
      if (!declaringType.isPresent()) {
        // primary type
        if (FormDataAnnotationDescriptor.isCreate(formDataAnnotation) || FormDataAnnotationDescriptor.isSdkCommandUse(formDataAnnotation)) {
          return formDataAnnotation.getFormDataType();
        }
        return null;
      }

      recursiveDeclaringType = declaringType.get();
    }
  }

  protected static String computeSuperTypeForFormData(IType modelType, FormDataAnnotationDescriptor formDataAnnotation) {
    // handle replace
    if (modelType.annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
      Optional<String> replaced = modelType.superClass()
          .flatMap(AbstractDtoGenerator::getFormDataType)
          .map(IType::reference);
      if (replaced.isPresent()) {
        return replaced.get();
      }
    }

    return computeSuperTypeForFormDataIgnoringReplace(modelType, formDataAnnotation);
  }

  private static String computeSuperTypeForFormDataIgnoringReplace(IType formField, FormDataAnnotationDescriptor formDataAnnotation) {
    IType superType = formDataAnnotation.getSuperType();
    if (superType == null) {
      return null;
    }

    if (formDataAnnotation.getGenericOrdinal() >= 0) {
      IType genericOrdinalDefinitionType = formDataAnnotation.getGenericOrdinalDefinitionType();
      if (genericOrdinalDefinitionType != null && superType.hasTypeParameters()) {
        Optional<IType> genericType = computeDtoGenericType(formField, genericOrdinalDefinitionType, formDataAnnotation.getGenericOrdinal());
        if (genericType.isPresent()) {
          return genericType
              .map(IType::reference)
              .map(fqn -> superType.name() + JavaTypes.C_GENERIC_START + fqn + JavaTypes.C_GENERIC_END)
              .get();
        }
      }
    }
    return superType.reference();
  }

  private static Optional<IType> findExtendsAnnotationValue(IType element) {
    return element.superTypes().withSuperInterfaces(false).stream()
        .map(curType -> curType.annotations().withManagedWrapper(ExtendsAnnotation.class).first())
        .filter(Optional::isPresent)
        .map(annot -> annot.get().value())
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
  private static Optional<IType> getExtendedType(IType modelType) {
    if (modelType == null) {
      return Optional.empty();
    }

    // 1. try to read from @Extends annotation
    Optional<IType> extendsValue = findExtendsAnnotationValue(modelType);
    if (extendsValue.isPresent()) {
      return extendsValue;
    }

    // 2. try to read from generic
    if (modelType.isInstanceOf(IScoutRuntimeTypes.IExtension)) {
      Optional<Stream<IType>> owner = modelType.resolveTypeParamValue(IScoutRuntimeTypes.TYPE_PARAM_EXTENSION__OWNER, IScoutRuntimeTypes.IExtension);
      if (owner.isPresent()) {
        return owner.get().findFirst();
      }
    }

    // 3. try in declaring type
    return modelType
        .declaringType()
        .flatMap(AbstractDtoGenerator::getExtendedType);
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
    if (modelType().annotations().withName(IScoutRuntimeTypes.Replace).existsAny()) {
      withAnnotation(ScoutAnnotationGenerator.createReplace());
    }
    return currentInstance();
  }

  protected abstract String computeSuperType();

  public IType modelType() {
    return m_modelType;
  }

  protected TYPE withPropertyDtos() {
    Predicate<IMethod> hasDtoAnnotation = method -> method.annotations().withName(IScoutRuntimeTypes.FormData).existsAny()
        || method.annotations().withName(IScoutRuntimeTypes.Data).existsAny();

    PropertyBean.of(modelType())
        .filter(bean -> bean.readMethod().isPresent() && bean.writeMethod().isPresent())
        .filter(bean -> hasDtoAnnotation.test(bean.readMethod().get()) || hasDtoAnnotation.test(bean.writeMethod().get()))
        .sorted(comparing(PropertyBean::name).thenComparing(PropertyBean::toString))
        .forEach(this::addPropertyDto);
    return currentInstance();
  }

  @SuppressWarnings("squid:UnusedPrivateMethod") // used as method-reference
  private void addPropertyDto(PropertyBean desc) {
    String lowerCaseBeanName = Introspector.decapitalize(desc.name());
    String upperCaseBeanName = Strings.ensureStartWithUpperCase(desc.name());

    String propName = upperCaseBeanName + ISdkProperties.SUFFIX_DTO_PROPERTY;
    String propDataType = desc.type().reference();
    String propDataTypeBoxed = JavaTypes.boxPrimitive(propDataType);

    // property class
    ITypeGenerator<?> propertyTypeBuilder = TypeGenerator.create()
        .asPublic()
        .asStatic()
        .withElementName(propName)
        .withSuperClass(IScoutRuntimeTypes.AbstractPropertyData + JavaTypes.C_GENERIC_START + propDataTypeBoxed + JavaTypes.C_GENERIC_END)
        .withField(FieldGenerator.createSerialVersionUid());
    copyAnnotations(desc.readMethod().get(), propertyTypeBuilder, targetEnvironment());

    withType(propertyTypeBuilder)
        .withMethod(ScoutMethodGenerator.create() // getter
            .asPublic()
            .withElementName(PropertyBean.GETTER_PREFIX + propName)
            .withReturnType(propName)
            .withBody(b -> b.returnClause().appendGetPropertyByClass(propName).semicolon()))
        .withMethod(MethodGenerator.create() // legacy getter
            .asPublic()
            .withElementName(PropertyBean.getterPrefixFor(propDataType) + upperCaseBeanName)
            .withComment(b -> b.appendJavaDocComment("access method for property " + upperCaseBeanName + JavaTypes.C_DOT))
            .withReturnType(propDataType)
            .withBody(b -> {
              String suffix = "().getValue()";
              b.returnClause().append("get").append(propName).append(suffix);
              if (JavaTypes.isPrimitive(propDataType)) {
                b.append(" == null ? ")
                    .appendDefaultValueOf(propDataTypeBoxed).append(" : get").append(propName).append(suffix);
              }
              b.semicolon();
            }))
        .withMethod(MethodGenerator.create() // legacy setter
            .asPublic()
            .withElementName(PropertyBean.SETTER_PREFIX + upperCaseBeanName)
            .withComment(b -> b.appendJavaDocComment("access method for property " + upperCaseBeanName + JavaTypes.C_DOT))
            .withReturnType(JavaTypes._void)
            .withParameter(MethodParameterGenerator.create()
                .withElementName(lowerCaseBeanName)
                .withDataType(propDataType))
            .withBody(b -> b.append("get").append(propName).append("().setValue(").append(lowerCaseBeanName).parenthesisClose().semicolon()));
  }

  public IJavaEnvironment targetEnvironment() {
    return m_targetEnvironment;
  }
}
