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
package test.client.ui.desktop;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.menu.AbstractBookmarkMenu;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.outline.DefaultOutlineTreeForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.service.SERVICES;

import test.client.ClientSession;
import test.shared.Icons;

public class Desktop extends AbstractDesktop implements IDesktop {

  public Desktop() {
  }

  @Override
  public String getConfiguredTitle() {
    return TEXTS.get("ApplicationTitle");
  }

  @Override
  protected void execOpened() throws ProcessingException {
    //outline form
    DefaultOutlineTreeForm treeForm = new DefaultOutlineTreeForm();
    treeForm.setIconId(Icons.EclipseScout);
    treeForm.startView();
    //outline table
    DefaultOutlineTableForm tableForm = new DefaultOutlineTableForm();
    tableForm.setIconId(Icons.EclipseScout);
    tableForm.startView();

    //load startup bookmark
    IBookmarkService bookmarkService = SERVICES.getService(IBookmarkService.class);
    bookmarkService.loadBookmarks();
    Bookmark bm = bookmarkService.getStartBookmark();
    if (bm != null) {
      bookmarkService.activate(bm);
    }
    else {
      if (getAvailableOutlines().length > 0) {
        setOutline(getAvailableOutlines()[0]);
      }
    }
  }

  @Order(10.0)
  public class FileMenu extends AbstractMenu {

    @Override
    public String getConfiguredText() {
      return TEXTS.get("FileMenu");
    }

    @Order(100.0)
    public class ExitMenu extends AbstractMenu {

      @Override
      public String getConfiguredText() {
        return TEXTS.get("ExitMenu");
      }

      @Override
      public void execAction() throws ProcessingException {
        ClientSyncJob.getCurrentSession(ClientSession.class).stopSession();
      }
    }
  }

  @Order(20.0)
  public class ToolsMenu extends AbstractMenu {

    @Override
    public String getConfiguredText() {
      return TEXTS.get("ToolsMenu");
    }
  }

  @Order(25)
  public class BookmarkMenu extends AbstractBookmarkMenu {
    public BookmarkMenu() {
      super(Desktop.this);
    }
  }

  @Order(30.0)
  public class HelpMenu extends AbstractMenu {

    @Override
    public String getConfiguredText() {
      return TEXTS.get("HelpMenu");
    }

    @Order(10.0)
    public class AboutMenu extends AbstractMenu {

      @Override
      public String getConfiguredText() {
        return TEXTS.get("AboutMenu");
      }

      @Override
      public void execAction() throws ProcessingException {
        ScoutInfoForm form = new ScoutInfoForm();
        form.startModify();
      }
    }

  }

}
