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
package formdata.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.PageChains.PageCalculateVisibleChain;

import formdata.client.ui.desktop.outline.pages.BaseTablePage.Table;
import formdata.shared.services.pages.PageWithTableExtensionData;

@Data(PageWithTableExtensionData.class)
public class PageWithoutTableExtension extends AbstractPageWithTableExtension<Table, BaseTablePage> {

  public PageWithoutTableExtension(BaseTablePage owner) {
    super(owner);
  }

  @Override
  public boolean execCalculateVisible(PageCalculateVisibleChain chain) {
    return false;
  }
}
