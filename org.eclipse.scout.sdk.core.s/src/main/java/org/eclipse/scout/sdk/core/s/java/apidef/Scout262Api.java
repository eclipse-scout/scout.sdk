/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;

@MaxApiLevel({26, 2})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout262Api extends IScoutApi, IScout242Api, IScoutChartApi, IScout22DoApi {
  @Override
  default String ecjVersion() {
    return "3.45.0";
  }

  IScoutInterfaceApi.ISession I_SESSION = new Scout262Api.ISession();

  @Override
  default IScoutInterfaceApi.ISession ISession() {
    return I_SESSION;
  }

  class ISession implements IScoutInterfaceApi.ISession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.session.ISession";
    }
  }

  IScoutAnnotationApi.RunWithServerSession RUN_WITH_SERVER_SESSION_ANNOTATION = new Scout262Api.RunWithServerSession();

  @Override
  default IScoutAnnotationApi.RunWithServerSession RunWithServerSession() {
    return RUN_WITH_SERVER_SESSION_ANNOTATION;
  }

  class RunWithServerSession implements IScoutAnnotationApi.RunWithServerSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.server.session.runner.RunWithServerSession";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutInterfaceApi.IServerSession I_SERVER_SESSION = new Scout262Api.IServerSession();

  @Override
  default IScoutInterfaceApi.IServerSession IServerSession() {
    return I_SERVER_SESSION;
  }

  class IServerSession implements IScoutInterfaceApi.IServerSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.session.IServerSession";
    }
  }

}
