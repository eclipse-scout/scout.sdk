/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractBigDecimalField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractBooleanField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractButton;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractCalendarField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractCode;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractDateField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractExtension;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractFileChooserField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractFormHandler;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractGroupBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractHtmlField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractImageField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractKeyStroke;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractLabelField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractListBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractListBoxExtension;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractLongField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractMenu;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractProposalField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractRadioButton;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractRadioButtonGroup;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractRadioButtonGroupExtension;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractSequenceBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractSmartField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractStringColumn;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractStringField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractTabBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractTabBoxExtension;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractTableField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractTreeBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractTreeBoxExtension;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.AbstractTreeField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IBigDecimalField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IBooleanField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IButton;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ICalendarField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ICode;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IColumn;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IDateField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IExtension;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IFileChooserField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IFormHandler;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IGroupBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IHtmlField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IImageField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IKeyStroke;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ILabelField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IListBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ILongField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IMenu;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IProposalField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IRadioButton;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IRadioButtonGroup;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ISequenceBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ISmartField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.IStringField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ITabBox;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ITableField;
import static org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes.ITreeField;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_BUTTON;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_CODE;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_COLUMN;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_COMPOSITE_FIELD;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_EXTENSION;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_FORM_FIELD;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_FORM_HANDLER;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_KEY_STROKE;
import static org.eclipse.scout.sdk.core.s.ISdkProperties.SUFFIX_MENU;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.ButtonAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.CodeAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.ColumnAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.DateFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.DoubleFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.ExtensionsAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.FileChooserFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.FormFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.FormHandler;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.GroupBoxAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.IntegerFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.KeyStrokeAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.MenuAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.RadioButtonAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.RadioButtonGroupAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.SequenceBoxAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.SmartFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.StringFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.TabBoxAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.TableFieldAdd;
import static org.eclipse.scout.sdk.s2e.ui.ISdkIcons.TreeFieldAdd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.ScoutModelHierarchy;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.RunnableJob;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>{@link ScoutTemplateProposalFactory}</h3>
 *
 * @since 5.2.0
 */
public final class ScoutTemplateProposalFactory {

  private ScoutTemplateProposalFactory() {
  }

  public static final Map<String, TemplateProposalDescriptor> TEMPLATES = new ConcurrentHashMap<>();

