/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.maven.plugins.updatesite;

import static java.time.LocalDateTime.now;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Stage the updatesite to staging area of the build server.
 */
@Mojo(name = "stage")
public class StagingMojo extends AbstractStagingMojo {

  private static final String COMPOSITE_CONTENT = "compositeContent";

  private static final String COMPOSITE_ARTIFACTS = "compositeArtifacts";

  private static final String COMPOSITE_ARTIFACTS_JAR = COMPOSITE_ARTIFACTS + ".jar";

  private static final String COMPOSITE_CONTENT_JAR = COMPOSITE_CONTENT + ".jar";

  /**
   * The directory where the generated archive file will be put.
   */
  @Parameter(defaultValue = "${project.build.directory}/repository")
  private String p2InputDirectory;

  /**
   * The directory where the generated archive file will be put.
   */
  @Parameter
  private String updatesiteDir;

  @Parameter(defaultValue = "nightly")
  private String compositeDir;

  @Parameter(defaultValue = "https://download.eclipse.org/scout")
  private String repositoryUrl;

  @Parameter(defaultValue = "100")
  private String maxSize;

  public String getP2InputDirectory() {
    return p2InputDirectory;
  }

  public void setP2InputDirectory(String p2InputDirectory) {
    this.p2InputDirectory = p2InputDirectory;
  }

  public String getCompositeDirName() {
    return compositeDir;
  }

  public void setCompositeDirName(String compositeDirName) {
    this.compositeDir = compositeDirName;
  }

  public String getUpdatesiteDir() {
    return updatesiteDir;
  }

  public int getMaxSize() {
    return Integer.parseInt(maxSize);
  }

  public void setUpdatesiteDir(String updatesiteDir) {
    this.updatesiteDir = updatesiteDir;
  }

  public String getCompositeUrl() {
    return getRepositoryUrl() + "/" + getCompositeDirName();
  }

  private File getStageDir() {
    return new File(getOutputDirectory() + File.separator + "stage");
  }

  @Override
  public void execute() throws MojoExecutionException {
    var compositeRepo = createCompositeRepo();
    updateCompositeJars(compositeRepo);
    var stageTargetDir = getStageTargetDir();
    try {
      FileUtility.ensureDirExists(stageTargetDir);
      var timestamp = createTimestamp();
      var zipFile = createStageZip(getStageDir(), timestamp);
      createDoStageFile(zipFile, timestamp);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not create directory '" + stageTargetDir.getAbsolutePath() + "'.", e);
    }
  }

  public File createCompositeRepo() throws MojoExecutionException {
    if (getUpdatesiteDir() == null) {
      throw new IllegalArgumentException("UpdatesiteDir cannot be null");
    }
    getLog().info("Creating composite Repository");
    try {
      var compositeRepo = new File(getStageDir(), getCompositeDirName());
      var p2Dir = new File(compositeRepo.getPath(), getUpdatesiteDir());
      FileUtility.ensureDirExists(p2Dir);
      var p2InputDir = new File(getP2InputDirectory());
      FileUtility.copy(p2InputDir, p2Dir);
      return compositeRepo;
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not copy repository.", e);
    }
  }

  private void updateCompositeJars(File outputDir) throws MojoExecutionException {
    var contentJar = downloadJar(getCompositeUrl(), COMPOSITE_CONTENT_JAR, outputDir.getPath());
    var artifactsJar = downloadJar(getCompositeUrl(), COMPOSITE_ARTIFACTS_JAR, outputDir.getPath());

    updateComposite(outputDir, contentJar, COMPOSITE_CONTENT);
    updateComposite(outputDir, artifactsJar, COMPOSITE_ARTIFACTS);
  }

  public void updateComposite(File outputDir, File contentJar, String folderName) throws MojoExecutionException {
    try {
      getLog().info("Extracting " + contentJar);
      var jarName = contentJar.getName();
      var contentXML = extractCompositeArchive(outputDir, contentJar);
      appendChild(contentXML, getUpdatesiteDir());
      truncateChildren(contentXML, getMaxSize());
      var contentFolder = new File(outputDir, folderName);
      FileUtility.ensureDirExists(contentFolder);
      FileUtility.copyToDir(contentXML, contentFolder);
      var newContentJar = new File(outputDir, jarName);
      FileUtility.compressArchive(contentFolder, newContentJar);
      FileUtility.deleteFile(contentXML);
      FileUtility.deleteFile(contentFolder);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not update archive", e);
    }
  }

