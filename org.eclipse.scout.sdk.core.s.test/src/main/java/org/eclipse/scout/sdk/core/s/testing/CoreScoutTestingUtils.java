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
package org.eclipse.scout.sdk.core.s.testing;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsModuleNewHelper;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.testing.maven.MavenCliRunner;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.xml.sax.SAXException;

/**
 * helpers used for scout core unit tests
 */
public final class CoreScoutTestingUtils {

  public static final String PROJECT_GROUP_ID = "group";
  public static final String PROJECT_ARTIFACT_ID = "artifact";
  public static final String SCOUT_VERSION_KEY = "org.eclipse.scout.rt_version";

  private CoreScoutTestingUtils() {
  }

  /**
   * Creates a new Scout project based on the Scout JS archetype using group id {@link #PROJECT_GROUP_ID} and artifactId
   * {@link #PROJECT_ARTIFACT_ID}.
   *
   * @return The root directory that contains the created projects.
   */
  public static Path createJsTestProject() throws IOException {
    return createTestProject(ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID);
  }

  /**
   * Creates a new Scout project based on the Scout classic helloworld archetype using group id
   * {@link #PROJECT_GROUP_ID} and artifactId {@link #PROJECT_ARTIFACT_ID}.
   *
   * @return The root directory that contains the created projects.
   */
  public static Path createClassicTestProject() throws IOException {
    return createTestProject(ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID);
  }

  private static Path createTestProject(String archetypeArtifactId) throws IOException {
    ensureMavenRunnerCreated();
    var targetDirectory = Files.createTempDirectory(CoreScoutTestingUtils.class.getSimpleName() + "-projectDir");

    // The testing runner does not make use of the environment and the progress: pass empty mocks
    ScoutProjectNewHelper.createProject(targetDirectory, PROJECT_GROUP_ID, PROJECT_ARTIFACT_ID, "Display Name", javaVersionForArchetypes(),
        ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID, archetypeArtifactId, archetypeVersion(), mock(IEnvironment.class), mock(IProgress.class));
    return targetDirectory;
  }

  /**
   * @return The archetype version that belongs to the {@link #currentScoutVersion()}.
   *         <p>
   *         The archetype version for numbers <= 11 is based on the SDK version which uses number segments to fulfill
   *         the OSGi version requirements. While {@code 11.0-SNAPSHOT} is valid maven version, for OSGi at least three
   *         numbers are required. Therefore {@code 11.0.0-SNAPSHOT} is the matching SDK version.
   *         <p>
   *         For versions > 11 the archetypes use the same version schema as the RT.
   */
  static String archetypeVersion() {
    // do not compute sdk version based on maven module. This version would always be the same.
    // instead derive it from the Scout RT version which might be modified using parameters.
    return rtToArchetypeVersion(currentScoutVersion());
  }

  static String rtToArchetypeVersion(String rtVersion) {
    var version = ApiVersion.parse(rtVersion).orElseThrow(() -> newFail("Invalid Scout RT version '{}'.", rtVersion));
    var numberSegments = version.segments();
    if (numberSegments[0] > 11) { // e.g. Scout RT 22.0-SNAPSHOT
      // For Scout RT version > 11 the archetype version uses the same version schema as the RT
      return rtVersion;
    }
    if (numberSegments.length < 2) {
      numberSegments = new int[]{numberSegments[0], 0};
    }
    if (numberSegments.length < 3) {
      numberSegments = new int[]{numberSegments[0], numberSegments[1], 0};
    }
    return new ApiVersion(numberSegments, version.suffix()).asString();
  }

  static String javaVersionForArchetypes() {
    var supportedJavaVersions = new JavaEnvironmentWithEcjBuilder<>()
        .withoutScoutSdk()
        .call(e -> e.requireApi(IScoutApi.class).supportedJavaVersions());
    return Integer.toString(supportedJavaVersions[supportedJavaVersions.length - 1]);
  }

