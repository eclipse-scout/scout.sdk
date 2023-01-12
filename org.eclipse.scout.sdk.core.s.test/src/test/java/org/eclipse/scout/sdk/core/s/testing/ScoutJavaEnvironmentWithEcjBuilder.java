/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing;

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;

/**
 * <h3>{@link ScoutJavaEnvironmentWithEcjBuilder}</h3>
 *
 * @since 7.0.0
 */
public class ScoutJavaEnvironmentWithEcjBuilder<T extends ScoutJavaEnvironmentWithEcjBuilder<T>> extends JavaEnvironmentWithEcjBuilder<T> {

  public ScoutJavaEnvironmentWithEcjBuilder() {
    withoutScoutSdk()
        .withParseMethodBodies(true) // required for testing
        .exclude(".*" + Pattern.quote(".scout.sdk.") + ".*target/test-classes") // exclude sdk
        .exclude(".*[\\/]org[\\/]apache[\\/]maven[\\/].*") // exclude Maven runtime
        .exclude(".*[\\/]org[\\/]junit[\\/].*"); // exclude jUnit 5 (only used in SDK)
  }

  public T withScoutServer(boolean withServer) {
    var rtServer = ".*\\.scout\\.rt\\.server.*|.*\\.scout\\.rt\\.[\\w\\.]+\\.server.*";
    var mom = ".*\\.scout\\.rt\\.mom.*";
    if (withServer) {
      include(rtServer);
      include(mom);
    }
    else {
      exclude(rtServer);
      exclude(mom);
    }
    return thisInstance();
  }

  public T withScoutClient(boolean withClient) {
    var regex = ".*\\.scout\\.rt\\.client.*|.*\\.scout\\.rt\\.[\\w\\.]+\\.client.*";
    if (withClient) {
      include(regex);
    }
    else {
      exclude(regex);
    }
    return thisInstance();
  }

  public T withScoutHtmlUi(boolean withHtmlUi) {
    var json = ".*\\.scout\\.json.*";
    var uiHtml = ".*\\.scout\\.rt\\.ui\\.html.*|.*\\.scout\\.rt\\.[\\w\\.]+\\.ui\\.html.*";
    if (withHtmlUi) {
      include(json);
      include(uiHtml);
    }
    else {
      exclude(json);
      exclude(uiHtml);
    }
    return thisInstance();
  }
}
