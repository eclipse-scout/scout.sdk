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
package org.eclipse.scout.sdk.core.s.nls;

import java.util.Comparator;

@SuppressWarnings({"squid:S2063", "ComparatorNotSerializable"}) // Comparators should be "Serializable". Not necessary here because translation stores are not.
public class TranslationStoreComparator implements Comparator<ITranslationStore> {

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
