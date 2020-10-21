/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.type;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link PrimaryTypeGenerator}</h3>
 * <p>
 * An {@link IJavaElementGenerator} that creates a compilation unit with a single primary type in it.
 *
 * @since 6.1.0
 */
public class PrimaryTypeGenerator<TYPE extends PrimaryTypeGenerator<TYPE>> implements ITypeGenerator<TYPE>, ICompilationUnitGenerator<TYPE> {

  private final ITypeGenerator<?> m_primaryType;
  private final ICompilationUnitGenerator<?> m_compilationUnit;
  private boolean m_filled;

  protected PrimaryTypeGenerator() {
    m_primaryType = TypeGenerator.create()
        .asPublic()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment);
    m_compilationUnit = CompilationUnitGenerator.create()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment);
  }

  /**
   * Creates a {@link PrimaryTypeGenerator} whose primary type and the compilation unit have their default comments
   * attached and the primary type is marked as {@code public}.
   *
   * @return The new {@link PrimaryTypeGenerator}.
   */
  public static PrimaryTypeGenerator<?> create() {
    return new PrimaryTypeGenerator<>();
  }

  /**
   * Callback for child classes to setup the primary type.
   *
   * @param mainType
   *          The primary type. Same as {@link #primary()}. Is never {@code null}.
   */
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    // nop
  }

  @Override
  public void generate(ISourceBuilder<?> builder) {
    if (!isFilled()) {
      fillMainType(primary());
      setFilled();
    }

    var cu = CompilationUnitGenerator.create()
        .withComment(m_compilationUnit.comment().orElse(null))
        .withElementName(m_compilationUnit.elementName().orElseThrow(() -> newFail("Type name is missing.")))
        .withPackage(getPackage().orElse(null))
        .withType(primary());
    m_compilationUnit.imports()
        .forEach(cu::withImport);
    m_compilationUnit.staticImports()
        .forEach(cu::withStaticImport);
    m_compilationUnit.footers()
        .forEach(cu::withFooter);

    cu.generate(builder);
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public Optional<ITypeGenerator<?>> mainType() {
    return Optional.of(primary());
  }

  @Override
  public Optional<String> fileName() {
    return m_compilationUnit.fileName();
  }

  /**
   * @return Content of {@link #mainType()}. Is never {@code null}.
   */
  private ITypeGenerator<? extends ITypeGenerator<?>> primary() {
    return m_primaryType;
  }

  @Override
  public Optional<PackageGenerator> getPackage() {
    return m_compilationUnit.getPackage();
  }

  @Override
  public Optional<String> packageName() {
    return m_compilationUnit.packageName();
  }

  @Override
  public TYPE withPackageName(String newPackage) {
    m_compilationUnit.withPackageName(newPackage);
    setDeclaringFullyQualifiedName(newPackage);
    return thisInstance();
  }

  @Override
  public TYPE withPackage(PackageGenerator generator) {
    m_compilationUnit.withPackage(generator);
    if (generator == null) {
      setDeclaringFullyQualifiedName(null);
    }
    else {
      setDeclaringFullyQualifiedName(generator.elementName().orElse(null));
    }
    return thisInstance();
  }

  @Override
  public TYPE withFlags(int flags) {
    primary().withFlags(flags);
    return thisInstance();
  }

  @Override
  public int flags() {
    return primary().flags();
  }

  @Override
  public TYPE withoutFlags(int flags) {
    primary().withoutFlags(flags);
    return thisInstance();
  }

  @Override
  public TYPE asPublic() {
    primary().asPublic();
    return thisInstance();
  }

  @Override
  public TYPE asAbstract() {
    primary().asAbstract();
    return thisInstance();
  }

  @Override
  public TYPE asPrivate() {
    primary().asPrivate();
    return thisInstance();
  }

  @Override
  public TYPE asPackagePrivate() {
    primary().asPackagePrivate();
    return thisInstance();
  }

  @Override
  public TYPE asProtected() {
    primary().asProtected();
    return thisInstance();
  }

  @Override
  public TYPE asStatic() {
    primary().asStatic();
    return thisInstance();
  }

  @Override
  public TYPE asFinal() {
    primary().asFinal();
    return thisInstance();
  }

  @Override
  public TYPE withAnnotation(IAnnotationGenerator<?> builder) {
    primary().withAnnotation(builder);
    return thisInstance();
  }

  @Override
  public TYPE withoutAnnotation(Predicate<IAnnotationGenerator<?>> removalFilter) {
    primary().withoutAnnotation(removalFilter);
    return thisInstance();
  }

  @Override
  public Stream<IAnnotationGenerator<?>> annotations() {
    return primary().annotations();
  }

  @Override
  public Optional<IAnnotationGenerator<?>> annotation(String annotationFqn) {
    return primary().annotation(annotationFqn);
  }

  @Override
  public TYPE clearAnnotations() {
    primary().clearAnnotations();
    return thisInstance();
  }

  @Override
  public TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentBuilder) {
    primary().withComment(commentBuilder);
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment() {
    return primary().comment();
  }

  @Override
  public TYPE withElementName(String newName) {
    primary().withElementName(newName);
    m_compilationUnit.withElementName(newName);
    return thisInstance();
  }

  @Override
  public Optional<String> elementName() {
    return primary().elementName();
  }

  @Override
  public Stream<ApiFunction<?, String>> interfaces() {
    return primary().interfaces();
  }

  @Override
  public TYPE withInterface(String interfaceReference) {
    primary().withInterface(interfaceReference);
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withInterfaceFrom(Class<A> apiDefinition, Function<A, String> interfaceSupplier) {
    primary().withInterfaceFrom(apiDefinition, interfaceSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withInterfaces(Stream<String> interfaceReferences) {
    primary().withInterfaces(interfaceReferences);
    return thisInstance();
  }

  @Override
  public TYPE withoutInterface(Predicate<ApiFunction<?, String>> filter) {
    primary().withoutInterface(filter);
    return thisInstance();
  }

  @Override
  public Optional<ApiFunction<?, String>> superClass() {
    return primary().superClass();
  }

  @Override
  public <A extends IApiSpecification> TYPE withSuperClassFrom(Class<A> apiDefinition, Function<A, String> superClassSupplier) {
    primary().withSuperClassFrom(apiDefinition, superClassSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withSuperClass(String superType) {
    primary().withSuperClass(superType);
    return thisInstance();
  }

  @Override
  public String fullyQualifiedName() {
    return primary().fullyQualifiedName();
  }

  @Override
  public String qualifier() {
    return primary().qualifier();
  }

  @Override
  public Stream<IFieldGenerator<?>> fields() {
    return primary().fields();
  }

  @Override
  public TYPE withField(IFieldGenerator<?> builder, Object... sortObject) {
    primary().withField(builder, sortObject);
    return thisInstance();
  }

  @Override
  public TYPE withoutField(Predicate<IFieldGenerator<?>> removalFilter) {
    primary().withoutField(removalFilter);
    return thisInstance();
  }

  @Override
  public Stream<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methods() {
    return primary().methods();
  }

  @Override
  public TYPE withMethod(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> builder, Object... sortObject) {
    primary().withMethod(builder, sortObject);
    return thisInstance();
  }

  @Override
  public TYPE withoutMethod(Predicate<IMethodGenerator<?, ?>> removalFilter) {
    primary().withoutMethod(removalFilter);
    return thisInstance();
  }

  @Override
  public Optional<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> method(String methodId, IJavaEnvironment context, boolean useErasureOnly) {
    return primary().method(methodId, context, useErasureOnly);
  }

  @Override
  public Stream<ITypeGenerator<?>> types() {
    return primary().types();
  }

  @Override
  public Optional<ITypeGenerator<?>> type(String simpleName) {
    return primary().type(simpleName);
  }

  @Override
  public TYPE withType(ITypeGenerator<?> generator, Object... sortObject) {
    primary().withType(generator, sortObject);
    return thisInstance();
  }

  @Override
  public TYPE withoutType(Predicate<ITypeGenerator<?>> removalFilter) {
    primary().withoutType(removalFilter);
    return thisInstance();
  }

  @Override
  public Stream<ITypeParameterGenerator<?>> typeParameters() {
    return primary().typeParameters();
  }

  @Override
  public TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter) {
    primary().withTypeParameter(typeParameter);
    return thisInstance();
  }

  @Override
  public TYPE withoutTypeParameter(String elementName) {
    primary().withoutTypeParameter(elementName);
    return thisInstance();
  }

  @Override
  public TYPE asInterface() {
    primary().asInterface();
    return thisInstance();
  }

  @Override
  public TYPE asAnnotationType() {
    primary().asAnnotationType();
    return thisInstance();
  }

  @Override
  public TYPE asEnum() {
    primary().asEnum();
    return thisInstance();
  }

  @Override
  public Optional<IJavaElementGenerator<?>> declaringGenerator() {
    return primary().declaringGenerator();
  }

  @Override
  public Optional<String> getDeclaringFullyQualifiedName() {
    return primary().getDeclaringFullyQualifiedName();
  }

  @Override
  public TYPE setDeclaringFullyQualifiedName(String parentFullyQualifiedName) {
    primary().setDeclaringFullyQualifiedName(parentFullyQualifiedName);
    return thisInstance();
  }

  @Override
  public TYPE withoutAllMethodsImplemented() {
    primary().withoutAllMethodsImplemented();
    return thisInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented() {
    primary().withAllMethodsImplemented();
    return thisInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded) {
    primary().withAllMethodsImplemented(callbackForMethodsAdded);
    return thisInstance();
  }

  @Override
  public boolean isWithAllMethodsImplemented() {
    return primary().isWithAllMethodsImplemented();
  }

  @Override
  public TYPE withImport(CharSequence name) {
    m_compilationUnit.withImport(name);
    return thisInstance();
  }

  @Override
  public TYPE withoutImport(CharSequence name) {
    m_compilationUnit.withoutImport(name);
    return thisInstance();
  }

  @Override
  public Stream<CharSequence> imports() {
    return m_compilationUnit.imports();
  }

  @Override
  public TYPE withStaticImport(CharSequence name) {
    m_compilationUnit.withStaticImport(name);
    return thisInstance();
  }

  @Override
  public TYPE withoutStaticImport(CharSequence name) {
    m_compilationUnit.withoutStaticImport(name);
    return thisInstance();
  }

  @Override
  public Stream<CharSequence> staticImports() {
    return m_compilationUnit.staticImports();
  }

  @Override
  public TYPE withoutAllImports() {
    m_compilationUnit.withoutAllImports();
    return thisInstance();
  }

  @Override
  public TYPE withFooter(ISourceGenerator<ICommentBuilder<?>> builder) {
    m_compilationUnit.withFooter(builder);
    return thisInstance();
  }

  @Override
  public Stream<ISourceGenerator<ICommentBuilder<?>>> footers() {
    return m_compilationUnit.footers();
  }

  private boolean isFilled() {
    return m_filled;
  }

  private void setFilled() {
    m_filled = true;
  }
}
