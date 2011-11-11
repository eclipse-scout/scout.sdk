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
import java.util.Map;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.JdtTypePropertyPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
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
import org.eclipse.scout.sdk.ws.jaxws.swt.action.StubGenerationAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.TypeOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.AnnotationPropertyTypePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.BindingFilePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.FilePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.FolderPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.SeparatorPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.StringPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.TypePresenter.ISearchJavaSearchScopeFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.WsdlFilePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.DefinitionBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SchemaBean;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPresenterValueChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class WebServiceConsumerNodePagePropertyViewPart extends JdtTypePropertyPart {

  public static final String SECTION_ID_REPAIR = "section.jaxws.repair";
  public static final String SECTION_ID_PROPERTIES = "section.jaxws.properties";
  public static final String SECTION_ID_STUB_PROPERTIES = "section.jaxws.build";
  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_LINKS_REF_WSDLS = "section.jaxws.links.ref.wsdl";
  public static final String SECTION_ID_LINKS_REF_SCHEMAS = "section.jaxws.links.ref.schema";
  public static final String SECTION_ID_SCOUT_WEB_SERVICE_CLIENT_ANNOTATION = "section.scoutWebServiceAnnotation";

  public static final int PRESENTER_ID_AUTHENTICATION_HANDLER = 1 << 1;

  private IPresenterValueChangedListener m_presenterListener;

  private StringPresenter m_targetNamespacePresenter;
  private StringPresenter m_servicePresenter;
  private StringPresenter m_portTypePresenter;
  private FolderPresenter m_stubFolderPresenter;
  private FilePresenter m_stubJarFilePresenter;
  private WsdlFilePresenter m_wsdlFilePresenter;

  private Composite m_bindingFilesComposite;

  private P_ScoutSeverityListener m_severityListener;
  private IPageLoadedListener m_pageLoadedListener;

  private AnnotationPropertyTypePresenter m_authenticationHandlerPresenter;
  private List<BindingFilePresenter> m_bindingFilePresenters;
  private ActionPresenter m_rebuildStubPresenter;

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutResource();
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
    super.cleanup();
  }

  @Override
  public WebServiceConsumerNodePage getPage() {
    return (WebServiceConsumerNodePage) super.getPage();
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
       * link section (imported / included XSD schemas)
       */
      createSection(SECTION_ID_LINKS_REF_SCHEMAS, Texts.get("ImportedIncludedXsdSchemas"));
      getSection(SECTION_ID_LINKS_REF_SCHEMAS).setExpanded(false);
      createQuickLinkPresentersForReferencedFiles();

      if (getPage().getBuildJaxWsBean() == null) {
        return;
      }

      /*
       * stub properties section
       */
      createSection(SECTION_ID_STUB_PROPERTIES, Texts.get("StubProperties"));

      StubGenerationAction b = new StubGenerationAction();
      b.init(m_bundle, getPage().getBuildJaxWsBean(), getPage().getWsdlResource(), getPage().getMarkerGroupUUID(), WebserviceEnum.Consumer);
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
      presenter.setEnabled(getPage().getBuildJaxWsBean() != null);
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
      createSection(SECTION_ID_SCOUT_WEB_SERVICE_CLIENT_ANNOTATION, Texts.get("Authentication"), null, true);
      getSection(SECTION_ID_SCOUT_WEB_SERVICE_CLIENT_ANNOTATION).setExpanded(false);

      // Scout authentication factory
      m_authenticationHandlerPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_SCOUT_WEB_SERVICE_CLIENT_ANNOTATION).getSectionClient(), getFormToolkit());
      m_authenticationHandlerPresenter.setPresenterId(PRESENTER_ID_AUTHENTICATION_HANDLER);
      m_authenticationHandlerPresenter.setLabel(Texts.get("Authentication"));
      m_authenticationHandlerPresenter.setAcceptNullValue(true);
      m_authenticationHandlerPresenter.setBundle(m_bundle);
      m_authenticationHandlerPresenter.setAnnotationType(JaxWsRuntimeClasses.ScoutWebServiceClient);
      m_authenticationHandlerPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER);
      m_authenticationHandlerPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedConsumerSecurityPackageName(m_bundle));
      m_authenticationHandlerPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_authenticationHandlerPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(JaxWsRuntimeClasses.IAuthenticationHandlerConsumer));
      m_authenticationHandlerPresenter.setAllowChangeOfInterfaceType(true);
      m_authenticationHandlerPresenter.setInterfaceTypes(new IType[]{JaxWsRuntimeClasses.IAuthenticationHandlerConsumer});
      m_authenticationHandlerPresenter.addValueChangedListener(m_presenterListener);
      m_authenticationHandlerPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_authenticationHandlerPresenter);

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

      // port type
      m_portTypePresenter = new StringPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_portTypePresenter.setLabel(Texts.get("PortType"));
      m_portTypePresenter.setBundle(m_bundle);
      m_portTypePresenter.setMarkerType(MarkerType.PortType);
      m_portTypePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_portTypePresenter.setEditable(false);
      applyLayoutData(m_portTypePresenter);

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
      m_stubJarFilePresenter.setLabel(Texts.get("StubJar"));
      m_stubJarFilePresenter.setShowBrowseButton(false);
      m_stubJarFilePresenter.setBundle(m_bundle);
      m_stubJarFilePresenter.setMarkerType(MarkerType.StubJar);
      m_stubJarFilePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_stubJarFilePresenter);

      // wsdl file
      m_wsdlFilePresenter = new WsdlFilePresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_wsdlFilePresenter.setLabel(Texts.get("WsdlFile"));
      m_wsdlFilePresenter.setBundle(m_bundle);
      m_wsdlFilePresenter.setMarkerType(MarkerType.Wsdl);
      m_wsdlFilePresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_wsdlFilePresenter.setBuildJaxWsBean(getPage().getBuildJaxWsBean());
      applyLayoutData(m_wsdlFilePresenter);

      updatePresenterValues();
    }
    finally {
      getForm().setRedraw(true);
    }
    super.createSections();

    if (getSection(JdtTypePropertyPart.SECTION_ID_PROPERTIES) != null) {
      getSection(JdtTypePropertyPart.SECTION_ID_PROPERTIES).setExpanded(false);
    }
  }

  private void updatePresenterValues() {
    createQuickLinkPresenters();
    createQuickLinkPresentersForReferencedFiles();

    BuildJaxWsBean buildJaxWsBean = getPage().getBuildJaxWsBean();
    Definition wsdlDefinition = getPage().getWsdlDefinition();

    String serviceName = null;
    String serviceTooltip = null;
    String portTypeName = null;
    String portTypeTooltip = null;
    IFile stubJarFile = null;

    QName serviceQName = JaxWsSdkUtility.extractServiceQNameFromWsClient(getPage().getType());
    if (serviceQName != null) {
      serviceName = serviceQName.getLocalPart();
      serviceTooltip = serviceQName.toString();
    }
    QName portTypeQName = JaxWsSdkUtility.extractPortTypeQNameFromWsClient(getPage().getType());
    if (portTypeQName != null) {
      portTypeName = portTypeQName.getLocalPart();
      portTypeTooltip = portTypeQName.toString();
    }

    if (buildJaxWsBean != null) {
      stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, buildJaxWsBean, buildJaxWsBean.getWsdl());
    }

    m_targetNamespacePresenter.setInput(wsdlDefinition != null ? wsdlDefinition.getTargetNamespace() : null);
    m_targetNamespacePresenter.setTooltip(m_targetNamespacePresenter.getValue());
    m_servicePresenter.setInput(serviceName);
    m_servicePresenter.setTooltip(serviceTooltip);
    m_portTypePresenter.setInput(portTypeName);
    m_portTypePresenter.setTooltip(portTypeTooltip);
    m_wsdlFilePresenter.setInput(getPage().getWsdlResource().getFile());
    m_stubJarFilePresenter.setInput(stubJarFile);
    m_stubFolderPresenter.setInput(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.STUB_FOLDER, false));

    IAnnotation scoutWebServiceClientAnnotation = JaxWsSdkUtility.getAnnotation(getPage().getType(), JaxWsRuntimeClasses.ScoutWebServiceClient.getFullyQualifiedName(), false);
    getSection(SECTION_ID_SCOUT_WEB_SERVICE_CLIENT_ANNOTATION).setVisible(TypeUtility.exists(scoutWebServiceClientAnnotation));
    if (TypeUtility.exists(scoutWebServiceClientAnnotation)) {
      AnnotationProperty propertyValue = JaxWsSdkUtility.parseAnnotationTypeValue(getPage().getType(), scoutWebServiceClientAnnotation, JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER);
      m_authenticationHandlerPresenter.setInput(propertyValue.getFullyQualifiedName());
      m_authenticationHandlerPresenter.setDeclaringType(getPage().getType());
      m_authenticationHandlerPresenter.setResetLinkVisible(!propertyValue.isInherited());
      m_authenticationHandlerPresenter.setBoldLabelText(!propertyValue.isInherited());
    }

    createBindingFilePresenters(buildJaxWsBean);
    validateRebuildStubPresenter();

    getSection(SECTION_ID_REPAIR).setVisible(JaxWsSdk.getDefault().containsMarkerCommands(getPage().getMarkerGroupUUID()));
  }

  private void createQuickLinkPresenters() {
    getForm().setRedraw(false);

    try {
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS).getSectionClient());

      createQuickLinkForTypesPresenters();

      SeparatorPresenter separatorPresenter = new SeparatorPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), getFormToolkit());
      applyLayoutData(separatorPresenter);

      // QuickLink 'Open build-jaxws.xml'
      FileOpenAction a = new FileOpenAction();
      a.init(ResourceFactory.getBuildJaxWsResource(m_bundle).getFile(), ResourceFactory.getBuildJaxWsResource(m_bundle).getFile().getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile), FileExtensionType.Xml);
      a.setToolTip(Texts.get("JaxWsBuildDescriptor"));
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), a, getFormToolkit());
      applyLayoutData(actionPresenter);

      if (getPage().getBuildJaxWsBean() == null) {
        return;
      }

      // QuickLink 'Open WSDL file'
      IFile wsdlFile = getPage().getWsdlResource().getFile();
      if (wsdlFile != null) {
        FileOpenAction b = new FileOpenAction();
        b.init(wsdlFile, wsdlFile.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), FileExtensionType.Auto);
        b.setToolTip("Web Services Description Language");
        actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), b, getFormToolkit());
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
      /*
       * referenced WSDL definitions
       */
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS_REF_WSDLS).getSectionClient());

      Definition wsdlDefinition = getPage().getWsdlDefinition();
      if (wsdlDefinition != null) {
        DefinitionBean[] relatedWsdlDefinitions = JaxWsSdkUtility.getRelatedDefinitions(m_bundle, wsdlDefinition);
        for (DefinitionBean relatedWsdlDefinition : relatedWsdlDefinitions) {
          IFile file = relatedWsdlDefinition.getFile();
          FileOpenAction action = new FileOpenAction();
          action.init(file, file.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), FileExtensionType.Auto);
          String namespace = StringUtility.nvl(relatedWsdlDefinition.getNamespaceUri(), "?");
          action.setToolTip("namespace: " + namespace);

          ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS_REF_WSDLS).getSectionClient(), action, getFormToolkit());
          actionPresenter.setEnabled(file.exists());
          applyLayoutData(actionPresenter);
        }
      }

      /*
       * referenced XSD schemas
       */
      JaxWsSdkUtility.disposeChildControls(getSection(SECTION_ID_LINKS_REF_SCHEMAS).getSectionClient());

      WsdlResource wsdlResource = getPage().getWsdlResource();
      if (wsdlResource != null) {
        SchemaBean[] schemas = JaxWsSdkUtility.getAllSchemas(m_bundle, wsdlResource);
        for (SchemaBean schemaBean : schemas) {
          Schema schema = schemaBean.getSchema();

          if (schema == null) {
            continue;
          }
          // add included schemas
          @SuppressWarnings("unchecked")
          List<SchemaReference> schemaReferences = schema.getIncludes();
          for (SchemaReference schemaReference : schemaReferences) {
            String location = schemaReference.getSchemaLocationURI();
            IFile file = JaxWsSdkUtility.getFile(m_bundle, JaxWsConstants.PATH_WSDL, location, false);

            // action
            FileOpenAction action = new FileOpenAction();
            action.init(file, file.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), FileExtensionType.Auto);
            String targetNamespace = null;
            if (schema.getElement().hasAttribute("targetNamespace")) {
              targetNamespace = schema.getElement().getAttribute("targetNamespace");
            }
            action.setToolTip("targetNamespace: " + StringUtility.nvl(targetNamespace, "?") + " (included schema)");

            // presenter
            ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS_REF_SCHEMAS).getSectionClient(), action, getFormToolkit());
            actionPresenter.setEnabled(file.exists());
            applyLayoutData(actionPresenter);
          }

          // add imported schemas
          @SuppressWarnings("unchecked")
          Map<String, List<SchemaImport>> schemaImports = schema.getImports();
          for (List<SchemaImport> schemaImportList : schemaImports.values()) {
            for (SchemaImport schemaImport : schemaImportList) {
              String location = schemaImport.getSchemaLocationURI();
              IFile file = JaxWsSdkUtility.getFile(m_bundle, JaxWsConstants.PATH_WSDL, location, false);

              FileOpenAction b = new FileOpenAction();
              b.init(file, file.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), FileExtensionType.Auto);
              b.setToolTip("namespace: " + StringUtility.nvl(schemaImport.getNamespaceURI(), "?") + " (imported schema)");
              ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS_REF_SCHEMAS).getSectionClient(), b, getFormToolkit());
              actionPresenter.setEnabled(file.exists());
              applyLayoutData(actionPresenter);
            }
          }
        }
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
    }
    finally {
      getForm().setRedraw(true);

      getSection(SECTION_ID_LINKS_REF_WSDLS).setVisible(getSection(SECTION_ID_LINKS_REF_WSDLS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS_REF_WSDLS));

      getSection(SECTION_ID_LINKS_REF_SCHEMAS).setVisible(getSection(SECTION_ID_LINKS_REF_SCHEMAS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS_REF_SCHEMAS));
    }
  }

  private void createQuickLinkForTypesPresenters() {
    // QuickLink 'Open PortType'
    TypeOpenAction a = new TypeOpenAction();
    a.init(getPage().getType());
    ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), a, getFormToolkit());
    applyLayoutData(actionPresenter);

    // QuickLink 'Open PortType Interface'
    IType portTypeInterfaceType = JaxWsSdkUtility.extractGenericSuperType(getPage().getType(), JaxWsConstants.GENERICS_WEBSERVICE_CLIENT_PORT_TYPE_INDEX);
    try {
      if (TypeUtility.exists(portTypeInterfaceType) && portTypeInterfaceType.isInterface()) {
        IAnnotation annotation = JaxWsSdkUtility.getAnnotation(portTypeInterfaceType, WebService.class.getName(), false);
        if (TypeUtility.exists(annotation)) {
          TypeOpenAction action = new TypeOpenAction();
          action.init(portTypeInterfaceType);
          action.setToolTip(Texts.get("JaxWsPortTypeInterface"));
          ActionPresenter presenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), action, getFormToolkit());
          applyLayoutData(presenter);
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("failed to validate port type interface", e);
    }
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
        BindingFilePresenter filePresenter = new BindingFilePresenter(m_bindingFilesComposite, getFormToolkit());
        filePresenter.setBundle(m_bundle);
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
        !MarkerUtility.containsMarker(m_bundle, MarkerType.Wsdl, getPage().getMarkerGroupUUID(), IMarker.SEVERITY_ERROR));
    m_rebuildStubPresenter.setEnabled(valid);

    if (valid) {
      StubGenerationAction action = new StubGenerationAction();
      action.init(m_bundle, getPage().getBuildJaxWsBean(), getPage().getWsdlResource(), getPage().getMarkerGroupUUID(), WebserviceEnum.Consumer);
      m_rebuildStubPresenter.setAction(action);
    }
  }

  private final class P_PresenterListener implements IPresenterValueChangedListener {

    @Override
    public void propertyChanged(int presenterId, Object value) {
      switch (presenterId) {
        case PRESENTER_ID_AUTHENTICATION_HANDLER:
          IType type = getPage().getType();
          String factoryFullyQualifiedName = (String) value;
          if (!ScoutSdk.existsType(factoryFullyQualifiedName)) {
            return;
          }
          IType factoryType = ScoutSdk.getType(factoryFullyQualifiedName);

          AnnotationUpdateOperation op = new AnnotationUpdateOperation();
          op.setDeclaringType(type);
          op.setAnnotationType(JaxWsRuntimeClasses.ScoutWebServiceClient);
          op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, factoryType);
          new OperationJob(op).schedule();
          break;
      }
    }
  }

  private class P_ScoutSeverityListener implements IScoutSeverityListener {

    @Override
    public void severityChanged(IResource resource) {
      boolean accept = false;
      if (resource == getPage().getType().getResource()) {
        accept = true;
      }
      if (!accept && resource == ResourceFactory.getSunJaxWsResource(m_bundle).getFile()) {
        accept = true;
      }
      if (!accept && resource == ResourceFactory.getBuildJaxWsResource(m_bundle).getFile()) {
        accept = true;
      }
      if (!accept && resource == getPage().getWsdlResource().getFile()) {
        accept = true;
      }
      if (!accept) {
        for (XmlResource bindingFileResource : getPage().getBindingFileResources()) {
          if (resource == bindingFileResource) {
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
          m_portTypePresenter.updateInfo();
          m_wsdlFilePresenter.updateInfo();
          m_authenticationHandlerPresenter.updateInfo();
          m_stubJarFilePresenter.updateInfo();
          m_stubFolderPresenter.updateInfo();

          for (BindingFilePresenter presenter : m_bindingFilePresenters) {
            presenter.updateInfo();
          }

          validateRebuildStubPresenter();
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
