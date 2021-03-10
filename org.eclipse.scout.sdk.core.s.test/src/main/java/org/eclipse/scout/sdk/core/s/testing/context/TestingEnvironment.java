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
package org.eclipse.scout.sdk.core.s.testing.context;

import static java.util.stream.Collectors.toCollection;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension.createJavaEnvironmentUsingBuilder;

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

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcj;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
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
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link TestingEnvironment}</h3> {@link IEnvironment} implementation used for testing.
 *
 * @since 7.0.0
 */
public class TestingEnvironment extends AbstractEnvironment implements AutoCloseable {

  private final boolean m_flushResourcesToDisk;
  private final Map<String, IType> m_dtoCache;
  private final IJavaEnvironment m_dtoEnv;
  private final IJavaEnvironment m_env;
  private final Collection<JavaEnvironmentWithEcj> m_javaEnvironments;

  protected TestingEnvironment(IJavaEnvironment env, boolean flushResourcesToDisk, IJavaEnvironment dtoEnv) {
    m_dtoCache = new HashMap<>();
    m_flushResourcesToDisk = flushResourcesToDisk;
    m_env = env;
    m_dtoEnv = dtoEnv;
    m_javaEnvironments = new ArrayList<>();
  }

  @Override
  protected StringBuilder runGenerator(ISourceGenerator<ISourceBuilder<?>> generator, IJavaEnvironment env, Path filePath) {
    //noinspection TypeMayBeWeakened
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
  protected IFuture<IType> doWriteCompilationUnit(CharSequence source, CompilationUnitInfo cuInfo, IProgress progress, boolean sync) {
    IType result;
    var javaEnv = cuInfo.sourceFolder().javaEnvironment();
    var name = cuInfo.mainTypeSimpleName();
    var pck = cuInfo.packageName();
    if (name.endsWith(ISdkConstants.SUFFIX_DTO) && pck.contains(".shared.")) {
      result = CoreTestingUtils.registerCompilationUnit(javaEnv, pck, name, source);
      // remember for later validation. As soon as the model type has been created the dto will be updated and validated.
      var baseName = name.substring(0, name.length() - ISdkConstants.SUFFIX_DTO.length());
      m_dtoCache.put(baseName, result);
    }
    else {
      result = CoreTestingUtils.registerCompilationUnit(javaEnv, pck, name, source);
      updateAndValidateDtoFor(result);
      assertNoCompileErrors(result);
    }

    Throwable err = null;
    if (isFlushResourcesToDisk()) {
      err = writeIcuToDisk(cuInfo.targetFile(), source);
    }
    return SdkFuture.completed(result, err);
  }

  public IClasspathEntry getTestingSourceFolder() {
    return m_env.primarySourceFolder().get();
  }

  public IJavaEnvironment primaryEnvironment() {
    return m_env;
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

  protected void updateAndValidateDtoFor(IType modelType) {
    var dto = m_dtoCache.get(modelType.elementName());
    if (dto == null) {
      return; // no dto has been registered
    }

    // A DTO has been registered for this model: update and validate now
    var shared = getSharedEnvForDtos();
    var input = new IDerivedResourceInput() {
      @Override
      public Optional<IType> getSourceType(IEnvironment env) {
        return Optional.of(modelType);
      }

      @Override
      public Optional<IClasspathEntry> getSourceFolderOf(IType t, IEnvironment env) {
        return shared.primarySourceFolder();
      }
    };

    // update DTO in shared environment
    new DtoUpdateHandler(input)
        .apply(this, new NullProgress())
        .forEach(IFuture::result);

    // write new source from shared environment to primary environment
    if (modelType.javaEnvironment() != shared) {
      var newDtoSrc = shared.requireType(dto.name()).requireCompilationUnit().source().get().asCharSequence();
      dto = CoreTestingUtils.registerCompilationUnit(modelType.javaEnvironment(), dto.containingPackage().elementName(), dto.elementName(), newDtoSrc);
      if (isFlushResourcesToDisk()) {
        var t = writeIcuToDisk(shared.primarySourceFolder().get().path(), newDtoSrc);
        if (t != null) {
          if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
          }
          throw new SdkException(t);
        }
      }
    }

    // validate DTO
    assertNoCompileErrors(dto);
    m_dtoCache.remove(modelType.elementName());
  }

  protected IJavaEnvironment getSharedEnvForDtos() {
    return m_dtoEnv;
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
    return createJavaEnvironmentUsingBuilder(
        new JavaEnvironmentWithEcjBuilder<>()
            .withoutScoutSdk()
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.MAIN_RESOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.TEST_JAVA_SOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.TEST_RESOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(IScoutSourceFolders.WEBAPP_RESOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.GENERATED_WS_IMPORT_SOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(root.resolve(IScoutSourceFolders.GENERATED_SOURCE_FOLDER).toString()))
                .map(this::registerJavaEnvironment);
  }

  @Override
  public Path rootOfJavaEnvironment(IJavaEnvironment environment) {
    return Paths.get("").toAbsolutePath();
  }

  @Override
  public Stream<IType> findType(String fqn) {
    List<IType> result = new ArrayList<>();
    if (m_env != null) {
      m_env.findType(fqn).ifPresent(result::add);
    }
    if (m_dtoEnv != null) {
      m_dtoEnv.findType(fqn).ifPresent(result::add);
    }
    m_javaEnvironments.stream()
        .map(e -> e.findType(fqn))
        .filter(Objects::nonNull)
        .map(TypeSpi::wrap)
        .collect(toCollection(() -> result));
    return result.stream();
  }

  protected IJavaEnvironment registerJavaEnvironment(JavaEnvironmentWithEcj env) {
    m_javaEnvironments.add(env); // remember to close later on
    return env.wrap();
  }

  public boolean isFlushResourcesToDisk() {
    return m_flushResourcesToDisk;
  }

  @Override
  public void close() {
    if (m_env != null) {
      var env = m_env.unwrap();
      if (env instanceof JavaEnvironmentWithEcj) {
        closeSafe((AutoCloseable) env);
      }
    }

    if (m_dtoEnv != null) {
      var dto = m_dtoEnv.unwrap();
      if (dto instanceof JavaEnvironmentWithEcj) {
        closeSafe((AutoCloseable) dto);
      }
    }

    var iterator = m_javaEnvironments.iterator();
    while (iterator.hasNext()) {
      closeSafe(iterator.next());
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
