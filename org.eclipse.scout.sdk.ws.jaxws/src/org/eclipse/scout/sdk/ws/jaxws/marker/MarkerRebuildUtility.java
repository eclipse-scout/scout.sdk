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
package org.eclipse.scout.sdk.ws.jaxws.marker;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.CorruptBindingFileCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.CorruptWsdlCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.DiscouragedWsdlFolderCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.InvalidEndpointInterfaceCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.InvalidPortTypeCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.InvalidServiceCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.JaxWsServletRegistrationCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingBuildJaxWsEntryCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingClasspathEntryForJarFileCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingEndpointPropertyCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingPortTypeCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingPortTypeInheritanceCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingScoutWebServiceAnnotationCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingServiceCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingWebServiceAnnotationCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingWsdlCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MultipleGlobalBindingsCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.StubRebuildCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.UrlPatternDefaultCommand;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean.IHandlerVisitor;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.part.AnnotationProperty;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;
import org.eclipse.scout.sdk.ws.jaxws.validator.IServletAliasValidation;
import org.eclipse.scout.sdk.ws.jaxws.validator.IUrlPatternValidation;
import org.eclipse.scout.sdk.ws.jaxws.validator.ServletAliasValidator;
import org.eclipse.scout.sdk.ws.jaxws.validator.UrlPatternValidator;

@SuppressWarnings("restriction")
public final class MarkerRebuildUtility {
  public static final String PROPERTY_ENDPOINT_INTERFACE = "endpointInterface";

  private MarkerRebuildUtility() {
  }

  public static boolean rebuildPortTypeImplMarkers(IFile sunJaxWsFile, SunJaxWsBean sunJaxWsBean, BuildJaxWsBean buildJaxWsBean, IType portType, Definition wsdlDefinition, String markerGroupUUID, IScoutBundle bundle) {
    // determine resource to store markers
    IResource markerResource = sunJaxWsFile;
    if (!JaxWsSdkUtility.exists(sunJaxWsFile)) {
      markerResource = bundle.getJavaProject().getResource();
    }

    try {
      IType portTypeInterfaceType = null;
      PortType wsdlPortType = JaxWsSdkUtility.getPortType(wsdlDefinition, sunJaxWsBean.getServiceQNameSafe(), sunJaxWsBean.getPort());
      if (wsdlPortType != null && wsdlPortType.getQName() != null) {
        IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(bundle, buildJaxWsBean, sunJaxWsBean.getWsdl());
        portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(wsdlPortType.getQName(), stubJarFile);
      }

      // validate port type
      if (!TypeUtility.exists(portType)) {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Implementation, markerGroupUUID, Texts.get("InvalidImplementationType"));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingPortTypeCommand(bundle, markerGroupUUID, sunJaxWsBean, portTypeInterfaceType));
        return false;
      }
      // validate port type package
      String recommendedPackageName = JaxWsSdkUtility.getRecommendedProviderImplPackageName(bundle);
      if (!portType.getPackageFragment().getElementName().startsWith(recommendedPackageName)) {
        MarkerUtility.createMarker(portType.getResource(), MarkerType.Package, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("ByConventionXShouldStartWithY", Texts.get("Package"), recommendedPackageName));
        return false;
      }

      // validate port type interface types
      Set<IType> interfacePortTypes = new HashSet<IType>();

      ITypeHierarchy hierarchy = portType.newSupertypeHierarchy(new NullProgressMonitor());

      for (IType superInterface : hierarchy.getAllSuperInterfaces(portType)) {
        IAnnotation annotation = JaxWsSdkUtility.getAnnotation(superInterface, WebService.class.getName(), false);
        if (annotation == null || !annotation.exists()) {
          continue;
        }
        interfacePortTypes.add(superInterface);
      }

