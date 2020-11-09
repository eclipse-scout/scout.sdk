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
package org.eclipse.scout.sdk.core.s.project;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.xml.transform.TransformerException;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.s.util.maven.MavenRunner;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <h3>{@link ScoutProjectNewHelper}</h3>
 *
 * @since 5.2.0
 */
public final class ScoutProjectNewHelper {

  public static final String SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID = "scout-helloworld-app";
  public static final String SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID = "scout-hellojs-app";
  public static final String SCOUT_ARCHETYPES_JAXWS_MODULE_ID = "scout-jaxws-module";
  public static final String SCOUT_ARCHETYPES_GROUP_ID = "org.eclipse.scout.archetypes";

  public static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("[^\"/<>=:]+");
  public static final Pattern SYMBOLIC_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,32}(?:\\.[a-z][a-z0-9_]{0,32}){0,16}$");
  public static final String DEFAULT_JAVA_ENV = "11";

  private ScoutProjectNewHelper() {
  }

  public static void createProject(Path workingDir, String groupId, String artifactId, String displayName, IEnvironment env, IProgress progress) throws IOException {
    createProject(workingDir, groupId, artifactId, displayName, null, env, progress);
  }

  public static void createProject(Path workingDir, String groupId, String artifactId, String displayName, String javaVersion, IEnvironment env, IProgress progress) throws IOException {
    createProject(workingDir, groupId, artifactId, displayName, javaVersion, null, null, null, env, progress);
  }

  @SuppressWarnings("squid:S00107")
  public static void createProject(Path workingDir, String groupId, String artifactId, String displayName, String javaVersion,
      String archetypeGroupId, String archetypeArtifactId, String archetypeVersion, IEnvironment env, IProgress progress) throws IOException {

    // validate input
    Ensure.notNull(workingDir);
    var groupIdMsg = getMavenGroupIdErrorMessage(groupId);
    if (groupIdMsg != null) {
      throw new IllegalArgumentException(groupIdMsg);
    }
    var artifactIdMsg = getMavenArtifactIdErrorMessage(artifactId);
    if (artifactIdMsg != null) {
      throw new IllegalArgumentException(artifactIdMsg);
    }
    var displayNameMsg = getDisplayNameErrorMessage(displayName);
    if (displayNameMsg != null) {
      throw new IllegalArgumentException(displayNameMsg);
    }
    if (Strings.isEmpty(javaVersion)) {
      javaVersion = DEFAULT_JAVA_ENV;
    }
    if (Strings.isBlank(archetypeGroupId)) {
      archetypeGroupId = SCOUT_ARCHETYPES_GROUP_ID;
    }
    if (Strings.isBlank(archetypeArtifactId)) {
      archetypeArtifactId = SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID;
    }
    if (Strings.isBlank(archetypeVersion)) {
      archetypeVersion = IMavenConstants.LATEST;
    }

    var pck = getPackage(groupId, artifactId);
    var artifactName = getArtifactName(artifactId);

    // create command
    var authKeysForWar = generateKeyPairSafe();
    var authKeysForDev = generateKeyPairSafe();
    var archetypeBuild = new MavenBuild()
        .withWorkingDirectory(workingDir)
        .withGoal("archetype:generate")
        .withOption(MavenBuild.OPTION_BATCH_MODE)
        .withProperty("archetypeGroupId", archetypeGroupId)
        .withProperty("archetypeArtifactId", archetypeArtifactId)
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
        .withProperty("simpleArtifactName", artifactName)
        .withProperty("userName", CoreUtils.getUsername());

    // execute archetype generation
    MavenRunner.execute(archetypeBuild, env, progress);

    postProcessRootPom(workingDir.resolve(artifactId));
  }

  static String getArtifactName(String artifactId) {
    var pos = artifactId.lastIndexOf('.');
    if (pos < 0 || pos >= artifactId.length() - 1) {
      return artifactId;
    }
    return artifactId.substring(pos + 1);
  }

  static String getPackage(String groupId, String artifactId) {
    if (artifactId.startsWith(groupId)) {
      return artifactId;
    }
    return new StringBuilder(groupId).append(JavaTypes.C_DOT).append(artifactId).toString();
  }

  static String[] generateKeyPairSafe() {
    try {
      return generateKeyPair();
    }
    catch (GeneralSecurityException e) {
      SdkLog.warning("Could not generate a new key pair.", e);
      var keyPlaceholder = "TODO_use_org.eclipse.scout.rt.platform.security.SecurityUtility.main(String[]))";
      return new String[]{keyPlaceholder, keyPlaceholder};
    }
  }

  /**
   * Creates a new key pair (private and public key) compatible with the Scout Runtime.<br>
   * <b>This method must behave exactly like the one implemented in
   * org.eclipse.scout.rt.platform.security.SecurityUtility.generateKeyPair().</b>
   *
   * @return A {@link String} array of length=2 containing the base64 encoded private key at index zero and the base64
   *         encoded public key at index 1.
   */
  static String[] generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
    var keyGen = KeyPairGenerator.getInstance("EC", "SunEC");
    AlgorithmParameterSpec spec = new ECGenParameterSpec("secp256k1");
    keyGen.initialize(spec, new SecureRandom());
    var keyPair = keyGen.generateKeyPair();

    EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
    EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());

    var base64Encoder = Base64.getEncoder();

    return new String[]{base64Encoder.encodeToString(pkcs8EncodedKeySpec.getEncoded()) /*private key*/, base64Encoder.encodeToString(x509EncodedKeySpec.getEncoded()) /* public key*/};
  }

  /**
   * Workaround so that only the parent module is referenced in the root (remove non-parent modules)
   */
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  static void postProcessRootPom(Path targetDirectory) throws IOException {
    try {
      var pom = targetDirectory.resolve(IMavenConstants.POM);
      if (!Files.isReadable(pom) || !Files.isRegularFile(pom)) {
        return;
      }

      var doc = Xml.get(pom);
      var modules = Xml.firstChildElement(doc.getDocumentElement(), IMavenConstants.MODULES).get();
      var childNodes = modules.getChildNodes();
      var targetDirectoryName = targetDirectory.getFileName().toString();
      var nodesToRemove = IntStream.range(0, childNodes.getLength())
          .mapToObj(childNodes::item)
          .filter(n -> isNodeToRemove(n, targetDirectoryName))
          .collect(toList());
      nodesToRemove.forEach(modules::removeChild);

      Ensure.isTrue(modules.getChildNodes().getLength() == 1, "Parent module is missing in root pom.");
      Xml.writeDocument(doc, false, pom);
    }
    catch (TransformerException e) {
      throw new IOException(e);
    }
  }

  static boolean isNodeToRemove(Node n, String targetDirectoryName) {
    if (n == null) {
      return false;
    }
    if (n.getNodeType() == Node.TEXT_NODE) {
      return true;
    }
    return n.getNodeType() == Node.ELEMENT_NODE
        && IMavenConstants.MODULE.equals(((Element) n).getTagName())
        && !targetDirectoryName.equals(n.getTextContent().trim());
  }

  public static String getDisplayNameErrorMessage(CharSequence displayNameCandidate) {
    if (Strings.isEmpty(displayNameCandidate)) {
      return "Display Name is not set.";
    }
    if (!DISPLAY_NAME_PATTERN.matcher(displayNameCandidate).matches()) {
      //noinspection HardcodedFileSeparator
      return "The Display Name must not contain these characters: \\\"/<>:=";
    }
    return null;
  }

  public static String getMavenArtifactIdErrorMessage(String artifactIdCandidate) {
    return getMavenNameErrorMessage(artifactIdCandidate, "Artifact Id");
  }

  public static String getMavenGroupIdErrorMessage(String groupIdCandidate) {
    return getMavenNameErrorMessage(groupIdCandidate, "Group Id");
  }

  private static String getMavenNameErrorMessage(String nameCandidate, String attributeName) {
    if (Strings.isEmpty(nameCandidate)) {
      return attributeName + " is not set.";
    }
    if (!SYMBOLIC_NAME_PATTERN.matcher(nameCandidate).matches()) {
      return "The " + attributeName + " value is not valid.";
    }
    // reserved java keywords
    var jkw = getContainingJavaKeyWord(nameCandidate);
    if (jkw != null) {
      return "The " + attributeName + " must not contain the Java keyword '" + jkw + "'.";
    }
    return null;
  }

  private static String getContainingJavaKeyWord(String s) {
    return JavaTypes.getJavaKeyWords().stream()
        .filter(keyWord -> s.startsWith(keyWord + JavaTypes.C_DOT) || s.endsWith(JavaTypes.C_DOT + keyWord) || s.contains(JavaTypes.C_DOT + keyWord + JavaTypes.C_DOT))
        .findFirst()
        .orElse(null);
  }
}
