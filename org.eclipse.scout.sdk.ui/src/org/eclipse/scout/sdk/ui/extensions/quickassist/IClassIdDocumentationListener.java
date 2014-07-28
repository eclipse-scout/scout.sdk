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
package org.eclipse.scout.sdk.ui.extensions.quickassist;

import java.util.EventListener;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;

/**
 * <h3>{@link IClassIdDocumentationListener}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 28.03.2014
 */
public interface IClassIdDocumentationListener extends EventListener {

  /**
   * Describes an event type in which a new documentation translation has been created using an existing class id
   * annotation as key.
   */
  int TYPE_NLS_VALUE_CREATED_EXISTING_CLASS_ID = 1 << 1;

  /**
   * describes an event type in which a new documentation translation and a new class id annotation have been created.
   */
  int TYPE_NLS_VALUE_CREATED_NEW_CLASS_ID = 1 << 2;

  /**
   * Describes an event type in which an existing documentation entry has been modified.
   */
  int TYPE_NLS_VALUE_EDITED = 1 << 3;

  /**
   * Callback when a documentation translation has been created or modified
   *
   * @param eventType
   *          The type of the event. One of {@link #TYPE_NLS_VALUE_CREATED_EXISTING_CLASS_ID},
   *          {@link #TYPE_NLS_VALUE_CREATED_NEW_CLASS_ID}, {@link #TYPE_NLS_VALUE_EDITED}
   * @param entry
   *          The new entry after modification or creation.
   * @param owner
   *          The owner type to which the documentation belongs
   * @see ClassIdDocumentationSupport
   */
  void modified(int eventType, INlsEntry entry, IType owner);
}
