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
package org.eclipse.scout.sdk.core.s.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.OptApiFunction;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.visitor.DefaultDepthFirstVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeTraversals;

public final class TierTree {

  private static final TierTree INSTANCE = create();

  private TierNode m_root;
  private final Map<ITier<?>, TierNode> m_tiers = new HashMap<>();

  static {
    ScoutTier.initTierTree();
  }

  private TierTree() {
  }

  static TierTree create() {
    return new TierTree();
  }

  public static boolean addDependency(ITier<?> tier, ITier<?> dependency) {
    return INSTANCE.addDependencyImpl(tier, dependency);
  }

  synchronized boolean addDependencyImpl(ITier<?> tier, ITier<?> dependency) {
    Ensure.notNull(tier, "tier is null");
    Ensure.notNull(dependency, "dependency is null");

    if (m_tiers.containsKey(tier) && m_tiers.containsKey(dependency)) {
      return false;
    }
    if (!m_tiers.isEmpty() && !m_tiers.containsKey(tier) && !m_tiers.containsKey(dependency)) {
      return false;
    }

    var tierNode = m_tiers.computeIfAbsent(tier, TierNode::of);
    var dependencyNode = m_tiers.computeIfAbsent(dependency, TierNode::of);

    if (m_root == null || m_root.isTier(tier)) {
      m_root = dependencyNode;
    }

    return dependencyNode.addChildren(tierNode);
  }

  synchronized boolean hasDependencyImpl(ITier<?> tier, ITier<?> dependency) {
    var tierNode = m_tiers.get(tier);
    if (tierNode == null) {
      return false;
    }

    var currentNode = tierNode.getParent();

    while (currentNode != null) {
      if (currentNode.isTier(dependency)) {
        return true;
      }
      currentNode = currentNode.getParent();
    }

    return false;
  }

  static boolean isAvailable(ITier<?> tier, ITier<?> other) {
    return INSTANCE.isAvailableImpl(tier, other);
  }

  synchronized boolean isAvailableImpl(ITier<?> tier, ITier<?> other) {
    if (!m_tiers.containsKey(tier)) {
      return false;
    }

    return tier.equals(other) || hasDependencyImpl(tier, other);
  }

  public static List<ITier<?>> topDownPath(ITier<?> top, ITier<?> bottom) {
    return INSTANCE.topDownPathImpl(top, bottom);
  }

  synchronized List<ITier<?>> topDownPathImpl(ITier<?> top, ITier<?> bottom) {
    var toNode = m_tiers.get(bottom);
    if (toNode == null) {
      return emptyList();
    }

    List<ITier<?>> path = new ArrayList<>();
    var currentNode = toNode;
    path.add(currentNode.getTier());

    do {
      currentNode = currentNode.getParent();
      if (currentNode != null) {
        path.add(0, currentNode.getTier());
      }
    }
    while (currentNode != null && !currentNode.isTier(top));

    if (currentNode == null && top != null) {
      // top was not found between bottom and root -> no path possible
      return emptyList();
    }

    return unmodifiableList(path);
  }

  /**
   * Gets the {@link ITier} the given {@link IJavaElement} belongs to.
   *
   * @param element
   *          The {@link IJavaElement} for which the {@link ITier} should be calculated.
   * @return The {@link ITier} the given element belongs to or {@code null} if it does not belong to a {@link ITier}.
   */
  static Optional<ITier<?>> tierOf(IJavaElement element) {
    return Optional.ofNullable(element)
        .map(IJavaElement::javaEnvironment)
        .flatMap(TierTree::tierOf);
  }

  static Optional<ITier<?>> tierOf(IJavaEnvironment env) {
    return Optional.ofNullable(env)
        .flatMap(e -> tierOf(e::exists, e::api));
  }

  static Optional<ITier<?>> tierOf(Predicate<String> typeLookupStrategy, OptApiFunction optApiFunction) {
    return INSTANCE.tierOfImpl(typeLookupStrategy, optApiFunction);
  }

  synchronized Optional<ITier<?>> tierOfImpl(Predicate<String> typeLookupStrategy, OptApiFunction optApiFunction) {
    var tier = new AtomicReference<ITier<?>>();
    TreeTraversals.create(new DefaultDepthFirstVisitor<>() {
      @Override
      public boolean postVisit(TierNode tierNode, int level, int index) {
        if (typeLookupStrategy.test(tierNode.getTier().getLookupFqn(optApiFunction))) {
          tier.set(tierNode.getTier());
          return false;
        }
        return true;
      }
    }, TierNode::children).traverse(m_root);

    return Optional.ofNullable(tier.get());
  }

  private static final class TierNode {
    private final ITier<?> m_tier;
    private TierNode m_parent;
    private final Collection<TierNode> m_children = new HashSet<>();

    private TierNode(ITier<?> tier) {
      m_tier = Ensure.notNull(tier, "tier is null");
    }

    private static TierNode of(ITier<?> tier) {
      return new TierNode(tier);
    }

    private ITier<?> getTier() {
      return m_tier;
    }

    private boolean isTier(ITier<?> tier) {
      return m_tier.equals(tier);
    }

    private void setParent(TierNode parent) {
      m_parent = parent;
    }

    private TierNode getParent() {
      return m_parent;
    }

    private Stream<TierNode> children() {
      return m_children.stream();
    }

    private boolean addChildren(TierNode tierNode) {
      tierNode.setParent(this);
      return m_children.add(tierNode);
    }
  }
}
