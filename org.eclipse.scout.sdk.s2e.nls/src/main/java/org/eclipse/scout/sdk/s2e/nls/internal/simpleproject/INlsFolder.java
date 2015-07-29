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

/**
 *
 */
public interface INlsFolder {
  int TYPE_PACKAGE_FOLDER = 1 << 0;
  int TYPE_SIMPLE_FOLDER = 1 << 2;

  IFolder getFolder();

  int getType();
}
