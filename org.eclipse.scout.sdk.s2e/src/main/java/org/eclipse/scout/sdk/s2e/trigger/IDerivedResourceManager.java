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
 * <h3>{@link IDerivedResourceManager}</h3> Manages the life cycle of any resources that are to be generated based on an
 * existing resource.<br>
 *
 * @author Andreas Hoegger, Matthias Villiger, Ivan Motsch
 * @since 3.10.0 21.08.2013
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public interface IDerivedResourceManager {
  String PROP_AUTO_UPDATE = "org.eclipse.scout.sdk.propAutoUpdate";

  /**
   * Checks if derived resources are updated automatically or not.
   *
   * @return <code>true</code> if the auto update is enabled, <code>false</code> otherwise.
   */
  boolean isEnabled();

  /**
   * Starts or stops the auto update.<br>
   * <br>
   * If a derived resource update is in progress, this update is not cancelled even if the manager is disabled.
   *
   * @param enabled
   *          <code>true</code> if the manager should automatically update derived resources, <code>false</code>
   *          otherwise.
   */
  void setEnabled(boolean enabled);

  /**
   * Adds a handler factory to resolve the necessary handlers for an {@link IType} that changes.
   *
   * @param handler
   *          factory
   */
  void addDerivedResourceHandlerFactory(IDerivedResourceHandlerFactory handler);

  /**
   * Removes a handler factory
   *
   * @param handler
   */
  void removeDerivedResourceHandlerFactory(IDerivedResourceHandlerFactory handler);

  /**
   * Trigger a change on an observed type
   *
   * @param jdtType
   *          The type for which the derived resources should be updated.
   */
  void trigger(IType jdtType);

  /**
   * Trigger changes on all observed types in the given scope
   */
  void triggerAll(IJavaSearchScope scope);

  /**
   * Trigger a derived resource cleanup for the given scope.
   * 
   * @param scope
   *          The scope that should be cleaned.
   */
  void triggerCleanup(IJavaSearchScope scope);

}
