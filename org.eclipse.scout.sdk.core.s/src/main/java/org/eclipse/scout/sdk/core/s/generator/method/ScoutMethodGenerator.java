/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.generator.method;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.joining;
import static org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator.createDoConvenienceMethodsGenerated;
import static org.eclipse.scout.sdk.core.util.Strings.capitalize;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.model.api.query.AbstractQuery;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.builder.java.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link ScoutMethodGenerator}</h3>
 *
 * @since 6.1.0
 */
public class ScoutMethodGenerator<TYPE extends IScoutMethodGenerator<TYPE, BODY>, BODY extends IScoutMethodBodyBuilder<?>> extends MethodGenerator<TYPE, BODY> implements IScoutMethodGenerator<TYPE, BODY> {

  protected ScoutMethodGenerator() {
  }

  protected ScoutMethodGenerator(IMethod method, IWorkingCopyTransformer transformer) {
    super(method, transformer);
  }

  public static IScoutMethodGenerator<?, ?> create(IMethod method, IWorkingCopyTransformer transformer) {
    return new ScoutMethodGenerator<>(method, transformer);
  }

  public static IScoutMethodGenerator<?, ?> create() {
    return new ScoutMethodGenerator<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected BODY createMethodBodyBuilder(ISourceBuilder<?> inner) {
    return (BODY) ScoutMethodBodyBuilder.create(inner, this);
  }

  /**
   * Creates an {@link IScoutMethodGenerator} which creates a form field getter of the form {@code public MyField
   * getMyField() { return getFieldByClass(MyField.class); }}
   *
   * @param fieldFqn
   *          The fully qualified name of the form field.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createFieldGetter(String fieldFqn) {
    var dotBasedFqn = Ensure.notBlank(fieldFqn).replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT);
    var fieldSimpleName = JavaTypes.simpleName(dotBasedFqn);
    return create()
        .asPublic()
        .withElementName(PropertyBean.GETTER_PREFIX + capitalize(fieldSimpleName))
        .withReturnType(fieldFqn)
        .withBody(b -> b.returnClause().appendGetFieldByClass(fieldFqn).semicolon());
  }

  /**
   * Creates a protected method that returns the text value of the given translation key of the form {@code @Override
   * protected methodName() { return TEXTS.get("nlsKeyName"); }}
   *
   * @param methodName
   *          The name of the method
   * @param nlsKeyName
   *          The nls key to insert
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createNlsMethod(String methodName, CharSequence nlsKeyName) {
    return createNlsMethodFrom(null, api -> methodName, nlsKeyName);
  }

  /**
   * Creates a protected method that returns the text value of the given translation key of the form {@code @Override
   * protected methodName() { return TEXTS.get("nlsKeyName"); }}
   *
   * @param api
   *          The {@link IApiSpecification} that contains the method name.
   * @param methodFunction
   *          A function that gets the required method name from the input api.
   * @param nlsKeyName
   *          The nls key to insert
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static <API extends IApiSpecification> IScoutMethodGenerator<?, ?> createNlsMethodFrom(Class<API> api, Function<API, String> methodFunction, CharSequence nlsKeyName) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withElementNameFrom(api, methodFunction)
        .withReturnType(String.class.getName())
        .withBody(b -> b.appendTodo("verify translation")
            .returnClause().refClassFrom(IScoutApi.class, IScoutVariousApi::TEXTS).dot()
            .appendFrom(IScoutApi.class, a -> a.TEXTS().getMethodName()).parenthesisOpen().stringLiteral(nlsKeyName).parenthesisClose().semicolon());
  }

  /**
   * Creates a DoNode chained setter method of the form:
   *
   * <pre>
   * &#64;Generated
   * public OwnerClass withName(dataType name) {
   *   name().set(name);
   *   return this;
   * }
   * </pre>
   *
   * An {@link Override} annotation is added automatically if required.
   *
   * @param name
   *          The DoNode name used for the method name without the "with" prefix.
   * @param dataTypeReference
   *          The data type reference of the DoNode.
   * @param owner
   *          The {@link IType} in which the method will be added.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoNodeSetter(String name, String dataTypeReference, IType owner) {
    return createDoNodeSetter(name, dataTypeReference, buildReturnTypeReferenceFor(owner))
        .withOverrideIfNecessary(true, owner);
  }

  /**
   * Creates a DoNode chained setter method of the form:
   *
   * <pre>
   * &#64;Generated
   * public returnType withName(dataType name) {
   *   name().set(name);
   *   return this;
   * }
   * </pre>
   *
   * @param name
   *          The DoNode name used for the method name without the "with" prefix.
   * @param dataTypeReference
   *          The data type reference of the DoNode.
   * @param returnTypeReference
   *          The data type reference of the method return type.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoNodeSetter(String name, String dataTypeReference, String returnTypeReference) {
    return create()
        .asPublic()
        .withReturnType(returnTypeReference)
        .withElementName(PropertyBean.CHAINED_SETTER_PREFIX + capitalize(name))
        .withParameter(MethodParameterGenerator.create()
            .withElementName(name)
            .withDataType(dataTypeReference))
        .withAnnotation(createDoConvenienceMethodsGenerated())
        .withBody(b -> b.appendDoNodeSet(name, name).nl().returnClause().appendThis().semicolon());
  }

  /**
   * Creates a DoList chained setter for collection values of the form:
   *
   * <pre>
   * &#64;Generated
   * public OwnerClass withName(Collection&#60;? extends dataType&#62; name) {
   *   name().updateAll(name);
   *   return this;
   * }
   * </pre>
   *
   * An {@link Override} annotation is added automatically if required.
   *
   * @param name
   *          The DoNode name used for the method name without the "with" prefix.
   * @param dataTypeReference
   *          The data type reference of a single DoList element.
   * @param owner
   *          The {@link IType} in which the method will be added.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoListSetterCollection(String name, CharSequence dataTypeReference, IType owner) {
    var methodName = PropertyBean.CHAINED_SETTER_PREFIX + capitalize(name);
    var paramDataType = computeDoNodeCollectionSetterParameterDataType(owner, methodName, dataTypeReference);
    return create()
        .asPublic()
        .withReturnType(buildReturnTypeReferenceFor(owner))
        .withElementName(methodName)
        .withParameter(MethodParameterGenerator.create()
            .withElementName(name)
            .withDataType(paramDataType))
        .withAnnotation(createDoConvenienceMethodsGenerated())
        .withBody(b -> b.appendDoNodeUpdateAll(name, name).nl().returnClause().appendThis().semicolon())
        .withOverrideIfNecessary(true, owner);
  }

  /**
   * Creates a DoList chained setter for varargs values of the form:
   *
   * <pre>
   * &#64;Generated
   * public OwnerClass withName(dataType... name) {
   *   name().updateAll(name);
   *   return this;
   * }
   * </pre>
   *
   * An {@link Override} annotation is added automatically if required.
   *
   * @param name
   *          The DoNode name used for the method name without the "with" prefix.
   * @param dataTypeReference
   *          The data type reference of a single DoList element.
   * @param owner
   *          The {@link IType} in which the method will be added.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoListSetterArray(String name, String dataTypeReference, IType owner) {
    var generator = createDoListSetterCollection(name, dataTypeReference, owner);
    generator.parameters().findAny().get().withDataType(dataTypeReference).asVarargs();
    return generator;
  }

  /**
   * Creates a DoNode getter of the form:
   *
   * <pre>
   * &#64;Generated
   * public dataType getName() {
   *   return name().get();
   * }
   * </pre>
   *
   * An {@link Override} annotation is added automatically if required.
   *
   * @param name
   *          The DoNode name used for the method name without the "get" or "is" prefix.
   * @param returnTypeReference
   *          The data type of the DoNode (the return type of the method).
   * @param owner
   *          The {@link IType} in which the method will be added.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoNodeGetter(CharSequence name, String returnTypeReference, IType owner) {
    return createDoNodeGetter(name, returnTypeReference)
        .withOverrideIfNecessary(true, owner);
  }

  /**
   * Creates a DoNode getter of the form:
   *
   * <pre>
   * &#64;Generated
   * public dataType getName() {
   *   return name().get();
   * }
   * </pre>
   *
   * @param name
   *          The DoNode name used for the method name without the "get" or "is" prefix.
   * @param returnTypeReference
   *          The data type of the DoNode (the return type of the method).
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoNodeGetter(CharSequence name, String returnTypeReference) {
    var getterPrefix = dataObjectGetterPrefixFor(returnTypeReference);
    return create()
        .asPublic()
        .withReturnType(returnTypeReference)
        .withElementName(getterPrefix + capitalize(name))
        .withAnnotation(createDoConvenienceMethodsGenerated())
        .withBody(b -> b.returnClause().appendDoNodeGet(name).semicolon());
  }

  protected static String computeDoNodeCollectionSetterParameterDataType(IType owner, CharSequence methodName, CharSequence dataTypeReference) {
    var methodId = JavaTypes.createMethodIdentifier(methodName, singleton(Collection.class.getName()));
    var parentMethod = owner.superTypes()
        .withSelf(false).stream()
        .flatMap(st -> st.methods().withMethodIdentifier(methodId).stream())
        .findAny();

    // inherit parameter signature from parent (sometimes it is Collection<? extends Xyz> and sometimes only implemented as Collection<Xyz>).
    var needsExtends = parentMethod
        .map(IMethod::parameters)
        .flatMap(AbstractQuery::first)
        .map(IMethodParameter::dataType)
        .map(IType::reference)
        .map(ref -> ref.contains(JavaTypes.EXTENDS))
        .orElse(true);

    var collectionDataTypeRef = new StringBuilder(Collection.class.getName()).append(JavaTypes.C_GENERIC_START);
    if (needsExtends) {
      collectionDataTypeRef.append(JavaTypes.C_QUESTION_MARK).append(' ').append(JavaTypes.EXTENDS).append(' ').append(dataTypeReference);
    }
    else {
      collectionDataTypeRef.append(dataTypeReference);
    }
    collectionDataTypeRef.append(JavaTypes.C_GENERIC_END);
    return collectionDataTypeRef.toString();
  }

  protected static String dataObjectGetterPrefixFor(String type) {
    // for DOs the bool prefix is used for boxed booleans
    if (Boolean.class.getName().equals(type)) {
      return PropertyBean.GETTER_BOOL_PREFIX;
    }
    return PropertyBean.GETTER_PREFIX;
  }

  protected static String buildReturnTypeReferenceFor(IType owner) {
    var ref = owner.reference();
    if (!owner.hasTypeParameters()) {
      return ref;
    }
    return owner.typeParameters()
        .map(IJavaElement::elementName)
        .collect(joining(", ", ref + JavaTypes.C_GENERIC_START, JavaTypes.C_GENERIC_END + ""));
  }

  /**
   * Creates a getConfiguredDisplayable method of the form:
   *
   * <pre>
   * &#64;Override
   * protected boolean getConfiguredDisplayable() {
   *   return true;
   * }
   * </pre>
   *
   * @param displayable
   *          The boolean return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredDisplayable(boolean displayable) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractColumn().getConfiguredDisplayableMethodName())
        .asProtected()
        .withReturnType(JavaTypes._boolean)
        .withBody(b -> b.returnClause().append(displayable).semicolon());
  }

  /**
   * Creates a getConfiguredPrimaryKey method of the form:
   *
   * <pre>
   * &#64;Override
   * protected boolean getConfiguredPrimaryKey() {
   *   return false;
   * }
   * </pre>
   *
   * @param primaryKey
   *          The boolean return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredPrimaryKey(boolean primaryKey) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractColumn().getConfiguredPrimaryKeyMethodName())
        .asProtected()
        .withReturnType(JavaTypes._boolean)
        .withBody(b -> b.returnClause().append(primaryKey).semicolon());
  }

  /**
   * Creates a getConfiguredWidth method of the form:
   *
   * <pre>
   * &#64;Override
   * protected int getConfiguredWidth() {
   *   return 60;
   * }
   * </pre>
   *
   * @param width
   *          The int return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredWidth(int width) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withReturnType(JavaTypes._int)
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractColumn().getConfiguredWidthMethodName())
        .withBody(b -> b.returnClause().append(width).semicolon());
  }

  /**
   * Creates a getConfiguredMenuTypes method of the form:
   *
   * <pre>
   * &#64;Override
   * protected Set<? extends IMenuType> getConfiguredMenuTypes() {
   *   return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
   * }
   * </pre>
   *
   * @param menuTypes
   *          A sourceGenerator that allows to add menu types.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredMenuTypes(ISourceGenerator<IExpressionBuilder<?>> menuTypes) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractMenu().getConfiguredMenuTypesMethodName())
        .asProtected()
        .withReturnTypeFrom(IScoutApi.class, api -> new StringBuilder(Set.class.getName()).append(JavaTypes.C_GENERIC_START)
            .append(JavaTypes.C_QUESTION_MARK).append(' ').append(JavaTypes.EXTENDS).append(' ').append(api.IMenuType().fqn())
            .append(JavaTypes.C_GENERIC_END).toString())
        .withBody(b -> {
          b.returnClause().refClassFrom(IScoutApi.class, IScoutApi::CollectionUtility).dot().appendFrom(IScoutApi.class, api -> api.CollectionUtility().hashSetMethodName()).parenthesisOpen();
          if (menuTypes != null) {
            b.append(menuTypes.generalize(ExpressionBuilder::create));
          }
          b.parenthesisClose().semicolon();
        });
  }

  /**
   * Creates a getConfiguredMandatory method of the form:
   *
   * <pre>
   * &#64;Override
   * protected boolean getConfiguredMandatory() {
   *   return false;
   * }
   * </pre>
   *
   * @param mandatory
   *          The boolean return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredMandatory(boolean mandatory) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractFormField().getConfiguredMandatoryMethodName())
        .asProtected()
        .withReturnType(JavaTypes._boolean)
        .withBody(b -> b.returnClause().append(mandatory).semicolon());
  }

  /**
   * Creates a getConfiguredMaxLength method of the form:
   *
   * <pre>
   * &#64;Override
   * protected int getConfiguredMaxLength() {
   *   return 60;
   * }
   * </pre>
   *
   * @param maxLength
   *          The int return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredMaxLength(int maxLength) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withReturnType(JavaTypes._int)
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractStringField().getConfiguredMaxLengthMethodName())
        .withBody(b -> b.returnClause().append(maxLength).semicolon());
  }

  /**
   * Creates a getConfiguredService method of the form:
   *
   * <pre>
   * &#64;Override
   * protected Class<? extends ILookupService<EntityKey>> getConfiguredService() {
   *   return IEntityLookupService.class;
   * }
   * </pre>
   *
   * @param serviceIfc
   *          The fully qualified name of the service interface.
   * @param keyType
   *          The fully qualified name of the key type.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredService(CharSequence serviceIfc, String keyType) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withReturnTypeFrom(IScoutApi.class, api -> new StringBuilder(Class.class.getName()).append(JavaTypes.C_GENERIC_START)
            .append(JavaTypes.C_QUESTION_MARK).append(JavaTypes.C_SPACE).append(JavaTypes.EXTENDS).append(JavaTypes.C_SPACE)
            .append(api.ILookupService().fqn()).append(JavaTypes.C_GENERIC_START).append(keyType).append(JavaTypes.C_GENERIC_END)
            .append(JavaTypes.C_GENERIC_END).toString())
        .withElementNameFrom(IScoutApi.class, api -> api.LookupCall().getConfiguredServiceMethodName())
        .withBody(b -> b.returnClause().classLiteral(serviceIfc).semicolon());
  }
}
