/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dataobject;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;

public class DoWithTypeParams<T, S extends CharSequence> extends DoEntity {
  public DoList<Long> versions() {
    return doList("versions");
  }

  public T getT() {
    return null;
  }

  public S getS() {
    return null;
  }
}
