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
package org.eclipse.scout.sdk.core.s.dataobject;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectNode.DataObjectNodeKind;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SourceState;
import org.eclipse.scout.sdk.core.util.Strings;

@SuppressWarnings("MethodMayBeStatic")
public class DoConvenienceMethodsUpdateOperation implements BiConsumer<IEnvironment, IProgress> {

  private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+[\\w_.]+;");
  public static final String CONVENIENCE_METHOD_MARKER_START = "/* ******************";

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

    var cuSource = originalSource.get().asCharSequence();
    var buildContext = new JavaBuilderContext(dataObjectType.javaEnvironment()); // to collect all imports
    var replacedMethods = new HashSet<IMethod>();

    // methods to add or update 
    var replacements = dataObject
        .nodes().stream()
        .flatMap(node -> buildMethodGeneratorsFor(node, dataObjectType))
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
        .ifPresent(r -> r.setNewSource(convenienceMethodsMarker() + r.newSource()));
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

  protected Stream<Replacement> buildReplacements(IMethodGenerator<?, ?> generator, IType dataObjectType, CharSequence cuSource, IJavaBuilderContext buildContext, Collection<IMethod> replacedMethods) {
    var newSource = generator.toJavaSource(buildContext); // pass a shared BuilderContext to collect imports
    var methodId = generator.identifier(dataObjectType.javaEnvironment());
    var existingMethod = dataObjectType.methods().withMethodIdentifier(methodId).first();

    // insert at the bottom of the class
    var insertIndex = dataObjectType.source().get().end() - 1;
    var newMethodReplacement = new Replacement(insertIndex, 0, newSource);
    if (existingMethod.isEmpty()) {
      return Stream.of(newMethodReplacement);
    }

    // additionally delete existing method
    var methodToDelete = existingMethod.get();
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
    var sourceRange = method.source().get();
    var methodStartOffset = sourceRange.start();

    var declarationStartRelativeToMethodSource = method.sourceOfDeclaration().get().start() - sourceRange.start();
    var pos = Strings.indexOf(CONVENIENCE_METHOD_MARKER_START, sourceRange.asCharSequence(), 0, declarationStartRelativeToMethodSource);
    if (pos > 0) {
      // if a convenience marker comment start is found before the method declaration start
      methodStartOffset += pos;
    }
    while (methodStartOffset >= 1 && Character.isWhitespace(cuSource.charAt(methodStartOffset - 1))) {
      methodStartOffset--;
    }
    return new Replacement(methodStartOffset, sourceRange.end() - methodStartOffset + 1, "");
  }

  protected Stream<IMethodGenerator<?, ?>> buildMethodGeneratorsFor(DataObjectNode node, IType owner) {
    Stream<IMethodGenerator<?, ?>> methodGenerators;
    if (node.kind() == DataObjectNodeKind.VALUE) {
      methodGenerators = buildMethodGeneratorsForValue(node, owner);
    }
    else {
      methodGenerators = buildMethodGeneratorsForCollection(node, owner);
    }

    if (!node.hasJavaDoc()) {
      return methodGenerators;
    }
    return methodGenerators.peek(g -> g.withComment(b -> appendJavaDocLink(b, node.name())));
  }

  protected void appendJavaDocLink(IJavaElementCommentBuilder<?> b, String name) {
    b.appendJavaDocStart().nl()
        .append("* See ").appendLink("#" + name + "()").append('.').nl()
        .appendBlockCommentEnd().nl();
  }

  protected Stream<IMethodGenerator<?, ?>> buildMethodGeneratorsForValue(DataObjectNode node, IType owner) {
    var dataTypeRef = node.dataType().reference();
    var chainedSetter = ScoutMethodGenerator.createDoValueSetter(node.name(), dataTypeRef, owner);
    if (node.isInherited()) {
      // for inherited nodes: only overwrite (and narrow) the chained setter
      return Stream.of(chainedSetter);
    }

    var valueGetter = ScoutMethodGenerator.createDoNodeGetter(node.name(), dataTypeRef, owner);
    if (implementedInSuperClass(valueGetter, owner)) {
      // the method already exists in the super class. no need to override
      return Stream.of(chainedSetter);
    }
    return Stream.of(chainedSetter, valueGetter);
  }

  protected Stream<IMethodGenerator<?, ?>> buildMethodGeneratorsForCollection(DataObjectNode node, IType owner) {
    var dataTypeRef = node.dataType().reference();
    var chainedSetterCollection = ScoutMethodGenerator.createDoCollectionSetterCollection(node.name(), dataTypeRef, owner);
    var chainedSetterArray = ScoutMethodGenerator.createDoCollectionSetterVarargs(node.name(), dataTypeRef, owner);
    if (node.isInherited()) {
      // for inherited nodes: only overwrite (and narrow) the chained setter
      return Stream.of(chainedSetterCollection, chainedSetterArray);
    }

    String getterCollectionFqn;
    switch (node.kind()) {
      case LIST:
        getterCollectionFqn = List.class.getName();
        break;
      case SET:
        getterCollectionFqn = Set.class.getName();
        break;
      case COLLECTION:
        getterCollectionFqn = Collection.class.getName();
        break;
      default:
        throw newFail("Unsupported DoNode kind of '{}' on '{}'.", node, owner.name());
    }

    var collectionGetterReturnTypeReference = getterCollectionFqn + JavaTypes.C_GENERIC_START + dataTypeRef + JavaTypes.C_GENERIC_END;
    var collectionGetter = ScoutMethodGenerator.createDoNodeGetter(node.name(), collectionGetterReturnTypeReference, owner);
    if (implementedInSuperClass(collectionGetter, owner)) {
      // the method already exists in the super class. no need to override the getter
      return Stream.of(chainedSetterCollection, chainedSetterArray);
    }
    return Stream.of(chainedSetterCollection, chainedSetterArray, collectionGetter);
  }

  protected String convenienceMethodsMarker() {
    return lineSeparator() + lineSeparator()
        + CONVENIENCE_METHOD_MARKER_START + "********************************************************" + lineSeparator()
        + "   * GENERATED CONVENIENCE METHODS" + lineSeparator()
        + "   * *************************************************************************/"
        + lineSeparator() + lineSeparator();
  }

  protected IAnnotationGenerator<?> createGenerated() {
    return AnnotationGenerator.createGenerated("DoConvenienceMethodsGenerator", null);
  }

  protected boolean implementedInSuperClass(IMethodGenerator<?, ?> method, IType owner) {
    var methodId = method.identifier(owner.javaEnvironment());
    return owner
        .superTypes()
        .withSelf(false)
        .stream()
        .flatMap(t -> t.methods().withMethodIdentifier(methodId).stream())
        .anyMatch(m -> !Flags.isAbstract(m.flags()) || Flags.isDefaultMethod(m.flags()));
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
