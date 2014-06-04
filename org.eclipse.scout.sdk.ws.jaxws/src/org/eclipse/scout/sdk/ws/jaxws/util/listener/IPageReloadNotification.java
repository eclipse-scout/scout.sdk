/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.util.listener;

public interface IPageReloadNotification {
  /**
   * @param markerGroupUUID
   *          the groupUUID this page belongs to
   * @param dataMask
   *          mask to describe data change (<em>bitwise OR</em>). Constants are defined on the respective page
   */
  void reloadPage(int dataMask);

  /**
   * @return the markerGroupUUID the page belongs to. The page is only notified if the notification belongs to this
   *         markerGroupUUID.
   */
  String getMarkerGroupUUID();
}