  static {
    int relevance = 10000;
    TEMPLATES.put(IStringField,
        new TemplateProposalDescriptor(IStringField, AbstractStringField, "MyString", SUFFIX_FORM_FIELD, StringFieldAdd, relevance, StringFieldProposal.class, singletonList("textfield")));
    TEMPLATES.put(IBigDecimalField,
        new TemplateProposalDescriptor(IBigDecimalField, AbstractBigDecimalField, "MyBigDecimal", SUFFIX_FORM_FIELD, DoubleFieldAdd, relevance, BigDecimalFieldProposal.class, asList("numberfield", "doublefield", "floatfield")));
    TEMPLATES.put(IBooleanField,
        new TemplateProposalDescriptor(IBooleanField, AbstractBooleanField, "MyBoolean", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, FormFieldProposal.class, asList("checkboxfield", "tristatefield")));
    TEMPLATES.put(IButton,
        new TemplateProposalDescriptor(IButton, AbstractButton, "My", SUFFIX_BUTTON, ButtonAdd, relevance, ButtonProposal.class));
    TEMPLATES.put(ICalendarField,
        new TemplateProposalDescriptor(ICalendarField, AbstractCalendarField, "MyCalendar", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, CalendarFieldProposal.class));
    TEMPLATES.put(IDateField,
        new TemplateProposalDescriptor(IDateField, AbstractDateField, "MyDate", SUFFIX_FORM_FIELD, DateFieldAdd, relevance, DateFieldProposal.class, singletonList("datetimefield")));
    TEMPLATES.put(IFileChooserField,
        new TemplateProposalDescriptor(IFileChooserField, AbstractFileChooserField, "MyFileChooser", SUFFIX_FORM_FIELD, FileChooserFieldAdd, relevance, FormFieldProposal.class));
    TEMPLATES.put(IGroupBox,
        new TemplateProposalDescriptor(IGroupBox, AbstractGroupBox, "MyGroup", SUFFIX_COMPOSITE_FIELD, GroupBoxAdd, relevance, FormFieldProposal.class));
    TEMPLATES.put(IHtmlField,
        new TemplateProposalDescriptor(IHtmlField, AbstractHtmlField, "MyHtml", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, FormFieldProposal.class));
    TEMPLATES.put(ILabelField,
        new TemplateProposalDescriptor(ILabelField, AbstractLabelField, "MyLabel", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, LabelFieldProposal.class));
    TEMPLATES.put(IListBox,
        new TemplateProposalDescriptor(IListBox, AbstractListBox, "MyList", SUFFIX_COMPOSITE_FIELD, FormFieldAdd, relevance, ListBoxFieldProposal.class));
    TEMPLATES.put(IProposalField,
        new TemplateProposalDescriptor(IProposalField, AbstractProposalField, "MyProposal", SUFFIX_FORM_FIELD, SmartFieldAdd, relevance, ValueTypeFieldProposal.class));
    TEMPLATES.put(ISmartField,
        new TemplateProposalDescriptor(ISmartField, AbstractSmartField, "MySmart", SUFFIX_FORM_FIELD, SmartFieldAdd, relevance, ValueTypeFieldProposal.class));
    TEMPLATES.put(ILongField,
        new TemplateProposalDescriptor(ILongField, AbstractLongField, "MyLong", SUFFIX_FORM_FIELD, IntegerFieldAdd, relevance, LongFieldProposal.class, asList("integerfield", "numberfield")));
    TEMPLATES.put(IRadioButtonGroup,
        new TemplateProposalDescriptor(IRadioButtonGroup, AbstractRadioButtonGroup, "MyRadioButtonGroup", "", RadioButtonGroupAdd, relevance, ValueTypeFieldProposal.class));
    TEMPLATES.put(ISequenceBox,
        new TemplateProposalDescriptor(ISequenceBox, AbstractSequenceBox, "MySequence", SUFFIX_COMPOSITE_FIELD, SequenceBoxAdd, relevance, SequenceBoxProposal.class));
    TEMPLATES.put(ITabBox,
        new TemplateProposalDescriptor(ITabBox, AbstractTabBox, "MyTab", SUFFIX_COMPOSITE_FIELD, TabBoxAdd, relevance, TabBoxProposal.class));
    TEMPLATES.put(ITableField,
        new TemplateProposalDescriptor(ITableField, AbstractTableField, "MyTable", SUFFIX_FORM_FIELD, TableFieldAdd, relevance, TableFieldProposal.class));
    TEMPLATES.put(ITreeField,
        new TemplateProposalDescriptor(ITreeField, AbstractTreeField, "MyTree", SUFFIX_FORM_FIELD, TreeFieldAdd, relevance, TreeFieldProposal.class));
    TEMPLATES.put(IRadioButton,
        new TemplateProposalDescriptor(IRadioButton, AbstractRadioButton, "MyRadio", SUFFIX_BUTTON, RadioButtonAdd, relevance, RadioButtonProposal.class));
    TEMPLATES.put(IMenu,
        new TemplateProposalDescriptor(IMenu, AbstractMenu, "MyMenu", SUFFIX_MENU, MenuAdd, relevance, MenuProposal.class));
    TEMPLATES.put(IKeyStroke,
        new TemplateProposalDescriptor(IKeyStroke, AbstractKeyStroke, "My", SUFFIX_KEY_STROKE, KeyStrokeAdd, relevance, KeyStrokeProposal.class));
    TEMPLATES.put(ICode,
        new TemplateProposalDescriptor(ICode, AbstractCode, "My", SUFFIX_CODE, CodeAdd, relevance, CodeProposal.class));
    TEMPLATES.put(IFormHandler,
        new TemplateProposalDescriptor(IFormHandler, AbstractFormHandler, "My", SUFFIX_FORM_HANDLER, FormHandler, relevance, FormHandlerProposal.class));
    TEMPLATES.put(IColumn,
        new TemplateProposalDescriptor(IColumn, AbstractStringColumn, "My", SUFFIX_COLUMN, ColumnAdd, relevance, ColumnProposal.class));
    TEMPLATES.put(IExtension,
        new TemplateProposalDescriptor(IExtension, AbstractExtension, "My", SUFFIX_EXTENSION, ExtensionsAdd, relevance, ExtensionProposal.class));
    TEMPLATES.put(IImageField,
        new TemplateProposalDescriptor(IImageField, AbstractImageField, "MyImage", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, ImageFieldProposal.class));
  }

