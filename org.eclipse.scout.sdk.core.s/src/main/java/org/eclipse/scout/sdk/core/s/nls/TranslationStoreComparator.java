/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import java.util.Comparator;

/**
 * Comparator naturally sorting {@link ITranslationStore} instances by its @Order annotation (low order come first =
 * ascending). This means the most important {@link ITranslationStore} is at the first position in the sorted
 * collection.
 */
@SuppressWarnings({"squid:S2063", "ComparatorNotSerializable"}) // Comparators should be "Serializable". Not necessary here because translation stores are not.
public final class TranslationStoreComparator implements Comparator<ITranslationStore> {

  public static final Comparator<ITranslationStore> INSTANCE = new TranslationStoreComparator();

  private TranslationStoreComparator() {
  }

  @Override
  public int compare(ITranslationStore o1, ITranslationStore o2) {
    if (o1 == o2) {
      return 0;
    }
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }

    var order1 = o1.service().order();
    var order2 = o2.service().order();
    var compare = Double.compare(order1, order2);
    if (compare != 0) {
      return compare;
    }

    var o1Fqn = o1.service().type().name();
    var o2Fqn = o2.service().type().name();
    return o1Fqn.compareTo(o2Fqn);
  }
}
