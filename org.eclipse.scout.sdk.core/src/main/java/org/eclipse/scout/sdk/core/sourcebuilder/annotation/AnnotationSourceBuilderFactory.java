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
package org.eclipse.scout.sdk.core.sourcebuilder.annotation;

import javax.annotation.Generated;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link AnnotationSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public final class AnnotationSourceBuilderFactory {

  private AnnotationSourceBuilderFactory() {
  }

  public static IAnnotationSourceBuilder createOverrideAnnotationSourceBuilder() {
    return new AnnotationSourceBuilder(Signature.createTypeSignature(Override.class.getName()));
  }

  public static IAnnotationSourceBuilder createSupressWarningAnnotation(String parameter) {
    AnnotationSourceBuilder orderAnnoation = new AnnotationSourceBuilder(Signature.createTypeSignature(SuppressWarnings.class.getName()));
    orderAnnoation.addParameter(parameter);
    return orderAnnoation;
  }

  public static IAnnotationSourceBuilder createGeneratedAnnotation(String classThatGeneratedTheCode) {
    return createGeneratedAnnotation(classThatGeneratedTheCode, null);
  }

  public static IAnnotationSourceBuilder createGeneratedAnnotation(final String classThatGeneratedTheCode, final String comments) {
    return new AnnotationSourceBuilder(Signature.createTypeSignature(Generated.class.getName())) {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append('@').append(SignatureUtils.getTypeReference(getSignature(), validator)).append('(');
        source.append("value = \"").append(classThatGeneratedTheCode).append("\"");
        if (StringUtils.isNotBlank(comments)) {
          source.append(", comments = \"").append(comments).append("\"");
        }
        source.append(")");
      }
    };
  }

}
