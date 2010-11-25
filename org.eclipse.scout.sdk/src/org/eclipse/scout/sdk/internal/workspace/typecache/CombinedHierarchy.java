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
package org.eclipse.scout.sdk.internal.workspace.typecache;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

/**
 *
 */
public class CombinedHierarchy extends TypeHierarchy {

  /**
   * @param type
   * @param jdtHierarchy
   */
  public CombinedHierarchy(IType type, ITypeHierarchy jdtHierarchy) {
    super(type, jdtHierarchy);
  }

}
