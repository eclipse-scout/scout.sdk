/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.generator.method;

import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.s.builder.java.body.IScoutMethodBodyBuilder;

/**
 * <h3>{@link IScoutMethodGenerator}</h3>
 *
 * @since 6.1.0
 */
public interface IScoutMethodGenerator<TYPE extends IScoutMethodGenerator<TYPE, BODY>, BODY extends IScoutMethodBodyBuilder<?>> extends IMethodGenerator<TYPE, BODY> {

}
