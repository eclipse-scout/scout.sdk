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
package org.eclipse.scout.sdk.core.model.spi.internal;

import org.eclipse.scout.sdk.core.model.api.internal.SourceRange;

/**
 * <h3>{@link SourceRangeWithJdt}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class SourceRangeWithJdt extends SourceRange {
  public SourceRangeWithJdt(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, int start, int end) {
    super(new String(sourceUnit.getContents(), start, end - start + 1), start, end);
  }
}
