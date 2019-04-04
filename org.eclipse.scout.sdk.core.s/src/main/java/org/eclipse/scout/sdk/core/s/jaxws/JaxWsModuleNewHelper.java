/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.log.SdkLog;
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
    Optional<Element> parent = Xml.firstChildElement(modulePomDocument.getDocumentElement(), IMavenConstants.PARENT);
    if (parent.isPresent()) {
      Optional<Element> relPat = Xml.firstChildElement(parent.get(), IMavenConstants.RELATIVE_PATH);
      if (relPat.isPresent()) {
        String path = relPat.get().getTextContent();
        if (Strings.isBlank(path)) {
          // parent is resolved from the repository. see http://maven.apache.org/ref/3.0.3/maven-model/maven.html#class_parent
          return null;
        }

        if (!path.endsWith(IMavenConstants.POM)) {
          if (path.charAt(path.length() - 1) != '/') {
            path += '/';
          }
          path += IMavenConstants.POM;
        }
        return modulePomFile.getParent().resolve(path).normalize();
      }
    }
    return modulePomFile.getParent().getParent().resolve(IMavenConstants.POM);
  }

  public static Path getParentPomOf(Path projectPomFile) throws IOException {
    return getParentPomOf(projectPomFile, Xml.get(projectPomFile));
  }

  public static Path createModule(Path targetModulePomFile, String artifactId) throws IOException {
    return createModuleImpl(targetModulePomFile, artifactId);
  }

  static Path createModuleImpl(Path targetModulePomFile, String artifactId) throws IOException {
    // validate input
    Ensure.isFile(targetModulePomFile, "Target module pom file '{}' could not be found.", targetModulePomFile);
    Ensure.notBlank(artifactId);
    Path targetDirectory = targetModulePomFile.getParent().getParent();
    Ensure.isDirectory(targetDirectory, "Target directory '{}' could not be found.", targetDirectory);

    // read values from target pom
    Document targetModulePomDocument = Xml.get(targetModulePomFile);
    String groupId = Pom.groupId(targetModulePomDocument)
        .orElseThrow(() -> newFail("Unable to calculate {} for new module.", IMavenConstants.GROUP_ID));
    String version = Pom.version(targetModulePomDocument)
        .orElseThrow(() -> newFail("Unable to calculate {} for new module.", IMavenConstants.VERSION));
    String parentArtifactId = Pom.parentArtifactId(targetModulePomDocument)
        .orElseThrow(() -> newFail("Unable to calculate parent for new module."));
    String displayName = Xml.firstChildElement(targetModulePomDocument.getDocumentElement(), IMavenConstants.NAME)
        .map(Element::getTextContent)
        .orElse("Server Web Services");

    Path tempDirectory = Files.createTempDirectory("jaxws-module-tmp");
    String createdProjectName;
    try {
      MavenBuild archetypeBuild = new MavenBuild()
          .withWorkingDirectory(tempDirectory)
          .withGoal("archetype:generate")
          .withOption(MavenBuild.OPTION_BATCH_MODE)
          .withProperty("archetypeGroupId", "org.eclipse.scout.archetypes")
          .withProperty("archetypeArtifactId", "scout-jaxws-module")
          .withProperty("archetypeVersion", ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION)
          .withProperty("groupId", groupId)
          .withProperty("artifactId", artifactId)
          .withProperty("version", version)
          .withProperty("package", "not.used") // we must specify a package value, but this variable is not used by the archetype
          .withProperty("displayName", displayName)
          .withProperty("parentArtifactId", parentArtifactId);

      // execute archetype generation
      MavenRunner.execute(archetypeBuild);
      try (Stream<Path> files = Files.list(tempDirectory)) {
        Path createdProjectDir = files.findAny().orElseThrow(() -> new IOException("Created project dir not found. Project creation failed."));
        deleteGitKeepFiles(createdProjectDir);
        Files.createDirectories(createdProjectDir.resolve(ISourceFolders.GENERATED_WSIMPORT_SOURCE_FOLDER));
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
    Files.delete(Ensure.isFile(createdProjectDir.resolve(JaxWsUtils.MODULE_REL_WEBINF_FOLDER_PATH + "/wsdl/.gitkeep")));
    Files.delete(Ensure.isFile(createdProjectDir.resolve(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER + "/.gitkeep")));
  }

  static void registerNewModuleInParent(Path targetModulePomFile, Document targetModulePom, String groupId, String artifactId, String version, String parentArtifactId) throws IOException {
    Path parentPomFile = getParentPomOf(targetModulePomFile, targetModulePom);
    if (parentPomFile == null || !Files.isReadable(parentPomFile) || !Files.isRegularFile(parentPomFile)) {
      SdkLog.warning("Parent pom for new JAX-WS module could not be found. New module will not be registered.");
      return;
    }

    try {
      Document parentPom = Xml.get(parentPomFile);

      // add module
      Element modulesElement = JaxWsUtils.getOrCreateElement(parentPom.getDocumentElement(), IMavenConstants.MODULES);
      Element newModuleElement = parentPom.createElement(IMavenConstants.MODULE);
      newModuleElement.setTextContent("../" + artifactId);
      modulesElement.appendChild(newModuleElement);

      // add dependency management
      Element dependencyManagementElement = JaxWsUtils.getOrCreateElement(parentPom.getDocumentElement(), IMavenConstants.DEPENDENCY_MANAGEMENT);
      Element dependenciesElement = JaxWsUtils.getOrCreateElement(dependencyManagementElement, IMavenConstants.DEPENDENCIES);

      Element newDependencyElement = parentPom.createElement(IMavenConstants.DEPENDENCY);
      dependenciesElement.appendChild(newDependencyElement);
      Element newGroupIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.GROUP_ID);
      newGroupIdElement.setTextContent(groupId);
      Element newArtifactIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.ARTIFACT_ID);
      newArtifactIdElement.setTextContent(artifactId);
      Element newVersionElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.VERSION);

      Optional<Element> properties = Xml.firstChildElement(parentPom.getDocumentElement(), IMavenConstants.PROPERTIES);
      if (properties.isPresent()) {
        String[] suffixes = {".version", "_version"};
        for (String suffix : suffixes) {
          String versionPropertyName = groupId + JavaTypes.C_DOT + parentArtifactId + suffix;
          if (Xml.firstChildElement(properties.get(), versionPropertyName).isPresent()) {
            newVersionElement.setTextContent("${" + versionPropertyName + '}');
            break;
          }
        }
      }
      if (Strings.isEmpty(newVersionElement.getTextContent())) {
        newVersionElement.setTextContent(version);
      }

      writeDocument(parentPom, parentPomFile);
    }
    catch (TransformerException e) {
      throw new IOException(e);
    }
  }

  static void writeDocument(Document document, Path file) throws TransformerException, IOException {
    Transformer transformer = Xml.createTransformer(true);
    try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      transformer.transform(new DOMSource(document), new StreamResult(out));
    }
  }

  static void addDependencyToTargetModule(Path targetModulePomFile, String groupId, String artifactId) throws IOException {
    try {
      Document pom = Xml.get(targetModulePomFile);
      Element dependenciesElement = JaxWsUtils.getOrCreateElement(pom.getDocumentElement(), IMavenConstants.DEPENDENCIES);
      Element newDependencyElement = pom.createElement(IMavenConstants.DEPENDENCY);
      dependenciesElement.appendChild(newDependencyElement);
      Element newGroupIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.GROUP_ID);
      newGroupIdElement.setTextContent(groupId);
      Element newArtifactIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.ARTIFACT_ID);
      newArtifactIdElement.setTextContent(artifactId);
      writeDocument(pom, targetModulePomFile);
    }
    catch (TransformerException e) {
      throw new IOException(e);
    }
  }
}
