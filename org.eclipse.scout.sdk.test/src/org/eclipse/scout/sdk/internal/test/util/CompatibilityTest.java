package org.eclipse.scout.sdk.internal.test.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.TestUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests methods of the CompatibilityUtility.
 * 
 * @see CompatibilityUtility
 */
public class CompatibilityTest extends AbstractScoutSdkTest {

  private final static String PROJECT_NAME = "test.platformUtility";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("resources/util/platformUtility", PROJECT_NAME);
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    clearWorkspace();
  }

  @Test
  public void testResolveTargetPlatform() throws Exception {
    try {
      IProject testProject = getProject(PROJECT_NAME);
      Assert.assertNotNull(testProject);

      IFile targetFile = testProject.getFile("resources/test.target");
      Assert.assertNotNull(targetFile);
      Assert.assertTrue(targetFile.exists());

      TargetPlatformUtility.resolveTargetPlatform(targetFile, true, new NullProgressMonitor());
    }
    finally {
      // reset the platform for all consecutive tests
      TestUtility.loadRunningOsgiAsTarget("testingTarget", new NullProgressMonitor());
    }
  }
}
