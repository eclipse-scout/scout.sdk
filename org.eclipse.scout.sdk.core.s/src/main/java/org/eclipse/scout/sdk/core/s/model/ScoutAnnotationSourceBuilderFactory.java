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
import java.util.List;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 *
 */
public final class ScoutAnnotationSourceBuilderFactory {

  private ScoutAnnotationSourceBuilderFactory() {
  }

  public static IAnnotationSourceBuilder createOrderAnnotation(double orderNr) {
    AnnotationSourceBuilder orderAnnoation = new AnnotationSourceBuilder(IScoutRuntimeTypes.Order);
    orderAnnoation.putElement("value", Double.toString(orderNr));
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
    AnnotationSourceBuilder classIdAnnoation = new AnnotationSourceBuilder(IScoutRuntimeTypes.ClassId);
    classIdAnnoation.putElement("value", CoreUtils.toStringLiteral(classIdValue));
    return classIdAnnoation;
  }

  public static IAnnotationSourceBuilder createFormDataAnnotation() {
    return createFormDataAnnotation(null, null, null);
  }

  public static IAnnotationSourceBuilder createDataAnnotation(final String pageDataTypeSignature) {
    return new AnnotationSourceBuilder(IScoutRuntimeTypes.Data) {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append('@').append(validator.useName(getName())).append('(');
        source.append(validator.useSignature(pageDataTypeSignature));
        source.append(".class)");
      }
    };
  }

  public static IAnnotationSourceBuilder createFormDataAnnotation(final String formDataSignature, final SdkCommand sdkCommand, final DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    return new AnnotationSourceBuilder(IScoutRuntimeTypes.FormData) {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        String formDataTypeRef = validator.useName(getName());
        source.append("@").append(formDataTypeRef);
        List<String> args = new ArrayList<>(3);
        if (formDataSignature != null) {
          args.add("value = " + validator.useSignature(formDataSignature) + ".class");
        }
        if (sdkCommand != null) {
          StringBuilder b = new StringBuilder();
          b.append("sdkCommand = ");
          b.append(formDataTypeRef).append('.');
          b.append(validator.useName(sdkCommand.getDeclaringClass().getName()));
          b.append(".").append(sdkCommand.name());
          args.add(b.toString());
        }
        if (defaultSubtypeCommand != null) {
          StringBuilder b = new StringBuilder();
          b.append("defaultSubtypeSdkCommand = ");
          b.append(formDataTypeRef).append('.');
          b.append(validator.useName(defaultSubtypeCommand.getDeclaringClass().getName()));
          b.append('.').append(defaultSubtypeCommand.name());
          args.add(b.toString());
        }
        if (args.size() > 0) {
          source.append('(');
          for (int i = 0; i < args.size(); i++) {
            source.append(args.get(i));
            if (i < args.size() - 1) {
              source.append(", ");
            }
          }
          source.append(')');
        }
      }
    };
  }

  public static IAnnotationSourceBuilder createReplaceAnnotation() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.Replace);
    return sourceBuilder;
  }
}
