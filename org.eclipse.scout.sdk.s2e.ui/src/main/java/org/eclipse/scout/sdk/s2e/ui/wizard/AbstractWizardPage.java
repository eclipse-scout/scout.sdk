/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.wizard;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.sdk.core.util.PropertySupport;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>AbstractScoutWizardPage</h3>
 *
 * @since 1.0.0 2008-01-11
 */
public abstract class AbstractWizardPage extends WizardPage {

  private IStatus m_status = Status.OK_STATUS;
  private Composite m_content;
  private int m_stateChangingCounter;
  private boolean m_excludePage;

  private final PropertySupport m_propertySupport;

  protected AbstractWizardPage(String pageName) {
    super(pageName, null, null);
    m_propertySupport = new PropertySupport(this);
  }

  @Override
  public final void createControl(Composite parent) {
    m_content = new Composite(parent, SWT.NONE);
    m_content.setLayout(new FillLayout());
    createContent(m_content);
    setControl(m_content);
    revalidate();
  }

  @Override
  protected boolean isControlCreated() {
    return super.isControlCreated() && !getControl().isDisposed();
  }

  /**
   * Overwrite this method to add your controls to the wizard page.
   */
  protected abstract void createContent(Composite parent);

  @Override
  public Composite getControl() {
    return m_content;
  }

  @Override
  public void setVisible(boolean visible) {
    m_content.setVisible(visible);
  }

  protected void setStatus(IStatus status) {
    if (status == null) {
      status = Status.OK_STATUS;
    }
    if (status.isOK()) {
      setMessage(getDescription(), IStatus.OK);
    }
    else {
      IStatus highestSeverityStatus = getHighestSeverityStatus(status);
      int messagetype;
      switch (highestSeverityStatus.getSeverity()) {
        case IStatus.INFO:
          messagetype = IMessageProvider.INFORMATION;
          break;
        case IStatus.WARNING:
          messagetype = IMessageProvider.WARNING;
          break;
        case IStatus.ERROR:
          messagetype = IMessageProvider.ERROR;
          break;
        default:
          messagetype = IMessageProvider.NONE;
          break;
      }
      String message = highestSeverityStatus.getMessage();
      setMessage(message, messagetype);
    }
    setPageComplete(!status.matches(IStatus.ERROR));
    m_status = status;
  }

  public static IStatus getHighestSeverityStatus(IStatus status) {
    return getHighestSeverityStatus(status, Status.OK_STATUS);
  }

  private static IStatus getHighestSeverityStatus(IStatus status, IStatus highestSeverity) {
    if (status.isMultiStatus()) {
      for (IStatus child : status.getChildren()) {
        highestSeverity = getHighestSeverityStatus(child, highestSeverity);
      }
      return highestSeverity;
    }

    if (highestSeverity.getSeverity() < status.getSeverity()) {
      highestSeverity = status;
    }
    return highestSeverity;
  }

  /**
   * to force a revalidate if needed.
   */
  public void pingStateChanging() {
    if (m_stateChangingCounter <= 0) {
      m_stateChangingCounter = 0;
      revalidate();
    }
  }

  /**
   * NOTE: always call this method in a try finally block.
   */
  protected void setStateChanging(boolean changing) {
    if (changing) {
      m_stateChangingCounter++;
    }
    else {
      m_stateChangingCounter--;
    }
    pingStateChanging();
  }

  protected boolean isStateChanging() {
    return m_stateChangingCounter > 0;
  }

  /**
   * call to revalidate the wizard page. this method calls the overwritable method
   * {@link AbstractWizardPage#validatePage(MultiStatus)}.
   *
   * @see AbstractWizardPage#validatePage(MultiStatus)
   */
  protected final void revalidate() {
    setStatus(computePageStatus());
  }

  protected final MultiStatus computePageStatus() {
    MultiStatus multiStatus = new MultiStatus(S2ESdkUiActivator.PLUGIN_ID, -1, "multi status", null);
    validatePage(multiStatus);
    return multiStatus;
  }

  /**
   * Classes extending {@link AbstractWizardPage} can overwrite this method to do some page validation and add
   * additional status to the given {@link MultiStatus}.
   *
   * @param multiStatus
   *          The {@link MultiStatus} to modify.
   */
  protected void validatePage(MultiStatus multiStatus) {
    // may be implemented by subclasses
  }

  public boolean performFinish() {
    return true;
  }

  protected IStatus getStatus() {
    return m_status;
  }

  public void setExcludePage(boolean excludePage) {
    m_excludePage = excludePage;
  }

  public boolean isExcludePage() {
    return m_excludePage;
  }

  /**
   * @param type
   *          defines the data type returned
   */
  public <T> T getProperty(String key, Class<T> type) {
    return m_propertySupport.getProperty(key, type);
  }

  public boolean getPropertyBool(String name) {
    return getPropertyBool(name, false);
  }

  public boolean getPropertyBool(String name, boolean defaultValue) {
    return m_propertySupport.getPropertyBool(name, defaultValue);
  }

  public double getPropertyDouble(String name) {
    return m_propertySupport.getPropertyDouble(name, 0.0);
  }

  public int getPropertyInt(String name) {
    return m_propertySupport.getPropertyInt(name, 0);
  }

  public long getPropertyLong(String name) {
    return m_propertySupport.getPropertyLong(name, 0L);
  }

  public String getPropertyString(String name) {
    return m_propertySupport.getPropertyString(name);
  }

  public boolean setProperty(String name, Object newValue) {
    return m_propertySupport.setProperty(name, newValue);
  }

  /**
   * Executes the specified property changer. If the result is {@code true} (meaning 'changed') and the specified
   * control is not {@code null}, the specified control consumer is executed to update the control if necessary.
   *
   * @param controlToChange
   *          The {@link Control} that should be updated in case the property changed.
   * @param propertyChanger
   *          The {@link BooleanSupplier} that updates the property. The return value indicates if the property has
   *          actually been changed.
   * @param controlChanger
   *          A {@link Consumer} that updates the specified {@link Control} if the property has changed.
   * @return {@code true} if the property changed, {@code false} otherwise.
   */
  protected <T extends Control> boolean setPropertyWithChangingControl(T controlToChange, BooleanSupplier propertyChanger, Consumer<T> controlChanger) {
    try {
      setStateChanging(true);
      boolean changed = propertyChanger.getAsBoolean();
      if (changed && controlToChange != null && isControlCreated()) {
        controlChanger.accept(controlToChange);
      }
      return changed;
    }
    finally {
      setStateChanging(false);
    }
  }

  public boolean setPropertyBool(String name, boolean b) {
    return m_propertySupport.setPropertyBool(name, b);
  }

  public boolean setPropertyDouble(String name, double d) {
    return m_propertySupport.setPropertyDouble(name, d);
  }

  public boolean setPropertyInt(String name, int i) {
    return m_propertySupport.setPropertyInt(name, i);
  }

  public boolean setPropertyLong(String name, long i) {
    return m_propertySupport.setPropertyLong(name, i);
  }

  public boolean setPropertyString(String name, String s) {
    return m_propertySupport.setPropertyString(name, s);
  }
}
