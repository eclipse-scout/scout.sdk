/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.IJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ScoutFixtureHelper}</h3>
 *
 * @since 6.1.0
 */
public final class ScoutFixtureHelper {

  public static final String SHARED_FIXTURE_PATH = "src/test/shared";
  public static final String CLIENT_FIXTURE_PATH = "src/test/client";
  public static final String RESOURCES_PREFIX = "src/test/resources/";
  public static final String RESOURCES_FIXTURE_PATH = RESOURCES_PREFIX + "fixture/";
  public static final String RESOURCES_CLIENT_FIXTURE_PATH = RESOURCES_FIXTURE_PATH + "client";
  public static final String RESOURCES_SHARED_FIXTURE_PATH = RESOURCES_FIXTURE_PATH + "shared";

  public static final Path NLS_TEST_DIR = Paths.get("").toAbsolutePath().resolve(SHARED_FIXTURE_PATH).resolve("nls");

  private ScoutFixtureHelper() {
  }

  /**
   * Create the page data of the given IPage model class name.
   *
   * @param modelFqn
   *          The fully qualified name of the page model class for which the data should be re-generated
   * @param withDtoTask
   *          An optional {@link Consumer} that gets the created page data.
   * @throws AssertionError
   *           if the created page data does not compile within the shared module.
   */
  public static void createPageDataAssertNoCompileErrors(String modelFqn, Consumer<IType> withDtoTask) {
    BiFunction<IJavaEnvironment, IJavaEnvironment, IType> createPageData = (shared, client) -> CoreScoutTestingUtils.createPageDataAssertNoCompileErrors(modelFqn, client, shared);
    runWithSharedAndClientEnv(createPageData.andThen(pageData -> runDtoTask(pageData, withDtoTask)));
  }

  /**
   * Create the row data for the given IExtension model class name.
   *
   * @param modelFqn
   *          The fully qualified name of the extension class for which the row data should be re-generated.
   * @param withDtoTask
   *          An optional {@link Consumer} that gets the created page data.
   * @throws AssertionError
   *           if the created page data does not compile within the shared module.
   */
  public static void createRowDataAssertNoCompileErrors(String modelFqn, Consumer<IType> withDtoTask) {
    BiFunction<IJavaEnvironment, IJavaEnvironment, IType> createRowData = (shared, client) -> CoreScoutTestingUtils.createRowDataAssertNoCompileErrors(modelFqn, client, shared);
    runWithSharedAndClientEnv(createRowData.andThen(rowData -> runDtoTask(rowData, withDtoTask)));
  }

  /**
   * Creates the form data for the given Scout model class.
   *
   * @param modelFqn
   *          The fully qualified name of the model class for which the form data should be re-generated.
   * @param withDtoTask
   *          An optional {@link Consumer} that gets the created page data.
   * @throws AssertionError
   *           if the created form data does not compile within the shared module.
   */
  public static void createFormDataAssertNoCompileErrors(String modelFqn, Consumer<IType> withDtoTask) {
    BiFunction<IJavaEnvironment, IJavaEnvironment, IType> createFormData = (shared, client) -> CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(modelFqn, client, shared);
    runWithSharedAndClientEnv(createFormData.andThen(formData -> runDtoTask(formData, withDtoTask)));
  }

  static <T> T runDtoTask(IType dto, Consumer<IType> withDtoTask) {
    Ensure.notNull(dto); // created dto must not be null
    Optional.ofNullable(withDtoTask).ifPresent(task -> task.accept(dto));
    return null;
  }

  public static <T> T runWithSharedAndClientEnv(BiFunction<IJavaEnvironment /* shared */, IJavaEnvironment /* client */, T> task) {
    Ensure.notNull(task);
    return ScoutJavaEnvironmentFactory.call(
        clientEnv -> ScoutJavaEnvironmentFactory.call(
            sharedEnv -> task.apply(sharedEnv, clientEnv),
            false, false),
        false, true);
  }

  private static Optional<ApiVersion> scoutApiVersionWithoutSuffix(IJavaEnvironment env) {
    return ScoutApi.version(env)
        .map(ApiVersion::segments)// remove suffix, e.g. SNAPSHOT
        .map(ApiVersion::new);
  }

