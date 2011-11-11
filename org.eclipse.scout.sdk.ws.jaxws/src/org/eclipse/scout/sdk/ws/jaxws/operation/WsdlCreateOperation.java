/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;

public class WsdlCreateOperation implements IOperation {

  private IScoutBundle m_bundle;

  private WsdlResource m_wsdlResource;
  private String m_alias;
  private String m_targetNamespace;
  private String m_service;
  private String m_portName;
  private String m_portType;
  private String m_binding;
  private String m_urlPattern;
  private String m_serviceOperationName;
  private WsdlStyleEnum m_wsdlStyle;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("bundle not set");
    }
    if (m_wsdlResource == null) {
      throw new IllegalArgumentException("WSDL resource must not be null");
    }
    if (!StringUtility.hasText(m_alias)) {
      throw new IllegalArgumentException("alias must not be empty");
    }
    if (m_targetNamespace == null) {
      throw new IllegalArgumentException("no targetNamespace set");
    }
    if (m_service == null) {
      throw new IllegalArgumentException("no service set");
    }
    if (m_portName == null) {
      throw new IllegalArgumentException("no port set");
    }
    if (m_portType == null) {
      throw new IllegalArgumentException("no porttype set");
    }
    if (m_binding == null) {
      throw new IllegalArgumentException("no binding set");
    }
    if (m_urlPattern == null) {
      throw new IllegalArgumentException("no urlPattern set");
    }
    if (m_serviceOperationName == null) {
      throw new IllegalArgumentException("no serviceOperationName set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    WSDLFactory factory;
    try {
      factory = WSDLFactory.newInstance();
      WSDLWriter writer = factory.newWSDLWriter();
      Definition definition = factory.newDefinition();

      definition.setExtensionRegistry(factory.newPopulatedExtensionRegistry());

      definition.setQName(new QName(m_alias));
      definition.setTargetNamespace(JaxWsSdkUtility.normalizePath(m_targetNamespace, SeparatorType.TrailingType));
      definition.addNamespace("tns", JaxWsSdkUtility.normalizePath(m_targetNamespace, SeparatorType.TrailingType));
      definition.addNamespace("mime", "http://schemas.xmlsoap.org/wsdl/mime/");
      definition.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
      definition.addNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
      definition.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
      definition.addNamespace("http", "http://schemas.xmlsoap.org/wsdl/http/");

      Document document = writer.getDocument(definition);

      // Port type
      PortType portType = definition.createPortType();
      definition.addPortType(portType);
      portType.setUndefined(false);
      portType.setQName(new QName(definition.getNamespace("tns"), m_portType));

      // Binding
      Binding binding = definition.createBinding();
      definition.addBinding(binding);
      binding.setUndefined(false);
      binding.setQName(new QName(definition.getNamespace("tns"), m_binding));
      binding.setPortType(portType);

      // Binding > Soap Binding
      SOAPBinding soapBinding = (SOAPBinding) definition.getExtensionRegistry().createExtension(Binding.class, new QName(definition.getNamespace("soap"), "binding"));
      binding.addExtensibilityElement(soapBinding);
      soapBinding.setStyle("document");
      soapBinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
      soapBinding.setElementType(new QName(definition.getNamespace("soap"), "binding"));

      // Binding > Binding operation
      BindingOperation bindingOperation = definition.createBindingOperation();
      binding.addBindingOperation(bindingOperation);
      bindingOperation.setName(m_serviceOperationName);

      // Binding > Binding operation > Soap operation
      SOAPOperation soapOperation = (SOAPOperation) definition.getExtensionRegistry().createExtension(BindingOperation.class, new QName(definition.getNamespace("soap"), "operation"));
      bindingOperation.addExtensibilityElement(soapOperation);
      soapOperation.setSoapActionURI(JaxWsSdkUtility.normalizePath(m_targetNamespace, SeparatorType.TrailingType) + m_serviceOperationName);

      // Binding > Binding operation > Binding input
      BindingInput bindingInput = definition.createBindingInput();
      bindingOperation.setBindingInput(bindingInput);

      // Binding > Binding operation > Binding input > Soap body
      SOAPBody soapBody = (SOAPBody) definition.getExtensionRegistry().createExtension(BindingInput.class, new QName(definition.getNamespace("soap"), "body"));
      soapBody.setUse("literal");
      bindingInput.addExtensibilityElement(soapBody);

      // Binding > Binding operation > Binding output
      BindingOutput bindingOutput = definition.createBindingOutput();
      bindingOperation.setBindingOutput(bindingOutput);

      // Binding > Binding operation > Binding output > Soap body
      soapBody = (SOAPBody) definition.getExtensionRegistry().createExtension(BindingOutput.class, new QName(definition.getNamespace("soap"), "body"));
      soapBody.setUse("literal");
      bindingOutput.addExtensibilityElement(soapBody);

      // Service
      Service service = definition.createService();
      definition.addService(service);
      service.setQName(new QName(m_service));

      // Port
      Port port = definition.createPort();
      service.addPort(port);
      port.setName(m_portName);
      port.setBinding(binding);

      // Port > Soap address
      SOAPAddress httpAddress = new SOAPAddressImpl();
      httpAddress.setLocationURI(JaxWsSdkUtility.normalizePath(m_targetNamespace, SeparatorType.None));
      httpAddress.setElementType(new QName(definition.getNamespace("soap"), "address"));
      port.addExtensibilityElement(httpAddress);

      // message (request)
      Message messageRequest = definition.createMessage();
      messageRequest.setUndefined(false);
      messageRequest.setQName(new QName(definition.getNamespace("tns"), m_serviceOperationName + "Request"));
      Part messagePartRequest = definition.createPart();
      if (getWsdlStyle() == WsdlStyleEnum.DocumentLiteralWrapped) {
        // naming of part name and element reference must follow document/literal WRAPPED convention
        messagePartRequest.setName("parameters");
        messagePartRequest.setElementName(new QName(definition.getNamespace("tns"), m_serviceOperationName));
      }
      else {
        // naming of part name and element reference must follow document/literal convention
        messagePartRequest.setName("in");
        messagePartRequest.setElementName(new QName(definition.getNamespace("tns"), "inElement"));
      }
      messageRequest.addPart(messagePartRequest);
      definition.addMessage(messageRequest);

      // message (response)
      Message messageResponse = definition.createMessage();
      messageResponse.setUndefined(false);
      messageResponse.setQName(new QName(definition.getNamespace("tns"), m_serviceOperationName + "Response"));
      Part messagePartResponse = definition.createPart();
      if (getWsdlStyle() == WsdlStyleEnum.DocumentLiteralWrapped) {
        // naming of part name and element reference must follow document/literal WRAPPED convention
        messagePartResponse.setName("parameters");
        messagePartResponse.setElementName(new QName(definition.getNamespace("tns"), m_serviceOperationName + "Response"));
      }
      else {
        // naming of part name and element reference must follow document/literal convention
        messagePartResponse.setName("out");
        messagePartResponse.setElementName(new QName(definition.getNamespace("tns"), "outElement"));
      }
      messageResponse.addPart(messagePartResponse);
      definition.addMessage(messageResponse);

      // operation
      Operation operation = definition.createOperation();
      operation.setUndefined(false);
      operation.setName(m_serviceOperationName);

      // operation > input
      Input input = definition.createInput();
      operation.setInput(input);
      input.setMessage(messageRequest);

      // operation > output
      Output output = definition.createOutput();
      operation.setOutput(output);
      portType.addOperation(operation);
      output.setMessage(messageResponse);

      // schema
      Types types = definition.createTypes();
      Schema schema = (Schema) definition.getExtensionRegistry().createExtension(Types.class, new QName(definition.getNamespace("xsd"), "schema"));
      types.addExtensibilityElement(schema);

      // definition of inline schema
      Element schemaXml = document.createElement("xsd:schema");
      schema.setElement(schemaXml);
      schemaXml.setAttribute("xmlns:xsd", definition.getNamespace("xsd"));
      schemaXml.setAttribute("targetNamespace", JaxWsSdkUtility.normalizePath(m_targetNamespace, SeparatorType.TrailingType));

      // definition of request
      Element schemaElementXml;
      if (getWsdlStyle() == WsdlStyleEnum.DocumentLiteralWrapped) {
        schemaElementXml = createDocumentLiteralWrappedSchemaElement(document, messagePartRequest, "in", "xsd:string");
        schemaXml.appendChild(schemaElementXml);
      }
      else {
        schemaElementXml = createDocumentLiteralSchemaElement(document, messagePartRequest, "xsd:string");
      }
      schemaXml.appendChild(schemaElementXml);

      // definition of response
      if (getWsdlStyle() == WsdlStyleEnum.DocumentLiteralWrapped) {
        schemaElementXml = createDocumentLiteralWrappedSchemaElement(document, messagePartResponse, "out", "xsd:string");
        schemaXml.appendChild(schemaElementXml);
      }
      else {
        schemaElementXml = createDocumentLiteralSchemaElement(document, messagePartResponse, "xsd:string");
      }
      schemaXml.appendChild(schemaElementXml);

      definition.setTypes(types);

      m_wsdlResource.storeWsdl(definition, IResourceListener.ELEMENT_FILE, IResourceListener.EVENT_WSDL_REPLACED, monitor);
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(IStatus.ERROR, "Could not persist WSDL file.", e));
    }
  }

  private Element createDocumentLiteralWrappedSchemaElement(Document document, Part part, String parameterName, String parameterXsdType) {
    Element xmlElement = document.createElement("xsd:element");
    xmlElement.setAttribute("name", part.getElementName().getLocalPart());

    // anonymous inner type
    Element xmlComplexType = document.createElement("xsd:complexType");
    xmlElement.appendChild(xmlComplexType);

    Element xmlSequence = document.createElement("xsd:sequence");
    xmlComplexType.appendChild(xmlSequence);

    Element xmlParameter = document.createElement("xsd:element");
    xmlParameter.setAttribute("name", parameterName);
    xmlParameter.setAttribute("type", parameterXsdType);
    xmlSequence.appendChild(xmlParameter);
    return xmlElement;
  }

  private Element createDocumentLiteralSchemaElement(Document document, Part part, String parameterXsdType) {
    Element xmlElement = document.createElement("xsd:element");
    xmlElement.setAttribute("name", part.getElementName().getLocalPart());
    xmlElement.setAttribute("type", parameterXsdType);
    return xmlElement;
  }

  @Override
  public String getOperationName() {
    return WsdlCreateOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public String getAlias() {
    return m_alias;
  }

  public void setAlias(String alias) {
    m_alias = alias;
  }

  public WsdlResource getWsdlResource() {
    return m_wsdlResource;
  }

  public void setWsdlResource(WsdlResource wsdlResource) {
    m_wsdlResource = wsdlResource;
  }

  public String getTargetNamespace() {
    return m_targetNamespace;
  }

  public void setTargetNamespace(String targetNamespace) {
    m_targetNamespace = targetNamespace;
  }

  public String getService() {
    return m_service;
  }

  public void setService(String service) {
    m_service = service;
  }

  public String getPort() {
    return m_portName;
  }

  public void setPortName(String portName) {
    m_portName = portName;
  }

  public String getPortType() {
    return m_portType;
  }

  public void setPortType(String portType) {
    m_portType = portType;
  }

  public String getBinding() {
    return m_binding;
  }

  public void setBinding(String binding) {
    m_binding = binding;
  }

  public String getUrlPattern() {
    return m_urlPattern;
  }

  public void setUrlPattern(String urlPattern) {
    m_urlPattern = urlPattern;
  }

  public String getServiceOperationName() {
    return m_serviceOperationName;
  }

  public void setServiceOperationName(String serviceOperationName) {
    m_serviceOperationName = serviceOperationName;
  }

  public WsdlStyleEnum getWsdlStyle() {
    return m_wsdlStyle;
  }

  public void setWsdlStyle(WsdlStyleEnum wsdlStyleEnum) {
    m_wsdlStyle = wsdlStyleEnum;
  }
}
