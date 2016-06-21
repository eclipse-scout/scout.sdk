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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.CoreScoutUtils;
import org.eclipse.scout.sdk.core.s.util.MavenCliRunner;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <h3>{@link JaxWsModuleNewHelper}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class JaxWsModuleNewHelper {

  private JaxWsModuleNewHelper() {
  }

  public static File getParentPomOf(File modulePomFile, Document modulePomDocument) {
    Element parent = CoreUtils.getFirstChildElement(modulePomDocument.getDocumentElement(), IMavenConstants.PARENT);
    if (parent != null) {
      Element relPat = CoreUtils.getFirstChildElement(parent, IMavenConstants.RELATIVE_PATH);
      if (relPat != null) {
        String path = relPat.getTextContent();
        if (StringUtils.isBlank(path)) {
          // parent is resolved from the repository. see http://maven.apache.org/ref/3.0.3/maven-model/maven.html#class_parent
          return null;
        }

        if (!path.endsWith(IMavenConstants.POM)) {
          if (path.charAt(path.length() - 1) != '/') {
            path += '/';
          }
          path += IMavenConstants.POM;
        }
        return modulePomFile.getParentFile().toPath().resolve(path).normalize().toFile();
      }
    }
    return new File(modulePomFile.getParentFile().getParentFile(), IMavenConstants.POM);
  }

  public static File getParentPomOf(File projectPomFile) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
    Document doc = docBuilder.parse(projectPomFile);
    return getParentPomOf(projectPomFile, doc);
  }

  public static File createModule(File targetModulePomFile, String artifactId, String mavenGlobalSettings, String mavenSettings) throws IOException {
    try {
      return createModule(targetModulePomFile.getParentFile().getParentFile(), targetModulePomFile, artifactId, mavenGlobalSettings, mavenSettings);
    }
    catch (ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }

  static File createModule(File targetDirectory, File targetModulePomFile, String artifactId, String mavenGlobalSettings, String mavenSettings) throws IOException, ParserConfigurationException, SAXException {
    // validate input
    Validate.notNull(targetDirectory);
    Validate.notNull(targetModulePomFile);
    Validate.notNull(artifactId);
    Validate.isTrue(targetDirectory.isDirectory(), "Target directory could not be found.");
    Validate.isTrue(targetModulePomFile.isFile(), "Target module pom file could not be found.");

    // read values from target pom
    DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
    Document targetModulePomDocument = docBuilder.parse(targetModulePomFile);
    String groupId = CoreScoutUtils.getGroupIdOfPom(targetModulePomDocument);
    if (StringUtils.isBlank(groupId)) {
      throw new IOException("Unable to calculate " + IMavenConstants.GROUP_ID + " for new module.");
    }
    String version = CoreScoutUtils.getVersionOfPom(targetModulePomDocument);
    if (StringUtils.isBlank(version)) {
      throw new IOException("Unable to calculate " + IMavenConstants.VERSION + " for new module.");
    }

    String parentArtifactId = CoreScoutUtils.getParentArtifactId(targetModulePomDocument);
    if (StringUtils.isBlank(version)) {
      throw new IOException("Unable to calculate parent for new module.");
    }

    String displayName = null;
    Element nameElement = CoreUtils.getFirstChildElement(targetModulePomDocument.getDocumentElement(), IMavenConstants.NAME);
    if (nameElement != null) {
      displayName = nameElement.getTextContent();
    }
    else {
      displayName = "Server Web Services";
    }

    // generate model using archetype
    String[] args = new String[]{"archetype:generate", "-B",
        "-DarchetypeGroupId=org.eclipse.scout.archetypes", "-DarchetypeArtifactId=scout-jaxws-module", "-DarchetypeVersion=" + ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION,
        "-DgroupId=" + groupId, "-DartifactId=" + artifactId, "-Dversion=" + version, "-Dpackage=''",
        "-DdisplayName=" + displayName, "-DparentArtifactId=" + parentArtifactId
    };

    File tempDirectory = Files.createTempDirectory("jaxws-module-tmp").toFile();
    String createdProjectName = null;
    try {
      // execute archetype generation
      new MavenCliRunner().execute(tempDirectory, args, mavenGlobalSettings, mavenSettings);

      // move to final destination
      File[] listFiles = tempDirectory.listFiles();
      if (listFiles == null || listFiles.length < 1) {
        throw new IOException("created project dir not found. Project creation failed.");
      }
      File createdProjectDir = listFiles[0];
      createdProjectName = createdProjectDir.getName();
      CoreUtils.moveDirectory(createdProjectDir, targetDirectory);
    }
    finally {
      CoreUtils.deleteDirectory(tempDirectory);
    }

    registerNewModuleInParent(targetModulePomFile, targetModulePomDocument, groupId, artifactId, version, parentArtifactId);
    addDependencyToTargetModule(targetModulePomFile, groupId, artifactId);

    return new File(targetDirectory, createdProjectName);
  }

  static void registerNewModuleInParent(File targetModulePomFile, Document targetModulePom, String groupId, String artifactId, String version, String parentArtifactId) throws IOException {
    File parentPomFile = getParentPomOf(targetModulePomFile, targetModulePom);
    if (parentPomFile == null || !parentPomFile.isFile()) {
      SdkLog.warning("Parent pom for new JAX-WS module could not be found. New module will not be registered.");
      return;
    }

    try {
      DocumentBuilder createDocumentBuilder = CoreUtils.createDocumentBuilder();
      Document parentPom = createDocumentBuilder.parse(parentPomFile);

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

      Element properties = CoreUtils.getFirstChildElement(parentPom.getDocumentElement(), IMavenConstants.PROPERTIES);
      if (properties != null) {
        String[] suffixes = new String[]{".version", "_version"};
        for (String suffix : suffixes) {
          String versionPropertyName = groupId + '.' + parentArtifactId + suffix;
          if (CoreUtils.getFirstChildElement(properties, versionPropertyName) != null) {
            newVersionElement.setTextContent("${" + versionPropertyName + '}');
            break;
          }
        }
      }
      if (StringUtils.isEmpty(newVersionElement.getTextContent())) {
        newVersionElement.setTextContent(version);
      }

      writeDocument(parentPom, parentPomFile);
    }
    catch (TransformerException | ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }

  static void writeDocument(Document document, File file) throws TransformerException {
    Transformer transformer = CoreUtils.createTransformer(true);
    transformer.transform(new DOMSource(document), new StreamResult(file));
  }

  static void addDependencyToTargetModule(File targetModulePomFile, String groupId, String artifactId) throws IOException {
    try {
      DocumentBuilder createDocumentBuilder = CoreUtils.createDocumentBuilder();
      Document pom = createDocumentBuilder.parse(targetModulePomFile);
      Element dependenciesElement = JaxWsUtils.getOrCreateElement(pom.getDocumentElement(), IMavenConstants.DEPENDENCIES);
      Element newDependencyElement = pom.createElement(IMavenConstants.DEPENDENCY);
      dependenciesElement.appendChild(newDependencyElement);
      Element newGroupIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.GROUP_ID);
      newGroupIdElement.setTextContent(groupId);
      Element newArtifactIdElement = JaxWsUtils.getOrCreateElement(newDependencyElement, IMavenConstants.ARTIFACT_ID);
      newArtifactIdElement.setTextContent(artifactId);
      writeDocument(pom, targetModulePomFile);
    }
    catch (TransformerException | ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }
}
