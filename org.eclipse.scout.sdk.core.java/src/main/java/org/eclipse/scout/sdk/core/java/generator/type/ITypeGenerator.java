/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.type;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;

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
  Stream<JavaBuilderContextFunction<String>> interfacesFunc();

  /**
   * @return A {@link Stream} with all interface references this {@link ITypeGenerator} implements and can be computed
   *         without context.
   */
  Stream<String> interfaces();

  /**
   * Adds the specified interface reference to this type.
   * <p>
   * If the type is an interface itself it will {@code extend} the specified reference. Otherwise, it will
   * {@code implement} the interface.
   *
   * @param interfaceReference
   *          The interface reference to add. E.g. "{@code java.util.List<? extends java.lang.CharSequence>}". In case
   *          the reference is blank or {@code null} this method does nothing.
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
   *          A {@link Function} to be called to obtain the interface type to add to this {@link ITypeGenerator}. Must
   *          not be {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withInterface(String)
   * @see #withInterfaces(Stream)
   */
  <A extends IApiSpecification> TYPE withInterfaceFrom(Class<A> apiDefinition, Function<A, String> interfaceSupplier);

  /**
   * Adds the result of the interfaceSupplier to the list of implemented interfaces.
   * <p>
   * This method may be handy in case the interface reference is context dependent.
   * </p>
   * 
   * @param interfaceSupplier
   *          A {@link Function} to be called to obtain the interface type to add to this {@link ITypeGenerator}. This
   *          method does nothing in case the supplier is {@code null}.
   * @return This generator.
   * @see #withInterface(String)
   * @see #withInterfaces(Stream)
   */
  TYPE withInterfaceFunc(Function<IJavaBuilderContext, String> interfaceSupplier);

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
   * Removes the given interface reference from this {@link ITypeGenerator}. Only context independent references can be
   * removed with this function.
   * 
   * @param toRemove
   *          The interface reference to remove.
   * @return This generator.
   */
  TYPE withoutInterface(String toRemove);

  /**
   * Removes all interface types for which the given {@link Predicate} returns {@code true}.
   * 
   * @param filter
   *          A {@link Predicate} that decides if an interface should be removed. May be {@code null}. In that case all
   *          interfaces are removed.
   * @return This generator.
   */
  TYPE withoutInterface(Predicate<JavaBuilderContextFunction<String>> filter);

  /**
   * @return The super class reference in case it can be computed without context.
   */
  Optional<String> superClass();

  /**
   * @param context
   *          The context for which the super class should be computed.
   * @return The super class reference or an empty {@link Optional} if this {@link ITypeGenerator} has no super class.
   */
  Optional<String> superClass(IJavaBuilderContext context);

  /**
   * @return The super class function of this {@link ITypeGenerator} or an empty {@link Optional} if it has none.
   */
  Optional<JavaBuilderContextFunction<String>> superClassFunc();

  /**
   * Sets the super class reference of this {@link ITypeGenerator}.
   *
   * @param superType
   *          The super class reference or {@code null} if this {@link ITypeGenerator} should not have a super class.
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
   *          {@link ITypeGenerator}. Must not be {@code null}.
   * @param <A>
   *          The API type that contains the class name
   * @return This generator.
   * @see #withSuperClass(String)
   */
  <A extends IApiSpecification> TYPE withSuperClassFrom(Class<A> apiDefinition, Function<A, String> superClassSupplier);

  /**
   * Sets the result of the superClassSupplier as super class reference.
   * <p>
   * This method may be handy if the super class type reference is context dependent.
   * </p>
   * 
   * @param superClassSupplier
   *          A {@link Function} to be called to obtain the super class type reference to set to this
   *          {@link ITypeGenerator} or {@code null} if it should not have a super class.
   * @return This generator.
   * @see #withSuperClass(String)
   */
  TYPE withSuperClassFunc(Function<IJavaBuilderContext, String> superClassSupplier);

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
   * Removes all {@link IMethodGenerator IMethodGenerators} having the
   * {@link IMethodGenerator#identifier(IJavaBuilderContext) identifier} given.
   * 
   * @param identifier
   *          The identifier of the methods to remove
   * @param context
   *          The {@link IJavaBuilderContext} to compute the identifiers of the methods currently registered in this
   *          {@link ITypeGenerator}. May be {@code null} if the methods ids can be computed without context.
   * @return This generator.
   */
  TYPE withoutMethod(String identifier, IJavaBuilderContext context);

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
   * @see IMethodGenerator#identifier(IJavaBuilderContext, boolean)
   * @see IMethod#identifier()
   * @see JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)
   */
  Optional<IMethodGenerator<?, ?>> method(String methodId, IJavaBuilderContext context, boolean includeTypeArguments);

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
   * Removes all nested {@link ITypeGenerator} instances having the given simple name (requires that the simple names
   * can be computed without context)
   * 
   * @param simpleName
   *          The simple name of the type to remove.
   * @return This generator.
   */
  TYPE withoutType(String simpleName);

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
   * Gets a synthetic created {@link IType} having the same super type hierarchy as this {@link ITypeGenerator}.<br>
   * The resulting {@link IType} uses a generated name and an empty body.<br>
   * This method may be handy if the super hierarchy of the resulting type of this {@link ITypeGenerator} must be
   * analyzed (including all resolved type arguments) before the type is actually created.
   * 
   * @param context
   *          The {@link IJavaBuilderContext} for which the hierarchy type should be returned.
   * @return A synthetic {@link IType} having the same super hierarchy as this {@link ITypeGenerator} including all type
   *         arguments.
   * @throws IllegalArgumentException
   *           If the {@link IJavaBuilderContext} passed has no {@link IJavaEnvironment} associated (see
   *           {@link IJavaBuilderContext#environment()}).
   * @throws NullPointerException
   *           If the {@link IJavaBuilderContext} passed is {@code null}.
   */
  IType getHierarchyType(IJavaBuilderContext context);

  /**
   * Instructs this {@link ITypeGenerator} to automatically add all missing methods that are required by a super type
   * (abstract method or interface method).
   * <p>
   * <b>Note:</b> Methods can only be added if the generation is running with an {@link IJavaEnvironment} (see
   * {@link IJavaBuilderContext#environment()}). Otherwise, an {@link IllegalArgumentException} is thrown.
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
   * {@link IJavaBuilderContext#environment()}). Otherwise, an {@link IllegalArgumentException} is thrown.
   * <p>
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
   * Instructs this {@link ITypeGenerator} to automatically add all missing methods that are required by a super type
   * (abstract method or interface method).
   * <p>
   * <b>Note:</b> Methods can only be added if the generation is running with an {@link IJavaEnvironment} (see
   * {@link IJavaBuilderContext#environment()}). Otherwise, an {@link IllegalArgumentException} is thrown.
   * <p>
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
   * @param methodSortOrderProvider
   *          Optional {@link Function} called for each unimplemented method which is automatically added. The
   *          {@link Function} gets the {@link IMethodGenerator} of the unimplemented method that will be added as input
   *          and returns the desired order for this method (may be {@code null}).
   * @return This generator.
   * @throws IllegalArgumentException
   *           if the generation is running without an {@link IJavaEnvironment}. See
   *           {@link IJavaBuilderContext#environment()}.
   * @see MethodOverrideGenerator#createOverride(IWorkingCopyTransformer)
   * @see ITransformInput#requestDefaultWorkingCopy()
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded, Function<IMethodGenerator<?, ?>, Object[]> methodSortOrderProvider);

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
