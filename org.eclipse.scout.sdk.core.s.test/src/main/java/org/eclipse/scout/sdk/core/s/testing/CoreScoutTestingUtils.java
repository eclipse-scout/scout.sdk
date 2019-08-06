/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.cli.CLIManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsModuleNewHelper;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.JavaEnvironmentBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * helpers used for scout core unit tests
 */
public final class CoreScoutTestingUtils {

  public static final String PROJECT_GROUP_ID = "group";
  public static final String PROJECT_ARTIFACT_ID = "artifact";

  private CoreScoutTestingUtils() {
  }

  /**
   * @return a new {@link IJavaEnvironment} that contains the src/main/client and src/main/shared folders.
   */
  public static IJavaEnvironment createClientJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .withSourceFolder("src/main/client")
        .withSourceFolder("src/main/shared")
        .build();
  }

  /**
   * @return a {@link IJavaEnvironment} for org.eclipse.*.shared tests, without the org.eclipse.scout.rt.client
   *         dependency
   */
  public static IJavaEnvironment createSharedJavaEnvironment() {
    return new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .without(".*" + Pattern.quote("org.eclipse.scout.rt.client") + ".*")
        .withSourceFolder("src/main/shared")
        .build();
  }

  /**
   * Create the page data of the given {@link IPage} model class name.
   *
   * @param modelFqn
   *          The fully qualified name of the page model class for which the data should be re-generated
   * @return The created page data
   * @throws AssertionError
   *           if the created page data does not compile within the shared module.
   */
  public static IType createPageDataAssertNoCompileErrors(String modelFqn) {
    return createPageDataAssertNoCompileErrors(modelFqn, createClientJavaEnvironment(), createSharedJavaEnvironment());
  }

  /**
   * Create the page data of the given {@link IPage} model class name.
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
    return createDtoAssertNoCompileErrors(modelFqn, false, clientEnv, sharedEnv);
  }

  /**
   * Create the row data for the given {@link IExtension} model class name.
   *
   * @param modelFqn
   *          The fully qualified name of the extension class for which the row data should be re-generated.
   * @return The created row data
   * @throws AssertionError
   *           if the created page data does not compile within the shared module.
   */
  public static IType createRowDataAssertNoCompileErrors(String modelFqn) {
    return createDtoAssertNoCompileErrors(modelFqn, true, createClientJavaEnvironment(), createSharedJavaEnvironment());
  }

  /**
   * Creates the form data for the given Scout model class.
   *
   * @param modelFqn
   *          The fully qualified name of the model class for which the form data should be re-generated.
   * @return The created form data type.
   * @throws AssertionError
   *           if the created form data does not compile within the shared module.
   */
  public static IType createFormDataAssertNoCompileErrors(String modelFqn) {
    return createFormDataAssertNoCompileErrors(modelFqn, createClientJavaEnvironment(), createSharedJavaEnvironment());
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
    // get model type
    IType modelType = clientEnv.findType(modelFqn);

    // build source
    FormDataAnnotationDescriptor formDataAnnotation = DtoUtils.getFormDataAnnotationDescriptor(modelType);
    ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, formDataAnnotation, sharedEnv);
    String source = CoreUtils.createJavaCode(cuSrc, sharedEnv, "\n", null);

    // ensure it compiles and get model of dto
    return CoreTestingUtils.assertNoCompileErrors(sharedEnv, cuSrc.getPackageName(), cuSrc.getMainType().getElementName(), source);
  }

  private static IType createDtoAssertNoCompileErrors(String modelFqn, boolean rowData, IJavaEnvironment clientEnv, IJavaEnvironment sharedEnv) {
    // get model type
    IType modelType = clientEnv.findType(modelFqn);
    DataAnnotationDescriptor dataAnnotation = DtoUtils.getDataAnnotationDescriptor(modelType);

    // build source
    ICompilationUnitSourceBuilder cuSrc;
    if (rowData) {
      cuSrc = DtoUtils.createTableRowDataBuilder(modelType, dataAnnotation, sharedEnv);
    }
    else {
      cuSrc = DtoUtils.createPageDataBuilder(modelType, dataAnnotation, sharedEnv);
    }
    String source = CoreUtils.createJavaCode(cuSrc, sharedEnv, "\n", null);

    // ensure it compiles and get model of dto
    return CoreTestingUtils.assertNoCompileErrors(sharedEnv, cuSrc.getPackageName(), cuSrc.getMainType().getElementName(), source);
  }

  /**
   * Creates a new Scout project based on the helloworld archetype using group id {@link #PROJECT_GROUP_ID} and
   * artifactId {@link #PROJECT_ARTIFACT_ID}.
   *
   * @return The root directory that contains the created projects.
   * @throws IOException
   */
  public static File createTestProject() throws IOException {
    File targetDirectory = Files.createTempDirectory(CoreScoutTestingUtils.class.getSimpleName() + "-projectDir").toFile();
    ScoutProjectNewHelper.createProject(targetDirectory, PROJECT_GROUP_ID, PROJECT_ARTIFACT_ID, "Display Name", SystemUtils.JAVA_SPECIFICATION_VERSION,
        ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID, ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID, ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION);
    return targetDirectory;
  }

  /**
   * Creates a new JAX WS module into the specified Scout project using the jax-ws-module archetype.
   *
   * @param serverModuleDir
   *          The absolute {@link File} that points to the scout-server Maven module that should refer the new jax-ws
   *          module.
   * @param artifactId
   *          The jax-ws-module artifact id.
   * @return The created module root {@link File}.
   * @throws IOException
   *           if there is an error creating the new module.
   */
  public static File createJaxWsModule(final File serverModuleDir, final String artifactId) throws IOException {
    addMetroDependency(new File(serverModuleDir, IMavenConstants.POM));
    return JaxWsModuleNewHelper.createModule(new File(serverModuleDir, IMavenConstants.POM), artifactId);
  }

  static void addMetroDependency(final File pomFile) throws IOException {
    try {
      DocumentBuilder createDocumentBuilder = CoreUtils.createDocumentBuilder();
      Document pom = createDocumentBuilder.parse(pomFile);
      Element dependenciesElement = CoreUtils.getFirstChildElement(pom.getDocumentElement(), IMavenConstants.DEPENDENCIES);

      // add jaxws Metro as test-dependency because the default implementor specific is Metro and it must be present so that the test platform can be started
      Element metroDependencyElement = pom.createElement(IMavenConstants.DEPENDENCY);
      Element metroGroupIdElement = pom.createElement(IMavenConstants.GROUP_ID);
      metroGroupIdElement.setTextContent("com.sun.xml.ws");
      Element metroArtifactIdElement = pom.createElement(IMavenConstants.ARTIFACT_ID);
      metroArtifactIdElement.setTextContent("jaxws-rt");
      Element metroScopeElement = pom.createElement(IMavenConstants.SCOPE);
      metroScopeElement.setTextContent("test");
      metroDependencyElement.appendChild(metroGroupIdElement);
      metroDependencyElement.appendChild(metroArtifactIdElement);
      metroDependencyElement.appendChild(metroScopeElement);
      dependenciesElement.appendChild(metroDependencyElement);

      // write
      Transformer transformer = CoreUtils.createTransformer(true);
      transformer.transform(new DOMSource(pom), new StreamResult(pomFile));
    }
    catch (ParserConfigurationException | SAXException | TransformerException e) {
      throw new SdkException("Unable to register Metro test dependency in " + pomFile, e);
    }
  }

  /**
   * Executes a 'mvn clean compile' in the given directory.
   *
   * @param pomDir
   *          The directory in which the maven command should be executed. Must contain a pom.xml file.
   * @throws IOException
   */
  public static void runMavenCleanCompile(File pomDir) {
    MavenRunner.execute(new MavenBuild()
        .withWorkingDirectory(pomDir)
        .withGoal("clean")
        .withGoal("compile")
        .withOption(CLIManager.BATCH_MODE)
        .withOption(CLIManager.DEBUG));
  }

  /**
   * Executes a 'mvn clean test' in the given directory.
   *
   * @param pomDir
   *          The directory in which the maven command should be executed. Must contain a pom.xml file.
   * @throws IOException
   */
  public static void runMavenCleanTest(File pomDir) {
    MavenRunner.execute(new MavenBuild()
        .withWorkingDirectory(pomDir)
        .withGoal("clean")
        .withGoal("test")
        .withOption(CLIManager.BATCH_MODE)
        .withOption(CLIManager.DEBUG));
  }
}
