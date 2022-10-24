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
package org.eclipse.scout.sdk.core.generator.type;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAnnotation;
import static org.eclipse.scout.sdk.core.model.api.Flags.isDefaultMethod;
import static org.eclipse.scout.sdk.core.model.api.Flags.isEnum;
import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPrivate;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformField;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformMethod;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformType;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformTypeParameter;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.MemberBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.imports.EnclosingTypeScopedImportCollector;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link TypeGenerator}</h3>
 *
 * @since 6.1.0
 */
public class TypeGenerator<TYPE extends ITypeGenerator<TYPE>> extends AbstractMemberGenerator<TYPE> implements ITypeGenerator<TYPE> {

  private static final AtomicLong HIERARCHY_TYPE_COUNTER = new AtomicLong();
  private static final String HIERARCHY_TYPE_KEY = "tmpHierarchyType";

  private final List<ITypeParameterGenerator<?>> m_typeParameters;
  private final List<JavaBuilderContextFunction<String>> m_interfaces;
  private final List<SortedMemberEntry> m_members;
  private final FinalValue<String> m_fullyQualifiedName;
  private final FinalValue<String> m_qualifier;
  private JavaBuilderContextFunction<String> m_superClass;
  private String m_declaringFullyQualifiedName;
  private boolean m_addAllNecessaryMethods;
  private IWorkingCopyTransformer m_unimplementedMethodsTransformer;
  private Function<IMethodGenerator<?, ?>, Object[]> m_unimplementedMethodSortOrderProvider;

  protected TypeGenerator() {
    m_typeParameters = new ArrayList<>();
    m_interfaces = new ArrayList<>();
    m_members = new ArrayList<>();
    m_fullyQualifiedName = new FinalValue<>();
    m_qualifier = new FinalValue<>();
  }

  protected TypeGenerator(IType type, IWorkingCopyTransformer transformer) {
    super(type, transformer);
    m_fullyQualifiedName = new FinalValue<>();
    m_qualifier = new FinalValue<>();
    m_typeParameters = type.typeParameters()
        .map(p -> transformTypeParameter(p, transformer))
        .flatMap(Optional::stream)
        .collect(toList());

    m_superClass = type.superClass()
        .map(IType::reference)
        .map(JavaBuilderContextFunction::create)
        .orElse(null);

    m_interfaces = type.superInterfaces()
        .map(IType::reference)
        .map(JavaBuilderContextFunction::create)
        .collect(toList());

    m_members = new ArrayList<>();
    type.fields().stream()
        .map(f -> transformField(f, transformer)
            .map(g -> new SortedMemberEntry(g, f)))
        .flatMap(Optional::stream)
        .peek(s -> applyConnection(s.generator(), this))
        .collect(toCollection(() -> m_members));
    type.methods().stream()
        .map(m -> transformMethod(m, transformer)
            .map(g -> new SortedMemberEntry(g, m)))
        .flatMap(Optional::stream)
        .peek(s -> applyConnection(s.generator(), this))
        .collect(toCollection(() -> m_members));
    type.innerTypes().stream()
        .map(t -> transformType(t, transformer)
            .map(g -> new SortedMemberEntry(g, t)))
        .flatMap(Optional::stream)
        .peek(s -> applyConnection(s.generator(), this))
        .collect(toCollection(() -> m_members));

    var declaringFqn = type.declaringType()
        .map(IType::name)
        .orElseGet(() -> type.compilationUnit()
            .map(ICompilationUnit::containingPackage)
            .flatMap(pck -> Strings.notBlank(pck.elementName()))
            .orElse(""));

    setDeclaringFullyQualifiedName(declaringFqn);
  }

  /**
   * @return A new empty {@link ITypeGenerator}.
   */
  public static ITypeGenerator<?> create() {
    return new TypeGenerator<>();
  }

