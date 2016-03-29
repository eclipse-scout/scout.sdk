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
package org.eclipse.scout.sdk.core.sourcebuilder.field;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;

/**
 * <h3>{@link FieldSourceBuilderFactory}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-03-07
 */
public final class FieldSourceBuilderFactory {
  private FieldSourceBuilderFactory() {
  }

  public static IFieldSourceBuilder createSerialVersionUidBuilder() {
    return createFieldSourceBuilder("serialVersionUID", ISignatureConstants.SIG_LONG, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal, "1L");
  }

  /**
   * @param fieldName
   * @param signature
   * @param flags
   * @param value
   *          without an ending semicolon e.g. <code>"1L"</code>
   * @return
   */
  public static IFieldSourceBuilder createFieldSourceBuilder(String fieldName, String signature, int flags, String initializerJavaSource) {
    FieldSourceBuilder fieldSourceBuilder = new FieldSourceBuilder(fieldName);
    fieldSourceBuilder.setFlags(flags);
    fieldSourceBuilder.setSignature(signature);
    if (initializerJavaSource != null) {
      fieldSourceBuilder.setValue(new RawSourceBuilder(initializerJavaSource));
    }
    return fieldSourceBuilder;
  }
}
