/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.resource.ImageDescriptor;

public class ElementBean {

  private int m_id;
  private String m_name;
  private ImageDescriptor m_imageDescriptor;
  private boolean m_checked;
  private boolean m_mandatory;
  private IResource m_resource;
  private IJavaElement m_javaElement;
  private Object m_data;

  public ElementBean(int id, String name, ImageDescriptor imageDescriptor, boolean mandatory) {
    m_id = id;
    m_name = name;
    m_imageDescriptor = imageDescriptor;
    m_mandatory = mandatory;
    m_checked = true;
  }

  public ElementBean(int id, String name, ImageDescriptor imageDescriptor, IJavaElement javaElement, boolean mandatory) {
    this(id, name, imageDescriptor, mandatory);
    m_javaElement = javaElement;
  }

  public ElementBean(int id, String name, ImageDescriptor imageDescriptor, IResource resource, boolean mandatory) {
    this(id, name, imageDescriptor, mandatory);
    m_resource = resource;
  }

  public int getId() {
    return m_id;
  }

  public void setId(int id) {
    m_id = id;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public ImageDescriptor getImageDescriptor() {
    return m_imageDescriptor;
  }

  public void setImageDescriptor(ImageDescriptor imageDescriptor) {
    m_imageDescriptor = imageDescriptor;
  }

  public boolean isChecked() {
    return m_checked;
  }

  public void setChecked(boolean checked) {
    m_checked = checked;
  }

  public boolean isMandatory() {
    return m_mandatory;
  }

  public void setMandatory(boolean mandatory) {
    m_mandatory = mandatory;
  }

  public IResource getResource() {
    return m_resource;
  }

  public void setResource(IResource resource) {
    m_resource = resource;
  }

  public IJavaElement getJavaElement() {
    return m_javaElement;
  }

  public void setJavaElement(IJavaElement javaElement) {
    m_javaElement = javaElement;
  }

  public Object getData() {
    return m_data;
  }

  public void setData(Object data) {
    m_data = data;
  }
}
