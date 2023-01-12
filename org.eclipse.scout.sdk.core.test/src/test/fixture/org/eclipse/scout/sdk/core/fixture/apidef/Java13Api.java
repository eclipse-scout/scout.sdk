/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.fixture.apidef;

import org.eclipse.scout.sdk.core.apidef.MaxApiLevel;

@MaxApiLevel(13)
public interface Java13Api extends IJavaApi, ICustomApi {
  int INT = 13;

  @Override
  default int customMethod() {
    return INT;
  }
}
