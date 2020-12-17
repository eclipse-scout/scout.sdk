/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.derived;

import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.jdt.core.IType;

/**
 * <h3>{@link IDerivedResourceManager}</h3> Manages the life cycle of any resources that are to be generated based on an
 * existing resource.<br>
 *
 * @since 3.10.0 2013-08-21
 */
public interface IDerivedResourceManager {
  String PROP_AUTO_UPDATE = "org.eclipse.scout.sdk.propAutoUpdate";

  /**
   * Checks if derived resources are updated automatically or not.
   *
   * @return {@code true} if the auto update is enabled, {@code false} otherwise.
   */
  boolean isEnabled();

  /**
   * Starts or stops the auto update.<br>
   * <br>
   * If a derived resource update is in progress, this update is not canceled even if the manager is disabled.
   *
   * @param enabled
   *          {@code true} if the manager should automatically update derived resources, {@code false} otherwise.
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
   *          The {@link IDerivedResourceHandlerFactory} to remove.
   */
  void removeDerivedResourceHandlerFactory(IDerivedResourceHandlerFactory handler);

  /**
   * Trigger an update of all resources that are derived from the given resources.<br>
   * This method enqueues a new derived resources update event and returns immediately. The update process itself is
   * executed asynchronously.<br>
   * If the given resources contain {@link IContainer}s, all children are searched recursively for base classes.
   *
   * @param resources
   *          A {@link Set} with the base resources that should update its derived resources. The resources may be of
   *          type {@link IResource#PROJECT}, {@link IResource#FOLDER}, {@link IResource#FILE}, {@link IResource#ROOT}.
   */
  void trigger(Set<IResource> resources);

  /**
   * Gets the filter used to decide whether an {@link IResourceChangeEvent} should be processed or not.<br>
   * By default the {@link DefaultResourceChangeEventFilter} is used.
   *
   * @return The {@link Predicate} deciding whether an {@link IResourceChangeEvent} should be processed by this
   *         {@link IDerivedResourceManager}. If {@code null} is returned, all events are processed (no filtering).
   */
  Predicate<IResourceChangeEvent> getResourceChangeEventFilter();

  /**
   * Sets a new filter to decide whether an {@link IResourceChangeEvent} should be processed by this
   * {@link IDerivedResourceManager}.
   *
   * @param resourceChangeEventFilter
   *          The new {@link Predicate} or {@code null} if no events should be filtered.
   */
  void setResourceChangeEventFilter(Predicate<IResourceChangeEvent> resourceChangeEventFilter);

}
