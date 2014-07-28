/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.properties.model.links;

import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;

/**
 * <h3>{@link UrlOpenLink}</h3> Link that opens the given URL in an external browser window.
 *
 * @author Matthias Villiger
 * @since 3.10.0 09.09.2013
 */
public class UrlOpenLink extends AbstractLink {

  private final String m_url;

  public UrlOpenLink(String name, String url, int order) {
    super(name, ScoutSdkUi.getImage(ScoutSdkUi.Web), order);
    m_url = url;
  }

  @Override
  public void execute() {
    ResourceUtility.showUrlInBrowser(getUrl());
  }

  public String getUrl() {
    return m_url;
  }
}
