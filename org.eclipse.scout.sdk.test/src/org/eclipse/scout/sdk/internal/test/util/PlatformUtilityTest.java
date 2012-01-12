package org.eclipse.scout.sdk.internal.test.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.PlatformUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests methods of the PlatformUtility.
 * 
 * @see PlatformUtility
 */
public class PlatformUtilityTest extends AbstractScoutSdkTest {

  private final static String PROJECT_NAME = "test.platformUtility";

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("util/platformUtility", PROJECT_NAME);
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    clearWorkspace();
  }

  /**
   * The PlatformUtility.resolveTargetPlatform method uses reflection to resolve and load a *.target file in the active
   * workspace. This is needed because the API differs depending on the eclipse version.<br>
   * This method tests if the resolve and load of a *.target file works against the used eclipse version.
   * This is important as with reflection no compiler checks exist anymore for this API.
   * 
   * @throws CoreException
   *           on a failure
   */
  @Test
  public void testResolveTargetPlatform() throws CoreException {
    IProject testProject = getProject(PROJECT_NAME);
    Assert.assertNotNull(testProject);

    IFile targetFile = testProject.getFile("resources/test.target");
    Assert.assertNotNull(targetFile);
    Assert.assertTrue(targetFile.exists());

    PlatformUtility.resolveTargetPlatform(targetFile, true, new NullProgressMonitor());
  }
}
