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
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.jdt.JdtEvent;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.CompilationUnitSaveOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.ExecMethodPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.ExecResetSerchFilterMethodPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BooleanPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonDisplayStylePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonSystemTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.CodeTypeProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ColorPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.DoublePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FontPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormDisplayHintPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormViewIdPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.HorizontalAlignmentPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.IconPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.IntegerPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LongPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupCallProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupServiceProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MasterFieldPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MultiLineStringPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.NlsTextPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.OutlineRootPagePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.OutlinesPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.PrimitiveTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.SearchFormPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.StringPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.VerticalAglinmentPresenter;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyType;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * <h3>JdtTypePropertyPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.07.2010
 */
public class JdtTypePropertyPart extends AbstractSinglePageSectionBasedViewPart {
  protected static final String SECTION_ID_FILTER = "section.filter";
  protected static final String SECTION_ID_PROPERTIES = "section.properties";
  protected static final String SECTION_ID_OPERATIONS = "section.operations";

  private IJavaResourceChangedListener m_methodChangedListener;
  private ConfigPropertyType m_configPropertyType;
  private HashMap<String, AbstractMethodPresenter> m_methodPresenters;

  private HashMap<String, ConfigurationMethod> m_methodsToUpdate;
  private Object m_methodUpdateLock = new Object();
  private P_UpdateMethodsJob m_updateJob;
  private Button m_saveButton;

  private IStatus m_icuNotSyncStatus;
  private P_MarkDirtyJob m_markDirtyJob = new P_MarkDirtyJob();
  private Object m_markDirtyLock = new Object();

  public JdtTypePropertyPart() {
    m_methodsToUpdate = new HashMap<String, ConfigurationMethod>();
    m_methodPresenters = new HashMap<String, AbstractMethodPresenter>();
  }

  @Override
  public AbstractScoutTypePage getPage() {
    return (AbstractScoutTypePage) super.getPage();
  }

  @Override
  public void setPage(IPage page) {
    super.setPage(page);
    m_icuNotSyncStatus = new Status(IStatus.INFO, ScoutSdkUi.PLUGIN_ID, Texts.get("SaveTheFile", getPage().getType().getResource().getName()));
  }

