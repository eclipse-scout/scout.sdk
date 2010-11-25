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
package org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile;

import java.beans.PropertyChangeListener;
import java.io.InputStream;

import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.nls.sdk.model.workspace.INlsResource;
import org.eclipse.scout.nls.sdk.model.workspace.util.PropertyFileReader;

public abstract class AbstractNlsFile implements INlsResource {

  public static final String PROP_NLS_TYPE_NAME = "nlsTypeName";
  private BasicPropertySupport m_propertySupport;

  public static final String MANIFEST_CLASS = "Nls-Class";
  private String m_name;

  public AbstractNlsFile(InputStream stream, String name) {
    m_propertySupport = new BasicPropertySupport(this);
    m_name = name;
    parseInput(stream, name);
  }

  protected void parseInput(InputStream stream, String name) {
    PropertyFileReader reader = new PropertyFileReader(stream, name);
    m_propertySupport.setPropertyString(PROP_NLS_TYPE_NAME, reader.getAttribute(MANIFEST_CLASS));
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  public String getName() {
    return m_name;
  }

  /**
   * @return the fully quallified class name (e.g. java.lang.String)
   */
  public String getNlsTypeName() {
    return m_propertySupport.getPropertyString(PROP_NLS_TYPE_NAME);
  }
}
