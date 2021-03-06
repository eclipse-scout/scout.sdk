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
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Spliterator;

import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link InnerTypeQuery}</h3>
 *
 * @since 6.1.0
 */
public class InnerTypeQuery extends AbstractInnerTypeQuery<InnerTypeQuery> {

  public InnerTypeQuery(Spliterator<IType> innerTypes) {
    super(innerTypes);
  }
}
