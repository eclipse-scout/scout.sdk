/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.sourceMethodOf;
import static org.eclipse.scout.sdk.core.log.SdkLog.onTrace;
import static org.eclipse.scout.sdk.core.util.Ensure.fail;

import java.nio.CharBuffer;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.ecj.SourcePositionComparators.MethodBindingComparator;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JavaEnvironmentWithEcj}</h3>
 *
 * @since 5.1.0
 */
public class JavaEnvironmentWithEcj extends AbstractJavaEnvironment implements AutoCloseable {

  // arguments (survives reinitialization)
  private final Path m_javaHome;
  private final CompilerOptions m_options; // may be null
  private final Collection<? extends ClasspathEntry> m_rawClassPath;

  // caches
  private final Map<Object, JavaElementSpi> m_elements; // all except TypeSpi. Types are stored in super class
  private final Map<ReferenceBinding, Map<String, ElementValuePair>> m_evpCache; // cache for annotation elements
  private final Map<TypeBinding, Map<String, MemberValuePair>> m_mvpCache; // cache for annotation elements
  private final Map<CharBuffer /* file path */, char[] /* source contents */> m_sourceCache; // cache for file source
  private FinalValue<FileSystemWithOverride> m_fs;
  private FinalValue<EcjAstCompiler> m_compiler;
  private FinalValue<List<ClasspathSpi>> m_classpath;
  private FileSystemWithOverride m_oldFsDuringReload;

  // state
  private volatile boolean m_initialized;

  protected JavaEnvironmentWithEcj(Path javaHome, Collection<? extends ClasspathEntry> classpath, CompilerOptions options) {
    m_javaHome = javaHome;
    m_options = options;
    m_rawClassPath = withoutNullElements(classpath);

    m_elements = new ConcurrentHashMap<>();
    m_evpCache = new ConcurrentHashMap<>();
    m_mvpCache = new ConcurrentHashMap<>();
    m_sourceCache = new HashMap<>(); // normal map is ok: all access is synchronized
    m_fs = new FinalValue<>();
    m_compiler = new FinalValue<>();
    m_classpath = new FinalValue<>();
    m_initialized = true;
  }

  protected static <T> List<T> withoutNullElements(Collection<T> list) {
    if (list == null || list.isEmpty()) {
      return emptyList();
    }
    return list.stream()
        .filter(Objects::nonNull)
        .collect(toList());
  }

  @Override
  protected TypeSpi doFindType(String fqn) {
    assertInitialized();
    var desc = TypeNameDescriptor.of(fqn);
    if (desc.getArrayDimension() <= 0) {
      return resolveAsType(desc);
    }
    return resolveAsArray(desc);
  }

  protected TypeSpi resolveAsType(TypeNameDescriptor desc) {
    var binding = lookupTypeBinding(desc.getPrimaryTypeName());
    if (binding == null) {
      return null;
    }
    var result = bindingToType(this, binding, () -> {
      var type = (AbstractTypeWithEcj) findType(desc.getFullyQualifiedName());
      if (type == null) {
        return null;
      }
      return type.getInternalBinding();
    });
    if (!desc.hasInnerType()) {
      return result;
    }
    return findInnerType(result, desc.getInnerTypeNames());
  }

  protected static TypeSpi findInnerType(TypeSpi primaryType, String innerTypes) {
    var result = primaryType;
    var st = new StringTokenizer(innerTypes, "$", false);
    while (st.hasMoreTokens()) {
      var name = st.nextToken();
      var innerType = result.getTypes().stream()
          .filter(t -> t.getElementName().equals(name))
          .findFirst()
          .orElse(null);
      if (innerType == null) {
        return null;
      }
      result = innerType;
    }
    return result;
  }

  protected TypeBinding getArrayTypeBinding(TypeNameDescriptor desc) {
    var elementType = resolveAsType(desc);
    if (elementType == null) {
      return null;
    }
    var elementTypeBinding = ((AbstractTypeWithEcj) elementType).getInternalBinding();
    return getCompiler().lookupEnvironment.createArrayType(elementTypeBinding, desc.getArrayDimension());
  }

