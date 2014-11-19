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
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.DtoUpdateProperties;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateHandler;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.DataAnnotation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractDtoUpdateHandler}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 16.08.2013
 */
public abstract class AbstractDtoUpdateHandler implements IDtoAutoUpdateHandler {

  protected FormDataAnnotation ensurePropertyFormDataAnnotation(DtoUpdateProperties properties) throws CoreException {
    if (!properties.contains(DtoUpdateProperties.PROP_FORM_DATA_ANNOTATION)) {
      FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(properties.getType(), ensurePropertySuperTypeHierarchy(properties));
      properties.setFormDataAnnotation(annotation);
      return annotation;
    }
    return properties.getFormDataAnnotation();
  }

  protected DataAnnotation ensurePropertyDataAnnotation(DtoUpdateProperties properties) throws CoreException {
    if (!properties.contains(DtoUpdateProperties.PROP_DATA_ANNOTATION)) {
      DataAnnotation annotation = ScoutTypeUtility.findDataAnnotation(properties.getType(), ensurePropertySuperTypeHierarchy(properties));
      properties.setDataAnnotation(annotation);
      return annotation;
    }
    return properties.getDataAnnotation();
  }

  protected ITypeHierarchy ensurePropertySuperTypeHierarchy(DtoUpdateProperties properties) throws CoreException {
    if (!properties.contains(DtoUpdateProperties.PROP_SUPER_TYPE_HIERARCHY)) {
      ITypeHierarchy hierarchy = TypeUtility.getSupertypeHierarchy(properties.getType());
      properties.setSuperTypeHierarchy(hierarchy);
      return hierarchy;
    }
    return properties.getSuperTypeHierarchy();
  }
}
