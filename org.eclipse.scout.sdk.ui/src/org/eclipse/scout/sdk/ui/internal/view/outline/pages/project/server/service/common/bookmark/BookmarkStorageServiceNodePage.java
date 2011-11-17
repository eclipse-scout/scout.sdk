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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.bookmark;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;

public class BookmarkStorageServiceNodePage extends AbstractServiceNodePage {

  public BookmarkStorageServiceNodePage(AbstractPage parent, IType serviceImplementation, IType serviceInterface) {
    super(parent, serviceImplementation, serviceInterface, SdkProperties.SUFFIX_BOOKMARK_STORAGE_SERVICE);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BOOKMARK_STORAGE_SERVICE_NODE_PAGE;
  }
}
