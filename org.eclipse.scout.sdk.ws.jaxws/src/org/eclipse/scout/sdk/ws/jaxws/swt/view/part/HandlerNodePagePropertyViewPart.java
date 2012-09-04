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
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.TypeOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.HandlerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.AnnotationPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.AnnotationPropertyTypePresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.TypePresenter.ISearchJavaSearchScopeFactory;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPresenterValueChangedListener;
import org.eclipse.swt.layout.GridData;

public class HandlerNodePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_PROPERTIES = "section.jaxws.properties";

  public static final int PRESENTER_ID_SESSION_FACTORY = 1 << 1;

  private IPresenterValueChangedListener m_presenterListener;

  private AnnotationPresenter m_transactionalPresenter;
  private AnnotationPropertyTypePresenter m_sessionFactoryPresenter;

  private P_ScoutSeverityListener m_severityListener;
  private IPageLoadedListener m_pageLoadedListener;

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutResource();
    m_presenterListener = new P_PresenterListener();
    m_pageLoadedListener = new P_PageLoadedListener();
    m_severityListener = new P_ScoutSeverityListener();
    getPage().addPageLoadedListener(m_pageLoadedListener);
  }

  @Override
  protected void cleanup() {
    getPage().removePageLoadedListener(m_pageLoadedListener);
    ScoutSeverityManager.getInstance().removeQualityManagerListener(m_severityListener);
    super.cleanup();
  }

  @Override
  public HandlerNodePage getPage() {
    return (HandlerNodePage) super.getPage();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(true);
    try {
      createSection(SECTION_ID_LINKS, Texts.get("ConsiderLinks"));
      createSection(SECTION_ID_PROPERTIES, Texts.get("Properties"));
      if (getPage().getType().isBinary()) {
        getSection(SECTION_ID_PROPERTIES).setText(Texts.get("Properties") + " (" + Texts.get("readOnlyBecauseBinaryFile") + ")");
      }
      // QuickLink 'Open Type'
      TypeOpenAction action = new TypeOpenAction();
      action.init(getPage().getType());
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), action, getFormToolkit());
      applyLayoutData(actionPresenter);

      m_transactionalPresenter = new AnnotationPresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit(), getPage().getType(), TypeUtility.getType(JaxWsRuntimeClasses.ScoutTransaction));
      m_transactionalPresenter.setLabel(Texts.get("Transactional"));
      applyLayoutData(m_transactionalPresenter);

      // Scout session factory
      m_sessionFactoryPresenter = new AnnotationPropertyTypePresenter(getSection(SECTION_ID_PROPERTIES).getSectionClient(), getFormToolkit());
      m_sessionFactoryPresenter.setPresenterId(PRESENTER_ID_SESSION_FACTORY);
      m_sessionFactoryPresenter.setLinkAlwaysEnabled(true);
      m_sessionFactoryPresenter.setLabel(Texts.get("SessionFactory"));
      m_sessionFactoryPresenter.setAcceptNullValue(true);
      m_sessionFactoryPresenter.setBundle(m_bundle);
      m_sessionFactoryPresenter.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutTransaction));
      m_sessionFactoryPresenter.setProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY);
      m_sessionFactoryPresenter.setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedSessionPackageName(m_bundle));
      m_sessionFactoryPresenter.setSearchScopeFactory(createSubClassesSearchScopeFactory(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory)));
      m_sessionFactoryPresenter.setAllowChangeOfInterfaceType(true);
      m_sessionFactoryPresenter.setInterfaceTypes(new IType[]{TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory)});
      m_sessionFactoryPresenter.addValueChangedListener(m_presenterListener);
      applyLayoutData(m_sessionFactoryPresenter);

      updatePresenterValues();
    }
    finally {
      getForm().setRedraw(true);
    }
    super.createSections();
  }

  private void updatePresenterValues() {
    m_transactionalPresenter.updatePresenter();

    IAnnotation scoutTransactionAnnotation = m_transactionalPresenter.getValue();
    boolean transactional = (scoutTransactionAnnotation != null);
    GridData gd = (GridData) m_sessionFactoryPresenter.getContainer().getLayoutData();
    gd.exclude = !transactional;
    m_sessionFactoryPresenter.getContainer().setVisible(transactional);
    JaxWsSdkUtility.doLayout(m_sessionFactoryPresenter.getContainer());
    JaxWsSdkUtility.doLayoutSection(getSection(SECTION_ID_PROPERTIES));

    if (scoutTransactionAnnotation != null) {
      AnnotationProperty propertyValue = JaxWsSdkUtility.parseAnnotationTypeValue(getPage().getType(), scoutTransactionAnnotation, JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY);
      m_sessionFactoryPresenter.setInput(propertyValue.getFullyQualifiedName());
      m_sessionFactoryPresenter.setDeclaringType(getPage().getType());
      m_sessionFactoryPresenter.setResetLinkVisible(!propertyValue.isInherited());
      m_sessionFactoryPresenter.setBoldLabelText(!propertyValue.isInherited());
      m_sessionFactoryPresenter.updateInfo();
    }

    if (getPage().getType().isBinary()) {
      m_transactionalPresenter.setEnabled(false);
      m_sessionFactoryPresenter.setEnabled(false);
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

  private final class P_PresenterListener implements IPresenterValueChangedListener {

    @Override
    public void propertyChanged(int presenterId, Object value) {
      switch (presenterId) {
        case PRESENTER_ID_SESSION_FACTORY:
          IType type = getPage().getType();
          if (!TypeUtility.exists(type)) {
            return;
          }
          String factoryFullyQualifiedName = (String) value;
          if (!TypeUtility.existsType(factoryFullyQualifiedName)) {
            return;
          }
          IType factoryType = TypeUtility.getType(factoryFullyQualifiedName);
          AnnotationUpdateOperation op = new AnnotationUpdateOperation();
          op.setDeclaringType(type);
          op.setAnnotationType(TypeUtility.getType(JaxWsRuntimeClasses.ScoutTransaction));
          op.addTypeProperty(JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY, factoryType);
          new OperationJob(op).schedule();
          break;
      }
    }
  }

  private class P_PageLoadedListener implements IPageLoadedListener {

    @Override
    public void pageLoaded() {
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          if (getForm().isDisposed()) {
            return;
          }
          updatePresenterValues();
        }
      });
    }
  }

  private class P_ScoutSeverityListener implements IScoutSeverityListener {

    @Override
    public void severityChanged(IResource resource) {
      if (getPage().getType() != null && resource == getPage().getType().getResource()) {
        ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

          @Override
          public void run() {
            if (getForm().isDisposed()) {
              return;
            }

            // update marker status of presenters (quality)
            m_sessionFactoryPresenter.updateInfo();
          }
        });
      }
    }
  }
}
