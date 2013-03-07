package @@BUNDLE_MOBILE_CLIENT_NAME@@.ui.desktop;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension;
import org.eclipse.scout.rt.client.ui.desktop.ContributionCommand;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.eclipse.scout.service.SERVICES;

public class DesktopExtension extends AbstractDesktopExtension {

  private HomeForm m_homeForm;
  private boolean m_active;

  public DesktopExtension() {
    setActive(!UserAgentUtility.isDesktopDevice());
  }

  @Override
  protected ContributionCommand execGuiAttached() throws ProcessingException {
    if (!isActive()) {
      return super.execGuiAttached();
    }

    if (m_homeForm == null) {
      m_homeForm = new HomeForm();
      m_homeForm.startView();
    }

    return ContributionCommand.Continue;
  }

  @Override
  protected ContributionCommand execGuiDetached() throws ProcessingException {
    if (!isActive()) {
      return super.execGuiDetached();
    }

    if (m_homeForm != null) {
      m_homeForm.doClose();
    }

    return ContributionCommand.Continue;
  }

  public boolean isActive() {
    return m_active;
  }

  public void setActive(boolean active) {
    m_active = active;
  }
}
