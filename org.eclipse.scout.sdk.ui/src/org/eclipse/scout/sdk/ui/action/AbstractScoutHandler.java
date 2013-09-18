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

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractScoutHandler extends AbstractHandler implements IScoutHandler {
  private String m_label;
  private String m_toolTip;
  private ImageDescriptor m_image;
  private String m_keyStroke;
  private boolean m_multiSelectSupported;
  private Category m_category;

  public AbstractScoutHandler(String label) {
    this(label, null);
  }

  public AbstractScoutHandler(String label, ImageDescriptor image) {
    this(label, image, null);
  }

  public AbstractScoutHandler(String label, ImageDescriptor image, String keyStroke) {
    this(label, image, keyStroke, false);
  }

  public AbstractScoutHandler(String label, ImageDescriptor image, String keyStroke, boolean multiSelectSupported) {
    this(label, image, keyStroke, multiSelectSupported, Category.OTHER);
  }

  public AbstractScoutHandler(String label, ImageDescriptor image, String keyStroke, boolean multiSelectSupported, Category cat) {
    m_label = label;
    m_image = image;
    m_keyStroke = keyStroke;
    m_multiSelectSupported = multiSelectSupported;
    if (cat == null) m_category = Category.OTHER;
    else m_category = cat;
  }

  /**
   * use {@link IScoutHandler#isActive()} to calculate and return the enabled/disabled state of the context menu.
   */
  @Override
  public final void setEnabled(Object evaluationContext) {
    super.setEnabled(evaluationContext);
    if (evaluationContext instanceof BooleanHolder) {
      setBaseEnabled(((BooleanHolder) evaluationContext).getValue());
    }
  }

  /**
   * use {@link IScoutHandler#isActive()} to calculate and return the enabled/disabled state of the context menu.
   * 
   * @return if the current context menu is active or not.
   */
  @Override
  public final boolean isEnabled() {
    return super.isEnabled();
  }

  /**
   * use {@link IScoutHandler#isActive()} to calculate and return the enabled/disabled state of the context menu.
   */
  @Override
  protected final void setBaseEnabled(boolean state) {
    super.setBaseEnabled(state);
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public String getLabel() {
    return m_label;
  }

  @Override
  public void setLabel(String label) {
    m_label = label;
  }

  @Override
  public String getToolTip() {
    return m_toolTip;
  }

  @Override
  public void setToolTip(String toolTip) {
    m_toolTip = toolTip;
  }

  @Override
  public ImageDescriptor getImage() {
    return m_image;
  }

  @Override
  public void setImage(ImageDescriptor imageName) {
    m_image = imageName;
  }

  @Override
  public String getKeyStroke() {
    return m_keyStroke;
  }

  @Override
  public void setKeyStroke(String keyStroke) {
    m_keyStroke = keyStroke;
  }

  /**
   * multi selection evaluation is done before the page has been prepared.
   * 
   * @return
   */
  @Override
  public boolean isMultiSelectSupported() {
    return m_multiSelectSupported;
  }

  @Override
  public void setMultiSelectSupported(boolean multiSelectSupported) {
    m_multiSelectSupported = multiSelectSupported;
  }

  @Override
  public Category getCategory() {
    return m_category;
  }

  @Override
  public void setCategory(Category category) {
    m_category = category;
  }

  @Override
  public final String getId() {
    return this.getClass().getName();
  }

  @SuppressWarnings("unchecked")
  @Override
  public final Object execute(ExecutionEvent event) throws ExecutionException {
    IScoutExplorerPart explorer = ScoutSdkUi.getExplorer(false);
    if (explorer != null) {
      IStructuredSelection selection = explorer.getSelection();
      if (selection != null && explorer.getSite() != null) {
        Shell shell = explorer.getSite().getShell();

        IPage[] selectedPages = new IPage[selection.size()];

        // iterator can only contain IPage's. this is ensured by the MenuVisibilityTester class
        Iterator<IPage> it = selection.iterator();
        int index = 0;
        while (it.hasNext()) {
          selectedPages[index++] = it.next();
        }

        return execute(shell, selectedPages, event);
      }
    }
    return null;
  }

  @Override
  public abstract Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException;

  protected boolean isEditable(IJavaElement element) {
    if (!TypeUtility.exists(element)) {
      return false;
    }
    if (element.isReadOnly()) {
      return false;
    }
    IScoutBundle b = ScoutTypeUtility.getScoutBundle(element);
    return b != null && !b.isBinary();
  }
}