  public static List<ICompletionProposal> createTemplateProposals(IType declaringType, int offset, String prefix, SourceViewer viewer) {
    Set<String> superTypesOfDeclaringType;
    ISourceRange surroundingTypeNameRange;
    try {
      superTypesOfDeclaringType = getAllSuperTypesOf(declaringType);
      surroundingTypeNameRange = declaringType.getNameRange();
    }
    catch (JavaModelException e) {
      SdkLog.error("Unable to calculate supertype hierarchy for '{}'.", declaringType.getFullyQualifiedName(), e);
      return emptyList();
    }

    Collection<String> possibleChildrenIfcFqn = getPossibleChildInterfaceNames(superTypesOfDeclaringType);
    if (possibleChildrenIfcFqn.isEmpty()) {
      return emptyList();
    }

    ICompilationUnit compilationUnit = declaringType.getCompilationUnit();

    // start java environment creation
    RunnableFuture<EclipseEnvironment> javaEnvProviderCreator = new FutureTask<>(new P_JavaEnvironmentPreloader(compilationUnit, prefix != null, offset, viewer));
    Job javaEnvCreatorJob = new RunnableJob("Init Java Environment", javaEnvProviderCreator);
    javaEnvCreatorJob.setUser(false);
    javaEnvCreatorJob.setSystem(true);
    javaEnvCreatorJob.setPriority(Job.SHORT);
    javaEnvCreatorJob.schedule();

    // create proposals
    IJavaProject javaProject = declaringType.getJavaProject();
    List<ICompletionProposal> result = new ArrayList<>();
    for (TemplateProposalDescriptor candidate : TEMPLATES.values()) {
      if (candidate.isActiveFor(possibleChildrenIfcFqn, javaProject, prefix)) {
        result.add(candidate.createProposal(compilationUnit, offset, surroundingTypeNameRange, javaEnvProviderCreator, prefix));
      }
    }
    return result;
  }

  private static Set<String> getAllSuperTypesOf(IType declaringType) throws JavaModelException {
    ITypeHierarchy supertypeHierarchy = declaringType.newSupertypeHierarchy(null);
    IType[] allTypes = supertypeHierarchy.getAllTypes();
    Set<String> superTypesOfDeclaringType = new HashSet<>(allTypes.length);
    for (IType superType : allTypes) {
      superTypesOfDeclaringType.add(superType.getFullyQualifiedName());
    }
    return superTypesOfDeclaringType;
  }

