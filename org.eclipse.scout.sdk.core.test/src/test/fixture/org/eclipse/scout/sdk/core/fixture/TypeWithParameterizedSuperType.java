/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture;

import java.util.AbstractList;

/**
 * <h3>{@link TypeWithParameterizedSuperType}</h3>
 *
 * @since 7.1.0
 */
public class TypeWithParameterizedSuperType extends AbstractList<Long> {

  @Override
  public Long get(int index) {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }

}