  public static File extractCompositeArchive(File outputDir, File content) throws MojoExecutionException {
    if (content == null || !content.getName().endsWith(".jar")) {
      throw new IllegalArgumentException("Composite Archive must be a jar file: " + content);
    }
    try {
      FileUtility.extractArchive(content, outputDir);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not extract archive '" + content + "'.", e);
    }
    var xmlName = content.getName().replace(".jar", ".xml");
    var xmlFile = new File(content.getParent(), xmlName);
    if (!xmlFile.exists()) {
      throw new MojoExecutionException("Could not extract composite archive. XML File not found " + xmlName);
    }
    return xmlFile;
  }

  public static void appendChild(File contentXML, String locationName) throws MojoExecutionException {
    try {
      var doc = FileUtility.readDOM(contentXML);
      var childrenNodes = doc.getElementsByTagName("children");
      var children = childrenNodes.item(0);

      var childElement = doc.createElement("child");
      childElement.setAttribute("location", locationName);
      children.appendChild(childElement);

      var size = getChildElementCount(children);
      children.getAttributes().getNamedItem("size").setNodeValue(size);
      FileUtility.writeDOM(doc, contentXML);
    }
    catch (ParserConfigurationException | IOException | SAXException e) {
      throw new MojoExecutionException("Could not append child", e);
    }
  }

  public static void truncateChildren(File contentXML, int truncateSize) throws MojoExecutionException {
    try {
      var doc = FileUtility.readDOM(contentXML);
      var childrenNodes = doc.getElementsByTagName("children");
      var children = childrenNodes.item(0);

      var childNodes = doc.getElementsByTagName("child");
      var removeCount = childNodes.getLength() - truncateSize;
      for (var i = 0; i < removeCount; i++) {
        children.removeChild(children.getFirstChild());
      }
      children.getAttributes().getNamedItem("size").setNodeValue(String.valueOf(truncateSize));
      FileUtility.writeDOM(doc, contentXML);
    }
    catch (ParserConfigurationException | IOException | SAXException e) {
      throw new MojoExecutionException("Could not truncate children", e);
    }
  }

  private static String getChildElementCount(Node node) {
    var childNodes = node.getChildNodes();
    var count = IntStream.range(0, childNodes.getLength()).filter(i -> childNodes.item(i) instanceof Element).count();
    return "" + count;
  }

  private File downloadJar(String url, String jarName, String outputDir) throws MojoExecutionException {
    var path = url + "/" + jarName;
    try {
      var u = new URL(path);
      var conn = u.openConnection();
      var outFile = new File(outputDir, jarName);
      getLog().info("Downloading " + path + " to " + outFile);
      //noinspection NestedTryStatement
      try (var inputStream = conn.getInputStream(); var f = new FileOutputStream(outFile)) {
        FileUtility.copy(inputStream, f);
      }
      return outFile;
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not download '" + path + "'." + e);
    }
  }

  public File createStageZip(File directory, String timestamp) throws MojoExecutionException {
    var stageTargetDir = getStageTargetDir();
    //noinspection ResultOfMethodCallIgnored
    stageTargetDir.mkdirs();

    var outZipFile = new File(stageTargetDir, "stage" + timestamp + ".zip");
    try {
      getLog().info("Zipping " + directory + " to " + outZipFile.getPath());
      FileUtility.compressArchive(directory, outZipFile);
      return outZipFile;
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not create stage zip file", e);
    }
  }

  private File createDoStageFile(File zipInputFile, String timestamp) throws MojoExecutionException {
    var out = new File(getStageTargetDir(), "doStage_" + timestamp);
    var line = createMD5(zipInputFile) + "  " + zipInputFile.getName();
    try (var writer = new FileWriter(out, StandardCharsets.UTF_8)) {
      writer.write(line);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not create doStage file", e);
    }
    return out;
  }

  /**
   * Use MD5 because it is not security relevant. The hash is only used to verify if the file copy was successful (no
   * binary changes)<br>
   */
  public static String createMD5(File data) throws MojoExecutionException {
    try (var is = new BufferedInputStream(Files.newInputStream(data.toPath()))) {
      var md = MessageDigest.getInstance("MD5");
      var buffer = new byte[8192];
      var numRead = 0;
      while ((numRead = is.read(buffer)) != -1) {
        md.update(buffer, 0, numRead);
      }

      var array = md.digest();
      var sb = new StringBuilder(array.length * 2);
      for (var element : array) {
        sb.append(Integer.toHexString((element & 0xFF) | 0x100), 1, 3);
      }
      return sb.toString();
    }
    catch (NoSuchAlgorithmException | IOException e) {
      throw new MojoExecutionException("Could not create md5", e);
    }
  }

  protected static String createTimestamp() {
    return now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern("yyyyMMdd-hhmmss-SSS", Locale.US));
  }

  /**
   * @return the repositoryUrl
   */
  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  /**
   * @param repositoryUrl
   *          the repositoryUrl to set
   */
  public void setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
  }
}
