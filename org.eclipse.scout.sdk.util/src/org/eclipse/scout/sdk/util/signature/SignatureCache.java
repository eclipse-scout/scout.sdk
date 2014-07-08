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
package org.eclipse.scout.sdk.util.signature;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.Signature;

/**
 * <h3>{@link SignatureCache}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 22.11.2012
 */
public final class SignatureCache {

  private static final Map<String, String> SIG_CACHE = new HashMap<String, String>(256);

  private SignatureCache() {
  }

  public static String createTypeSignature(String fqn) { // no need to make synchronized
    String existing = SIG_CACHE.get(fqn);
    if (existing == null) {
      existing = Signature.createTypeSignature(fqn, true);
      SIG_CACHE.put(fqn, existing);
    }
    return existing;
  }
}
