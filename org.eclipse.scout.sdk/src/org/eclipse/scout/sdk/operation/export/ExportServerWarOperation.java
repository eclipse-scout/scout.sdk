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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.ProductExportOperation;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

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
  private String m_warFileName;
  private IFolder m_htmlFolder;

  private IFile m_clientZipFile;
  private File m_resultingWarFile;

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
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IStatus result = null;
    final String SERVLET_BRIDGE_JAR_NAME = "org.eclipse.equinox.servletbridge_1.3.0.v20130718-2032.jar";
    try {
      m_tempBuildDir = IOUtility.createTempDirectory("warExportBuildDir");
      buildClientProduct(monitor, workingCopyManager);
      result = buildServerProduct(monitor);
      if (result.isOK()) {
        installFile(new URL("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/server.war/lib/" + SERVLET_BRIDGE_JAR_NAME), WEB_INF + "/lib/" + SERVLET_BRIDGE_JAR_NAME);
        installFile(new URL("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/server.war/web.xml"), WEB_INF + "/web.xml");
        installFile(new URL("platform:/plugin/" + ScoutSdk.PLUGIN_ID + "/templates/server.war/eclipse/launch.ini"), WEB_INF + "/eclipse/launch.ini");
        m_resultingWarFile = packWar();

        if (m_clientZipFile != null && m_clientZipFile.exists()) {
          m_clientZipFile.delete(true, monitor);
          getHtmlFolder().refreshLocal(IResource.DEPTH_INFINITE, monitor);
        }

        try {
          IOUtility.deleteDirectory(m_tempBuildDir);
        }
        catch (Exception e) {
          // nop
        }
      }
      else {
        throw new CoreException(result);
      }
    }
    catch (Exception e) {
      if (e instanceof CoreException) {
        throw (CoreException) e;
      }
      else {
        throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not create '" + SERVLET_BRIDGE_JAR_NAME + "' in temp folder", e));
      }
    }
  }

  private void buildClientProduct(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getClientProduct() == null) return;
    ExportClientZipOperation exportClient = new ExportClientZipOperation(getClientProduct());
    exportClient.setHtmlFolder(getHtmlFolder());
    exportClient.setTargetDirectory(getHtmlFolder().getLocation().toOSString());
    exportClient.validate();
    exportClient.run(monitor, workingCopyManager);

    // zip has directly been exported into the html folder -> refresh and remember the file
    getHtmlFolder().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    m_clientZipFile = getHtmlFolder().getFile(exportClient.getZipName());

    IOUtility.deleteDirectory(new File(m_tempBuildDir.getAbsolutePath() + "/client"));
  }

  private IStatus buildServerProduct(IProgressMonitor monitor) throws Exception {
    ProductFileModelHelper pfmh = new ProductFileModelHelper(getServerProduct());
    FeatureExportInfo featureInfo = new FeatureExportInfo();
    featureInfo.toDirectory = true;
    featureInfo.exportSource = false;
    featureInfo.exportSourceBundle = false;
    featureInfo.allowBinaryCycles = true;
    featureInfo.exportMetadata = false;
    featureInfo.destinationDirectory = m_tempBuildDir.getAbsolutePath() + "/" + WEB_INF + "/eclipse";
    featureInfo.zipFileName = "export.zip";
    featureInfo.items = pfmh.ProductFile.getPluginModels();

    IProduct prod = pfmh.ProductFile.getProduct();
    ProductExportOperation productExportOp = new ProductExportOperation(featureInfo, "Build product '" + prod.getName() + "'...", prod, ".");
    productExportOp.schedule();
    productExportOp.join();
    IStatus result = productExportOp.getResult();
    if (!result.isOK()) {
      return result;
    }
    // clean up
    deleteFile(m_tempBuildDir.getAbsolutePath(), WEB_INF, "eclipse", "config.ini");
    deleteFile(m_tempBuildDir.getAbsolutePath(), WEB_INF, "eclipse", "eclipse.exe");
    deleteFile(m_tempBuildDir.getAbsolutePath(), WEB_INF, "eclipse", "eclipse.ini");

    return Status.OK_STATUS;
  }

  private File packWar() throws FileNotFoundException, IOException {
    File destinationFile = new File(getWarFileName());
    if (!destinationFile.exists()) {
      destinationFile.getParentFile().mkdirs();
    }
    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destinationFile));
    ResourceUtility.addFolderToZip(m_tempBuildDir, zipOut);
    zipOut.flush();
    zipOut.close();
    return destinationFile;
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

  public File getResultingWarFile() {
    return m_resultingWarFile;
  }
}
