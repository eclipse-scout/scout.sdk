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
package org.eclipse.scout.sdk.sourcebuilder.field;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;

/**
 * <h3>{@link FieldSourceBuilderFactory}</h3> ...
 * 
 * @author aho
 * @since 3.10.0 07.03.2013
 */
public final class FieldSourceBuilderFactory {
  private FieldSourceBuilderFactory() {
  }

  public static IFieldSourceBuilder createSerialVersionUidBuilder() {
    FieldSourceBuilder serialVersionUidFieldBuilder = new FieldSourceBuilder("serialVersionUID");
    serialVersionUidFieldBuilder.setFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    serialVersionUidFieldBuilder.setSignature(Signature.SIG_LONG);
    serialVersionUidFieldBuilder.setValue("1L");
    return serialVersionUidFieldBuilder;
  }

  /**
   * @param fieldName
   * @param signature
   * @param flags
   * @param value
   *          without an ending semicolon e.g. <code>"1L"</code>
   * @return
   */
  public static IFieldSourceBuilder createFieldSourceBuilder(String fieldName, String signature, int flags, String value) {
    FieldSourceBuilder serialVersionUidFieldBuilder = new FieldSourceBuilder(fieldName);
    serialVersionUidFieldBuilder.setFlags(flags);
    serialVersionUidFieldBuilder.setSignature(signature);
    serialVersionUidFieldBuilder.setValue(value);
    return serialVersionUidFieldBuilder;
  }

}
