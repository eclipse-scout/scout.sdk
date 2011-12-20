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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.part.AnnotationProperty;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPresenterValueChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class HandlerPresenter extends TypePresenter {

  private SunJaxWsBean m_sunJaxWsBean;
  private ScoutXmlElement m_xmlHandlerElement;
  private IPresenterValueChangedListener m_valueChangedListener;
  private int m_handlerIndex;
  private int m_handlerCount;

  public HandlerPresenter(IScoutBundle bundle, Composite parent, int handlerIndex, int handlerCount, FormToolkit toolkit) {
    super(parent, toolkit, 70, false);
    m_handlerIndex = handlerIndex;
    m_handlerCount = handlerCount;
    setMarkerType(MarkerType.HandlerClass);
    setBundle(bundle);
    setAcceptNullValue(false);
    setUseLinkAsLabel(true);
    setAllowChangeOfInterfaceType(false);
    setAllowChangeOfSourceFolder(false);
    setResetLinkVisible(true);
    setResetTooltip(Texts.get("TooltipRemoveHandlerRegistration"));
    setDefaultPackageNameNewType(JaxWsSdkUtility.getRecommendedHandlerPackageName(m_bundle));
    m_valueChangedListener = new P_ValueChangedListener();
    addValueChangedListener(m_valueChangedListener);

    String interfaceSignature = Signature.createTypeSignature(SOAPHandler.class.getName() + "<" + SOAPMessageContext.class.getName() + ">", false);
    IType interfaceType = TypeUtility.getTypeBySignature(interfaceSignature);
    setInterfaceSignatures(new String[]{interfaceSignature});
    setSearchScopeFactory(createSubClassesSearchScopeFactory(interfaceType));

    callInitializer();
  }

  @Override
  protected Control createContent(Composite parent) {
    Composite composite = getToolkit().createComposite(parent, SWT.NONE);

    Control content = super.createContent(composite);

    ImageHyperlink upButton = getToolkit().createImageHyperlink(composite, SWT.NONE);
    upButton.setImage(JaxWsSdk.getImage(JaxWsIcons.Up));
    upButton.setToolTipText(Texts.get("ClickToIncreasePrecendence"));
    upButton.setEnabled(m_handlerIndex > 0);
    upButton.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
        ScoutXmlElement xmlChain = m_xmlHandlerElement.getParent();
        if (m_sunJaxWsBean.swapHandler(xmlChain, m_handlerIndex, m_handlerIndex - 1)) {
          // persist
          ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
        }
      }
    });

    ImageHyperlink downButton = getToolkit().createImageHyperlink(composite, SWT.NONE);
    downButton.setImage(JaxWsSdk.getImage(JaxWsIcons.Down));
    downButton.setToolTipText(Texts.get("ClickToLowerPrecendence"));
    downButton.setEnabled(m_handlerIndex < m_handlerCount - 1);
    downButton.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
        ScoutXmlElement xmlChain = m_xmlHandlerElement.getParent();
        if (m_sunJaxWsBean.swapHandler(xmlChain, m_handlerIndex, m_handlerIndex + 1)) {
          // persist
          ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
        }
      }
    });
    composite.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(upButton, -2, SWT.LEFT);
    content.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(2, 0);
    formData.right = new FormAttachment(100, 0);
    upButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 12);
    formData.right = new FormAttachment(100, 0);
    downButton.setLayoutData(formData);

    return composite;
  }

  @Override
  public void dispose() {
    if (m_valueChangedListener != null) {
      removeValueChangedListener(m_valueChangedListener);
      m_valueChangedListener = null;
    }
    super.dispose();
  }

  @Override
  protected void execResetAction() throws CoreException {
    // remove itself
    m_xmlHandlerElement.getParent().removeChild(m_xmlHandlerElement);

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }

  public ScoutXmlElement getXmlHandlerElement() {
    return m_xmlHandlerElement;
  }

  public void setXmlHandlerElement(ScoutXmlElement xmlHandlerElement) {
    m_xmlHandlerElement = xmlHandlerElement;
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

  private class P_ValueChangedListener implements IPresenterValueChangedListener {

    @Override
    public void propertyChanged(int presenterId, Object value) {
      String fqn = (String) value;
      String handlerClassElementName = m_sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_CLASS);

      m_xmlHandlerElement.removeChild(handlerClassElementName);
      ScoutXmlElement child = m_xmlHandlerElement.addChild();
      child.setName(handlerClassElementName);
      child.addText(fqn);

      updateTransactionalIcon(fqn);

      // persist
      ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
    }
  }

  private void updateTransactionalIcon(String fqn) {
    IType type = TypeUtility.getType(fqn);
    ImageDescriptor icon = null;
    String tooltip = null;
    if (TypeUtility.exists(type)) {
      IAnnotation annotation = JaxWsSdkUtility.getAnnotation(type, JaxWsRuntimeClasses.ScoutTransaction.getFullyQualifiedName(), false);
      if (annotation != null) {
        icon = JaxWsSdk.getImageDescriptor(JaxWsIcons.Transactional);
        AnnotationProperty prop = JaxWsSdkUtility.parseAnnotationTypeValue(type, annotation, JaxWsRuntimeClasses.PROP_SWS_SESSION_FACTORY);
        tooltip = "This is a transactional handler\nServer session factory: " + Signature.getSimpleName(prop.getFullyQualifiedName());
      }
    }

    setIconImageDescriptor(icon);
    setIconTooltip(tooltip);
  }

  @Override
  public void updateInfo() {
    updateTransactionalIcon(getValue());
    super.updateInfo();
  }
}
