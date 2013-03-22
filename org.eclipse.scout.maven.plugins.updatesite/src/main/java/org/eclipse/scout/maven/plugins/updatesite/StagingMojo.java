package org.eclipse.scout.maven.plugins.updatesite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Stage the updatesite to staging area of the build server.
 *
 * @goal stage
 */
public class StagingMojo extends AbstractMojo {

  private static final String COMPOSITE_CONTENT = "compositeContent";

  private static final String COMPOSITE_ARTIFACTS = "compositeArtifacts";

  private static final String COMPOSITE_ARTIFACTS_JAR = COMPOSITE_ARTIFACTS + ".jar";

  private static final String COMPOSITE_CONTENT_JAR = COMPOSITE_CONTENT + ".jar";

  /**
   * The directory where the generated archive file will be put.
   *
   * @parameter default-value="${project.build.directory}"
   */
  private String outputDirectory;

  /**
   * The directory where the generated archive file will be put.
   *
   * @parameter default-value="${project.build.directory}/repository"
   */
  private String p2InputDirectory;

  /**
   * The directory where the generated archive file will be put.
   *
   * @parameter
   */
  private String updatesiteDir;

  /**
   *
   * @parameter default-value="nightly"
   */
  private String compositeDir;

  /**
   * @parameter default-value="http://download.eclipse.org/scout/nightly/3.9"
   */
  private String compositeUrl;


  /**
   * @parameter default-value="/home/data/httpd/download.eclipse.org/scout/stagingArea"
   */
  private String stagingArea;

  /**
   * @parameter default-value=100
   */
  private String maxSize;


  public String getP2InputDirectory() {
    return p2InputDirectory;
  }

  public void setP2InputDirectory(String p2InputDirectory) {
    this.p2InputDirectory = p2InputDirectory;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
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
    return compositeUrl;
  }

  public void setCompositeUrl(String compositeUrl) {
    this.compositeUrl = compositeUrl;
  }

  private File getStageDir(){
	  return new File(getOutputDirectory() + File.separator + "stage");
  }

  public void execute() throws MojoExecutionException {
    File compositeRepo = createCompositeRepo();
    updateCompositeJars(compositeRepo);
    File zipFile = createStageZip(getStageDir());
    File doStageFile = createDoStageFile(zipFile);
    publish(zipFile);
    publish(doStageFile);
  }

  public void publish(File file) throws MojoExecutionException{
    try {
      FileUtility.copyToDir(file, new File(getStagingArea()));
    }
    catch (IOException e) {
      throw new MojoExecutionException("Publishing failed ", e);
    }
  }

  public File createCompositeRepo() throws MojoExecutionException {
    if (getUpdatesiteDir() == null) throw new IllegalArgumentException("UpdatesiteDir cannot be null");
    getLog().info("Creating composite Repository");
    try {
      File compositeRepo = new File(getStageDir(), getCompositeDirName());
      File p2Dir = new File(compositeRepo.getPath(), getUpdatesiteDir());
      p2Dir.mkdirs();
      File p2InputDir = new File(getP2InputDirectory());
      FileUtility.copy(p2InputDir, p2Dir);
      return compositeRepo;
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not copy repository.", e);
    }
  }

  private void updateCompositeJars(File outputDir)
      throws MojoExecutionException {
    File contentJar = downloadJar(getCompositeUrl(), COMPOSITE_CONTENT_JAR, outputDir.getPath());
    File artifactsJar = downloadJar(getCompositeUrl(), COMPOSITE_ARTIFACTS_JAR, outputDir.getPath());

    updateComposite(outputDir, contentJar, COMPOSITE_CONTENT);
    updateComposite(outputDir, artifactsJar, COMPOSITE_ARTIFACTS);
  }

  public void updateComposite(File outputDir, File contentJar, String folderName) throws MojoExecutionException {
    try {
      String jarName = contentJar.getName();
      File contentXML = extractCompositeArchive(outputDir, contentJar);
      appendChild(contentXML, getUpdatesiteDir());
      truncateChildren(contentXML,getMaxSize());
      File contentFolder = new File(outputDir,folderName);
      contentFolder.mkdir();
      FileUtility.copyToDir(contentXML, contentFolder);
      File newContentJar = new File(outputDir, jarName);
      FileUtility.compressArchive(contentFolder, newContentJar);
      FileUtility.deleteFile(contentXML);
      FileUtility.deleteFile(contentFolder);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not update archive",e);
    }
  }

