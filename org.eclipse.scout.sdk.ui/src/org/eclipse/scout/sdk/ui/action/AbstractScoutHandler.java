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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

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
    ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
    if (selection instanceof IStructuredSelection) {
      Shell shell = HandlerUtil.getActiveShell(event);
      IStructuredSelection strucSelection = (IStructuredSelection) selection;

      IPage[] selectedPages = new IPage[strucSelection.size()];

      // iterator can only contain IPage's. this is ensured by the MenuVisibilityTester class
      Iterator<IPage> it = strucSelection.iterator();
      int index = 0;
      while (it.hasNext()) {
        selectedPages[index++] = it.next();
      }

      return execute(shell, selectedPages, event);
    }
    return null;
  }

  @Override
  public abstract Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException;
}
