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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table;

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.TableColumnDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TableColumnRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.SdkProperties;

public class ColumnNodePage extends AbstractScoutTypePage {

  public ColumnNodePage() {
    super(SdkProperties.SUFFIX_TABLE_COLUMN);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableColumn));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COLUMN_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredHeaderText";
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(TableColumnRenameAction.class, TableColumnDeleteAction.class, ShowJavaReferencesAction.class);
  }
}