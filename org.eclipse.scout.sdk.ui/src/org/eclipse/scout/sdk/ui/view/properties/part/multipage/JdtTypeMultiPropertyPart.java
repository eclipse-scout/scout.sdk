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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.scout.sdk.util.jdt.AbstractElementChangedListener;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyTypeSet;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>JdtTypeMultiPropertyPart</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 23.07.2010
 */
public class JdtTypeMultiPropertyPart extends AbstractMultiPageSectionBasedViewPart {
  private static final String SECTION_ID_PROPERTIES = "section.properties";

  private final Map<String, AbstractMultiMethodPresenter<?>> m_methodPresenters;

  private IElementChangedListener m_methodChangedListener;
  private ConfigPropertyTypeSet m_configPropertyTypeSet;
  private P_DelayedUpdateJob m_updateJob;

  public JdtTypeMultiPropertyPart() {
    m_methodPresenters = new HashMap<>();
  }

  @Override
  protected void createSections() {
    List<IType> types = new ArrayList<>(getPages().length);
    for (IPage p : getPages()) {
      if (p instanceof AbstractScoutTypePage) {
        types.add(((AbstractScoutTypePage) p).getType());
      }
      else {
        return;
      }
    }
    try {
      m_configPropertyTypeSet = new ConfigPropertyTypeSet(types);
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
    catch (CoreException e) {
      ScoutSdkUi.logError("Unable to create multi property part.", e);
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

    if ("BOOLEAN".equals(propertyType)) {
      presenter = new MultiBooleanPresenter(getFormToolkit(), parent);
      presenter.setMethodSet(methodSet);
    }
    else if ("INTEGER".equals(propertyType)) {
      presenter = new MultiIntegerPresenter(getFormToolkit(), parent);
      presenter.setMethodSet(methodSet);
    }
    else if ("LONG".equals(propertyType)) {
      presenter = new MultiLongPresenter(getFormToolkit(), parent);
      presenter.setMethodSet(methodSet);
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
      try {
        ConfigurationMethod updatedMethod = m_configPropertyTypeSet.updateIfChanged(method);
        if (updatedMethod != null) {
          AbstractMultiMethodPresenter presenter = m_methodPresenters.get(updatedMethod.getMethodName());
          if (presenter != null) {
            m_updateJob.update(presenter, m_configPropertyTypeSet.getConfigurationMethodSet(updatedMethod.getMethodName()));
          }
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("Unable to update method '" + method.getElementName() + "'.", e);
      }
    }
  }

  private class P_MethodChangedListener2 extends AbstractElementChangedListener {
    @Override
    protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
      if (e != null && e.getElementType() == IJavaElement.METHOD) {
        handleMethodChanged((IMethod) e);
        return false;
      }
      return super.visit(kind, flags, e, ast);
    }
  } // end class P_MethodChangedListener2

  private static final class P_DelayedUpdateJob extends Job {
    private final Object m_delayedUpdateLock = new Object();
    private final Display m_display;
    private AbstractMultiMethodPresenter m_presenter;
    private ConfigurationMethodSet m_methodSet;

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
