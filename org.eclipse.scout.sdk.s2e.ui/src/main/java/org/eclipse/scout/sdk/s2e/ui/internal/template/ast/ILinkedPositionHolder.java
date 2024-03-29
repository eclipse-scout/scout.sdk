/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 * <h3>{@link ILinkedPositionHolder}</h3>
 *
 * @since 5.2.0
 */
public interface ILinkedPositionHolder {
  /**
   * Adds a linked position to be shown when the proposal is applied. All positions with the same group id are linked.
   *
   * @param position
   *          The position to add.
   * @param isFirst
   *          If set, the proposal is jumped to first.
   * @param groupId
   *          The id of the group the proposal belongs to. All proposals in the same group are linked.
   */
  void addLinkedPosition(ITrackedNodePosition position, boolean isFirst, String groupId);

  /**
   * Adds a linked position proposal to the group with the given id.
   *
   * @param groupId
   *          The id of the group that should present the proposal
   * @param proposal
   *          The string to propose.
   */
  void addLinkedPositionProposal(String groupId, String proposal);

  /**
   * Adds linked position proposals showing boolean proposals ({@code true}, {@code false}).
   *
   * @param groupId
   *          The id of the group that should present the proposal
   */
  void addLinkedPositionProposalsBoolean(String groupId);

  /**
   * Adds a linked position proposal to the group with the given id.
   *
   * @param groupId
   *          The id of the group that should present the proposal
   * @param type
   *          The binding to use as type name proposal.
   */
  void addLinkedPositionProposal(String groupId, ITypeBinding type);

  /**
   * Adds a linked position proposal showing all abstract classes below the given hierarchy base class.
   *
   * @param groupId
   *          The id of the group that should present the proposal
   * @param hierarchyBaseTypeFqn
   *          The fully qualified name of the base type whose abstract sub classes should be shown.
   */
  void addLinkedPositionProposalsHierarchy(String groupId, String hierarchyBaseTypeFqn);
}
