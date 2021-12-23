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
package org.eclipse.scout.sdk.core.s.apidef;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.MaxApiLevel;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.generator.method.IScoutMethodGenerator;
import org.eclipse.scout.sdk.core.s.generator.method.ScoutDoMethodGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

@MaxApiLevel(22)
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout22Api extends IScoutApi, IScoutChartApi, IScout22DoApi {

  IScout22DoApi.DoSet DO_SET = new DoSet();

  @Override
  default IScout22DoApi.DoSet DoSet() {
    return DO_SET;
  }

  class DoSet implements IScout22DoApi.DoSet {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoSet";
    }
  }

  IScout22DoApi.DoCollection DO_COLLECTION = new DoCollection();

  @Override
  default IScout22DoApi.DoCollection DoCollection() {
    return DO_COLLECTION;
  }

  class DoCollection implements IScout22DoApi.DoCollection {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoCollection";
    }
  }

  IScout22DoApi.DoEntity DO_ENTITY = new Scout22Api.DoEntity();

  @Override
  default IScout22DoApi.DoEntity DoEntity() {
    return DO_ENTITY;
  }

  class DoEntity extends Scout10Api.DoEntity implements IScout22DoApi.DoEntity {
    @Override
    public String nvlMethodName() {
      return "nvl";
    }

    @Override
    public String doSetMethodName() {
      return "doSet";
    }

    @Override
    public String doCollectionMethodName() {
      return "doCollection";
    }
  }

  IScoutInterfaceApi.IDoEntity I_DO_ENTITY = new Scout22Api.IDoEntity();

  @Override
  default IScoutInterfaceApi.IDoEntity IDoEntity() {
    return I_DO_ENTITY;
  }

  class IDoEntity extends Scout10Api.IDoEntity {
    @Override
    public String computeGetterPrefixFor(CharSequence dataTypeRef) {
      // since Scout 22 standard bean specification is used
      // because there are two getters: one for boxed and one for primitive boolean
      return PropertyBean.getterPrefixFor(dataTypeRef);
    }

    @Override
    public Stream<IScoutMethodGenerator<?, ?>> getAdditionalDoNodeGetters(CharSequence name, CharSequence dataTypeRef, IType ownerType) {
      if (!Strings.equals(JavaTypes.Boolean, dataTypeRef)) {
        return Stream.empty();
      }

      // since Scout 22: generate additional getter for Booleans returning a primitive boolean
      return Stream.of(ScoutDoMethodGenerator.createDoNodeGetter(name, JavaTypes._boolean, ownerType));
    }
  }

  IScout22DoApi.ITypeVersion I_TYPE_VERSION = new Scout22Api.ITypeVersion();

  @Override
  default IScout22DoApi.ITypeVersion ITypeVersion() {
    return I_TYPE_VERSION;
  }

  class ITypeVersion implements IScout22DoApi.ITypeVersion {

    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.ITypeVersion";
    }
  }

  IScout22DoApi.INamespace I_NAMESPACE = new Scout22Api.INamespace();

  @Override
  default IScout22DoApi.INamespace INamespace() {
    return I_NAMESPACE;
  }

  class INamespace implements IScout22DoApi.INamespace {

    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.namespace.INamespace";
    }
  }
}
