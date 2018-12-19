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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.testing.mock.FileSystemMockFactory;
import org.eclipse.scout.sdk.s2e.util.ScoutStatus;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <h3>{@link WebServiceNewOperationTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceNewOperationTest {

  protected static final URL MULTI_FILE_WSDL = Validate.notNull(WebServiceNewOperationTest.class.getClassLoader().getResource("testws/multi_file/MultiFile.wsdl"));
  protected static final URL MULTI_SERVICE_WSDL = Validate.notNull(WebServiceNewOperationTest.class.getClassLoader().getResource("testws/multi_service/MultiService.wsdl"));
  protected static final URL RPC_ENCODED_WSDL = Validate.notNull(WebServiceNewOperationTest.class.getClassLoader().getResource("testws/encoded/unsupported.wsdl"));

  @Test
  public void testUnsupportedService() throws IOException, WSDLException, URISyntaxException {
    try (InputStream in = RPC_ENCODED_WSDL.openStream()) {
      ParsedWsdl info = ParsedWsdl.create(RPC_ENCODED_WSDL.toURI(), in, true);
      Assert.assertTrue(info.isEmpty());
    }
  }

  @Test
  public void testNewWebServicesConsumerFirst() throws CoreException, IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    File root = CoreScoutTestingUtils.createTestProject();
    try {
      WebServiceNewOperation executedOperation = createWebServiceConsumer(root, true, MULTI_FILE_WSDL, null, "test.consumer");
      IJavaProject jaxWsProject = executedOperation.getJaxWsProject();
      assertMultiFileWsdlCorrect(executedOperation, true);

      executedOperation = createEmptyWebServiceProvider(root, false, "TestEmpty" + ISdkProperties.SUFFIX_WS_PROVIDER, jaxWsProject, "test.provider.empty");
      assertEmptyWsdlCorrect(executedOperation);

      executedOperation = createWebServiceProvider(root, false, MULTI_SERVICE_WSDL, jaxWsProject, "test.provider.multiservice");
      assertMultiServiceWsdlCorrect(executedOperation, false);
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  @Test
  public void testNewWebServicesProviderFirst() throws XPathExpressionException, CoreException, ParserConfigurationException, SAXException, IOException {
    File root = CoreScoutTestingUtils.createTestProject();
    try {
      WebServiceNewOperation executedOperation = createWebServiceProvider(root, true, MULTI_FILE_WSDL, null, "test.provider.multifile");
      assertMultiFileWsdlCorrect(executedOperation, false);
      IJavaProject jaxWsProject = executedOperation.getJaxWsProject();

      executedOperation = createWebServiceConsumer(root, false, MULTI_SERVICE_WSDL, jaxWsProject, "test.consumer.multiservice");
      assertMultiServiceWsdlCorrect(executedOperation, true);

      executedOperation = createEmptyWebServiceProvider(root, false, "Whatever", jaxWsProject, "test.provider.empty");
      assertEmptyWsdlCorrect(executedOperation);
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  protected static void assertEmptyWsdlCorrect(WebServiceNewOperation executedOperation) {
    Assert.assertEquals(1, executedOperation.getCreatedEntryPointDefinitions().size());
    Assert.assertEquals(1, executedOperation.getCreatedJaxwsBindingFiles().size());
    Assert.assertEquals(1, executedOperation.getCreatedProviderServiceImpls().size());
    Assert.assertEquals(0, executedOperation.getCreatedUrlProperties().size());
    Assert.assertEquals(0, executedOperation.getCreatedWebServiceClients().size());
  }

  protected static void assertMultiServiceWsdlCorrect(WebServiceNewOperation executedOperation, boolean isConsumer) {
    Assert.assertEquals(1, executedOperation.getCreatedJaxwsBindingFiles().size());
    Assert.assertEquals(isConsumer ? 0 : 4, executedOperation.getCreatedEntryPointDefinitions().size());
    Assert.assertEquals(isConsumer ? 0 : 4, executedOperation.getCreatedProviderServiceImpls().size());
    Assert.assertEquals(isConsumer ? 4 : 0, executedOperation.getCreatedUrlProperties().size());
    Assert.assertEquals(isConsumer ? 4 : 0, executedOperation.getCreatedWebServiceClients().size());
  }

  protected static void assertMultiFileWsdlCorrect(WebServiceNewOperation executedOperation, boolean isConsumer) {
    Assert.assertEquals(2, executedOperation.getCreatedJaxwsBindingFiles().size());
    Assert.assertEquals(isConsumer ? 0 : 1, executedOperation.getCreatedEntryPointDefinitions().size());
    Assert.assertEquals(isConsumer ? 0 : 1, executedOperation.getCreatedProviderServiceImpls().size());
    Assert.assertEquals(isConsumer ? 1 : 0, executedOperation.getCreatedUrlProperties().size());
    Assert.assertEquals(isConsumer ? 1 : 0, executedOperation.getCreatedWebServiceClients().size());
  }

  protected static WebServiceNewOperation createWebServiceConsumer(File root, boolean isCreateNewModule, URL wsdl, IJavaProject jaxWsProject, String pck)
      throws XPathExpressionException, CoreException, ParserConfigurationException, SAXException, IOException {
    return createWebService(root, true, false, isCreateNewModule, null, wsdl, jaxWsProject, pck);
  }

  protected static WebServiceNewOperation createEmptyWebServiceProvider(File root, boolean isCreateNewModule, String webServiceName, IJavaProject jaxWsProject, String pck)
      throws XPathExpressionException, CoreException, ParserConfigurationException, SAXException, IOException {
    return createWebService(root, false, true, isCreateNewModule, webServiceName, null, jaxWsProject, pck);
  }

  protected static WebServiceNewOperation createWebServiceProvider(File root, boolean isCreateNewModule, URL wsdl, IJavaProject jaxWsProject, String pck)
      throws XPathExpressionException, CoreException, ParserConfigurationException, SAXException, IOException {
    return createWebService(root, false, false, isCreateNewModule, null, wsdl, jaxWsProject, pck);
  }

  private static WebServiceNewOperation createWebService(File root, boolean isConsumer, boolean isEmptyProvider, boolean isCreateNewModule, String wsdlName, URL wsdl, IJavaProject jaxWsProject, String pck)
      throws CoreException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    final FileSystemMockFactory factory = new FileSystemMockFactory();
    final File serverModule = new File(root, CoreScoutTestingUtils.PROJECT_ARTIFACT_ID + File.separatorChar + CoreScoutTestingUtils.PROJECT_ARTIFACT_ID + ".server");
    final IJavaProject serverJavaProject = factory.createJavaProject(serverModule);
    final IJavaEnvironmentProvider provider = factory.createJavaEnvProvider();
    final WebServiceNewOperation op = new WebServiceNewOperation() {
      @Override
      protected IJavaProject createNewJaxWsModule(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        try {
          File createdProjectDir = CoreScoutTestingUtils.createJaxWsModule(serverModule, getArtifactId());
          return factory.createJavaProject(createdProjectDir);
        }
        catch (IOException e) {
          throw new CoreException(new ScoutStatus(e));
        }
      }

      @Override
      protected void setIgnoreOptionalProblems(String entryPath, IProgressMonitor monitor) throws JavaModelException {
        // nop
      }

      @Override
      protected void enableApt(String lineDelimiter, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        // nop
      }

      @Override
      protected IJavaEnvironment createNewEnv() {
        return provider.get(getJaxWsProject());
      }

      @Override
      protected void createDerivedResources(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
        CoreScoutTestingUtils.runMavenCleanCompile(getJaxWsProject().getProject().getLocation().toFile());
      }
    };
    op.setCreateConsumer(isConsumer);
    op.setCreateEmptyWsdl(isEmptyProvider);
    if (isEmptyProvider) {
      op.setWsdlName(wsdlName);
    }
    else {
      op.setWsdlUrl(wsdl);
    }
    op.setCreateNewModule(isCreateNewModule);
    String jaxWsArtifactId = CoreScoutTestingUtils.PROJECT_ARTIFACT_ID + ".server.jaxws";
    if (isCreateNewModule) {
      op.setServerModule(serverJavaProject);
      op.setArtifactId(jaxWsArtifactId);
    }
    else {
      op.setJaxWsProject(jaxWsProject);
    }
    op.setPackage(pck);
    op.validate();
    op.run(new NullProgressMonitor(), ScoutSdkCore.createWorkingCopyManager());

    final File parentModule = new File(root, CoreScoutTestingUtils.PROJECT_ARTIFACT_ID + File.separatorChar + CoreScoutTestingUtils.PROJECT_ARTIFACT_ID);

    // ensure the module is present in the parent pom
    assertJaxWsModulePresent(parentModule, jaxWsArtifactId);

    // ensure the module is present in the server dependencies
    assertServerModuleHasJaxWsDependency(serverModule, jaxWsArtifactId);

    // ensure the generated resources compile
    CoreScoutTestingUtils.runMavenCleanTest(parentModule);

    return op;
  }

  private static void assertServerModuleHasJaxWsDependency(File serverModuleDir, String jaxWsArtifactId) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
    Document doc = docBuilder.parse(new File(serverModuleDir, IMavenConstants.POM));
    final String prefix = "p";
    final String p = prefix + ":";
    StringBuilder builder = new StringBuilder();
    builder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.DEPENDENCIES).append('/').append(p).append(IMavenConstants.DEPENDENCY)
        .append("[./").append(p).append(IMavenConstants.ARTIFACT_ID).append("='").append(jaxWsArtifactId).append("']");
    List<Element> elements = CoreUtils.evaluateXPath(builder.toString(), doc, prefix, IMavenConstants.POM_XML_NAMESPACE);
    if (elements.isEmpty()) {
      Assert.fail("Jax Ws Module has not been added to the dependencies of the server module.");
    }
  }

  private static void assertJaxWsModulePresent(File parentModuleDir, String jaxWsArtifactId) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
    Document doc = docBuilder.parse(new File(parentModuleDir, IMavenConstants.POM));
    final String prefix = "p";
    final String p = prefix + ":";
    StringBuilder builder = new StringBuilder();
    builder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.MODULES)
        .append("[./").append(p).append(IMavenConstants.MODULE).append("='../").append(jaxWsArtifactId).append("']");
    List<Element> elements = CoreUtils.evaluateXPath(builder.toString(), doc, prefix, IMavenConstants.POM_XML_NAMESPACE);
    if (elements.isEmpty()) {
      Assert.fail("Jax Ws Module has not been added to the parent pom modules.");
    }
  }
}
