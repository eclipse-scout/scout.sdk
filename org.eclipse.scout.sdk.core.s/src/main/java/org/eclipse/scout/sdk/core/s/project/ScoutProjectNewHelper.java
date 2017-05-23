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
package org.eclipse.scout.sdk.core.s.project;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.cli.CLIManager;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <h3>{@link ScoutProjectNewHelper}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class ScoutProjectNewHelper {

  public static final String SCOUT_ARCHETYPES_VERSION = "7.0.0.004_RC1";
  public static final String SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID = "scout-helloworld-app";
  public static final String SCOUT_ARCHETYPES_GROUP_ID = "org.eclipse.scout.archetypes";

  public static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("[^\"\\/<>=:]+");
  public static final Pattern SYMBOLIC_NAME_PATTERN = Pattern.compile("^[a-z]{1}[a-z0-9_]{0,32}(\\.[a-z]{1}[a-z0-9_]{0,32}){0,16}$");
  public static final String DEFAULT_JAVA_ENV = "1.8";

  private ScoutProjectNewHelper() {
  }

  public static void createProject(File workingDir, String groupId, String artifactId, String displayName) throws IOException {
    createProject(workingDir, groupId, artifactId, displayName, null);
  }

  public static void createProject(File workingDir, String groupId, String artifactId, String displayName, String javaVersion) throws IOException {
    createProject(workingDir, groupId, artifactId, displayName, javaVersion, null, null, null);
  }

  @SuppressWarnings("squid:S00107")
  public static void createProject(File workingDir, String groupId, String artifactId, String displayName, String javaVersion,
      String archetypeGroupId, String archeTypeArtifactId, String archetypeVersion) throws IOException {

    // validate input
    Validate.notNull(workingDir);
    String groupIdMsg = getMavenNameErrorMessage(groupId, "groupId");
    if (groupIdMsg != null) {
      throw new IllegalArgumentException(groupIdMsg);
    }
    String artifactIdMsg = getMavenNameErrorMessage(artifactId, "artifactId");
    if (artifactIdMsg != null) {
      throw new IllegalArgumentException(artifactIdMsg);
    }
    String displayNameMsg = getDisplayNameErrorMEssage(displayName);
    if (displayNameMsg != null) {
      throw new IllegalArgumentException(displayNameMsg);
    }
    if (StringUtils.isEmpty(javaVersion)) {
      javaVersion = DEFAULT_JAVA_ENV;
    }
    if (StringUtils.isBlank(archetypeGroupId) || StringUtils.isBlank(archeTypeArtifactId) || StringUtils.isBlank(archetypeVersion)) {
      // use default
      archetypeGroupId = SCOUT_ARCHETYPES_GROUP_ID;
      archeTypeArtifactId = SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID;
      archetypeVersion = SCOUT_ARCHETYPES_VERSION;
    }

    String pck = null;
    if (groupId.equals(artifactId)) {
      pck = artifactId;
    }
    else {
      pck = new StringBuilder(groupId).append('.').append(artifactId).toString();
    }

    // create command
    String[] authKeysForWar = generateKeyPair();
    String[] authKeysForDev = generateKeyPair();
    MavenBuild archetypeBuild = new MavenBuild()
        .withWorkingDirectory(workingDir)
        .withGoal("archetype:generate")
        .withOption(CLIManager.BATCH_MODE)
        .withProperty("archetypeGroupId", archetypeGroupId)
        .withProperty("archetypeArtifactId", archeTypeArtifactId)
        .withProperty("archetypeVersion", archetypeVersion)
        .withProperty("groupId", groupId)
        .withProperty("artifactId", artifactId)
        .withProperty("version", "1.0.0-SNAPSHOT")
        .withProperty("package", pck)
        .withProperty("displayName", displayName)
        .withProperty("scoutAuthPublicKey", authKeysForWar[1])
        .withProperty("scoutAuthPrivateKey", authKeysForWar[0])
        .withProperty("scoutAuthPublicKeyDev", authKeysForDev[1])
        .withProperty("scoutAuthPrivateKeyDev", authKeysForDev[0])
        .withProperty("javaVersion", javaVersion)
        .withProperty("userName", CoreUtils.getUsername());

    // execute archetype generation
    MavenRunner.execute(archetypeBuild);

    postProcessRootPom(new File(workingDir, artifactId));
  }

  static String[] generateKeyPair() {
    try {
      return CoreUtils.generateKeyPair();
    }
    catch (GeneralSecurityException e) {
      SdkLog.warning("Could not generate a new key pair.", e);
      String keyPlaceholder = "TODO_use_org.eclipse.scout.rt.platform.security.SecurityUtility.main(String[]))";
      return new String[]{keyPlaceholder, keyPlaceholder};
    }
  }

  /**
   * Workaround so that only the parent module is referenced in the root (remove non-parent modules)
   */
  static void postProcessRootPom(File targetDirectory) throws IOException {
    try {
      File pom = new File(targetDirectory, IMavenConstants.POM);
      if (!pom.isFile()) {
        return;
      }

      DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
      Document doc = docBuilder.parse(pom);

      Element modules = CoreUtils.getFirstChildElement(doc.getDocumentElement(), "modules");

      NodeList childNodes = modules.getChildNodes();
      List<Node> nodesToRemove = new ArrayList<>();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node n = childNodes.item(i);
        if (n.getNodeType() == Node.TEXT_NODE
            || (n.getNodeType() == Node.ELEMENT_NODE && "module".equals(((Element) n).getTagName()) && !targetDirectory.getName().equals(n.getTextContent().trim()))) {
          nodesToRemove.add(n);
        }
      }
      for (Node n : nodesToRemove) {
        modules.removeChild(n);
      }

      Validate.isTrue(modules.getChildNodes().getLength() == 1, "Parent module is missing in root pom.");
      writeDocument(doc, new StreamResult(pom));
    }
    catch (ParserConfigurationException | SAXException | TransformerException e) {
      throw new IOException(e);
    }
  }

  static void writeDocument(Document document, Result result) throws TransformerException {
    Transformer transformer = CoreUtils.createTransformer(false);
    transformer.transform(new DOMSource(document), result);
  }

  public static String getDisplayNameErrorMEssage(String displayNameCandidate) {
    if (StringUtils.isEmpty(displayNameCandidate)) {
      return "Display Name is not set.";
    }
    if (!DISPLAY_NAME_PATTERN.matcher(displayNameCandidate).matches()) {
      return "The Display Name must not contain these characters: \\\"/<>:=";
    }
    return null;
  }

  public static String getMavenNameErrorMessage(String symbolicNameCandidate, String attribName) {
    if (StringUtils.isEmpty(symbolicNameCandidate)) {
      return attribName + " is not set.";
    }
    if (!SYMBOLIC_NAME_PATTERN.matcher(symbolicNameCandidate).matches()) {
      return "The " + attribName + " value is not valid.";
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(symbolicNameCandidate);
    if (jkw != null) {
      return "The " + attribName + " must not contain the Java keyword '" + jkw + "'.";
    }
    return null;
  }

  private static String getContainingJavaKeyWord(String s) {
    for (String keyWord : CoreUtils.getJavaKeyWords()) {
      if (s.startsWith(keyWord + ".") || s.endsWith("." + keyWord) || s.contains("." + keyWord + ".")) {
        return keyWord;
      }
    }
    return null;
  }
}
