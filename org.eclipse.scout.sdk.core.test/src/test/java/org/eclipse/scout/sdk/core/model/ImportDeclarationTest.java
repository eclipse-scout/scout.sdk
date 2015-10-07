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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ImportDeclarationTest {
  @Test
  public void testImportDeclaration() {
    ICompilationUnit childClassIcu = CoreTestingUtils.getChildClassIcu();
    Assert.assertNotNull(childClassIcu);

    IImport imp = childClassIcu.imports().iterator().next();
    Assert.assertNotNull(imp);
    Assert.assertEquals(childClassIcu, imp.compilationUnit());
    Assert.assertEquals(Signature.getQualifier(IOException.class.getName()), imp.qualifier());
    Assert.assertEquals(Signature.getSimpleName(IOException.class.getName()), imp.simpleName());
    Assert.assertEquals(IOException.class.getName(), imp.elementName());
  }

  @Test
  public void testToString() {
    ICompilationUnit childClassIcu = CoreTestingUtils.getChildClassIcu();
    Assert.assertNotNull(childClassIcu);

    IImport imp = childClassIcu.imports().iterator().next();
    Assert.assertFalse(StringUtils.isBlank(imp.toString()));
  }
}
