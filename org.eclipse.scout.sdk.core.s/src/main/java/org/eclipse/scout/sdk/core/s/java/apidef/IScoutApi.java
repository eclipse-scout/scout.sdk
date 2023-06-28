/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;


import java.nio.charset.Charset;

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;

public interface IScoutApi extends IApiSpecification, IScoutAnnotationApi, IScoutInterfaceApi, IScoutAbstractApi, IScoutExtensionApi, IScoutVariousApi {

  boolean REGISTERED = ScoutApi.register();

  default ScoutModelHierarchy hierarchy() {
    return new ScoutModelHierarchy(this);
  }

  /**
   * @return The supported Java major version (e.g. 8 or 11)
   */
  int[] supportedJavaVersions();

  /**
   * @return The {@link Charset} to use for reading and writing .properties files.
   */
  Charset propertiesEncoding();
}
