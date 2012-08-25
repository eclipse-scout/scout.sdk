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
package org.eclipse.scout.sdk.operation.annotation;

import java.util.ArrayList;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 *
 */
public class FormDataAnnotationCreateOperation extends AnnotationCreateOperation {
  private FormData.SdkCommand m_sdkCommand;

  private FormData.DefaultSubtypeSdkCommand m_defaultSubtypeCommand;
  private String m_formDataSignature;

  public FormDataAnnotationCreateOperation(IType type) {
    super(type, Signature.createTypeSignature(RuntimeClasses.FormData, true), true);
  }

  @Override
  public String createSource(IImportValidator validator, String NL) throws JavaModelException {
    StringBuilder source = new StringBuilder();
    source.append("@" + SignatureUtility.getTypeReference(getSignature(), validator));
    if (hasArguments()) {
      ArrayList<String> args = new ArrayList<String>();
      if (getFormDataSignature() != null) {
        args.add("value = " + SignatureUtility.getTypeReference(getFormDataSignature(), validator) + ".class");
      }
      if (getSdkCommand() != null) {
        StringBuilder b = new StringBuilder();
        b.append("sdkCommand = " + SignatureUtility.getTypeReference(Signature.createTypeSignature(getSdkCommand().getDeclaringClass().getName(), true), validator));
        b.append("." + getSdkCommand().name());
        args.add(b.toString());
      }
      if (getDefaultSubtypeCommand() != null) {
        StringBuilder b = new StringBuilder();
        b.append("defaultSubtypeSdkCommand = " + SignatureUtility.getTypeReference(Signature.createTypeSignature(getDefaultSubtypeCommand().getDeclaringClass().getName(), true), validator));
        b.append("." + getDefaultSubtypeCommand().name());
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
    return source.toString();
  }

  public boolean hasArguments() {
    return getFormDataSignature() != null || getSdkCommand() != null || getDefaultSubtypeCommand() != null;
  }

  public FormData.SdkCommand getSdkCommand() {
    return m_sdkCommand;
  }

  public void setSdkCommand(FormData.SdkCommand sdkCommand) {
    m_sdkCommand = sdkCommand;
  }

  public FormData.DefaultSubtypeSdkCommand getDefaultSubtypeCommand() {
    return m_defaultSubtypeCommand;
  }

  public void setDefaultSubtypeCommand(FormData.DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    m_defaultSubtypeCommand = defaultSubtypeCommand;
  }

  public String getFormDataSignature() {
    return m_formDataSignature;
  }

  public void setFormDataSignature(String formDataSignature) {
    m_formDataSignature = formDataSignature;
  }

}
