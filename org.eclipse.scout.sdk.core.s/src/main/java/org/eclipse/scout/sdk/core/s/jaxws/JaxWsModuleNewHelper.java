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
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.xml.transform.TransformerException;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.s.util.maven.Pom;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link JaxWsModuleNewHelper}</h3>
 *
 * @since 5.2.0
 */
public final class JaxWsModuleNewHelper {

  private JaxWsModuleNewHelper() {
  }

  public static Path getParentPomOf(Path modulePomFile, Document modulePomDocument) {
    var parent = Xml.firstChildElement(modulePomDocument.getDocumentElement(), IMavenConstants.PARENT);
    if (parent.isPresent()) {
      var relPat = Xml.firstChildElement(parent.orElseThrow(), IMavenConstants.RELATIVE_PATH);
      if (relPat.isPresent()) {
        var path = relPat.orElseThrow().getTextContent();
        if (Strings.isBlank(path)) {
          // parent is resolved from the repository. see https://maven.apache.org/ref/3.0.3/maven-model/maven.html#class_parent
          return null;
        }

        if (!path.endsWith(IMavenConstants.POM)) {
          if (path.charAt(path.length() - 1) != '/') {
            path += '/';
          }
          path += IMavenConstants.POM;
        }
        var parentDir = modulePomFile.getParent();
        if (parentDir != null) {
          return parentDir.resolve(path).normalize();
        }
        return null;
      }
    }
    var parentDir = modulePomFile.getParent();
    if (parentDir != null) {
      var grandParentDir = parentDir.getParent();
      if (grandParentDir != null) {
        return grandParentDir.resolve(IMavenConstants.POM);
      }
    }
    return null;
  }

  public static Path getParentPomOf(Path projectPomFile) throws IOException {
    return getParentPomOf(projectPomFile, Xml.get(projectPomFile));
  }

  /**
   * @param targetModulePomFile
   *          The {@link Path} to the server module pom.xml file for which the jaxws module should be created
   * @param artifactId
   *          The artifact id of the jaxws module to be created. Must not be blank.
   * @param env
   *          The {@link IEnvironment} in which the module is created.
   * @param progress
   *          The progress monitor
   * @return The path to the created jaxws module directory.
   * @throws IOException
   */
  public static Path createModule(Path targetModulePomFile, String artifactId, IEnvironment env, IProgress progress) throws IOException {
    return createModuleImpl(targetModulePomFile, artifactId, env, progress);
  }

