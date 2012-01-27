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
package org.eclipse.scout.sdk.ui.extensions.view.property;

import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>ISinglePropertyViewInput</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 22.07.2010
 */
public interface ISinglePropertyViewPart extends IPropertyViewPart {

  void setPage(IPage page);

  IPage getPage();
}
