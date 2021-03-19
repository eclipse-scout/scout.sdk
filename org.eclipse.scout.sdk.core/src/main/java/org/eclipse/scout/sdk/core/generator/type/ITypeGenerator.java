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
package org.eclipse.scout.sdk.core.generator.type;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ITypeGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates classes, enums, interfaces or annotations.
 *
 * @since 6.1.0
 */
public interface ITypeGenerator<TYPE extends ITypeGenerator<TYPE>> extends IMemberGenerator<TYPE> {

  /**
   * @return A {@link Stream} with all interface types this {@link ITypeGenerator} implements.
   */
  Stream<ApiFunction<?, String>> interfaces();

  /**
   * Adds the specified interface reference to this type.
   * <p>
   * If the type is an interface itself it will {@code extend} the specified reference. Otherwise it will
   * {@code implement} the interface.
   *
   * @param interfaceReference
   *          The interface reference to add. Must not be blank (see {@link Strings#isBlank(CharSequence)}). E.g.
   *          {@code java.util.List<? extends java.lang.CharSequence>}.
   * @return This generator.
   * @see #withInterfaceFrom(Class, Function)
   * @see #withInterfaces(Stream)
   */
  TYPE withInterface(String interfaceReference);

  /**
   * Adds the result of the interfaceSupplier to the list of implemented interfaces.
   * <p>
   * This method may be handy if the interface type changes between different versions of an API. The builder then
   * decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code typeGenerator.withInterfaceFrom(IJavaApi.class, IJavaApi::Serializable)}.
   * 
   * @param apiDefinition
   *          The api type that defines the interface type. An instance of this API is passed to the interfaceSupplier.
   *          May be {@code null} in case the given interfaceSupplier can handle a {@code null} input.
   * @param interfaceSupplier
   *          A {@link Function} to be called to obtain the interface type to add to this {@link ITypeGenerator}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withInterface(String)
   * @see #withInterfaces(Stream)
   */
  <A extends IApiSpecification> TYPE withInterfaceFrom(Class<A> apiDefinition, Function<A, String> interfaceSupplier);

  /**
   * Adds all interface references of the specified {@link Stream} to the interfaces of this {@link ITypeGenerator}.
   *
   * @param interfaceReferences
   *          A {@link Stream} returning the interface references to add. Must not be {@code null}. The {@link Stream}
   *          must not contain any blank interface references.
   * @return This generator.
   * @see #withInterface(String)
   */
  TYPE withInterfaces(Stream<String> interfaceReferences);

  /**
   * Removes all interface types for which the given {@link Predicate} returns {@code true}.
   * 
   * @param filter
   *          A {@link Predicate} that decides if an interface should be removed. May be {@code null}. In that case all
   *          interfaces are removed.
   * @return This generator.
   */
  TYPE withoutInterface(Predicate<ApiFunction<?, String>> filter);

  /**
   * @return The super class of this {@link ITypeGenerator} or an empty {@link Optional} if has none.
   */
  Optional<ApiFunction<?, String>> superClass();

  /**
   * Sets the super class of this {@link ITypeGenerator}.
   *
   * @param superType
   *          The super class reference.
   * @return This generator.
   * @see #withSuperClassFrom(Class, Function)
   */
  TYPE withSuperClass(String superType);

  /**
   * Sets the result of the superClassSupplier as super class reference.
   * <p>
   * This method may be handy if the super class type reference changes between different versions of an API. The
   * builder then decides which API to use based on the version found in the {@link IJavaEnvironment} of the
   * {@link IJavaBuilderContext}.
   * </p>
   * <b>Example:</b> {@code typeGenerator.withSuperClassFrom(IJavaApi.class, IJavaApi::AbstractList)}.
   * 
   * @param apiDefinition
   *          The api type that defines the super class type. An instance of this API is passed to the
   *          superClassSupplier. May be {@code null} in case the given superClassSupplier can handle a {@code null}
   *          input.
   * @param superClassSupplier
   *          A {@link Function} to be called to obtain the super class type reference to set to this
   *          {@link ITypeGenerator}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withSuperClass(String)
   */
  <A extends IApiSpecification> TYPE withSuperClassFrom(Class<A> apiDefinition, Function<A, String> superClassSupplier);

