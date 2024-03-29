/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import static java.util.stream.Collectors.toCollection;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.ISourceFolders;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContext;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfoWithClasspath;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.java.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler;
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceInput;
import org.eclipse.scout.sdk.core.s.environment.AbstractEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link TestingEnvironment}</h3> {@link IEnvironment} implementation used for testing.
 *
 * @since 7.0.0
 */
public class TestingEnvironment extends AbstractEnvironment {

  private final boolean m_flushResourcesToDisk;
  private final boolean m_assertNoCompileErrors;
  private final Map<String, IType> m_dtoCache;
  private final IJavaEnvironment m_dtoEnv;
  private final IJavaEnvironment m_env;
  private final Collection<JavaEnvironmentSpi> m_javaEnvironments;

  protected TestingEnvironment(IJavaEnvironment env, boolean flushResourcesToDisk, boolean assertNoCompileErrors, IJavaEnvironment dtoEnv) {
    m_dtoCache = new HashMap<>();
    m_javaEnvironments = new ArrayList<>();
    m_flushResourcesToDisk = flushResourcesToDisk;
    m_assertNoCompileErrors = assertNoCompileErrors;
    m_env = env;
    m_dtoEnv = dtoEnv;

    if (env != null) {
      registerJavaEnvironment(env.unwrap());
    }
    if (dtoEnv != null) {
      registerJavaEnvironment(dtoEnv.unwrap());
    }
  }

  @Override
  protected StringBuilder runGenerator(ISourceGenerator<ISourceBuilder<?>> generator, IJavaEnvironment env, Path filePath) {
    var context = new JavaBuilderContext(new BuilderContext(), Ensure.notNull(env));
    var builder = MemorySourceBuilder.create(context);
    generator.generate(builder);
    return builder.source();
  }

  @Override
  protected IFuture<Void> doWriteResource(CharSequence content, Path filePath, IProgress progress, boolean sync) {
    Throwable ex = null;
    if (isFlushResourcesToDisk()) {
      var normalizedPath = filePath.normalize();
      try {
        writeFile(normalizedPath, content.toString().getBytes(StandardCharsets.UTF_8));
      }
      catch (IOException e) {
        ex = e;
      }
    }
    return SdkFuture.completed(null, ex);
  }

  @Override
  protected IFuture<IType> doWriteCompilationUnit(CharSequence source, CompilationUnitInfoWithClasspath cuInfo, IProgress progress, boolean sync) {
    var result = registerCompilationUnit(source, cuInfo);
    var name = cuInfo.mainTypeSimpleName();
    if (name.endsWith(ISdkConstants.SUFFIX_DTO) && cuInfo.packageName().contains(".shared.")) {
      // remember for later validation. As soon as the model type has been created the dto will be updated and validated.
      var baseName = name.substring(0, name.length() - ISdkConstants.SUFFIX_DTO.length());
      m_dtoCache.put(baseName, result);
    }
    else {
      updateAndValidateDtoFor(result);
      if (isAssertNoCompileErrors()) {
        assertNoCompileErrors(result);
      }
    }

    Throwable err = null;
    if (isFlushResourcesToDisk()) {
      err = writeIcuToDisk(cuInfo.targetFile(), source);
    }
    return SdkFuture.completed(result, err);
  }

  public IClasspathEntry primarySourceFolder() {
    return primaryEnvironment().primarySourceFolder().orElseThrow();
  }

  public IJavaEnvironment primaryEnvironment() {
    return m_env;
  }

  public IJavaEnvironment dtoEnvironment() {
    return m_dtoEnv;
  }

  public IClasspathEntry dtoSourceFolder() {
    return dtoEnvironment().primarySourceFolder().orElseThrow();
  }

  protected static Throwable writeIcuToDisk(Path targetFile, CharSequence source) {
    try {
      writeFile(targetFile, source.toString().getBytes(StandardCharsets.UTF_8));
      return null;
    }
    catch (IOException e) {
      return e;
    }
  }

  /**
   * Make visible for testing
   */
  @Override
  @SuppressWarnings("squid:S1185") // Remove this method to simply inherit it
  protected <T extends JavaEnvironmentSpi> T initNewJavaEnvironment(T javaEnvironment) {
    return super.initNewJavaEnvironment(javaEnvironment);
  }

