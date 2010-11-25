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
package org.eclipse.scout.sdk.ui.view.properties.presenter;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>{@link AbstractPresenter}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 */
public abstract class AbstractPresenter {
  private Composite m_container;
  private final FormToolkit m_toolkit;

  public AbstractPresenter(FormToolkit toolkit, Composite parent) {
    m_toolkit = toolkit;
    m_container = getToolkit().createComposite(parent);

  }

  public boolean isMultiLine() {
    return false;
  }

  protected Font getFont(String symbolicName, boolean bold) {
    if (bold) {
      return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(symbolicName);
    }
    else {
      return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(symbolicName);
    }
  }

  public Composite getContainer() {
    return m_container;
  }

  public void dispose() {
    if (getContainer() != null && (!getContainer().isDisposed())) {
      getContainer().dispose();
    }
  }

  public boolean isDisposed() {
    boolean disposed = true;
    if (getContainer() != null) {
      disposed = getContainer().isDisposed();
    }
    return disposed;
  }

  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      getContainer().setEnabled(enabled);
    }
  }

  public boolean isEnabled() {
    boolean enabled = false;
    if (getContainer() != null && !getContainer().isDisposed()) {
      enabled = getContainer().isEnabled();
    }
    return enabled;
  }

  public FormToolkit getToolkit() {
    return m_toolkit;
  }

  protected void toolkitAdapt(Control control) {
    if (control instanceof Composite) {
      for (Control c : ((Composite) control).getChildren()) {
        toolkitAdapt(c);
      }
    }
    getToolkit().adapt(control, false, false);
  }

}
