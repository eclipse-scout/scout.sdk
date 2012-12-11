package org.eclipse.scout.sdk.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.rt.testing.shared.ScoutJUnitPluginTestExecutor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;
import org.junit.Assert;

/**
 * This application launches the eclipse workbench and registers a call-back that executes JUnit tests after the
 * workbench has been started. The tests are selected and executed by a {@link ScoutJUnitPluginTestExecutor}.
 */
public class ScoutSdkTestApplication implements ITestHarness, IApplication {

  private static final String WORKBENCH_APPLICATION_ID = "org.eclipse.ui.ide.workbench";

  private TestableObject m_testableObject;
  private int m_scoutJUnitPluginTesetExecutorResult = -1;

  @Override
  public Object start(IApplicationContext context) throws Exception {
    // Get the application to test
    IApplication application = getApplication();
    if (application == null) {
      System.err.println("ScoutSdkTestApplication: application '" + WORKBENCH_APPLICATION_ID + "' is missing");
      return ScoutJUnitPluginTestExecutor.EXIT_CODE_ERRORS_OCCURRED;
    }
    m_testableObject = PlatformUI.getTestableObject();
    m_testableObject.setTestHarness(this);

    Object result = application.start(context);
    if (!EXIT_OK.equals(result)) {
      System.err.println("ScoutSdkTestApplication: Unexpected result from running application " + application + ": " + result);
    }
    return new Integer(m_scoutJUnitPluginTesetExecutorResult);
  }

  @Override
  public void stop() {
  }

  private IApplication getApplication() throws CoreException {
    IExtension extension = Platform.getExtensionRegistry().getExtension(Platform.PI_RUNTIME, Platform.PT_APPLICATIONS, WORKBENCH_APPLICATION_ID);
    Assert.assertNotNull(extension);

    // create a new instance of the provided IApplication
    IConfigurationElement[] elements = extension.getConfigurationElements();
    if (elements.length > 0) {
      IConfigurationElement[] runs = elements[0].getChildren("run");
      if (runs.length > 0) {
        return (IApplication) runs[0].createExecutableExtension("class");
      }
    }
    return null;
  }

  /**
   * Call-back method invoked by the workbench application.
   */
  @Override
  public void runTests() {
    m_testableObject.testingStarting();
    // run tests in UI thread
    m_testableObject.runTest(new Runnable() {
      @Override
      public void run() {
        m_scoutJUnitPluginTesetExecutorResult = new ScoutJUnitPluginTestExecutor().runAllTests();
      }
    });
    m_testableObject.testingFinished();
  }
}
