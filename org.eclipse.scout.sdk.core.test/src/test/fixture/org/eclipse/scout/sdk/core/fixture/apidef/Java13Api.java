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

@ApiLevel(13)
public interface Java13Api extends IJavaApi, ICustomApi {
  int INT = 11;

  @Override
  default int customMethod() {
    return INT;
  }
}
