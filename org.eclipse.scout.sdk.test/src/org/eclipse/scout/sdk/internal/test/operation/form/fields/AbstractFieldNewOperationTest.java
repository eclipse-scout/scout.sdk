package org.eclipse.scout.sdk.internal.test.operation.form.fields;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.internal.test.Activator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.testing.compare.CompareUtility;
import org.eclipse.scout.sdk.testing.compare.ICompareResult;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public abstract class AbstractFieldNewOperationTest extends AbstractScoutSdkTest {
  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/operation/form/fields", "formfield.shared", "formfield.client");
  }

  public abstract IType getCreatedField();

  public abstract IOperation getOperation(IType delcaringType, IStructuredType structuredType);

  public abstract String getReferenceFilePath();

  protected IType getTestCodeType() {
    return TypeUtility.getType("formfield.shared.services.code.TestCodeType");
  }

  public final void doTestCreateField() throws Exception {
    IType form = TypeUtility.getType("formfield.client.ui.forms.DesktopForm.MainBox");
    IStructuredType structuredType = ScoutTypeUtility.createStructuredCompositeField(form);

    IOperation op = getOperation(form, structuredType);
    executeBuildAssertNoCompileErrors(op);
    IType createdField = getCreatedField();

    ICompareResult<String> result = CompareUtility.compareSource(createdField, Activator.getDefault().getBundle(), getReferenceFilePath(), true);
    if (!result.isEqual()) {
      Assert.fail(result.toString());
    }
  }

  protected String getContent(InputStream is) throws Exception {
    char[] buffer = new char[8192];
    Reader in = new InputStreamReader(is);
    StringWriter sw = new StringWriter();
    int charsRead;
    while ((charsRead = in.read(buffer)) != -1) {
      sw.write(buffer, 0, charsRead);
    }
    return sw.toString();
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
