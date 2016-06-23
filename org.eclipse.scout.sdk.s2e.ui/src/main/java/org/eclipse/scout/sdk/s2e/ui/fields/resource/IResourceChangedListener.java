/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields.resource;

import java.io.File;
import java.net.URL;
import java.util.EventListener;

/**
 * <h3>{@link IResourceChangedListener}</h3> Interface for listeners that want to be notified about value changes of a
 * {@link ResourceTextField}.
 *
 * @author Andreas Hoegger
 * @since 1.0.8 2011-02-06
 * @see ResourceTextField
 */
public interface IResourceChangedListener extends EventListener {

  /**
   * Callback that will be invoked when the selected resource ({@link URL} or {@link File}) have been changed.
   *
   * @param newUrl
   *          The new value as {@link URL}. May be <code>null</code> if the field is empty.
   * @param newFile
   *          The new value as {@link File}. May be <code>null</code> if the field is empty or contains an {@link URL}
   *          that does not point to a {@link File}.
   */
  void resourceChanged(URL newUrl, File newFile);

}