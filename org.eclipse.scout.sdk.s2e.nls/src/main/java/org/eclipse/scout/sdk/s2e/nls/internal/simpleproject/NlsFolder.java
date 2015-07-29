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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleproject;

import org.eclipse.core.resources.IFolder;

public class NlsFolder implements INlsFolder {

  private int m_type;
  private IFolder m_folder;

  public NlsFolder(IFolder folder, int type) {
    m_folder = folder;
    m_type = type;
  }

  @Override
  public IFolder getFolder() {
    return m_folder;
  }

  @Override
  public int getType() {
    return m_type;
  }
}
