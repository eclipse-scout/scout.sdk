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
package org.eclipse.scout.sdk.core.model;

import org.apache.commons.collections4.set.ListOrderedSet;

/**
 * <h3>{@link ITypeParameter}</h3>
 *
 * @author Andreas Hoegger
 * @since 4.1.0 09.11.2014
 */
public interface ITypeParameter {

  String getName();

  ListOrderedSet<IType> getBounds();

  IType getType();

}
