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
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;

public abstract class BaseDo extends DoEntity {
  public DoValue<CharSequence> id() {
    return doValue("id");
  }

  public DoList<Long> versions() {
    return doList("versions");
  }

  /**
   * JavaDoc for enabled node
   */
  public DoValue<Boolean> enabled() {
    return doValue("enabled");
  }

  public DoValue<Boolean> notANodeBecauseHasArg(boolean argument) {
    return doValue("notANodeBecauseHasArg" + argument);
  }

  protected DoValue<String> notANodeBecauseNotPublic() {
    return doValue("notANodeBecauseNotPublic");
  }

  public abstract DoValue<String> abstractNodeTestingInherit();

  public String notANodeBecauseWrongReturnType() {
    return notANodeBecauseNotPublic().get();
  }
}
