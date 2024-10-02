/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.BindingClassUpdate;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.EntryPointDefinitionUpdate;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.WebServiceClientUpdate;
import org.eclipse.scout.sdk.core.s.jaxws.WebServiceUpdateOperation.WebServiceImplementationUpdate;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link WebServiceUpdateOperationTest}</h3>
 *
 * @since 5.2.0
 */
@Tag("IntegrationTest")
public class WebServiceUpdateOperationTest {

  private static final String PORT_TYPE_NAME_IN_WSDL = "scoutQueryInterface2";
  private static final String WEB_SERVICE_NAME_IN_WSDL = "scoutQuery";
  private static final String ORIGINAL_PORT_TYPE_NAME = "IScoutQueryInterface2PortType";
  private static final String ORIGINAL_WEB_SERVICE_NAME = "ScoutQueryService";
  private static final String ORIGINAL_ENTRY_POINT_NAME = "ScoutQueryInterface2WebServiceEntryPoint";

  @Test
  public void testUpdateWebServiceConsumer() throws IOException {
    if ("25.1-SNAPSHOT".equals(CoreScoutTestingUtils.currentScoutVersion())) {
      return; // archetype currently not supported for 25.1 as this uses Java 21 and the current maven-master has no Java 21 capable compiler yet. Enable again as soon as the new maven-master is deployed.
    }
    var root = CoreScoutTestingUtils.createClassicTestProject();
    try {
      WebServiceNewOperationTest.runCreateJaxWsModule(testUpdateConsumer(root));
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  @Test
  public void testUpdateWebServiceProvider() throws IOException {
    if ("25.1-SNAPSHOT".equals(CoreScoutTestingUtils.currentScoutVersion())) {
      return; // archetype currently not supported for 25.1 as this uses Java 21 and the current maven-master has no Java 21 capable compiler yet. Enable again as soon as the new maven-master is deployed.
    }
    var root = CoreScoutTestingUtils.createClassicTestProject();
    try {
      WebServiceNewOperationTest.runCreateJaxWsModule(testUpdateProvider(root));
    }
    finally {
      if (root != null) {
        CoreUtils.deleteDirectory(root);
      }
    }
  }

  protected static Consumer<TestingEnvironment> testUpdateConsumer(Path root) {
    return env -> {
      // create project with web service
      var origPackage = "test.consumer.multifile";
      var wsCreateOp = WebServiceNewOperationTest.createWebServiceConsumer(root, true, WebServiceNewOperationTest.MULTI_FILE_WSDL, origPackage, env);
      var jaxwsProjectDirectory = wsCreateOp.getProjectRoot();
      var sourceFolder = wsCreateOp.getSourceFolder();
      var javaEnvironment = sourceFolder.javaEnvironment();
      var wsClient = javaEnvironment.requireType(origPackage + JavaTypes.C_DOT + "ScoutQueryInterface2WebServiceClient");

      // start update
      var newPortTypePackage = "test.consumer.newMultiFile";
      var newPortTypeName = "IChangedPortType";
      var newServiceName = "ChangedNameService";

      var wsUpdateOp = createWsUpdateOp(wsCreateOp);
      wsUpdateOp.setPackage(newPortTypePackage);
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getWebServiceXPath(WEB_SERVICE_NAME_IN_WSDL), newServiceName));
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getPortTypeXPath(PORT_TYPE_NAME_IN_WSDL), newPortTypeName));
      wsUpdateOp.addWebServiceClientUpdate(new WebServiceClientUpdate(wsClient, newPortTypePackage, newPortTypeName, newServiceName, sourceFolder));

      env.run(wsUpdateOp);

      // ensure the generated resources compile
      assertProjectCompiles(jaxwsProjectDirectory);

