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

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;

public class ChildDoWithTypeArg extends AbstractBaseDoWithTypeParam<Long> {

  public Long getId() {
    return id().get();
  }

  @Override
  public DoValue<Long> id() {
    return createIdAttribute(this);
  }

  // here a comment that must survive the DO convenience method update

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public ChildDoWithTypeArg withId(Long id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public void oldAnnotatedMethodThatShouldBeRemoved(Long id) {
    doValue("whatever" + id);
  }
}