  protected TypeSpi resolveAsArray(TypeNameDescriptor desc) {
    return bindingToType(this, getArrayTypeBinding(desc), () -> getArrayTypeBinding(desc));
  }

  protected TypeBinding lookupTypeBinding(String fqn) {
    if (fqn.length() <= 7) {
      switch (fqn) {
        case JavaTypes._boolean:
          return TypeBinding.BOOLEAN;
        case JavaTypes._char:
          return TypeBinding.CHAR;
        case JavaTypes._byte:
          return TypeBinding.BYTE;
        case JavaTypes._short:
          return TypeBinding.SHORT;
        case JavaTypes._int:
          return TypeBinding.INT;
        case JavaTypes._long:
          return TypeBinding.LONG;
        case JavaTypes._float:
          return TypeBinding.FLOAT;
        case JavaTypes._double:
          return TypeBinding.DOUBLE;
        case JavaTypes._void:
          return TypeBinding.VOID;
        default:
          // all short class names continue
          break;
      }
    }
    var lookupName = CharOperation.splitOn(JavaTypes.C_DOT, fqn.toCharArray());
    ReferenceBinding binding;
    synchronized (lock()) {
      binding = getCompiler().lookupEnvironment.getType(lookupName);
    }
    if (binding instanceof MissingTypeBinding) {
      return null;
    }
    return binding;
  }

  @Override
  protected Collection<JavaElementSpi> allElements() {
    return m_elements.values();
  }

  /**
   * @return A {@link Path} to the JRE (not JDK!) home. May be {@code null}. In that case the running Java home will be
   *         used.
   */
  public Path javaHome() {
    return m_javaHome;
  }

  @Override
  protected void onReloadStart() {
    runPreservingOverrides(this, this, this::doReloadStart);
  }

  private void doReloadStart() {
    // backup old FS so that it can be closed after reload
    m_oldFsDuringReload = m_fs.get();
    clear(false); // old FS is still used during reload (to find new SPIs). Do not close it here already.
    m_initialized = true;
  }

  protected boolean isInitialized() {
    return m_initialized;
  }

  protected void assertInitialized() {
    if (isInitialized()) {
      return;
    }
    fail("JavaEnvironment has already been closed.");
  }

  @Override
  public void close() {
    runPreservingOverrides(this, this, this::doClose);
  }

  private void doClose() {
    clear(true);
    m_initialized = false;
  }

  private void clear(boolean closeFs) {
    cleanup();
    m_elements.clear();
    m_evpCache.clear();
    m_mvpCache.clear();
    m_sourceCache.clear();

    var oldFs = m_fs.get();
    m_fs = new FinalValue<>(); // remove the current file system before closing it
    if (closeFs && oldFs != null) {
      oldFs.cleanup();
    }
    m_compiler = new FinalValue<>();
    m_classpath = new FinalValue<>();
  }

  @Override
  protected void onReloadEnd() {
    super.onReloadEnd();
    var oldFs = m_oldFsDuringReload;
    if (oldFs != null) {
      oldFs.cleanup();
      m_oldFsDuringReload = null;
    }
  }

  /**
   * Executes the specified {@link Runnable} ensuring that the overridden compilation units of the source environment
   * are also available in the destination environment after the runnable has been executed.
   *
   * @param src
   *          The source environment
   * @param dest
   *          The destination environment
   * @param task
   *          The task. May be {@code null} if nothing should be executed but only the overrides should be copied from
   *          src to dest.
   */
  protected void runPreservingOverrides(JavaEnvironmentWithEcj src, JavaEnvironmentWithEcj dest, Runnable task) {
    synchronized (lock()) {
      var units = new ArrayList<>(src.m_fs.opt() // only obtain overridden compilation units if filesystem has already been created
          .map(FileSystemWithOverride::overrideSupport)
          .map(CompilationUnitOverrideSupport::getCompilationUnits)
          .orElse(Collections.emptyList()));
      if (task != null) {
        task.run();
      }
      if (units.isEmpty()) {
        return;
      }

      var overrideSupport = dest.getNameEnvironment().overrideSupport();
      for (var cu : units) {
        overrideSupport.addCompilationUnit(cu);
      }
    }
  }

