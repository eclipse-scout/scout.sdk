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

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.AttributeName;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IgnoreConvenienceMethodGeneration;
import org.eclipse.scout.rt.dataobject.ValueFormat;

public class SampleDo extends DoEntity implements DataObjectTestInterface {
  @Override // is not copied to convenience getter/setter
  @Deprecated
  @AttributeName("myTestName") // is not copied to convenience getter/setter
  @ValueFormat(pattern = "test-pattern") // is not copied to convenience getter/setter
  public DoValue<Boolean> enabled() {
    return doValue("enabled");
  }

  public DoValue<String> stringAttribute() {
    return doValue("stringAttribute");
  }

  public DoList<Long> versions() {
    return doList("versions");
  }

  @IgnoreConvenienceMethodGeneration
  public DoValue<Long> ignored() {
    return doValue("ignored");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Deprecated
  @Generated("DoConvenienceMethodsGenerator")
  public SampleDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Deprecated
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean isEnabled() {
    return enabled().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SampleDo withStringAttribute(String stringAttribute) {
    stringAttribute().set(stringAttribute);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public String getStringAttribute() {
    return stringAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SampleDo withVersions(Collection<? extends Long> versions) {
    versions().updateAll(versions);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public SampleDo withVersions(Long... versions) {
    versions().updateAll(versions);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public List<Long> getVersions() {
    return versions().get();
  }
}
