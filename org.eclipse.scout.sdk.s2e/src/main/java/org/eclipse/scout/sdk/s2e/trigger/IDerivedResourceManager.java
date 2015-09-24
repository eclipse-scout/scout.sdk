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
package org.eclipse.scout.sdk.s2e.trigger;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;

/**
 * <h3>{@link IDerivedResourceManager}</h3> Manages the lifecycle of any resources that are to be generated based on an
 * existing resource.<br>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 21.08.2013
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public interface IDerivedResourceManager {
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
   * Adds a handler to resolve the necessary operations for a compilation unit candidate.
   *
   * @param handler
   */
  void addDerivedResourceHandler(IDerivedResourceHandler handler);

  /**
   * Removes a handler
   *
   * @param handler
   */
  void removeDerivedResourceHandler(IDerivedResourceHandler handler);

  /**
   * Trigger a change on an observed type
   */
  void trigger(IType jdtType);

  /**
   * Trigger changes on all observed types
   */
  void triggerAll(IJavaSearchScope scope);

}
