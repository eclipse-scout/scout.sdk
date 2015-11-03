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
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.s.util.MavenCliRunner;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * <h3>{@link ScoutProjectNewHelper}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ScoutProjectNewHelper {

  public static final String ROOT_PROJECT_SUFFIX = ".root";
  public static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("[^\"\\/<>=:]+");
  public static final Pattern SYMBOLIC_NAME_PATTERN = Pattern.compile("^[a-z]{1}[a-z0-9_]{0,32}(\\.[a-z]{1}[a-z0-9_]{0,32}){0,16}$");
  public static final String DEFAULT_JAVA_VERSION = "1.8";

  public static void createProject(File targetDirectory, String symbolicName, String displayName) throws IOException, GeneralSecurityException {
    createProject(targetDirectory, symbolicName, displayName, null);
  }

  public static void createProject(File targetDirectory, String symbolicName, String displayName, String javaVersion) throws IOException, GeneralSecurityException {
    createProject(targetDirectory, symbolicName, displayName, javaVersion, null, null, null);
  }

  public static void createProject(File targetDirectory, String symbolicName, String displayName, String javaVersion, String groupId, String artifactId, String version) throws IOException, GeneralSecurityException {
    // validate input
    Validate.notNull(targetDirectory);
    String symbolicNameMsg = getSymbolicNameErrorMessage(symbolicName);
    if (symbolicNameMsg != null) {
      throw new IllegalArgumentException(symbolicNameMsg);
    }
    String displayNameMsg = getDisplayNameErrorMEssage(displayName);
    if (displayNameMsg != null) {
      throw new IllegalArgumentException(displayNameMsg);
    }
    if (StringUtils.isEmpty(javaVersion)) {
      javaVersion = DEFAULT_JAVA_VERSION;
    }
    if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId) || StringUtils.isBlank(version)) {
      // use default
      groupId = "org.eclipse.scout.archetypes";
      artifactId = "scout-helloworld-app";
      version = "5.2.0-SNAPSHOT";
    }

    // create command
    String[] authKeysForWar = CoreUtils.generateKeyPair();
    String[] authKeysForDev = CoreUtils.generateKeyPair();
    String[] args = new String[]{"archetype:generate", "-B", "-X",
        "-DarchetypeGroupId=" + groupId, "-DarchetypeArtifactId=" + artifactId, "-DarchetypeVersion=" + version,
        "-DgroupId=" + symbolicName, "-DartifactId=" + symbolicName + ROOT_PROJECT_SUFFIX, "-Dversion=1.0.0-SNAPSHOT", "-Dpackage=" + symbolicName,
        "-DdisplayName=" + displayName, "-DscoutAuthPublicKey=" + authKeysForWar[1], "-DscoutAuthPrivateKey=" + authKeysForWar[0], "-DscoutAuthPublicKeyDev=" + authKeysForDev[1], "-DscoutAuthPrivateKeyDev=" + authKeysForDev[0],
        "-DjavaVersion=" + javaVersion, "-DuserName=" + CoreUtils.getUsername(),
        "-Dmaven.ext.class.path=''"};

    // execute archetype generation
    new MavenCliRunner().execute(targetDirectory, args);
  }

  public static String getDisplayNameErrorMEssage(String displayNameCandidate) {
    if (StringUtils.isEmpty(displayNameCandidate)) {
      return "Display Name is not set";
    }
    if (!DISPLAY_NAME_PATTERN.matcher(displayNameCandidate).matches()) {
      return "The Display Name must not contain these characters: \\\"/<>:=";
    }
    return null;
  }

  public static String getSymbolicNameErrorMessage(String symbolicNameCandidate) {
    if (StringUtils.isEmpty(symbolicNameCandidate)) {
      return "Project Name is not set";
    }
    if (!SYMBOLIC_NAME_PATTERN.matcher(symbolicNameCandidate).matches()) {
      return "The symbolic name is invalid. Use e.g. 'org.eclipse.scout.test'.";
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(symbolicNameCandidate);
    if (jkw != null) {
      return "The Symbolic Name must not contain the Java keyword '" + jkw + "'.";
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
