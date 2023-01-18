/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.generator.method;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.joining;
import static org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator.createDoConvenienceMethodsGenerated;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.Strings.capitalize;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.java.model.api.query.AbstractQuery;
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode;
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode.DataObjectNodeKind;
import org.eclipse.scout.sdk.core.s.java.apidef.IScout22DoApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.builder.body.IScoutMethodBodyBuilder;

/**
 * Provides data object related method factory functions.
 */
public final class ScoutDoMethodGenerator extends ScoutMethodGenerator<ScoutDoMethodGenerator, IScoutMethodBodyBuilder<?>> {

  public static final String CONVENIENCE_METHOD_MARKER_START = "/* ******************";

  private ScoutDoMethodGenerator() {
  }

  /**
   * Creates a new data object node (data object attribute) of the form (example for {@link DataObjectNodeKind#VALUE}:
   * 
   * <pre>
   * public DoValue&#60;dataType&#62; name() {
   *   return doValue("name");
   * }
   * </pre>
   * 
   * @param name
   *          The name of the node. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the node. Must not be {@code null}.
   * @param dataType
   *          The datatype of the DoNode. If the kind is {@link DataObjectNodeKind#LIST}, {@link DataObjectNodeKind#SET}
   *          or {@link DataObjectNodeKind#COLLECTION} the datatype always describes the type of element without the
   *          collection type itself. Must not be {@code null} or blank.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoNode(String name, DataObjectNodeKind kind, String dataType) {
    return createDoNodeFunc(name, kind, JavaBuilderContextFunction.orNull(dataType));
  }

  /**
   * Creates a new data object node (data object attribute) of the form (example for {@link DataObjectNodeKind#VALUE}:
   * 
   * <pre>
   * public DoValue&#60;dataType&#62; name() {
   *   return doValue("name");
   * }
   * </pre>
   * 
   * @param name
   *          The name of the node. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the node. Must not be {@code null}.
   * @param api
   *          The {@link IApiSpecification} that defines the datatype of the data object node. May be {@code null} in
   *          case the dataType function can handle a {@code null} input.
   * @param dataType
   *          A function returning the data type of the data object node. Must not be {@code null}.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static <API extends IApiSpecification> IScoutMethodGenerator<?, ?> createDoNodeFrom(String name, DataObjectNodeKind kind, Class<API> api, Function<API, String> dataType) {
    return createDoNodeFunc(name, kind, new ApiFunction<>(api, dataType));
  }

  /**
   * Creates a new data object node (data object attribute) of the form (example for {@link DataObjectNodeKind#VALUE}:
   * 
   * <pre>
   * public DoValue&#60;dataType&#62; name() {
   *   return doValue("name");
   * }
   * </pre>
   * 
   * @param name
   *          The name of the node. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the node. Must not be {@code null}.
   * @param dataType
   *          A function returning the data type of the data object node. Must not be {@code null}.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoNodeFunc(String name, DataObjectNodeKind kind, Function<IJavaBuilderContext, String> dataType) {
    return create()
        .asPublic()
        .withReturnTypeFunc(ctx -> buildDoNodeType(kind, dataType, ctx))
        .withElementName(name)
        .withBody(b -> b.returnClause().append(buildDoNodeMethod(kind, b.context())).parenthesisOpen().stringLiteral(name).parenthesisClose().semicolon().nl());
  }

  static String buildDoNodeMethod(DataObjectNodeKind kind, IJavaBuilderContext ctx) {
    var scoutApi = ctx.requireApi(IScoutApi.class);
    return switch (kind) {
      case VALUE -> scoutApi.DoEntity().doValueMethodName();
      case LIST -> scoutApi.DoEntity().doListMethodName();
      case COLLECTION -> scoutApi.requireApi(IScout22DoApi.class).DoEntity().doCollectionMethodName();
      case SET -> scoutApi.requireApi(IScout22DoApi.class).DoEntity().doSetMethodName();
    };
  }

  static String buildDoNodeType(DataObjectNodeKind kind, Function<IJavaBuilderContext, String> dataType, IJavaBuilderContext ctx) {
    var scoutApi = ctx.requireApi(IScoutApi.class);
    var type = switch (kind) {
      case VALUE -> scoutApi.DoValue();
      case LIST -> scoutApi.DoList();
      case COLLECTION -> scoutApi.requireApi(IScout22DoApi.class).DoCollection();
      case SET -> scoutApi.requireApi(IScout22DoApi.class).DoSet();
    };
    return type.fqn() + JavaTypes.C_GENERIC_START + dataType.apply(ctx) + JavaTypes.C_GENERIC_END;
  }

  /**
   * Creates a DoValue chained setter method of the form:
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
  public static IScoutMethodGenerator<?, ?> createDoValueSetter(String name, String dataTypeReference, IType owner) {
    return createDoValueSetter(name, dataTypeReference, buildReturnTypeReferenceFor(owner))
        .withOverrideIfNecessary(true, owner);
  }

  /**
   * Creates a DoValue chained setter method of the form:
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
  public static IScoutMethodGenerator<?, ?> createDoValueSetter(String name, String dataTypeReference, String returnTypeReference) {
    return createDoValueSetterFrom(name, null, a -> dataTypeReference, returnTypeReference);
  }

  /**
   * Creates a DoValue chained setter method of the form:
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
   * @param api
   *          The {@link IApiSpecification} that defines the datatype of the setter. May be {@code null} in case the
   *          dataTypeFunc can handle a {@code null} input.
   * @param dataTypeFunc
   *          A function returning the data type of the setter. Must not be {@code null}.
   * @param returnTypeReference
   *          The data type reference of the method return type.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static <API extends IApiSpecification> IScoutMethodGenerator<?, ?> createDoValueSetterFrom(String name, Class<API> api, Function<API, String> dataTypeFunc, String returnTypeReference) {
    return create()
        .asPublic()
        .withReturnType(returnTypeReference)
        .withElementName(PropertyBean.CHAINED_SETTER_PREFIX + capitalize(name))
        .withParameter(MethodParameterGenerator.create()
            .withElementName(name)
            .withDataTypeFrom(api, dataTypeFunc))
        .withAnnotation(createDoConvenienceMethodsGenerated())
        .withBody(b -> b.appendDoNodeSet(name, name).nl().returnClause().appendThis().semicolon());
  }

  /**
   * Creates a collection DO chained setter for collection values of the form:
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
   *          The data type reference of a single collection DO element.
   * @param owner
   *          The {@link IType} in which the method will be added.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoCollectionSetterCollection(String name, CharSequence dataTypeReference, IType owner) {
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
        .withBody(b -> b.appendDoCollectionUpdateAll(name, name).nl().returnClause().appendThis().semicolon())
        .withOverrideIfNecessary(true, owner);
  }

  static String computeDoNodeCollectionSetterParameterDataType(IType owner, CharSequence methodName, CharSequence dataTypeReference) {
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

  /**
   * Creates a collection DO chained setter for varargs values of the form:
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
   *          The data type reference of a single collection DO element.
   * @param owner
   *          The {@link IType} in which the method will be added.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createDoCollectionSetterVarargs(String name, String dataTypeReference, IType owner) {
    var generator = createDoCollectionSetterCollection(name, dataTypeReference, owner);
    generator.parameters().findAny().orElseThrow().withDataType(dataTypeReference).asVarargs();
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
    return createDoNodeGetterFrom(name, null, a -> returnTypeReference);
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
   * @param api
   *          The {@link IApiSpecification} that defines the datatype of the getter. May be {@code null} in case the
   *          returnTypeFunction can handle a {@code null} input.
   * @param returnTypeFunction
   *          A function returning the data type of the getter. Must not be {@code null}.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static <API extends IApiSpecification> IScoutMethodGenerator<?, ?> createDoNodeGetterFrom(CharSequence name, Class<API> api, Function<API, String> returnTypeFunction) {
    return create()
        .asPublic()
        .withReturnTypeFrom(api, returnTypeFunction)
        .withElementNameFunc(c -> computeDoNodeGetterName(c, name, computeDoNodeGetterReturnType(c, api, returnTypeFunction)))
        .withAnnotation(createDoConvenienceMethodsGenerated())
        .withBody(b -> computeDoNodeGetterBody(name, computeDoNodeGetterReturnType(b.context(), api, returnTypeFunction), b));
  }

  static <API extends IApiSpecification> String computeDoNodeGetterReturnType(IJavaBuilderContext context, Class<API> returnTypeApi, Function<API, String> returnTypeFunction) {
    return new ApiFunction<>(returnTypeApi, returnTypeFunction).apply(context);
  }

  static String computeDoNodeGetterName(IJavaBuilderContext c, CharSequence doNodeName, CharSequence doNodeType) {
    return c.requireApi(IScoutApi.class).IDoEntity().computeGetterPrefixFor(doNodeType) + capitalize(doNodeName);
  }

  static void computeDoNodeGetterBody(CharSequence name, String returnTypeReference, IScoutMethodBodyBuilder<?> builder) {
    if (JavaTypes._boolean.equals(returnTypeReference)) {
      // special body for primitive boolean getters (Scout >= 22 only)
      var scout22DoApi = builder.context()
          .api(IScoutApi.class)
          .flatMap(scoutApi -> scoutApi.api(IScout22DoApi.class));
      if (scout22DoApi.isPresent()) {
        builder.returnClause()
            .append(scout22DoApi.orElseThrow().DoEntity().nvlMethodName())
            .parenthesisOpen()
            .appendFunc(c -> computeDoNodeGetterName(c, name, JavaTypes.Boolean)).parenthesisOpen().parenthesisClose()
            .parenthesisClose().semicolon();
        return;
      }
    }
    builder.returnClause().appendDoNodeGet(name).semicolon();
  }

  static String buildReturnTypeReferenceFor(IType owner) {
    var ref = owner.reference();
    if (!owner.hasTypeParameters()) {
      return ref;
    }
    return owner.typeParameters()
        .map(IJavaElement::elementName)
        .collect(joining(", ", ref + JavaTypes.C_GENERIC_START, JavaTypes.C_GENERIC_END + ""));
  }

  /**
   * Creates the convenience methods for a data object node having the properties given.
   * 
   * @param name
   *          The name of the node. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the node. Must not be {@code null}.
   * @param dataTypeRef
   *          The datatype of the DoNode. If the kind is {@link DataObjectNodeKind#LIST}, {@link DataObjectNodeKind#SET}
   *          or {@link DataObjectNodeKind#COLLECTION} the datatype always describes the type of element without the
   *          collection type itself. Must not be {@code null} or blank.
   * @param isInherited
   *          {@code true} if the node is inherited from a super class (see {@link DataObjectNode#isInherited()}).
   * @param declaringType
   *          The {@link ITypeGenerator} in which the node exists. Must not be {@code null}.
   * @param context
   *          The {@link IJavaBuilderContext} for which the convenience methods should be generated.
   * @return The convenience {@link IScoutMethodGenerator method generators} for a data object node with given
   *         properties.
   */
  public static Stream<IScoutMethodGenerator<?, ?>> createConvenienceMethods(String name, DataObjectNodeKind kind, String dataTypeRef, boolean isInherited,
      ITypeGenerator<?> declaringType, IJavaBuilderContext context) {
    var declaringClassFqn = declaringType.fullyQualifiedName();
    var hierarchyType = declaringType.getHierarchyType(context);
    return createConvenienceMethods(name, kind, dataTypeRef, isInherited, hierarchyType)
        .peek(m -> {
          // for chained setters: replace the return type from the name of the synthetic temporary class to the one of the generator given 
          if (m.elementName(context).orElse("").startsWith(PropertyBean.CHAINED_SETTER_PREFIX)) {
            m.withReturnType(declaringClassFqn);
          }
        });
  }

