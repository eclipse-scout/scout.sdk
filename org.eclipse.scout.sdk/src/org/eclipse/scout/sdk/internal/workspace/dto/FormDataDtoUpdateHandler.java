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
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link FormDataDtoUpdateHandler}</h3>
 * 
 * @author aho
 * @since 3.10.0 16.08.2013
 */
public class FormDataDtoUpdateHandler extends AbstractDtoUpdateHandler {

  private boolean checkType(DtoUpdateProperties properties) throws CoreException {
    ITypeHierarchy superTypeHierarchy = ensurePropertySuperTypeHierarchy(properties);
    if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IForm)) || superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormField))) {
      FormDataAnnotation formDataAnnotation = ensurePropertyFormDataAnnotation(properties);
      return FormDataAnnotation.isCreate(formDataAnnotation);
    }
    return false;
  }

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(DtoUpdateProperties properties) throws CoreException {
    if (checkType(properties)) {
      return new FormDataDtoUpdateOperation(properties.getType(), ensurePropertyFormDataAnnotation(properties));
    }
    else {
      return null;
    }
  }

}
