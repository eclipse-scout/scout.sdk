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
package org.eclipse.scout.sdk.core.sourcebuilder.typeparameter;

import java.util.List;

import org.eclipse.scout.sdk.core.sourcebuilder.IAnnotatableSourceBuilder;

/**
 * <h3>{@link ITypeParameterSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface ITypeParameterSourceBuilder extends IAnnotatableSourceBuilder {

  List<String> getBoundsSignatures();

  void addBoundSignature(String boundSignature);
}
