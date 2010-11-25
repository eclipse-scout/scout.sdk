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
package org.eclipse.scout.nls.sdk.internal.model;

import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.search.ui.text.Match;

/** <h4>IReferenceProvider</h4> */
public interface IReferenceProvider {
  public Match[] getReferences(INlsEntry entry);

  public int getReferenceCount(INlsEntry entry);

}
