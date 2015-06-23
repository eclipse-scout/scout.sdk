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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.search.ui.text.Match;

/** <h4>IReferenceProvider</h4> */
public interface IReferenceProvider {
  Match[] getReferences(INlsEntry entry);

  int getReferenceCount(INlsEntry entry);
}
