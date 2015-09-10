/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.fixture;

public class ClassWithTypeVariables<IN, OUT extends CharSequence> {
  protected OUT m_value;

  public OUT transform(IN raw) {
    return m_value;
  }
}
