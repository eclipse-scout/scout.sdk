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

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformField;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformMethod;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformType;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformTypeParameter;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAnnotation;
import static org.eclipse.scout.sdk.core.model.api.Flags.isDefaultMethod;
import static org.eclipse.scout.sdk.core.model.api.Flags.isEnum;
import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPrivate;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.MemberBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.imports.EnclosingTypeScopedImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
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

  private final List<ITypeParameterGenerator<?>> m_typeParameters;
  private final Set<String> m_interfaces;
  private final List<SortedMemberEntry> m_members;
  private final FinalValue<String> m_fullyQualifiedName;
  private final FinalValue<String> m_qualifier;
  private String m_superType;
  private String m_declaringFullyQualifiedName;
  private IJavaElementGenerator<?> m_declaringGenerator;
  private boolean m_addAllNecessaryMehtods;
  private IWorkingCopyTransformer m_unimplementedMethodsTransformer;

  protected TypeGenerator() {
    m_typeParameters = new ArrayList<>();
    m_interfaces = new LinkedHashSet<>();
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
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toList());

    m_superType = type.superClass()
        .map(IType::reference)
        .orElse(null);

    m_interfaces = type.superInterfaces()
        .map(IType::reference)
        .collect(toCollection(LinkedHashSet::new));

    m_members = new ArrayList<>();
    type.fields().stream()
        .map(f -> transformField(f, transformer)
            .map(g -> new SortedMemberEntry(g, f)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toCollection(() -> m_members));
    type.methods().stream()
        .map(m -> transformMethod(m, transformer)
            .map(g -> new SortedMemberEntry(g, m)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toCollection(() -> m_members));
    type.innerTypes().stream()
        .map(t -> transformType(t, transformer)
            .map(g -> new SortedMemberEntry(g, t)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .peek(s -> applyConnection(s.generator(), this))
        .collect(toCollection(() -> m_members));

    String declaringFqn = type.declaringType()
        .map(IType::name)
        .orElseGet(() -> type.compilationUnit()
            .map(ICompilationUnit::containingPackage)
            .flatMap(pck -> Strings.notBlank(pck.elementName()))
            .orElse(""));

    setDeclaringFullyQualifiedName(declaringFqn);
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

  /**
   * @return A new empty {@link ITypeGenerator}.
   */
  public static ITypeGenerator<?> create() {
    return new TypeGenerator<>();
  }

  @Override
  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    return JavaElementCommentBuilder.createForType(builder, this);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    IImportValidator currentValidator = builder.context().validator();
    currentValidator.runWithImportCollector(() -> buildType(builder), inner -> new EnclosingTypeScopedImportCollector(inner, this));
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
    int flags = flags();
    boolean isInterface = isInterface(flags);
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
    builder.append(elementName().orElseThrow(() -> newFail("Type must have a name.")));

    // type parameters
    builder.append(typeParameters(), "<", ", ", ">");

    // super type
    superClass()
        .filter(sup -> !isInterface)
        .filter(sup -> !Object.class.getName().equals(sup))
        .ifPresent(sup -> builder.append(" extends ").ref(sup));

    // interfaces
    String prefix = isInterface ? " extends " : " implements ";
    builder.appendReferences(interfaces(), prefix, ", ", null);
  }

  protected void buildTypeBody(IJavaSourceBuilder<?> builder) {
    if (isInterface(flags())) {
      methods().forEach(m -> m.withFlags(Flags.AccInterface).withoutFlags(Flags.AccPublic));
    }

    Stream<ISourceGenerator<ISourceBuilder<?>>> memberSource = m_members.stream()
        .sorted()
        .map(SortedMemberEntry::generator);
    builder.append(memberSource, null, builder.context().lineDelimiter(), null);
    if (isWithAllMethodsImplemented()) {
      buildUnimplementedMethods(builder);
    }
  }

  protected void buildUnimplementedMethods(IJavaSourceBuilder<?> builder) {
    Stream<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> unimplementedMethods =
        UnimplementedMethodGenerator.create(this, builder.context().environment().orElseThrow(() -> newFail("Unimplemented methods can only be added if running with a java environment.")), m_unimplementedMethodsTransformer)
            .sorted(comparing(g -> g.elementName().orElse("")));
    String startDelim;
    if (m_members.size() == 1) {
      startDelim = builder.context().lineDelimiter();
    }
    else {
      startDelim = null;
    }
    builder.append(unimplementedMethods, startDelim, builder.context().lineDelimiter(), null);
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
    return m_interfaces.stream();
  }

  @Override
  public TYPE withInterface(String interfaceReference) {
    m_interfaces.add(Ensure.notBlank(interfaceReference));
    return currentInstance();
  }

  @Override
  public TYPE withInterfaces(Stream<String> interfaceReferences) {
    Ensure.notNull(interfaceReferences).forEach(this::withInterface);
    return currentInstance();
  }

  @Override
  public TYPE withoutInterface(String interfaceReference) {
    m_interfaces.remove(Ensure.notBlank(interfaceReference));
    return currentInstance();
  }

  @Override
  public Optional<String> superClass() {
    return Strings.notBlank(m_superType);
  }

  @Override
  public TYPE withSuperClass(String superType) {
    m_superType = superType;
    return currentInstance();
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
    IJavaElementGenerator<?> parent = declaringGenerator().orElse(null);
    if (parent instanceof ITypeGenerator<?>) {
      String declaringTypeFqn = ((ITypeGenerator<?>) parent).fullyQualifiedName();
      return new String[]{declaringTypeFqn, "$"};
    }

    if (parent instanceof ICompilationUnitGenerator<?>) {
      Optional<String> packageName = ((ICompilationUnitGenerator<?>) parent).packageName();
      return packageName
          .map(s -> new String[]{s, "."})
          .orElseGet(() -> new String[]{"", ""});
    }

    String declaringFqn = getDeclaringFullyQualifiedName()
        .orElseThrow(() -> newFail("Cannot calculate the fully qualified name of generator '{}' if no parent context is available.", elementName().orElse(null)));
    return new String[]{declaringFqn, null};
  }

  protected String buildFullyQualifiedName() {
    String[] buildQualifier = buildQualifier();
    String qualifier = buildQualifier[0];
    String delimiter = buildQualifier[1];
    if (delimiter == null) {
      int lastDotPos = qualifier.lastIndexOf(JavaTypes.C_DOT);
      boolean isInnerType = lastDotPos > 0 && lastDotPos < qualifier.length() && Character.isUpperCase(qualifier.charAt(lastDotPos + 1));
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
  public TYPE withField(IFieldGenerator<?> builder, Object... sortObject) {
    m_members.add(new SortedMemberEntry(builder, sortObject));
    return currentInstance();
  }

  @Override
  public TYPE withoutField(String elementName) {
    removeMember(IFieldGenerator.class, elementName);
    return currentInstance();
  }

  @SuppressWarnings("unchecked")
  protected <T extends IMemberGenerator<?>> T removeMember(Class<T> type, String memberName) {
    Ensure.notNull(memberName);
    for (Iterator<SortedMemberEntry> it = m_members.iterator(); it.hasNext();) {
      SortedMemberEntry entry = it.next();
      if (entry.hasType(type) && memberName.equals(entry.generator().elementName().orElse(null))) {
        it.remove();
        return (T) entry.generator();
      }
    }
    return null;
  }

  @Override
  public Stream<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> methods() {
    return m_members.stream()
        .filter(SortedMemberEntry::isMethod)
        .map(SortedMemberEntry::generator)
        .map(g -> (IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>) g);
  }

  @Override
  public TYPE withMethod(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> builder, Object... sortObject) {
    m_members.add(new SortedMemberEntry(applyConnection(builder, this), sortObject));
    return currentInstance();
  }

  @Override
  public TYPE withoutMethod(String elementName) {
    applyConnection(removeMember(IMethodGenerator.class, elementName), null);
    return currentInstance();
  }

  @Override
  public Optional<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> method(String methodId) {
    Ensure.notBlank(methodId);
    return methods()
        .filter(m -> methodId.equals(m.identifier()))
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
    m_addAllNecessaryMehtods = false;
    m_unimplementedMethodsTransformer = null;
    return currentInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented(IWorkingCopyTransformer callbackForMethodsAdded) {
    m_addAllNecessaryMehtods = true;
    m_unimplementedMethodsTransformer = callbackForMethodsAdded;
    return currentInstance();
  }

  @Override
  public TYPE withAllMethodsImplemented() {
    return withAllMethodsImplemented(null);
  }

  @Override
  public boolean isWithAllMethodsImplemented() {
    return m_addAllNecessaryMehtods;
  }

  @Override
  public TYPE withType(ITypeGenerator<?> generator, Object... sortObject) {
    Ensure.isFalse(generator instanceof ICompilationUnitGenerator<?>,
        "A {} cannot be added as nested type. Use a {} instead.", PrimaryTypeGenerator.class.getSimpleName(), TypeGenerator.class.getSimpleName());
    m_members.add(new SortedMemberEntry(applyConnection(generator, this), sortObject));
    return currentInstance();
  }

  @Override
  public TYPE withoutType(String elementName) {
    applyConnection(removeMember(ITypeGenerator.class, elementName), null);
    return currentInstance();
  }

  protected static <T extends IMemberGenerator<?>> T applyConnection(T child, ITypeGenerator<?> parent) {
    if (child instanceof TypeGenerator) {
      ((TypeGenerator<?>) child).withDeclaringGenerator(parent);
    }
    if (child instanceof MethodOverrideGenerator) {
      ((MethodOverrideGenerator<?, ?>) child).withDeclaringGenerator(parent);
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
    return currentInstance();
  }

  @Override
  public TYPE withoutTypeParameter(String elementName) {
    Ensure.notNull(elementName);
    m_typeParameters.removeIf(generator -> elementName.equals(generator.elementName().orElse(null)));
    return currentInstance();
  }

  @Override
  public Optional<IJavaElementGenerator<?>> declaringGenerator() {
    return Optional.ofNullable(m_declaringGenerator);
  }

  public TYPE withDeclaringGenerator(IJavaElementGenerator<?> parent) {
    m_declaringGenerator = parent;
    return currentInstance();
  }

  @Override
  public Optional<String> getDeclaringFullyQualifiedName() {
    return Optional.ofNullable(m_declaringFullyQualifiedName);
  }

  @Override
  public TYPE setDeclaringFullyQualifiedName(String parentFullyQualifiedName) {
    m_declaringFullyQualifiedName = parentFullyQualifiedName;
    return currentInstance();
  }

  private static class UnimplementedMethodGenerator extends MethodOverrideGenerator<UnimplementedMethodGenerator, IMethodBodyBuilder<?>> {

    protected UnimplementedMethodGenerator(IWorkingCopyTransformer transformer) {
      super(transformer);
    }

    protected static Stream<IMethodGenerator<?, ? extends IMethodBodyBuilder<?>>> create(ITypeGenerator<?> typeGenerator, IJavaEnvironment env, IWorkingCopyTransformer transformer) {
      return callWithTmpType(typeGenerator, env, tmpType -> getUnimplementedMethods(tmpType)
          .map(m -> toMethodGenerator(typeGenerator, m, transformer)));
    }

    protected static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> toMethodGenerator(ITypeGenerator<?> typeGenerator, IMethod unimplementedMethod, IWorkingCopyTransformer transformer) {
      UnimplementedMethodGenerator generator = new UnimplementedMethodGenerator(transformer)
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
      Map<String, IMethod> abstractMethodIds = type.methods().withSuperTypes(true).stream()
          .filter(method -> !isDefaultMethod(method.flags()) && !Flags.isStatic(method.flags()))
          .filter(method -> isAbstract(method.flags()) || isInterface(method.flags()) || isInterface(method.requireDeclaringType().flags()))
          .collect(toMap(m -> m.identifier(true), identity(), (u, v) -> u));

      // remove all implemented methods
      type.methods().withSuperClasses(true).stream()
          .filter(m -> !isPrivate(m.flags()) && !isAbstract(m.flags()))
          .map(m -> m.identifier(true))
          .forEach(abstractMethodIds::remove);

      return abstractMethodIds.values().stream();
    }
  }
}
