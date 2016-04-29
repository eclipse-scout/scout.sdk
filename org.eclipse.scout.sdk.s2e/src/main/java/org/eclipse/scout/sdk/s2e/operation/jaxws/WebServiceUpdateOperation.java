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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link WebServiceUpdateOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceUpdateOperation implements IOperation {

  private String m_package;
  private final Collection<IFile> m_jaxwsBindingFiles;
  private final Collection<BindingClassUpdate> m_bindingClassUpdates;
  private final Collection<EntryPointDefinitionUpdate> m_entryPointDefinitionUpdates;
  private final Collection<WebServiceClientUpdate> m_webServiceClientUpdates;
  private final Collection<WebServiceImplementationUpdate> m_webServiceImplUpdates;
  private final IJavaEnvironmentProvider m_provider;

  public WebServiceUpdateOperation() {
    this(new CachingJavaEnvironmentProvider());
  }

  protected WebServiceUpdateOperation(IJavaEnvironmentProvider provider) {
    m_jaxwsBindingFiles = new LinkedList<>();
    m_bindingClassUpdates = new LinkedList<>();
    m_entryPointDefinitionUpdates = new LinkedList<>();
    m_webServiceClientUpdates = new LinkedList<>();
    m_webServiceImplUpdates = new LinkedList<>();
    m_provider = provider;
  }

  @Override
  public String getOperationName() {
    return "Update Web Service";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    final SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 40);
    if (progress.isCanceled()) {
      return;
    }

    updateJaxWsBinding(progress.newChild(10), workingCopyManager);
    updateEntryPointDefinitions(progress.newChild(10), workingCopyManager);
    updateWebServiceClients(progress.newChild(10), workingCopyManager);
    updateWebServiceImpls(progress.newChild(10), workingCopyManager);

    // explicitly flush all modified java files to disk so that the maven compiler can see the changes
    // the maven compiler is executed as part of the RebuildArtifactsOperation after all resource updates.
    // see org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws.WebServiceEditor.doSave(IProgressMonitor)
    workingCopyManager.unregisterAll(monitor, true);
  }

  protected void updateWebServiceImpls(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    if (m_webServiceImplUpdates.isEmpty()) {
      return;
    }

    for (WebServiceImplementationUpdate up : m_webServiceImplUpdates) {
      ICompilationUnit wsImpl = S2eUtils.jdtTypeToScoutType(up.getWebServiceImpl(), m_provider.get(up.getWebServiceImpl().getJavaProject())).compilationUnit();
      ICompilationUnitSourceBuilder builder = new CompilationUnitSourceBuilder(wsImpl);

      // remove the old import to port type
      ITypeSourceBuilder mainType = builder.getMainType();
      for (String oldIfcSig : mainType.getInterfaceSignatures()) {
        builder.removeDeclaredImport(Signature.toString(oldIfcSig));
      }

      // update wsdl faults (exceptions)
      updateWsdlFaults(builder, up.getPackage());

      String newPortTypeFqn = up.getPackage() + '.' + up.getPortTypeName();
      mainType.setInterfaceSignatures(Collections.singletonList(Signature.createTypeSignature(newPortTypeFqn)));

      S2eUtils.writeType((IPackageFragmentRoot) up.getWebServiceImpl().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), builder, wsImpl.javaEnvironment(), monitor, workingCopyManager);
    }
  }

  protected static void updateWsdlFaults(ICompilationUnitSourceBuilder icuBuilder, String newPackage) {
    for (IMethodSourceBuilder msb : icuBuilder.getMainType().getMethods()) {
      List<String> exceptionSignatures = new ArrayList<>(msb.getExceptionSignatures());
      for (String excSig : exceptionSignatures) {
        msb.removeExceptionSignature(excSig);
        msb.addExceptionSignature(Signature.createTypeSignature(newPackage + '.' + Signature.getSignatureSimpleName(excSig)));
        icuBuilder.removeDeclaredImport(Signature.toString(excSig));
      }
    }
  }

  protected void updateWebServiceClients(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    if (m_webServiceClientUpdates.isEmpty()) {
      return;
    }

    for (WebServiceClientUpdate up : m_webServiceClientUpdates) {
      ICompilationUnit wsClient = S2eUtils.jdtTypeToScoutType(up.getWebServiceClient(), m_provider.get(up.getWebServiceClient().getJavaProject())).compilationUnit();
      ICompilationUnitSourceBuilder builder = new CompilationUnitSourceBuilder(wsClient);

      // remove the old imports
      ITypeSourceBuilder mainType = builder.getMainType();
      String[] typeParameters = Signature.getTypeArguments(mainType.getSuperTypeSignature());
      for (String oldImport : typeParameters) {
        builder.removeDeclaredImport(Signature.toString(oldImport));
      }

      String newPortTypeFqn = up.getPackage() + '.' + up.getPortTypeName();
      String newWebServiceFqn = up.getPackage() + '.' + up.getWebServiceName();
      mainType.setInterfaceSignatures(Collections.singletonList(Signature.createTypeSignature(newPortTypeFqn)));
      StringBuilder superTypeFqnBuilder = new StringBuilder(IScoutRuntimeTypes.AbstractWebServiceClient);
      superTypeFqnBuilder.append(ISignatureConstants.C_GENERIC_START).append(newWebServiceFqn).append(", ").append(newPortTypeFqn).append(ISignatureConstants.C_GENERIC_END);
      mainType.setSuperTypeSignature(Signature.createTypeSignature(superTypeFqnBuilder.toString()));

      // update wsdl faults (exceptions)
      updateWsdlFaults(builder, up.getPackage());

      S2eUtils.writeType((IPackageFragmentRoot) up.getWebServiceClient().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), builder, wsClient.javaEnvironment(), monitor, workingCopyManager);
    }
  }

  protected void updateJaxWsBinding(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getJaxwsBindingFiles().isEmpty()) {
      return;
    }

    for (IFile jaxwsBindingFile : getJaxwsBindingFiles()) {
      Document document = S2eUtils.readXmlDocument(jaxwsBindingFile);
      String prefix = document.lookupPrefix(JaxWsUtils.JAX_WS_NAMESPACE);
      try {
        for (BindingClassUpdate up : m_bindingClassUpdates) {
          Element nodeElement = JaxWsUtils.getJaxWsBindingElement(up.getNodeValue(), document);
          Element portTypeNameClassElement = CoreUtils.getFirstChildElement(nodeElement, JaxWsUtils.BINDINGS_CLASS_ELEMENT_NAME);
          if (portTypeNameClassElement != null) {
            portTypeNameClassElement.setAttribute(JaxWsUtils.BINDINGS_NAME_ATTRIBUTE, up.getClassName());
          }
        }

        // package
        Element packageElement = JaxWsUtils.getJaxWsBindingElement(JaxWsUtils.PACKAGE_XPATH, document);
        if (packageElement == null) {
          packageElement = document.createElement(prefix + ':' + JaxWsUtils.BINDINGS_ELEMENT_NAME);
          packageElement.setAttribute(JaxWsUtils.BINDINGS_NODE_ATTRIBUTE_NAME, JaxWsUtils.PACKAGE_XPATH);
          document.getDocumentElement().appendChild(packageElement);
        }
        Element packageNameElement = CoreUtils.getFirstChildElement(packageElement, JaxWsUtils.BINDING_PACKAGE_ELEMENT_NAME);
        if (packageNameElement == null) {
          packageNameElement = document.createElement(prefix + ':' + JaxWsUtils.BINDING_PACKAGE_ELEMENT_NAME);
          packageElement.appendChild(packageNameElement);
        }
        packageNameElement.setAttribute(JaxWsUtils.BINDINGS_NAME_ATTRIBUTE, getPackage());
      }
      catch (XPathExpressionException e) {
        throw new CoreException(new ScoutStatus(e));
      }
      S2eUtils.writeXmlDocument(document, jaxwsBindingFile, monitor, workingCopyManager);
    }
  }

  protected void updateEntryPointDefinitions(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    for (EntryPointDefinitionUpdate up : m_entryPointDefinitionUpdates) {
      IType entryPointDefinition = up.getEntryPointDefinition();
      if (!S2eUtils.exists(entryPointDefinition)) {
        continue;
      }

      IJavaEnvironment env = m_provider.get(entryPointDefinition.getJavaProject());
      ICompilationUnit definition = S2eUtils.jdtTypeToScoutType(entryPointDefinition, env).compilationUnit();

      ICompilationUnitSourceBuilder builder = new CompilationUnitSourceBuilder(definition);
      builder.removeAllDeclaredImports();
      for (IAnnotationSourceBuilder annotationSourceBuilder : builder.getMainType().getAnnotations()) {
        if (annotationSourceBuilder.getName().equals(IScoutRuntimeTypes.WebServiceEntryPoint)) {

          final String newPortTypeFqn = up.getPortTypePackage() + '.' + up.getPortTypeName();
          annotationSourceBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_ENDPOINTINTERFACE_ATTRIBUTE, new ISourceBuilder() {
            @Override
            public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
              source.append(validator.useName(newPortTypeFqn)).append(SuffixConstants.SUFFIX_STRING_class);
            }
          });
          annotationSourceBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE, new RawSourceBuilder(CoreUtils.toStringLiteral(up.getEntryPointName())));
          annotationSourceBuilder.putElement(JaxWsUtils.ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE, new RawSourceBuilder(CoreUtils.toStringLiteral(up.getEntryPointPackage())));
          break;
        }
      }

      S2eUtils.writeType((IPackageFragmentRoot) entryPointDefinition.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), builder, definition.javaEnvironment(), monitor, workingCopyManager);
    }
  }

  public Collection<IFile> getJaxwsBindingFiles() {
    return Collections.unmodifiableCollection(m_jaxwsBindingFiles);
  }

  public void setJaxwsBindingFiles(Collection<? extends IFile> files) {
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

    public WebServiceClientUpdate(IType webServiceClient, String pck, String portTypeName, String webServiceName) {
      m_webServiceClient = webServiceClient;
      m_package = pck;
      m_portTypeName = portTypeName;
      m_webServiceName = webServiceName;
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
  }

  public static class WebServiceImplementationUpdate {
    private final IType m_webServiceImpl;
    private final String m_package;
    private final String m_portTypeName;

    public WebServiceImplementationUpdate(IType webServiceImpl, String package1, String portTypeName) {
      m_webServiceImpl = webServiceImpl;
      m_package = package1;
      m_portTypeName = portTypeName;
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

    public EntryPointDefinitionUpdate(IType entryPointDefinitions, String entryPointPackage, String entryPointName, String portTypeName, String portTypePackage) {
      m_entryPointDefinition = entryPointDefinitions;
      m_entryPointPackage = entryPointPackage;
      m_entryPointName = entryPointName;
      m_portTypeName = portTypeName;
      m_portTypePackage = portTypePackage;
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
  }
}
