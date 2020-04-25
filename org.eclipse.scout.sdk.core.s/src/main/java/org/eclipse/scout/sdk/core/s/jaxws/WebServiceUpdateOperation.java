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

import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.generator.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link WebServiceUpdateOperation}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceUpdateOperation implements BiConsumer<IEnvironment, IProgress> {

  private String m_package;
  private final Collection<Path> m_jaxwsBindingFiles;
  private final Collection<BindingClassUpdate> m_bindingClassUpdates;
  private final Collection<EntryPointDefinitionUpdate> m_entryPointDefinitionUpdates;
  private final Collection<WebServiceClientUpdate> m_webServiceClientUpdates;
  private final Collection<WebServiceImplementationUpdate> m_webServiceImplUpdates;

  public WebServiceUpdateOperation() {
    m_jaxwsBindingFiles = new ArrayList<>();
    m_bindingClassUpdates = new ArrayList<>();
    m_entryPointDefinitionUpdates = new ArrayList<>();
    m_webServiceClientUpdates = new ArrayList<>();
    m_webServiceImplUpdates = new ArrayList<>();
  }

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    progress.init(40, toString());
    updateJaxWsBinding(env, progress.newChild(10));
    updateWebServiceImpls(env, progress.newChild(10));
    updateEntryPointDefinitions(env, progress.newChild(10));
    updateWebServiceClients(env, progress.newChild(10));
  }

  protected void updateWebServiceImpls(IEnvironment env, IProgress progress) {
    if (m_webServiceImplUpdates.isEmpty()) {
      return;
    }

    for (WebServiceImplementationUpdate up : m_webServiceImplUpdates) {
      ICompilationUnit wsImpl = up.getWebServiceImpl().requireCompilationUnit();
      ICompilationUnitGenerator<?> builder = wsImpl.toWorkingCopy();

      builder.mainType().ifPresent(mainType -> {
        // remove the old import to port type
        mainType.interfaces().forEach(builder::withoutImport);

        // remove old port type super interface
        removePortTypeSuperInterfaces(mainType);

        // new super interface
        String newPortTypeFqn = up.getPackage() + JavaTypes.C_DOT + up.getPortTypeName();
        mainType.withInterface(newPortTypeFqn);
      });

      // update wsdl faults (exceptions)
      updateWsdlFaults(builder, up.getPackage());

      env.writeCompilationUnit(builder, up.getSourceFolder(), progress);
    }
  }

  protected static void updateWsdlFaults(ICompilationUnitGenerator<?> icuBuilder, String newPackage) {
    icuBuilder
        .mainType()
        .ifPresent(t -> t.methods().forEach(methodBuilder -> {
          List<String> exceptions = methodBuilder.exceptions().collect(toList());
          for (String exc : exceptions) {
            methodBuilder
                .withoutException(exc)
                .withException(newPackage + JavaTypes.C_DOT + JavaTypes.simpleName(exc));
            icuBuilder.withoutImport(exc);
          }
        }));
  }

  protected void updateWebServiceClients(IEnvironment env, IProgress progress) {
    if (m_webServiceClientUpdates.isEmpty()) {
      return;
    }

    for (WebServiceClientUpdate up : m_webServiceClientUpdates) {
      ICompilationUnit wsClient = up.getWebServiceClient().requireCompilationUnit();
      ICompilationUnitGenerator<?> builder = wsClient.toWorkingCopy();

      builder.mainType()
          .ifPresent(mainType -> {
            // remove the old imports
            for (String oldImport : JavaTypes.typeArguments(mainType.superClass().get())) {
              builder.withoutImport(oldImport);
            }

            // remove old port type super interface
            removePortTypeSuperInterfaces(mainType);

            // set new super class and super interface
            String newPortTypeFqn = up.getPackage() + JavaTypes.C_DOT + up.getPortTypeName();
            String newWebServiceFqn = up.getPackage() + JavaTypes.C_DOT + up.getWebServiceName();
            StringBuilder superTypeFqnBuilder = new StringBuilder(IScoutRuntimeTypes.AbstractWebServiceClient);
            superTypeFqnBuilder.append(JavaTypes.C_GENERIC_START).append(newWebServiceFqn).append(", ").append(newPortTypeFqn).append(JavaTypes.C_GENERIC_END);
            mainType
                .withInterface(newPortTypeFqn)
                .withSuperClass(superTypeFqnBuilder.toString());
          });

      // update wsdl faults (exceptions)
      updateWsdlFaults(builder, up.getPackage());
      env.writeCompilationUnit(builder, up.getSourceFolder(), progress);
    }
  }

  protected static void removePortTypeSuperInterfaces(ITypeGenerator<?> generator) {
    generator.interfaces()
        .filter(ifc -> ifc.endsWith(ISdkProperties.SUFFIX_WS_PORT_TYPE))
        .forEach(generator::withoutInterface);
  }

  protected void updateJaxWsBinding(IEnvironment env, IProgress progress) {
    Collection<Path> jaxwsBindingFiles = getJaxwsBindingFiles();
    if (jaxwsBindingFiles.isEmpty()) {
      return;
    }

    progress.init(jaxwsBindingFiles.size() * 2, "Update Jax-Ws Bindings");
    try {
      for (Path jaxwsBindingFile : jaxwsBindingFiles) {
        Document document = Xml.get(jaxwsBindingFile);
        String prefix = document.lookupPrefix(JaxWsUtils.JAX_WS_NAMESPACE);
        for (BindingClassUpdate up : m_bindingClassUpdates) {
          Element nodeElement = JaxWsUtils.getJaxWsBindingElement(up.getNodeValue(), document);
          Xml.firstChildElement(nodeElement, JaxWsUtils.BINDINGS_CLASS_ELEMENT_NAME)
              .ifPresent(e -> e.setAttribute(JaxWsUtils.BINDINGS_NAME_ATTRIBUTE, up.getClassName()));
        }

        // package
        Element packageElement = JaxWsUtils.getJaxWsBindingElement(JaxWsUtils.PACKAGE_XPATH, document);
        if (packageElement == null) {
          packageElement = document.createElement(prefix + ':' + JaxWsUtils.BINDINGS_ELEMENT_NAME);
          packageElement.setAttribute(JaxWsUtils.BINDINGS_NODE_ATTRIBUTE_NAME, JaxWsUtils.PACKAGE_XPATH);
          document.getDocumentElement().appendChild(packageElement);
        }
        Element packageNameElement = Xml.firstChildElement(packageElement, JaxWsUtils.BINDING_PACKAGE_ELEMENT_NAME)
            .orElseGet(() -> {
              Element e = document.createElement(prefix + ':' + JaxWsUtils.BINDING_PACKAGE_ELEMENT_NAME);
              e.appendChild(e);
              return e;
            });
        packageNameElement.setAttribute(JaxWsUtils.BINDINGS_NAME_ATTRIBUTE, getPackage());
        progress.worked(1);

        env.writeResource(Xml.documentToString(document, true), jaxwsBindingFile, progress.newChild(1));
      }
    }
    catch (XPathExpressionException | TransformerException | IOException e) {
      throw new SdkException(e);
    }
  }

  protected void updateEntryPointDefinitions(IEnvironment env, IProgress progress) {
    for (EntryPointDefinitionUpdate up : m_entryPointDefinitionUpdates) {
      IType entryPointDefinition = up.getEntryPointDefinition();
      if (entryPointDefinition == null) {
        continue;
      }

      ICompilationUnit definition = entryPointDefinition.requireCompilationUnit();
      IWorkingCopyTransformer transformer = new SimpleWorkingCopyTransformerBuilder()
          .withAnnotationElementMapper(input -> rewriteEntryPointDefinitionAnnotationElements(input, up))
          .build();
      ICompilationUnitGenerator<?> builder = definition.toWorkingCopy(transformer);
      env.writeCompilationUnit(builder, up.getSourceFolder(), progress);
    }
  }

  protected static ISourceGenerator<IExpressionBuilder<?>> rewriteEntryPointDefinitionAnnotationElements(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input, EntryPointDefinitionUpdate up) {
    if (IScoutRuntimeTypes.WebServiceEntryPoint.equals(input.model().declaringAnnotation().name())) {
      if (JaxWsUtils.ENTRY_POINT_DEFINITION_ENDPOINTINTERFACE_ATTRIBUTE.equals(input.model().elementName())) {
        String newPortTypeFqn = up.getPortTypePackage() + JavaTypes.C_DOT + up.getPortTypeName();
        return b -> b.classLiteral(newPortTypeFqn);
      }
      if (JaxWsUtils.ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE.equals(input.model().elementName())) {
        return b -> b.stringLiteral(up.getEntryPointName());
      }
      if (JaxWsUtils.ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE.equals(input.model().elementName())) {
        return b -> b.stringLiteral(up.getEntryPointPackage());
      }
    }
    return input.requestDefaultWorkingCopy();
  }

  public Collection<Path> getJaxwsBindingFiles() {
    return unmodifiableCollection(m_jaxwsBindingFiles);
  }

  public void setJaxwsBindingFiles(Collection<? extends Path> files) {
    m_jaxwsBindingFiles.addAll(files);
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public void addWebServiceImplementationUpdate(WebServiceImplementationUpdate up) {
    m_webServiceImplUpdates.add(up);
  }

  public void addBindingClassUpdate(BindingClassUpdate up) {
    m_bindingClassUpdates.add(up);
  }

  public void addEntryPointDefinitionUpdate(EntryPointDefinitionUpdate up) {
    m_entryPointDefinitionUpdates.add(up);
  }

  public void addWebServiceClientUpdate(WebServiceClientUpdate up) {
    m_webServiceClientUpdates.add(up);
  }

  public static class WebServiceClientUpdate {
    private final IType m_webServiceClient;
    private final String m_package;
    private final String m_portTypeName;
    private final String m_webServiceName;
    private final IClasspathEntry m_sourceFolder;

    public WebServiceClientUpdate(IType webServiceClient, String pck, String portTypeName, String webServiceName, IClasspathEntry sourceFolder) {
      m_webServiceClient = webServiceClient;
      m_package = pck;
      m_portTypeName = portTypeName;
      m_webServiceName = webServiceName;
      m_sourceFolder = sourceFolder;
    }

    public IType getWebServiceClient() {
      return m_webServiceClient;
    }

    public String getPackage() {
      return m_package;
    }

    public String getPortTypeName() {
      return m_portTypeName;
    }

    public String getWebServiceName() {
      return m_webServiceName;
    }

    public IClasspathEntry getSourceFolder() {
      return m_sourceFolder;
    }
  }

  public static class WebServiceImplementationUpdate {
    private final IType m_webServiceImpl;
    private final String m_package;
    private final String m_portTypeName;
    private final IClasspathEntry m_sourceFolder;

    public WebServiceImplementationUpdate(IType webServiceImpl, String package1, String portTypeName, IClasspathEntry sourceFolder) {
      m_webServiceImpl = webServiceImpl;
      m_package = package1;
      m_portTypeName = portTypeName;
      m_sourceFolder = sourceFolder;
    }

    public IType getWebServiceImpl() {
      return m_webServiceImpl;
    }

    public String getPackage() {
      return m_package;
    }

    public String getPortTypeName() {
      return m_portTypeName;
    }

    public IClasspathEntry getSourceFolder() {
      return m_sourceFolder;
    }
  }

  public static class BindingClassUpdate {
    private String m_nodeValue;
    private String m_className;

    public BindingClassUpdate(String nodeValue, String className) {
      m_nodeValue = nodeValue;
      m_className = className;
    }

    public String getNodeValue() {
      return m_nodeValue;
    }

    public void setNodeValue(String nodeValue) {
      m_nodeValue = nodeValue;
    }

    public String getClassName() {
      return m_className;
    }

    public void setClassName(String className) {
      m_className = className;
    }
  }

  public static class EntryPointDefinitionUpdate {
    private final IType m_entryPointDefinition;
    private final String m_entryPointPackage;
    private final String m_entryPointName;
    private final String m_portTypeName;
    private final String m_portTypePackage;
    private final IClasspathEntry m_sourceFolder;

    public EntryPointDefinitionUpdate(IType entryPointDefinitions, String entryPointPackage, String entryPointName, String portTypeName, String portTypePackage, IClasspathEntry sourceFolder) {
      m_entryPointDefinition = entryPointDefinitions;
      m_entryPointPackage = entryPointPackage;
      m_entryPointName = entryPointName;
      m_portTypeName = portTypeName;
      m_portTypePackage = portTypePackage;
      m_sourceFolder = sourceFolder;
    }

    public IType getEntryPointDefinition() {
      return m_entryPointDefinition;
    }

    public String getEntryPointPackage() {
      return m_entryPointPackage;
    }

    public String getEntryPointName() {
      return m_entryPointName;
    }

    public String getPortTypeName() {
      return m_portTypeName;
    }

    public String getPortTypePackage() {
      return m_portTypePackage;
    }

    public IClasspathEntry getSourceFolder() {
      return m_sourceFolder;
    }
  }

  @Override
  public String toString() {
    return "Update Web Service";
  }
}
