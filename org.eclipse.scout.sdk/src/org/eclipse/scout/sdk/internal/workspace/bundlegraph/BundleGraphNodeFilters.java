/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace.bundlegraph;

import java.util.Set;

/**
 *
 */
public final class BundleGraphNodeFilters {
  private BundleGraphNodeFilters() {

  }

  public static IBundleGraphNodeFilter getFilterByType(final int nodeType) {
    return new IBundleGraphNodeFilter() {
      @Override
      public boolean accept(BundleGraphNode node) {
        return node.getNodeType() == nodeType;
      }
    };
  }

  public static IBundleGraphNodeFilter getNotInSetFilter(final Set<BundleGraphNode> excluded) {
    return new IBundleGraphNodeFilter() {
      @Override
      public boolean accept(BundleGraphNode node) {
        return !excluded.contains(node);
      }
    };
  }

  public static IBundleGraphNodeFilter getFilterByTypes(final int[] nodeTypes) {
    return new IBundleGraphNodeFilter() {

      @Override
      public boolean accept(BundleGraphNode node) {
        for (int i : nodeTypes) {
          if (i == node.getNodeType()) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static IBundleGraphNodeFilter getDirectChildFilter(final BundleGraphNode parentNode) {
    return new IBundleGraphNodeFilter() {
      @Override
      public boolean accept(BundleGraphNode node) {
        return parentNode.hasChildNode(node);
      }
    };
  }

  public static IBundleGraphNodeFilter getMultiFilter(final IBundleGraphNodeFilter... filters) {
    return new IBundleGraphNodeFilter() {
      @Override
      public boolean accept(BundleGraphNode node) {
        for (IBundleGraphNodeFilter f : filters) {
          if (!f.accept(node)) {
            return false;
          }
        }
        return true;
      }
    };
  }
}
