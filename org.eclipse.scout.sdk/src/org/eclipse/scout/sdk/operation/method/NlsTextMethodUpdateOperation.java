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
package org.eclipse.scout.sdk.operation.method;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;

/**
 * <h3>NlsTextMethodOverrideOperation</h3> If the method is not implemented on the method owner, the method will be
 * overridden. Otherwise the content of the method will be updated to the new NLS translation text.
 */
public class NlsTextMethodUpdateOperation extends ConfigPropertyMethodUpdateOperation {

  public static final String GET_CONFIGURED_LABEL = "getConfiguredLabel";
  public static final String GET_CONFIGURED_TEXT = "getConfiguredText";
  public static final String GET_CONFIGURED_TITLE = "getConfiguredTitle";
  public static final String GET_CONFIGURED_HEADER_TEXT = "getConfiguredHeaderText";

  private INlsEntry m_nlsEntry;

  public NlsTextMethodUpdateOperation(IType declaringType, String methodName) {
    this(declaringType, methodName, false);
  }

  /**
   * @param declaringType
   *          the owner type of the method will be updated or created.
   * @param methodName
   *          the method name to override
   * @param sourceFormat
   *          true to ensure the owner type of the method will be formated after the method is created/updated.
   */
  public NlsTextMethodUpdateOperation(IType declaringType, String methodName, boolean sourceFormat) {
    super(declaringType, methodName, null, sourceFormat);
  }

  @Override
  protected String createMethodBody(IMethod methodToOverride, IImportValidator validator) throws JavaModelException {
    StringBuilder source = new StringBuilder();
    source.append("  return ");
    if (getNlsEntry() != null) {
      String nlsTypeSig = Signature.createTypeSignature(getNlsEntry().getProject().getNlsAccessorType().getFullyQualifiedName(), true);
      source.append(ScoutSdkUtility.getSimpleTypeRefName(nlsTypeSig, validator));
      source.append(".get(\"" + getNlsEntry().getKey() + "\");");
    }
    else {
      source.append("null;");
    }
    return source.toString();
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }
}
