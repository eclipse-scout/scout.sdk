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
package org.eclipse.scout.sdk.util.pde;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.core.iproduct.IConfigurationFileInfo;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.FormatPreservingProperties;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;

@SuppressWarnings("restriction")
public final class LazyProductFileModel {

  private static final Pattern FRAGMENT_PATTERN = Pattern.compile("^(\\sfragment\\=\\\")([^\\\"]*)(\\\")");

  private final IFile m_productFile;

  //lazily instantiated models
  private volatile WorkspaceProductModel m_productModel;
  private volatile IConfigurationFileInfo m_configFileInfo;
  private volatile FormatPreservingProperties m_configFileProperties;
  private volatile IFile m_configIniFile;

  public LazyProductFileModel(IFile productFile) {
    if (productFile == null) {
      throw new IllegalArgumentException("null product file not allowed.");
    }
    if (!productFile.exists()) {
      throw new IllegalArgumentException("invalid product file passed: '" + productFile.getFullPath().toString() + "'.");
    }
    m_productFile = productFile;
  }

  public synchronized WorkspaceProductModel getWorkspaceProductModel() throws CoreException {
    if (m_productModel == null) {
      WorkspaceProductModel tmp = new WorkspaceProductModel(m_productFile, true);
      tmp.load();
      tmp.setDirty(false);
      m_productModel = tmp;
    }
    return m_productModel;
  }

  public synchronized IConfigurationFileInfo getConfigurationFileInfo() throws CoreException {
    if (m_configFileInfo == null) {
      m_configFileInfo = getWorkspaceProductModel().getProduct().getConfigurationFileInfo();
    }
    return m_configFileInfo;
  }

  /**
   * gets the config.ini file specified in the product.
   *
   * @return the config.ini file or null if no file is specified for the running OS ({@link Platform#getOS()}).
   * @throws CoreException
   */
  public synchronized IFile getConfigIniFile() throws CoreException {
    if (m_configIniFile == null) {
      String osPath = getConfigurationFileInfo().getPath(Platform.getOS());
      if (StringUtility.hasText(osPath)) {
        IFile configIniFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(osPath.trim()));
        if (ResourceUtility.exists(configIniFile)) {
          m_configIniFile = configIniFile;
        }
        else {
          throw new CoreException(new ScoutStatus("could not find product configuration file: " + osPath));
        }
      }
    }
    return m_configIniFile;
  }

  public synchronized FormatPreservingProperties getConfigFileProperties() throws CoreException {
    if (m_configFileProperties == null) {
      FormatPreservingProperties tmp = new FormatPreservingProperties();
      IFile configIni = getConfigIniFile();
      if (configIni != null) {
        tmp.load(configIni);
      }
      m_configFileProperties = tmp;
    }
    return m_configFileProperties;
  }

  public synchronized void save() throws CoreException {
    if (m_productModel != null && m_productModel.isDirty()) {
      saveProductModel();
    }

    if (m_configFileProperties != null && m_configFileProperties.isDirty()) {
      saveConfigIni();
    }
  }

  private void saveConfigIni() throws CoreException {
    IFile configIni = getConfigIniFile();
    if (configIni == null) {
      return;
    }

    OutputStream stream = null;
    try {
      stream = new BufferedOutputStream(new FileOutputStream(configIni.getRawLocation().toFile()));
      m_configFileProperties.store(stream);
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("unable to save product configuration file: " + configIni.getRawLocation().toOSString(), e));
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        }
        catch (IOException e) {
        }
      }
      configIni.refreshLocal(IFile.DEPTH_ZERO, null);
    }
  }

  /**
   * TODO can be eliminated when <b>BUG 362398</b> is fixed (was fixed for Indigo SR1).<br>
   * When the bug is fixed call m_model.save() directly instead of calling this method.
   *
   * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=362398">Bugzilla #362398</a>
   */
  private void saveProductModel() throws CoreException {
    // save product file into byte array not writing empty fragment parts (fragment="").
    ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
    PrintWriter writer = new PrintWriter(out, false) {
      @Override
      public void print(String s) {
        // overwrite writer.print(" fragment=\"true\""); //$NON-NLS-1$
        Matcher matcher = FRAGMENT_PATTERN.matcher(s);
        if (matcher.matches()) {
          if (StringUtility.isNullOrEmpty(matcher.group(2))) {
            return;
          }
        }
        super.print(s);
      }
    };
    m_productModel.save(writer);
    writer.flush();
    writer.close();

    // get corrected contents from ByteArrayOutputStream and write it to the file.
    IFile file = (IFile) m_productModel.getUnderlyingResource();
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    file.setContents(in, true, false, null);
    file.refreshLocal(IFile.DEPTH_ONE, null);
    // no need to close a ByteArrayInputStream
  }
}
