/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.apidef;

import org.eclipse.scout.sdk.core.apidef.IApiSpecification;

public interface IScoutApi extends IApiSpecification, IScoutAnnotationApi, IScoutInterfaceApi, IScoutAbstractApi, IScoutExtensionApi, IScoutVariousApi {

  boolean REGISTERED = ScoutApi.register();

  default ScoutModelHierarchy hierarchy() {
    return new ScoutModelHierarchy(this);
  }

  /**
   * @return The supported Java major version (e.g. 8 or 11)
   */
  int[] supportedJavaVersions();
}
