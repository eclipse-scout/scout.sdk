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
package org.eclipse.scout.sdk.ui.internal.view.properties.model.links;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>AbstractLink</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public abstract class AbstractLink implements ILink {

  private final int m_orderNumber;
  private BasicPropertySupport m_propertySupport;

  public AbstractLink(int orderNumber) {
    this(null, null, orderNumber);
  }

  public AbstractLink(String name) {
    this(name, null, 0);
  }

  public AbstractLink(String name, int order) {
    this(name, null, order);
  }

  public AbstractLink(String name, Image image, int orderNumber) {
    m_propertySupport = new BasicPropertySupport(this);
    setName(name);
    setImage(image);
    m_orderNumber = orderNumber;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void dispose() {
  }

  @Override
  public int getOrderNumber() {
    return m_orderNumber;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    m_propertySupport.setPropertyString(PROP_NAME, name);
  }

  @Override
  public String getName() {
    return m_propertySupport.getPropertyString(PROP_NAME);
  }

  /**
   * @param image
   *          the image to set
   */
  public void setImage(Image image) {
    m_propertySupport.setProperty(PROP_IMAGE, image);
  }

  @Override
  public Image getImage() {
    return (Image) m_propertySupport.getProperty(PROP_IMAGE);
  }
}
