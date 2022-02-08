/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dataobject;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.JavaTypes.createMethodIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.IAnnotatableGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutAnnotationApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.IScoutMethodGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutDoMethodGenerator;
import org.eclipse.scout.sdk.core.util.SourceState;
import org.eclipse.scout.sdk.core.util.Strings;

@SuppressWarnings("MethodMayBeStatic")
public class DoConvenienceMethodsUpdateOperation implements BiConsumer<IEnvironment, IProgress> {

  private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+[\\w_.]+;");

  private final List<IType> m_dataObjects = new ArrayList<>();
  private String m_lineSeparator;

  @Override
  public void accept(IEnvironment environment, IProgress progress) {
    var dataObjects = dataObjects();
    progress.init(dataObjects.size(), toString());
    var writeOperations = dataObjects.stream()
        .flatMap(t -> updateDataObject(t, environment, progress.newChild(1)).stream())
        .collect(toList());
    SdkFuture.awaitAll(writeOperations);
  }

  protected Optional<IFuture<IType>> updateDataObject(IType dataObject, IEnvironment environment, IProgress progress) {
    return DataObjectModel.wrap(dataObject)
        .flatMap(this::buildNewSource)
        .map(newSrc -> write(newSrc, dataObject, environment, progress));
  }

  protected IFuture<IType> write(CharSequence newSource, IType dataObjectType, IEnvironment environment, IProgress progress) {
    return environment.writeCompilationUnitAsync(newSource, dataObjectType.requireCompilationUnit(), progress);
  }

  protected Optional<CharSequence> buildNewSource(DataObjectModel dataObject) {
    var dataObjectType = dataObject.unwrap();
    var originalSource = dataObjectType.requireCompilationUnit().source();
    if (originalSource.isEmpty()) {
      return Optional.empty(); // cannot update. no source available
    }

    var cuSource = originalSource.orElseThrow().asCharSequence();
    var buildContext = new JavaBuilderContext(dataObjectType.javaEnvironment()); // to collect all imports
    var replacedMethods = new HashSet<IMethod>();
    var scoutApi = dataObjectType.javaEnvironment().requireApi(IScoutApi.class);

    // methods to add or update 
    var replacements = dataObject
        .nodes().stream()
        .flatMap(node -> buildMethodGeneratorsFor(node, dataObjectType, scoutApi))
        .flatMap(gen -> buildReplacements(gen, dataObjectType, cuSource, buildContext, replacedMethods))
        .collect(toList());

    // already existing methods which are no longer necessary: all annotated methods which are not updated
    dataObjectType.methods()
        .withAnnotation(GeneratedAnnotation.FQN)
        .stream()
        .filter(m -> !replacedMethods.contains(m))
        .filter(this::hasDoConvenienceGeneratedAnnotation)
        .map(m -> toMethodDeleteReplacement(m, cuSource))
        .forEach(replacements::add);
    if (replacements.isEmpty()) {
      return Optional.empty(); // nothing to do
    }

    replacements.sort(comparingInt(Replacement::offset).thenComparing(Replacement::order));
    addConvenienceMethodsMarkerCommentToFirst(replacements);
    var newSource = insertMissingImports(buildContext, applyModifications(replacements, cuSource));
    return Optional.of(newSource);
  }

  protected void addConvenienceMethodsMarkerCommentToFirst(Collection<Replacement> replacements) {
    replacements.stream()
        .filter(r -> r.newSource().length() > 0) // not a delete replacement
        .findFirst()
        .ifPresent(r -> r.setNewSource(lineSeparator() + ScoutDoMethodGenerator.convenienceMethodsMarkerComment(lineSeparator()) + lineSeparator() + r.newSource()));
  }

  protected CharSequence insertMissingImports(IJavaBuilderContext buildContext, CharSequence newSource) {
    var importsToAdd = buildContext
        .validator().importCollector()
        .createImportDeclarations(false)
        .collect(joining(lineSeparator(), lineSeparator(), ""));
    if (Strings.isBlank(importsToAdd)) {
      return newSource;
    }
    return IMPORT_PATTERN.matcher(newSource).results()
        .filter(r -> SourceState.isInCode(newSource, r.start()))
        .findAny()
        .map(r -> insertInto(newSource, importsToAdd, r.end()))
        .orElse(newSource);
  }

  protected CharSequence insertInto(CharSequence target, String insertText, int pos) {
    return new StringBuilder(target).insert(pos, insertText);
  }

  protected CharSequence applyModifications(Iterable<Replacement> replacements, CharSequence originalSource) {
    var newSource = new StringBuilder();
    var startPos = 0;
    for (var replacement : replacements) {
      newSource.append(originalSource, startPos, replacement.offset());
      newSource.append(replacement.newSource());
      startPos = replacement.offset() + replacement.length();
    }
    newSource.append(originalSource, startPos, originalSource.length());
    return newSource.toString();
  }

  protected String createMethodDeclarationSource(IMethod m) {
    var paramSource = m
        .parameters().stream()
        .map(p -> p.source().orElseThrow())
        .map(ISourceRange::asCharSequence)
        .collect(toList());
    return createMethodIdentifier(m.elementName(), paramSource);
  }

  protected String createMethodDeclarationSource(IMethodGenerator<?, ?> m, IJavaBuilderContext context) {
    var paramSource = m.parameters()
        .map(p -> p.toJavaSource(context))
        .collect(toList());
    return createMethodIdentifier(m.elementName(context).orElseThrow(), paramSource);
  }

