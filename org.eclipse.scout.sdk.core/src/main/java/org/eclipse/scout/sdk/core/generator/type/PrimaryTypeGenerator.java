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
import java.util.stream.Stream;

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
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;

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

    ICompilationUnitGenerator<?> cu = CompilationUnitGenerator.create()
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
  protected TYPE currentInstance() {
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
    return currentInstance();
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
    return currentInstance();
  }

  @Override
  public TYPE withFlags(int flags) {
    primary().withFlags(flags);
    return currentInstance();
  }

  @Override
  public int flags() {
    return primary().flags();
  }

  @Override
  public TYPE withoutFlags(int flags) {
    primary().withoutFlags(flags);
    return currentInstance();
  }

  @Override
  public TYPE asPublic() {
    primary().asPublic();
    return currentInstance();
  }

  @Override
  public TYPE asAbstract() {
    primary().asAbstract();
    return currentInstance();
  }

  @Override
  public TYPE asPrivate() {
    primary().asPrivate();
    return currentInstance();
  }

  @Override
  public TYPE asPackagePrivate() {
    primary().asPackagePrivate();
    return currentInstance();
  }

  @Override
  public TYPE asProtected() {
    primary().asProtected();
    return currentInstance();
  }

  @Override
  public TYPE asStatic() {
    primary().asStatic();
    return currentInstance();
  }

  @Override
  public TYPE asFinal() {
    primary().asFinal();
    return currentInstance();
  }

  @Override
  public TYPE withAnnotation(IAnnotationGenerator<?> builder) {
    primary().withAnnotation(builder);
    return currentInstance();
  }

  @Override
  public TYPE withoutAnnotation(String annotationFqn) {
    primary().withoutAnnotation(annotationFqn);
    return currentInstance();
  }

  @Override
  public Stream<IAnnotationGenerator<?>> annotations() {
    return primary().annotations();
  }

  @Override
  public TYPE clearAnnotations() {
    primary().clearAnnotations();
    return currentInstance();
  }

  @Override
  public TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentBuilder) {
    primary().withComment(commentBuilder);
    return currentInstance();
  }

  @Override
  public Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment() {
    return primary().comment();
  }

  @Override
  public TYPE withElementName(String newName) {
    primary().withElementName(newName);
    m_compilationUnit.withElementName(newName);
    return currentInstance();
  }

  @Override
  public Optional<String> elementName() {
    return primary().elementName();
  }

  @Override
  public Stream<String> interfaces() {
    return primary().interfaces();
  }

  @Override
  public TYPE withInterface(String interfaceReference) {
    primary().withInterface(interfaceReference);
    return currentInstance();
  }

  @Override
  public TYPE withInterfaces(Stream<String> interfaceReferences) {
    primary().withInterfaces(interfaceReferences);
    return currentInstance();
  }

  @Override
  public TYPE withoutInterface(String interfaceReference) {
    primary().withoutInterface(interfaceReference);
    return currentInstance();
  }

  @Override
  public Optional<String> superClass() {
    return primary().superClass();
  }

  @Override
  public TYPE withSuperClass(String superType) {
    primary().withSuperClass(superType);
    return currentInstance();
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
    return currentInstance();
  }

  @Override
  public TYPE withoutField(String elementName) {
    primary().withoutField(elementName);
    return currentInstance();
  }

  @Override
  public Stream<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methods() {
    return primary().methods();
  }

  @Override
  public TYPE withMethod(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> builder, Object... sortObject) {
    primary().withMethod(builder, sortObject);
    return currentInstance();
  }

  @Override
  public TYPE withoutMethod(String methodName) {
    primary().withoutMethod(methodName);
    return currentInstance();
  }

  @Override
  public Optional<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> method(String methodId) {
    return primary().method(methodId);
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
    return currentInstance();
  }

  @Override
  public TYPE withoutType(String elementName) {
    primary().withoutType(elementName);
    return currentInstance();
  }

  @Override
  public Stream<ITypeParameterGenerator<?>> typeParameters() {
    return primary().typeParameters();
  }

  @Override
  public TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter) {
    primary().withTypeParameter(typeParameter);
    return currentInstance();
  }

  @Override
  public TYPE withoutTypeParameter(String elementName) {
    primary().withoutTypeParameter(elementName);
    return currentInstance();
  }

  @Override
  public TYPE asInterface() {
    primary().asInterface();
    return currentInstance();
  }

  @Override
  public TYPE asAnnotationType() {
    primary().asAnnotationType();
    return currentInstance();
  }

  @Override
  public TYPE asEnum() {
    primary().asEnum();
    return currentInstance();
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
    return currentInstance();
  }

  @Override
  public TYPE withoutAllMethodsImplemented() {
    primary().withoutAllMethodsImplemented();
    return currentInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented() {
    primary().withAllMethodsImplemented();
    return currentInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded) {
    primary().withAllMethodsImplemented(callbackForMethodsAdded);
    return currentInstance();
  }

  @Override
  public boolean isWithAllMethodsImplemented() {
    return primary().isWithAllMethodsImplemented();
  }

  @Override
  public TYPE withImport(CharSequence name) {
    m_compilationUnit.withImport(name);
    return currentInstance();
  }

  @Override
  public TYPE withoutImport(CharSequence name) {
    m_compilationUnit.withoutImport(name);
    return currentInstance();
  }

  @Override
  public Stream<CharSequence> imports() {
    return m_compilationUnit.imports();
  }

  @Override
  public TYPE withStaticImport(CharSequence name) {
    m_compilationUnit.withStaticImport(name);
    return currentInstance();
  }

  @Override
  public TYPE withoutStaticImport(CharSequence name) {
    m_compilationUnit.withoutStaticImport(name);
    return currentInstance();
  }

  @Override
  public Stream<CharSequence> staticImports() {
    return m_compilationUnit.staticImports();
  }

  @Override
  public TYPE withoutAllImports() {
    m_compilationUnit.withoutAllImports();
    return currentInstance();
  }

  @Override
  public TYPE withFooter(ISourceGenerator<ICommentBuilder<?>> builder) {
    m_compilationUnit.withFooter(builder);
    return currentInstance();
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
