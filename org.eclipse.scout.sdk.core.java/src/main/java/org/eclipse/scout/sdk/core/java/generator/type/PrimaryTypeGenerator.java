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

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

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
  private final List<BiConsumer<TYPE, IJavaBuilderContext>> m_buildPreProcessors;
  private boolean m_setupDone;

  protected PrimaryTypeGenerator() {
    m_buildPreProcessors = new ArrayList<>();
    m_primaryType = new TypeGenerator<>()
        .asPublic()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment);
    m_compilationUnit = CompilationUnitGenerator.create()
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withType(m_primaryType);
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

  @Override
  @SuppressWarnings("unchecked")
  public void generate(ISourceBuilder<?> builder) {
    if (!isSetupDone()) {
      setup();
      setSetupDone();
    }

    preProcessors().forEach(c -> c.accept((TYPE) this, AbstractJavaElementGenerator.ensureJavaSourceBuilder(builder).context()));

    var templateCu = compilationUnit();
    var templateType = primaryType();
    templateCu.withoutType(t -> t == templateType); // detach type from template CU
    try {
      var currentCu = CompilationUnitGenerator.create()
          .withComment(templateCu.comment().orElse(null))
          .withElementNameFunc(templateCu.elementNameFunc().orElseThrow(() -> newFail("Type name is missing.")))
          .withPackage(getPackage().orElse(null))
          .withType(templateType); // attach type to temporary CU
      templateCu.imports().forEach(currentCu::withImport);
      templateCu.staticImports().forEach(currentCu::withStaticImport);
      templateCu.footers().forEach(currentCu::withFooter);

      currentCu.generate(builder);
    }
    finally {
      // attach type to template CU again
      templateCu.withType(templateType);
    }
  }

  /**
   * Callback for child classes to set up the template. This method is only called once for each
   * {@link PrimaryTypeGenerator}.
   */
  protected void setup() {
    // nop
  }

  private ITypeGenerator<?> primaryType() {
    return m_primaryType;
  }

  private ICompilationUnitGenerator<?> compilationUnit() {
    return m_compilationUnit;
  }

  @Override
  public Stream<BiConsumer<TYPE, IJavaBuilderContext>> preProcessors() {
    return m_buildPreProcessors.stream();
  }

  @Override
  public TYPE withPreProcessor(BiConsumer<TYPE, IJavaBuilderContext> processor) {
    if (processor != null) {
      m_buildPreProcessors.add(processor);
    }
    return thisInstance();
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public Optional<ITypeGenerator<?>> mainType() {
    return Optional.of(primaryType());
  }

  @Override
  public Optional<String> fileName() {
    return compilationUnit().fileName();
  }

  @Override
  public Optional<PackageGenerator> getPackage() {
    return compilationUnit().getPackage();
  }

  @Override
  public Optional<String> packageName() {
    return compilationUnit().packageName();
  }

  @Override
  public TYPE withPackageName(String newPackage) {
    compilationUnit().withPackageName(newPackage);
    setDeclaringFullyQualifiedName(newPackage);
    return thisInstance();
  }

  @Override
  public TYPE withPackage(PackageGenerator generator) {
    compilationUnit().withPackage(generator);
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
    primaryType().withFlags(flags);
    return thisInstance();
  }

  @Override
  public int flags() {
    return primaryType().flags();
  }

  @Override
  public TYPE withoutFlags(int flags) {
    primaryType().withoutFlags(flags);
    return thisInstance();
  }

  @Override
  public TYPE asPublic() {
    primaryType().asPublic();
    return thisInstance();
  }

  @Override
  public TYPE asAbstract() {
    primaryType().asAbstract();
    return thisInstance();
  }

  @Override
  public TYPE asPrivate() {
    primaryType().asPrivate();
    return thisInstance();
  }

  @Override
  public TYPE asPackagePrivate() {
    primaryType().asPackagePrivate();
    return thisInstance();
  }

  @Override
  public TYPE asProtected() {
    primaryType().asProtected();
    return thisInstance();
  }

  @Override
  public TYPE asStatic() {
    primaryType().asStatic();
    return thisInstance();
  }

  @Override
  public TYPE asFinal() {
    primaryType().asFinal();
    return thisInstance();
  }

  @Override
  public TYPE withAnnotation(IAnnotationGenerator<?> builder) {
    primaryType().withAnnotation(builder);
    return thisInstance();
  }

  @Override
  public TYPE withoutAnnotation(String annotationFqn) {
    primaryType().withoutAnnotation(annotationFqn);
    return thisInstance();
  }

  @Override
  public TYPE withoutAnnotation(Predicate<IAnnotationGenerator<?>> removalFilter) {
    primaryType().withoutAnnotation(removalFilter);
    return thisInstance();
  }

  @Override
  public TYPE withoutAllAnnotations() {
    primaryType().withoutAllAnnotations();
    return thisInstance();
  }

  @Override
  public Stream<IAnnotationGenerator<?>> annotations() {
    return primaryType().annotations();
  }

  @Override
  public Optional<IAnnotationGenerator<?>> annotation(String annotationFqn) {
    return primaryType().annotation(annotationFqn);
  }

  @Override
  public TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentBuilder) {
    primaryType().withComment(commentBuilder);
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment() {
    return primaryType().comment();
  }

  @Override
  public TYPE withElementName(String newName) {
    primaryType().withElementName(newName);
    compilationUnit().withElementName(newName);
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, String> nameSupplier) {
    primaryType().withElementNameFrom(apiDefinition, nameSupplier);
    compilationUnit().withElementNameFrom(apiDefinition, nameSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withElementNameFunc(Function<IJavaBuilderContext, String> nameSupplier) {
    primaryType().withElementNameFunc(nameSupplier);
    compilationUnit().withElementNameFunc(nameSupplier);
    return thisInstance();
  }

  @Override
  public Optional<String> elementName() {
    return primaryType().elementName();
  }

  @Override
  public Optional<String> elementName(IJavaBuilderContext context) {
    return primaryType().elementName(context);
  }

  @Override
  public Optional<JavaBuilderContextFunction<String>> elementNameFunc() {
    return primaryType().elementNameFunc();
  }

  @Override
  public Stream<String> interfaces() {
    return primaryType().interfaces();
  }

  @Override
  public Stream<JavaBuilderContextFunction<String>> interfacesFunc() {
    return primaryType().interfacesFunc();
  }

  @Override
  public TYPE withInterface(String interfaceReference) {
    primaryType().withInterface(interfaceReference);
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withInterfaceFrom(Class<A> apiDefinition, Function<A, String> interfaceSupplier) {
    primaryType().withInterfaceFrom(apiDefinition, interfaceSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withInterfaceFunc(Function<IJavaBuilderContext, String> interfaceSupplier) {
    primaryType().withInterfaceFunc(interfaceSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withInterfaces(Stream<String> interfaceReferences) {
    primaryType().withInterfaces(interfaceReferences);
    return thisInstance();
  }

  @Override
  public TYPE withoutInterface(String toRemove) {
    primaryType().withoutInterface(toRemove);
    return thisInstance();
  }

  @Override
  public TYPE withoutInterface(Predicate<JavaBuilderContextFunction<String>> filter) {
    primaryType().withoutInterface(filter);
    return thisInstance();
  }

  @Override
  public Optional<String> superClass() {
    return primaryType().superClass();
  }

  @Override
  public Optional<String> superClass(IJavaBuilderContext context) {
    return primaryType().superClass(context);
  }

  @Override
  public Optional<JavaBuilderContextFunction<String>> superClassFunc() {
    return primaryType().superClassFunc();
  }

  @Override
  public <A extends IApiSpecification> TYPE withSuperClassFrom(Class<A> apiDefinition, Function<A, String> superClassSupplier) {
    primaryType().withSuperClassFrom(apiDefinition, superClassSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withSuperClassFunc(Function<IJavaBuilderContext, String> superClassSupplier) {
    primaryType().withSuperClassFunc(superClassSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withSuperClass(String superType) {
    primaryType().withSuperClass(superType);
    return thisInstance();
  }

  @Override
  public String fullyQualifiedName() {
    return primaryType().fullyQualifiedName();
  }

  @Override
  public String qualifier() {
    return primaryType().qualifier();
  }

  @Override
  public Stream<IFieldGenerator<?>> fields() {
    return primaryType().fields();
  }

  @Override
  public TYPE withField(IFieldGenerator<?> builder, Object... sortObject) {
    primaryType().withField(builder, sortObject);
    return thisInstance();
  }

  @Override
  public TYPE withoutField(Predicate<IFieldGenerator<?>> removalFilter) {
    primaryType().withoutField(removalFilter);
    return thisInstance();
  }

  @Override
  public Stream<IMethodGenerator<?, ?>> methods() {
    return primaryType().methods();
  }

  @Override
  public TYPE withMethod(IMethodGenerator<?, ?> builder, Object... sortObject) {
    primaryType().withMethod(builder, sortObject);
    return thisInstance();
  }

  @Override
  public TYPE withoutMethod(Predicate<IMethodGenerator<?, ?>> removalFilter) {
    primaryType().withoutMethod(removalFilter);
    return thisInstance();
  }

  @Override
  public TYPE withoutMethod(String identifier, IJavaBuilderContext context) {
    primaryType().withoutMethod(identifier, context);
    return thisInstance();
  }

  @Override
  public Optional<IMethodGenerator<?, ?>> method(String methodId, IJavaBuilderContext context, boolean includeTypeArguments) {
    return primaryType().method(methodId, context, includeTypeArguments);
  }

  @Override
  public Stream<ITypeGenerator<?>> types() {
    return primaryType().types();
  }

  @Override
  public Optional<ITypeGenerator<?>> type(String simpleName) {
    return primaryType().type(simpleName);
  }

  @Override
  public TYPE withType(ITypeGenerator<?> generator, Object... sortObject) {
    primaryType().withType(generator, sortObject);
    return thisInstance();
  }

  @Override
  public TYPE withoutType(String simpleName) {
    primaryType().withoutType(simpleName);
    return thisInstance();
  }

  @Override
  public TYPE withoutType(Predicate<ITypeGenerator<?>> removalFilter) {
    primaryType().withoutType(removalFilter);
    return thisInstance();
  }

  @Override
  public Stream<ITypeParameterGenerator<?>> typeParameters() {
    return primaryType().typeParameters();
  }

  @Override
  public TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter) {
    primaryType().withTypeParameter(typeParameter);
    return thisInstance();
  }

  @Override
  public TYPE withoutTypeParameter(String elementName) {
    primaryType().withoutTypeParameter(elementName);
    return thisInstance();
  }

  @Override
  public TYPE asInterface() {
    primaryType().asInterface();
    return thisInstance();
  }

  @Override
  public TYPE asAnnotationType() {
    primaryType().asAnnotationType();
    return thisInstance();
  }

  @Override
  public TYPE asEnum() {
    primaryType().asEnum();
    return thisInstance();
  }

  @Override
  public Optional<IJavaElementGenerator<?>> declaringGenerator() {
    return primaryType().declaringGenerator();
  }

  @Override
  public Optional<String> getDeclaringFullyQualifiedName() {
    return primaryType().getDeclaringFullyQualifiedName();
  }

  @Override
  public TYPE setDeclaringFullyQualifiedName(String parentFullyQualifiedName) {
    primaryType().setDeclaringFullyQualifiedName(parentFullyQualifiedName);
    return thisInstance();
  }

  @Override
  public IType getHierarchyType(IJavaBuilderContext context) {
    return primaryType().getHierarchyType(context);
  }

  @Override
  public TYPE withoutAllMethodsImplemented() {
    primaryType().withoutAllMethodsImplemented();
    return thisInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented() {
    primaryType().withAllMethodsImplemented();
    return thisInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded) {
    primaryType().withAllMethodsImplemented(callbackForMethodsAdded);
    return thisInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded, Function<IMethodGenerator<?, ?>, Object[]> methodSortOrderProvider) {
    primaryType().withAllMethodsImplemented(callbackForMethodsAdded, methodSortOrderProvider);
    return thisInstance();
  }

  @Override
  public boolean isWithAllMethodsImplemented() {
    return primaryType().isWithAllMethodsImplemented();
  }

  @Override
  public TYPE withImport(CharSequence name) {
    compilationUnit().withImport(name);
    return thisInstance();
  }

  @Override
  public TYPE withoutImport(CharSequence name) {
    compilationUnit().withoutImport(name);
    return thisInstance();
  }

  @Override
  public Stream<CharSequence> imports() {
    return compilationUnit().imports();
  }

  @Override
  public TYPE withStaticImport(CharSequence name) {
    compilationUnit().withStaticImport(name);
    return thisInstance();
  }

  @Override
  public TYPE withoutStaticImport(CharSequence name) {
    compilationUnit().withoutStaticImport(name);
    return thisInstance();
  }

  @Override
  public Stream<CharSequence> staticImports() {
    return compilationUnit().staticImports();
  }

  @Override
  public TYPE withoutAllImports() {
    compilationUnit().withoutAllImports();
    return thisInstance();
  }

  @Override
  public TYPE withFooter(ISourceGenerator<ICommentBuilder<?>> builder) {
    compilationUnit().withFooter(builder);
    return thisInstance();
  }

  @Override
  public Stream<ISourceGenerator<ICommentBuilder<?>>> footers() {
    return compilationUnit().footers();
  }

  private boolean isSetupDone() {
    return m_setupDone;
  }

  private void setSetupDone() {
    m_setupDone = true;
  }
}
