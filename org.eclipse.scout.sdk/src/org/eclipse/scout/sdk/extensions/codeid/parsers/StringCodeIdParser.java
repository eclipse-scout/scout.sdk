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
package org.eclipse.scout.sdk.extensions.codeid.parsers;

import org.eclipse.scout.sdk.util.jdt.JdtUtility;

public class StringCodeIdParser implements ICodeIdParser {
  @Override
  public boolean isValid(String val) {
    return true;
  }

  @Override
  public String getSource(String val) {
    if (val == null) {
      return null;
    }
    return JdtUtility.toStringLiteral(val);
  }
}
