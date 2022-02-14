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
package org.eclipse.scout.sdk.core.s.project;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.xml.transform.TransformerException;

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.apidef.ApiVersion;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.MavenArtifactVersions;
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
@SuppressWarnings("squid:S00107") // Methods should not have too many parameters
public final class ScoutProjectNewHelper {

  public static final String SCOUT_ARCHETYPES_GROUP_ID = "org.eclipse.scout.archetypes";

  public static final String SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID = "scout-helloworld-app";
  public static final String SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID = "scout-hellojs-app";
  public static final String SCOUT_ARCHETYPES_JAXWS_MODULE_ID = "scout-jaxws-module";

  public static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("[^\"/<>=:]+");
  public static final Pattern SYMBOLIC_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,32}(?:\\.[a-z][a-z0-9_]{0,32}){0,16}$");

  private ScoutProjectNewHelper() {
  }

  public static void createProject(Path workingDir, String groupId, String artifactId, String displayName, boolean useJsUiLanguage, String javaVersion, IEnvironment env, IProgress progress) throws IOException {
    createProject(workingDir, groupId, artifactId, displayName, useJsUiLanguage, javaVersion, null /* use default (latest) */, env, progress);
  }

  public static void createProject(Path workingDir, String groupId, String artifactId, String displayName, boolean useJsUiLanguage, String javaVersion, String archetypeVersion, IEnvironment env, IProgress progress) throws IOException {
    String archetypeArtifactId;
    if (useJsUiLanguage) {
      archetypeArtifactId = ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID;
    }
    else {
      archetypeArtifactId = ScoutProjectNewHelper.SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID;
    }
    createProject(workingDir, groupId, artifactId, displayName, javaVersion, null /* use default */, archetypeArtifactId, archetypeVersion, env, progress);
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
    if (Strings.isBlank(archetypeGroupId)) {
      archetypeGroupId = SCOUT_ARCHETYPES_GROUP_ID;
    }
    if (Strings.isBlank(archetypeArtifactId)) {
      archetypeArtifactId = SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID;
    }
    if (Strings.isBlank(archetypeVersion)) {
      archetypeVersion = IMavenConstants.LATEST;
    }
    if (Strings.isEmpty(javaVersion)) {
      // no java version specified: use the highest version supported by the selected scout version
      var selectedJavaVersion = Arrays.stream(getSupportedJavaVersions(archetypeVersion)).max().orElseThrow();
      javaVersion = Integer.toString(selectedJavaVersion);
    }

    var pck = getPackage(groupId, artifactId);
    var artifactName = getArtifactName(artifactId);

    // create command
    Files.createDirectories(workingDir);
    var archetypeBuild = new MavenBuild()
        .withWorkingDirectory(workingDir)
        .withGoal(MavenBuild.GOAL_ARCHETYPE_GENERATE)
        .withProperty(MavenBuild.PROPERTY_INTERACTIVE_MODE, "false")
        .withProperty(MavenBuild.PROPERTY_ARCHETYPE_GROUP_ID, archetypeGroupId)
        .withProperty(MavenBuild.PROPERTY_ARCHETYPE_ARTIFACT_ID, archetypeArtifactId)
        .withProperty(MavenBuild.PROPERTY_ARCHETYPE_VERSION, archetypeVersion)
        .withProperty(IMavenConstants.GROUP_ID, groupId)
        .withProperty(IMavenConstants.ARTIFACT_ID, artifactId)
        .withProperty(IMavenConstants.VERSION, "1.0.0-SNAPSHOT") // requires 3 digits because also used as SemVer
        .withProperty(MavenBuild.PROPERTY_PACKAGE, pck)
        .withProperty("displayName", displayName)
        .withProperty("javaVersion", javaVersion)
        .withProperty("simpleArtifactName", artifactName)
        .withProperty("userName", CoreUtils.getUsername());
    if (SdkLog.isDebugEnabled()) {
      // enables Groovy debug log file
      archetypeBuild.withProperty("debug", "true");
    }

    // execute archetype generation
    MavenRunner.execute(archetypeBuild, env, progress);

    postProcessRootPom(workingDir.resolve(artifactId));
  }

  /**
   * Workaround so that only the parent module is referenced in the root (remove non-parent modules).<br>
   * Otherwise, maven returns errors like
   * {@code org.apache.maven.project.DuplicateProjectException: Project 'xyz' is duplicated
   * in the reactor}.
   */
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  static void postProcessRootPom(Path targetDirectory) throws IOException {
    try {
      var pom = targetDirectory.resolve(IMavenConstants.POM);
      if (!Files.isReadable(pom) || !Files.isRegularFile(pom)) {
        return;
      }

      var doc = Xml.get(pom);
      var modules = Xml.firstChildElement(doc.getDocumentElement(), IMavenConstants.MODULES).orElseThrow();
      var childNodes = modules.getChildNodes();
      var targetDirectoryName = targetDirectory.getFileName().toString();
      var nodesToRemove = IntStream.range(0, childNodes.getLength())
          .mapToObj(childNodes::item)
          .filter(n -> isNodeToRemove(n, targetDirectoryName))
          .collect(toList());
      nodesToRemove.forEach(modules::removeChild);

      Ensure.isTrue(modules.getChildNodes().getLength() == 1, "Parent module is missing in root pom.");
      Xml.writeDocument(doc, true, pom);
    }
    catch (TransformerException e) {
      throw new IOException(e);
    }
  }

  static boolean isNodeToRemove(Node n, CharSequence targetDirectoryName) {
    if (n == null) {
      return false;
    }
    if (n.getNodeType() == Node.TEXT_NODE) {
      return true;
    }
    return n.getNodeType() == Node.ELEMENT_NODE
        && IMavenConstants.MODULE.equals(((Element) n).getTagName())
        && !Strings.equals(targetDirectoryName, Strings.trim(n.getTextContent()));
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
        .findAny()
        .orElse(null);
  }

  /**
   * Gets all major Java versions (e.g. 8 or 11 or 17) that are officially supported by the Scout version given.
   * 
   * @param scoutVersion
   *          The Scout version (e.g. 11.0.14 or 22.0-SNAPSHOT or LATEST).
   * @return The supported major Java versions (e.g. 8 or 11 or 17) for the Scout version given. If the given Scout
   *         version is invalid, the supported Java version of the newest Scout version is returned.
   */
  public static int[] getSupportedJavaVersions(String scoutVersion) {
    ApiVersion scoutApiVersion;
    if (Strings.isBlank(scoutVersion) || IMavenConstants.LATEST.equalsIgnoreCase(scoutVersion)) {
      scoutApiVersion = ApiVersion.LATEST;
    }
    else {
      scoutApiVersion = ApiVersion.parse(scoutVersion).orElse(ApiVersion.LATEST);
    }
    return Api.create(IScoutApi.class, scoutApiVersion).supportedJavaVersions();
  }

  /**
   * Gets all Scout versions available on Maven central which are supported by this SDK.
   * 
   * @param useJavaClient
   *          {@code true} if the versions for the archetype with the Java UI should be returned, {@code false} for the
   *          versions of the archetype with the JavaScript UI.
   * @param includePreviewVersions
   *          {@code true} if preview releases should be included in the result list.
   * @return A {@link List} holding the versions available on Maven central sorted descending (the newest version
   *         first).
   * @throws IOException
   *           if there is an error reading the versions from Maven central
   */
  public static List<String> getSupportedArchetypeVersions(boolean useJavaClient, boolean includePreviewVersions) throws IOException {
    var artifactId = useJavaClient ? SCOUT_ARCHETYPES_HELLOWORLD_ARTIFACT_ID : SCOUT_ARCHETYPES_HELLOJS_ARTIFACT_ID;
    var min = ScoutApi.allKnown()
        .map(IApiSpecification::maxLevel)
        .min(naturalOrder())
        .orElseThrow();
    return MavenArtifactVersions.allOnCentral(SCOUT_ARCHETYPES_GROUP_ID, artifactId)
        .map(ApiVersion::parse)
        .flatMap(Optional::stream)
        .filter(v -> v.compareCommonSegmentsTo(min) >= 0) // only supported versions
        .filter(v -> includePreviewVersions || Strings.isEmpty(v.suffix())) // include preview versions?
        .map(ApiVersion::asString)
        .collect(toList());
  }
}
