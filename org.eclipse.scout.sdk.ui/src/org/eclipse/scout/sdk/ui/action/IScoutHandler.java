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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 *
 */
public interface IScoutHandler {
  public enum Category {
    OPEN("org.eclipse.scout.sdk.ui.menu.category.new", 5),
    NEW("org.eclipse.scout.sdk.ui.menu.category.new", 10),
    RENAME("org.eclipse.scout.sdk.ui.menu.category.rename", 20),
    UDPATE("org.eclipse.scout.sdk.ui.menu.category.update", 30),
    MOVE("org.eclipse.scout.sdk.ui.menu.category.move", 40),
    TEMPLATE("org.eclipse.scout.sdk.ui.menu.category.template", 50),
    IMPORT("org.eclipse.scout.sdk.ui.menu.category.import", 900),
    DELETE("org.eclipse.scout.sdk.ui.menu.category.delete", 1000),
    WS("org.eclipse.scout.sdk.ui.menu.category.ws", 2000),
    SPEC("org.eclipse.scout.sdk.ui.menu.category.spec", 3000),
    WICKET("org.eclipse.scout.sdk.ui.menu.category.wicket", 4000),
    OTHER("org.eclipse.scout.sdk.ui.menu.category.other", 10000);

    private String m_id;
    private int m_order;

    private Category(String id, int order) {
      m_id = id;
      m_order = order;
    }

    public int getOrder() {
      return m_order;
    }

    public String getId() {
      return m_id;
    }
  }

  /**
   * is evaluated after the menu has been prepared by the contributor.
   * 
   * @return
   */
  public boolean isVisible();

  public String getLabel();

  public void setLabel(String label);

  /**
   * Currently not supported.
   * 
   * @see {@link CommandContributionItemParameter#tooltip}
   * @param toolTip
   */
  public void setToolTip(String toolTip);

  public String getToolTip();

  public ImageDescriptor getImage();

  public void setImage(ImageDescriptor imageName);

  public String getKeyStroke();

  public void setKeyStroke(String keyStroke);

  /**
   * multi selection evaluation is done before the page has been prepared by the contributor.
   * 
   * @return
   */
  public boolean isMultiSelectSupported();

  public void setMultiSelectSupported(boolean multiSelectSupported);

  public Category getCategory();

  public void setCategory(Category category);

  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException;
}
