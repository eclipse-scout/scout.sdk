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
package org.eclipse.scout.sdk.s2e.dto;

import java.util.List;

/**
 * <h3>{@link IDtoAutoUpdateManager}</h3> Manages the lifecycle of the Scout DTO auto update feature. <br>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 21.08.2013
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public interface IDtoAutoUpdateManager {
  String PROP_AUTO_UPDATE = "org.eclipse.scout.sdk.propAutoUpdate";

  /**
   * Gets if the DTO Auto update is enabled or not.
   *
   * @return true if the auto update is enabled, false otherwise.
   */
  boolean isEnabled();

  /**
   * Starts or stops the manager.<br>
   * According to the enabled flag all listeners are registered/removed and the resource event handling job is
   * cancelled.<br>
   * <br>
   * If the DTO update job is already running and updating DTO classes, this job is not touched even if the manager is
   * disabled. This way no events are lost (which would lead in obsolete DTO classes). The user can cancel the job
   * manually anyway.
   *
   * @param enabled
   *          true if the manager should update DTO classes, false otherwise.
   */
  void setEnabled(boolean enabled);

  /**
   * Adds an update handler to resolve the necessary operations for a compilation unit candidate.
   *
   * @param handler
   */
  void addModelDataUpdateHandler(IDtoAutoUpdateHandler handler);

  /**
   * Removes an update handler
   *
   * @param handler
   */
  void removeModelDataUpdateHandler(IDtoAutoUpdateHandler handler);

  /**
   * @return
   */
  List<IDtoAutoUpdateHandler> getUpdateHandlers();
}
