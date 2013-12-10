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
package org.eclipse.scout.sdk.ui.view.outline;

import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPageFilter;

/**
 * <h3>{@link IPageFilterable}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 09.12.2013
 */
public interface IPageFilterable {
  IPageFilter getPageFilter(IPage page);

  void markFilterChanged(IPage page);

  void addPageFilter(IPage page, IPageFilter filter);
}