  /**
   * Creates a new {@link ITypeGenerator} based on the given {@link IType}.
   *
   * @param type
   *          The {@link IType} that should be converted to an {@link ITypeGenerator}. Must not be {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the type to a
   *          working copy. May be {@code null} if no custom transformation is required and the type should be converted
   *          into a working copy without any modification.
   * @return A new {@link ITypeGenerator} initialized to generate source that is structurally similar to the one from
   *         the given {@link IType}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static ITypeGenerator<?> create(IType type, IWorkingCopyTransformer transformer) {
    return new TypeGenerator<>(type, transformer).setDeclaringFullyQualifiedName(type.qualifier());
  }

  @Override
  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    return JavaElementCommentBuilder.createForType(builder, this);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    var currentValidator = builder.context().validator();
    try {
      currentValidator.runWithImportCollector(() -> buildType(builder), inner -> new EnclosingTypeScopedImportCollector(inner, this));
    }
    finally {
      builder.context().properties().setProperty(HIERARCHY_TYPE_KEY, null);
    }
  }

  protected void buildType(IJavaSourceBuilder<?> builder) {
    buildTypeDeclaration(MemberBuilder.create(builder));
    builder
        .space()
        .blockStart().nl();
    buildTypeBody(builder);
    builder
        .nl()
        .blockEnd();
  }

  protected void buildTypeDeclaration(IMemberBuilder<?> builder) {
    // flags
    var flags = flags();
    var isInterface = isInterface(flags);
    builder.appendFlags(flags);

    // type definition
    if (isAnnotation(flags)) {
      builder.at().append("interface ");
    }
    else if (isEnum(flags)) {
      builder.append("enum ");
    }
    else if (isInterface) {
      builder.append("interface ");
    }
    else {
      builder.append("class ");
    }
    builder.append(elementName(builder.context()).orElseThrow(() -> newFail("Type must have a name.")));

    // type parameters
    builder.append(typeParameters(), "<", ", ", ">");

    // super type
    if (!isInterface) {
      superClassFunc()
          .map(af -> af.apply(builder.context()))
          .filter(sup -> !Object.class.getName().equals(sup))
          .ifPresent(sup -> builder.append(" extends ").ref(sup));
    }

    // interfaces
    var ifcReferences = interfacesFunc()
        .map(af -> af.apply(builder.context()))
        .distinct();
    var prefix = " " + (isInterface ? JavaTypes.EXTENDS : JavaTypes.IMPLEMENTS) + " ";
    builder.references(ifcReferences, prefix, ", ", null);
  }

  protected void buildTypeBody(IJavaSourceBuilder<?> builder) {
    if (isInterface(flags())) {
      methods().forEach(m -> m.withFlags(Flags.AccInterface).withoutFlags(Flags.AccPublic));
    }

    builder.append(
        Stream.concat(m_members.stream(), buildUnimplementedMethods(builder.context()))
            .sorted()
            .map(SortedMemberEntry::generator),
        null, builder.context().lineDelimiter(), null);
  }

  protected Stream<SortedMemberEntry> buildUnimplementedMethods(IJavaBuilderContext context) {
    if (!isWithAllMethodsImplemented()) {
      return Stream.empty();
    }
    var orderFunction = Objects.requireNonNullElseGet(m_unimplementedMethodSortOrderProvider, () -> g -> null);
    return UnimplementedMethodGenerator.create(this, context, m_unimplementedMethodsTransformer)
        .map(g -> new SortedMemberEntry(g, orderFunction.apply(g)));
  }

  @Override
  public TYPE asInterface() {
    return withFlags(Flags.AccInterface);
  }

  @Override
  public TYPE asAbstract() {
    return withFlags(Flags.AccAbstract);
  }

  @Override
  public TYPE asAnnotationType() {
    return withFlags(Flags.AccAnnotation);
  }

  @Override
  public TYPE asEnum() {
    return withFlags(Flags.AccEnum);
  }

  @Override
  public Stream<String> interfaces() {
    return interfacesFunc()
        .map(JavaBuilderContextFunction::apply)
        .flatMap(Optional::stream);
  }

  @Override
  public Stream<JavaBuilderContextFunction<String>> interfacesFunc() {
    return m_interfaces.stream();
  }

  @Override
  public TYPE withInterface(String interfaceReference) {
    if (Strings.hasText(interfaceReference)) {
      m_interfaces.add(JavaBuilderContextFunction.create(interfaceReference));
    }
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withInterfaceFrom(Class<A> apiDefinition, Function<A, String> interfaceSupplier) {
    if (interfaceSupplier != null) {
      m_interfaces.add(new ApiFunction<>(apiDefinition, interfaceSupplier));
    }
    return thisInstance();
  }

  @Override
  public TYPE withInterfaceFunc(Function<IJavaBuilderContext, String> interfaceSupplier) {
    if (interfaceSupplier != null) {
      m_interfaces.add(JavaBuilderContextFunction.create(interfaceSupplier));
    }
    return thisInstance();
  }

  @Override
  public TYPE withInterfaces(Stream<String> interfaceReferences) {
    Ensure.notNull(interfaceReferences).forEach(this::withInterface);
    return thisInstance();
  }

  @Override
  public TYPE withoutInterface(String toRemove) {
    return withoutInterface(f -> f.apply()
        .filter(isEqual(toRemove))
        .isPresent());
  }

  @Override
  public TYPE withoutInterface(Predicate<JavaBuilderContextFunction<String>> filter) {
    if (filter == null) {
      m_interfaces.clear();
    }
    else {
      m_interfaces.removeIf(filter);
    }
    return thisInstance();
  }

  @Override
  public Optional<String> superClass() {
    return superClassFunc().flatMap(JavaBuilderContextFunction::apply);
  }

  @Override
  public Optional<String> superClass(IJavaBuilderContext context) {
    return superClassFunc().map(f -> f.apply(context));
  }

  @Override
  public Optional<JavaBuilderContextFunction<String>> superClassFunc() {
    return Optional.ofNullable(m_superClass);
  }

  @Override
  public TYPE withSuperClass(String superType) {
    m_superClass = JavaBuilderContextFunction.orNull(superType);
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withSuperClassFrom(Class<A> apiDefinition, Function<A, String> superClassSupplier) {
    m_superClass = new ApiFunction<>(apiDefinition, superClassSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withSuperClassFunc(Function<IJavaBuilderContext, String> superClassSupplier) {
    m_superClass = JavaBuilderContextFunction.orNull(superClassSupplier);
    return thisInstance();
  }

  @Override
  public String fullyQualifiedName() {
    return m_fullyQualifiedName.computeIfAbsentAndGet(this::buildFullyQualifiedName);
  }

  @Override
  public String qualifier() {
    return m_qualifier.computeIfAbsentAndGet(() -> buildQualifier()[0]);
  }

  protected String[] buildQualifier() {
    //noinspection RedundantExplicitVariableType
    IJavaElementGenerator<?> parent = declaringGenerator().orElse(null);
    if (parent instanceof ITypeGenerator<?>) {
      var declaringTypeFqn = ((ITypeGenerator<?>) parent).fullyQualifiedName();
      return new String[]{declaringTypeFqn, "$"};
    }

    if (parent instanceof ICompilationUnitGenerator<?>) {
      var packageName = ((ICompilationUnitGenerator<?>) parent).packageName();
      return packageName
          .map(s -> new String[]{s, "."})
          .orElseGet(() -> new String[]{"", ""});
    }

    var declaringFqn = getDeclaringFullyQualifiedName()
        .orElseThrow(() -> newFail("Cannot calculate the fully qualified name of generator '{}' if no parent context is available.", elementName().orElse(null)));
    return new String[]{declaringFqn, null};
  }

  protected String buildFullyQualifiedName() {
    var buildQualifier = buildQualifier();
    var qualifier = buildQualifier[0];
    var delimiter = buildQualifier[1];
    if (delimiter == null) {
      var lastDotPos = qualifier.lastIndexOf(JavaTypes.C_DOT);
      var isInnerType = lastDotPos > 0 && Character.isUpperCase(qualifier.charAt(lastDotPos + 1));
      if (isInnerType) {
        delimiter = "$";
      }
      else {
        delimiter = ".";
      }
    }

    return new StringBuilder(qualifier)
        .append(delimiter)
        .append(elementName().orElseThrow(() -> newFail("Cannot calculate the fully qualified name if the class name (elementName) is not set.")))
        .toString();
  }

  @Override
  public Stream<IFieldGenerator<?>> fields() {
    return m_members.stream()
        .filter(SortedMemberEntry::isField)
        .map(SortedMemberEntry::generator)
        .map(g -> (IFieldGenerator<?>) g);
  }

  @Override
  public TYPE withField(IFieldGenerator<?> generator, Object... sortObject) {
    m_members.add(new SortedMemberEntry(applyConnection(generator, this), sortObject));
    return thisInstance();
  }

  @Override
  public TYPE withoutField(Predicate<IFieldGenerator<?>> removalFilter) {
    removeMemberIf(IFieldGenerator.class, removalFilter);
    return thisInstance();
  }

  @SuppressWarnings("unchecked")
  protected <T extends IMemberGenerator<?>, P extends IMemberGenerator<?>> void removeMemberIf(Class<T> type, Predicate<P> removalFilter) {
    for (var it = m_members.iterator(); it.hasNext();) {
      var entry = it.next();
      if (entry.hasType(type)) {
        var generator = (P) entry.generator();
        if (removalFilter == null || removalFilter.test(generator)) {
          it.remove();
          applyConnection(generator, null);
        }
      }
    }
  }

  @Override
  public Stream<IMethodGenerator<?, ?>> methods() {
    return m_members.stream()
        .filter(SortedMemberEntry::isMethod)
        .map(SortedMemberEntry::generator)
        .map(g -> (IMethodGenerator<?, ?>) g);
  }

  @Override
  public TYPE withMethod(IMethodGenerator<?, ?> builder, Object... sortObject) {
    m_members.add(new SortedMemberEntry(applyConnection(builder, this), sortObject));
    return thisInstance();
  }

  @Override
  public TYPE withoutMethod(Predicate<IMethodGenerator<?, ?>> removalFilter) {
    removeMemberIf(IMethodGenerator.class, removalFilter);
    return thisInstance();
  }

  @Override
  public TYPE withoutMethod(String identifier, IJavaBuilderContext context) {
    this.removeMemberIf(IMethodGenerator.class, m -> Objects.equals(((IMethodGenerator<?, ?>) m).identifier(context), identifier));
    return thisInstance();
  }

  @Override
  public Optional<IMethodGenerator<?, ?>> method(String methodId, IJavaBuilderContext context, boolean includeTypeArguments) {
    Ensure.notBlank(methodId);
    return methods()
        .filter(m -> methodId.equals(m.identifier(context, includeTypeArguments)))
        .findAny();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Stream<ITypeGenerator<?>> types() {
    return m_members.stream()
        .filter(SortedMemberEntry::isType)
        .map(SortedMemberEntry::generator)
        .map(g -> (ITypeGenerator<? extends IMemberBuilder<?>>) g);
  }

  @Override
  public Optional<ITypeGenerator<?>> type(String simpleName) {
    Ensure.notBlank(simpleName);
    return types()
        .filter(t -> simpleName.equals(t.elementName().orElse(null)))
        .findAny();
  }

  @Override
  public TYPE withoutAllMethodsImplemented() {
    m_addAllNecessaryMethods = false;
    m_unimplementedMethodsTransformer = null;
    m_unimplementedMethodSortOrderProvider = null;
    return thisInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented() {
    return withAllMethodsImplemented(null);
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded) {
    return withAllMethodsImplemented(callbackForMethodsAdded, null);
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded, Function<IMethodGenerator<?, ?>, Object[]> methodSortOrderProvider) {
    m_addAllNecessaryMethods = true;
    m_unimplementedMethodsTransformer = callbackForMethodsAdded;
    m_unimplementedMethodSortOrderProvider = methodSortOrderProvider;
    return thisInstance();
  }

  @Override
  public boolean isWithAllMethodsImplemented() {
    return m_addAllNecessaryMethods;
  }

  @Override
  public TYPE withType(ITypeGenerator<?> generator, Object... sortObject) {
    Ensure.isFalse(generator instanceof ICompilationUnitGenerator<?>,
        "A {} cannot be added as nested type. Use a {} instead.", PrimaryTypeGenerator.class.getSimpleName(), TypeGenerator.class.getSimpleName());
    m_members.add(new SortedMemberEntry(applyConnection(generator, this), sortObject));
    return thisInstance();
  }

  @Override
  public TYPE withoutType(String simpleName) {
    return withoutType(t -> Objects.equals(simpleName, t.elementName().orElse(null)));
  }

  @Override
  public TYPE withoutType(Predicate<ITypeGenerator<?>> removalFilter) {
    removeMemberIf(ITypeGenerator.class, removalFilter);
    return thisInstance();
  }

  protected static <T extends IMemberGenerator<?>> T applyConnection(T child, IJavaElementGenerator<?> parent) {
    if (child instanceof AbstractMemberGenerator) {
      ((AbstractMemberGenerator<?>) child).withDeclaringGenerator(parent);
    }
    return child;
  }

  @Override
  public Stream<ITypeParameterGenerator<?>> typeParameters() {
    return m_typeParameters.stream();
  }

  @Override
  public TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter) {
    m_typeParameters.add(Ensure.notNull(typeParameter));
    return thisInstance();
  }

  @Override
  public TYPE withoutTypeParameter(String elementName) {
    Ensure.notNull(elementName);
    m_typeParameters.removeIf(generator -> elementName.equals(generator.elementName().orElse(null)));
    return thisInstance();
  }

  @Override
  public Optional<String> getDeclaringFullyQualifiedName() {
    return Strings.notBlank(m_declaringFullyQualifiedName);
  }

  @Override
  public TYPE setDeclaringFullyQualifiedName(String parentFullyQualifiedName) {
    m_declaringFullyQualifiedName = parentFullyQualifiedName;
    return thisInstance();
  }

  @Override
  public IType getHierarchyType(IJavaBuilderContext context) {
    return context.properties().computeIfAbsent(HIERARCHY_TYPE_KEY, k -> createHierarchyType(context));
  }

  protected IType createHierarchyType(IJavaBuilderContext context) {
    var javaEnvironment = context.environment().orElseThrow(() -> newFail("Cannot override a method without Java environment."));
    var targetPackage = "not.existing.scout.sdk.tmp_pck";
    var typeName = "ScoutSdkTempClass__" + HIERARCHY_TYPE_COUNTER.getAndIncrement();
    var hierarchyTypeGenerator = PrimaryTypeGenerator.create()
        .withPackageName(targetPackage)
        .withElementName(typeName)
        .withInterfaces(interfacesFunc()
            .map(af -> af.apply(context)));
    superClassFunc()
        .map(af -> af.apply(context))
        .ifPresent(hierarchyTypeGenerator::withSuperClass);
    var hierarchyTypeSource = hierarchyTypeGenerator.toJavaSource(javaEnvironment);
    javaEnvironment.registerCompilationUnitOverride(hierarchyTypeSource, targetPackage, typeName + JavaTypes.JAVA_FILE_SUFFIX);
    return javaEnvironment.requireType(targetPackage + JavaTypes.C_DOT + typeName);
  }

  private static class UnimplementedMethodGenerator extends MethodOverrideGenerator<UnimplementedMethodGenerator, IMethodBodyBuilder<?>> {

    protected UnimplementedMethodGenerator(IWorkingCopyTransformer transformer) {
      super(transformer);
    }

    protected static Stream<IMethodGenerator<?, ?>> create(ITypeGenerator<?> typeGenerator, IJavaBuilderContext context, IWorkingCopyTransformer transformer) {
      return getUnimplementedMethods(typeGenerator.getHierarchyType(context))
          .map(m -> toMethodGenerator(typeGenerator, m, transformer));
    }

    protected static IMethodGenerator<?, ?> toMethodGenerator(IJavaElementGenerator<?> typeGenerator, IMethod unimplementedMethod, IWorkingCopyTransformer transformer) {
      var generator = new UnimplementedMethodGenerator(transformer)
          .withDeclaringGenerator(typeGenerator)
          .withElementName(unimplementedMethod.elementName());
      // add parameters in case the method has an overload
      unimplementedMethod
          .parameters().stream()
          .map(UnimplementedMethodGenerator::toMethodParamGenerator)
          .forEach(generator::withParameter);
      return generator;
    }

    protected static IMethodParameterGenerator<?> toMethodParamGenerator(IMethodParameter model) {
      return MethodParameterGenerator.create()
          .withDataType(model.dataType().name());
    }

    protected static Stream<IMethod> getUnimplementedMethods(IType type) {
      var abstractMethodIds = type.methods().withSuperTypes(true).stream()
          .filter(method -> !isDefaultMethod(method.flags()) && !Flags.isStatic(method.flags()))
          .filter(method -> isAbstract(method.flags()) || isInterface(method.flags()) || isInterface(method.requireDeclaringType().flags()))
          .collect(toMap(IMethod::identifier, identity(), (u, v) -> u, LinkedHashMap::new));

      // remove all implemented methods
      type.methods().withSuperClasses(true).stream()
          .filter(m -> !isPrivate(m.flags()) && !isAbstract(m.flags()))
          .map(IMethod::identifier)
          .forEach(abstractMethodIds::remove);

      return abstractMethodIds.values().stream();
    }
  }
}
