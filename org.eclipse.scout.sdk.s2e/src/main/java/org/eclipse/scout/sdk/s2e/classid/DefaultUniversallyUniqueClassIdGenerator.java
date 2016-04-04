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
package org.eclipse.scout.sdk.s2e.classid;

import java.util.UUID;

/**
 * <h3>{@link DefaultUniversallyUniqueClassIdGenerator}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 2014-01-02
 */
public class DefaultUniversallyUniqueClassIdGenerator implements IClassIdGenerator {
  @Override
  public String generate(ClassIdGenerationContext context) {
    return UUID.randomUUID().toString();
  }
}
