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

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IgnoreConvenienceMethodGeneration;

public class SampleDo extends DoEntity implements DataObjectTestInterface {
  public DoValue<Boolean> enabled() {
    return doValue("enabled");
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

  @Generated("DoConvenienceMethodsGenerator")
  public SampleDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public Boolean isEnabled() {
    return enabled().get();
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