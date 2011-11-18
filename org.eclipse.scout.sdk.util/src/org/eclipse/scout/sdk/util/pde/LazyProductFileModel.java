package org.eclipse.scout.sdk.util.pde;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.core.iproduct.IConfigurationFileInfo;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;

@SuppressWarnings("restriction")
public final class LazyProductFileModel {

  private final static Pattern FRAGMENT_PATTERN = Pattern.compile("^(\\sfragment\\=\\\")([^\\\"]*)(\\\")");

  private final IFile m_productFile;

  //lazily instantiated models
  private WorkspaceProductModel m_productModel;
  private IConfigurationFileInfo m_configFileInfo;
  private Properties m_configFileProperties;
  private Hashtable<Object, Object> m_origConfigFileProperties;
  private IFile m_configIniFile;

  public LazyProductFileModel(IFile productFile) {
    if (productFile == null || !productFile.exists()) throw new IllegalArgumentException("invalid product file passed");
    m_productFile = productFile;
  }

  public synchronized WorkspaceProductModel getWorkspaceProductModel() throws CoreException {
    if (m_productModel == null) {
      m_productModel = new WorkspaceProductModel(m_productFile, true);
      m_productModel.load();
      m_productModel.setDirty(false);
    }
    return m_productModel;
  }

  public synchronized IConfigurationFileInfo getConfigurationFileInfo() throws CoreException {
    if (m_configFileInfo == null) {
      m_configFileInfo = getWorkspaceProductModel().getProduct().getConfigurationFileInfo();
    }
    return m_configFileInfo;
  }

  public synchronized IFile getConfigIniFile() throws CoreException {
    if (m_configIniFile == null) {
      IPath path = new Path(getConfigurationFileInfo().getPath(Platform.getOS())).removeFirstSegments(1);
      m_configIniFile = m_productFile.getProject().getFile(path);
      if (m_configIniFile == null || !m_configIniFile.exists()) {
        m_configIniFile = null;
        throw new CoreException(new ScoutStatus("could not find product configuration file: " + path.toOSString()));
      }
    }
    return m_configIniFile;
  }

  @SuppressWarnings("unchecked")
  public synchronized Properties getConfigFileProperties() throws CoreException {
    if (m_configFileProperties == null) {
      m_configFileProperties = new Properties();
      IFile configIni = getConfigIniFile();
      try {
        m_configFileProperties.load(configIni.getContents());

        // remember all entries at the moment of property loading.
        // this allows to check if the config file is dirty later on.
        // shallow copy is sufficient as the properties map can only store Strings which are immutable.
        m_origConfigFileProperties = (Hashtable<Object, Object>) m_configFileProperties.clone();
      }
      catch (IOException e) {
        m_configFileProperties = null; // throw away the empty unloaded instance so that we can try again next time.
        throw new CoreException(new ScoutStatus("unable to load product configuration file: " + configIni.getFullPath().toOSString(), e));
      }
    }
    return m_configFileProperties;
  }

  public synchronized void save() throws CoreException {
    if (m_productModel != null && m_productModel.isDirty()) {
      saveProductModel();
    }

    if (isConfigFileDirty()) {
      saveConfigIni();
    }
  }

  private void saveConfigIni() throws CoreException {
    IFile configIni = getConfigIniFile();
    OutputStream stream = null;
    try {
      stream = new BufferedOutputStream(new FileOutputStream(configIni.getFullPath().toFile()));
      m_configFileProperties.store(stream, null);
      stream.flush();
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("unable to save product configuration file: " + configIni.getFullPath().toOSString(), e));
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        }
        catch (IOException e) {
        }
      }
      configIni.refreshLocal(IFile.DEPTH_ONE, null);
    }
  }

  private boolean isConfigFileDirty() {
    if (m_configFileProperties == null || m_origConfigFileProperties == null) return false;
    if (m_configFileProperties.size() != m_origConfigFileProperties.size()) return true;

    for (Entry<Object, Object> entry : m_configFileProperties.entrySet()) {
      if (CompareUtility.notEquals(m_origConfigFileProperties.get(entry.getKey()), entry.getValue())) {
        return true;
      }
    }
    return false;
  }

  /**
   * TODO can be eliminated when <b>BUG 362398</b> is fixed.<br>
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