  /**
   * Creates the convenience methods for a data object node having the properties given.
   * 
   * @param name
   *          The name of the node. Must not be {@code null} or blank.
   * @param kind
   *          The {@link DataObjectNodeKind} of the node. Must not be {@code null}.
   * @param dataTypeRef
   *          The datatype of the DoNode. If the kind is {@link DataObjectNodeKind#LIST}, {@link DataObjectNodeKind#SET}
   *          or {@link DataObjectNodeKind#COLLECTION} the datatype always describes the type of element without the
   *          collection type itself. Must not be {@code null} or blank.
   * @param isInherited
   *          {@code true} if the node is inherited from a super class (see {@link DataObjectNode#isInherited()}).
   * @param declaringType
   *          The {@link IType} in which the node exists. Must not be {@code null}.
   * @return The convenience {@link IScoutMethodGenerator method generators} for a data object node with given
   *         properties.
   */
  public static Stream<IScoutMethodGenerator<?, ?>> createConvenienceMethods(String name, DataObjectNodeKind kind, String dataTypeRef, boolean isInherited, IType declaringType) {
    if (kind == DataObjectNodeKind.VALUE) {
      return buildMethodGeneratorsForValue(name, dataTypeRef, isInherited, declaringType);
    }
    return buildMethodGeneratorsForCollection(name, kind, dataTypeRef, isInherited, declaringType);
  }

