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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.part;

import java.util.LinkedList;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.BindingFileNewAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.BuildPropertiesEditAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction.FileExtensionType;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.RepairAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.StubRebuildAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.TypeOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.AnnotationPropertyTypePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.BindingFilePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.FilePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.FolderPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.SeparatorPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.StringPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.TypePresenter.ISearchJavaSearchScopeFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.UrlPatternPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.WsdlFilePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.WsdlFolderPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.IFileHandle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaArtifactVisitor;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaImportArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaIncludeArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPresenterValueChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class WebServiceProviderNodePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_REPAIR = "section.jaxws.repair";
  public static final String SECTION_ID_PROPERTIES = "section.jaxws.properties";
  public static final String SECTION_ID_STUB_PROPERTIES = "section.jaxws.build";
  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_LINKS_REF_WSDLS = "section.jaxws.links.ref.wsdl";
  public static final String SECTION_ID_LINKS_INCLUDED_SCHEMAS = "section.jaxws.links.included.schema";
  public static final String SECTION_ID_LINKS_IMPORTED_SCHEMAS = "section.jaxws.links.imported.schema";
  public static final String SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION = "section.scoutWebServiceAnnotation";

  public static final int PRESENTER_ID_SESSION_FACTORY = 1 << 1;
  public static final int PRESENTER_ID_AUTHENTICATION_HANDLER = 1 << 2;
  public static final int PRESENTER_ID_CREDENTIAL_VALIDATION_STRATEGY = 1 << 3;

  private IPresenterValueChangedListener m_presenterListener;

  private StringPresenter m_targetNamespacePresenter;
  private StringPresenter m_servicePresenter;
  private StringPresenter m_portPresenter;
  private StringPresenter m_bindingPresenter;
  private StringPresenter m_portTypePresenter;
  private UrlPatternPresenter m_urlPatternPresenter;
  private FolderPresenter m_stubFolderPresenter;
  private FilePresenter m_stubJarFilePresenter;
  private WsdlFolderPresenter m_wsdlFolderPresenter;
  private WsdlFilePresenter m_wsdlFilePresenter;

  private Composite m_bindingFilesComposite;

  private P_ScoutSeverityListener m_severityListener;
  private IPageLoadedListener m_pageLoadedListener;

  private AnnotationPropertyTypePresenter m_sessionFactoryPresenter;
  private AnnotationPropertyTypePresenter m_authenticationHandlerPresenter;
  private AnnotationPropertyTypePresenter m_credentialValidationStrategyPresenter;
  private List<BindingFilePresenter> m_bindingFilePresenters;
  private ActionPresenter m_rebuildStubPresenter;

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutBundle();
    m_presenterListener = new P_PresenterListener();
    m_severityListener = new P_ScoutSeverityListener();
    m_pageLoadedListener = new P_PageLoadedListener();
    ScoutSeverityManager.getInstance().addQualityManagerListener(m_severityListener);
    getPage().addPageLoadedListener(m_pageLoadedListener);
    m_bindingFilePresenters = new LinkedList<BindingFilePresenter>();
  }

  @Override
  protected void cleanup() {
    ScoutSeverityManager.getInstance().removeQualityManagerListener(m_severityListener);
    getPage().removePageLoadedListener(m_pageLoadedListener);
  }

  @Override
  public WebServiceProviderNodePage getPage() {
    return (WebServiceProviderNodePage) super.getPage();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(true);
    try {
      /*
       * repair section
       */
      createSection(SECTION_ID_REPAIR, Texts.get("RepairRequired"), Texts.get("SectionRepairDescription"), false);
      getSection(SECTION_ID_REPAIR).setVisible(JaxWsSdk.getDefault().containsMarkerCommands(getPage().getMarkerGroupUUID()));
      RepairAction a = new RepairAction();
      a.init(getPage().getMarkerGroupUUID(), m_bundle);
      AbstractPresenter presenter = new ActionPresenter(getSection(SECTION_ID_REPAIR).getSectionClient(), a, getFormToolkit());
      applyLayoutData(presenter);

      /*
       * link section
       */
      createSection(SECTION_ID_LINKS, Texts.get("ConsiderLinks"));
      createQuickLinkPresenters();

      /*
       * link section (referenced WSDL definitions)
       */
      createSection(SECTION_ID_LINKS_REF_WSDLS, Texts.get("ReferencedWsdlDefintions"));
      getSection(SECTION_ID_LINKS_REF_WSDLS).setExpanded(false);

      /*
       * link section (imported XSD schemas)
       */
      createSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS, Texts.get("ImportedXsdSchemas"));
      getSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS).setExpanded(false);

      /*
       * link section (included XSD schemas)
       */
      createSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS, Texts.get("IncludedXsdSchemas"));
      getSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS).setExpanded(false);

      createQuickLinkPresentersForReferencedFiles();

      if (getPage().getBuildJaxWsBean() == null) {
        return;
      }

      /*
       * stub properties section
       */
      createSection(SECTION_ID_STUB_PROPERTIES, Texts.get("StubProperties"));

      StubRebuildAction b = new StubRebuildAction();
      b.init(m_bundle, getPage().getBuildJaxWsBean(), getPage().getWsdlResource(), getPage().getMarkerGroupUUID(), WebserviceEnum.Provider);
      m_rebuildStubPresenter = new ActionPresenter(getSection(SECTION_ID_STUB_PROPERTIES).getSectionClient(), b, getFormToolkit());
      applyLayoutData(m_rebuildStubPresenter);

      // edit build properties
      BuildPropertiesEditAction c = new BuildPropertiesEditAction();
      c.init(m_bundle, getPage().getBuildJaxWsBean());
      presenter = new ActionPresenter(getSection(SECTION_ID_STUB_PROPERTIES).getSectionClient(), c, getFormToolkit());
      GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      gd.horizontalAlignment = SWT.RIGHT;
      presenter.getContainer().setLayoutData(gd);

      // add binding file
      BindingFileNewAction d = new BindingFileNewAction();
      d.init(m_bundle, getPage().getBuildJaxWsBean(), getPage().getWsdlResource());
      presenter = new ActionPresenter(getSection(SECTION_ID_STUB_PROPERTIES).getSectionClient(), d, getFormToolkit());
      presenter.setEnabled(getPage().getBuildJaxWsBean() != null && TypeUtility.exists(getPage().getPortType()) && !getPage().getPortType().isReadOnly());
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      gd.horizontalAlignment = SWT.RIGHT;
      presenter.getContainer().setLayoutData(gd);

      // binding files composite
      m_bindingFilesComposite = new Composite(getSection(SECTION_ID_STUB_PROPERTIES).getSectionClient(), SWT.NONE);
      gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      gd.verticalIndent = 5;
      m_bindingFilesComposite.setLayoutData(gd);

      /*
       * Scout webservice annotation section
       */
      createSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION, Texts.get("AuthenticationAndSessionContext"), null, true);
      getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).setExpanded(false);

      // Scout authentication mechanism
      m_authenticationHandlerPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).getSectionClient(), getFormToolkit());
      m_authenticationHandlerPresenter.setPresenterId(PRESENTER_ID_AUTHENTICATION_HANDLER);
      m_authenticationHandlerPresenter.setLabel(Texts.get("Authentication"));
      m_authenticationHandlerPresenter.setAcceptNullValue(true);
      m_authenticationHandlerPresenter.setBundle(m_bundle);
      m_authenticationHandlerPresenter.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService));
      m_authenticationHandlerPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER);
      m_authenticationHandlerPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      m_authenticationHandlerPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_authenticationHandlerPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider)));
      m_authenticationHandlerPresenter.setAllowChangeOfInterfaceType(true);
      m_authenticationHandlerPresenter.setInterfaceTypes(new IType[]{TypeUtility.getType(JaxWsRuntimeClasses.IAuthenticationHandlerProvider)});
      m_authenticationHandlerPresenter.addValueChangedListener(m_presenterListener);
      m_authenticationHandlerPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_authenticationHandlerPresenter);

      // Scout credential validation strategy
      m_credentialValidationStrategyPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).getSectionClient(), getFormToolkit());
      m_credentialValidationStrategyPresenter.setPresenterId(PRESENTER_ID_CREDENTIAL_VALIDATION_STRATEGY);
      m_credentialValidationStrategyPresenter.setLabel(Texts.get("CredentialValidation"));
      m_credentialValidationStrategyPresenter.setAcceptNullValue(true);
      m_credentialValidationStrategyPresenter.setBundle(m_bundle);
      m_credentialValidationStrategyPresenter.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService));
      m_credentialValidationStrategyPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY);
      m_credentialValidationStrategyPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      m_credentialValidationStrategyPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_credentialValidationStrategyPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(TypeUtility.getType(JaxWsRuntimeClasses.ICredentialValidationStrategy)));
      m_credentialValidationStrategyPresenter.setAllowChangeOfInterfaceType(true);
      m_credentialValidationStrategyPresenter.setInterfaceTypes(new IType[]{TypeUtility.getType(JaxWsRuntimeClasses.ICredentialValidationStrategy)});
      m_credentialValidationStrategyPresenter.addValueChangedListener(m_presenterListener);
      m_credentialValidationStrategyPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_credentialValidationStrategyPresenter);

      // Scout session factory
      m_sessionFactoryPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).getSectionClient(), getFormToolkit());
      m_sessionFactoryPresenter.setPresenterId(PRESENTER_ID_SESSION_FACTORY);
      m_sessionFactoryPresenter.setLabel(Texts.get("Session"));
      m_sessionFactoryPresenter.setAcceptNullValue(true);
      m_sessionFactoryPresenter.setBundle(m_bundle);
      m_sessionFactoryPresenter.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService));
      m_sessionFactoryPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY);
      m_sessionFactoryPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      m_sessionFactoryPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_sessionFactoryPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory)));
      m_sessionFactoryPresenter.setAllowChangeOfInterfaceType(true);
      m_sessionFactoryPresenter.setInterfaceTypes(new IType[]{TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory)});
      m_sessionFactoryPresenter.addValueChangedListener(m_presenterListener);
      m_sessionFactoryPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_sessionFactoryPresenter);

      /*
       * properties section
       */
      createSection(SECTION_ID_PROPERTIES, Texts.get("WebserviceProperties"));
      getSection(SECTION_ID_PROPERTIES).setExpanded(true);

      // service
      m_servicePresenter = new StringPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_servicePresenter.setLabel(Texts.get("Service"));
      m_servicePresenter.setBundle(m_bundle);
      m_servicePresenter.setMarkerType(MarkerType.Service);
      m_servicePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_servicePresenter.setEditable(false);
      applyLayoutData(m_servicePresenter);

      // port
      m_portPresenter = new StringPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_portPresenter.setLabel(Texts.get("Port"));
      m_portPresenter.setBundle(m_bundle);
      m_portPresenter.setMarkerType(MarkerType.Port);
      m_portPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_portPresenter.setEditable(false);
      applyLayoutData(m_portPresenter);

      // port type
      m_portTypePresenter = new StringPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_portTypePresenter.setLabel(Texts.get("PortType"));
      m_portTypePresenter.setBundle(m_bundle);
      m_portTypePresenter.setMarkerType(MarkerType.PortType);
      m_portTypePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_portTypePresenter.setEditable(false);
      applyLayoutData(m_portTypePresenter);

      // binding
      m_bindingPresenter = new StringPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_bindingPresenter.setLabel(Texts.get("Binding"));
      m_bindingPresenter.setEditable(false);
      applyLayoutData(m_bindingPresenter);

      // URL pattern
      m_urlPatternPresenter = new UrlPatternPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_urlPatternPresenter.setLabel(Texts.get("UrlPattern"));
      m_urlPatternPresenter.setBundle(m_bundle);
      m_urlPatternPresenter.setMarkerType(MarkerType.UrlPattern);
      m_urlPatternPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_urlPatternPresenter);

      // target namespace
      m_targetNamespacePresenter = new StringPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_targetNamespacePresenter.setLabel(Texts.get("TargetNamespace"));
      m_targetNamespacePresenter.setBundle(m_bundle);
      m_targetNamespacePresenter.setMarkerType(MarkerType.TargetNamespace);
      m_targetNamespacePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_targetNamespacePresenter.setEditable(false);
      applyLayoutData(m_targetNamespacePresenter);

      // stub folder
      m_stubFolderPresenter = new FolderPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_stubFolderPresenter.setShowBrowseButton(false);
      m_stubFolderPresenter.setLabel(Texts.get("StubFolder"));
      m_stubFolderPresenter.setBundle(m_bundle);
      m_stubFolderPresenter.setMarkerType(MarkerType.StubFolder);
      m_stubFolderPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_stubFolderPresenter);

      // stub jar
      m_stubJarFilePresenter = new FilePresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_stubJarFilePresenter.setShowBrowseButton(false);
      m_stubJarFilePresenter.setLabel(Texts.get("StubJar"));
      m_stubJarFilePresenter.setBundle(m_bundle);
      m_stubJarFilePresenter.setMarkerType(MarkerType.StubJar);
      m_stubJarFilePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_stubJarFilePresenter);

      // wsdl folder
      m_wsdlFolderPresenter = new WsdlFolderPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit(), WebserviceEnum.Provider);
      m_wsdlFolderPresenter.setLabel(Texts.get("WsdlFolder"));
      m_wsdlFolderPresenter.setBundle(m_bundle);
      m_wsdlFolderPresenter.setMarkerType(MarkerType.WsdlFolder);
      m_wsdlFolderPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_wsdlFolderPresenter);

      // wsdl file
      m_wsdlFilePresenter = new WsdlFilePresenter(m_bundle, getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_wsdlFilePresenter.setLabel(Texts.get("WsdlFile"));
      m_wsdlFilePresenter.setBundle(m_bundle);
      m_wsdlFilePresenter.setMarkerType(MarkerType.Wsdl);
      m_wsdlFilePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_wsdlFilePresenter);

      updatePresenterValues();
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  private void updatePresenterValues() {
    createQuickLinkPresenters();
    createQuickLinkPresentersForReferencedFiles();

    SunJaxWsBean sunJaxWsBean = getPage().getSunJaxWsBean();
    BuildJaxWsBean buildJaxWsBean = getPage().getBuildJaxWsBean();
    Definition wsdlDefinition = getPage().getWsdlDefinition();

    String serviceName = null;
    String serviceTooltip = null;
    String portName = null;
    String portTooltip = null;
    String bindingName = null;
    String bindingTooltip = null;
    String portTypeName = null;
    String portTypeTooltip = null;
    String urlPattern = null;
    IFile stubJarFile = null;

    if (sunJaxWsBean != null) {
      urlPattern = sunJaxWsBean.getUrlPattern();

      QName serviceQName = sunJaxWsBean.getServiceQNameSafe();
      if (serviceQName != null) {
        serviceName = serviceQName.getLocalPart();
        serviceTooltip = serviceQName.toString();
      }
      QName portTypeQName = sunJaxWsBean.getPortQNameSafe();
      if (portTypeQName != null) {
        portName = portTypeQName.getLocalPart();
        portTooltip = portTypeQName.toString();
      }

      if (wsdlDefinition != null) {
        PortType portType = JaxWsSdkUtility.getPortType(wsdlDefinition, sunJaxWsBean.getServiceQNameSafe(), sunJaxWsBean.getPort());
        if (portType != null) {
          portTypeName = portType.getQName().getLocalPart();
          portTypeTooltip = portType.getQName().toString();
        }

        Binding binding = JaxWsSdkUtility.getBinding(wsdlDefinition, sunJaxWsBean.getServiceQNameSafe(), sunJaxWsBean.getPort());
        if (binding != null) {
          bindingName = binding.getQName().getLocalPart();
          bindingTooltip = binding.getQName().toString();
        }
      }
    }

    if (buildJaxWsBean != null) {
      String wsdlFileName = null;
      if (sunJaxWsBean != null) {
        wsdlFileName = sunJaxWsBean.getWsdl();
      }
      stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, buildJaxWsBean, wsdlFileName);
    }

    m_targetNamespacePresenter.setInput(wsdlDefinition != null ? wsdlDefinition.getTargetNamespace() : null);
    m_targetNamespacePresenter.setTooltip(m_targetNamespacePresenter.getValue());
    m_servicePresenter.setInput(serviceName);
    m_servicePresenter.setTooltip(serviceTooltip);
    m_portPresenter.setInput(portName);
    m_portPresenter.setTooltip(portTooltip);
    m_bindingPresenter.setInput(bindingName);
    m_bindingPresenter.setTooltip(bindingTooltip);
    m_portTypePresenter.setInput(portTypeName);
    m_portTypePresenter.setTooltip(portTypeTooltip);
    m_urlPatternPresenter.setInput(urlPattern);
    m_urlPatternPresenter.setSunJaxWsBean(sunJaxWsBean);
    String servletRegistrationBundleName = ServletRegistrationUtility.getBuildJaxServletRegistrationBundleName(m_bundle);
    if (servletRegistrationBundleName != null) {
      m_urlPatternPresenter.setTooltip(Texts.get("JaxWsServletRegistrationInBundleX", servletRegistrationBundleName));
    }
    else {
      m_urlPatternPresenter.setTooltip(null);
    }
    // WSDL folder
    IFile wsdlFile = getPage().getWsdlResource().getFile();
    if (wsdlFile != null) {
      IPath wsdlFolderPath = wsdlFile.getProjectRelativePath().removeLastSegments(1);
      IFolder folder = JaxWsSdkUtility.getFolder(m_bundle, wsdlFolderPath, false);
      if (folder == null) {
        folder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false);
      }
      m_wsdlFolderPresenter.setInput(folder);
      m_wsdlFilePresenter.setFileDirectory(folder);
    }
    m_wsdlFolderPresenter.setBuildJaxWsBean(buildJaxWsBean);
    m_wsdlFolderPresenter.setSunJaxWsBean(sunJaxWsBean);

    // WSDL file
    m_wsdlFilePresenter.setInput(wsdlFile);
    m_wsdlFilePresenter.setBuildJaxWsBean(buildJaxWsBean);
    m_wsdlFilePresenter.setSunJaxWsBean(sunJaxWsBean);

    m_stubJarFilePresenter.setInput(stubJarFile);
    m_stubFolderPresenter.setInput(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.STUB_FOLDER, false));

    IType portType = getPage().getPortType();
    if (portType != null) {
      IAnnotation scoutWebServiceAnnotation = JaxWsSdkUtility.getAnnotation(portType, TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService).getFullyQualifiedName(), false);
      getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).setVisible(TypeUtility.exists(scoutWebServiceAnnotation));
      if (TypeUtility.exists(scoutWebServiceAnnotation)) {
        AnnotationProperty propertyValue = JaxWsSdkUtility.parseAnnotationTypeValue(portType, scoutWebServiceAnnotation, JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY);
        m_sessionFactoryPresenter.setInput(propertyValue.getFullyQualifiedName());
        m_sessionFactoryPresenter.setDeclaringType(portType);
        m_sessionFactoryPresenter.setResetLinkVisible(!propertyValue.isInherited());
        m_sessionFactoryPresenter.setBoldLabelText(!propertyValue.isInherited());

        propertyValue = JaxWsSdkUtility.parseAnnotationTypeValue(portType, scoutWebServiceAnnotation, JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER);
        m_authenticationHandlerPresenter.setInput(propertyValue.getFullyQualifiedName());
        m_authenticationHandlerPresenter.setDeclaringType(portType);
        m_authenticationHandlerPresenter.setResetLinkVisible(!propertyValue.isInherited());
        m_authenticationHandlerPresenter.setBoldLabelText(!propertyValue.isInherited());

        propertyValue = JaxWsSdkUtility.parseAnnotationTypeValue(portType, scoutWebServiceAnnotation, JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY);
        m_credentialValidationStrategyPresenter.setInput(propertyValue.getFullyQualifiedName());
        m_credentialValidationStrategyPresenter.setDeclaringType(portType);
        m_credentialValidationStrategyPresenter.setResetLinkVisible(!propertyValue.isInherited());
        m_credentialValidationStrategyPresenter.setBoldLabelText(!propertyValue.isInherited());
      }
    }

    createBindingFilePresenters(buildJaxWsBean);
    validateRebuildStubPresenter();

    getSection(SECTION_ID_REPAIR).setVisible(JaxWsSdk.getDefault().containsMarkerCommands(getPage().getMarkerGroupUUID()));

    controlView();
  }

  private void createQuickLinkPresenters() {
    getForm().setRedraw(false);

    try {
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS).getSectionClient());

      createQuickLinkForTypesPresenters();

      SeparatorPresenter separatorPresenter = new SeparatorPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), getFormToolkit());
      applyLayoutData(separatorPresenter);

      // QuickLink 'Open sun-jaxws.xml'
      FileOpenAction a = new FileOpenAction();
      a.init(ResourceFactory.getSunJaxWsResource(m_bundle).getFile(), ResourceFactory.getSunJaxWsResource(m_bundle).getFile().getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.SunJaxWsXmlFile), FileExtensionType.Xml);
      a.setToolTip(Texts.get("JaxWsDeploymentDescriptor"));
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), a, getFormToolkit());
      applyLayoutData(actionPresenter);

      // QuickLink 'Open build-jaxws.xml'
      FileOpenAction b = new FileOpenAction();
      b.init(ResourceFactory.getBuildJaxWsResource(m_bundle).getFile(), ResourceFactory.getBuildJaxWsResource(m_bundle).getFile().getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile), FileExtensionType.Xml);
      actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), b, getFormToolkit());
      b.setToolTip(Texts.get("JaxWsBuildDescriptor"));
      applyLayoutData(actionPresenter);

      if (getPage().getBuildJaxWsBean() == null) {
        return;
      }

      // QuickLink 'Open WSDL file'
      IFile wsdlFile = getPage().getWsdlResource().getFile();
      if (wsdlFile != null) {
        FileOpenAction c = new FileOpenAction();
        c.init(wsdlFile, wsdlFile.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), FileExtensionType.Auto);
        c.setToolTip("Web Services Description Language");
        actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), c, getFormToolkit());
        actionPresenter.setEnabled(wsdlFile.exists());
        applyLayoutData(actionPresenter);
      }
    }
    finally {
      getForm().setRedraw(true);
      getSection(SECTION_ID_LINKS).setVisible(getSection(SECTION_ID_LINKS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS));
    }
  }

  private void createQuickLinkPresentersForReferencedFiles() {
    getForm().setRedraw(false);
    try {
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS_REF_WSDLS).getSectionClient());
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS).getSectionClient());
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS).getSectionClient());

      WsdlResource wsdlResource = getPage().getWsdlResource();
      SchemaUtility.visitArtifacts(wsdlResource.getFile(), new SchemaArtifactVisitor<IFile>() {

        @Override
        public void onReferencedWsdlArtifact(WsdlArtifact<IFile> wsdlArtifact) {
          IFileHandle<IFile> fileHandle = wsdlArtifact.getFileHandle();
          FileOpenAction action = new FileOpenAction();
          action.init(fileHandle.getFile(), fileHandle.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), FileExtensionType.Auto);

          ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS_REF_WSDLS).getSectionClient(), action, getFormToolkit());
          actionPresenter.setEnabled(fileHandle.exists());
          applyLayoutData(actionPresenter);
        }

        @Override
        public void onSchemaIncludeArtifact(SchemaIncludeArtifact<IFile> schemaIncludeArtifact) {
          IFileHandle<IFile> fileHandle = schemaIncludeArtifact.getFileHandle();

          FileOpenAction b = new FileOpenAction();
          b.init(fileHandle.getFile(), fileHandle.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), FileExtensionType.Auto);
          ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS).getSectionClient(), b, getFormToolkit());
          actionPresenter.setEnabled(fileHandle.exists());
          applyLayoutData(actionPresenter);
        }

        @Override
        public void onSchemaImportArtifact(SchemaImportArtifact<IFile> schemaImportArtifact) {
          IFileHandle<IFile> fileHandle = schemaImportArtifact.getFileHandle();

          FileOpenAction b = new FileOpenAction();
          b.init(fileHandle.getFile(), fileHandle.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), FileExtensionType.Auto);
          b.setToolTip("namespace: " + StringUtility.nvl(schemaImportArtifact.getNamespaceUri(), "?"));
          ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS).getSectionClient(), b, getFormToolkit());
          actionPresenter.setEnabled(fileHandle.exists());
          applyLayoutData(actionPresenter);
        }

      });
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
    }
    finally {
      getForm().setRedraw(true);

      getSection(SECTION_ID_LINKS_REF_WSDLS).setVisible(getSection(SECTION_ID_LINKS_REF_WSDLS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS_REF_WSDLS));

      getSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS).setVisible(getSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS_IMPORTED_SCHEMAS));

      getSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS).setVisible(getSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS_INCLUDED_SCHEMAS));
    }
  }

  private void createQuickLinkForTypesPresenters() {
    SunJaxWsBean sunJaxWsBean = getPage().getSunJaxWsBean();

    if (sunJaxWsBean == null || sunJaxWsBean.getImplementation() == null) {
      return;
    }

    String fqn = sunJaxWsBean.getImplementation();
    if (!TypeUtility.existsType(fqn)) {
      return;
    }
    IType wsProviderImplType = TypeUtility.getType(fqn);

    // QuickLink 'Open PortType'
    TypeOpenAction a = new TypeOpenAction();
    a.init(wsProviderImplType);
    ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), a, getFormToolkit());
    applyLayoutData(actionPresenter);

    // QuickLink 'Open PortType Interface'
    createQuickLinkForPortTypeInterfaceType();
  }

  private void createQuickLinkForPortTypeInterfaceType() {
    BuildJaxWsBean buildJaxWsBean = getPage().getBuildJaxWsBean();
    SunJaxWsBean sunJaxWsBean = getPage().getSunJaxWsBean();
    if (sunJaxWsBean == null) {
      return;
    }
    PortType portType = JaxWsSdkUtility.getPortType(getPage().getWsdlDefinition(), sunJaxWsBean.getServiceQNameSafe(), sunJaxWsBean.getPort());
    if (portType == null || portType.getQName() == null) {
      return;
    }
    IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, buildJaxWsBean, sunJaxWsBean.getWsdl());
    if (stubJarFile == null) {
      return;
    }
    IType portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(portType.getQName(), stubJarFile);
    if (!TypeUtility.exists(portTypeInterfaceType)) {
      return;
    }

    TypeOpenAction action = new TypeOpenAction();
    action.init(portTypeInterfaceType);
    action.setToolTip(Texts.get("JaxWsPortTypeInterface"));
    ActionPresenter presenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), action, getFormToolkit());
    applyLayoutData(presenter);
  }

  private void createBindingFilePresenters(BuildJaxWsBean buildJaxWsBean) {
    getForm().setRedraw(false);
    try {
      m_bindingFilePresenters.clear();
      JaxWsSdkUtility.disposeChildControls(m_bindingFilesComposite);
      GridLayout layout = new GridLayout(1, false);
      layout.marginWidth = 0;
      layout.marginHeight = 5;
      m_bindingFilesComposite.setLayout(layout);

      if (buildJaxWsBean == null) {
        return;
      }

      IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(m_bundle, buildJaxWsBean.getPropertiers());
      for (int index = 0; index < bindingFiles.length; index++) {
        BindingFilePresenter filePresenter = new BindingFilePresenter(m_bundle, m_bindingFilesComposite, getFormToolkit());
        filePresenter.setMarkerGroupUUID(JaxWsSdkUtility.toMarkerGroupUUID(getPage().getMarkerGroupUUID(), index));
        filePresenter.setInput(bindingFiles[index]);
        filePresenter.setBuildJaxWsBean(buildJaxWsBean);
        applyLayoutData(filePresenter);

        m_bindingFilePresenters.add(filePresenter);
      }
    }
    finally {
      getForm().setRedraw(true);
      JaxWsSdkUtility.doLayout(m_bindingFilesComposite);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_STUB_PROPERTIES));
    }
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }

  private ISearchJavaSearchScopeFactory createSubClassesSearchScopeFactory(final IType superType) {
    return new ISearchJavaSearchScopeFactory() {

      @Override
      public IJavaSearchScope create() {
        // do not use PrimaryTypeHierarchy to get subtypes due to static inner classes
        IType[] subTypes = JaxWsSdkUtility.getJdtSubTypes(m_bundle, superType.getFullyQualifiedName(), false, false, true, false);
        return SearchEngine.createJavaSearchScope(subTypes);
      }
    };
  }

  private void validateRebuildStubPresenter() {
    boolean valid = (getPage().getBuildJaxWsBean() != null &&
        getPage().getSunJaxWsBean() != null &&
        !MarkerUtility.containsMarker(m_bundle, MarkerType.Wsdl, getPage().getMarkerGroupUUID(), IMarker.SEVERITY_ERROR));
    m_rebuildStubPresenter.setEnabled(valid);

    if (valid) {
      StubRebuildAction action = new StubRebuildAction();
      action.init(m_bundle, getPage().getBuildJaxWsBean(), getPage().getWsdlResource(), getPage().getMarkerGroupUUID(), WebserviceEnum.Provider);
      m_rebuildStubPresenter.setAction(action);
    }
  }

  private void controlView() {
    // enable / disable credential validation strategy presenter
    m_credentialValidationStrategyPresenter.setEnabled(JaxWsSdkUtility.isProviderAuthenticationSet(m_authenticationHandlerPresenter.getValue()));
  }

  private final class P_PresenterListener implements IPresenterValueChangedListener {

    @Override
    public void propertyChanged(int presenterId, Object value) {
      switch (presenterId) {
        case PRESENTER_ID_SESSION_FACTORY:
        case PRESENTER_ID_AUTHENTICATION_HANDLER:
        case PRESENTER_ID_CREDENTIAL_VALIDATION_STRATEGY: {
          IType portType = getPage().getPortType();
          if (TypeUtility.exists(portType)) {
            String factoryFullyQualifiedName = (String) value;
            if (!TypeUtility.existsType(factoryFullyQualifiedName)) {
              return;
            }
            IType factoryType = TypeUtility.getType(factoryFullyQualifiedName);
            AnnotationUpdateOperation op = new AnnotationUpdateOperation();
            op.setDeclaringType(portType);
            op.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutWebService));
            switch (presenterId) {
              case PRESENTER_ID_SESSION_FACTORY:
                op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, factoryType);
                break;
              case PRESENTER_ID_AUTHENTICATION_HANDLER:
                op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, factoryType);
                if (!JaxWsSdkUtility.isProviderAuthenticationSet(factoryFullyQualifiedName)) {
                  op.removeProperty(JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY);
                }
                break;
              case PRESENTER_ID_CREDENTIAL_VALIDATION_STRATEGY:
                op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY, factoryType);
                break;
            }
            new OperationJob(op).schedule();
          }
          break;
        }
      }
    }
  }

  private class P_ScoutSeverityListener implements IScoutSeverityListener {

    @Override
    public void severityChanged(IResource resource) {
      boolean accept = false;
      if (resource == ResourceFactory.getSunJaxWsResource(m_bundle).getFile()) {
        accept = true;
      }
      if (!accept && resource == ResourceFactory.getBuildJaxWsResource(m_bundle).getFile()) {
        accept = true;
      }
      if (!accept && resource == getPage().getWsdlResource().getFile()) {
        accept = true;
      }
      if (!accept && getPage().getPortType() != null && resource == getPage().getPortType().getResource()) {
        accept = true;
      }
      if (!accept) {
        for (XmlResource bindingFileResource : getPage().getBindingFileResources()) {
          if (resource == bindingFileResource.getFile()) {
            accept = true;
            break;
          }
        }
      }
      if (!accept) {
        return;
      }
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (getPage().isPageUnloaded() || getForm().isDisposed()) {
            return;
          }
          getSection(SECTION_ID_REPAIR).setVisible(JaxWsSdk.getDefault().containsMarkerCommands(getPage().getMarkerGroupUUID()));

          if (MarkerUtility.containsMarker(m_bundle, MarkerType.MissingBuildJaxWsEntry, getPage().getMarkerGroupUUID(), IMarker.SEVERITY_ERROR)) {
            return;
          }

          // update marker status of presenters (quality)
          m_targetNamespacePresenter.updateInfo();
          m_servicePresenter.updateInfo();
          m_portPresenter.updateInfo();
          m_bindingPresenter.updateInfo();
          m_portTypePresenter.updateInfo();
          m_urlPatternPresenter.updateInfo();
          m_wsdlFilePresenter.updateInfo();
          m_wsdlFolderPresenter.updateInfo();
          m_sessionFactoryPresenter.updateInfo();
          m_authenticationHandlerPresenter.updateInfo();
          m_credentialValidationStrategyPresenter.updateInfo();
          m_stubJarFilePresenter.updateInfo();
          m_stubFolderPresenter.updateInfo();

          for (BindingFilePresenter presenter : m_bindingFilePresenters) {
            presenter.updateInfo();
          }

          validateRebuildStubPresenter();
          controlView();
        }
      });
    }
  }

  private class P_PageLoadedListener implements IPageLoadedListener {

    @Override
    public void pageLoaded() {
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (getPage().isPageUnloaded() || getForm().isDisposed()) {
            return;
          }
          if (MarkerUtility.containsMarker(m_bundle, MarkerType.MissingBuildJaxWsEntry, getPage().getMarkerGroupUUID(), IMarker.SEVERITY_ERROR)) {
            return;
          }
          updatePresenterValues();
        }
      });
    }
  }
}