  /**
   * Builds the fully qualified name this type will have when generated.
   * <p>
   * The name can only be computed if the name of this {@link ITypeGenerator} is set and at least one of the following
   * conditions applies:
   * <ul>
   * <li>This {@link ITypeGenerator} is assigned to a declaring {@link ITypeGenerator} which itself is fully connected
   * as well.</li>
   * <li>This {@link ITypeGenerator} is assigned to a declaring {@link ICompilationUnitGenerator}.</li>
   * <li>The qualifier of the declaring element (a type or compilation unit) has been explicitly set using
   * {@link #setDeclaringFullyQualifiedName(String)}.</li>
   * </ul>
   *
   * @return The fully qualified name with the $ sign as member type delimiter. E.g.
   *         {@code org.eclipse.scout.PrimaryClass$DeclaringClass$ThisClass}.
   * @throws IllegalArgumentException
   *           If it cannot be computed because no declaring context is available.
   */
  String fullyQualifiedName();

  /**
   * Builds the qualifier this type will have when generated.
   * <p>
   * The qualifier can only be computed if at least one of the following conditions applies:
   * <ul>
   * <li>This {@link ITypeGenerator} is assigned to a declaring {@link ITypeGenerator} which itself is fully connected
   * as well.</li>
   * <li>This {@link ITypeGenerator} is assigned to a declaring {@link ICompilationUnitGenerator}.</li>
   * <li>The qualifier of the declaring element (a type or compilation unit) has been explicitly set using
   * {@link #setDeclaringFullyQualifiedName(String)}.</li>
   * </ul>
   *
   * @return The qualifier with the $ sign as member type delimiter. E.g.
   *         {@code org.eclipse.scout.PrimaryClass$DeclaringClass}.
   * @throws IllegalArgumentException
   *           If it cannot be computed because no declaring context is available.
   */
  String qualifier();

  /**
   * @return A {@link Stream} returning all {@link IFieldGenerator}s of this {@link ITypeGenerator}.
   */
  Stream<IFieldGenerator<?>> fields();

  /**
   * Adds the specified field to this {@link ITypeGenerator}.
   *
   * @param generator
   *          The {@link IFieldGenerator} to add. Must not be {@code null}.
   * @param sortObject
   *          Optional elements used to define the position of the {@link IFieldGenerator} within this
   *          {@link ITypeGenerator}. May be {@code null} or omitted (in that case a default position is calculated).
   *          The generators are sorted according to the natural order of the elements specified.
   * @return This generator.
   * @see FieldGenerator#create()
   */
  TYPE withField(IFieldGenerator<?> generator, Object... sortObject);

  /**
   * Removes all {@link IFieldGenerator IFieldGenerators} for which the specified {@link Predicate} returns
   * {@code true}.
   *
   * @param removalFilter
   *          A {@link Predicate} that decides if a field should be removed. May be {@code null}. In that case all
   *          fields are removed.
   * @return This generator.
   */
  TYPE withoutField(Predicate<IFieldGenerator<?>> removalFilter);

  /**
   * @return A {@link Stream} returning all {@link IMethodGenerator}s in this {@link ITypeGenerator}.
   */
  Stream<IMethodGenerator<?, ?>> methods();

  /**
   * Adds the specified method to this {@link ITypeGenerator}.
   *
   * @param generator
   *          The {@link IMethodGenerator} to add. Must not be {@code null}.
   * @param sortObject
   *          Optional elements used to define the position of the {@link IMethodGenerator} within this
   *          {@link ITypeGenerator}. May be {@code null} or omitted (in that case a default position is calculated).
   *          The generators are sorted according to the natural order of the elements specified.
   * @return This generator.
   * @see MethodGenerator#create()
   * @see MethodGenerator#createGetter(IFieldGenerator)
   * @see MethodGenerator#createSetter(IFieldGenerator)
   * @see MethodOverrideGenerator#create()
   */
  TYPE withMethod(IMethodGenerator<?, ?> generator, Object... sortObject);

