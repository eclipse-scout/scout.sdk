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
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsElementSpliterator;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * Query to retrieve {@link IScoutJsEnum}s of a {@link ScoutJsModel}.<br>
 * By default, this query returns all {@link IScoutJsEnum}s directly declared in the {@link ScoutJsModel}.
 */
public class ScoutJsEnumQuery extends AbstractScoutJsElementQuery<IScoutJsEnum, ScoutJsEnumQuery> {

  private IDataType m_fulfillDataType;

  public ScoutJsEnumQuery(ScoutJsModel model) {
    super(model);
  }

  /**
   * Limits the {@link IScoutJsEnum enums} returned to the ones that could be assigned to the given {@link IDataType}
   * (see {@link IScoutJsEnum#fulfills(IDataType)}).
   * 
   * @param dataType
   *          The {@link IDataType} to which it should be compatible or {@code null} for no filtering by
   *          {@link IDataType}.
   * @return This query.
   */
  public ScoutJsEnumQuery withFulfillsDataType(IDataType dataType) {
    m_fulfillDataType = dataType;
    return this;
  }

  protected IDataType fulfillDataType() {
    return m_fulfillDataType;
  }

  @Override
  protected ScoutJsElementSpliterator<IScoutJsEnum> createSpliterator() {
    return new ScoutJsElementSpliterator<>(model(), isIncludeSelf(), isIncludeDependencies(), ScoutJsModel::scoutEnums);
  }

  @Override
  protected Predicate<IScoutJsEnum> createFilter() {
    var result = super.createFilter();

    var fulfillDataType = fulfillDataType();
    if (fulfillDataType != null) {
      result = AbstractScoutJsElementQuery.appendOrCreateFilter(result, e -> e.fulfills(fulfillDataType));
    }
    return result;
  }
}
