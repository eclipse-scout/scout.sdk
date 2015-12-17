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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.sdk.core.util.BasicPropertySupport;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>AbstractScoutWizardPage</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.0 11.01.2008
 */
public abstract class AbstractWizardPage extends WizardPage {

  private IStatus m_status = Status.OK_STATUS;
  private Composite m_content;
  private int m_stateChangingCounter = 0;
  private boolean m_excludePage;

  private final List<IStatusProvider> m_statusProvider;
  private final FieldToolkit m_fieldToolkit;
  private final BasicPropertySupport m_propertySupport;

  public AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
    super(pageName, title, titleImage);
    m_fieldToolkit = new FieldToolkit();
    m_statusProvider = new ArrayList<>();
    m_propertySupport = new BasicPropertySupport(this);
  }

  public AbstractWizardPage(String pageName) {
    this(pageName, null, (ImageDescriptor) null);

  }

  public FieldToolkit getFieldToolkit() {
    return m_fieldToolkit;
  }

  /**
   * Override to handle each page activate.
   */
  public void postActivate() {
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
   *
   * @param parent
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

  public boolean addStatusProvider(IStatusProvider provider) {
    return m_statusProvider.add(provider);
  }

  public boolean removeStatusProvider(IStatusProvider provider) {
    return m_statusProvider.remove(provider);
  }

  protected void setStatus(IStatus status) {
    if (status == null) {
      status = Status.OK_STATUS;
    }
    if (status.isOK()) {
      setMessage(getDescription(), IStatus.OK);
    }
    else {
      IStatus highestSeverityStatus = getHighestSeverityStatus(status, Status.OK_STATUS);
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

  private IStatus getHighestSeverityStatus(IStatus status, IStatus highestSeverity) {
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
   *
   * @param changing
   */
  protected void setStateChanging(boolean changing) {
    if (changing) {
      m_stateChangingCounter++;
    }
    else {
      m_stateChangingCounter--;
    }
    if (m_stateChangingCounter <= 0) {
      m_stateChangingCounter = 0;
      revalidate();
    }
  }

  /**
   * call to revalidate the wizard page. this method calls the overwritable method
   * {@link AbstractWizardPage#validatePage(MultiStatus)}.
   *
   * @see {@link AbstractWizardPage#validatePage(MultiStatus)}
   */
  protected final void revalidate() {
    setStatus(computePageStatus());
  }

  protected final MultiStatus computePageStatus() {
    MultiStatus multiStatus = new MultiStatus(S2ESdkUiActivator.PLUGIN_ID, -1, "multi status", null);
    validatePage(multiStatus);
    for (IStatusProvider p : m_statusProvider) {
      p.validate(this, multiStatus);
    }
    return multiStatus;
  }

  /**
   * Classes extending BCWizardPage can overwrite this method to do some page validation and add additional status to
   * the given multi status.
   *
   * @param multiStatus
   */
  protected void validatePage(MultiStatus multiStatus) {
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
   * sets the focus when the page is activated. Can be overridden.
   */
  public void setFocus() {
    getControl().setFocus();
  }

  /**
   * @param type
   *          defines the data type returned
   */
  @SuppressWarnings("unchecked")
  public <T extends Object> T getProperty(String key, Class<T> type) {
    return (T) m_propertySupport.getProperty(key);
  }

  public boolean getPropertyBool(String name) {
    return m_propertySupport.getPropertyBool(name);
  }

  public double getPropertyDouble(String name) {
    return m_propertySupport.getPropertyDouble(name);
  }

  public int getPropertyInt(String name) {
    return m_propertySupport.getPropertyInt(name);
  }

  public long getPropertyLong(String name) {
    return m_propertySupport.getPropertyLong(name);
  }

  public String getPropertyString(String name) {
    return m_propertySupport.getPropertyString(name);
  }

  public boolean setProperty(String name, Object newValue) {
    return m_propertySupport.setProperty(name, newValue);
  }

  public boolean setPropertyBool(String name, boolean b) {
    return m_propertySupport.setPropertyBool(name, b);
  }

  public void setPropertyDouble(String name, double d) {
    m_propertySupport.setPropertyDouble(name, d);
  }

  public void setPropertyInt(String name, int i) {
    m_propertySupport.setPropertyInt(name, i);
  }

  public void setPropertyLong(String name, long i) {
    m_propertySupport.setPropertyLong(name, i);
  }

  public void setPropertyString(String name, String s) {
    m_propertySupport.setPropertyString(name, s);
  }
}
