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
package org.eclipse.scout.sdk.s2e.ui.fields.file;

import java.io.File;
import java.util.EventListener;

/**
 * <h3>{@link IFileSelectionListener}</h3> Interface for listeners that want to be notified about file selection changes
 * of a {@link FileSelectionField}.
 *
 * @author Andreas Hoegger
 * @since 1.0.8 2011-02-06
 * @see FileSelectionField
 */
public interface IFileSelectionListener extends EventListener {

  /**
   * Listener notification if a new {@link File} has been selected in the {@link FileSelectionField}.
   * 
   * @param file
   *          The newly selected {@link File}.
   */
  void fileSelected(File file);

}
