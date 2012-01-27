/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.view.properties.part.multipage;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.multi.MultiBooleanPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.multi.MultiIntegerPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.multi.MultiLongPresenter;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.presenter.multi.AbstractMultiMethodPresenter;
import org.eclipse.scout.sdk.util.jdt.ElementChangedListenerEx;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyTypeSet;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>JdtTypeMultiPropertyPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.07.2010
 */
public class JdtTypeMultiPropertyPart extends AbstractMultiPageSectionBasedViewPart {
  private static final String SECTION_ID_PROPERTIES = "section.properties";

  private IElementChangedListener m_methodChangedListener;
  private ConfigPropertyTypeSet m_configPropertyTypeSet;

  private HashMap<String, AbstractMultiMethodPresenter<?>> m_methodPresenters;
  private P_DelayedUpdateJob m_updateJob;

  public JdtTypeMultiPropertyPart() {
    m_methodPresenters = new HashMap<String, AbstractMultiMethodPresenter<?>>();
  }

  @Override
  protected void createSections() {
    ArrayList<IType> types = new ArrayList<IType>();
    for (IPage p : getPages()) {
      if (p instanceof AbstractScoutTypePage) {
        types.add(((AbstractScoutTypePage) p).getType());
      }
      else {
        return;
      }
    }
    m_configPropertyTypeSet = new ConfigPropertyTypeSet(types.toArray(new IType[types.size()]));
    if (m_configPropertyTypeSet.hasConfigPropertyMethods()) {
      ISection propertySection = createSection(SECTION_ID_PROPERTIES, "Properties");
      for (ConfigurationMethodSet set : m_configPropertyTypeSet.getCommonConfigPropertyMethodSets()) {
        createConfigMethodPresenter(propertySection.getSectionClient(), set);
      }
    }
    super.createSections();
    if (m_updateJob == null) {
      m_updateJob = new P_DelayedUpdateJob(getForm().getDisplay());
    }
    if (m_methodChangedListener == null) {
      m_methodChangedListener = new P_MethodChangedListener2();
      JavaCore.addElementChangedListener(m_methodChangedListener);
    }
  }

  @Override
  public void cleanup() {
    if (m_methodChangedListener != null) {
      JavaCore.removeElementChangedListener(m_methodChangedListener);
      m_methodChangedListener = null;
    }
    if (m_updateJob != null) {
      m_updateJob.cancel();
      m_updateJob = null;
    }
  }

