/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.smtp;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;

public class SmtpServiceNodePage extends AbstractServiceNodePage {

  public SmtpServiceNodePage(AbstractPage parent, IType type, IType interfaceType) {
    super(parent, type, interfaceType, SdkProperties.SUFFIX_SMTP_SERVICE);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SMTP_SERVICE_NODE_PAGE;
  }
}
