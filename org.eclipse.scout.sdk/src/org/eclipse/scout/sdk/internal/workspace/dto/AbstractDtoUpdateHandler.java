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
package org.eclipse.scout.sdk.internal.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractDtoUpdateHandler}</h3>
 * 
 * @author aho
 * @since 3.10.0 16.08.2013
 */
public abstract class AbstractDtoUpdateHandler implements IDtoAutoUpdateHandler {

  protected FormDataAnnotation ensurePropertyFormDataAnnotation(DtoUpdateProperties properties) throws CoreException {

    FormDataAnnotation annotation = properties.getFormDataAnnotation();
    if (annotation == null && !properties.contains(DtoUpdateProperties.PROP_FORM_DATA_ANNOTATION)) {
      annotation = ScoutTypeUtility.findFormDataAnnotation(properties.getType(), ensurePropertySuperTypeHierarchy(properties));
      properties.put(DtoUpdateProperties.PROP_FORM_DATA_ANNOTATION, annotation);
    }
    return annotation;
  }

  protected PageDataAnnotation ensurePropertyPageDataAnnotation(DtoUpdateProperties properties) throws CoreException {
    PageDataAnnotation annotation = properties.getPageDataAnnotation();
    if (annotation == null && !properties.contains(DtoUpdateProperties.PROP_PAGE_DATA_ANNOTATION)) {
      annotation = ScoutTypeUtility.findPageDataAnnotation(properties.getType(), ensurePropertySuperTypeHierarchy(properties));
      properties.put(DtoUpdateProperties.PROP_FORM_DATA_ANNOTATION, annotation);
    }
    return annotation;
  }

  protected ITypeHierarchy ensurePropertySuperTypeHierarchy(DtoUpdateProperties properties) throws CoreException {
    ITypeHierarchy hierarchy = properties.getSuperTypeHierarchy();
    if (hierarchy == null && !properties.contains(DtoUpdateProperties.PROP_SUPER_TYPE_HIERARCHY)) {
      hierarchy = TypeUtility.getSuperTypeHierarchy(properties.getType());
      properties.put(DtoUpdateProperties.PROP_SUPER_TYPE_HIERARCHY, hierarchy);
    }
    return hierarchy;
  }
}
