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
package org.eclipse.scout.sdk.internal.workspace.dto.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractDtoUpdateHandler;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.DtoUpdateProperties;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.DataAnnotation;

/**
 * <h3>{@link FormDataDtoUpdateHandler}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 16.08.2013
 */
public class FormDataDtoUpdateHandler extends AbstractDtoUpdateHandler {

  private boolean checkType(DtoUpdateProperties properties) throws CoreException {
    FormDataAnnotation formDataAnnotation = null;

    ITypeHierarchy superTypeHierarchy = ensurePropertySuperTypeHierarchy(properties);
    if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IForm)) || superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormField))) {
      formDataAnnotation = ensurePropertyFormDataAnnotation(properties);
    }
    else if (superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormExtension)) || superTypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IFormFieldExtension))) {
      DataAnnotation dataAnnotation = ensurePropertyDataAnnotation(properties);
      if (dataAnnotation != null) {
        formDataAnnotation = new FormDataAnnotation();
        formDataAnnotation.setAnnotationOwner(properties.getType());
        formDataAnnotation.setDefaultSubtypeSdkCommand(DefaultSubtypeSdkCommand.CREATE);
        formDataAnnotation.setFormDataTypeSignature(dataAnnotation.getDataTypeSignature());
        formDataAnnotation.setGenericOrdinal(-1);
        formDataAnnotation.setSdkCommand(SdkCommand.CREATE);
        String superDataTypeSignature = dataAnnotation.getSuperDataTypeSignature();
        if (superDataTypeSignature != null) {
          formDataAnnotation.setSuperTypeSignature(superDataTypeSignature);
        }
        else {
          formDataAnnotation.setSuperTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractFormFieldData));
        }
        properties.setFormDataAnnotation(formDataAnnotation);
      }
    }
    return FormDataAnnotation.isCreate(formDataAnnotation);
  }

  @Override
  public IDtoAutoUpdateOperation createUpdateOperation(DtoUpdateProperties properties) throws CoreException {
    if (checkType(properties)) {
      return new FormDataDtoUpdateOperation(properties.getType(), ensurePropertyFormDataAnnotation(properties));
    }
    return null;
  }
}
