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

import javax.jws.WebService;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction.FileExtensionType;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.RepairAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.TypeOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderCodeFirstNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.AnnotationPropertyTypePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.SeparatorPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.TypePresenter.ISearchJavaSearchScopeFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.UrlPatternPresenter;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPresenterValueChangedListener;
import org.eclipse.swt.layout.GridData;

public class WebServiceProviderCodeFirstNodePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_REPAIR = "section.jaxws.repair";
  public static final String SECTION_ID_PROPERTIES = "section.jaxws.properties";
  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION = "section.scoutWebServiceAnnotation";

  public static final int PRESENTER_ID_SESSION_FACTORY = 1 << 1;
  public static final int PRESENTER_ID_AUTHENTICATION_HANDLER = 1 << 2;
  public static final int PRESENTER_ID_CREDENTIAL_VALIDATION_STRATEGY = 1 << 3;

  private IPresenterValueChangedListener m_presenterListener;

  private UrlPatternPresenter m_urlPatternPresenter;

  private P_ScoutSeverityListener m_severityListener;
  private IPageLoadedListener m_pageLoadedListener;

  private AnnotationPropertyTypePresenter m_sessionFactoryPresenter;
  private AnnotationPropertyTypePresenter m_authenticationHandlerPresenter;
  private AnnotationPropertyTypePresenter m_credentialValidationStrategyPresenter;

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutResource();
    m_presenterListener = new P_PresenterListener();
    m_severityListener = new P_ScoutSeverityListener();
    m_pageLoadedListener = new P_PageLoadedListener();
    ScoutSeverityManager.getInstance().addQualityManagerListener(m_severityListener);
    getPage().addPageLoadedListener(m_pageLoadedListener);
  }

  @Override
  protected void cleanup() {
    ScoutSeverityManager.getInstance().removeQualityManagerListener(m_severityListener);
    getPage().removePageLoadedListener(m_pageLoadedListener);
  }

  @Override
  public WebServiceProviderCodeFirstNodePage getPage() {
    return (WebServiceProviderCodeFirstNodePage) super.getPage();
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
      m_authenticationHandlerPresenter.setAnnotationType(JaxWsRuntimeClasses.ScoutWebService);
      m_authenticationHandlerPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER);
      m_authenticationHandlerPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      m_authenticationHandlerPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_authenticationHandlerPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(JaxWsRuntimeClasses.IAuthenticationHandlerProvider));
      m_authenticationHandlerPresenter.setAllowChangeOfInterfaceType(true);
      m_authenticationHandlerPresenter.setInterfaceTypes(new IType[]{JaxWsRuntimeClasses.IAuthenticationHandlerProvider});
      m_authenticationHandlerPresenter.addValueChangedListener(m_presenterListener);
      m_authenticationHandlerPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_authenticationHandlerPresenter);

      // Scout credential validation strategy
      m_credentialValidationStrategyPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).getSectionClient(), getFormToolkit());
      m_credentialValidationStrategyPresenter.setPresenterId(PRESENTER_ID_CREDENTIAL_VALIDATION_STRATEGY);
      m_credentialValidationStrategyPresenter.setLabel(Texts.get("CredentialValidation"));
      m_credentialValidationStrategyPresenter.setAcceptNullValue(true);
      m_credentialValidationStrategyPresenter.setBundle(m_bundle);
      m_credentialValidationStrategyPresenter.setAnnotationType(JaxWsRuntimeClasses.ScoutWebService);
      m_credentialValidationStrategyPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_CREDENTIAL_STRATEGY);
      m_credentialValidationStrategyPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      m_credentialValidationStrategyPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_credentialValidationStrategyPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(JaxWsRuntimeClasses.ICredentialValidationStrategy));
      m_credentialValidationStrategyPresenter.setAllowChangeOfInterfaceType(true);
      m_credentialValidationStrategyPresenter.setInterfaceTypes(new IType[]{JaxWsRuntimeClasses.ICredentialValidationStrategy});
      m_credentialValidationStrategyPresenter.addValueChangedListener(m_presenterListener);
      m_credentialValidationStrategyPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_credentialValidationStrategyPresenter);

      // Scout session factory
      m_sessionFactoryPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_SCOUT_WEB_SERVICE_ANNOTATION).getSectionClient(), getFormToolkit());
      m_sessionFactoryPresenter.setPresenterId(PRESENTER_ID_SESSION_FACTORY);
      m_sessionFactoryPresenter.setLabel(Texts.get("Session"));
      m_sessionFactoryPresenter.setAcceptNullValue(true);
      m_sessionFactoryPresenter.setBundle(m_bundle);
      m_sessionFactoryPresenter.setAnnotationType(JaxWsRuntimeClasses.ScoutWebService);
      m_sessionFactoryPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY);
      m_sessionFactoryPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedProviderSecurityPackageName(m_bundle));
      m_sessionFactoryPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      m_sessionFactoryPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(JaxWsRuntimeClasses.IServerSessionFactory));
      m_sessionFactoryPresenter.setAllowChangeOfInterfaceType(true);
      m_sessionFactoryPresenter.setInterfaceTypes(new IType[]{JaxWsRuntimeClasses.IServerSessionFactory});
      m_sessionFactoryPresenter.addValueChangedListener(m_presenterListener);
      m_sessionFactoryPresenter.getContainer().setLayoutData(new GridData());
      applyLayoutData(m_sessionFactoryPresenter);

      /*
       * properties section
       */
      createSection(SECTION_ID_PROPERTIES, Texts.get("WebserviceProperties"));
      getSection(SECTION_ID_PROPERTIES).setExpanded(true);

      // URL pattern
      m_urlPatternPresenter = new UrlPatternPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_urlPatternPresenter.setLabel(Texts.get("UrlPattern"));
      m_urlPatternPresenter.setBundle(m_bundle);
      m_urlPatternPresenter.setMarkerType(MarkerType.UrlPattern);
      m_urlPatternPresenter.setMarkerGroupUUID(getPage().getMarkerGroupUUID());
      applyLayoutData(m_urlPatternPresenter);

      updatePresenterValues();
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  private void updatePresenterValues() {
    createQuickLinkPresenters();

    SunJaxWsBean sunJaxWsBean = getPage().getSunJaxWsBean();

    String urlPattern = null;

    if (sunJaxWsBean != null) {
      urlPattern = sunJaxWsBean.getUrlPattern();
    }

    m_urlPatternPresenter.setInput(urlPattern);
    m_urlPatternPresenter.setSunJaxWsBean(sunJaxWsBean);
    String servletRegistrationBundleName = ServletRegistrationUtility.getBuildJaxServletRegistrationBundleName(m_bundle);
    if (servletRegistrationBundleName != null) {
      m_urlPatternPresenter.setTooltip(Texts.get("JaxWsServletRegistrationInBundleX", servletRegistrationBundleName));
    }
    else {
      m_urlPatternPresenter.setTooltip(null);
    }

    IType portType = getPage().getPortType();
    if (portType != null) {
      IAnnotation scoutWebServiceAnnotation = JaxWsSdkUtility.getAnnotation(portType, JaxWsRuntimeClasses.ScoutWebService.getFullyQualifiedName(), false);
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

    getSection(SECTION_ID_REPAIR).setVisible(JaxWsSdk.getDefault().containsMarkerCommands(getPage().getMarkerGroupUUID()));
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
    }
    finally {
      getForm().setRedraw(true);
      getSection(SECTION_ID_LINKS).setVisible(getSection(SECTION_ID_LINKS).getSectionClient().getChildren().length > 0);
      JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_LINKS));
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
    IType portType = getPage().getPortType();
    if (!TypeUtility.exists(portType)) {
      return;
    }
    IAnnotation annotation = JaxWsSdkUtility.getAnnotation(portType, WebService.class.getName(), false);
    if (annotation == null) {
      return;
    }
    AnnotationProperty property = JaxWsSdkUtility.parseAnnotationTypeValue(portType, annotation, "endpointInterface");
    IType portTypeInterfaceType = TypeUtility.getType(property.getFullyQualifiedName());
    if (portTypeInterfaceType == null) {
      return;
    }

    TypeOpenAction action = new TypeOpenAction();
    action.init(portTypeInterfaceType);
    action.setToolTip(Texts.get("JaxWsPortTypeInterface"));
    ActionPresenter presenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), action, getFormToolkit());
    applyLayoutData(presenter);
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
            op.setAnnotationType(JaxWsRuntimeClasses.ScoutWebService);
            switch (presenterId) {
              case PRESENTER_ID_SESSION_FACTORY:
                op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, factoryType);
                break;
              case PRESENTER_ID_AUTHENTICATION_HANDLER:
                op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_AUTH_HANDLER, factoryType);
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
      if (!accept && getPage().getPortType() != null && resource == getPage().getPortType().getResource()) {
        accept = true;
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

          // update marker status of presenters (quality)
          m_urlPatternPresenter.updateInfo();
          m_sessionFactoryPresenter.updateInfo();
          m_authenticationHandlerPresenter.updateInfo();
          m_credentialValidationStrategyPresenter.updateInfo();
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
          updatePresenterValues();
        }
      });
    }
  }
}