      // validate change
      assertFalse(javaEnvironment.findType(origPackage + JavaTypes.C_DOT + ORIGINAL_PORT_TYPE_NAME).isPresent());
      assertFalse(javaEnvironment.findType(origPackage + JavaTypes.C_DOT + ORIGINAL_WEB_SERVICE_NAME).isPresent());
      assertTrue(javaEnvironment.findType(newPortTypePackage + JavaTypes.C_DOT + newServiceName).isPresent());
      assertTrue(javaEnvironment.findType(newPortTypePackage + JavaTypes.C_DOT + newPortTypeName).isPresent());
    };
  }

  protected static WebServiceUpdateOperation createWsUpdateOp(AbstractWebServiceNewOperation wsCreateOp) {
    var wsUpdateOp = new WebServiceUpdateOperation() {
      @Override
      protected void updateJaxWsBinding(IEnvironment e, IProgress p) {
        super.updateJaxWsBinding(e, p);
        CoreScoutTestingUtils.runMavenCommand(wsCreateOp.getProjectRoot(), "clean", "process-resources");
        WebServiceNewOperationTest.ensureGeneratedSourceFoldersExist(wsCreateOp.getProjectRoot());
      }
    };
    wsUpdateOp.setJaxwsBindingFiles(wsCreateOp.getCreatedJaxwsBindingFiles());
    return wsUpdateOp;
  }

  protected static Consumer<TestingEnvironment> testUpdateProvider(Path root) {
    return env -> {
      // create project with web service
      var origPackage = "test.provider.multifile";
      var wsCreateOp = WebServiceNewOperationTest.createWebServiceProvider(root, true, WebServiceNewOperationTest.MULTI_FILE_WSDL, origPackage, env);
      var jaxwsProjectDirectory = wsCreateOp.getProjectRoot();
      var sourceFolder = wsCreateOp.getSourceFolder();
      var javaEnvironment = sourceFolder.javaEnvironment();
      var entryPointDef = javaEnvironment.requireType(origPackage + JavaTypes.C_DOT + "IScoutQueryInterface2WebServiceEntryPointDefinition");
      var serviceImpl = javaEnvironment.requireType(origPackage + JavaTypes.C_DOT + "ScoutQueryInterface2WebService");

      assertTrue(javaEnvironment.findType(origPackage + JavaTypes.C_DOT + ORIGINAL_PORT_TYPE_NAME).isPresent());

      // start update
      var newPortTypePackage = "test.provider.newMultiFile";
      var newPortTypeName = "IChangedPortType";
      var newEntryPointPackage = "entry.point.changedpackage";
      var newEntryPointName = "ChangedEntryPointName";
      var newServiceName = "ChangedNameService";

      var wsUpdateOp = createWsUpdateOp(wsCreateOp);
      wsUpdateOp.setPackage(newPortTypePackage);
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getWebServiceXPath(WEB_SERVICE_NAME_IN_WSDL), newServiceName));
      wsUpdateOp.addBindingClassUpdate(new BindingClassUpdate(JaxWsUtils.getPortTypeXPath(PORT_TYPE_NAME_IN_WSDL), newPortTypeName));
      wsUpdateOp.addEntryPointDefinitionUpdate(new EntryPointDefinitionUpdate(entryPointDef, newEntryPointPackage, newEntryPointName, newPortTypeName, newPortTypePackage, sourceFolder));
      wsUpdateOp.addWebServiceImplementationUpdate(new WebServiceImplementationUpdate(serviceImpl, newPortTypePackage, newPortTypeName, sourceFolder));

      env.run(wsUpdateOp);

      // ensure the generated resources compile
      assertProjectCompiles(jaxwsProjectDirectory);

      // validate change
      assertFalse(javaEnvironment.findType(origPackage + JavaTypes.C_DOT + ORIGINAL_PORT_TYPE_NAME).isPresent());
      assertFalse(javaEnvironment.findType(origPackage + JavaTypes.C_DOT + ORIGINAL_WEB_SERVICE_NAME).isPresent());
      assertFalse(javaEnvironment.findType(origPackage + JavaTypes.C_DOT + ORIGINAL_ENTRY_POINT_NAME).isPresent());
      assertTrue(javaEnvironment.findType(newEntryPointPackage + JavaTypes.C_DOT + newEntryPointName).isPresent());
      assertTrue(javaEnvironment.findType(newPortTypePackage + JavaTypes.C_DOT + newServiceName).isPresent());
      assertTrue(javaEnvironment.findType(newPortTypePackage + JavaTypes.C_DOT + newPortTypeName).isPresent());

    };
  }

  protected static void assertProjectCompiles(Path jaxwsProjectDirectory) {
    try {
      CoreUtils.deleteDirectory(jaxwsProjectDirectory.resolve("target"));
    }
    catch (IOException e) {
      SdkLog.warning("Unable to delete directory '{}'.", jaxwsProjectDirectory, e);
    }

    var parentDir = jaxwsProjectDirectory.getParent().resolve(CoreScoutTestingUtils.PROJECT_ARTIFACT_ID);
    CoreScoutTestingUtils.runMavenCleanVerify(parentDir);
  }
}
