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
package org.eclipse.scout.sdk.internal.test.operation.form.fields;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.TableFieldNewOperation;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.junit.Test;

public class TableFieldNewOperationTest extends AbstractFieldNewOperationTest {

  private TableFieldNewOperation m_operation;

  @Override
  public IType getCreatedField() {
    return m_operation.getCreatedField();
  }

  @Override
  public String getReferenceFilePath() {
    return "operation/form/fields/fieldReferences/Test1CreateSimpleTableField.java";
  }

  @Override
  public IOperation getOperation(IType declaringType, IStructuredType structuredType) {
    m_operation = new TableFieldNewOperation(declaringType);
    m_operation.setFormatSource(true);
    m_operation.setTypeName("TestTableField");
    m_operation.setSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField"));
    m_operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD));
    return m_operation;
  }

  @Test
  public final void testCreateField() throws Exception {
    doTestCreateField();
  }
}
