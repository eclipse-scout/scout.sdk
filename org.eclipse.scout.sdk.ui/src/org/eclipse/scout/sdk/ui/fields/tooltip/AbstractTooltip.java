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
package org.eclipse.scout.sdk.ui.fields.tooltip;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>AbstractTooltip</h3> Capturing the tooltip handling. Extend this class to
 * provide a customized tooltip.
 */
public abstract class AbstractTooltip {

  private Control m_sourceControl;
  private CloseJob m_closeJob = new CloseJob();

  public Control getSourceControl() {
    return m_sourceControl;
  }

  private P_PopupListener m_listener;
  private Shell m_shell;
  private Object m_lock = new Object();

  public AbstractTooltip(Control sourceControl) {
    m_sourceControl = sourceControl;
    attachListeners();
    m_sourceControl.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        detachListeners();
      }
    });
  }

  private void attachListeners() {
    if (m_listener == null) {
      m_listener = new P_PopupListener();
    }
    m_sourceControl.addListener(SWT.MouseExit, m_listener);
    m_sourceControl.addListener(SWT.Dispose, m_listener);
    m_sourceControl.addListener(SWT.KeyDown, m_listener);
    m_sourceControl.addListener(SWT.MouseDown, m_listener);
    m_sourceControl.addListener(SWT.MouseMove, m_listener);
    m_sourceControl.addListener(SWT.MouseHover, m_listener);
    m_sourceControl.addListener(SWT.FocusOut, m_listener);

  }

  private void detachListeners() {
    if (m_listener != null) {
      m_sourceControl.getShell().removeListener(SWT.Deactivate, m_listener);
      m_sourceControl.removeListener(SWT.MouseExit, m_listener);
      m_sourceControl.removeListener(SWT.Dispose, m_listener);
      m_sourceControl.removeListener(SWT.KeyDown, m_listener);
      m_sourceControl.removeListener(SWT.MouseDown, m_listener);
      m_sourceControl.removeListener(SWT.MouseMove, m_listener);
      m_sourceControl.removeListener(SWT.MouseHover, m_listener);
      m_sourceControl.removeListener(SWT.FocusOut, m_listener);
      m_listener = null;
    }

  }

  protected abstract void createContent(Composite parent);

  protected void show(int x, int y) {
    Shell[] shells = getSourceControl().getShell().getShells();
    if (shells.length > 0) {
      for (Shell s : shells) {
        if (s != null && s.isVisible()) {
          return;
        }
      }
    }
    synchronized (m_lock) {
      if (m_shell == null) {
        m_shell = new Shell(m_sourceControl.getShell(), SWT.TOOL | SWT.ON_TOP | SWT.NO_FOCUS);
        m_shell.setBackground(m_shell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        GridLayout shellLayout = new GridLayout(1, true);
        m_shell.setLayout(shellLayout);
        createContent(m_shell);
      }
      m_shell.pack();
      m_shell.setBounds(computeBounds(m_shell.getSize()));
      if (m_shell.getChildren().length > 0) {
        m_shell.setVisible(true);
      }
      handleContentListeners(m_shell, true);
      m_shell.getParent().addListener(SWT.Deactivate, m_listener);
    }
  }

  private void handleContentListeners(Composite content, boolean add) {
    if (add) {
      content.addListener(SWT.MouseExit, m_listener);
    }
    else {
      content.removeListener(SWT.MouseExit, m_listener);
    }
    for (Control c : content.getChildren()) {
      if (c instanceof Composite) {
        handleContentListeners((Composite) c, add);
      }
      else {
        if (add) {
          c.addListener(SWT.MouseExit, m_listener);
        }
        else {
          c.removeListener(SWT.MouseExit, m_listener);
        }
      }
    }
  }

  private Rectangle computeBounds(Point shellSize) {
    Rectangle displayBounds = m_shell.getDisplay().getBounds();
    Rectangle sourceControlBounds = m_shell.getDisplay().map(m_sourceControl, null, m_sourceControl.getBounds());
    // try bottom right
    Rectangle bounds = new Rectangle(sourceControlBounds.x, sourceControlBounds.y + sourceControlBounds.height + 5, shellSize.x, shellSize.y);
    if ((bounds.x + bounds.width) > displayBounds.width) {
      // move left
      bounds.x = sourceControlBounds.x + sourceControlBounds.y - shellSize.y;
    }
    if (bounds.y + bounds.height > displayBounds.height) {
      // move up
      bounds.y = sourceControlBounds.y - shellSize.y - 5;
    }
    return bounds;
  }

  private void closeDelayed() {
    if (m_closeJob != null) {
      m_closeJob.cancel();
      m_closeJob.schedule(400);
    }
  }

  public void close() {
    synchronized (m_lock) {
      if (m_shell != null && !m_shell.isDisposed()) {
        Point cursorLocation = m_shell.getDisplay().getCursorLocation();
        Rectangle tooltipBounds = m_shell.getBounds();
        if (!tooltipBounds.contains(cursorLocation)) {
          m_shell.removeListener(SWT.MouseExit, m_listener);
          handleContentListeners(m_shell, false);
          m_shell.close();
          m_shell = null;
        }
      }
    }
  }

  private class P_PopupListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MouseHover:
          show(event.x, event.y);
          break;
        case SWT.Dispose:
        case SWT.KeyDown:
        case SWT.MouseDown:
        case SWT.MouseExit:
        case SWT.Deactivate:
          closeDelayed();
          break;
      }
    }
  } // end class P_PopupListener

  private class CloseJob extends Job {
    public CloseJob() {
      super("close tooltip");
      setSystem(true);
      setUser(false);
      setPriority(DECORATE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (m_shell != null && !m_shell.isDisposed()) {
        m_shell.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            close();
          }
        });
      }
      return Status.OK_STATUS;
    }

  }
}