  /**
   * Removes all {@link IMethodGenerator IMethodGenerators} for which the specified {@link Predicate} returns
   * {@code true}.
   *
   * @param removalFilter
   *          A {@link Predicate} that decides if a method should be removed. May be {@code null}. In that case all
   *          methods are removed.
   * @return This generator.
   */
  TYPE withoutMethod(Predicate<IMethodGenerator<?, ?>> removalFilter);

  /**
   * Gets the {@link IMethodGenerator} in this {@link ITypeGenerator} having the specified method identifier.
   *
   * @param methodId
   *          The method identifier for which a matching {@link IMethodGenerator} should be searched. Must not be
   *          {@code null}.
   * @param context
   *          The context {@link IJavaEnvironment} for which the identifier should be computed. This is required because
   *          method parameter data types may be API dependent (see
   *          {@link MethodParameterGenerator#withDataTypeFrom(Class, Function)}).
   * @param includeTypeArguments
   *          If {@code false} only the type erasure is used for all method parameter types.
   * @return The {@link IMethodGenerator} with the specified method id or an empty {@link Optional}.
   * @see IMethodGenerator#identifier(IJavaEnvironment, boolean)
   * @see IMethod#identifier()
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   */
  Optional<IMethodGenerator<?, ?>> method(String methodId, IJavaEnvironment context, boolean includeTypeArguments);

  /**
   * @return A {@link Stream} returning all member {@link ITypeGenerator}s in this {@link ITypeGenerator}.
   */
  Stream<ITypeGenerator<?>> types();

  /**
   * Gets the direct member {@link ITypeGenerator} with the specified simple name.
   *
   * @param simpleName
   *          The simple name for which a matching {@link ITypeGenerator} should be searched. Must not be {@code null}.
   * @return The {@link ITypeGenerator} with the specified simple name or an empty {@link Optional}.
   * @see ITypeGenerator#elementName()
   */
  Optional<ITypeGenerator<?>> type(String simpleName);

  /**
   * Adds the specified type to this {@link ITypeGenerator}.
   * <p>
   * The type is added at the specified position in this {@link ITypeGenerator}.
   *
   * @param generator
   *          The {@link ITypeGenerator} to add. Must not be {@code null}.
   * @param sortObject
   *          Optional elements used to define the position of the {@link ITypeGenerator} within this
   *          {@link ITypeGenerator}. May be {@code null} or omitted (in that case a default position is calculated).
   *          The generators are sorted according to the natural order of the elements specified.
   * @return This generator.
   * @see TypeGenerator#create()
   */
  TYPE withType(ITypeGenerator<?> generator, Object... sortObject);

  /**
   * Removes all {@link ITypeGenerator ITypeGenerators} for which the specified {@link Predicate} returns {@code true}.
   *
   * @param removalFilter
   *          A {@link Predicate} that decides if a type should be removed. May be {@code null}. In that case all types
   *          are removed.
   * @return This generator.
   */
  TYPE withoutType(Predicate<ITypeGenerator<?>> removalFilter);

  /**
   * @return A {@link Stream} returning all {@link ITypeParameterGenerator}s of this {@link ITypeGenerator}.
   */
  Stream<ITypeParameterGenerator<?>> typeParameters();

