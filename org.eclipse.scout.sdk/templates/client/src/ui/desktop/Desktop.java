package @@BUNDLE_CLIENT_NAME@@.ui.desktop;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.menu.AbstractBookmarkMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;
import org.eclipse.scout.rt.extension.client.ui.desktop.AbstractExtensibleDesktop;
import @@BUNDLE_CLIENT_NAME@@.ClientSession;
import org.eclipse.scout.rt.shared.TEXTS;

public class Desktop extends AbstractExtensibleDesktop implements IDesktop {
  private static IScoutLogger logger = ScoutLogManager.getLogger(Desktop.class);

  public Desktop() {
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("ApplicationTitle");
  }

  @Order(1000.0)
  public class FileMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("FileMenu");
    }

    @Order(1000.0)
    public class ExitMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("ExitMenu");
      }

      @Override
      public void execAction() throws ProcessingException {
        ClientSyncJob.getCurrentSession(ClientSession.class).stopSession();
      }
    }
  }

  @Order(2000.0)
  public class ToolsMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("ToolsMenu");
    }
  }

  @Order(3000.0)
  public class BookmarkMenu extends AbstractBookmarkMenu {
    public BookmarkMenu() {
      super(Desktop.this);
    }
  }

  @Order(4000.0)
  public class HelpMenu extends AbstractMenu {

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("HelpMenu");
    }

    @Order(1000.0)
    public class AboutMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return TEXTS.get("AboutMenu");
      }

      @Override
      public void execAction() throws ProcessingException {
        ScoutInfoForm form=new ScoutInfoForm();
        form.startModify();
      }
    }
  }

  @Order(1000.0)
  public class RefreshOutlineKeyStroke extends AbstractKeyStroke {

    @Override
    protected String getConfiguredKeyStroke() {
      return "f5";
    }

    @Override
    protected void execAction() throws ProcessingException {
      if (getOutline() != null) {
        IPage page = getOutline().getActivePage();
        if (page != null) {
          page.reloadPage();
        }
      }
    }
  }
}
