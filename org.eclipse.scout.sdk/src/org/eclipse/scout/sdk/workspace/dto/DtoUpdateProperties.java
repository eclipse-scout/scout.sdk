/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;

/**
 * <h3>{@link DtoUpdateProperties}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 16.08.2013
 */
public class DtoUpdateProperties {
  public static final String PROP_TYPE = "Type";
  public static final String PROP_SUPER_TYPE_HIERARCHY = "SuperTypeHierarchy";
  public static final String PROP_FORM_DATA_ANNOTATION = "FormDataAnnotation";
  public static final String PROP_PAGE_DATA_ANNOTATION = "PageDataAnnotation";

  private Map<String, Object> m_properties;

  public DtoUpdateProperties() {
    m_properties = new HashMap<String, Object>();
  }

  public void put(String key, Object value) {
    m_properties.put(key, value);
  }

  public Object get(String key) {
    return m_properties.get(key);
  }

  public boolean contains(String key) {
    return m_properties.containsKey(key);
  }

  public IType getType() {
    return (IType) get(PROP_TYPE);
  }

  public void setType(IType type) {
    put(PROP_TYPE, type);
  }

  public ITypeHierarchy getSuperTypeHierarchy() {
    return (ITypeHierarchy) get(PROP_SUPER_TYPE_HIERARCHY);
  }

  public void setSuperTypeHierarchy(ITypeHierarchy hierarchy) {
    put(PROP_SUPER_TYPE_HIERARCHY, hierarchy);
  }

  public FormDataAnnotation getFormDataAnnotation() {
    return (FormDataAnnotation) get(PROP_FORM_DATA_ANNOTATION);
  }

  public void setFormDataAnnotation(FormDataAnnotation annotation) {
    put(PROP_FORM_DATA_ANNOTATION, annotation);
  }

  public PageDataAnnotation getPageDataAnnotation() {
    return (PageDataAnnotation) get(PROP_PAGE_DATA_ANNOTATION);
  }

  public void setPageDataAnnotation(PageDataAnnotation annotation) {
    put(PROP_PAGE_DATA_ANNOTATION, annotation);
  }
}