  private static Collection<String> getPossibleChildInterfaceNames(Collection<String> superTypesOfDeclaringType) {
    Collection<String> possibleChildrenIfcFqn = new HashSet<>();
    if (superTypesOfDeclaringType.contains(AbstractTabBox)
        || superTypesOfDeclaringType.contains(AbstractTabBoxExtension)) {
      // special case for tab boxes
      possibleChildrenIfcFqn.add(IGroupBox);
      possibleChildrenIfcFqn.add(IMenu);
      possibleChildrenIfcFqn.add(IKeyStroke);
    }
    else if (superTypesOfDeclaringType.contains(AbstractListBox)
        || superTypesOfDeclaringType.contains(AbstractTreeBox)
        || superTypesOfDeclaringType.contains(AbstractListBoxExtension)
        || superTypesOfDeclaringType.contains(AbstractTreeBoxExtension)) {
      // special case for list boxes & tree boxes
      possibleChildrenIfcFqn.add(IMenu);
      possibleChildrenIfcFqn.add(IKeyStroke);
    }
    else if (superTypesOfDeclaringType.contains(AbstractRadioButtonGroup)
        || superTypesOfDeclaringType.contains(AbstractRadioButtonGroupExtension)) {
      // special case for radio button groups
      possibleChildrenIfcFqn.add(IRadioButton);
      possibleChildrenIfcFqn.add(IMenu);
      possibleChildrenIfcFqn.add(IKeyStroke);
    }
    else {
      for (String superType : superTypesOfDeclaringType) {
        Set<String> possibleChildren = ScoutModelHierarchy.getPossibleChildren(superType);
        if (!possibleChildren.isEmpty()) {
          possibleChildrenIfcFqn.addAll(possibleChildren);
        }
      }
    }
    return possibleChildrenIfcFqn;
  }

  private static final class P_JavaEnvironmentPreloader implements Callable<EclipseEnvironment>, ICompletionListener {

    private final ICompilationUnit m_icu;
    private final boolean m_hasSearchString;
    private final int m_pos;
    private final ContentAssistantFacade m_contentAssistFacade;
    private final EclipseEnvironment m_provider;
    private final Display m_display;

    private P_JavaEnvironmentPreloader(ICompilationUnit icu, boolean hasSearchString, int pos, SourceViewer viewer) {
      m_icu = icu;
      m_hasSearchString = hasSearchString;
      m_pos = pos;
      m_contentAssistFacade = viewer.getContentAssistantFacade();
      m_display = viewer.getControl().getDisplay();
      m_provider = EclipseEnvironment.createUnsafe(env -> m_contentAssistFacade.addCompletionListener(this));
    }

    @Override
    public EclipseEnvironment call() throws JavaModelException {
      String pck = JdtUtils.getPackage(m_icu);
      if (Strings.isBlank(pck)) {
        pck = null;
      }
      IJavaEnvironment env = m_provider.toScoutJavaEnvironment(m_icu.getJavaProject());

      StringBuilder buf = new StringBuilder(m_icu.getSource());
      if (m_hasSearchString) {
        buf.insert(m_pos, AbstractTypeProposal.SEARCH_STRING_END_FIX);
      }
      env.registerCompilationUnitOverride(pck, m_icu.getElementName(), buf);

      env.findType(m_icu.findPrimaryType().getFullyQualifiedName()); // pre-load
      return m_provider;
    }

    @Override
    public void assistSessionStarted(ContentAssistEvent event) {
      // nop
    }

    @Override
    public void assistSessionEnded(ContentAssistEvent event) {
      m_contentAssistFacade.removeCompletionListener(this);

      // the session end event is executed in the SWT thread. but the end event is fired before the content assist changes are applied.
      // therefore it is not possible to close the environment already. instead schedule another SWT task which will be executed afterwards.
      m_display.asyncExec(this::scheduleEnvironmentClose);
    }

    private void scheduleEnvironmentClose() {
      // close the environment async. Because if currently a type lookup is going on (in the environment), the close operation is blocked until it is finished.
      // this method is invoked in the SWT thread. To not freeze the UI close the environment in a worker thread which will finish as soon as the type lookup completes.
      AbstractJob closeJob = new AbstractJob("close content assist environment") {
        @Override
        protected void execute(IProgressMonitor monitor) {
          m_provider.close();
        }
      };
      closeJob.setSystem(true);
      closeJob.setUser(false);
      closeJob.schedule();
    }

    @Override
    public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
      // nop
    }
  }
}
