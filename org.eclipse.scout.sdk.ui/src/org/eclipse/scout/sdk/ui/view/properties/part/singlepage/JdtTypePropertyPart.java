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
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.RunnableWithData;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.CompilationUnitSaveOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.DocumentationPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.ExecMethodPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.ExecResetSearchFilterMethodPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BigDecimalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BigIntegerPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BooleanPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BorderDecorationPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonDisplayStylePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonSystemTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.CodeTypeProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ColorPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ComposerAttributeTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.DoublePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.DragAndDropTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FontPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormDisplayHintPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormViewIdPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.GroupBoxBodyGridPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.HorizontalAlignmentPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.IconPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.IntegerPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelHorizontalAlignmentPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelPositionPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LongPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupCallProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LookupServiceProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MasterFieldPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MenuProposalPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MenuTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.MultiLineStringPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.NlsTextPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.OutlineRootPagePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.OutlinesPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.PrimitiveTypePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.RoundingModePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.SearchFormPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.StringPresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.VerticalAglinmentPresenter;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.PropertyViewConfig.ConfigTypes;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractMethodPresenter;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyType;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * *
 * h3>JdtTypePropertyPart</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 19.07.2010
 */
public class JdtTypePropertyPart extends AbstractSinglePageSectionBasedViewPart {
  protected static final String SECTION_ID_FILTER = "section.filter";
  protected static final String SECTION_ID_DOCUMENTATION = "section.documentation";
  protected static final String SECTION_ID_PROPS_IMPORTANT = "section.properties.important";
  protected static final String SECTION_ID_PROPS_ADVANCED = "section.properties.advanced";
  protected static final String SECTION_ID_OPS_IMPORTANT = "section.operations.important";
  protected static final String SECTION_ID_OPS_ADVANCED = "section.operations.advanced";

  private final Map<String, AbstractMethodPresenter> m_methodPresenters;
  private final Map<String, ConfigurationMethod> m_methodsToUpdate;

  private P_UpdateMethodsJob m_updateJob;
  private final Object m_methodUpdateLock;

  private final P_MarkDirtyJob m_markDirtyJob;
  private final Object m_markDirtyLock;

  private IJavaResourceChangedListener m_methodChangedListener;
  private ConfigPropertyType m_configPropertyType;
  private Button m_saveButton;
  private IStatus m_icuNotSyncStatus;

  public JdtTypePropertyPart() {
    m_methodsToUpdate = new HashMap<String, ConfigurationMethod>();
    m_methodPresenters = new HashMap<String, AbstractMethodPresenter>();
    m_methodUpdateLock = new Object();
    m_markDirtyJob = new P_MarkDirtyJob();
    m_markDirtyLock = new Object();
  }

  @Override
  protected String getPartKey() {
    if (getPage() == null || !TypeUtility.exists(getPage().getType())) return null;
    return getPage().getType().getFullyQualifiedName();
  }

  @Override
  public AbstractScoutTypePage getPage() {
    return (AbstractScoutTypePage) super.getPage();
  }

  ISection addSection(String sectionId, String title) {
    return super.createSection(sectionId, title);
  }

  private boolean isDirty() {
    return m_markDirtyJob.isDirty();
  }

  @Override
  public void setPage(IPage page) {
    super.setPage(page);
    m_icuNotSyncStatus = new Status(IStatus.INFO, ScoutSdkUi.PLUGIN_ID, Texts.get("SaveTheFile", getPage().getType().getPath().toOSString()));
  }

  private int getTitleHyperlinkWidth() {
    final int TEXT_SPACE_MARGIN_RIGHT = 110;
    Rectangle b = getForm().getParent().getBounds();
    int w = Math.max(b.width - TEXT_SPACE_MARGIN_RIGHT, 110);
    return w;
  }

