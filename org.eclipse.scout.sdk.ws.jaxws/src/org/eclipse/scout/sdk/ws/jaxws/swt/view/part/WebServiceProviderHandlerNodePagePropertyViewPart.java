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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction.FileExtensionType;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.HandlerAddAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.HandlerChainFilterEditAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.HandlerChainNewAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.HandlerChainRemoveAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean.IHandlerVisitor;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderHandlerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.HandlerPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.TypePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.HandlerChainFilterWizardPage.FilterTypeEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

public class WebServiceProviderHandlerNodePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final int PRESENTER_ID_HANDLER = 1 << 1;

  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_HANDLER_CHAIN_PREFIX = "section.jaxws.handlerchain.";

  private P_ScoutSeverityListener m_severityListener;
  private IPageLoadedListener m_pageLoadedListener;

  private List<TypePresenter> m_handlerPresenters;
  private Set<String> m_sectionsIdentifiers;
  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutBundle();
    m_sectionsIdentifiers = new HashSet<String>();
    m_handlerPresenters = new ArrayList<TypePresenter>();

    m_severityListener = new P_ScoutSeverityListener();
    ScoutSeverityManager.getInstance().addQualityManagerListener(m_severityListener);

    m_pageLoadedListener = new P_PageLoadedListener();
    getPage().addPageLoadedListener(m_pageLoadedListener);
  }

  @Override
  protected void cleanup() {
    ScoutSeverityManager.getInstance().removeQualityManagerListener(m_severityListener);
    getPage().removePageLoadedListener(m_pageLoadedListener);
  }

  @Override
  public WebServiceProviderHandlerNodePage getPage() {
    return (WebServiceProviderHandlerNodePage) super.getPage();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(true);
    try {
      /*
       * link section
       */
      createSection(SECTION_ID_LINKS, Texts.get("ConsiderLinks"));

      // QuickLink 'Open sun-jaxws.xml'
      FileOpenAction a = new FileOpenAction();
      a.init(ResourceFactory.getSunJaxWsResource(m_bundle).getFile(), ResourceFactory.getSunJaxWsResource(m_bundle).getFile().getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.SunJaxWsXmlFile), FileExtensionType.Xml);
      a.setToolTip(Texts.get("JaxWsDeploymentDescriptor"));
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), a, getFormToolkit());
      applyLayoutData(actionPresenter);

      if (getPage().getSunJaxWsBean() == null) {
        return;
      }

      // Add Handler Chain
      HandlerChainNewAction action = new HandlerChainNewAction();
      action.init(m_bundle, getPage().getSunJaxWsBean());
      ActionPresenter presenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), action, getFormToolkit());
      presenter.setEnabled(getPage().getSunJaxWsBean() != null);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      gd.horizontalAlignment = SWT.RIGHT;
      presenter.getContainer().setLayoutData(gd);

      createHandlerChainSections(getPage().getSunJaxWsBean());
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }

  private void createHandlerChainSections(SunJaxWsBean sunJaxWsBean) {
    getForm().setRedraw(false);
    try {
      // reset sections
      for (String sectionId : m_sectionsIdentifiers) {
        removeSection(sectionId);
      }
      m_sectionsIdentifiers.clear();

      if (getPage().getSunJaxWsBean() == null) {
        return;
      }

      // create handler chain sections
      List<ScoutXmlElement> xmlHandlerChains = sunJaxWsBean.getHandlerChains();
      for (int i = 0; i < xmlHandlerChains.size(); i++) {
        ScoutXmlElement xmlHandlerChain = xmlHandlerChains.get(i);
        final String sectionId = SECTION_ID_HANDLER_CHAIN_PREFIX + Integer.toString(i);
        m_sectionsIdentifiers.add(sectionId);
        ISection section = createSection(sectionId, "", null, false);

        // set title of section
        String handlerChainName = null;
        if (xmlHandlerChain.hasAttribute("name")) {
          handlerChainName = xmlHandlerChain.getAttribute("name");
        }
        if (StringUtility.hasText(handlerChainName)) {
          section.setText(Texts.get("HandlerChainX", handlerChainName));
        }
        else {
          section.setText(Texts.get("HandlerChain"));
        }

        // Edit Filter
        HandlerChainFilterEditAction filterAction = new HandlerChainFilterEditAction();
        FilterTypeEnum filterType = getFilterType(sunJaxWsBean, xmlHandlerChain);
        if (filterType != FilterTypeEnum.NoFilter) {
          filterAction.setLinkText(Texts.get("EditFilterXActive", filterType.getLabel()));
        }
        filterAction.init(m_bundle, getPage().getSunJaxWsBean(), xmlHandlerChain);
        ActionPresenter presenter = new ActionPresenter(section.getSectionClient(), filterAction, getFormToolkit());
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gd.horizontalAlignment = SWT.RIGHT;
        presenter.getContainer().setLayoutData(gd);

        // Remove Handler Chain
        HandlerChainRemoveAction removeChainAction = new HandlerChainRemoveAction();
        removeChainAction.init(m_bundle, getPage().getSunJaxWsBean(), xmlHandlerChain);
        presenter = new ActionPresenter(section.getSectionClient(), removeChainAction, getFormToolkit());
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gd.horizontalAlignment = SWT.RIGHT;
        presenter.getContainer().setLayoutData(gd);

        // Add Handler
        HandlerAddAction newHandlerAction = new HandlerAddAction();
        newHandlerAction.init(m_bundle, getPage().getSunJaxWsBean(), xmlHandlerChain);
        presenter = new ActionPresenter(section.getSectionClient(), newHandlerAction, getFormToolkit());
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gd.verticalIndent = 20;
        gd.horizontalAlignment = SWT.RIGHT;
        presenter.getContainer().setLayoutData(gd);

        // handler presenters
        sunJaxWsBean.visitHandlers(xmlHandlerChain, new IHandlerVisitor() {

          @Override
          public boolean visit(ScoutXmlElement xmlHandlerElement, String fullyQualifiedName, int handlerIndex, int handlerCount) {
            String handlerClassElementName = getPage().getSunJaxWsBean().toQualifiedName(SunJaxWsBean.XML_HANDLER_CLASS);
            ScoutXmlElement xmlHandlerClassElement = xmlHandlerElement.getChild(handlerClassElementName);
            String handlerClass = null;
            if (xmlHandlerClassElement != null) {
              handlerClass = xmlHandlerClassElement.getText();
            }

            HandlerPresenter p = new HandlerPresenter(m_bundle, getSection(sectionId).getSectionClient(), handlerIndex, handlerCount, getFormToolkit());
            p.setPresenterId(PRESENTER_ID_HANDLER + NumberUtility.randomInt());
            p.setMarkerGroupUUID(JaxWsSdkUtility.toMarkerGroupUUID(getPage().getMarkerGroupUUID(), handlerIndex));
            p.setXmlHandlerElement(xmlHandlerElement);
            p.setSunJaxWsBean(getPage().getSunJaxWsBean());
            p.setInput(handlerClass);

            m_handlerPresenters.add(p);
            applyLayoutData(p);
            return true;
          }
        });
        JaxWsSdkUtility.doLayoutSection(section);
      }
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  private FilterTypeEnum getFilterType(SunJaxWsBean sunJaxWsBean, ScoutXmlElement xmlHandlerChain) {
    if (xmlHandlerChain.hasChild(sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PROTOCOL))) {
      return FilterTypeEnum.ProtocolFilter;
    }
    else if (xmlHandlerChain.hasChild(sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_SERVICE))) {
      return FilterTypeEnum.ServiceFilter;
    }
    else if (xmlHandlerChain.hasChild(sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_FILTER_PORT))) {
      return FilterTypeEnum.PortFilter;
    }
    return FilterTypeEnum.NoFilter;
  }

  private class P_ScoutSeverityListener implements IScoutSeverityListener {

    @Override
    public void severityChanged(IResource resource) {
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (getPage().isPageUnloaded() || getForm().isDisposed()) {
            return;
          }

          // update marker status of presenters (quality)
          for (TypePresenter presenter : m_handlerPresenters) {
            presenter.updateInfo();
          }
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
          createHandlerChainSections(getPage().getSunJaxWsBean());
        }
      });
    }
  }
}