  protected void updateAndValidateDtoFor(IType modelType) {
    var dto = m_dtoCache.get(modelType.elementName());
    if (dto == null) {
      return; // no dto has been registered
    }

    // A DTO has been registered for this model: update in dto environment
    var input = new IDerivedResourceInput() {
      @Override
      public Optional<IType> getSourceType(IEnvironment env) {
        return Optional.of(modelType);
      }

      @Override
      public Optional<IClasspathEntry> getSourceFolderOf(IType t, IEnvironment env) {
        return dtoEnvironment().primarySourceFolder();
      }
    };
    new DtoUpdateHandler(input)
        .apply(this, new NullProgress())
        .forEach(IFuture::result);

    // validate created DTO
    if (isAssertNoCompileErrors()) {
      assertNoCompileErrors(dto);
    }
    m_dtoCache.remove(modelType.elementName());
  }

  @Override
  protected Collection<? extends JavaEnvironmentSpi> javaEnvironments() {
    return m_javaEnvironments;
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected static void writeFile(Path filePath, byte[] content) throws IOException {
    Files.createDirectories(filePath.getParent());
    Files.write(filePath, content);
  }

  public void run(BiConsumer<IEnvironment, IProgress> operation) {
    IProgress progress = new NullProgress();
    Ensure.notNull(operation).accept(this, progress);
  }

  @Override
  public Optional<IJavaEnvironment> findJavaEnvironment(Path root) {
    var javaEnvBuilder = new JavaEnvironmentWithEcjBuilder<>()
        .withoutScoutSdk()
        .withAbsoluteSourcePath(root.resolve(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(ISourceFolders.MAIN_RESOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(ISourceFolders.TEST_JAVA_SOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(ISourceFolders.TEST_RESOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(IScoutSourceFolders.WEBAPP_RESOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(ISourceFolders.GENERATED_WS_IMPORT_SOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER).toString())
        .withAbsoluteSourcePath(root.resolve(IScoutSourceFolders.GENERATED_SOURCE_FOLDER).toString());
    return JavaEnvironmentExtension.createJavaEnvironmentUsingBuilder(javaEnvBuilder)
        .map(this::registerJavaEnvironment);
  }

  @Override
  public Path rootOfJavaEnvironment(IJavaEnvironment environment) {
    return Paths.get("").toAbsolutePath();
  }

  @Override
  public Stream<IType> findType(String fqn) {
    List<IType> result = new ArrayList<>();
    javaEnvironments().stream()
        .map(e -> e.findType(fqn))
        .filter(Objects::nonNull)
        .map(TypeSpi::wrap)
        .collect(toCollection(() -> result));
    return result.stream();
  }

  @Override
  public void deleteIfExists(Path file) {
    try {
      if (Files.isDirectory(file)) {
        CoreUtils.deleteDirectory(file);
      }
      else {
        Files.deleteIfExists(file);
      }
    }
    catch (IOException e) {
      throw new SdkException("Unable to delete '{}'.", file, e);
    }
  }

  protected IJavaEnvironment registerJavaEnvironment(JavaEnvironmentSpi env) {
    if (env == null) {
      return null;
    }
    m_javaEnvironments.add(initNewJavaEnvironment(env)); // remember to close later on
    return env.wrap();
  }

  public boolean isFlushResourcesToDisk() {
    return m_flushResourcesToDisk;
  }

  public boolean isAssertNoCompileErrors() {
    return m_assertNoCompileErrors;
  }

  @Override
  public void close() {
    super.close();
    var iterator = javaEnvironments().iterator();
    while (iterator.hasNext()) {
      var javaEnv = iterator.next();
      if (javaEnv instanceof AutoCloseable) {
        closeSafe((AutoCloseable) javaEnv);
      }
      iterator.remove();
    }
  }

  private static void closeSafe(AutoCloseable c) {
    try {
      c.close();
    }
    catch (Exception e) {
      SdkLog.warning("Unable to close {}.", c, e);
    }
  }
}
