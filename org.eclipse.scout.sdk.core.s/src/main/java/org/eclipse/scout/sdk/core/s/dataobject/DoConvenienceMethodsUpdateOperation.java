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

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.model.api.query.AbstractQuery;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SourceState;
import org.eclipse.scout.sdk.core.util.Strings;

@SuppressWarnings("MethodMayBeStatic")
public class DoConvenienceMethodsUpdateOperation implements BiConsumer<IEnvironment, IProgress> {

  private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+[\\w_.]+;");
  @SuppressWarnings("HardcodedLineSeparator")
  private static final String NL = "\n";

  private final List<IType> m_dataObjects = new ArrayList<>();

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

    var buildContext = new JavaBuilderContext(dataObjectType.javaEnvironment());
    var replacements = dataObject
        .nodes().stream()
        .flatMap(node -> buildMethodGeneratorsFor(node, dataObjectType))
        .map(gen -> toReplacement(gen, dataObjectType, buildContext))
        .sorted(Comparator.comparingInt(Replacement::offset).thenComparing(Replacement::order))
        .collect(toList());
    if (replacements.isEmpty()) {
      return Optional.empty(); // no attributes
    }

    var newSource = applyReplacements(replacements, originalSource.get().asCharSequence());
    return Optional.of(insertMissingImports(buildContext, newSource));
  }

  protected CharSequence insertMissingImports(IJavaBuilderContext buildContext, CharSequence newSource) {
    var importsToAdd = buildContext
        .validator().importCollector()
        .createImportDeclarations(false)
        .collect(joining(NL, NL, ""));
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

  protected CharSequence applyReplacements(Iterable<Replacement> replacements, CharSequence originalSource) {
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

  protected Replacement toReplacement(IMethodGenerator<?, ?> generator, IType dataObjectType, IJavaBuilderContext buildContext) {
    var environment = dataObjectType.javaEnvironment();
    var newSourceForReplace = generator.toJavaSource(buildContext);
    var methodId = generator.identifier(environment);
    var existingMethod = dataObjectType.methods().withMethodIdentifier(methodId).first();
    if (existingMethod.isPresent()) {
      // replace
      var sourceRange = existingMethod.get().source().get();
      var offset = sourceRange.start();
      var fullMethodSourceSequence = sourceRange.asCharSequence(); // including leading documentations
      var methodSourceSequenceWithoutComments = CoreUtils.removeComments(fullMethodSourceSequence).trim();
      var methodStartWithoutComments = Strings.indexOf(methodSourceSequenceWithoutComments, fullMethodSourceSequence);
      if (methodStartWithoutComments > 0) {
        offset += methodStartWithoutComments;
      }
      return new Replacement(offset, sourceRange.end() - offset + 1, newSourceForReplace);
    }

    // insert new method at the bottom of the class
    var newSourceForInsert = new StringBuilder(newSourceForReplace.length() + 1)
        .append(NL) // when inserting a new method: add newline before
        .append(newSourceForReplace);
    var insertIndex = dataObjectType.source().get().end() - 1;
    return new Replacement(insertIndex, 0, newSourceForInsert);
  }

  protected Stream<IMethodGenerator<?, ?>> buildMethodGeneratorsFor(DataObjectNode node, IType owner) {
    switch (node.kind()) {
      case DO_VALUE:
        return buildMethodGeneratorsForValue(node, owner);
      case DO_LIST:
        return buildMethodGeneratorsForList(node, owner);
      default:
        throw newFail("DataObject node type '{}' of '{}' is not supported yet.", node.kind(), node);
    }
  }

  protected Stream<IMethodGenerator<?, ?>> buildMethodGeneratorsForValue(DataObjectNode node, IType owner) {
    var upperCaseName = Strings.ensureStartWithUpperCase(node.name());
    var dataTypeRef = node.dataType().reference();
    var chainedSetter = MethodGenerator.create()
        .asPublic()
        .withReturnType(buildReturnTypeReferenceFor(owner))
        .withElementName(PropertyBean.CHAINED_SETTER_PREFIX + upperCaseName)
        .withParameter(MethodParameterGenerator.create()
            .withElementName(node.name())
            .withDataType(dataTypeRef))
        .withAnnotation(createGenerated())
        .withBody(b -> buildValueChainedSetterBody(node, b));
    withOverrideAnnotationIfNecessary(chainedSetter, owner);
    if (node.isInherited()) {
      // for inherited nodes: only overwrite (and narrow) the chained setter
      return Stream.of(chainedSetter);
    }

    var getterPrefix = getterPrefixFor(dataTypeRef);
    var valueGetter = MethodGenerator.create()
        .asPublic()
        .withReturnType(dataTypeRef)
        .withElementName(getterPrefix + upperCaseName)
        .withAnnotation(createGenerated())
        .withBody(b -> buildValueGetterBody(node, b));
    withOverrideAnnotationIfNecessary(valueGetter, owner);
    return Stream.of(chainedSetter, valueGetter);
  }

  protected String getterPrefixFor(String type) {
    if (Boolean.class.getName().equals(type)) {
      return PropertyBean.GETTER_BOOL_PREFIX;
    }
    return PropertyBean.GETTER_PREFIX;
  }

  protected IAnnotationGenerator<?> createGenerated() {
    return AnnotationGenerator.createGenerated("DoConvenienceMethodsGenerator", null);
  }

  protected String buildReturnTypeReferenceFor(IType owner) {
    var ref = owner.reference();
    if (!owner.hasTypeParameters()) {
      return ref;
    }
    return owner.typeParameters()
        .map(IJavaElement::elementName)
        .collect(joining(", ", ref + JavaTypes.C_GENERIC_START, JavaTypes.C_GENERIC_END + ""));
  }

  protected void appendCollectionSetterParameter(IType owner, IMethodGenerator<?, ?> generator, String nodeName, IType dataType) {
    var methodId = JavaTypes.createMethodIdentifier(generator.elementName(owner.javaEnvironment()).get(), singleton(Collection.class.getName()));
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
      collectionDataTypeRef.append(JavaTypes.C_QUESTION_MARK).append(' ').append(JavaTypes.EXTENDS).append(' ').append(dataType.reference());
    }
    else {
      collectionDataTypeRef.append(dataType.reference());
    }
    collectionDataTypeRef.append(JavaTypes.C_GENERIC_END);

    generator.withParameter(MethodParameterGenerator.create()
        .withElementName(nodeName)
        .withDataType(collectionDataTypeRef.toString()));
  }

  protected Stream<IMethodGenerator<?, ?>> buildMethodGeneratorsForList(DataObjectNode node, IType owner) {
    var upperCaseName = Strings.ensureStartWithUpperCase(node.name());
    var dataTypeRef = node.dataType().reference();
    var ownerRetRef = buildReturnTypeReferenceFor(owner);
    var chainedSetterCollection = MethodGenerator.create()
        .asPublic()
        .withReturnType(ownerRetRef)
        .withElementName(PropertyBean.CHAINED_SETTER_PREFIX + upperCaseName)
        .withAnnotation(createGenerated())
        .withBody(b -> buildListChainedSetterBody(node, b));
    appendCollectionSetterParameter(owner, chainedSetterCollection, node.name(), node.dataType());
    withOverrideAnnotationIfNecessary(chainedSetterCollection, owner);
    var chainedSetterArray = MethodGenerator.create()
        .asPublic()
        .withReturnType(ownerRetRef)
        .withElementName(PropertyBean.CHAINED_SETTER_PREFIX + upperCaseName)
        .withParameter(MethodParameterGenerator.create()
            .withElementName(node.name())
            .asVarargs()
            .withDataType(dataTypeRef))
        .withAnnotation(createGenerated())
        .withBody(b -> buildListChainedSetterBody(node, b));
    withOverrideAnnotationIfNecessary(chainedSetterArray, owner);
    if (node.isInherited()) {
      // for inherited nodes: only overwrite (and narrow) the chained setter
      return Stream.of(chainedSetterCollection, chainedSetterArray);
    }
    var getterPrefix = getterPrefixFor(dataTypeRef);
    var listGetter = MethodGenerator.create()
        .asPublic()
        .withReturnType(List.class.getName() + JavaTypes.C_GENERIC_START + dataTypeRef + JavaTypes.C_GENERIC_END)
        .withElementName(getterPrefix + upperCaseName)
        .withAnnotation(createGenerated())
        .withBody(b -> buildValueGetterBody(node, b));
    withOverrideAnnotationIfNecessary(listGetter, owner);
    return Stream.of(chainedSetterCollection, chainedSetterArray, listGetter);
  }

  protected void withOverrideAnnotationIfNecessary(IMethodGenerator<?, ?> method, IType owner) {
    var methodId = method.identifier(owner.javaEnvironment());
    var existsInSuperHierarchy = owner.superTypes().withSelf(false).stream()
        .flatMap(st -> st.methods().withMethodIdentifier(methodId).stream())
        .findAny().isPresent();
    if (existsInSuperHierarchy) {
      method.withAnnotation(AnnotationGenerator.createOverride());
    }
  }

  protected void buildValueGetterBody(DataObjectNode node, IMethodBodyBuilder<?> builder) {
    builder.returnClause().append(node.name()).parenthesisOpen().parenthesisClose().dot()
        .appendFrom(IScoutApi.class, api -> api.DoNode().getMethodName()).parenthesisOpen().parenthesisClose().semicolon();
  }

  protected void buildValueChainedSetterBody(DataObjectNode node, IMethodBodyBuilder<?> builder) {
    builder.append(node.name()).parenthesisOpen().parenthesisClose().dot().appendFrom(IScoutApi.class, api -> api.DoNode().setMethodName())
        .parenthesisOpen().append(node.name()).parenthesisClose().semicolon().nl()
        .returnClause().appendThis().semicolon();
  }

  protected void buildListChainedSetterBody(DataObjectNode node, IMethodBodyBuilder<?> builder) {
    builder.append(node.name()).parenthesisOpen().parenthesisClose().dot().appendFrom(IScoutApi.class, api -> api.DoList().updateAllMethodName())
        .parenthesisOpen().append(node.name()).parenthesisClose().semicolon().nl()
        .returnClause().appendThis().semicolon();
  }

  private static class Replacement {

    private static final AtomicLong ORDER_SEQUENCE = new AtomicLong();
    private final int m_offset;
    private final int m_length;
    private final CharSequence m_newSource;
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
  }

  public List<IType> dataObjects() {
    return unmodifiableList(m_dataObjects);
  }

  public DoConvenienceMethodsUpdateOperation withDataObjects(Collection<IType> dos) {
    m_dataObjects.clear();
    m_dataObjects.addAll(dos);
    return this;
  }

  @Override
  public String toString() {
    return "Update DataObject convenience methods";
  }
}
