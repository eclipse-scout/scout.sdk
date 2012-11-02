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
package org.eclipse.scout.sdk.ui.action.library;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link LibraryBundleRemoveAction}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 12.03.2012
 */
public class LibraryBundleRemoveAction extends AbstractScoutHandler {

  public LibraryBundleRemoveAction() {
    super(Texts.get("RemoveLibraryBundlePopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LibrariesRemove));
  }

  @Override
  public Object execute(final Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    return null;
  }
}
