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

import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Represents a scout menu contribution
 */
public interface IScoutHandler extends IHandler2 {

  /**
   * <h3>{@link Category}</h3> Enum for the supported context menu categories. The grouping of the menus is according to
   * the category.
   *
   * @author Matthias Villiger
   * @since 3.10.0 18.09.2013
   */
  enum Category {
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
   * Specifies if the menu should be visible for the given {@link IStructuredSelection}
   *
   * @param selection
   *          The {@link IStructuredSelection} to evaluate
   * @return <code>true</code> if the menu should be visible, <code>false</code> otherwise.
   */
  boolean isVisible(IStructuredSelection selection);

  /**
   * Specifies if the menu should be enabled for the given {@link IStructuredSelection}.
   * 
   * @param selection
   *          the {@link IStructuredSelection} to evaluate
   * @return <code>true</code> if the menu should be visible, <code>false</code> otherwise.
   */
  boolean isActive(IStructuredSelection selection);

  /**
   * @return the text shown on the context menu
   */
  String getLabel();

  /**
   * @param label
   *          the text shown on the context menu.
   */
  void setLabel(String label);

  /**
   * Currently not supported.
   *
   * @see {@link CommandContributionItemParameter#tooltip}
   * @param toolTip
   */
  void setToolTip(String toolTip);

  /**
   * Currently not supported.
   *
   * @see {@link CommandContributionItemParameter#tooltip}
   * @param toolTip
   */
  String getToolTip();

  /**
   * @return the icon of the context menu.
   */
  ImageDescriptor getImage();

  /**
   * @param imageName
   *          the icon of the context menu
   */
  void setImage(ImageDescriptor imageName);

  /**
   * @return the key stroke used for the context menu
   */
  String getKeyStroke();

  /**
   * @param keyStroke
   *          the key stroke used for the context menu
   */
  void setKeyStroke(String keyStroke);

  /**
   * multi selection evaluation is done before the page has been prepared by the contributor.
   *
   * @return true if the context menu supports multi selection. If true, the context menu will also be shown, when
   *         multiple pages supporting a context menu are selected.
   */
  boolean isMultiSelectSupported();

  /**
   * sets if the context menu supports multi selection
   *
   * @param multiSelectSupported
   */
  void setMultiSelectSupported(boolean multiSelectSupported);

  /**
   * @return the category of the context menu.
   * @see Category
   */
  Category getCategory();

  /**
   * @param category
   *          The category of the context menu.
   * @see Category
   */
  void setCategory(Category category);

  /**
   * @return the id of the context menu. must be unique over all context menus.
   */
  String getId();
}
