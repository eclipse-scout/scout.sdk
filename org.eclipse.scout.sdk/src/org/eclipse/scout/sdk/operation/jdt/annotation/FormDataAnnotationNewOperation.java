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
package org.eclipse.scout.sdk.operation.jdt.annotation;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;

/**
 *
 */
public class FormDataAnnotationNewOperation extends AnnotationNewOperation {

  public FormDataAnnotationNewOperation(String formDataSignature, FormData.SdkCommand sdkCommand, FormData.DefaultSubtypeSdkCommand defaultSubtypeCommand, IType declaringType) {
    super(AnnotationSourceBuilderFactory.createFormDataAnnotation(formDataSignature, sdkCommand, defaultSubtypeCommand), declaringType);
  }

}