      if (interfacePortTypes.size() == 0) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("WsImplXMustImplementPortTypeInterface", portType.getElementName()));
        if (portTypeInterfaceType != null) {
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingPortTypeInheritanceCommand(bundle, markerGroupUUID, portType, portTypeInterfaceType, sunJaxWsBean));
        }
        return false;
      }

      // validate WebService annotation of port type
      IAnnotation webServiceAnnotation = JaxWsSdkUtility.getAnnotation(portType, WebService.class.getSimpleName(), false);
      if (!TypeUtility.exists(webServiceAnnotation)) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("TypeMustBeAnnotatedWithXAnnotationInOrderToBeInstalledAsWebservice", WebService.class.getSimpleName()));
        registerMissingEndpointCommand(markerSourceId, bundle, sunJaxWsBean, buildJaxWsBean, portType, wsdlDefinition);
        return false;
      }
      IMemberValuePair[] memberValuePairs = webServiceAnnotation.getMemberValuePairs();
      String interfacePortTypeQualifiedNameFromAnnotation = null;
      for (IMemberValuePair mvPair : memberValuePairs) {
        if (PROPERTY_ENDPOINT_INTERFACE.equals(mvPair.getMemberName())) {
          interfacePortTypeQualifiedNameFromAnnotation = (String) mvPair.getValue();
          break;
        }
      }
      if (interfacePortTypeQualifiedNameFromAnnotation == null) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("MissingPropertyXOnAnnotationYToLinkThePortTypeWithTheRespectivePortTypeInterface", PROPERTY_ENDPOINT_INTERFACE, WebService.class.getSimpleName()));
        registerMissingEndpointCommand(markerSourceId, bundle, sunJaxWsBean, buildJaxWsBean, portType, wsdlDefinition);
        return false;
      }

      // validate mismatch among endpoint property in annotation and implemented interfaces
      boolean match = false;
      for (IType interfacePortTypeCandidate : interfacePortTypes) {
        if (interfacePortTypeCandidate.getFullyQualifiedName().equals(interfacePortTypeQualifiedNameFromAnnotation)) {
          match = true;
          break;
        }
      }
      if (!match) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("TheEndpointInterfaceConfiguredWithinAnnotationXDoesNotExistOrIsNotImplementedByThisEndpoint", WebService.class.getSimpleName()));
        registerMissingEndpointCommand(markerSourceId, bundle, sunJaxWsBean, buildJaxWsBean, portType, wsdlDefinition);
        return false;
      }

      // validate ScoutWebService annotation
      IAnnotation scoutWebServiceAnnotation = JaxWsSdkUtility.getAnnotation(portType, TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getElementName(), false);
      if (!TypeUtility.exists(scoutWebServiceAnnotation)) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, IMarker.SEVERITY_WARNING,
            Texts.get("ToConfigureAuthenticationAndSessionHandlingForTheWebserviceAnnotateThePortTypeWithX", TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getElementName()));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingScoutWebServiceAnnotationCommand(portType));
        return false;
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("failed to rebuild markers for endpoint", e);
    }
    return true;
  }

  public static boolean rebuildBuildJaxWsMarkers(IFile buildJaxWsFile, BuildJaxWsBean buildJaxWsBean, String alias, WsdlResource wsdlResource, String markerGroupUUID, IScoutBundle bundle, WebserviceEnum webserviceEnum) {
    // determine resource to store markers
    IResource markerResource = buildJaxWsFile;
    if (!JaxWsSdkUtility.exists(buildJaxWsFile)) {
      markerResource = bundle.getJavaProject().getResource();
    }

    if (buildJaxWsBean == null) {
      String markerSourceId = MarkerUtility.createMarker(bundle.getJavaProject().getResource(), MarkerType.MissingBuildJaxWsEntry, markerGroupUUID, Texts.get("MissingBuildEntryInFileX", JaxWsConstants.PATH_BUILD_JAXWS.toString()));
      JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingBuildJaxWsEntryCommand(bundle, alias, webserviceEnum));
      return false;
    }

    // validate stub folder
    IFolder folder = JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.STUB_FOLDER, false);
    if (folder == null || !folder.exists()) {
      MarkerUtility.createMarker(markerResource, MarkerType.StubFolder, markerGroupUUID, Texts.get("StubFolderXDoesNotExist", JaxWsConstants.STUB_FOLDER.toString()));
      return false;
    }

    // validate package
    String stubPackageName = JaxWsSdkUtility.getBuildProperty(buildJaxWsBean.getPropertiers(), JaxWsConstants.OPTION_PACKAGE);
    if (stubPackageName != null) {
      // warning as custom package
      MarkerUtility.createMarker(markerResource, MarkerType.Package, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("ConventionPackageNameEqualsToTargetNamespaceX"));
      if (!StringUtility.hasText(stubPackageName)) {
        MarkerUtility.createMarker(markerResource, MarkerType.Package, markerGroupUUID, Texts.get("PackageNameMustNotBeEmpty"));
        return false;
      }
      // only validate custom package
      IStatus status = JavaConventionsUtil.validatePackageName(stubPackageName, bundle.getJavaProject());
      if (status.getSeverity() == IStatus.ERROR) {
        MarkerUtility.createMarker(markerResource, MarkerType.Package, markerGroupUUID, Texts.get("PackageNameNotValidJavaPackageName", status.getMessage()));
        return false;
      }
      else if (status.getSeverity() == IStatus.WARNING) {
        MarkerUtility.createMarker(markerResource, MarkerType.Package, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("PackageNameNotValidJavaPackageName", status.getMessage()));
      }
    }
    return true;
  }

  public static boolean rebuildSunJaxWsMarkers(IFile sunJaxWsFile, SunJaxWsBean sunJaxWsBean, WsdlResource wsdlResource, String markerGroupUUID, IScoutBundle bundle) {
    // determine resource to store markers
    IResource markerResource = sunJaxWsFile;
    if (!JaxWsSdkUtility.exists(sunJaxWsFile)) {
      markerResource = bundle.getJavaProject().getResource();
    }

    // validate alias
    if (!StringUtility.hasText(sunJaxWsBean.getAlias())) {
      MarkerUtility.createMarker(markerResource, markerGroupUUID, Texts.get("MissingOrEmptyAttributeX", SunJaxWsBean.XML_ALIAS));
      return false;
    }

    // validate implementation
    if (!StringUtility.hasText(sunJaxWsBean.getImplementation())) {
      MarkerUtility.createMarker(markerResource, markerGroupUUID, Texts.get("MissingOrEmptyAttributeX", SunJaxWsBean.XML_IMPLEMENTATION));
      return false;
    }

    if (!validateJaxWsServletRegistartionAndUrlPattern(sunJaxWsBean, markerGroupUUID, bundle)) {
      return false;
    }

    Definition wsdlDefinition = wsdlResource.loadWsdlDefinition();
    if (wsdlDefinition == null) {
      return true;
    }

    String wsdlProjectRelativePath = sunJaxWsBean.getWsdl();
    if (wsdlProjectRelativePath != null && !wsdlProjectRelativePath.equals(PathNormalizer.toWsdlPath(sunJaxWsBean.getWsdl()))) {
      MarkerUtility.createMarker(markerResource, MarkerType.WsdlFolder, markerGroupUUID, "Path to WSDL file in 'sun-jaxws.xml' must not start with a slash.");
    }

    // validate service
    Map services = wsdlDefinition.getServices();
    if (sunJaxWsBean.getServiceQNameSafe() == null) {
      MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("InvalidServiceConfigurationMissingOrInvalidQNameInAttributeX", SunJaxWsBean.XML_SERVICE));
      return false;
    }
    else if (!services.keySet().contains(sunJaxWsBean.getServiceQNameSafe())) {
      MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, Texts.get("ServiceCouldNotBeFoundInWSDLmodel"));

      if (!sunJaxWsBean.getServiceQNameSafe().getNamespaceURI().endsWith("/")) {
        MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, Texts.get("ServiceNamespaceMustEndWithASlash"));
      }
      return false;
    }

    // validate port
    Service service = (Service) services.get(sunJaxWsBean.getServiceQNameSafe());
    Map ports = service.getPorts();
    if (ports == null || ports.size() == 0) {
      MarkerUtility.createMarker(markerResource, MarkerType.Port, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("DiscouragedPortConfigurationNoPortFoundInWSDLFileX", wsdlResource.getFile().getProjectRelativePath().toString()));
      return false;
    }
    else if (sunJaxWsBean.getPortQNameSafe() == null) {
      MarkerUtility.createMarker(markerResource, MarkerType.Port, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("DiscouragedServiceConfigurationMissingOrIinvalidQNameInAttributeX", SunJaxWsBean.XML_PORT));
      return false;
    }
    else if (!ports.keySet().contains(sunJaxWsBean.getPortQNameSafe().getLocalPart())) {
      MarkerUtility.createMarker(markerResource, MarkerType.Port, markerGroupUUID, Texts.get("UnknownPortConfiguredInAttributeX", SunJaxWsBean.XML_PORT));
      return false;
    }
    return true;
  }

  private static boolean validateJaxWsServletRegistartionAndUrlPattern(final SunJaxWsBean sunJaxWsBean, final String markerGroupUUID, final IScoutBundle bundle) {
    // validate servlet alias
    final String servletAlias = ServletRegistrationUtility.getAlias(bundle);
    boolean validationResult = ServletAliasValidator.validate(servletAlias, new IServletAliasValidation() {

      @Override
      public void onEmpty() {
        String markerSourceId = MarkerUtility.createMarker(bundle.getJavaProject().getResource(), MarkerType.UrlPattern, markerGroupUUID, IMarker.SEVERITY_ERROR, "No or wrong servlet Plug-In specified in build-jaxws.xml to host JAX-WS servlet registration.\nAdd the element 'servlet-bundle' with the attribute 'name' to build-jaxws.xml and specify the symbolic name of the plugin that contains the JAX-WS servlet registration in its plugin.xml.");
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new JaxWsServletRegistrationCommand(bundle));
      }

      @Override
      public void onIllegalCharacters() {
        String markerSourceId = MarkerUtility.createMarker(bundle.getJavaProject().getResource(), MarkerType.UrlPattern, markerGroupUUID, IMarker.SEVERITY_ERROR, "Invalid JAX-WS servlet alias '" + servletAlias + "' specified.");
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new JaxWsServletRegistrationCommand(bundle));
      }

      @Override
      public void onWrongSeparators() {
        String markerSourceId = MarkerUtility.createMarker(bundle.getJavaProject().getResource(), MarkerType.UrlPattern, markerGroupUUID, IMarker.SEVERITY_ERROR, "Invalid JAX-WS servlet alias '" + servletAlias + "'. Must start with a slash with no empty segments and no trailing slash.");
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new JaxWsServletRegistrationCommand(bundle));
      }
    });
    if (!validationResult) {
      return false;
    }

    // validate URL pattern
    IResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(bundle).getFile();
    final IResource markerResource;
    if (JaxWsSdkUtility.exists(sunJaxWsResource)) {
      markerResource = sunJaxWsResource;
    }
    else {
      markerResource = bundle.getJavaProject().getResource();
    }

    final String urlPattern = sunJaxWsBean.getUrlPattern();
    return UrlPatternValidator.validate(urlPattern, servletAlias, new IUrlPatternValidation() {

      @Override
      public void onWrongSeparators() {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.UrlPattern, markerGroupUUID, IMarker.SEVERITY_ERROR, "Invalid URL pattern '" + urlPattern + "'. Must start with a slash with no empty segments and no trailing slash.");
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new JaxWsServletRegistrationCommand(bundle, sunJaxWsBean));
      }

      @Override
      public void onNotStartingWithServletAlias() {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.UrlPattern, markerGroupUUID, IMarker.SEVERITY_ERROR, Texts.get("UrlPatternJaxWsAliasMismatch", servletAlias));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new UrlPatternDefaultCommand(bundle, sunJaxWsBean, servletAlias, "URL pattern must start with JAX-WS servlet alias."));
      }

      @Override
      public void onIllegalCharacters() {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.UrlPattern, markerGroupUUID, IMarker.SEVERITY_ERROR, "Invalid URL pattern '" + urlPattern + "'.");
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new UrlPatternDefaultCommand(bundle, sunJaxWsBean, servletAlias, "Illegal characters in URL pattern."));
      }

      @Override
      public void onEmpty() {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.UrlPattern, markerGroupUUID, Texts.get("MissingOrEmptyAttributeX", SunJaxWsBean.XML_URL_PATTERN));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new UrlPatternDefaultCommand(bundle, sunJaxWsBean, servletAlias, "No URL Pattern specified."));
      }
    });
  }

  public static boolean rebuildBindingFileMarkers(IFile buildJaxWsFile, XmlResource[] bindingFileResources, WsdlResource wsdlResource, String markerGroupUUID, IScoutBundle bundle) {
    IResource defaultMarkerResource = buildJaxWsFile;

    // binding file
    for (int index = 0; index < bindingFileResources.length; index++) {
      XmlResource bindingFileResource = bindingFileResources[index];

      if (bindingFileResource.getFile() == null) {
        MarkerUtility.createMarker(defaultMarkerResource, MarkerType.BindingFile, JaxWsSdkUtility.toMarkerGroupUUID(markerGroupUUID, index), IMarker.SEVERITY_ERROR, Texts.get("NonExistingBindingFileConfigured"));
        return false;
      }
      else if (!bindingFileResource.getFile().exists()) {
        String markerSourceId = MarkerUtility.createMarker(defaultMarkerResource, MarkerType.BindingFile, JaxWsSdkUtility.toMarkerGroupUUID(markerGroupUUID, index), IMarker.SEVERITY_ERROR, Texts.get("BindingFileXCouldNotBeFound", bindingFileResource.getFile().getProjectRelativePath().toString()));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new CorruptBindingFileCommand(bundle, bindingFileResource.getFile(), wsdlResource));
        return false;
      }
      else if (bindingFileResource.loadXml() == null) {
        String markerSourceId = MarkerUtility.createMarker(bindingFileResource.getFile(), MarkerType.BindingFile, JaxWsSdkUtility.toMarkerGroupUUID(markerGroupUUID, index), IMarker.SEVERITY_ERROR, Texts.get("BindingFileXIsNotAValidXMLFile", bindingFileResource.getFile().getProjectRelativePath().toString()));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new CorruptBindingFileCommand(bundle, bindingFileResource.getFile(), wsdlResource));
        return false;
      }
    }

    if (JaxWsSdkUtility.containsGlobalBindingSection(bindingFileResources, true)) {
      String markerSourceId = MarkerUtility.createMarker(defaultMarkerResource, MarkerType.BindingFile, markerGroupUUID, IMarker.SEVERITY_ERROR, Texts.get("MultipleGlobalBindingDefinitionsFoundOnlyOneAllowed"));
      JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MultipleGlobalBindingsCommand());
      return false;
    }
    return true;
  }

  public static boolean rebuildWsdlMarkers(WsdlResource wsdlResource, BuildJaxWsBean buildJaxWsBean, SunJaxWsBean sunJaxWsBean, String markerGroupUUID, IScoutBundle bundle) {
    // determine resource to store markers
    IResource markerResource = wsdlResource.getFile();
    if (!JaxWsSdkUtility.exists(wsdlResource.getFile())) {
      markerResource = bundle.getJavaProject().getResource();
    }

    IFile wsdlFile = wsdlResource.getFile();
    if (wsdlFile == null) {
      MarkerUtility.createMarker(markerResource, MarkerType.Wsdl, markerGroupUUID, Texts.get("NoWSDLFileConfigured"));
      return false;
    }
    boolean provider = (sunJaxWsBean != null);
    if (!wsdlResource.existsFile()) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Wsdl, markerGroupUUID, Texts.get("WSDLFileXCouldNotBeFound", wsdlFile.getProjectRelativePath().toString()));
      if (provider) {
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingWsdlCommand(bundle, wsdlResource, sunJaxWsBean));
      }
      else {
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingWsdlCommand(bundle, wsdlResource, buildJaxWsBean));
      }
      return false;
    }

    Definition wsdlDefinition = wsdlResource.loadWsdlDefinition();
    if (wsdlDefinition == null) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Wsdl, markerGroupUUID, Texts.get("CorruptWSDLFileAtLocationX", wsdlFile.getProjectRelativePath().toString()));
      if (provider) {
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new CorruptWsdlCommand(bundle, wsdlResource, sunJaxWsBean));
      }
      else {
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new CorruptWsdlCommand(bundle, wsdlResource, buildJaxWsBean));
      }
      return false;
    }

    // validate services
    Map services = wsdlDefinition.getServices();
    if (services == null || services.size() == 0) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, IMarker.SEVERITY_ERROR, Texts.get("NoServiceFoundInWSDLFileX", wsdlResource.getFile().getProjectRelativePath().toString()));
      JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingServiceCommand(wsdlResource.getFile().getName()));
      return false;
    }

    // validate WSDL folder > only accept subfolders of root folder
    IFolder rootFolder;
    if (provider) {
      rootFolder = JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false);
    }
    else {
      rootFolder = JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false);
    }
    IFolder folder = JaxWsSdkUtility.getParentFolder(bundle, wsdlFile);
    IPath wsdlRootPath = rootFolder.getProjectRelativePath();
    IPath candidatePath = folder.getProjectRelativePath();
    candidatePath = candidatePath.makeRelativeTo(wsdlRootPath);
    if (candidatePath.toString().startsWith("..")) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.WsdlFolder, markerGroupUUID, IMarker.SEVERITY_WARNING, Texts.get("WarningWsdlFolder", rootFolder.getProjectRelativePath().toString()));
      if (provider) {
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new DiscouragedWsdlFolderCommand(bundle, markerGroupUUID, buildJaxWsBean, sunJaxWsBean));
      }
      else {
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new DiscouragedWsdlFolderCommand(bundle, markerGroupUUID, buildJaxWsBean));
      }
    }
    return true;
  }

  public static boolean rebuildWebserviceClientType(IType webserviceClientType, BuildJaxWsBean buildJaxWsBean, WsdlResource wsdlResource, String markerGroupUUID, IScoutBundle bundle) {
    if (buildJaxWsBean == null) {
      return false;
    }

    if (!TypeUtility.exists(webserviceClientType)) {
      return false;
    }
    // determine resource to store markers
    IResource markerResource = webserviceClientType.getResource();
    if (!JaxWsSdkUtility.exists(markerResource)) {
      markerResource = bundle.getJavaProject().getResource();
    }

    IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(bundle, buildJaxWsBean, buildJaxWsBean.getWsdl());

    // validate service
    QName serviceQName = JaxWsSdkUtility.extractServiceQNameFromWsClient(webserviceClientType);
    if (serviceQName == null) {
      if (stubJarFile != null && stubJarFile.exists()) {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, Texts.get("InvalidServiceInSuperTypeGenericParameter", javax.xml.ws.Service.class.getName(), WebServiceClient.class.getSimpleName()));
        InvalidServiceCommand cmd = new InvalidServiceCommand(webserviceClientType, stubJarFile);
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
      }
      else {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, Texts.get("InvalidServiceInWsdl", javax.xml.ws.Service.class.getName(), WebServiceClient.class.getSimpleName()));
        registerStubRebuildCommand(markerSourceId, buildJaxWsBean, wsdlResource, bundle);
      }
      return false;
    }

    // validate port type interface type
    QName portTypeQName = JaxWsSdkUtility.extractPortTypeQNameFromWsClient(webserviceClientType);
    if (portTypeQName == null) {
      if (stubJarFile != null && stubJarFile.exists()) {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.PortType, markerGroupUUID, Texts.get("InvalidPortTypeInSuperGenericParameter", WebService.class.getSimpleName()));
        InvalidPortTypeCommand cmd = new InvalidPortTypeCommand(webserviceClientType, stubJarFile);
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
      }
      else {
        String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.PortType, markerGroupUUID, Texts.get("InvalidPortTypeInWsdl", WebService.class.getSimpleName()));
        registerStubRebuildCommand(markerSourceId, buildJaxWsBean, wsdlResource, bundle);
      }
      return false;
    }

    Definition wsdlDefinition = wsdlResource.loadWsdlDefinition();
    // check that service exists in WSDL
    if (wsdlDefinition == null) {
      return true;
    }

    if (wsdlDefinition.getService(serviceQName) == null) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.Service, markerGroupUUID, Texts.get("ServiceXNotFoundInWsdl", serviceQName.toString()));
      if (stubJarFile != null && stubJarFile.exists()) {
        InvalidServiceCommand cmd = new InvalidServiceCommand(webserviceClientType, stubJarFile);
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
      }
      return false;
    }

    if (wsdlDefinition.getPortType(portTypeQName) == null) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.PortType, markerGroupUUID, Texts.get("PortTypeXNotFoundInWsdl", portTypeQName.toString()));
      if (stubJarFile != null && stubJarFile.exists()) {
        InvalidPortTypeCommand cmd = new InvalidPortTypeCommand(webserviceClientType, stubJarFile);
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
      }
      return false;
    }
    return true;
  }

  public static boolean rebuildStubJarFileMarkers(BuildJaxWsBean buildJaxWsBean, WsdlResource wsdlResource, QName portTypeQName, QName serviceQName, IScoutBundle bundle, String markerGroupUUID) {
    if (buildJaxWsBean == null || wsdlResource == null || wsdlResource.getFile() == null) {
      return false;
    }

    IResource markerResource = bundle.getJavaProject().getResource();

    // validate existence of Jar file
    IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(bundle, buildJaxWsBean, wsdlResource.getFile().getName());
    if (!stubJarFile.exists()) {
      String markerSourceId = MarkerUtility.createMarker(markerResource, MarkerType.StubJar, markerGroupUUID, "Could not find JAR file '" + stubJarFile.getName() + "' with generated stub classes for webservice '" + buildJaxWsBean.getAlias() + "'");
      registerStubRebuildCommand(markerSourceId, buildJaxWsBean, wsdlResource, bundle);
      return false;
    }
    else {
      // ensure JAR file to be on project classpath
      try {
        PluginModelHelper h = new PluginModelHelper(bundle.getProject());
        if (!h.Manifest.existsClasspathEntry(stubJarFile.getProjectRelativePath().toString())) {
          String markerSourceId = MarkerUtility.createMarker(wsdlResource.getFile(), MarkerType.StubJar, markerGroupUUID, Texts.get("JarFileXOfWsYMustBeOnClasspath", stubJarFile.getName(), buildJaxWsBean.getAlias()));
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingClasspathEntryForJarFileCommand(bundle, buildJaxWsBean.getAlias(), stubJarFile));
          return false;
        }
      }
      catch (Exception e) {
        JaxWsSdk.logError(e);
      }
    }

    // validate existence of PortType interface type
    if (portTypeQName != null) {
      IType portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(portTypeQName, stubJarFile);
      if (portTypeInterfaceType == null) {
        String markerSourceId = MarkerUtility.createMarker(wsdlResource.getFile(), MarkerType.StubJar, markerGroupUUID, Texts.get("PortTypeXNotFoundInStubJarY", portTypeQName == null ? "<?>" : portTypeQName.toString(), stubJarFile.getName()));
        registerStubRebuildCommand(markerSourceId, buildJaxWsBean, wsdlResource, bundle);
      }
    }

    // validate existence of service
    if (serviceQName != null) {
      IType serviceType = JaxWsSdkUtility.resolveServiceType(serviceQName, stubJarFile);
      if (serviceType == null) {
        String markerSourceId = MarkerUtility.createMarker(wsdlResource.getFile(), MarkerType.StubJar, markerGroupUUID, Texts.get("ServiceTypeXNotFoundInStubJarY", serviceQName == null ? "<?>" : serviceQName.toString(), stubJarFile.getName()));
        registerStubRebuildCommand(markerSourceId, buildJaxWsBean, wsdlResource, bundle);
        return false;
      }
    }
    return true;
  }

  public static void rebuildHandlerMarkers(SunJaxWsBean sunJaxWsBean, final IScoutBundle bundle, final String markerGroupUUID) {

    sunJaxWsBean.visitHandlers(new IHandlerVisitor() {

      @Override
      public boolean visit(ScoutXmlElement xmlHandlerElement, String fullyQualifiedName, int handlerIndex, int handlerCount) {
        if (fullyQualifiedName == null) {
          MarkerUtility.createMarker(ResourceFactory.getSunJaxWsResource(bundle).getFile(), MarkerType.HandlerClass, markerGroupUUID, "Missing handler class");
          return false;
        }
        else if (!TypeUtility.existsType(fullyQualifiedName)) {
          MarkerUtility.createMarker(ResourceFactory.getSunJaxWsResource(bundle).getFile(), MarkerType.HandlerClass, markerGroupUUID, "Configured handler class '" + fullyQualifiedName + "' could not be found.");
          return false;
        }
        return true;
      }
    });
  }

  public static void rebuildCodeFirstPortTypeMarkers(IScoutBundle bundle, IType portType, SunJaxWsBean sunJaxWsBean, String markerGroupUUID) {
    try {
      // validate port type
      if (!TypeUtility.exists(portType)) {
        String markerSourceId = MarkerUtility.createMarker(ResourceFactory.getSunJaxWsResource(bundle).getFile(), MarkerType.Implementation, markerGroupUUID, Texts.get("InvalidImplementationType"));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingPortTypeCommand(bundle, markerGroupUUID, sunJaxWsBean, null));
        return;
      }

      if (!TypeUtility.exists(portType)) {
        return;
      }

      // validate WebService annotation
      IAnnotation wsAnnotation = JaxWsSdkUtility.getAnnotation(portType, WebService.class.getName(), false);
      if (wsAnnotation == null) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("MissingAnnotationOnPortType", WebService.class.getSimpleName(), portType.getElementName()));
        MissingWebServiceAnnotationCommand cmd = new MissingWebServiceAnnotationCommand(portType);
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
        return;
      }
      AnnotationProperty endpointProperty = JaxWsSdkUtility.parseAnnotationTypeValue(portType, wsAnnotation, "endpointInterface");
      if (endpointProperty.isDefined()) {
        IType endpointInterfaceType = TypeUtility.getType(endpointProperty.getFullyQualifiedName());
        if (endpointInterfaceType == null) {
          String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("InvalidEndpointInterfaceSpecified", WebService.class.getSimpleName(), portType.getElementName()));
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new InvalidEndpointInterfaceCommand(portType));
          return;
        }
        wsAnnotation = JaxWsSdkUtility.getAnnotation(endpointInterfaceType, WebService.class.getName(), true);
        if (wsAnnotation == null) {
          String markerSourceId = MarkerUtility.createMarker(endpointInterfaceType.getResource(), MarkerType.EndpointInterface, markerGroupUUID, Texts.get("MissingAnnotationOnEndpointInterface", endpointInterfaceType.getElementName(), WebService.class.getSimpleName()));
          MissingWebServiceAnnotationCommand cmd = new MissingWebServiceAnnotationCommand(endpointInterfaceType);
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
          return;
        }

        // validate that port type implements the endpoint interface
        if (!JaxWsSdkUtility.isJdtSubType(endpointInterfaceType.getFullyQualifiedName(), portType)) {
          String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, Texts.get("WsImplXMustImplementPortTypeInterface", portType.getElementName()));
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingPortTypeInheritanceCommand(bundle, markerGroupUUID, portType, endpointInterfaceType, sunJaxWsBean));
          return;
        }
      }
      if (!validateJaxWsServletRegistartionAndUrlPattern(sunJaxWsBean, markerGroupUUID, bundle)) {
        return;
      }

      // validate ScoutWebService annotation
      IAnnotation scoutWebServiceAnnotation = JaxWsSdkUtility.getAnnotation(portType, TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getElementName(), false);
      if (!TypeUtility.exists(scoutWebServiceAnnotation)) {
        String markerSourceId = MarkerUtility.createMarker(portType.getResource(), MarkerType.Implementation, markerGroupUUID, IMarker.SEVERITY_WARNING,
            Texts.get("ToConfigureAuthenticationAndSessionHandlingForTheWebserviceAnnotateThePortTypeWithX", TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getElementName()));
        JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingScoutWebServiceAnnotationCommand(portType));
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
    }
  }

  private static void registerStubRebuildCommand(String markerSourceId, BuildJaxWsBean buildJaxWsBean, WsdlResource wsdlResource, IScoutBundle bundle) {
    if (wsdlResource.getFile() != null) {
      StubRebuildCommand cmd = new StubRebuildCommand(bundle);
      cmd.setAlias(buildJaxWsBean.getAlias());
      cmd.setProperties(buildJaxWsBean.getPropertiers());
      cmd.setWsdlResource(wsdlResource);
      JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
    }
  }

  private static void registerMissingEndpointCommand(String markerSourceId, IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, BuildJaxWsBean buildJaxWsBean, IType implType, Definition wsdlDefinition) {
    if (sunJaxWsBean == null || wsdlDefinition == null) {
      return;
    }
    IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(bundle, buildJaxWsBean, sunJaxWsBean.getWsdl());
    if (stubJarFile == null) {
      return;
    }
    MissingEndpointPropertyCommand cmd = new MissingEndpointPropertyCommand(implType);
    PortType wsdlPortType = JaxWsSdkUtility.getPortType(wsdlDefinition, sunJaxWsBean.getServiceQNameSafe(), sunJaxWsBean.getPort());
    if (wsdlPortType != null) {
      cmd.setPortTypeQName(wsdlPortType.getQName());
    }
    cmd.setStubJarFile(stubJarFile);
    JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, cmd);
  }
}
