package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.widgets.Composite;

public class ExportServerWizardPage extends AbstractExportProductWizardPage {

  private final static String SETTINGS_PRODUCT_FILE = "productFileSetting";
  private final static String SETTINGS_WAR_FILE_NAME = "warFileNameSetting";

  public ExportServerWizardPage(IScoutProject scoutProject) {
    super(scoutProject, ExportServerWizardPage.class.getName(), Texts.get("ExportWebArchive"), IScoutBundle.BUNDLE_SERVER,
        SETTINGS_PRODUCT_FILE, SETTINGS_WAR_FILE_NAME);
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    String lastVal = getDialogSettings().get(SETTINGS_WAR_FILE_NAME);
    if (StringUtility.hasText(lastVal)) {
      m_warFileName.setText(lastVal);
    }
    else {
      String warName = findServerWarName();
      if (warName == null) {
        warName = getWizard().getProjectAlias();
      }
      m_warFileName.setText(warName);
    }
  }

  private String findServerWarName() {
    String warName = null;
    if (getScoutProject().getUiSwingBundle() != null) {
      warName = findServerNameInClientBundle(getScoutProject().getUiSwingBundle());
    }
    if (warName == null && getScoutProject().getUiSwtBundle() != null) {
      warName = findServerNameInClientBundle(getScoutProject().getUiSwtBundle());
    }
    return warName;
  }

  private String findServerNameInClientBundle(IScoutBundle bundle) {
    P_ConfigIniVisitor visitor = new P_ConfigIniVisitor();
    try {
      bundle.getProject().accept(visitor);
    }
    catch (CoreException e) {
      if (e.getStatus().isOK()) {
        return visitor.getName();
      }
    }
    return null;
  }

  private static class P_ConfigIniVisitor implements IResourceVisitor {
    private final static Pattern SERVER_URL_PATTERN = Pattern.compile("\\/([^\\/]*)\\/process");
    private String m_name;

    @Override
    public boolean visit(IResource resource) throws CoreException {
      if (resource.getType() == IResource.FILE && CompareUtility.equals("config.ini", resource.getName()) && resource.exists()) {
        parseConfigIniFile((IFile) resource);
      }
      return true;
    }

    private void parseConfigIniFile(IFile configIniFile) throws CoreException {
      Properties props = new Properties();
      InputStream is = null;
      try {
        is = configIniFile.getContents();
        props.load(is);
        String serverUrl = props.getProperty("server.url");
        if (StringUtility.hasText(serverUrl)) {
          Matcher m = SERVER_URL_PATTERN.matcher(serverUrl);
          if (m.find()) {
            m_name = m.group(1);
          }
          throw new CoreException(Status.OK_STATUS);
        }
      }
      catch (IOException e) {
        ScoutSdkUi.logError("could not parse file '" + configIniFile.getFullPath() + "'.", e);
      }
      finally {
        if (is != null) {
          try {
            is.close();
          }
          catch (IOException e) {
          }
        }
      }
    }

    public String getName() {
      return m_name;
    }
  }
}
