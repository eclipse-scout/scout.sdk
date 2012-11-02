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
package org.eclipse.scout.nls.sdk.internal.jdt;

import org.eclipse.core.resources.IFolder;

public interface INlsFolder {
  public static final int TYPE_PACKAGE_FOLDER = 1 << 0;
  public static final int TYPE_SIMPLE_FOLDER = 1 << 2;

  public IFolder getFolder();

  public int getType();
}
