/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
