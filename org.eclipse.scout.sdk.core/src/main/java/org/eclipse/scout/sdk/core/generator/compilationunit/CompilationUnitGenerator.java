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
package org.eclipse.scout.sdk.core.generator.compilationunit;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPublic;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformImport;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformPackage;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.CommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.generator.AbstractJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.SortedMemberEntry;
import org.eclipse.scout.sdk.core.imports.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link CompilationUnitGenerator}</h3>
 *
 * @since 6.1.0
 */
public class CompilationUnitGenerator<TYPE extends ICompilationUnitGenerator<TYPE>> extends AbstractJavaElementGenerator<TYPE> implements ICompilationUnitGenerator<TYPE> {

  private PackageGenerator m_package;
  private final List<CharSequence> m_declaredImports;
  private final List<CharSequence> m_declaredStaticImports;
  private final Collection<SortedMemberEntry> m_types;
  private final List<ISourceGenerator<ICommentBuilder<?>>> m_footerSourceBuilders;

  protected CompilationUnitGenerator() {
    m_declaredImports = new ArrayList<>();
    m_declaredStaticImports = new ArrayList<>();
    m_types = new ArrayList<>();
    m_footerSourceBuilders = new ArrayList<>();
  }

  protected CompilationUnitGenerator(ICompilationUnit cu, IWorkingCopyTransformer transformer) {
    super(cu);

    m_package = transformPackage(cu.containingPackage(), transformer).orElse(null);
    m_declaredImports = cu.imports()
        .filter(i -> !i.isStatic())
        .map(i -> transformImport(i, transformer))
        .flatMap(Optional::stream)
        .collect(toList());
    m_declaredStaticImports = cu.imports()
        .filter(IImport::isStatic)
        .map(i -> transformImport(i, transformer))
        .flatMap(Optional::stream)
        .collect(toList());
    m_types = cu.types().stream()
        .filter(t -> !t.elementName().equals(JavaTypes.PackageInfo))
        .map(t -> transformType(t, transformer)
            .map(g -> new SortedMemberEntry(g, t)))
        .flatMap(Optional::stream)
        .peek(entry -> applyConnection((ITypeGenerator<?>) entry.generator(), this))
        .collect(toList());
    m_footerSourceBuilders = new ArrayList<>();
    cu.javaDoc()
        .map(ISourceRange::asCharSequence)
        .<ISourceGenerator<IJavaElementCommentBuilder<?>>> map(s -> b -> {
          b.append(s);
          if (!Strings.endsWith(s, b.context().lineDelimiter())) {
            b.nl();
          }
        })
        .ifPresent(this::withComment);
  }

  /**
   * @return A new empty {@link ICompilationUnitGenerator}.
   */
  public static ICompilationUnitGenerator<?> create() {
    return new CompilationUnitGenerator<>();
  }