  static Stream<IScoutMethodGenerator<?, ?>> buildMethodGeneratorsForValue(String name, String dataTypeRef, boolean isInherited, IType owner) {
    var chainedSetter = createDoValueSetter(name, dataTypeRef, owner);
    if (isInherited) {
      // for inherited nodes: only overwrite (and narrow) the chained setter
      return Stream.of(chainedSetter);
    }

    var valueGetter = createDoNodeGetter(name, dataTypeRef, owner);
    var additionalGetters = owner.javaEnvironment().requireApi(IScoutApi.class)
        .IDoEntity().getAdditionalDoNodeGetters(name, dataTypeRef, owner);
    var allMissingGetters = Stream.concat(Stream.of(valueGetter), additionalGetters)
        .filter(gen -> !implementedInSuperClass(gen, owner)); // skip getters already existing in the super class. no need to override
    return Stream.concat(Stream.of(chainedSetter), allMissingGetters);
  }

  static Stream<IScoutMethodGenerator<?, ?>> buildMethodGeneratorsForCollection(String name, DataObjectNodeKind kind, String dataTypeRef, boolean isInherited, IType owner) {
    var chainedSetterCollection = createDoCollectionSetterCollection(name, dataTypeRef, owner);
    var chainedSetterArray = createDoCollectionSetterVarargs(name, dataTypeRef, owner);
    if (isInherited) {
      // for inherited nodes: only overwrite (and narrow) the chained setter
      return Stream.of(chainedSetterCollection, chainedSetterArray);
    }

    var getterCollectionFqn = switch (kind) {
      case LIST -> List.class.getName();
      case SET -> Set.class.getName();
      case COLLECTION -> Collection.class.getName();
      default -> throw newFail("Unsupported DoNode kind of '{}'.", name);
    };

    var collectionGetterReturnTypeReference = getterCollectionFqn + JavaTypes.C_GENERIC_START + dataTypeRef + JavaTypes.C_GENERIC_END;
    var collectionGetter = createDoNodeGetter(name, collectionGetterReturnTypeReference, owner);
    if (implementedInSuperClass(collectionGetter, owner)) {
      // the method already exists in the super class. no need to override the getter
      return Stream.of(chainedSetterCollection, chainedSetterArray);
    }
    return Stream.of(chainedSetterCollection, chainedSetterArray, collectionGetter);
  }

  static boolean implementedInSuperClass(IMethodGenerator<?, ?> method, IType owner) {
    var methodId = method.identifier(new JavaBuilderContext(owner.javaEnvironment()));
    return owner
        .superTypes()
        .withSelf(false)
        .stream()
        .flatMap(t -> t.methods().withMethodIdentifier(methodId).stream())
        .anyMatch(m -> !Flags.isAbstract(m.flags()) || Flags.isDefaultMethod(m.flags()));
  }

  /**
   * The complete convenience methods marker comment
   * 
   * @param nl
   *          The new line delimiter to use
   * @return The raw comment source.
   */
  public static String convenienceMethodsMarkerComment(String nl) {
    return nl
        + CONVENIENCE_METHOD_MARKER_START + "********************************************************" + nl
        + "   * GENERATED CONVENIENCE METHODS" + nl
        + "   * *************************************************************************/"
        + nl;
  }
}