  static String scoutVersionOfModule(Path targetModulePomFile, IEnvironment env) {
    var serverModuleDir = targetModulePomFile.getParent();
    return ScoutApi.version(serverModuleDir, env)
        .map(ApiVersion::asString)
        .orElseThrow(() -> newFail("Cannot compute Scout version of module at '{}'.", serverModuleDir));
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  static Path createModuleImpl(Path targetModulePomFile, String artifactId, IEnvironment env, IProgress progress) throws IOException {
    // validate input
    Ensure.isFile(targetModulePomFile, "Target module pom file '{}' could not be found.", targetModulePomFile);
    var parentDir = Ensure.isDirectory(targetModulePomFile.getParent(), "Unknown module structure. Cannot find parent of pom '{}'.", targetModulePomFile);
    var targetDirectory = Ensure.isDirectory(parentDir.getParent(), "Unknown module structure. Cannot find parent of '{}'.", parentDir);
    Ensure.notBlank(artifactId);

    // read values from target pom
    var targetModulePomDocument = Xml.get(targetModulePomFile);
    var groupId = Pom.groupId(targetModulePomDocument)
        .orElseThrow(() -> newFail("Unable to calculate {} for new module.", IMavenConstants.GROUP_ID));
    var version = Pom.version(targetModulePomDocument)
        .orElseThrow(() -> newFail("Unable to calculate {} for new module.", IMavenConstants.VERSION));
    var parentArtifactId = Pom.parentArtifactId(targetModulePomDocument)
        .orElseThrow(() -> newFail("Unable to calculate parent for new module."));
    var displayName = Xml.firstChildElement(targetModulePomDocument.getDocumentElement(), IMavenConstants.NAME)
        .map(Element::getTextContent)
        .orElse("Server Web Services");

    var tempDirectory = Files.createTempDirectory("jaxws-module-tmp");

    String createdProjectName;
    try {
      var archetypeBuild = new MavenBuild()
          .withWorkingDirectory(tempDirectory)
          .withGoal(MavenBuild.GOAL_ARCHETYPE_GENERATE)
          .withProperty(MavenBuild.PROPERTY_INTERACTIVE_MODE, "false")
          .withProperty(MavenBuild.PROPERTY_ARCHETYPE_GROUP_ID, ScoutProjectNewHelper.SCOUT_ARCHETYPES_GROUP_ID)
          .withProperty(MavenBuild.PROPERTY_ARCHETYPE_ARTIFACT_ID, ScoutProjectNewHelper.SCOUT_ARCHETYPES_JAXWS_MODULE_ID)
          .withProperty(MavenBuild.PROPERTY_ARCHETYPE_VERSION, scoutVersionOfModule(targetModulePomFile, env))
          .withProperty(IMavenConstants.GROUP_ID, groupId)
          .withProperty(IMavenConstants.ARTIFACT_ID, artifactId)
          .withProperty(IMavenConstants.VERSION, version)
          .withProperty(MavenBuild.PROPERTY_PACKAGE, "not.used") // package value must be specified, but this variable is not used by the archetype
          .withProperty("displayName", displayName)
          .withProperty("parentArtifactId", parentArtifactId);

      // execute archetype generation
      MavenRunner.execute(archetypeBuild, env, progress);
      //noinspection NestedTryStatement
      try (var files = Files.list(tempDirectory)) {
        var createdProjectDir = files.findAny().orElseThrow(() -> new IOException("Created project dir not found. Project creation failed."));
        deleteGitKeepFiles(createdProjectDir);
        Files.createDirectories(createdProjectDir.resolve(ISourceFolders.GENERATED_WS_IMPORT_SOURCE_FOLDER));
        Files.createDirectories(createdProjectDir.resolve(ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER));
        createdProjectName = createdProjectDir.getFileName().toString();

        // move to final destination
        CoreUtils.moveDirectory(createdProjectDir, targetDirectory);
      }
    }
    finally {
      CoreUtils.deleteDirectory(tempDirectory);
    }

    registerNewModuleInParent(targetModulePomFile, targetModulePomDocument, groupId, artifactId, version, parentArtifactId);
    addDependencyToTargetModule(targetModulePomFile, groupId, artifactId);

    return targetDirectory.resolve(createdProjectName);
  }

  static void deleteGitKeepFiles(Path createdProjectDir) throws IOException {
    // delete .gitkeep files
    Files.delete(Ensure.isFile(createdProjectDir.resolve(JaxWsUtils.MODULE_REL_WEB_INF_FOLDER_PATH + "/wsdl/.gitkeep")));
    Files.delete(Ensure.isFile(createdProjectDir.resolve(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER + "/.gitkeep")));
  }

  static void registerNewModuleInParent(Path targetModulePomFile, Document targetModulePom, String groupId, String artifactId, String version, String parentArtifactId) throws IOException {
    var parentPomFile = getParentPomOf(targetModulePomFile, targetModulePom);
    if (parentPomFile == null || !Files.isReadable(parentPomFile) || !Files.isRegularFile(parentPomFile)) {
      SdkLog.warning("Parent pom for new JAX-WS module could not be found. New module will not be registered.");
      return;
    }

    try {
      var parentPom = Xml.get(parentPomFile);

      // add module
      var modulesElement = JaxWsUtils.getOrCreateElement(parentPom.getDocumentElement(), IMavenConstants.MODULES);
      var newModuleElement = parentPom.createElement(IMavenConstants.MODULE);
      newModuleElement.setTextContent("../" + artifactId);
      modulesElement.appendChild(newModuleElement);

      // add dependency management
      var dependencyManagementElement = JaxWsUtils.getOrCreateElement(parentPom.getDocumentElement(), IMavenConstants.DEPENDENCY_MANAGEMENT);
      var dependenciesElement = JaxWsUtils.getOrCreateElement(dependencyManagementElement, IMavenConstants.DEPENDENCIES);

      var newDependencyElement = createDependencyElement(groupId, artifactId, parentPom);
      dependenciesElement.appendChild(newDependencyElement);
      var newVersionElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.VERSION);

      var properties = Xml.firstChildElement(parentPom.getDocumentElement(), IMavenConstants.PROPERTIES);
      if (properties.isPresent()) {
        var suffixes = new String[]{".version", "_version"};
        Arrays.stream(suffixes)
            .map(suffix -> groupId + JavaTypes.C_DOT + parentArtifactId + suffix)
            .filter(versionPropertyName -> Xml.firstChildElement(properties.orElseThrow(), versionPropertyName).isPresent())
            .findFirst()
            .ifPresent(versionPropertyName -> newVersionElement.setTextContent("${" + versionPropertyName + '}'));
      }
      if (Strings.isEmpty(newVersionElement.getTextContent())) {
        newVersionElement.setTextContent(version);
      }

      Xml.writeDocument(parentPom, true, parentPomFile);
    }
    catch (TransformerException e) {
      throw new IOException(e);
    }
  }

  private static Element createDependencyElement(String groupId, String artifactId, Document parentPom) {
    var newDependencyElement = parentPom.createElement(IMavenConstants.DEPENDENCY);

    var newGroupIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.GROUP_ID);
    newGroupIdElement.setTextContent(groupId);
    var newArtifactIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.ARTIFACT_ID);
    newArtifactIdElement.setTextContent(artifactId);

    return newDependencyElement;
  }

  static void addDependencyToTargetModule(Path targetModulePomFile, String groupId, String artifactId) throws IOException {
    try {
      var pom = Xml.get(targetModulePomFile);
      var dependenciesElement = JaxWsUtils.getOrCreateElement(pom.getDocumentElement(), IMavenConstants.DEPENDENCIES);
      var dependencyElement = createDependencyElement(groupId, artifactId, pom);
      dependenciesElement.appendChild(dependencyElement);
      Xml.writeDocument(pom, true, targetModulePomFile);
    }
    catch (TransformerException e) {
      throw new IOException(e);
    }
  }
}