  @Override
  public List<String> getCompileErrors(TypeSpi typeSpi) {
    var cuSpi = Ensure.notNull(typeSpi).getCompilationUnit();
    var decl = Ensure.instanceOf(cuSpi, DeclarationCompilationUnitWithEcj.class, "Type '{}' is not a source type.", typeSpi.getName())
        .getInternalCompilationUnitDeclaration();
    synchronized (lock()) {
      return getCompiler().getCompileErrors(decl);
    }
  }

  @Override
  public List<String> getCompileErrors(String fqn) {
    return getCompileErrors(Ensure.notNull(findType(fqn), "Cannot find type '{}'.", fqn));
  }

  @Override
  public boolean registerCompilationUnitOverride(char[] src, CompilationUnitInfo cuInfo) {
    Ensure.notNull(cuInfo);
    Ensure.notNull(src);

    var cu = new StringBasedCompilationUnitWithEcj(cuInfo, src, null /* ModuleBinding.UNNAMED */);
    synchronized (lock()) {
      var reloadRequired = getNameEnvironment().overrideSupport().addCompilationUnit(cu);

      var fqn = cu.getFullyQualifiedName();
      removeTypeFromCache(fqn);// clear cache info for this element
      if (!reloadRequired && isInitialized()) {
        // if not used in name-env: also check in compiler
        reloadRequired = isLoadedInCompiler(fqn, src);
      }

      // ensure the package of the new override CU exists. It may be in the lookupEnv cache as 'notExisting' from a call before where it really did not exist.
      if (!Strings.isEmpty(cuInfo.packageName()) && isInitialized()) {
        getCompiler().lookupEnvironment.createPackage(cu.getPackageName());
      }

      if (reloadRequired) {
        m_sourceCache.keySet().removeIf(cuFileName -> CharOperation.endsWith(cuFileName.array(), cu.getFileName()));
      }

      return reloadRequired;
    }
  }

  private boolean isLoadedInCompiler(String fqn, char[] src) {
    var cachedType = findExistingBindingFor(fqn);
    if (cachedType == null) {
      return false; // has not yet been used. no reload required.
    }

    if (cachedType instanceof SourceTypeBinding) {
      var decl = ((SourceTypeBinding) cachedType).scope.compilationUnitScope().referenceContext;
      var existing = getSource(decl);
      return !Arrays.equals(existing, src); // only reload if source is different
    }
    return true;
  }

