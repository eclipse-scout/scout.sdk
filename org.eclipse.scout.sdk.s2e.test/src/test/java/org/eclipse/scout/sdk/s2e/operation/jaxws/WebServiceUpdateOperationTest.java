/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.s.testing.IntegrationTest;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.BindingClassUpdate;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.EntryPointDefinitionUpdate;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.WebServiceClientUpdate;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceUpdateOperation.WebServiceImplementationUpdate;
import org.eclipse.scout.sdk.s2e.testing.mock.FileSystemMockFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

/**
 * <h3>{@link WebServiceUpdateOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
@Category(IntegrationTest.class)
public class WebServiceUpdateOperationTest {

  private static final String PORT_TYPE_NAME_IN_WSDL = "scoutQueryInterface2";
  private static final String WEB_SERVICE_NAME_IN_WSDL = "scoutQuery";
  private static final String ORIGINAL_PORT_TYPE_NAME = "IScoutQueryInterface2PortType";
  private static final String ORIGINA_WEB_SERVICE_NAME = "ScoutQueryService";
  private static final String ORIGINAL_ENTRY_POINT_NAME = "ScoutQueryInterface2WebServiceEntryPoint";

  @Test
  public void testUpdateWebServiceConsumer() throws CoreException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    File root = CoreScoutTestingUtils.createTestProject();
    try {
      // create project with web service
      String origPackage = "test.consumer.multifile";
      WebServiceNewOperation wsCreateOp = WebServiceNewOperationTest.createWebServiceConsumer(root, true, WebServiceNewOperationTest.MULTI_FILE_WSDL, null, origPackage);

      // setup mocks
      final FileSystemMockFactory factory = new FileSystemMockFactory();
      File pckLoc = new File(wsCreateOp.getJaxWsProject().getResource().getLocation().toFile(), "src/main/java/" + origPackage.replace('.', '/') + '/');
      IType wsClient = factory.createIType(new File(pckLoc, "ScoutQueryInterface2WebServiceClient.java"), wsCreateOp.getJaxWsProject());
      IJavaEnvironmentProvider javaEnvProvider = factory.createJavaEnvProvider();

      // start update
      String newPortTypePackage = "test.consumer.newMultiFile";
      String newPortTypeName = "IChangedPortType";
      String newServiceName = "ChangedNameService";
      WebServiceUpdateOperation wsUpdateOp = new WebServiceUpdateOperation(javaEnvProvider);
      wsUpdateOp.setJaxwsBindingFiles(wsCreateOp.getCreatedJaxwsBindingFiles());
      wsUpdateOp.setPackage(newPortTypePackage);
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getWebServiceXPath(WEB_SERVICE_NAME_IN_WSDL), newServiceName));
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getPortTypeXPath(PORT_TYPE_NAME_IN_WSDL), newPortTypeName));
      wsUpdateOp.addWebServiceClientUpdate(new WebServiceClientUpdate(wsClient, newPortTypePackage, newPortTypeName, newServiceName));
      wsUpdateOp.validate();
      wsUpdateOp.run(new NullProgressMonitor(), Mockito.mock(IWorkingCopyManager.class));

      // ensure the generated resources compile
      assertProjectCompiles(wsCreateOp.getJaxWsProject());

      // validate change
      IJavaEnvironment env = javaEnvProvider.get(wsCreateOp.getJaxWsProject());
      Assert.assertNull(env.findType(origPackage + '.' + ORIGINAL_PORT_TYPE_NAME));
      Assert.assertNull(env.findType(origPackage + '.' + ORIGINA_WEB_SERVICE_NAME));
      Assert.assertNotNull(env.findType(newPortTypePackage + '.' + newServiceName));
      Assert.assertNotNull(env.findType(newPortTypePackage + '.' + newPortTypeName));
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  protected static void assertProjectCompiles(IJavaProject project) throws IOException {
    File jaxWsProjectDir = project.getProject().getLocation().toFile();
    File parentDir = new File(jaxWsProjectDir.getParentFile(), CoreScoutTestingUtils.PROJECT_ARTIFACT_ID);
    CoreUtils.deleteDirectory(new File(jaxWsProjectDir, "target"));
    CoreScoutTestingUtils.runMavenCleanTest(parentDir);
  }

  @Test
  public void testUpdateWebServiceProvider() throws CoreException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {

    File root = CoreScoutTestingUtils.createTestProject();
    try {
      // create project with web service
      String origPackage = "test.provider.multifile";
      WebServiceNewOperation wsCreateOp = WebServiceNewOperationTest.createWebServiceProvider(root, true, WebServiceNewOperationTest.MULTI_FILE_WSDL, null, origPackage);

      // setup mocks
      final FileSystemMockFactory factory = new FileSystemMockFactory();
      File pckLoc = new File(wsCreateOp.getJaxWsProject().getResource().getLocation().toFile(), "src/main/java/" + origPackage.replace('.', '/') + '/');
      IType entryPointDef = factory.createIType(new File(pckLoc, "IScoutQueryInterface2WebServiceEntryPointDefinition.java"), wsCreateOp.getJaxWsProject());
      IType serviceImpl = factory.createIType(new File(pckLoc, "ScoutQueryInterface2WebService.java"), wsCreateOp.getJaxWsProject());
      IJavaEnvironmentProvider javaEnvProvider = factory.createJavaEnvProvider();

      // start update
      String newPortTypePackage = "test.provider.newMultiFile";
      String newPortTypeName = "IChangedPortType";
      String newEntryPointPackage = "entry.point.changedpackage";
      String newEntryPointName = "ChangedEntryPointName";
      String newServiceName = "ChangedNameService";
      WebServiceUpdateOperation wsUpdateOp = new WebServiceUpdateOperation(javaEnvProvider);
      wsUpdateOp.setJaxwsBindingFiles(wsCreateOp.getCreatedJaxwsBindingFiles());
      wsUpdateOp.setPackage(newPortTypePackage);
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getWebServiceXPath(WEB_SERVICE_NAME_IN_WSDL), newServiceName));
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getPortTypeXPath(PORT_TYPE_NAME_IN_WSDL), newPortTypeName));
      wsUpdateOp.addEntryPointDefinitionUpdate(new EntryPointDefinitionUpdate(entryPointDef, newEntryPointPackage, newEntryPointName, newPortTypeName, newPortTypePackage));
      wsUpdateOp.addWebServiceImplementationUpdate(new WebServiceImplementationUpdate(serviceImpl, newPortTypePackage, newPortTypeName));
      wsUpdateOp.validate();
      wsUpdateOp.run(new NullProgressMonitor(), Mockito.mock(IWorkingCopyManager.class));

      // ensure the generated resources compile
      assertProjectCompiles(wsCreateOp.getJaxWsProject());

      // validate change
      IJavaEnvironment env = javaEnvProvider.get(wsCreateOp.getJaxWsProject());
      Assert.assertNull(env.findType(origPackage + '.' + ORIGINAL_PORT_TYPE_NAME));
      Assert.assertNull(env.findType(origPackage + '.' + ORIGINA_WEB_SERVICE_NAME));
      Assert.assertNull(env.findType(origPackage + '.' + ORIGINAL_ENTRY_POINT_NAME));
      Assert.assertNotNull(env.findType(newEntryPointPackage + '.' + newEntryPointName));
      Assert.assertNotNull(env.findType(newPortTypePackage + '.' + newServiceName));
      Assert.assertNotNull(env.findType(newPortTypePackage + '.' + newPortTypeName));
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }
}
