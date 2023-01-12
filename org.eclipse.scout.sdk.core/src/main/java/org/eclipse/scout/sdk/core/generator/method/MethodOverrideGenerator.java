/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.generator.method;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transform;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link MethodOverrideGenerator}</h3>
 * <p>
 * A generator that will override a method in the super hierarchy. This is only available if the generator is running
 * with an {@link IJavaEnvironment} (see {@link IJavaBuilderContext#environment()}).
 *
 * @since 6.1.0
 */
public class MethodOverrideGenerator<TYPE extends IMethodGenerator<TYPE, BODY>, BODY extends IMethodBodyBuilder<?>> extends MethodGenerator<TYPE, BODY> {

  private final IWorkingCopyTransformer m_transformer; // may be null

  /**
   * Creates a new {@link IMethodGenerator} that will override the method (having the same
   * {@link #identifier(IJavaBuilderContext)}) in the super hierarchy of the owning {@link ITypeGenerator}. If there is
   * only one overload for a method it is sufficient to provide the method name.
   * <p>
   * The generated method uses the signature, return type and exceptions from the overridden method. An {@link Override}
   * annotation is added automatically. If the overridden method is annotated, these annotations are NOT automatically
   * copied to the new method! The same is valid for annotations of the method parameters. If no body is specified, an
   * auto generated body is created. This may be a super call (if a super method is available) or an auto generated
   * method stub.
   * <p>
   * <b>Note:</b> Methods can only be overridden if the generation is running with an {@link IJavaEnvironment} (see
   * {@link IJavaBuilderContext#environment()}). Otherwise an {@link IllegalArgumentException} is thrown.
   * <p>
   * <b>Example:</b>
   *
   * <pre>
   * TypeGenerator.create()
   *     .asPublic()
   *     .withElementName("MyClass")
   *     .withSuperClass("MySuperClass") // the methods to override must exist in this hierarchy
   *     .withMethod(MethodOverrideGenerator.createOverride()
   *         .withElementName("toString")
   *         .withBody(...)) // overrides toString with this specific body
   *     .withMethod(MethodOverrideGenerator.createOverride()
   *         .withElementName("methodToOverride")
   *         .withParameter(MethodParameterGenerator.create()
   *             .withDataType(JavaTypes._int))); // overrides methodToOverride(int) and uses a default body.
   * </pre>
   *
   * @return The created {@link IMethodGenerator}.
   * @throws IllegalArgumentException
   *           if no {@link IJavaEnvironment} is available in the builder context (see
   *           {@link IJavaBuilderContext#environment()}).
   */
  public static IMethodGenerator<?, ?> createOverride() {
    return createOverride(null);
  }

  /**
   * Creates a new {@link IMethodGenerator} that will override the method (having the same
   * {@link #identifier(IJavaBuilderContext)}) in the super hierarchy of the owning {@link ITypeGenerator}. If there is
   * only one overload for a method it is sufficient to provide the method name.
   * <p>
   * By default the generated method uses the signature, return type and exceptions from the overridden method. An
   * {@link Override} annotation is added automatically. If the overridden method is annotated, these annotations are
   * NOT automatically copied to the new method! The same is valid for annotations of the method parameters. <br>
   * If no body is specified, an auto generated body is created. This may be a super call (if a super method is
   * available) or an auto generated method stub.<br>
   * This default behavior may be modified by providing an {@link IWorkingCopyTransformer}.
   * <p>
   * <b>Note:</b> Methods can only be overridden if the generation is running with an {@link IJavaEnvironment} (see
   * {@link IJavaBuilderContext#environment()}). Otherwise an {@link IllegalArgumentException} is thrown.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the template
   *          method from the super type to a working copy. May be {@code null} if no custom transformation is required
   *          and the template method should be converted into a working copy with the default configuration.<br>
   *          The {@link ITransformInput} passed to the transformer returns the template {@link IMethod} from the super
   *          type when calling {@link ITransformInput#model()}.<br>
   *          Please note that the transformer is invoked at source generation time once for each generation. It is not
   *          invoked when creating the generator because the template method can only be found at generation time if a
   *          {@link IJavaEnvironment} is available on the {@link IJavaBuilderContext}.
   * @return The created {@link IMethodGenerator}.
   * @throws IllegalArgumentException
   *           if no {@link IJavaEnvironment} is available in the builder context (see
   *           {@link IJavaBuilderContext#environment()}).
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static IMethodGenerator<?, ?> createOverride(IWorkingCopyTransformer transformer) {
    return new MethodOverrideGenerator<>(transformer);
  }

  protected MethodOverrideGenerator(IWorkingCopyTransformer transformer) {
    m_transformer = transformer;
  }

  protected IMethodGenerator<?, ?> createDefaultOverrideGenerator(IMethod template) {
    var isFromInterface = isInterface(template.requireDeclaringType().flags());
    var needsImplementation = isFromInterface || isAbstract(template.flags());

    var innerGenerator =
        template.toWorkingCopy(m_transformer)
            .withoutAllAnnotations() // clear annotations from method
            .withComment(null)
            .withAnnotation(AnnotationGenerator.createOverride())
            .withoutFlags(Flags.AccAbstract | Flags.AccInterface | Flags.AccDefaultMethod);

    // apply body as specified on this generator (overwrites defaults)
    innerGenerator.withBody(b -> b.append(body()
        .orElseGet(() -> createDefaultMethodBody(needsImplementation))
        .generalize(inner -> createMethodBodyBuilder(inner, innerGenerator))));

    // clear annotations of parameters
    innerGenerator.parameters().forEach(IAnnotatableGenerator::withoutAllAnnotations);

    // apply return type (may be narrowed)
    returnTypeFunc().ifPresent(innerGenerator::withReturnTypeFunc);

    if (isFromInterface) {
      innerGenerator.asPublic();
    }
    return innerGenerator;
  }

  protected ISourceGenerator<BODY> createDefaultMethodBody(boolean needsImplementation) {
    return b -> {
      if (needsImplementation) {
        b.appendAutoGenerated();
      }
      else {
        b.appendSuperCall();
      }
    };
  }

  protected Optional<IMethodGenerator<?, ?>> createOverrideGenerator(IMethod template) {
    return transform(m_transformer, template, () -> createDefaultOverrideGenerator(template), (t, i) -> t.transformMethod(i));
  }

  protected Optional<IMethod> findMethodToOverride(IType container, IJavaBuilderContext context) {
    var templateCandidates = container.methods()
        .withSuperTypes(true).stream()
        .filter(m -> m.elementName().equals(elementName(context).orElseThrow(() -> newFail("To override a method at least the method name must be specified."))))
        .collect(toMap(IMethod::identifier, identity(), (a, b) -> a));

    if (templateCandidates.isEmpty()) {
      return Optional.empty();
    }
    if (templateCandidates.size() == 1) {
      return Optional.of(templateCandidates.values().iterator().next());
    }

    return Optional.ofNullable(templateCandidates.get(identifier(context)));
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    var declaring = Ensure.instanceOf(declaringGenerator().orElse(null), ITypeGenerator.class, "Method can only be overridden if existing in a type.");
    createOverrideGenerator(declaring.getHierarchyType(builder.context()), builder.context()).ifPresent(overrideGenerator -> overrideGenerator.generate(builder));
  }

  protected Optional<IMethodGenerator<?, ?>> createOverrideGenerator(IType tmpType, IJavaBuilderContext context) {
    var template = findMethodToOverride(tmpType, context)
        .orElseThrow(() -> newFail("Method '{}' cannot be found in the super hierarchy.", elementName(context).orElse(null)));
    return createOverrideGenerator(template);
  }
}
