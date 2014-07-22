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
package org.eclipse.scout.sdk.sql.binding.model;

/**
 * <h3>{@link IBindBase}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public interface IBindBase {
  int TYPE_PROPERTY_BASE = 1;
  int TYPE_NVPAIR = 2;
  int TYPE_SERVER_SESSION = 3;
  int TYPE_IGNORED = 4;
  int TYPE_UNRESOLVED = 10;

  int getType();
}
