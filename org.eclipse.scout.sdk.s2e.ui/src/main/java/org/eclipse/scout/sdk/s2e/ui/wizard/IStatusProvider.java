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
package org.eclipse.scout.sdk.s2e.ui.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * <h3>IStatusProvider</h3> Provides additional {@link IStatus} information
 *
 * @author Andreas Hoegger
 * @since 1.0.8 12.02.2010
 */
public interface IStatusProvider {

  /**
   * Perform the status validation. Add {@link IStatus} instances to the given {@link MultiStatus}.
   * 
   * @param source
   *          The source of the validation.
   * @param multiStatus
   *          The {@link MultiStatus} to append the {@link IStatus} instances to.
   */
  void validate(Object source, MultiStatus multiStatus);
}
