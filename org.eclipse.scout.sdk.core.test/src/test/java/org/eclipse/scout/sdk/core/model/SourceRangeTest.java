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
package org.eclipse.scout.sdk.core.model;

import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link SourceRangeTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class SourceRangeTest {
  @Test
  public void testSourceRange() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    IType childClassType = CoreTestingUtils.getChildClassType();

    Assert.assertSame(ISourceRange.NO_SOURCE, baseClassType.containingPackage().source());
    Assert.assertFalse(baseClassType.source().isAvailable());

    ISourceRange source = childClassType.source();
    Assert.assertTrue(source.isAvailable());
    Assert.assertTrue(source.start() > childClassType.compilationUnit().source().start());
    Assert.assertTrue(source.end() < childClassType.compilationUnit().source().end());
    Assert.assertTrue(source.end() < childClassType.compilationUnit().source().end());
    Assert.assertTrue(source.toString().contains("ChildClass<X extends AbstractList<String> & Runnable & Serializable> extends BaseClass<X, Long> implements InterfaceLevel0 {"));
    Assert.assertTrue(source.length() < childClassType.compilationUnit().source().length());
  }
}
