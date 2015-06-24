/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.model;

import java.util.ArrayList;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.AnnotationEnums.SdkCommand;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 *
 */
public final class ScoutAnnotationSourceBuilderFactory {

  public static IAnnotationSourceBuilder createOrderAnnotation(double orderNr) {
    AnnotationSourceBuilder orderAnnoation = new AnnotationSourceBuilder(Signature.createTypeSignature(IRuntimeClasses.Order));
    orderAnnoation.addParameter(Double.toString(orderNr));
    return orderAnnoation;
  }

  /**
   * Creates a new {@link ClassId} annotation source builder
   *
   * @param classIdValue
   *          the class id value to use
   * @return the created source builder
   */
  public static IAnnotationSourceBuilder createClassIdAnnotation(String classIdValue) {
    AnnotationSourceBuilder classIdAnnoation = new AnnotationSourceBuilder(Signature.createTypeSignature(IRuntimeClasses.ClassId));
    classIdAnnoation.addParameter(CoreUtils.toStringLiteral(classIdValue));
    return classIdAnnoation;
  }

  public static IAnnotationSourceBuilder createFormDataAnnotation() {
    return createFormDataAnnotation(null, null, null);
  }

  public static IAnnotationSourceBuilder createPageDataAnnotation(final String pageDataTypeSignature) {
    return new AnnotationSourceBuilder(Signature.createTypeSignature(IRuntimeClasses.PageData)) {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append('@').append(SignatureUtils.getTypeReference(getSignature(), validator)).append('(');
        source.append(SignatureUtils.getTypeReference(pageDataTypeSignature, validator));
        source.append(".class)");
      }
    };
  }

  public static IAnnotationSourceBuilder createFormDataAnnotation(final String formDataSignature, final SdkCommand sdkCommand, final DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    return new AnnotationSourceBuilder(Signature.createTypeSignature(IRuntimeClasses.FormData)) {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String formDataTypeRef = SignatureUtils.getTypeReference(getSignature(), validator);
        source.append("@").append(formDataTypeRef);
        ArrayList<String> args = new ArrayList<>(3);
        if (formDataSignature != null) {
          args.add("value = " + SignatureUtils.getTypeReference(formDataSignature, validator) + ".class");
        }
        if (sdkCommand != null) {
          StringBuilder b = new StringBuilder();
          b.append("sdkCommand = ");
          b.append(formDataTypeRef).append(".");
          b.append(SignatureUtils.getTypeReference(Signature.createTypeSignature(sdkCommand.getDeclaringClass().getName()), validator));
          b.append(".").append(sdkCommand.name());
          args.add(b.toString());
        }
        if (defaultSubtypeCommand != null) {
          StringBuilder b = new StringBuilder();
          b.append("defaultSubtypeSdkCommand = ");
          b.append(formDataTypeRef).append(".");
          b.append(SignatureUtils.getTypeReference(Signature.createTypeSignature(defaultSubtypeCommand.getDeclaringClass().getName()), validator));
          b.append(".").append(defaultSubtypeCommand.name());
          args.add(b.toString());
        }
        if (args.size() > 0) {
          source.append("(");
          for (int i = 0; i < args.size(); i++) {
            source.append(args.get(i));
            if (i < args.size() - 1) {
              source.append(", ");
            }
          }
          source.append(")");
        }
      }
    };
  }

  public static IAnnotationSourceBuilder createReplaceAnnotationBuilder() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(Signature.createTypeSignature(IRuntimeClasses.Replace));
    return sourceBuilder;
  }
}