  /**
   * Creates a new {@link ICompilationUnitGenerator} based on the given {@link ICompilationUnit}.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param cu
   *          The {@link ICompilationUnit} that should be converted to an {@link ICompilationUnitGenerator}. Must not be
   *          {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the compilation
   *          unit to a working copy. May be {@code null} if no custom transformation is required and the compilation
   *          unit should be converted into a working copy without any modification.
   * @return A new {@link ICompilationUnitGenerator} initialized to generate source that is structurally similar to the
   *         one from the given {@link ICompilationUnit}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static ICompilationUnitGenerator<?> create(ICompilationUnit cu, IWorkingCopyTransformer transformer) {
    return new CompilationUnitGenerator<>(cu, transformer);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    var currentValidator = builder.context().validator();
    currentValidator.runWithImportCollector(() -> buildCompilationUnit(builder), inner -> new CompilationUnitScopedImportCollector(inner, packageName().orElse(null), this));
  }

  protected void buildCompilationUnit(IJavaSourceBuilder<?> builder) {
    super.build(builder);

    var typeSource = buildTypeSource(builder.context()); // pre build type source so that all imports are defined
    var nl = builder.context().lineDelimiter();

    builder
        .append(getPackage()
            .filter(pck -> pck.elementName(builder.context()).isPresent())
            .map(pck -> b -> b.append(pck).nl().nl())) // only add newlines if a package is available
        .append(builder.context().validator().importCollector().createImportDeclarations().map(ISourceGenerator::raw), null, nl, nl)
        .append(typeSource)
        .nl()
        .append(footers()
            .map(b -> b.generalize(CommentBuilder::create)), nl, nl, null);
  }

  protected StringBuilder buildTypeSource(IBuilderContext context) {
    var typeGenerator = (ISourceGenerator<ISourceBuilder<?>>) builder -> builder.append(
        m_types.stream().sorted().map(SortedMemberEntry::generator),
        context.lineDelimiter(), context.lineDelimiter() + context.lineDelimiter(), null);
    return typeGenerator.toSource(identity(), context);
  }

  @Override
  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    return JavaElementCommentBuilder.createForCompilationUnit(builder, this);
  }

  @Override
  public Optional<PackageGenerator> getPackage() {
    return Optional.ofNullable(m_package);
  }

  @Override
  public Optional<String> packageName() {
    return getPackage().flatMap(PackageGenerator::elementName);
  }

  @Override
  public TYPE withPackage(PackageGenerator generator) {
    m_package = generator;
    return thisInstance();
  }

  @Override
  public TYPE withPackageName(String name) {
    if (m_package == null) {
      m_package = PackageGenerator.create();
    }
    m_package.withElementName(name);
    return thisInstance();
  }

  @Override
  public TYPE withImport(CharSequence name) {
    m_declaredImports.add(Ensure.notBlank(name));
    return thisInstance();
  }

  @Override
  public TYPE withoutImport(CharSequence name) {
    m_declaredImports.remove(name);
    return thisInstance();
  }

  @Override
  public Stream<CharSequence> imports() {
    return m_declaredImports.stream();
  }

  @Override
  public TYPE withStaticImport(CharSequence name) {
    m_declaredStaticImports.add(Ensure.notBlank(name));
    return thisInstance();
  }

  @Override
  public TYPE withoutStaticImport(CharSequence name) {
    m_declaredStaticImports.remove(name);
    return thisInstance();
  }

  @Override
  public Stream<CharSequence> staticImports() {
    return m_declaredStaticImports.stream();
  }

  @Override
  public TYPE withoutAllImports() {
    m_declaredImports.clear();
    m_declaredStaticImports.clear();
    return thisInstance();
  }

  @Override
  public Optional<ITypeGenerator<?>> mainType() {
    return types()
        .filter(t -> isPublic(t.flags()))
        .findAny();
  }

  @Override
  public Optional<String> elementName() {
    return super.elementName().map(s -> Strings.removeSuffix(s, JavaTypes.JAVA_FILE_SUFFIX));
  }

  @Override
  public TYPE withElementName(String newName) {
    if (newName != null && !newName.endsWith(JavaTypes.JAVA_FILE_SUFFIX)) {
      return super.withElementName(newName + JavaTypes.JAVA_FILE_SUFFIX);
    }
    return super.withElementName(newName);
  }

  @Override
  public Optional<String> fileName() {
    return super.elementName();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Stream<ITypeGenerator<?>> types() {
    return m_types.stream()
        .map(SortedMemberEntry::generator)
        .map(g -> (ITypeGenerator<? extends IMemberBuilder<?>>) g);
  }

  @Override
  public TYPE withType(ITypeGenerator<?> generator, Object... sortObjects) {
    m_types.add(new SortedMemberEntry(applyConnection(generator, this), sortObjects));
    return thisInstance();
  }

  @Override
  public TYPE withoutType(Predicate<ITypeGenerator<?>> removalFilter) {
    for (var it = m_types.iterator(); it.hasNext();) {
      @SuppressWarnings("unchecked")
      var generator = (ITypeGenerator<? extends IMemberBuilder<?>>) it.next().generator();
      if (removalFilter == null || removalFilter.test(generator)) {
        it.remove();
        applyConnection(generator, null);
        return thisInstance();
      }
    }
    return thisInstance();
  }

  protected static ITypeGenerator<?> applyConnection(ITypeGenerator<?> child, IJavaElementGenerator<?> parent) {
    if (child instanceof AbstractMemberGenerator) {
      ((AbstractMemberGenerator<?>) child).withDeclaringGenerator(parent);
    }
    return child;
  }

  @Override
  public TYPE withFooter(ISourceGenerator<ICommentBuilder<?>> builder) {
    if (builder != null) {
      m_footerSourceBuilders.add(builder);
    }
    return thisInstance();
  }

  @Override
  public Stream<ISourceGenerator<ICommentBuilder<?>>> footers() {
    return m_footerSourceBuilders.stream();
  }
}
