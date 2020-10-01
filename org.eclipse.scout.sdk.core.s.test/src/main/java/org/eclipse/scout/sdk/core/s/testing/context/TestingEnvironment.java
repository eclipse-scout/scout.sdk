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
package org.eclipse.scout.sdk.core.s.testing.context;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcj;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler;
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceInput;
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
public class TestingEnvironment implements IEnvironment, AutoCloseable {

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

  public IClasspathEntry getTestingSourceFolder() {
    return m_env.primarySourceFolder().get();
  }

  public IJavaEnvironment primaryEnvironment() {
    return m_env;
  }

  @Override
  public IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder) {
    return writeCompilationUnit(generator, targetFolder, null);
  }

  @Override
  public IType writeCompilationUnit(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress) {
    return writeCompilationUnitAsync(generator, targetFolder, progress).result();
  }

  @Override
  public IFuture<IType> writeCompilationUnitAsync(ICompilationUnitGenerator<?> generator, IClasspathEntry targetFolder, IProgress progress) {
    IType result;
    String className = generator.elementName().get();
    if (className.endsWith(ISdkConstants.SUFFIX_DTO) && generator.packageName().orElse("").contains(".shared.")) {
      result = CoreTestingUtils.registerCompilationUnit(targetFolder.javaEnvironment(), generator);
      // remember for later validation. As soon as the model type has been created the dto will be updated and validated.
      String baseName = className.substring(0, className.length() - ISdkConstants.SUFFIX_DTO.length());
      m_dtoCache.put(baseName, result);
    }
    else {
      result = CoreTestingUtils.registerCompilationUnit(targetFolder.javaEnvironment(), generator);
      updateAndValidateDtoFor(result);
      assertNoCompileErrors(result);
    }

    Throwable err = null;
    if (isFlushResourcesToDisk()) {
      err = writeIcuToDisk(targetFolder, result.requireCompilationUnit());
    }
    return SdkFuture.completed(result, err);
  }

  protected static Throwable writeIcuToDisk(IClasspathEntry sourceFolder, ICompilationUnit icu) {
    Path targetFolder = sourceFolder.path().resolve(icu.containingPackage().asPath()).normalize();
    try {
      writeFile(targetFolder.resolve(icu.elementName()), icu.source().get().asCharSequence().toString().getBytes(StandardCharsets.UTF_8));
      return null;
    }
    catch (IOException e) {
      return e;
    }
  }

  protected void updateAndValidateDtoFor(IType modelType) {
    IType dto = m_dtoCache.get(modelType.elementName());
    if (dto == null) {
      return; // no dto has been registered
    }

    // A DTO has been registered for this model: update and validate now
    IJavaEnvironment shared = getSharedEnvForDtos();
    IDerivedResourceInput input = new IDerivedResourceInput() {
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
      CharSequence newDtoSrc = shared.requireType(dto.name()).requireCompilationUnit().source().get().asCharSequence();
      dto = CoreTestingUtils.registerCompilationUnit(modelType.javaEnvironment(), dto.containingPackage().elementName(), dto.elementName(), newDtoSrc);
      if (isFlushResourcesToDisk()) {
        Throwable t = writeIcuToDisk(shared.primarySourceFolder().get(), dto.requireCompilationUnit());
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

  @Override
  public void writeResource(CharSequence content, Path filePath, IProgress progress) {
    writeResourceAsync(content, filePath, progress).awaitDoneThrowingOnErrorOrCancel();
  }

  @Override
  public IFuture<Void> writeResourceAsync(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress) {
    return writeResourceAsync(doCreateResource(generator, filePath), filePath, progress);
  }

  @Override
  public IFuture<Void> writeResourceAsync(CharSequence content, Path filePath, IProgress progress) {
    Throwable ex = null;
    if (isFlushResourcesToDisk()) {
      Path normalizedPath = filePath.normalize();
      try {
        writeFile(normalizedPath, content.toString().getBytes(StandardCharsets.UTF_8));
      }
      catch (IOException e) {
        ex = e;
      }
    }
    return SdkFuture.completed(null, ex);
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
  public StringBuilder createResource(ISourceGenerator<ISourceBuilder<?>> generator, IClasspathEntry targetFolder) {
    return doCreateResource(generator, targetFolder.javaEnvironment());
  }

  protected StringBuilder doCreateResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath) {
    return doCreateResource(generator, findJavaEnvironment(filePath).orElse(null));
  }

  protected static StringBuilder doCreateResource(ISourceGenerator<ISourceBuilder<?>> generator, IJavaEnvironment env) {
    JavaBuilderContext context = new JavaBuilderContext(new BuilderContext(), Ensure.notNull(env));
    MemorySourceBuilder builder = new MemorySourceBuilder(context);
    generator.generate(builder);
    return builder.source();
  }

  @Override
  public void writeResource(ISourceGenerator<ISourceBuilder<?>> generator, Path filePath, IProgress progress) {
    writeResourceAsync(doCreateResource(generator, filePath), filePath, progress).awaitDoneThrowingOnErrorOrCancel();
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
            .withAbsoluteSourcePath(root.resolve(ISourceFolders.GENERATED_WSIMPORT_SOURCE_FOLDER).toString())
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
        .collect(Collectors.toCollection(() -> result));
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
      JavaEnvironmentSpi env = m_env.unwrap();
      if (env instanceof JavaEnvironmentWithEcj) {
        closeSafe((AutoCloseable) env);
      }
    }

    if (m_dtoEnv != null) {
      JavaEnvironmentSpi dto = m_dtoEnv.unwrap();
      if (dto instanceof JavaEnvironmentWithEcj) {
        closeSafe((AutoCloseable) dto);
      }
    }

    Iterator<JavaEnvironmentWithEcj> iterator = m_javaEnvironments.iterator();
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