  /**
   * Adds the specified type parameter to this {@link ITypeGenerator}.
   *
   * @param typeParameter
   *          The {@link ITypeParameterGenerator} to add. Must not be {@code null}.
   * @return This generator.
   * @see TypeParameterGenerator#create()
   */
  TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter);

  /**
   * Removes the {@link ITypeParameterGenerator} with the specified name from this {@link ITypeGenerator}.
   *
   * @param elementName
   *          The name of the {@link ITypeParameterGenerator} to remove. Must not be {@code null}.
   * @return This generator.
   */
  TYPE withoutTypeParameter(String elementName);

  /**
   * Marks this {@link ITypeGenerator} to be created as {@code interface}.
   *
   * @return This generator.
   */
  TYPE asInterface();

  /**
   * Marks this {@link ITypeGenerator} to be created as {@code abstract class}.
   *
   * @return This generator.
   */
  TYPE asAbstract();

  /**
   * Marks this {@link ITypeGenerator} to be created as annotation ({@code @interface}).
   *
   * @return This generator.
   */
  TYPE asAnnotationType();

  /**
   * Marks this {@link ITypeGenerator} to be created as {@code enum} type.
   *
   * @return This generator.
   */
  TYPE asEnum();

  /**
   * @return The fully qualified name of the declaring element (type or compilation unit). Member types are separated
   *         using the $ sign.
   * @see #fullyQualifiedName()
   * @see #qualifier()
   */
  Optional<String> getDeclaringFullyQualifiedName();

  /**
   * @param parentFullyQualifiedName
   *          The fully qualified name of the declaring element (type or compilation unit). Member types must be
   *          separated using the $ sign. May be {@code null} if the declaring name is unknown.
   * @return This generator.
   * @see #fullyQualifiedName()
   * @see #qualifier()
   */
  TYPE setDeclaringFullyQualifiedName(String parentFullyQualifiedName);

  /**
   * Instructs this {@link ITypeGenerator} to automatically add all missing methods that are required by a super type
   * (abstract method or interface method).
   * <p>
   * <b>Note:</b> Methods can only be added if the generation is running with an {@link IJavaEnvironment} (see
   * {@link IJavaBuilderContext#environment()}). Otherwise an {@link IllegalArgumentException} is thrown.
   *
   * @return This generator.
   * @see #withAllMethodsImplemented(IWorkingCopyTransformer)
   * @see MethodOverrideGenerator#createOverride()
   */
  TYPE withAllMethodsImplemented();

  /**
   * Instructs this {@link ITypeGenerator} to automatically add all missing methods that are required by a super type
   * (abstract method or interface method).
   * <p>
   * <b>Note:</b> Methods can only be added if the generation is running with an {@link IJavaEnvironment} (see
   * {@link IJavaBuilderContext#environment()}). Otherwise an {@link IllegalArgumentException} is thrown.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param callbackForMethodsAdded
   *          Optional callback that is invoked for each missing method that is generated. May be {@code null}.<br>
   *          The {@link IWorkingCopyTransformer} is called for each {@link IJavaElement} in every method that is
   *          created. The corresponding {@link ITransformInput}s default working copy (see
   *          {@link ITransformInput#requestDefaultWorkingCopy()}) is a {@link IMethodGenerator} with an
   *          {@link Override} annotation and an auto generated body. This may be a super call (if a super method is
   *          available) or an auto generated method stub (see {@link MethodOverrideGenerator#createOverride()} for
   *          details).<br>
   *          The result of the transformation will be the {@link IMethodGenerator} that is used. This may be the
   *          modified input, a completely new one or {@code null}. If the result is {@code null}, this method will not
   *          be generated for this {@link ITypeGenerator} and will be missing in the resulting type.
   * @return This generator.
   * @throws IllegalArgumentException
   *           if the generation is running without an {@link IJavaEnvironment}. See
   *           {@link IJavaBuilderContext#environment()}.
   * @see MethodOverrideGenerator#createOverride(IWorkingCopyTransformer)
   * @see ITransformInput#requestDefaultWorkingCopy()
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded);

  /**
   * Instructs this {@link ITypeGenerator} to not automatically add missing methods (as required by super types). This
   * is the default.
   *
   * @return This generator.
   */
  TYPE withoutAllMethodsImplemented();

  /**
   * @return {@code true} if this {@link ITypeGenerator} will add all missing methods automatically. {@code false}
   *         otherwise.
   * @see #withAllMethodsImplemented(IWorkingCopyTransformer)
   */
  boolean isWithAllMethodsImplemented();
}
