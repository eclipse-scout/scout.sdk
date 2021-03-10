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
