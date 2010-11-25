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

import org.eclipse.swt.graphics.Image;

/**
 * <h3>AbstractLink</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public abstract class AbstractLink implements ILink {

  private final int m_orderNumber;
  private String m_name;
  private Image m_image;

  public AbstractLink(int orderNumber) {
    this(null, null, orderNumber);
  }

  public AbstractLink(String name) {
    this(name, null, 0);
  }

  public AbstractLink(String name, int order) {
    this(name, null, order);
  }

  public AbstractLink(String name, Image imgage, int orderNumber) {
    m_name = name;
    m_image = imgage;
    m_orderNumber = orderNumber;
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
    m_name = name;
  }

  @Override
  public String getName() {
    return m_name;
  }

  /**
   * @param image
   *          the image to set
   */
  public void setImage(Image image) {
    m_image = image;
  }

  @Override
  public Image getImage() {
    return m_image;
  }
}
