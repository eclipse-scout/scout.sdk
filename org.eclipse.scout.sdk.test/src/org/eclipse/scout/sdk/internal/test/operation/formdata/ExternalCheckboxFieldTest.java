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
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExternalCheckboxFieldTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");
  }

  @Test
  public void testCreateFormData() throws Exception {
    String templateName = "AbstractTestCheckboxField";
    IType template = TypeUtility.getType("formdata.client.ui.template.formfield." + templateName);
    Assert.assertTrue(TypeUtility.exists(template));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataUpdateOperation op = new FormDataUpdateOperation(template);
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    buildWorkspace();
    IType formData = op.getFormDataType();
    Assert.assertTrue(TypeUtility.exists(formData));
    Assert.assertTrue(TypeUtility.exists(formData));
    Assert.assertEquals(formData.getFullyQualifiedName(), "formdata.shared.services.process." + templateName + "Data");
    Assert.assertEquals(formData.getSuperclassTypeSignature(), "QAbstractValueFieldData<QBoolean;>;");

  }

}