  private static Stream<String> versionedResourcesFolders(String resourcesPath, ApiVersion maxVersion, boolean withResourcesPrefix) {
    resourcesPath = resourcesPath.endsWith("/") ? resourcesPath : resourcesPath + "/";
    var resourcesPathWithPrefix = resourcesPath.startsWith(RESOURCES_PREFIX) ? resourcesPath : RESOURCES_PREFIX + resourcesPath;
    var path = Paths.get("", resourcesPathWithPrefix);
    if (!Files.exists(path)) {
      return Stream.empty();
    }
    List<String> versionedFixtureFolders;
    try (var versionedFolders = Files.list(path)) {
      versionedFixtureFolders = versionedFolders
          .map(Path::getFileName)
          .map(Path::toString)
          .map(ApiVersion::parse)
          .flatMap(Optional::stream)
          .filter(v -> maxVersion.compareCommonSegmentsTo(v) >= 0)
          .sorted(Comparator.reverseOrder())
          .map(ApiVersion::asString)
          .map(v -> resourcesPathWithPrefix + v)
          .map(folder -> withResourcesPrefix ? folder : Strings.removePrefix(folder, RESOURCES_PREFIX))
          .map(folder -> folder + "/")
          .collect(Collectors.toList());
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
    return versionedFixtureFolders.stream();
  }

  public static Stream<String> versionedResourcesFolders(String resourcesPath, IJavaEnvironment env) {
    return versionedResourcesFolders(resourcesPath, env, false);
  }

  public static Stream<String> versionedResourcesFolders(String resourcesPath, IJavaEnvironment env, boolean withResourcesPrefix) {
    return scoutApiVersionWithoutSuffix(env)
        .map(apiVersion -> versionedResourcesFolders(resourcesPath, apiVersion, withResourcesPrefix))
        .orElse(Stream.empty());
  }

  public static class ScoutJavaEnvironmentFactory implements IJavaEnvironmentFactory {

    private final boolean m_withServer;
    private final boolean m_withClient;
    private final boolean m_withUi;

    public ScoutJavaEnvironmentFactory(boolean withServer, boolean withClient, boolean withUi) {
      m_withServer = withServer;
      m_withClient = withClient;
      m_withUi = withUi;
    }

    @Override
    public ScoutJavaEnvironmentWithEcjBuilder<?> get() {
      var withClient = m_withClient || m_withUi;

      var builder = new ScoutJavaEnvironmentWithEcjBuilder<>()
          .withScoutServer(m_withServer)
          .withScoutClient(withClient)
          .withScoutHtmlUi(m_withUi);

      // client must be added first so that it becomes the primary source folder in testing.
      if (withClient) {
        builder.withSourceFolder(CLIENT_FIXTURE_PATH);
        builder.accept(env -> scoutApiVersionWithoutSuffix(env)
            .ifPresent(maxVersion -> addVersionedClientFixtureFolders(builder, maxVersion)));
      }

      builder.withSourceFolder(ScoutFixtureHelper.SHARED_FIXTURE_PATH);
      builder.accept(env -> scoutApiVersionWithoutSuffix(env)
          .ifPresent(maxVersion -> addVersionedSharedFixtureFolders(builder, maxVersion)));

      return builder;
    }

    protected static void addVersionedFixtureFolders(JavaEnvironmentWithEcjBuilder<?> builder, String resourcesFixturePath, ApiVersion maxVersion) {
      versionedResourcesFolders(resourcesFixturePath, maxVersion, true).forEachOrdered(builder::withSourceFolder);
    }

    protected static void addVersionedClientFixtureFolders(JavaEnvironmentWithEcjBuilder<?> builder, ApiVersion maxVersion) {
      addVersionedFixtureFolders(builder, RESOURCES_CLIENT_FIXTURE_PATH, maxVersion);
    }

    protected static void addVersionedSharedFixtureFolders(JavaEnvironmentWithEcjBuilder<?> builder, ApiVersion maxVersion) {
      addVersionedFixtureFolders(builder, RESOURCES_SHARED_FIXTURE_PATH, maxVersion);
    }

    public static void run(Consumer<IJavaEnvironment> task, boolean withServer, boolean withClient) {
      new ScoutJavaEnvironmentFactory(withServer, withClient, false).accept(task);
    }

    public static <T> T call(Function<IJavaEnvironment, T> task, boolean withServer, boolean withClient) {
      return new ScoutJavaEnvironmentFactory(withServer, withClient, false).call(task);
    }
  }

  public static final class ScoutServerJavaEnvironmentFactory extends ScoutJavaEnvironmentFactory {
    public ScoutServerJavaEnvironmentFactory() {
      super(true, false, false);
    }
  }

  public static final class ScoutClientJavaEnvironmentFactory extends ScoutJavaEnvironmentFactory {
    public ScoutClientJavaEnvironmentFactory() {
      super(false, true, false);
    }
  }

  public static final class ScoutSharedJavaEnvironmentFactory extends ScoutJavaEnvironmentFactory {
    public ScoutSharedJavaEnvironmentFactory() {
      super(false, false, false);
    }
  }

  public static final class ScoutFullJavaEnvironmentFactory extends ScoutJavaEnvironmentFactory {
    public ScoutFullJavaEnvironmentFactory() {
      super(true, true, true);
    }
  }
}
