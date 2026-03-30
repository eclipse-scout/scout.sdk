/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;

public interface IScoutSessionApi extends IApiSpecification {

  ISession ISession();

  interface ISession extends ITypeNameSupplier {
  }

  RunWithServerSession RunWithServerSession();

  interface RunWithServerSession extends ITypeNameSupplier {
    String valueElementName();
  }

  IServerSession IServerSession();

  interface IServerSession extends ITypeNameSupplier {
  }
}
