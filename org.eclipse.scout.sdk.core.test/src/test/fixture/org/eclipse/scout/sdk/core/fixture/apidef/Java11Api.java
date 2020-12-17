/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture.apidef;

import org.eclipse.scout.sdk.core.apidef.ApiLevel;

@ApiLevel(11)
public interface Java11Api extends IJavaApi, ICustomApi {
  String VALUE = "11";
  int INT = 11;

  @Override
  default String method() {
    return VALUE;
  }

  @Override
  default int customMethod() {
    return INT;
  }
}
