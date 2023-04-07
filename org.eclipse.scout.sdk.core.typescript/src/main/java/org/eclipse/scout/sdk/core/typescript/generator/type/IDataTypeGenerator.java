/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;

/**
 * <h3>{@link IDataTypeGenerator}</h3>
 * <p>
 * Marker interface for {@link INodeElementGenerator}s that can be used as aliased types.
 *
 * @since 13.0
 */
public interface IDataTypeGenerator<TYPE extends IDataTypeGenerator<TYPE>> extends INodeElementGenerator<TYPE> {
}
