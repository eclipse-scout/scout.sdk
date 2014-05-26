package org.eclipse.scout.sdk.ui.internal.wizard.export;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Composite;

public class ExportServerWizardPage extends AbstractExportProductWizardPage {

  private static final Pattern SERVER_URL_PATTERN = Pattern.compile("\\/([^\\/]*)\\/process");
  private static final String SETTINGS_PRODUCT_FILE = "productFileSetting";
  private static final String SETTINGS_WAR_FILE_NAME = "warFileNameSetting";

  public ExportServerWizardPage(IScoutBundle scoutProject) {
    super(scoutProject, ExportServerWizardPage.class.getName(), Texts.get("ExportWebArchive"), IRuntimeClasses.ScoutServerBundleId,
        SETTINGS_PRODUCT_FILE, SETTINGS_WAR_FILE_NAME);
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    if (!StringUtility.hasText(m_warFileName.getModifiableText())) {
      String warName = findServerWarName();
      if (warName == null) {
        String alias = getWizard().getProjectAlias();
        if (alias != null) {
          warName = alias + "_server";
        }
      }
      m_warFileName.setText(warName);
    }
  }

  private String findServerWarName() {
    try {
      List<IResource> swingOrSwtProductFiles = ResourceUtility.getAllResources(ResourceFilters.getProductFileByContentFilter(true, IRuntimeClasses.ScoutUiSwingBundleId, IRuntimeClasses.ScoutUiSwtBundleId));
      for (IResource r : swingOrSwtProductFiles) {
        ProductFileModelHelper pfmh = new ProductFileModelHelper((IFile) r);
        String serverUrl = pfmh.ConfigurationFile.getEntry("server.url");
        if (StringUtility.hasText(serverUrl)) {
          Matcher m = SERVER_URL_PATTERN.matcher(serverUrl);
          if (m.find()) {
            return m.group(1);
          }
          throw new CoreException(Status.OK_STATUS);
        }
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning("Unable to determin possible server war name.", e);
    }
    return null;
  }
}
