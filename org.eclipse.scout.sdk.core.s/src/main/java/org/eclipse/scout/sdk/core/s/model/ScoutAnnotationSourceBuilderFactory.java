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

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
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

  public static IAnnotationSourceBuilder createOrder(double orderNr) {
    AnnotationSourceBuilder orderAnnoation = new AnnotationSourceBuilder(IScoutRuntimeTypes.Order);
    String orderStr = Double.toString(orderNr);
    final String zeroSuffix = ".0";
    if (orderStr.endsWith(zeroSuffix)) {
      orderStr = orderStr.substring(0, orderStr.length() - zeroSuffix.length());
    }
    orderAnnoation.putElement("value", orderStr);
    return orderAnnoation;
  }

  /**
   * Creates a new {@link ClassId} annotation source builder
   *
   * @param classIdValue
   *          the class id value to use
   * @return the created source builder
   */
  public static IAnnotationSourceBuilder createClassId(String classIdValue) {
    AnnotationSourceBuilder classIdAnnoation = new AnnotationSourceBuilder(IScoutRuntimeTypes.ClassId);
    classIdAnnoation.putElement("value", CoreUtils.toStringLiteral(classIdValue));
    return classIdAnnoation;
  }

  public static IAnnotationSourceBuilder createBefore() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.Before);
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createApplicationScoped() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.ApplicationScoped);
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createBeanMock() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.BeanMock);
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createTunnelToServer() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.TunnelToServer);
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createTest() {
    return new AnnotationSourceBuilder(IScoutRuntimeTypes.Test);
  }

  /**
   * Creates a new {@link IAnnotationSourceBuilder} creating an @RunWithSubject annotation.
   *
   * @param valueBuilder
   *          The {@link ISourceBuilder} creating the value part of the annotation. Can be <code>null</code> in which an
   *          anonymous subject is used.
   * @return
   */
  public static IAnnotationSourceBuilder createRunWithSubject(ISourceBuilder valueBuilder) {
    if (valueBuilder == null) {
      valueBuilder = new RawSourceBuilder(CoreUtils.toStringLiteral("anonymous"));
    }
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.RunWithSubject);
    sourceBuilder.putElement("value", valueBuilder);
    return sourceBuilder;
  }

  /**
   * Creates a new {@link IAnnotationSourceBuilder} creating a @RunWithClientSession annotation using the given client
   * session signature.
   *
   * @param clientSessionSignature
   *          The client session signature to use or <code>null</code> if the default client session should be used.
   * @return The created {@link IAnnotationSourceBuilder}.
   */
  public static IAnnotationSourceBuilder createRunWithClientSession(final String clientSessionSignature) {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.RunWithClientSession);
    sourceBuilder.putElement("value", new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        if (clientSessionSignature == null) {
          source.append(validator.useName(IScoutRuntimeTypes.TestEnvironmentClientSession));
        }
        else {
          source.append(validator.useSignature(clientSessionSignature));
        }
        source.append(SuffixConstants.SUFFIX_class);
      }
    });
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createRunWithServerSession(final String serverSessionSignature) {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.RunWithServerSession);
    sourceBuilder.putElement("value", new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(validator.useSignature(serverSessionSignature)).append(SuffixConstants.SUFFIX_class);
      }
    });
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createRunWith(final String runnerSignature) {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.RunWith);
    sourceBuilder.putElement("value", new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(validator.useSignature(runnerSignature)).append(SuffixConstants.SUFFIX_class);
      }
    });
    return sourceBuilder;
  }

  public static IAnnotationSourceBuilder createData(final String pageDataTypeSignature) {
    IAnnotationSourceBuilder asb = new AnnotationSourceBuilder(IScoutRuntimeTypes.Data);
    asb.putElement(DataAnnotation.VALUE_ELEMENT_NAME, new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append(validator.useSignature(pageDataTypeSignature)).append(SuffixConstants.SUFFIX_class);
      }
    });
    return asb;
  }

  public static IAnnotationSourceBuilder createFormData() {
    return createFormData(null, null, null);
  }

  public static IAnnotationSourceBuilder createFormData(final String formDataSignature, final SdkCommand sdkCommand, final DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    final IAnnotationSourceBuilder formDataAnnot = new AnnotationSourceBuilder(IScoutRuntimeTypes.FormData);
    if (formDataSignature != null && !ISignatureConstants.SIG_JAVA_LANG_OBJECT.equals(formDataSignature)) {
      formDataAnnot.putElement(FormDataAnnotation.VALUE_ELEMENT_NAME, new ISourceBuilder() {
        @Override
        public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
          source.append(validator.useSignature(formDataSignature)).append(SuffixConstants.SUFFIX_class);
        }
      });
    }
    if (sdkCommand != null && !SdkCommand.DEFAULT.equals(sdkCommand)) {
      formDataAnnot.putElement(FormDataAnnotation.SDK_COMMAND_ELEMENT_NAME, new ISourceBuilder() {
        @Override
        public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
          String formDataTypeRef = validator.useName(formDataAnnot.getName());
          source.append(formDataTypeRef);
          source.append(".SdkCommand.");
          source.append(sdkCommand.name());
        }
      });
    }
    if (defaultSubtypeCommand != null && !DefaultSubtypeSdkCommand.DEFAULT.equals(defaultSubtypeCommand)) {
      formDataAnnot.putElement(FormDataAnnotation.DEFAULT_SUBTYPE_SDK_COMMAND_ELEMENT_NAME, new ISourceBuilder() {
        @Override
        public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
          String formDataTypeRef = validator.useName(formDataAnnot.getName());
          source.append(formDataTypeRef);
          source.append(".DefaultSubtypeSdkCommand.");
          source.append(defaultSubtypeCommand.name());
        }
      });
    }
    return formDataAnnot;
  }

  public static IAnnotationSourceBuilder createReplace() {
    AnnotationSourceBuilder sourceBuilder = new AnnotationSourceBuilder(IScoutRuntimeTypes.Replace);
    return sourceBuilder;
  }
}
