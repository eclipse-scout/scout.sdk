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

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;

public class ChildDoWithTypeArg extends AbstractBaseDoWithTypeParam<Long> {

  @Override
  public DoValue<Long> id() {
    return createIdAttribute(this);
  }

  public Integer getTestAttribute() {
    return testAttribute().get();
  }

  public DoValue<Integer> testAttribute() {
    return doValue("testAttribute");
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
  public ChildDoWithTypeArg withTestAttribute(Integer testAttribute) {
    testAttribute().set(testAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public void oldAnnotatedMethodThatShouldBeRemoved(Long id) {
    doValue("whatever" + id);
  }
}