  private File extractCompositeArchive(File outputDir, File content) throws MojoExecutionException {
    if (content.getName() == null || !content.getName().endsWith(".jar")) {
      throw new IllegalArgumentException("Composite Archive must be a jar file " + content.getName());
    }
    try {
      FileUtility.extractArchive(content, outputDir);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not extract archive");
    }
    String xmlName = content.getName().replace(".jar", ".xml");
    File xmlFile = new File(content.getParent(), xmlName);
    if (!xmlFile.exists()) {
      throw new MojoExecutionException("Could not extract composite archive. XML File not found " + xmlName);
    }
    return xmlFile;
  }

  public void appendChild(File contentXML, String locationName) throws MojoExecutionException {
    try {
      Document doc = FileUtility.readDOM(contentXML);
      NodeList childrenNodes = doc.getElementsByTagName("children");
      Node children = childrenNodes.item(0);

      Element childElement = doc.createElement("child");
      childElement.setAttribute("location", locationName);
      children.appendChild(childElement);

      String size = getChildElementCount(children);
      children.getAttributes().getNamedItem("size").setNodeValue(size);
      FileUtility.writeDOM(doc, contentXML);
    }
    catch (ParserConfigurationException e) {
      throw new MojoExecutionException("Could not append child", e);
    }
    catch (SAXException e) {
      throw new MojoExecutionException("Could not append child", e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not append child", e);
    }
  }

  public void truncateChildren(File contentXML, int truncateSize) throws MojoExecutionException {
    try {
      Document doc = FileUtility.readDOM(contentXML);
      NodeList childrenNodes = doc.getElementsByTagName("children");
      Node children = childrenNodes.item(0);

      NodeList childNodes = doc.getElementsByTagName("child");
      int removeCount = childNodes.getLength()- truncateSize;
      for (int i = 0; i < removeCount; i++) {
        children.removeChild(children.getFirstChild());
      }
      children.getAttributes().getNamedItem("size").setNodeValue(String.valueOf(truncateSize));
      FileUtility.writeDOM(doc, contentXML);
    }
    catch (ParserConfigurationException e) {
      throw new MojoExecutionException("Could not truncate children", e);
    }
    catch (SAXException e) {
      throw new MojoExecutionException("Could not truncate children", e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not truncate children", e);
    }
  }

  private String getChildElementCount(Node node){
    int count=0;
    NodeList childNodes = node.getChildNodes();
    for(int i=0;i<childNodes.getLength();i++){
      if(childNodes.item(i) instanceof Element){
        count++;
      }
    }
    return ""+count;
  }

  private File downloadJar(String url, String jarName, String outputDir) throws MojoExecutionException {
    try {
      URL u = new URL(url + "/" + jarName);
      InputStream inputStream = u.openConnection().getInputStream();
      File outfile = new File(outputDir, jarName);
      FileOutputStream f = new FileOutputStream(outfile);
      f.write(FileUtility.getContent(inputStream));
      f.flush();
      f.close();
      return outfile;
    }
    catch (MalformedURLException e) {
      throw new MojoExecutionException("Could not downlaod Jar " + e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not downlaod Jar " + e);
    }
  }

  public File createStageZip(File directory) throws MojoExecutionException {
    File outZipFile = new File(getOutputDirectory(), getStageFileName());
    try {
      getLog().info("Zipping " + directory + " to " + outZipFile.getPath());
      FileUtility.compressArchive(directory, outZipFile);
      return outZipFile;
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not create stage zip file", e);
    }
  }

  private File createDoStageFile(File zipInputFile) throws MojoExecutionException {
    try {
      File out = new File(getOutputDirectory(), "doStage");
      String md5 = createMD5(zipInputFile) + "  " + zipInputFile.getName();
      FileWriter writer = new FileWriter(out);
      writer.write(md5);
      writer.flush();
      writer.close();
      return out;
    }
    catch (FileNotFoundException e) {
      throw new MojoExecutionException("Could not create doStage file", e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not create doStage file", e);
    }
  }

  public String createMD5(File data) throws MojoExecutionException {
    try {
      byte[] content = FileUtility.getContent(data.getPath());
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] array = md.digest(content);
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
      }
      return sb.toString();
    }
    catch (java.security.NoSuchAlgorithmException e) {
      throw new MojoExecutionException("Could not create md5", e);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not create md5", e);
    }
  }

  private String getStageFileName() {
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-hhmm", Locale.ENGLISH);
    String timestamp = f.format(new Date());
    return "stage_" + timestamp + ".zip";
  }

  /**
   * @return the stagingArea
   */
  public String getStagingArea() {
    return stagingArea;
  }

  /**
   * @param stagingArea the stagingArea to set
   */
  public void setStagingArea(String stagingArea) {
    this.stagingArea = stagingArea;
  }
}
