/**
 *
 */
package org.eclipse.scout.sdk.internal.test.types;

import junit.framework.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <h3>{@link RecreateTypeTest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 15.07.2011
 */
public class RecreateTypeTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("util/typeCache", "test.client", "test.shared");
  }

  @Test
  public void testRecreateType() throws Exception {
    IType iformField = TypeUtility.getType(RuntimeClasses.IFormField);
    IPrimaryTypeTypeHierarchy primaryFormFieldHierarchy = TypeUtility.getPrimaryTypeHierarchy(iformField);
    Assert.assertFalse(primaryFormFieldHierarchy.isCreated());
    IProject clientProject = getProject("test.client");
    IScoutBundle clientBundle = ScoutSdkCore.getScoutWorkspace().getScoutBundle(clientProject);
    // create new MyAbstractFormField
    IType abstractMyStringField = createType(clientBundle, "AbstractMyStringField", "test.client.ui.custom.field");
    Assert.assertTrue(primaryFormFieldHierarchy.contains(abstractMyStringField));

    // delete MyAbstractFormField
    deleteType(abstractMyStringField);
    Assert.assertFalse(primaryFormFieldHierarchy.contains(abstractMyStringField));

    // recreate
    abstractMyStringField = createType(clientBundle, "AbstractMyStringField", "test.client.ui.custom.field");
    Assert.assertTrue(primaryFormFieldHierarchy.contains(abstractMyStringField));

    // delete MyAbstractFormField
    deleteType(abstractMyStringField);
    Assert.assertFalse(primaryFormFieldHierarchy.contains(abstractMyStringField));

  }

  private IType createType(IScoutBundle bundle, String typeName, String packageName) throws Exception {
    // create new MyAbstractFormField
    ScoutTypeNewOperation op = new ScoutTypeNewOperation(typeName, packageName, bundle);
    op.setTypeModifiers(Flags.AccAbstract | Flags.AccPublic);
    op.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractStringField, true));
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    return op.getCreatedType();
  }

  private void deleteType(IType abstractMyStringField) throws Exception {
    JavaElementDeleteOperation deleteTypeOp = new JavaElementDeleteOperation();
    deleteTypeOp.addMember(abstractMyStringField);
    OperationJob deleteJob = new OperationJob(deleteTypeOp);
    deleteJob.schedule();
    deleteJob.join();
  }
}