  protected Stream<Replacement> buildReplacements(IMethodGenerator<?, ?> generator, IType dataObjectType, CharSequence cuSource, IJavaBuilderContext context, Collection<IMethod> replacedMethods) {
    // search existing method not using methodId. Because the type used in the existing method might no longer exist. This would result in an exception (type missing).
    // instead compare the source of the method declaration to be created with the source of the existing method declarations (ignoring fully qualified type names).
    var methodDeclarationSource = createMethodDeclarationSource(generator, context);
    var methodName = generator.elementName(context).orElseThrow();
    var existingMethod = dataObjectType
        .methods()
        .withName(methodName).stream()
        .filter(m -> methodDeclarationSource.equals(createMethodDeclarationSource(m)))
        .findAny();

    // insert at the bottom of the class
    var insertIndex = dataObjectType.source().orElseThrow().end() - 1;
    var newSource = generator.toJavaSource(context); // pass a shared BuilderContext to collect imports
    var newMethodReplacement = new Replacement(insertIndex, 0, newSource);
    if (existingMethod.isEmpty()) {
      return Stream.of(newMethodReplacement);
    }

    // additionally delete existing method
    var methodToDelete = existingMethod.orElseThrow();
    replacedMethods.add(methodToDelete);
    var methodDeleteReplacement = toMethodDeleteReplacement(methodToDelete, cuSource);
    return Stream.of(methodDeleteReplacement, newMethodReplacement);
  }

  protected boolean hasDoConvenienceGeneratedAnnotation(IAnnotatable annotatable) {
    var generatedValues = annotatable.annotations()
        .withManagedWrapper(GeneratedAnnotation.class)
        .first()
        .map(GeneratedAnnotation::value)
        .map(Arrays::asList)
        .orElse(emptyList());
    if (generatedValues.isEmpty()) {
      return false;
    }
    return generatedValues.contains(ScoutAnnotationGenerator.DO_CONVENIENCE_METHODS_GENERATED_COMMENT);
  }

  protected Replacement toMethodDeleteReplacement(IMethod method, CharSequence cuSource) {
    var sourceRange = method.source().orElseThrow();
    var methodStartOffset = sourceRange.start();

    var declarationStartRelativeToMethodSource = method.sourceOfDeclaration().orElseThrow().start() - sourceRange.start();
    var pos = Strings.indexOf(ScoutDoMethodGenerator.CONVENIENCE_METHOD_MARKER_START, sourceRange.asCharSequence(), 0, declarationStartRelativeToMethodSource);
    if (pos > 0) {
      // if a convenience marker comment start is found before the method declaration start
      methodStartOffset += pos;
    }
    while (methodStartOffset >= 1 && Character.isWhitespace(cuSource.charAt(methodStartOffset - 1))) {
      methodStartOffset--;
    }
    return new Replacement(methodStartOffset, sourceRange.end() - methodStartOffset + 1, "");
  }

  protected Stream<IScoutMethodGenerator<?, ?>> buildMethodGeneratorsFor(DataObjectNode node, IType owner, IScoutAnnotationApi scoutApi) {
    var methodGenerators = ScoutDoMethodGenerator.createConvenienceMethods(node.name(), node.kind(), node.dataType().reference(), node.isInherited(), owner);
    methodGenerators = methodGenerators.peek(g -> copyAnnotations(node.method(), g, scoutApi));
    if (!node.hasJavaDoc()) {
      return methodGenerators;
    }
    return methodGenerators.peek(g -> g.withComment(b -> appendJavaDocLink(b, node.name())));
  }

  protected void copyAnnotations(IAnnotatable source, IAnnotatableGenerator<?> target, IScoutAnnotationApi scoutApi) {
    source.annotations().stream()
        .filter(a -> !isAnnotationIgnoredForCopy(a, scoutApi))
        .map(IAnnotation::toWorkingCopy)
        .forEach(target::withAnnotation);
  }

  protected boolean isAnnotationIgnoredForCopy(IAnnotation a, IScoutAnnotationApi scoutApi) {
    var annotationFqn = a.type().name();
    return Override.class.getName().equals(annotationFqn)
        || scoutApi.AttributeName().fqn().equals(annotationFqn)
        || scoutApi.ValueFormat().fqn().equals(annotationFqn);
  }

  protected void appendJavaDocLink(IJavaElementCommentBuilder<?> b, String name) {
    b.appendJavaDocStart().nl()
        .append("* See ").appendLink(JavaElementCommentBuilder.LINK_MEMBER_DELIMITER + name + "()").append('.').nl()
        .appendBlockCommentEnd().nl();
  }

  protected IAnnotationGenerator<?> createGenerated() {
    return AnnotationGenerator.createGenerated("DoConvenienceMethodsGenerator", null);
  }

  protected static class Replacement {

    private static final AtomicLong ORDER_SEQUENCE = new AtomicLong();
    private final int m_offset;
    private final int m_length;
    private CharSequence m_newSource;
    private final long m_order;

    public Replacement(int offset, int length, CharSequence newSource) {
      m_offset = offset;
      m_length = length;
      m_newSource = newSource;
      m_order = ORDER_SEQUENCE.incrementAndGet();
    }

    public int offset() {
      return m_offset;
    }

    public int length() {
      return m_length;
    }

    public long order() {
      return m_order;
    }

    public CharSequence newSource() {
      return m_newSource;
    }

    public void setNewSource(CharSequence src) {
      m_newSource = src;
    }
  }

  public List<IType> dataObjects() {
    return unmodifiableList(m_dataObjects);
  }

  public DoConvenienceMethodsUpdateOperation withDataObjects(Collection<IType> dos) {
    m_dataObjects.clear();
    m_dataObjects.addAll(dos);
    return this;
  }

  public DoConvenienceMethodsUpdateOperation withLineSeparator(String lineSeparator) {
    m_lineSeparator = lineSeparator;
    return this;
  }

  public String lineSeparator() {
    return m_lineSeparator;
  }

  @Override
  public String toString() {
    return "Update DataObject convenience methods";
  }
}
