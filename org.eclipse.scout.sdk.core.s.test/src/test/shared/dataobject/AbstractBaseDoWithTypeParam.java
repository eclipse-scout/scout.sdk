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
import org.eclipse.scout.rt.dataobject.DoValue;

public abstract class AbstractBaseDoWithTypeParam<ID> extends DoEntity {
  public abstract DoValue<ID> id();

  public ID getId() {
    return id().get();
  }

  public AbstractBaseDoWithTypeParam<ID> withId(ID id) {
    id().set(id);
    return this;
  }

  protected static <ID> DoValue<ID> createIdAttribute(AbstractBaseDoWithTypeParam<ID> self) {
    return self.doValue("id");
  }
}
