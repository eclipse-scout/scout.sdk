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
package org.eclipse.scout.sdk.ui.wizard;

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>AbstractProcessWizardPage</h3> ...
 */
public abstract class AbstractWorkspaceWizardPage extends AbstractScoutWizardPage {

  private BasicPropertySupport m_propertySupport;

  public AbstractWorkspaceWizardPage(String pageName, String title, ImageDescriptor titleImage) {
    super(pageName, title, titleImage);
    m_propertySupport = new BasicPropertySupport(this);

  }

  public AbstractWorkspaceWizardPage(String pageName) {
    this(pageName, null, (ImageDescriptor) null);
  }

  @Override
  public final boolean performFinish() {
    return true;
  }

  /**
   * Override this method to do modifications on resources. This method will be called within
   * a ProcessJob using the IWorkspaceRoot rule.
   * 
   * @param monitor
   * @param manager
   * @return
   */
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    return true;
  }

  /**
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @param name
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#getProperty(java.lang.String)
   */
  public Object getProperty(String name) {
    return m_propertySupport.getProperty(name);
  }

  /**
   * @param name
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#getPropertyBool(java.lang.String)
   */
  public boolean getPropertyBool(String name) {
    return m_propertySupport.getPropertyBool(name);
  }

  /**
   * @param name
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#getPropertyDouble(java.lang.String)
   */
  public double getPropertyDouble(String name) {
    return m_propertySupport.getPropertyDouble(name);
  }

  /**
   * @param name
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#getPropertyInt(java.lang.String)
   */
  public int getPropertyInt(String name) {
    return m_propertySupport.getPropertyInt(name);
  }

  /**
   * @param name
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#getPropertyLong(java.lang.String)
   */
  public long getPropertyLong(String name) {
    return m_propertySupport.getPropertyLong(name);
  }

  /**
   * @param name
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#getPropertyString(java.lang.String)
   */
  public String getPropertyString(String name) {
    return m_propertySupport.getPropertyString(name);
  }

  /**
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * @param name
   * @param newValue
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setProperty(java.lang.String, java.lang.Object)
   */
  public boolean setProperty(String name, Object newValue) {
    return m_propertySupport.setProperty(name, newValue);
  }

  /**
   * @param name
   * @param newValue
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyAlwaysFire(java.lang.String, java.lang.Object)
   */
  public void setPropertyAlwaysFire(String name, Object newValue) {
    m_propertySupport.setPropertyAlwaysFire(name, newValue);
  }

  /**
   * @param name
   * @param b
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyBool(java.lang.String, boolean)
   */
  public boolean setPropertyBool(String name, boolean b) {
    return m_propertySupport.setPropertyBool(name, b);
  }

  /**
   * @param name
   * @param d
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyDouble(java.lang.String, double)
   */
  public void setPropertyDouble(String name, double d) {
    m_propertySupport.setPropertyDouble(name, d);
  }

  /**
   * @param name
   * @param i
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyInt(java.lang.String, int)
   */
  public void setPropertyInt(String name, int i) {
    m_propertySupport.setPropertyInt(name, i);
  }

  /**
   * @param name
   * @param i
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyLong(java.lang.String, long)
   */
  public void setPropertyLong(String name, long i) {
    m_propertySupport.setPropertyLong(name, i);
  }

  /**
   * @param name
   * @param newValue
   * @return
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyNoFire(java.lang.String, java.lang.Object)
   */
  public boolean setPropertyNoFire(String name, Object newValue) {
    return m_propertySupport.setPropertyNoFire(name, newValue);
  }

  /**
   * @param name
   * @param s
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#setPropertyString(java.lang.String, java.lang.String)
   */
  public void setPropertyString(String name, String s) {
    m_propertySupport.setPropertyString(name, s);
  }

}
