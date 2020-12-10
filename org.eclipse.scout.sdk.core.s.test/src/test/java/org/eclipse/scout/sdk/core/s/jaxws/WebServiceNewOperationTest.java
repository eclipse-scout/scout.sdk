/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.wsdl.WSDLException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.s.testing.ScoutJavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentBuilder;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Xml;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link WebServiceNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceNewOperationTest {

  protected static final URL MULTI_FILE_WSDL = Ensure.notNull(WebServiceNewOperationTest.class.getClassLoader().getResource("testws/multi_file/MultiFile.wsdl"));
  protected static final URL MULTI_SERVICE_WSDL = Ensure.notNull(WebServiceNewOperationTest.class.getClassLoader().getResource("testws/multi_service/MultiService.wsdl"));
  protected static final URL RPC_ENCODED_WSDL = Ensure.notNull(WebServiceNewOperationTest.class.getClassLoader().getResource("testws/encoded/unsupported.wsdl"));

  @Test
  public void testUnsupportedService() throws IOException, WSDLException, URISyntaxException {
    try (var in = RPC_ENCODED_WSDL.openStream()) {
      var info = ParsedWsdl.create(RPC_ENCODED_WSDL.toURI(), in, true);
      assertTrue(info.isEmpty());
    }
  }

  @Test
  @Tag("IntegrationTest")
  public void testNewWebServicesConsumerFirst() throws IOException {
    var root = CoreScoutTestingUtils.createClassicTestProject();
    try {
      var projectRoot = new AtomicReference<Path>();
      runCreateJaxWsModule(env -> {
        var executedOperation = createWebServiceConsumer(root, true, MULTI_FILE_WSDL, "test.consumer", env);
        assertMultiFileWsdlCorrect(executedOperation, true);
        projectRoot.set(executedOperation.getProjectRoot());
      });

      runInExistingJaxWsModule(projectRoot.get(), env -> {
        var executedOperation = createEmptyWebServiceProvider(root, false, "TestEmpty" + ISdkConstants.SUFFIX_WS_PROVIDER, "test.provider.empty", env);
        assertEmptyWsdlCorrect(executedOperation);
      });
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  @Test
  @Tag("IntegrationTest")
  public void testNewWebServicesProviderFirst() throws IOException {
    var root = CoreScoutTestingUtils.createClassicTestProject();
    try {
      var projectRoot = new AtomicReference<Path>();
      runCreateJaxWsModule(env -> {
        var executedOperation = createWebServiceProvider(root, true, MULTI_FILE_WSDL, "test.provider.multifile", env);
        assertMultiFileWsdlCorrect(executedOperation, false);
        projectRoot.set(executedOperation.getProjectRoot());
      });

      runInExistingJaxWsModule(projectRoot.get(), env -> {
        var executedOperation = createWebServiceConsumer(root, false, MULTI_SERVICE_WSDL, "test.consumer.multiservice", env);
        assertMultiServiceWsdlCorrect(executedOperation, true);
      });
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  static void runCreateJaxWsModule(Consumer<TestingEnvironment> consumer) {
    new TestingEnvironmentBuilder()
        .withFlushResourcesToDisk(true)
        .run(consumer);
  }

  private static void runInExistingJaxWsModule(Path path, Consumer<TestingEnvironment> consumer) {
    new TestingEnvironmentBuilder()
        .withFlushResourcesToDisk(true)
        .withPrimaryEnvironment(task -> new ScoutJavaEnvironmentWithEcjBuilder<>()
            .withScoutClient(false)
            .withScoutHtmlUi(false)
            .withAbsoluteSourcePath(path.resolve(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(path.resolve(ISourceFolders.GENERATED_WSIMPORT_SOURCE_FOLDER).toString())
            .withAbsoluteSourcePath(path.resolve(ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER).toString())
            .accept(task))
        .run(consumer);
  }

  private static void assertEmptyWsdlCorrect(AbstractWebServiceNewOperation executedOperation) {
    assertEquals(1, executedOperation.getCreatedEntryPointDefinitions().size());
    assertEquals(1, executedOperation.getCreatedJaxwsBindingFiles().size());
    assertEquals(1, executedOperation.getCreatedProviderServiceImpls().size());
    assertEquals(0, executedOperation.getCreatedUrlProperties().size());
    assertEquals(0, executedOperation.getCreatedWebServiceClients().size());
  }

  private static void assertMultiServiceWsdlCorrect(AbstractWebServiceNewOperation executedOperation, boolean isConsumer) {
    assertEquals(1, executedOperation.getCreatedJaxwsBindingFiles().size());
    assertEquals(isConsumer ? 0 : 4, executedOperation.getCreatedEntryPointDefinitions().size());
    assertEquals(isConsumer ? 0 : 4, executedOperation.getCreatedProviderServiceImpls().size());
    assertEquals(isConsumer ? 4 : 0, executedOperation.getCreatedUrlProperties().size());
    assertEquals(isConsumer ? 4 : 0, executedOperation.getCreatedWebServiceClients().size());
  }

  private static void assertMultiFileWsdlCorrect(AbstractWebServiceNewOperation executedOperation, boolean isConsumer) {
    assertEquals(2, executedOperation.getCreatedJaxwsBindingFiles().size());
    assertEquals(isConsumer ? 0 : 1, executedOperation.getCreatedEntryPointDefinitions().size());
    assertEquals(isConsumer ? 0 : 1, executedOperation.getCreatedProviderServiceImpls().size());
    assertEquals(isConsumer ? 1 : 0, executedOperation.getCreatedUrlProperties().size());
    assertEquals(isConsumer ? 1 : 0, executedOperation.getCreatedWebServiceClients().size());
  }

  protected static AbstractWebServiceNewOperation createWebServiceConsumer(Path root, boolean isCreateNewModule, URL wsdl, String pck, TestingEnvironment env) {
    return createWebService(root, true, false, isCreateNewModule, null, wsdl, pck, env);
  }

  protected static AbstractWebServiceNewOperation createEmptyWebServiceProvider(Path root, boolean isCreateNewModule, String webServiceName, String pck, TestingEnvironment env) {
    return createWebService(root, false, true, isCreateNewModule, webServiceName, null, pck, env);
  }

  protected static AbstractWebServiceNewOperation createWebServiceProvider(Path root, boolean isCreateNewModule, URL wsdl, String pck, TestingEnvironment env) {
    return createWebService(root, false, false, isCreateNewModule, null, wsdl, pck, env);
  }

  /**
   * Ensures the target/generated-sources/... folders exist in the given project. This is required that during the
   * creation of the {@link IJavaEnvironment} the folders are available to be part of the classpath!
   */
  protected static void ensureGeneratedSourceFoldersExist(Path root) {
    try {
      Files.createDirectories(root.resolve(ISourceFolders.GENERATED_WSIMPORT_SOURCE_FOLDER));
      Files.createDirectories(root.resolve(ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER));
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  private static AbstractWebServiceNewOperation createWebService(Path root, boolean isConsumer, boolean isEmptyProvider, boolean isCreateNewModule,
      String wsdlName, URL wsdl, String pck, TestingEnvironment env) {
    var jaxWsArtifactId = CoreScoutTestingUtils.PROJECT_ARTIFACT_ID + ".server.jaxws";
    var serverModule = root.resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID).resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID + ".server");
    var jaxWsModule = root.resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID).resolve(jaxWsArtifactId);

    var op = new AbstractWebServiceNewOperation() {
      @Override
      protected Path createNewJaxWsModule(IEnvironment e, IProgress progress) {
        try {
          var createdProjectDir = CoreScoutTestingUtils.createJaxWsModule(serverModule, jaxWsArtifactId, env);
          ensureGeneratedSourceFoldersExist(createdProjectDir);
          return createdProjectDir;
        }
        catch (IOException ex) {
          throw new SdkException(ex);
        }
      }

      @Override
      protected void createEntryPointDefinitions(IEnvironment e, IProgress p) {
        createDerivedResources(e, p); // already create the resources here the first time so that port type is available to be validated in the entry point definition.
        super.createEntryPointDefinitions(e, p);
      }

      @Override
      protected void createDerivedResources(IEnvironment e, IProgress progress) {
        CoreScoutTestingUtils.runMavenCommand(getProjectRoot(), "clean", "process-resources");
        ensureGeneratedSourceFoldersExist(getProjectRoot());
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

    if (!isCreateNewModule) {
      op.setProjectRoot(jaxWsModule);
      op.setSourceFolder(env.getTestingSourceFolder());
    }
    op.setPackage(pck);
    env.run(op);

    var parentModule = root.resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID).resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID);

    // ensure the module is present in the parent pom
    assertJaxWsModulePresent(parentModule, jaxWsArtifactId);

    // ensure the module is present in the server dependencies
    assertServerModuleHasJaxWsDependency(serverModule, jaxWsArtifactId);

    // ensure the generated resources compile
    CoreScoutTestingUtils.runMavenCleanTest(parentModule);

    return op;
  }

  private static void assertServerModuleHasJaxWsDependency(Path serverModuleDir, String jaxWsArtifactId) {
    try {
      var doc = Xml.get(serverModuleDir.resolve(IMavenConstants.POM));
      var prefix = "p";
      var p = prefix + ':';
      var builder = new StringBuilder();
      builder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.DEPENDENCIES).append('/').append(p).append(IMavenConstants.DEPENDENCY)
          .append("[./").append(p).append(IMavenConstants.ARTIFACT_ID).append("='").append(jaxWsArtifactId).append("']");
      var elements = Xml.evaluateXPath(builder.toString(), doc, prefix, IMavenConstants.POM_XML_NAMESPACE);
      if (elements.isEmpty()) {
        fail("Jax Ws Module has not been added to the dependencies of the server module.");
      }
    }
    catch (IOException | XPathExpressionException e) {
      throw new SdkException(e);
    }
  }

  private static void assertJaxWsModulePresent(Path parentModuleDir, String jaxWsArtifactId) {
    try {
      var doc = Xml.get(parentModuleDir.resolve(IMavenConstants.POM));
      var prefix = "p";
      var p = prefix + ':';
      var builder = new StringBuilder();
      builder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.MODULES)
          .append("[./").append(p).append(IMavenConstants.MODULE).append("='../").append(jaxWsArtifactId).append("']");
      var elements = Xml.evaluateXPath(builder.toString(), doc, prefix, IMavenConstants.POM_XML_NAMESPACE);
      if (elements.isEmpty()) {
        fail("Jax Ws Module has not been added to the parent pom modules.");
      }
    }
    catch (IOException | XPathExpressionException e) {
      throw new SdkException(e);
    }
  }
}
