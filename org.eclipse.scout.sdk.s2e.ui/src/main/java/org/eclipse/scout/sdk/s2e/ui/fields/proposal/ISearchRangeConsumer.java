/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.scout.sdk.s2e.ui.util.NormalizedPattern;

/**
 * <h3>{@link ISearchRangeConsumer}</h3> Feature interface for {@link IBaseLabelProvider}s. Label providers implementing
 * this interface allow the {@link ProposalTextField} to mark the parts that match a given search criterion.<br>
 * For this {@link ISearchRangeConsumer} can collect all match ranges for each proposal.
 *
 * @since 3.8.0 2012-02-17
 * @see SearchPattern#getMatchingRegions(String, String, int)
 * @see NormalizedPattern#getMatchingRegions(String)
 */
public interface ISearchRangeConsumer {

  /**
   * Gets the match ranges for the given proposal element.
   *
   * @param element
   *          The element
   * @return The match ranges or {@code null}.
   */
  int[] getMatchRanges(Object element);

  /**
   * Starts recording of matching regions.
   */
  void startRecordMatchRegions();

  /**
   * Adds matching regions for the given proposal element.
   *
   * @param element
   *          the proposal
   * @param matchRegions
   *          the new regions for this element.
   */
  void addMatchRegions(Object element, int[] matchRegions);

  /**
   * Stop recording of matching regions
   */
  void endRecordMatchRegions();
}
