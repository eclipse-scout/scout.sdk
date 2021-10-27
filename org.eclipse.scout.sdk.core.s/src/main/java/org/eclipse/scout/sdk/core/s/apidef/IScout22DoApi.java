/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;

@SuppressWarnings({"squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // method naming conventions
public interface IScout22DoApi extends IApiSpecification {

  DoSet DoSet();

  interface DoSet extends IClassNameSupplier {
  }

  DoCollection DoCollection();

  interface DoCollection extends IClassNameSupplier {
  }

  DoEntity DoEntity();

  interface DoEntity extends IClassNameSupplier {
    String nvlMethodName();
  }
}