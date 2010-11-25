/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.fields.proposal;

public interface IElementProposal extends IContentProposalEx {
  public static final int TYPE_BCTYPE = 1;
  public static final int TYPE_PRIMITIVE = 2;

  /**
   * @return one of {@link IElementProposal#TYPE_BCTYPE} or {@link IElementProposal#TYPE_PRIMITIVE}
   */
  int getProposalType();

}