  /**
   * @return The Scout runtime version of the current test run. Uses the version present in the maven dependencies or
   *         from the {@link #SCOUT_VERSION_KEY} system property if present.
   */
  @SuppressWarnings("AccessOfSystemProperties")
  public static String currentScoutVersion() {
    var prop = System.getProperty(SCOUT_VERSION_KEY);
    if (Strings.hasText(prop)) {
      return Strings.trim(prop).toString();
    }
    return new JavaEnvironmentWithEcjBuilder<>()
        .withoutScoutSdk()
        .call(ScoutApi::version)
        .map(ApiVersion::asString)
        .orElseThrow(() -> newFail("Unable to determine Scout RT version."));
  }

  /**
   * Creates a new JAX WS module into the specified Scout project using the jax-ws-module archetype.
   *
   * @param serverModuleDir
   *          The absolute {@link Path} that points to the scout-server Maven module that should refer the new jax-ws
   *          module.
   * @param artifactId
   *          The jax-ws-module artifact id.
   * @param env
   *          The {@link IEnvironment} to use. It is required to compute the Scout version of the target server module.
   *          Must not be {@code null}.
   * @return The created module root {@link Path}.
   * @throws IOException
   *           if there is an error creating the new module.
   */
  public static Path createJaxWsModule(Path serverModuleDir, String artifactId, IEnvironment env) throws IOException {
    ensureMavenRunnerCreated();
    addMetroDependency(serverModuleDir.resolve(IMavenConstants.POM));

    // The testing runner does not make use of the environment and the progress: pass empty mocks
    return JaxWsModuleNewHelper.createModule(serverModuleDir.resolve(IMavenConstants.POM), artifactId, env, mock(IProgress.class));
  }

  static void addMetroDependency(Path pomFile) throws IOException {
    try {
      var createDocumentBuilder = Xml.createDocumentBuilder();
      var pom = createDocumentBuilder.parse(pomFile.toFile());
      var dependenciesElement = Xml.firstChildElement(pom.getDocumentElement(), IMavenConstants.DEPENDENCIES)
          .orElseThrow(() -> newFail("Pom '{}' does not contain a '{}' element.", pomFile, IMavenConstants.DEPENDENCIES));

      // add jaxws Metro as test-dependency because the default implementor specific is Metro, and it must be present so that the test platform can be started
      var metroDependencyElement = pom.createElement(IMavenConstants.DEPENDENCY);
      var metroGroupIdElement = pom.createElement(IMavenConstants.GROUP_ID);
      metroGroupIdElement.setTextContent("com.sun.xml.ws");
      var metroArtifactIdElement = pom.createElement(IMavenConstants.ARTIFACT_ID);
      metroArtifactIdElement.setTextContent("jaxws-rt");
      var metroScopeElement = pom.createElement(IMavenConstants.SCOPE);
      metroScopeElement.setTextContent("test");
      metroDependencyElement.appendChild(metroGroupIdElement);
      metroDependencyElement.appendChild(metroArtifactIdElement);
      metroDependencyElement.appendChild(metroScopeElement);
      dependenciesElement.appendChild(metroDependencyElement);

      // write
      Xml.writeDocument(pom, true, pomFile);
    }
    catch (ParserConfigurationException | SAXException | TransformerException e) {
      throw new SdkException("Unable to register Metro test dependency in '{}'.", pomFile, e);
    }
  }

  private enum DtoType {
    FORM_DATA,
    PAGE_DATA,
    ROW_DATA
  }

  /**
   * Create the page data of the given IPage model class name.
   *
   * @param modelFqn
   *          The fully qualified name of the page model class for which the data should be re-generated
   * @param clientEnv
   *          The client {@link IJavaEnvironment} to use.
   * @param sharedEnv
   *          The shared {@link IJavaEnvironment} to use.
   * @return The created page data
   * @throws AssertionError
   *           if the created page data does not compile within the shared module.
   */
  public static IType createPageDataAssertNoCompileErrors(String modelFqn, IJavaEnvironment clientEnv, IJavaEnvironment sharedEnv) {
    return createDtoAssertNoCompileErrors(modelFqn, clientEnv, sharedEnv, DtoType.PAGE_DATA);
  }

