/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
        .withSourceFolder(ScoutFixtureHelper.SHARED_FIXTURE_PATH)
        .exclude(".*" + Pattern.quote(".scout.sdk.") + ".*target/test-classes");
  }

  public T withScoutServer(boolean withServer) {
    String rtServer = ".*" + Pattern.quote(".scout.rt.server") + ".*";
    String mom = ".*" + Pattern.quote(".scout.rt.mom") + ".*";
    if (withServer) {
      include(rtServer);
      include(mom);
    }
    else {
      exclude(rtServer);
      exclude(mom);
    }
    return currentInstance();
  }

  public T withScoutClient(boolean withClient) {
    String regex = ".*" + Pattern.quote(".scout.rt.client") + ".*";
    if (withClient) {
      include(regex);
    }
    else {
      exclude(regex);
    }
    return currentInstance();
  }

  public T withScoutHtmlUi(boolean withHtmlUi) {
    String json = ".*" + Pattern.quote(".scout.json") + ".*";
    String uiHtml = ".*" + Pattern.quote(".scout.rt.ui.html") + ".*";
    if (withHtmlUi) {
      include(json);
      include(uiHtml);
    }
    else {
      exclude(json);
      exclude(uiHtml);
    }
    return currentInstance();
  }
}
