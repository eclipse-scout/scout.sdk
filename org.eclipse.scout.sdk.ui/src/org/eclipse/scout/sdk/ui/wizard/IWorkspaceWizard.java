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
package org.eclipse.scout.sdk.ui.wizard;

import org.eclipse.scout.sdk.operation.IOperation;

/**
 * <h3>{@link IWorkspaceWizard}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 28.10.2013
 */
public interface IWorkspaceWizard {

  double ORDER_DEFAULT = 0;
  double ORDER_BEFORE_WIZARD = -10;
  double ORDER_AFTER_WIZARD = 10;

  IOperation addAdditionalPerformFinishOperation(IOperation op, double orderNr);

  IOperation removeAdditionalPerformFinishOperation(IOperation op);

}
