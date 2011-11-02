/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.ProductExportOperation;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.osgi.framework.Version;

/**
 * <h3>{@link ExportServerWarOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 04.02.2011
 */
@SuppressWarnings("restriction")
public class ExportServerWarOperation implements IOperation {
  public static final String BUNDLE_NAME_SERVLETBRIDGE = "org.eclipse.equinox.servletbridge";

  public static final String WEB_INF = "WEB-INF";
  private IFile m_serverProduct;
  private IFile m_clientProduct;
  private File m_tempBuildDir;
  private String m_warFileName = "D:/Temp/max24h/warBuild/export.war";
  private IFolder m_htmlFolder;

  private IFile m_clientZipFile;

  public ExportServerWarOperation(IFile serverProduct) {
    m_serverProduct = serverProduct;
  }

  @Override
  public String getOperationName() {
    return "Export '" + getServerProduct().getName() + "' to war file...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getWarFileName())) {
      throw new IllegalArgumentException("WarFileName can not be null or empty!");
    }
    if (getServerProduct() == null) {
      throw new IllegalArgumentException("Server product can not be null!");
    }
    if (getClientProduct() != null) {
      if (getHtmlFolder() == null || !getHtmlFolder().exists()) {
        throw new IllegalArgumentException("Html folder does not exist!");
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // create war
    try {
      m_tempBuildDir = IOUtility.createTempDirectory("warExportBuildDir");
      buildClientProduct(monitor);
      buildServerProduct(monitor);
      installFile(new URL("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/server.war/lib/servletbridge.jar"), WEB_INF + "/lib/servletbridge.jar");
      installFile(new URL("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/server.war/web.xml"), WEB_INF + "/web.xml");
      installFile(new URL("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/server.war/eclipse/launch.ini"), WEB_INF + "/eclipse/launch.ini");
      packWar();
    }
    catch (Exception e) {

      if (e instanceof CoreException) {
        throw (CoreException) e;
      }
      else {
        throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not create 'servletbridge.jar' in temp folder", e));
      }
    }
    finally {
      if (m_clientZipFile != null && m_clientZipFile.exists()) {
        m_clientZipFile.delete(true, monitor);
      }
      try {
        IOUtility.deleteDirectory(m_tempBuildDir);
      }
      catch (Exception e) {
        // viod
      }
    }

  }

  private void buildClientProduct(IProgressMonitor monitor) throws CoreException {
    if (getClientProduct() == null) return;
    WorkspaceProductModel clientModel = new WorkspaceProductModel(getClientProduct(), false);
    clientModel.load();
    try {
      String productName = getZipName(getHtmlFolder(), clientModel);

      FeatureExportInfo featureInfo = new FeatureExportInfo();
      featureInfo.toDirectory = true;
      featureInfo.exportSource = false;
      featureInfo.exportSourceBundle = false;
      featureInfo.allowBinaryCycles = true;
      featureInfo.exportMetadata = false;
      featureInfo.destinationDirectory = m_tempBuildDir.getAbsolutePath() + "/client/buildDir/" + productName;
//      featureInfo.zipFileName = productName + ".zip";
      featureInfo.items = getPluginModels(clientModel);

      ProductExportOperation productExportOp = new ProductExportOperation(featureInfo, "Build product '" + productName + "'...", clientModel.getProduct(), ".");
      productExportOp.schedule();
      productExportOp.join();
      IStatus result = productExportOp.getResult();
      if (!result.isOK()) {
        throw new CoreException(result);
      }
      // create zip file
      File zipDir = new File(m_tempBuildDir.getAbsolutePath() + File.separator + "client");
      File tempZipFile = new File(zipDir.getAbsoluteFile() + File.separator + productName + ".zip");
      if (!tempZipFile.exists()) {
        tempZipFile.getParentFile().mkdirs();
      }
      ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempZipFile));
      addFolderToZip(new File(m_tempBuildDir.getAbsolutePath() + File.separator + "client/buildDir/"), new File(m_tempBuildDir.getAbsolutePath() + File.separator + "client/buildDir/"), zipOut);
      zipOut.flush();
      zipOut.close();
      // copy to server bundle
      m_clientZipFile = getHtmlFolder().getFile(productName + ".zip");
      if (m_clientZipFile.exists()) {
        m_clientZipFile.delete(true, monitor);
      }
      m_clientZipFile.create(new FileInputStream(tempZipFile), true, monitor);
    }
    catch (Exception e) {
      if (e instanceof CoreException) {
        throw (CoreException) e;
      }
      else {
        throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Error during product export.", e));
      }
    }
    finally {
      IOUtility.deleteDirectory(new File(m_tempBuildDir.getAbsolutePath() + "/client"));
    }
  }

  private void buildServerProduct(IProgressMonitor monitor) throws CoreException {
    WorkspaceProductModel serverModel = new WorkspaceProductModel(getServerProduct(), false);
    serverModel.load();
    try {
      FeatureExportInfo featureInfo = new FeatureExportInfo();
      featureInfo.toDirectory = true;
      featureInfo.exportSource = false;
      featureInfo.exportSourceBundle = false;
      featureInfo.allowBinaryCycles = true;
      featureInfo.exportMetadata = false;
      featureInfo.destinationDirectory = m_tempBuildDir.getAbsolutePath() + "/" + WEB_INF + "/eclipse";
      featureInfo.zipFileName = "export.zip";
      featureInfo.items = getPluginModels(serverModel);
      ProductExportOperation productExportOp = new ProductExportOperation(featureInfo, "Build product '" + serverModel.getProduct().getName() + "'...", serverModel.getProduct(), ".");
      productExportOp.schedule();
      productExportOp.join();
      IStatus result = productExportOp.getResult();
      if (!result.isOK()) {
        throw new CoreException(result);
      }
      // clean up
      deleteFile(m_tempBuildDir.getAbsolutePath(), WEB_INF, "eclipse", "config.ini");
      deleteFile(m_tempBuildDir.getAbsolutePath(), WEB_INF, "eclipse", "eclipse.exe");
      deleteFile(m_tempBuildDir.getAbsolutePath(), WEB_INF, "eclipse", "eclipse.ini");
    }
    catch (Exception e) {
      if (e instanceof CoreException) {
        throw (CoreException) e;
      }
      else {
        throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Error during product export.", e));
      }
    }
  }

  protected File packWar() throws FileNotFoundException, IOException {
    File destinationFile = new File(getWarFileName());
    if (!destinationFile.exists()) {
      destinationFile.getParentFile().mkdirs();
    }
    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destinationFile));
    addFolderToZip(m_tempBuildDir, m_tempBuildDir, zipOut);
    zipOut.flush();
    zipOut.close();
    return destinationFile;
  }

  private void addFolderToZip(File baseDir, File file, ZipOutputStream zOut) throws IOException {
    if ((!file.exists()) || (!file.isDirectory())) {
      throw new IOException("source directory " + file + " does not exist or is not a folder");
    }
    for (File f : file.listFiles()) {
      if (f.exists() && (!f.isHidden())) {
        if (f.isDirectory()) {
          addFolderToZip(baseDir, f, zOut);
        }
        else {
          String name = f.getAbsolutePath();
          String prefix = baseDir.getAbsolutePath();
          if (prefix.endsWith("/") || prefix.endsWith("\\")) {
            prefix = prefix.substring(0, prefix.length() - 1);
          }
          name = name.substring(prefix.length() + 1);
          name = name.replace('\\', '/');
          byte[] data = FileUtility.readFile(f);
          zOut.putNextEntry(new ZipEntry(name));
          zOut.write(data);
          zOut.closeEntry();
        }
      }
    }
  }

  private File installFile(URL platformUrl, String filePath) throws IOException, ProcessingException {
    URL absSourceUrl = FileLocator.resolve(platformUrl);
    byte[] content = IOUtility.getContent(absSourceUrl.openStream());
    File destFile = new File(m_tempBuildDir + File.separator + filePath.replaceAll("\\\\\\/$", ""));
    if (!destFile.exists()) {
      destFile.getParentFile().mkdirs();
    }
    IOUtility.writeContent(new FileOutputStream(destFile), content, true);
    return destFile;
  }

  private boolean deleteFile(String... segments) {
    if (segments != null) {
      IPath path = null;
      if (segments.length > 0) {
        path = new Path(segments[0]);
        for (int i = 1; i < segments.length; i++) {
          path = path.append(new Path(segments[i]));
        }
      }
      if (path != null) {
        return IOUtility.deleteFile(path.toOSString());
      }
    }
    return false;
  }

  private String getZipName(IFolder destinationFolder, IProductModel clientModel) {
    BufferedReader reader = null;
    try {
      IResource indexResource = destinationFolder.findMember("index.html");
      if (indexResource.exists() && indexResource.getType() == IResource.FILE) {
        reader = new BufferedReader(new InputStreamReader(((IFile) indexResource).getContents()));
        Pattern p = Pattern.compile("\\<a\\shref\\=(\\\")?[^a-zA-Z0-9]*([a-zA-Z0-9]*)\\.zip(\\\")?\\>");
        String l = reader.readLine();
        while (l != null) {
          Matcher m = p.matcher(l);
          if (m.find()) {
            return m.group(2);
          }
          l = reader.readLine();
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not parse application name out of the index.html in '" + destinationFolder.getFullPath() + "'");
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          // void
        }

      }
    }
    String productName = clientModel.getProduct().getName();
    if (productName == null) {
      productName = "clientApplication";
    }
    else {
      StringBuilder productNameBuilder = new StringBuilder();
      String[] split = productName.split("\\s");
      for (String s : split) {
        if (s.length() > 0) {
          productNameBuilder.append(Character.toUpperCase(s.charAt(0)));
          if (s.length() > 1) {
            productNameBuilder.append(s.substring(1));
          }
        }
      }
      productName = productNameBuilder.toString();
    }

    return productName;
  }

  /**
   * @param warFileName
   *          the warFileName to set
   */
  public void setWarFileName(String warFileName) {
    m_warFileName = warFileName;
  }

  /**
   * @return the warFileName
   */
  public String getWarFileName() {
    return m_warFileName;
  }

  /**
   * @return the serverProduct
   */
  public IFile getServerProduct() {
    return m_serverProduct;
  }

  /**
   * @param serverProduct
   *          the serverProduct to set
   */
  public void setServerProduct(IFile serverProduct) {
    m_serverProduct = serverProduct;
  }

  /**
   * @return the htmlFolder
   */
  public IFolder getHtmlFolder() {
    return m_htmlFolder;
  }

  /**
   * @param htmlFolder
   *          the htmlFolder to set
   */
  public void setHtmlFolder(IFolder htmlFolder) {
    m_htmlFolder = htmlFolder;
  }

  /**
   * @return the clientProduct
   */
  public IFile getClientProduct() {
    return m_clientProduct;
  }

  /**
   * @param clientProduct
   *          the clientProduct to set
   */
  public void setClientProduct(IFile clientProduct) {
    m_clientProduct = clientProduct;
  }

  private BundleDescription[] getPluginModels(WorkspaceProductModel model) {
    ArrayList<BundleDescription> list = new ArrayList<BundleDescription>();
    State state = TargetPlatformHelper.getState();
    IProductPlugin[] plugins = model.getProduct().getPlugins();
    for (int i = 0; i < plugins.length; i++) {
      BundleDescription bundle = null;
      String v = plugins[i].getVersion();
      if (v != null && v.length() > 0) {
        bundle = state.getBundle(plugins[i].getId(), Version.parseVersion(v));
      }
      // if there's no version, just grab a bundle like before
      if (bundle == null) bundle = state.getBundle(plugins[i].getId(), null);
      if (bundle != null) list.add(bundle);
    }
    return list.toArray(new BundleDescription[list.size()]);
  }
}
