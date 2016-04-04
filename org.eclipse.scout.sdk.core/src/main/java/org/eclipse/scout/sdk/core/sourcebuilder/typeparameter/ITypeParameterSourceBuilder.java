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
package org.eclipse.scout.sdk.core.sourcebuilder.typeparameter;

import java.util.List;

import org.eclipse.scout.sdk.core.sourcebuilder.IJavaElementSourceBuilder;

/**
 * <h3>{@link ITypeParameterSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-03-07
 */
public interface ITypeParameterSourceBuilder extends IJavaElementSourceBuilder {

  List<String> getBoundsSignatures();

  void addBoundSignature(String boundSignature);
}
