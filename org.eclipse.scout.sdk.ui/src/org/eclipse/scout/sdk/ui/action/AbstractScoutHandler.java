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

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.ui.extensions.executor.ExecutorExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.executor.IExecutor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;

public abstract class AbstractScoutHandler extends AbstractHandler implements IScoutHandler {
  private String m_label;
  private String m_toolTip;
  private ImageDescriptor m_image;
  private String m_keyStroke;
  private boolean m_multiSelectSupported;
  private Category m_category;
  private final IExecutor m_menuExecutor;

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
    if (cat == null) {
      m_category = Category.OTHER;
    }
    else {
      m_category = cat;
    }
    m_menuExecutor = ExecutorExtensionPoint.getExecutorFor(getExecutorId());
  }

  protected String getExecutorId() {
    return getClass().getName();
  }

  /**
   * use {@link IScoutHandler#isActive(IStructuredSelection))} to calculate and return the enabled/disabled state of the
   * context menu.
   *
   * @see IScoutHandler#isActive(IStructuredSelection)
   */
  @Override
  public final void setEnabled(Object evaluationContext) {
    super.setEnabled(evaluationContext);
    if (evaluationContext instanceof BooleanHolder) {
      setBaseEnabled(((BooleanHolder) evaluationContext).getValue());
    }
  }

  /**
   * use {@link IScoutHandler#isActive(IStructuredSelection))} to calculate and return the enabled/disabled state of the
   * context menu.
   *
   * @see IScoutHandler#isActive(IStructuredSelection)
   */
  @Override
  public final boolean isEnabled() {
    return super.isEnabled();
  }

  /**
   * use {@link IScoutHandler#isActive(IStructuredSelection))} to calculate and return the enabled/disabled state of the
   * context menu.
   *
   * @see IScoutHandler#isActive(IStructuredSelection)
   */
  @Override
  protected final void setBaseEnabled(boolean state) {
    super.setBaseEnabled(state);
  }

  @Override
  public boolean isActive(IStructuredSelection selection) {
    return true;
  }

  @Override
  public final boolean isVisible(IStructuredSelection selection) {
    return m_menuExecutor.canRun(selection);
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
    return getClass().getName();
  }

  public static void exec(Shell shell, IScoutHandler action, Object... args) {
    ExecutionEvent event = createEvent(shell, args);
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
    if (action.isVisible(selection) && action.isActive(selection)) {
      try {
        action.execute(event);
      }
      catch (ExecutionException e1) {
        ScoutSdkUi.logError("Unable to execute action. ", e1);
      }
    }
  }

  public static ExecutionEvent createEvent(Shell shell, IStructuredSelection sel) {
    IEvaluationContext parent = null;
    IEvaluationService esrvc = (IEvaluationService) PlatformUI.getWorkbench().getService(IEvaluationService.class);
    if (esrvc != null) {
      parent = esrvc.getCurrentState();
    }

    EvaluationContext ctx = new EvaluationContext(parent, sel);
    ctx.setAllowPluginActivation(true);
    ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, sel);
    ctx.addVariable(ISources.ACTIVE_SHELL_NAME, shell);
    ExecutionEvent event = new ExecutionEvent(null, Collections.EMPTY_MAP, null, ctx);
    return event;
  }

  public static ExecutionEvent createEvent(Shell shell, Object... args) {
    return createEvent(shell, new StructuredSelection(args));
  }

  @Override
  public final Object execute(ExecutionEvent event) throws ExecutionException {

    ISelection sel = HandlerUtil.getCurrentSelection(event);

    // get selection
    IStructuredSelection selection = null;
    if (sel instanceof IStructuredSelection) {
      selection = (IStructuredSelection) sel;
    }
    else {
      selection = StructuredSelection.EMPTY;
    }

    // get shell
    Shell shell = HandlerUtil.getActiveShell(event);
    if (shell == null) {
      shell = ScoutSdkUi.getShell();
    }
    if (shell == null) {
      Display display = Display.getDefault();
      if (display != null) {
        shell = display.getActiveShell();
      }
    }
    return m_menuExecutor.run(shell, selection, event);
  }
}