  private ReferenceBinding findExistingBindingFor(String fqn) {
    var lookupEnvironment = getCompiler().lookupEnvironment;
    var compoundName = CharOperation.splitOn(JavaTypes.C_DOT, fqn.toCharArray());
    var cachedType = lookupEnvironment.getCachedType(compoundName);
    if (cachedType != null) {
      return cachedType;
    }

    return Arrays.stream(lookupEnvironment.knownModules.valueTable)
        .filter(Objects::nonNull)
        .map(module -> module.environment.getCachedType(compoundName))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * @param cu
   *          the compilation unit to get the source for
   * @return the source of the compilation unit or null. The source is only available if the compilation unit is one of
   *         the following
   *         <ul>
   *         <li>source in workspace</li>
   *         <li>class in jar and source in same jar</li>
   *         <li>class in jar and source attachment to jar is defined</li>
   *         </ul>
   */
  public SourceRange getSource(CompilationUnitSpi cu, int start, int end) {
    if (!(cu instanceof DeclarationCompilationUnitWithEcj)) {
      return null;
    }
    assertInitialized();

    var src = getSource(((DeclarationCompilationUnitWithEcj) cu).getInternalCompilationUnitDeclaration());
    if (src == null) {
      return null;
    }

    return new SourceRange(CharBuffer.wrap(src, start, end - start + 1), start);
  }

  @SuppressWarnings("squid:S1168")
  protected char[] getSource(CompilationUnitDeclaration decl) {
    synchronized (lock()) {
      var sourceUnit = getCompiler().getSource(decl);
      if (sourceUnit == null) {
        return null;
      }

      return m_sourceCache.computeIfAbsent(CharBuffer.wrap(sourceUnit.getFileName()), k -> sourceUnit.getContents());
    }
  }

  protected FileSystemWithOverride getNameEnvironment() {
    return m_fs.computeIfAbsentAndGet(this::buildNameEnvironment);
  }

  private FileSystemWithOverride buildNameEnvironment() {
    // classpath registers a system-wide file system but does not handle the fact that it might already have been created.
    // see org.eclipse.jdt.internal.compiler.batch.ClasspathMultiReleaseJar.initialize
    var cp = new ClasspathBuilder(javaHome(), m_rawClassPath);
    while (true) {
      try {
        // optimistic creation without locking
        return new FileSystemWithOverride(cp);
      }
      catch (FileSystemAlreadyExistsException e) {
        SdkLog.debug("Concurrent registration of process wide filesystem.", onTrace(e));
      }
    }
  }

  /**
   * Only use the compiler under lock of {@link #lock()}!
   */
  private EcjAstCompiler getCompiler() {
    return m_compiler.computeIfAbsentAndGet(() -> new EcjAstCompiler(getNameEnvironment(), m_options, lock()));
  }

  @Override
  public List<ClasspathSpi> getClasspath() {
    return m_classpath.computeIfAbsentAndGet(() -> m_rawClassPath.stream()
        .map(this::classpathEntryToSpi)
        .toList());
  }

  public VoidTypeWithEcj createVoidType() {
    assertInitialized();
    synchronized (lock()) {
      return (VoidTypeWithEcj) m_elements.computeIfAbsent(VoidTypeWithEcj.class, k -> new VoidTypeWithEcj(this));
    }
  }

  public WildcardOnlyTypeWithEcj createWildcardOnlyType() {
    assertInitialized();
    synchronized (lock()) {
      return (WildcardOnlyTypeWithEcj) m_elements.computeIfAbsent(WildcardOnlyTypeWithEcj.class, k -> new WildcardOnlyTypeWithEcj(this));
    }
  }

  public BindingAnnotationWithEcj createBindingAnnotation(AnnotatableSpi owner, AnnotationBinding binding) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(binding, owner); // binding may be shared amongst different owners if it is a marker annotation. Therefore, include the owner in the key.
      return (BindingAnnotationWithEcj) m_elements.computeIfAbsent(key, k -> new BindingAnnotationWithEcj(this, owner, binding));
    }
  }

  public BindingAnnotationElementWithEcj createBindingAnnotationValue(AnnotationSpi owner, ElementValuePair bindingPair, boolean syntheticDefaultValue) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(owner, bindingPair);
      return (BindingAnnotationElementWithEcj) m_elements.computeIfAbsent(key, k -> new BindingAnnotationElementWithEcj(this, owner, bindingPair, syntheticDefaultValue));
    }
  }

  public NullAnnotationElementWithEcj createNullAnnotationValue(AnnotationSpi owner, String name, boolean syntheticDefaultValue) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(NullAnnotationElementWithEcj.class, owner, name);
      return (NullAnnotationElementWithEcj) m_elements.computeIfAbsent(key, k -> new NullAnnotationElementWithEcj(this, owner, name, syntheticDefaultValue));
    }
  }

  public BindingArrayTypeWithEcj createBindingArrayType(ArrayBinding binding, boolean isWildcard, Supplier<ArrayBinding> newElementLookupStrategy) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(binding, isWildcard);
      return (BindingArrayTypeWithEcj) m_elements.computeIfAbsent(key, k -> new BindingArrayTypeWithEcj(this, binding, isWildcard, newElementLookupStrategy));
    }
  }

  public BindingBaseTypeWithEcj createBindingBaseType(BaseTypeBinding binding) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(binding);
      return (BindingBaseTypeWithEcj) m_elements.computeIfAbsent(key, k -> new BindingBaseTypeWithEcj(this, binding));
    }
  }

  public BindingFieldWithEcj createBindingField(AbstractTypeWithEcj declaringType, FieldBinding binding) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(binding);
      return (BindingFieldWithEcj) m_elements.computeIfAbsent(key, k -> new BindingFieldWithEcj(this, declaringType, binding));
    }
  }

  public BindingMethodWithEcj createBindingMethod(BindingTypeWithEcj declaringType, MethodBinding binding) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(binding);
      return (BindingMethodWithEcj) m_elements.computeIfAbsent(key, k -> new BindingMethodWithEcj(this, declaringType, binding));
    }
  }

  public BindingMethodParameterWithEcj createBindingMethodParameter(BindingMethodWithEcj declaringMethod, TypeBinding binding, char[] name, int flags, int index) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(BindingMethodParameterWithEcj.class, declaringMethod, binding, index);
      return (BindingMethodParameterWithEcj) m_elements.computeIfAbsent(key, k -> new BindingMethodParameterWithEcj(this, declaringMethod, binding, name, flags, index));
    }
  }

  public BindingTypeWithEcj createBindingType(ReferenceBinding binding, TypeSpi declaringType, boolean isWildcard, Supplier<? extends ReferenceBinding> newElementLookupStrategy) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(binding, isWildcard);
      return (BindingTypeWithEcj) m_elements.computeIfAbsent(key, k -> new BindingTypeWithEcj(this, binding, declaringType, isWildcard, newElementLookupStrategy));
    }
  }

  public BindingTypeParameterWithEcj createBindingTypeParameter(AbstractMemberWithEcj<?> declaringMember, TypeVariableBinding binding, int index) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(declaringMember, binding, index);
      return (BindingTypeParameterWithEcj) m_elements.computeIfAbsent(key, k -> new BindingTypeParameterWithEcj(this, declaringMember, binding, index));
    }
  }

  public DeclarationAnnotationWithEcj createDeclarationAnnotation(AnnotatableSpi owner, Annotation astNode) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationAnnotationWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationAnnotationWithEcj(this, owner, astNode));
    }
  }

  public DeclarationAnnotationElementWithEcj createDeclarationAnnotationValue(AnnotationSpi declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationAnnotationElementWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationAnnotationElementWithEcj(this, declaringAnnotation, astNode, syntheticDefaultValue));
    }
  }

  public DeclarationCompilationUnitWithEcj createDeclarationCompilationUnit(CompilationUnitDeclaration astNode) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationCompilationUnitWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationCompilationUnitWithEcj(this, astNode));
    }
  }

  public DeclarationFieldWithEcj createDeclarationField(DeclarationTypeWithEcj declaringType, FieldDeclaration astNode) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationFieldWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationFieldWithEcj(this, declaringType, astNode));
    }
  }

  public DeclarationImportWithEcj createDeclarationImport(DeclarationCompilationUnitWithEcj owner, ImportReference astNode) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationImportWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationImportWithEcj(this, owner, astNode));
    }
  }

  public DeclarationMethodWithEcj createDeclarationMethod(DeclarationTypeWithEcj declaringType, AbstractMethodDeclaration astNode) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationMethodWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationMethodWithEcj(this, declaringType, astNode));
    }
  }

  public DeclarationMethodParameterWithEcj createDeclarationMethodParameter(DeclarationMethodWithEcj declaringMethod, Argument astNode, int index) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationMethodParameterWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationMethodParameterWithEcj(this, declaringMethod, astNode, index));
    }
  }

  public DeclarationTypeWithEcj createDeclarationType(CompilationUnitSpi cu, DeclarationTypeWithEcj declaringType, TypeDeclaration astNode) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationTypeWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationTypeWithEcj(this, cu, declaringType, astNode));
    }
  }

  public DeclarationTypeParameterWithEcj createDeclarationTypeParameter(AbstractMemberWithEcj<?> declaringMember, TypeParameter astNode, int index) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(astNode);
      return (DeclarationTypeParameterWithEcj) m_elements.computeIfAbsent(key, k -> new DeclarationTypeParameterWithEcj(this, declaringMember, astNode, index));
    }
  }

  public PackageWithEcj createPackage(String name) {
    assertInitialized();
    synchronized (lock()) {
      var key = new CompositeObject(PackageWithEcj.class, name);
      return (PackageWithEcj) m_elements.computeIfAbsent(key, k -> new PackageWithEcj(this, name));
    }
  }

  public PackageWithEcj createDefaultPackage() {
    return createPackage(null);
  }

  @Override
  public PackageSpi getPackage(String name) {
    return createPackage(name);
  }

  public SyntheticCompilationUnitWithEcj createSyntheticCompilationUnit(BindingTypeWithEcj mainType) {
    assertInitialized();
    synchronized (lock()) {
      var key = new SameCompositeObject(SyntheticCompilationUnitWithEcj.class, mainType);
      return (SyntheticCompilationUnitWithEcj) m_elements.computeIfAbsent(key, k -> new SyntheticCompilationUnitWithEcj(this, mainType));
    }
  }

  /**
   * @return the (cached) default values {@link AnnotationElementSpi#isDefaultValue()} for the annotation in correct
   *         source order of the annotation type declaration
   */
  public Map<String, ElementValuePair> getBindingAnnotationSyntheticDefaultValues(ReferenceBinding annotationType) {
    assertInitialized();
    synchronized (lock()) {
      return m_evpCache.computeIfAbsent(annotationType, JavaEnvironmentWithEcj::computeBindingAnnotationSyntheticDefaultValues);
    }
  }

  protected static Map<String, ElementValuePair> computeBindingAnnotationSyntheticDefaultValues(ReferenceBinding annotationType) {
    var valueMethods = annotationType.methods();
    if (valueMethods == null || valueMethods.length < 1) {
      return emptyMap();
    }

    valueMethods = Arrays.copyOf(valueMethods, valueMethods.length);
    Arrays.sort(valueMethods, MethodBindingComparator.INSTANCE);

    Map<String, ElementValuePair> defaultValues = new LinkedHashMap<>(valueMethods.length);
    for (var mb : valueMethods) {
      var name = new String(mb.selector);
      var value = mb.getDefaultValue();
      if (value != null) {
        defaultValues.put(name, new ElementValuePair(mb.selector, value, mb));
      }
      else {
        defaultValues.put(name, null);
      }
    }
    return unmodifiableMap(defaultValues);
  }

  public Map<String, MemberValuePair> getDeclarationAnnotationSyntheticDefaultValues(TypeBinding typeBinding) {
    assertInitialized();
    synchronized (lock()) {
      return m_mvpCache.computeIfAbsent(typeBinding, JavaEnvironmentWithEcj::computeDeclarationAnnotationSyntheticDefaultValues);
    }
  }

  protected static Map<String, MemberValuePair> computeDeclarationAnnotationSyntheticDefaultValues(TypeBinding typeBinding) {
    var valueMethods = ((ReferenceBinding) typeBinding).methods();
    if (valueMethods == null || valueMethods.length < 1) {
      return emptyMap();
    }

    valueMethods = Arrays.copyOf(valueMethods, valueMethods.length);
    Arrays.sort(valueMethods, MethodBindingComparator.INSTANCE);

    Map<String, MemberValuePair> defaultValues = new LinkedHashMap<>(valueMethods.length);
    for (var mb : valueMethods) {
      var name = new String(mb.selector);
      var md0 = Ensure.notNull(sourceMethodOf(mb), "binding is binary. Source method could not be found.");
      if (md0 instanceof AnnotationMethodDeclaration md) {
        if (md.defaultValue != null) {
          defaultValues.put(name, new MemberValuePair(mb.selector, md.defaultValue.sourceStart, md.defaultValue.sourceEnd, md.defaultValue));
        }
        else {
          defaultValues.put(name, null);
        }
      }
    }
    return unmodifiableMap(defaultValues);
  }

  protected ClasspathSpi classpathEntryToSpi(ClasspathEntry entry) {
    return new ClasspathWithEcj(entry, this);
  }
}
