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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * <h3>{@link ScoutProjectNewHelper}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class ScoutProjectNewHelper {

  public static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("[^\"\\/<>=:]+");
  public static final Pattern SYMBOLIC_NAME_PATTERN = Pattern.compile("^[a-z]{1}[a-z0-9_]{0,32}(\\.[a-z]{1}[a-z0-9_]{0,32}){0,16}$");

  public static final String[] TEXT_FILE_EXTENSIONS = new String[]{"xml", "java", "launch", "properties", "nls", "html", "htm", "js", "css", "json"};

  public static final String PROPERTY_MARKER = "@@";
  public static final String DEFAULT_JAVA_VERSION = "1.8";

  public static final String PROP_USER_NAME = "user.name";
  public static final String PROP_PROJECT_SYMBOLIC_NAME = "project.symbolicName";
  public static final String PROP_PROJECT_DISPLAY_NAME = "project.displayName";
  public static final String PROP_PROJECT_FOLDERS = "project.folders";
  public static final String PROP_JAVA_VERSION = "java.version";
  public static final String PROP_AUTH_PUBLIC_KEY = "scout.auth.publickey";
  public static final String PROP_AUTH_PRIVATE_KEY = "scout.auth.privatekey";
  public static final String PROP_AUTH_PUBLIC_KEY_WAR = "scout.auth.publickey.war";
  public static final String PROP_AUTH_PRIVATE_KEY_WAR = "scout.auth.privatekey.war";

  private ScoutProjectNewHelper() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      printHelp();
      return;
    }

    File targetDir = new File(args[0]);

    // optional: java version
    String javaVersion = null;
    if (args.length > 3) {
      javaVersion = args[3];
    }

    // optional: template file
    ZipInputStream source = null;
    try {
      if (args.length > 4) {
        File template = new File(args[4]);
        if (template.exists()) {
          source = new ZipInputStream(new BufferedInputStream(new FileInputStream(template)));
        }
        else {
          System.out.println("Template File '" + template.toString() + "' could not be found.");
          return;
        }
      }

      createProject(targetDir, args[1], args[2], source, javaVersion);
    }
    finally {
      if (source != null) {
        try {
          source.close();
        }
        catch (IOException e) {
          // nop;
        }
      }
    }
  }

  protected static void printHelp() {
    System.out.println("Scout Project Creation Helper Arguments: ");
    System.out.println("1. target directory  [required]. E.g.: \"C:\\Projects\\\"");
    System.out.println("2. symbolic name     [required]. E.g.: my.app.symbolic.name");
    System.out.println("3. display name      [required]. E.g.: \"My Application Name\"");
    System.out.println("4. java version      [optional]. If no Java version is specified, \"" + DEFAULT_JAVA_VERSION + "\" is used.");
    System.out.println("5. template zip file [optional]. If no custom Template zip file is specified, the default Scout Template is used.");
    System.out.println();
    System.out.println("Sample usage: ");
    System.out.println("java -cp commons-lang3-3.1.jar;org.eclipse.scout.sdk.core-5.1.0.jar;org.eclipse.scout.sdk.core.s-5.1.0.jar org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper \"C:\\Projects\" my.app.test \"My App\" "
        + DEFAULT_JAVA_VERSION + " \"C:\\templates\\myTemplate.zip\"");
  }

  public static void createProject(File targetDirectory, String symbolicName, String displayName) throws IOException {
    createProject(targetDirectory, symbolicName, displayName, null, null);
  }

  public static void createProject(File targetDirectory, String symbolicName, String displayName, String javaVersion) throws IOException {
    createProject(targetDirectory, symbolicName, displayName, null, javaVersion);
  }

  public static void createProject(File targetDirectory, String symbolicName, String displayName, ZipInputStream source) throws IOException {
    createProject(targetDirectory, symbolicName, displayName, source, null);
  }

  public static void createProject(File targetDirectory, String symbolicName, String displayName, ZipInputStream source, String javaVersion) throws IOException {
    // validate input
    if (StringUtils.isEmpty(javaVersion)) {
      javaVersion = DEFAULT_JAVA_VERSION;
    }
    String symbolicNameMsg = getSymbolicNameErrorMessage(symbolicName);
    if (symbolicNameMsg != null) {
      throw new IllegalArgumentException(symbolicNameMsg);
    }
    String displayNameMsg = getDisplayNameErrorMEssage(displayName);
    if (displayNameMsg != null) {
      throw new IllegalArgumentException(displayNameMsg);
    }

    ZipInputStream templateZipFile = null;
    try {
      if (source == null) {
        // use default scout template as source
        templateZipFile = new ZipInputStream(ScoutProjectNewHelper.class.getClassLoader().getResourceAsStream("scoutProjectTemplate.zip"));
      }
      else {
        templateZipFile = source;
      }

      Map<String, String> props = new HashMap<>(9);
      props.put(PROP_USER_NAME, CoreUtils.getUsername());
      props.put(PROP_PROJECT_SYMBOLIC_NAME, symbolicName);
      props.put(PROP_PROJECT_DISPLAY_NAME, displayName);
      props.put(PROP_PROJECT_FOLDERS, symbolicName.replace('.', File.separatorChar));
      props.put(PROP_JAVA_VERSION, javaVersion);

      try {
        String[] authKeysForDev = CoreUtils.generateKeyPair();
        String[] authKeysForWar = CoreUtils.generateKeyPair();
        props.put(PROP_AUTH_PUBLIC_KEY, authKeysForDev[1]);
        props.put(PROP_AUTH_PRIVATE_KEY, authKeysForDev[0]);
        props.put(PROP_AUTH_PUBLIC_KEY_WAR, authKeysForWar[1]);
        props.put(PROP_AUTH_PRIVATE_KEY_WAR, authKeysForWar[0]);
      }
      catch (GeneralSecurityException gse) {
        throw new IOException(gse);
      }

      ZipEntry entry = null;
      while ((entry = templateZipFile.getNextEntry()) != null) {
        String resultingName = replaceAllProperties(entry.getName(), props);
        File targetFile = new File(targetDirectory, resultingName);
        if (entry.isDirectory()) {
          mkdirs(targetFile);
        }
        else if (isTextFile(resultingName, templateZipFile)) {
          extractTxtFile(templateZipFile, props, targetFile);
        }
        else {
          extractBinFile(templateZipFile, targetFile);
        }
        templateZipFile.closeEntry();
      }
    }
    finally {
      if (source == null && templateZipFile != null) {
        // only close the stream if we created it
        try {
          templateZipFile.close();
        }
        catch (IOException e) {
          // nop
        }
      }
    }
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

  protected static String getFileExtension(String name) {
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos < 0) {
      return "";
    }

    return name.substring(lastDotPos + 1);
  }

  protected static String getContentType(String name, InputStream data) {
    try {
      String contentType = Files.probeContentType(Paths.get(name));
      if (StringUtils.isNotEmpty(contentType)) {
        return contentType;
      }

      if (data != null) {
        contentType = URLConnection.guessContentTypeFromStream(data);
        if (StringUtils.isNotEmpty(contentType)) {
          return contentType;
        }
      }
    }
    catch (Exception e) {
      // nop
    }
    return null;
  }

  protected static boolean isTextFile(String name, InputStream data) {
    // fast detect: known text file types
    String fileExt = getFileExtension(name);
    for (String txtExtension : TEXT_FILE_EXTENSIONS) {
      if (txtExtension.equalsIgnoreCase(fileExt)) {
        return true;
      }
    }

    // try content type detection
    String contentType = getContentType(name, data);
    if (StringUtils.isNotBlank(contentType)) {
      String lowerContentType = contentType.toLowerCase();
      // Note: application/plain is no official content type (see http://www.iana.org/assignments/media-types/media-types.xhtml).
      // Even though it is used on some platforms
      return lowerContentType.contains("text") || lowerContentType.contains("xml") || lowerContentType.equals("application/plain");
    }

    return false;
  }

  protected static void ensureNoUnreplacedProperties(String s) throws IOException {
    Matcher m = Pattern.compile("@@([^@]+)@@").matcher(s);
    if (m.find()) {
      throw new IOException("Unsupported property: '" + m.group(1) + "'.");
    }
  }

  protected static void mkdirs(File dirToCreate) throws IOException {
    if (dirToCreate.exists()) {
      // to ensure we have a clean folder
      CoreUtils.deleteFolder(dirToCreate);
    }
    Files.createDirectories(Paths.get(dirToCreate.toURI()));
  }

  protected static String replaceAllProperties(String s, Map<String, String> props) throws IOException {
    for (Entry<String, String> prop : props.entrySet()) {
      s = s.replace(PROPERTY_MARKER + prop.getKey() + PROPERTY_MARKER, prop.getValue());
    }
    ensureNoUnreplacedProperties(s);
    return s;
  }

  protected static void extractBinFile(ZipInputStream zipIn, File targetFile) throws IOException {
    Files.copy(zipIn, Paths.get(targetFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
  }

  protected static void extractTxtFile(ZipInputStream zipIn, Map<String, String> props, File targetFile) throws IOException {
    String rawContent = CoreUtils.inputStreamToString(zipIn, StandardCharsets.UTF_8).toString();
    String content = replaceAllProperties(rawContent, props);

    Files.write(Paths.get(targetFile.toURI()), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
  }
}
