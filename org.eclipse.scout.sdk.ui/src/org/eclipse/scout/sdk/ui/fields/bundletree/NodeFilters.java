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
package org.eclipse.scout.sdk.ui.fields.bundletree;

import org.eclipse.scout.commons.CompareUtility;

/**
 * <h3>NodeFilters</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 03.02.2010
 */
public final class NodeFilters {
  private NodeFilters() {
  }

  public static ITreeNodeFilter getByData(final Object... data) {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        if (data == null) {
          return node.getData() == null;
        }
        for (Object o : data) {
          if (o.equals(node.getData())) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static ITreeNodeFilter getByType(final String... types) {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        if (types == null || types.length < 1) {
          return false;
        }
        for (String t : types) {
          if (CompareUtility.equals(t, node.getType())) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static ITreeNodeFilter getByLabel(final String label) {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        return CompareUtility.equals(label, node.getText());
      }
    };
  }

  public static ITreeNodeFilter getVisible() {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        return node != null && node.isVisible();
      }
    };
  }

  public static ITreeNodeFilter getCombinedFilter(final ITreeNodeFilter[] filters) {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        for (ITreeNodeFilter f : filters) {
          if (!f.accept(node)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  /**
   * @return
   */
  public static ITreeNodeFilter getAcceptAll() {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        return true;
      }
    };
  }

  /**
   * @return
   */
  public static ITreeNodeFilter getAcceptNone() {
    return new ITreeNodeFilter() {
      @Override
      public boolean accept(ITreeNode node) {
        return false;
      }
    };
  }

}
