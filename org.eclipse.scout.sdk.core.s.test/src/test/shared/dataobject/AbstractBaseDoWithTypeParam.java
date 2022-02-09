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