  @Override
  protected Control createHead(Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    Hyperlink title = getFormToolkit().createHyperlink(headArea, getPage().getName(), SWT.WRAP);
    title.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        try {
          JavaUI.openInEditor(getPage().getType());
        }
        catch (Exception e1) {
          ScoutSdkUi.logError("could not open '" + getPage().getType().getElementName() + "' in editor.", e1);
        }
      }
    });
    m_saveButton = getFormToolkit().createButton(headArea, Texts.get("SaveFile"), SWT.PUSH);
    m_saveButton.setEnabled(false);
    m_saveButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ICompilationUnit icu = getPage().getType().getCompilationUnit();
        if (icu.isWorkingCopy()) {
          CompilationUnitSaveOperation op = new CompilationUnitSaveOperation(icu);
          new OperationJob(op).schedule();
        }
      }
    });
    // layout
    headArea.setLayout(new GridLayout(2, false));
    GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    title.setLayoutData(titleData);
    m_saveButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_HORIZONTAL));
    return headArea;
  }

  @Override
  protected void createSections() {
    if (m_updateJob == null) {
      m_updateJob = new P_UpdateMethodsJob(getForm().getDisplay());
    }
    if (getPage().isFolder()) {
      ISection filterSection = createSection(SECTION_ID_FILTER, Texts.get("Filter"));
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      filterPresenter.getContainer().setLayoutData(layoutData);
      getSection(SECTION_ID_FILTER).setExpanded(false);
    }
    m_configPropertyType = new ConfigPropertyType(getPage().getType());
    ConfigurationMethod[] configPropertyMethods = m_configPropertyType.getConfigurationMethods(ConfigurationMethod.PROPERTY_METHOD);
    if (configPropertyMethods != null && configPropertyMethods.length > 0) {
      ISection configPropertiesSection = createSection(SECTION_ID_PROPERTIES, Texts.get("Properties"));
      for (ConfigurationMethod m : configPropertyMethods) {
        AbstractMethodPresenter presenter = null;
        presenter = createConfigMethodPresenter(configPropertiesSection.getSectionClient(), m);
        if (presenter != null) {
          GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
          layoutData.widthHint = 200;
          presenter.getContainer().setLayoutData(layoutData);
          m_methodPresenters.put(m.getMethodName(), presenter);
        }
      }
    }

    ConfigurationMethod[] operationPropertyMethods = m_configPropertyType.getConfigurationMethods(ConfigurationMethod.OPERATION_METHOD);
    if (operationPropertyMethods != null && operationPropertyMethods.length > 0) {
      ISection operationPropertiesSection = createSection(SECTION_ID_OPERATIONS, Texts.get("Operations"));
      for (ConfigurationMethod m : operationPropertyMethods) {
        createOperationPresenter(operationPropertiesSection.getSectionClient(), m);
      }
    }

    if (m_methodChangedListener == null) {
      m_methodChangedListener = new P_MethodChangedListener();
      ScoutSdk.addJavaResourceChangedListener(m_methodChangedListener);
//      JavaCore.addElementChangedListener(m_methodChangedListener);
    }
    try {
      setCompilationUnitDirty(getPage().getType().getCompilationUnit().isWorkingCopy() && getPage().getType().getCompilationUnit().getBuffer().hasUnsavedChanges());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logWarning("could not determ working copy '" + getPage().getType().getElementName() + "'.");
    }
  }

  @Override
  protected void cleanup() {
    if (m_methodChangedListener != null) {
      ScoutSdk.removeJavaResourceChangedListener(m_methodChangedListener);
//      JavaCore.removeElementChangedListener(m_methodChangedListener);
      m_methodChangedListener = null;
    }
  }

  protected AbstractMethodPresenter createConfigMethodPresenter(Composite parent, ConfigurationMethod method) {
    AbstractMethodPresenter presenter = null;
    String propertyType = method.getConfigAnnotationType();

    if (propertyType.equals("BOOLEAN")) {
      presenter = new BooleanPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("DOUBLE")) {
      presenter = new DoublePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }

    else if (propertyType.equals("DRAG_AND_DROP_TYPE")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("INTEGER")) {
      presenter = new IntegerPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("LONG")) {
      presenter = new LongPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("STRING")) {
      presenter = new StringPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("FONT")) {
      presenter = new FontPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }

    else if (propertyType.equals("COLOR")) {
      presenter = new ColorPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("OBJECT")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("BUTTON_DISPLAY_STYLE")) {
      presenter = new ButtonDisplayStylePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("BUTTON_SYSTEM_TYPE")) {
      presenter = new ButtonSystemTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("CODE_TYPE")) {
      presenter = new CodeTypeProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("COMPOSER_ATTRIBUTE_TYPE")) {
      // TODO
//      presenter = new Abc(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("FILE_EXTENSIONS")) {
      // TODO$
//      presenter = new StringPresenter(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("FORM_DISPLAY_HINT")) {
      presenter = new FormDisplayHintPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }

    else if (propertyType.equals("FORM_VIEW_ID")) {
      presenter = new FormViewIdPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }

    else if (propertyType.equals("HORIZONTAL_ALIGNMENT")) {
      presenter = new HorizontalAlignmentPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("ICON_ID")) {
      presenter = new IconPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("KEY_STROKE")) {
      // NOT in use
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("LOOKUP_CALL")) {
      presenter = new LookupCallProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("LOOKUP_SERVICE")) {
      presenter = new LookupServiceProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("MASTER_FIELD")) {
      presenter = new MasterFieldPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("OUTLINE_ROOT_PAGE")) {
      presenter = new OutlineRootPagePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("OUTLINE")) {
      // TODO
//      presenter = new Outline(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("OUTLINES")) {
      presenter = new OutlinesPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("FORM")) {
      // TODO
//      presenter = new Form(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("SEARCH_FORM")) {

      presenter = new SearchFormPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("NLS_PROVIDER")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("SQL_STYLE")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("SQL")) {
      presenter = new MultiLineStringPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("TEXT")) {
      presenter = new NlsTextPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("VERTICAL_ALIGNMENT")) {
      presenter = new VerticalAglinmentPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if (propertyType.equals("CHART_QNAME")) {
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("HOUR_OF_DAY")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("DURATION_MINUTES")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("MENU_CLASS")) {
      // TODO
//      presenter = new ABC(getFormToolkit(), parent);
//      presenter.setMethod(method);
    }
    else if (propertyType.equals("PRIMITIVE_TYPE")) {
      presenter = new PrimitiveTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    // layout
    if (presenter != null) {
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      presenter.getContainer().setLayoutData(layoutData);
      m_methodPresenters.put(method.getMethodName(), presenter);
    }
    else {
      ScoutSdkUi.logWarning("Could not find a presenter for property '" + propertyType + "'.");
    }
    return presenter;
  }

  protected AbstractMethodPresenter createOperationPresenter(Composite parent, ConfigurationMethod method) {
    AbstractMethodPresenter presenter = null;
    if (method.getMethodName().equals("execResetSearchFilter")) {
      presenter = new ExecResetSerchFilterMethodPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else {
      presenter = new ExecMethodPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    //layout
    if (presenter != null) {
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      presenter.getContainer().setLayoutData(layoutData);
      m_methodPresenters.put(method.getMethodName(), presenter);
    }
    return presenter;
  }

  private void handleMethodChanged(IMethod method) {
    if (m_configPropertyType.isRelevantType(method.getDeclaringType())) {
      ConfigurationMethod updatedMethod = m_configPropertyType.updateIfChanged(method);
      if (updatedMethod != null) {
        synchronized (m_methodUpdateLock) {
          m_methodsToUpdate.put(updatedMethod.getMethodName(), updatedMethod);
          m_updateJob.cancel();
          m_updateJob.schedule(150);
        }
      }
    }
  }

  private void setCompilationUnitDirty(final boolean dirty) {
    synchronized (m_markDirtyLock) {
      ScrolledForm form = getForm();
      if (form != null && !form.isDisposed()) {
        form.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (getForm() != null && !getForm().isDisposed()) {
              m_saveButton.setEnabled(dirty);
              for (AbstractMethodPresenter p : m_methodPresenters.values()) {
                p.setEnabled(!dirty);
              }
              if (dirty) {
                addStatus(m_icuNotSyncStatus);
              }
              else {
                removeStatus(m_icuNotSyncStatus);
              }
            }
          }
        });
      }
    }
  }

  private class P_MethodChangedListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {

      switch (event.getEventType()) {
        case JdtEvent.ADDED:
        case JdtEvent.REMOVED:
        case JdtEvent.CHANGED:
          if (event.getElement().getElementType() == IJavaElement.METHOD) {
            handleMethodChanged((IMethod) event.getElement());
          }
          break;
        case JdtEvent.BUFFER_DIRTY:
          if (event.getElementType() == IJavaElement.COMPILATION_UNIT && getPage().getType().getCompilationUnit().equals(event.getElement())) {
            m_markDirtyJob.cancel();
            m_markDirtyJob.setDirty(true);
            m_markDirtyJob.schedule(150);
          }
          break;
        case JdtEvent.BUFFER_SYNC:
          if (event.getElementType() == IJavaElement.COMPILATION_UNIT && getPage().getType().getCompilationUnit().equals(event.getElement())) {
            m_markDirtyJob.cancel();
            m_markDirtyJob.setDirty(false);
            m_markDirtyJob.schedule(150);
          }
          break;
      }

    }
  }

  private class P_UpdateMethodsJob extends Job {
    private final Display m_display;

    public P_UpdateMethodsJob(Display display) {
      super("");
      m_display = display;
      setSystem(true);
      setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }
      ConfigurationMethod[] methods = new ConfigurationMethod[0];
      synchronized (m_methodUpdateLock) {
        if (m_methodsToUpdate.size() > 0) methods = m_methodsToUpdate.values().toArray(new ConfigurationMethod[m_methodsToUpdate.size()]);
        m_methodsToUpdate.clear();
      }
      for (ConfigurationMethod cm : methods) {
        AbstractMethodPresenter presenter = m_methodPresenters.get(cm.getMethodName());
        if (presenter != null) {
          RunnableWithData runnable = new RunnableWithData() {
            @Override
            public void run() {
              ((AbstractMethodPresenter) getData("presenter")).setMethod((ConfigurationMethod) getData("configMethod"));

            }
          };
          runnable.setData("presenter", presenter);
          runnable.setData("configMethod", cm);
          if (m_display != null && !m_display.isDisposed()) {
            m_display.syncExec(runnable);
          }
        }
      }
      return Status.OK_STATUS;
    }
  } // end P_UpdateMethodsJob

  private class P_MarkDirtyJob extends Job {
    private boolean m_dirty;

    public P_MarkDirtyJob() {
      super("");
      setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      setCompilationUnitDirty(m_dirty);
      return Status.OK_STATUS;
    }

    /**
     * @param dirty
     *          the dirty to set
     */
    public void setDirty(boolean dirty) {
      m_dirty = dirty;
    }

    /**
     * @return the dirty
     */
    public boolean isDirty() {
      return m_dirty;
    }
  }
}