  /**
   * Create the row data for the given IExtension model class name.
   *
   * @param modelFqn
   *          The fully qualified name of the extension class for which the row data should be re-generated.
   * @param clientEnv
   *          The client {@link IJavaEnvironment} to use.
   * @param sharedEnv
   *          The shared {@link IJavaEnvironment} to use.
   * @return The created row data
   * @throws AssertionError
   *           if the created row data does not compile within the shared module.
   */
  public static IType createRowDataAssertNoCompileErrors(String modelFqn, IJavaEnvironment clientEnv, IJavaEnvironment sharedEnv) {
    return createDtoAssertNoCompileErrors(modelFqn, clientEnv, sharedEnv, DtoType.ROW_DATA);
  }

  /**
   * Creates the form data for the given Scout model class.
   *
   * @param modelFqn
   *          The fully qualified name of the model class for which the data should be re-generated
   * @param clientEnv
   *          The client {@link IJavaEnvironment} to use.
   * @param sharedEnv
   *          The shared {@link IJavaEnvironment} to use.
   * @return The created form data
   * @throws AssertionError
   *           if the created form data does not compile within the shared module.
   */
  public static IType createFormDataAssertNoCompileErrors(String modelFqn, IJavaEnvironment clientEnv, IJavaEnvironment sharedEnv) {
    return createDtoAssertNoCompileErrors(modelFqn, clientEnv, sharedEnv, DtoType.FORM_DATA);
  }

  private static IType createDtoAssertNoCompileErrors(String modelFqn, IJavaEnvironment clientEnv, IJavaEnvironment sharedEnv, DtoType dtoType) {
    // get model type
    var modelType = clientEnv.requireType(modelFqn);

    // build source
    var cuSrc = switch (dtoType) {
      case ROW_DATA -> DtoGeneratorFactory.createTableRowDataGenerator(modelType, sharedEnv);
      case PAGE_DATA -> DtoGeneratorFactory.createPageDataGenerator(modelType, sharedEnv);
      default -> DtoGeneratorFactory.createFormDataGenerator(modelType, sharedEnv);
    };

    // ensure it compiles and get model of dto
    return assertNoCompileErrors(sharedEnv, cuSrc.orElseThrow(() -> new IllegalArgumentException("cannot create DTO for model Type " + modelFqn)));
  }

  /**
   * Executes a 'mvn clean test' in the given directory.
   *
   * @param pomDir
   *          The directory in which the maven command should be executed. Must contain a pom.xml file.
   */
  public static void runMavenCleanVerify(Path pomDir) {
    runMavenCommand(pomDir, "clean", "verify");
  }

  /**
   * Executes a maven command with the given goals in the given directory.
   *
   * @param pomDir
   *          The directory in which the maven command should be executed. Must contain a pom.xml file.
   * @param goals
   *          The goals to execute. E.g.: {@code ["clean", "compile"]}.
   */
  public static void runMavenCommand(Path pomDir, String... goals) {
    ensureMavenRunnerCreated();
    var build = new MavenBuild()
        .withWorkingDirectory(pomDir)
        .withProperty(MavenBuild.PROPERTY_INTERACTIVE_MODE, "false")
        .withOption(MavenBuild.OPTION_DEBUG);
    if (goals != null && goals.length > 0) {
      for (var goal : goals) {
        build.withGoal(goal);
      }
    }
    // The testing runner does not make use of the environment and the progress: pass empty mocks
    MavenRunner.execute(build, mock(IEnvironment.class), mock(IProgress.class));
  }

  private static void ensureMavenRunnerCreated() {
    MavenRunner.setIfAbsent(MavenCliRunner::new);
  }
}