  @Override
  protected Control createHead(final Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    final Font font = ScoutSdkUi.getFont(ScoutSdkUi.FONT_SYSTEM_TITLE);
    String text = UiUtility.getTextForBounds(getPage().getName(), font, parent, getTitleHyperlinkWidth());
    final Hyperlink title = getFormToolkit().createHyperlink(headArea, text, SWT.WRAP);
    title.addControlListener(new ControlListener() {
      @Override
      public void controlResized(ControlEvent e) {
        try {
          getForm().setRedraw(false);
          String txt = UiUtility.getTextForBounds(getPage().getName(), font, parent, getTitleHyperlinkWidth());
          title.setText(txt);
          getForm().reflow(true);
          getForm().getParent().layout(true, true);
        }
        finally {
          getForm().setRedraw(true);
        }
      }

      @Override
      public void controlMoved(ControlEvent e) {
      }
    });
    title.setToolTipText(getPage().getName());
    title.setFont(font);

    title.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        UiUtility.showJavaElementInEditor(getPage().getType(), true);
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

  protected void createDocumentationSectionContent(ISection docSection) {
    DocumentationPresenter docPresenter = new DocumentationPresenter(getFormToolkit(), docSection.getSectionClient(), getPage());
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    docPresenter.getContainer().setLayoutData(layoutData);
  }

  @Override
  protected void createSections() {
    if (m_updateJob == null) {
      m_updateJob = new P_UpdateMethodsJob(getForm().getDisplay());
    }

    // Filter Section
    if (getPage().isFolder()) {
      ISection filterSection = createSection(SECTION_ID_FILTER, Texts.get("Filter"));
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), filterSection.getSectionClient(), getPage());
      GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
      layoutData.widthHint = 200;
      filterPresenter.getContainer().setLayoutData(layoutData);
      filterSection.setExpanded(wasSectionExpanded(SECTION_ID_FILTER, false));
    }

