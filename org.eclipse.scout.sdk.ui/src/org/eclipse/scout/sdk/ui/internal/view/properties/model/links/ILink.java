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
package org.eclipse.scout.sdk.ui.internal.view.properties.model.links;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;

/**
 * <h3>ILink</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public interface ILink {

  /**
   * @return
   */
  String getName();

  Image getImage();

  int getOrderNumber();

  void execute();

  /**
   * is called once the link is disposed. can be used to free {@link Resource}
   */
  void dispose();

}
