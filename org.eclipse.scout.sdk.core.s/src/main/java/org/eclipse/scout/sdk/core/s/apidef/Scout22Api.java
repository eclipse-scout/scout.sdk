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

import org.eclipse.scout.sdk.core.apidef.ApiLevel;

@ApiLevel(22)
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout22Api extends IScoutApi, IScoutChartApi, IScoutDoCollectionApi {

  IScoutDoCollectionApi.DoSet DO_SET = new DoSet();

  @Override
  default IScoutDoCollectionApi.DoSet DoSet() {
    return DO_SET;
  }

  class DoSet implements IScoutDoCollectionApi.DoSet {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoSet";
    }
  }

  IScoutDoCollectionApi.DoCollection DO_COLLECTION = new DoCollection();

  @Override
  default IScoutDoCollectionApi.DoCollection DoCollection() {
    return DO_COLLECTION;
  }

  class DoCollection implements IScoutDoCollectionApi.DoCollection {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoCollection";
    }
  }
}