    // documentation
    IScoutBundle bundle = getPage().getScoutBundle();
    if (bundle != null) {
      INlsProject docsNlsProject = bundle.getDocsNlsProject();
      if (docsNlsProject != null) {
        IType iTypeWithClassId = TypeUtility.getType(IRuntimeClasses.ITypeWithClassId);
        if (TypeUtility.exists(iTypeWithClassId) && TypeUtility.exists(getPage().getType())) {
          if (TypeUtility.getSupertypeHierarchy(getPage().getType()).contains(iTypeWithClassId)) {
            // documentation is supported
            final ISection docSection = createSection(SECTION_ID_DOCUMENTATION, Texts.get("Documentation"));
            docSection.setExpanded(wasSectionExpanded(SECTION_ID_DOCUMENTATION, false));
            if (docSection.isExpanded()) {
              createDocumentationSectionContent(docSection);
            }
            else {
              docSection.addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanging(ExpansionEvent e) {
                  if (e.getState()) {
                    createDocumentationSectionContent(docSection);
                    docSection.removeExpansionListener(this);
                  }
                }
              });
            }
          }
        }
      }
    }

    try {
      m_configPropertyType = new ConfigPropertyType(getPage().getType());

      // ensure consistent order
      boolean noImpProps = !createImportantProperties();
      if (noImpProps) {
        createAdvancedProperties(false);
        boolean created = createImportantOperations();
        createAdvancedOperations(created);
      }
      else {
        boolean noImpOps = !createImportantOperations();
        if (noImpOps) {
          createAdvancedOperations(false);
          createAdvancedProperties(true);
        }
        else {
          createAdvancedProperties(true);
          createAdvancedOperations(true);
        }
      }

      if (m_methodChangedListener == null) {
        m_methodChangedListener = new P_MethodChangedListener();
        ScoutSdkCore.getJavaResourceChangedEmitter().addJavaResourceChangedListener(m_methodChangedListener);
      }

      if (getPage().getType().getCompilationUnit() == null) {
        setCompilationUnitDirty(false);
      }
      else {
        setCompilationUnitDirty(getPage().getType().getCompilationUnit().isWorkingCopy() && getPage().getType().getCompilationUnit().getBuffer().hasUnsavedChanges());
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logWarning("could not create config property page for '" + getPage().getType().getFullyQualifiedName() + "'.", e);
    }
  }

  private boolean createImportantProperties() {
    ConfigurationMethodSection impConfigPropsSection = new ConfigurationMethodSection(m_configPropertyType, ConfigurationMethod.PROPERTY_METHOD, ConfigTypes.NORMAL);
    ISection impPropsSection = impConfigPropsSection.createContent(this, SECTION_ID_PROPS_IMPORTANT, Texts.get("Properties"), wasSectionExpanded(SECTION_ID_PROPS_IMPORTANT, true));
    return impPropsSection != null;
  }

  private boolean createImportantOperations() {
    ConfigurationMethodSection importantExecSection = new ConfigurationMethodSection(m_configPropertyType, ConfigurationMethod.OPERATION_METHOD, ConfigTypes.NORMAL);
    ISection impOpsSection = importantExecSection.createContent(this, SECTION_ID_OPS_IMPORTANT, Texts.get("Operations"), wasSectionExpanded(SECTION_ID_OPS_IMPORTANT, true));
    return impOpsSection != null;
  }

  private boolean createAdvancedProperties(boolean importantCreated) {
    ConfigurationMethodSection advancedConfigPropsSection = new ConfigurationMethodSection(m_configPropertyType, ConfigurationMethod.PROPERTY_METHOD, ConfigTypes.ADVANCED);
    ISection advPropsSection = advancedConfigPropsSection.createContent(this, SECTION_ID_PROPS_ADVANCED, Texts.get(importantCreated ? "AdvancedProperties" : "Properties"), wasSectionExpanded(SECTION_ID_PROPS_ADVANCED, !importantCreated));
    return advPropsSection != null;
  }

  private boolean createAdvancedOperations(boolean importantCreated) {
    ConfigurationMethodSection advancedExecSection = new ConfigurationMethodSection(m_configPropertyType, ConfigurationMethod.OPERATION_METHOD, ConfigTypes.ADVANCED);
    ISection advOpsSection = advancedExecSection.createContent(this, SECTION_ID_OPS_ADVANCED, Texts.get(importantCreated ? "AdvancedOperations" : "Operations"), wasSectionExpanded(SECTION_ID_OPS_ADVANCED, !importantCreated));
    return advOpsSection != null;
  }

  @Override
  protected void cleanup() {
    if (m_methodChangedListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeJavaResourceChangedListener(m_methodChangedListener);
      m_methodChangedListener = null;
    }
    super.cleanup();
  }

  AbstractMethodPresenter createConfigMethodPresenter(Composite parent, ConfigurationMethod method) {
    AbstractMethodPresenter presenter = null;
    String propertyType = StringUtility.uppercase(method.getConfigAnnotationType());

    if ("BOOLEAN".equals(propertyType)) {
      presenter = new BooleanPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("DOUBLE".equals(propertyType)) {
      presenter = new DoublePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("BIG_DECIMAL".equals(propertyType)) {
      presenter = new BigDecimalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("DRAG_AND_DROP_TYPE".equals(propertyType)) {
      presenter = new DragAndDropTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("INTEGER".equals(propertyType)) {
      presenter = new IntegerPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("LONG".equals(propertyType)) {
      presenter = new LongPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("BIG_INTEGER".equals(propertyType)) {
      presenter = new BigIntegerPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("STRING".equals(propertyType)) {
      presenter = new StringPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("FONT".equals(propertyType)) {
      presenter = new FontPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("COLOR".equals(propertyType)) {
      presenter = new ColorPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("BUTTON_DISPLAY_STYLE".equals(propertyType)) {
      presenter = new ButtonDisplayStylePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("BUTTON_SYSTEM_TYPE".equals(propertyType)) {
      presenter = new ButtonSystemTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("CODE_TYPE".equals(propertyType)) {
      presenter = new CodeTypeProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("COMPOSER_ATTRIBUTE_TYPE".equals(propertyType)) {
      presenter = new ComposerAttributeTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("FORM_DISPLAY_HINT".equals(propertyType)) {
      presenter = new FormDisplayHintPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("FORM_VIEW_ID".equals(propertyType)) {
      presenter = new FormViewIdPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("HORIZONTAL_ALIGNMENT".equals(propertyType)) {
      presenter = new HorizontalAlignmentPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("ICON_ID".equals(propertyType)) {
      presenter = new IconPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("LOOKUP_CALL".equals(propertyType)) {
      presenter = new LookupCallProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("LOOKUP_SERVICE".equals(propertyType)) {
      presenter = new LookupServiceProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("MASTER_FIELD".equals(propertyType)) {
      presenter = new MasterFieldPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("OUTLINE_ROOT_PAGE".equals(propertyType)) {
      presenter = new OutlineRootPagePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("OUTLINES".equals(propertyType)) {
      presenter = new OutlinesPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("SEARCH_FORM".equals(propertyType)) {
      presenter = new SearchFormPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("SQL".equals(propertyType)) {
      presenter = new MultiLineStringPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("TEXT".equals(propertyType)) {
      presenter = new NlsTextPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("VERTICAL_ALIGNMENT".equals(propertyType)) {
      presenter = new VerticalAglinmentPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("MENU_CLASS".equals(propertyType)) {
      presenter = new MenuProposalPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("PRIMITIVE_TYPE".equals(propertyType)) {
      presenter = new PrimitiveTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("LABEL_POSITION".equals(propertyType)) {
      presenter = new LabelPositionPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("LABEL_HORIZONTAL_ALIGNMENT".equals(propertyType)) {
      presenter = new LabelHorizontalAlignmentPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("BORDER_DECORATION".equals(propertyType)) {
      presenter = new BorderDecorationPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("ROUNDING_MODE".equals(propertyType)) {
      presenter = new RoundingModePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("GROUP_BOX_BODY_GRID".equals(propertyType)) {
      presenter = new GroupBoxBodyGridPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else if ("MENU_TYPE".equals(propertyType)) {
      presenter = new MenuTypePresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }
    else {
      // fall back for all other property types
      presenter = new ExecMethodPresenter(getFormToolkit(), parent);
      presenter.setMethod(method);
    }

    // layout
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    layoutData.widthHint = 200;
    presenter.getContainer().setLayoutData(layoutData);
    presenter.setEnabled(isPresenterEnabled(method));
    m_methodPresenters.put(method.getMethodName(), presenter);

    return presenter;
  }

  private static boolean isFinal(IMethod m) {
    if (m == null) {
      return false;
    }

    try {
      return (m.getFlags() & Flags.AccFinal) != 0;
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("Unable to retrieve flags for method '" + m.getElementName() + "'.", e);
      return false;
    }
  }

  private boolean isPresenterEnabled(ConfigurationMethod m) {
    return isPresenterEnabled(m, isDirty());
  }

  private boolean isPresenterEnabled(ConfigurationMethod m, boolean compilationUnitDirty) {
    return !isFinal(m.getDefaultMethod()) && !compilationUnitDirty && !getPage().getType().isReadOnly();
  }

  AbstractMethodPresenter createOperationPresenter(Composite parent, ConfigurationMethod method) {
    AbstractMethodPresenter presenter = null;
    if ("execResetSearchFilter".equals(method.getMethodName())) {
      presenter = new ExecResetSearchFilterMethodPresenter(getFormToolkit(), parent);
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
      presenter.setEnabled(isPresenterEnabled(method));
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
                p.setEnabled(isPresenterEnabled(p.getMethod(), dirty));
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
          if (event.getElementType() == IJavaElement.COMPILATION_UNIT && event.getElement().equals(getPage().getType().getCompilationUnit())) {
            m_markDirtyJob.cancel();
            m_markDirtyJob.setDirty(true);
            m_markDirtyJob.schedule(150);
          }
          break;
        case JdtEvent.BUFFER_SYNC:
          if (event.getElementType() == IJavaElement.COMPILATION_UNIT && event.getElement().equals(getPage().getType().getCompilationUnit())) {
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
      List<ConfigurationMethod> methods = null;
      synchronized (m_methodUpdateLock) {
        methods = CollectionUtility.arrayList(m_methodsToUpdate.values());
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
