/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.enums;

import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.s.model.js.AbstractScoutJsElementQuery;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

public class ScoutJsEnumQuery extends AbstractScoutJsElementQuery<IScoutJsEnum, ScoutJsEnumQuery> {

  private IDataType m_fulfillDataType;

  public ScoutJsEnumQuery(ScoutJsModel model) {
    super(model);
  }

  public ScoutJsEnumQuery withFulfillsDataType(IDataType dataType) {
    m_fulfillDataType = dataType;
    return this;
  }

  protected IDataType fulfillDataType() {
    return m_fulfillDataType;
  }

  @Override
  protected ScoutJsEnumSpliterator createSpliterator() {
    return new ScoutJsEnumSpliterator(model(), isIncludeSelf(), isIncludeDependencies());
  }

  @Override
  protected Predicate<IScoutJsEnum> createFilter() {
    var result = super.createFilter();

    var fulfillDataType = fulfillDataType();
    if (fulfillDataType != null) {
      Predicate<IScoutJsEnum> fulfillDataTypeFilter = e -> e.fulfills(fulfillDataType);
      if (result == null) {
        result = fulfillDataTypeFilter;
      }
      else {
        result = result.and(fulfillDataTypeFilter);
      }
    }

    return result;
  }
}