  protected AbstractMultiMethodPresenter createConfigMethodPresenter(Composite parent, ConfigurationMethodSet methodSet) {
    AbstractMultiMethodPresenter presenter = null;
    String propertyType = methodSet.getConfigAnnotationType();

    if (propertyType.equals("BOOLEAN")) {
      presenter = new MultiBooleanPresenter(getFormToolkit(), parent);
      presenter.setMethodSet(methodSet);
    }
    else if (propertyType.equals("DOUBLE")) {
      //TODO: multi double presenter
    }
    else if (propertyType.equals("DRAG_AND_DROP_TYPE")) {
      // TODO: multi DRAG_AND_DROP_TYPE presenter
    }
    else if (propertyType.equals("INTEGER")) {
      presenter = new MultiIntegerPresenter(getFormToolkit(), parent);
      presenter.setMethodSet(methodSet);
    }
    else if (propertyType.equals("LONG")) {
      presenter = new MultiLongPresenter(getFormToolkit(), parent);
      presenter.setMethodSet(methodSet);
    }
    else if (propertyType.equals("STRING")) {
      //TODO: multi string presenter
    }
    else if (propertyType.equals("FONT")) {
      //TODO: multi font presenter
    }
    else if (propertyType.equals("COLOR")) {
      //TODO: multi color presenter
    }
    else if (propertyType.equals("OBJECT")) {
      //TODO: multi object presenter
    }
    else if (propertyType.equals("BUTTON_DISPLAY_STYLE")) {
      //TODO: multi BUTTON_DISPLAY_STYLE presenter
    }
    else if (propertyType.equals("BUTTON_SYSTEM_TYPE")) {
      //TODO: multi BUTTON_SYSTEM_TYPE presenter
    }
    else if (propertyType.equals("CODE_TYPE")) {
      //TODO: multi CODE_TYPE presenter
    }
    else if (propertyType.equals("COMPOSER_ATTRIBUTE_TYPE")) {
      //TODO: multi COMPOSER_ATTRIBUTE_TYPE presenter
    }
    else if (propertyType.equals("FILE_EXTENSIONS")) {
      // TODO: multi FILE_EXTENSIONS presenter
    }
    else if (propertyType.equals("FORM_DISPLAY_HINT")) {
      //TODO: multi FORM_DISPLAY_HINT presenter
    }
    else if (propertyType.equals("FORM_VIEW_ID")) {
      //TODO: multi FORM_VIEW_ID presenter
    }
    else if (propertyType.equals("HORIZONTAL_ALIGNMENT")) {
      //TODO: multi HORIZONTAL_ALIGNMENT presenter
    }
    else if (propertyType.equals("ICON_ID")) {
      //TODO: multi ICON_ID presenter
    }
    else if (propertyType.equals("KEY_STROKE")) {
      // NOT in use
    }
    else if (propertyType.equals("LOOKUP_CALL")) {
      //TODO: multi LOOKUP_CALL presenter
    }
    else if (propertyType.equals("LOOKUP_SERVICE")) {
      //TODO: multi LOOKUP_SERVICE presenter
    }
    else if (propertyType.equals("MASTER_FIELD")) {
      //TODO: multi MASTER_FIELD presenter
    }
    else if (propertyType.equals("OUTLINE_ROOT_PAGE")) {
      //TODO: multi OUTLINE_ROOT_PAGE presenter
    }
    else if (propertyType.equals("OUTLINE")) {
      //TODO: multi OUTLINE presenter
    }
    else if (propertyType.equals("OUTLINES")) {
      //TODO: multi OUTLINES presenter
    }
    else if (propertyType.equals("FORM")) {
      //TODO: multi form presenter
    }
    else if (propertyType.equals("SEARCH_FORM")) {
      //TODO: multi SEARCH_FORM presenter
    }
    else if (propertyType.equals("NLS_PROVIDER")) {
      //TODO: multi NLS_PROVIDER presenter
    }
    else if (propertyType.equals("SQL_STYLE")) {
      //TODO: multi sql_style presenter
    }
    else if (propertyType.equals("SQL")) {
      //TODO: multi SQL presenter
    }
    else if (propertyType.equals("TEXT")) {
      //TODO: multi TEXT presenter
    }
    else if (propertyType.equals("VERTICAL_ALIGNMENT")) {
      //TODO: multi VERTICAL_ALIGNMENT presenter
    }
    else if (propertyType.equals("CHART_QNAME")) {
      //TODO: multi CHART_QNAME presenter
    }
    else if (propertyType.equals("HOUR_OF_DAY")) {
      //TODO: multi HOUR_OF_DAY presenter
    }
    else if (propertyType.equals("DURATION_MINUTES")) {
      //TODO: multi DURATION_MINUTES presenter
    }
    else if (propertyType.equals("MENU_CLASS")) {
      //TODO: multi MENU_CLASS presenter
    }
    else if (propertyType.equals("PRIMITIVE_TYPE")) {
      //TODO: multi PRIMITIVE_TYPE presenter
    }

    // layout
    if (presenter != null) {
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      presenter.getContainer().setLayoutData(layoutData);
      m_methodPresenters.put(methodSet.getMethodName(), presenter);
    }
    else {
      ScoutSdkUi.logInfo("Could not find a multi presenter for property '" + propertyType + "'.");
    }
    return presenter;
  }

  private void handleMethodChanged(IMethod method) {
    if (m_configPropertyTypeSet.isRelevantType(method.getDeclaringType())) {
      ConfigurationMethod updatedMethod = m_configPropertyTypeSet.updateIfChanged(method);
      if (updatedMethod != null) {
        AbstractMultiMethodPresenter presenter = m_methodPresenters.get(updatedMethod.getMethodName());
        if (presenter != null) {
          m_updateJob.update(presenter, m_configPropertyTypeSet.getConfigurationMethodSet(updatedMethod.getMethodName()));

        }
      }
    }
  }

  private class P_MethodChangedListener2 extends ElementChangedListenerEx {
    @Override
    protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
      if (e != null && e.getElementType() == IJavaElement.METHOD) {
        handleMethodChanged((IMethod) e);
        return true;
      }
      return super.visit(kind, flags, e, ast);
    }
  } // end class P_MethodChangedListener2

  private class P_DelayedUpdateJob extends Job {
    private Object m_delayedUpdateLock = new Object();
    private ConfigurationMethodSet m_methodSet;
    private AbstractMultiMethodPresenter m_presenter;
    private final Display m_display;

    /**
     * @param name
     */
    public P_DelayedUpdateJob(Display display) {
      super("");
      m_display = display;
    }

    private void update(AbstractMultiMethodPresenter presenter, ConfigurationMethodSet methodSet) {
      synchronized (m_delayedUpdateLock) {
        cancel();
        setName(Texts.get("UpdatePresenterForX", methodSet.getMethodName()));
        m_presenter = presenter;
        m_methodSet = methodSet;
        schedule(200);
      }
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      synchronized (m_delayedUpdateLock) {
        m_display.syncExec(new Runnable() {
          @Override
          public void run() {
            if (m_presenter.getContainer() != null && !m_presenter.getContainer().isDisposed()) {
              m_presenter.setMethodSet(m_methodSet);
            }
          }
        });
        return Status.OK_STATUS;
      }
    }
  }
}
