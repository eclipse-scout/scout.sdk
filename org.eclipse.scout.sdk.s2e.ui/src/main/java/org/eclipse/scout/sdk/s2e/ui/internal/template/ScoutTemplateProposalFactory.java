/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_BUTTON;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_CODE;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_COLUMN;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_COMPOSITE_FIELD;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_EXTENSION;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_FORM_FIELD;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_FORM_HANDLER;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_KEY_STROKE;
import static org.eclipse.scout.sdk.core.s.ISdkConstants.SUFFIX_MENU;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.BiConsumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ContentAssistantFacade;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.ScoutModelHierarchy;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.RunnableJob;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>{@link ScoutTemplateProposalFactory}</h3>
 *
 * @since 5.2.0
 */
public final class ScoutTemplateProposalFactory {

  private static final Collection<BiConsumer<Map<String, TemplateProposalDescriptor>, ScoutModelHierarchy>> TEMPLATE_PARTICIPANTS = new HashSet<>();

  public static synchronized void registerTemplateParticipant(BiConsumer<Map<String, TemplateProposalDescriptor>, ScoutModelHierarchy> participant) {
    TEMPLATE_PARTICIPANTS.add(Ensure.notNull(participant));
  }

  public static synchronized void unregisterTemplateParticipant(BiConsumer<Map<String, TemplateProposalDescriptor>, ScoutModelHierarchy> participant) {
    TEMPLATE_PARTICIPANTS.remove(participant);
  }

  static synchronized Collection<BiConsumer<Map<String, TemplateProposalDescriptor>, ScoutModelHierarchy>> participants() {
    return new HashSet<>(TEMPLATE_PARTICIPANTS);
  }

  private ScoutTemplateProposalFactory() {
  }

  static Map<String, TemplateProposalDescriptor> descriptors(ScoutModelHierarchy hierarchy) {
    Map<String, TemplateProposalDescriptor> templates = new HashMap<>();
    var relevance = 10000;
    var api = hierarchy.api();

    templates.put(api.IStringField().fqn(), new TemplateProposalDescriptor(
        api.IStringField().fqn(),
        api.AbstractStringField().fqn(),
        "MyString", SUFFIX_FORM_FIELD, StringFieldAdd, relevance, StringFieldProposal.class, singletonList("textfield")));
    templates.put(api.IBigDecimalField().fqn(), new TemplateProposalDescriptor(
        api.IBigDecimalField().fqn(),
        api.AbstractBigDecimalField().fqn(),
        "MyBigDecimal", SUFFIX_FORM_FIELD, DoubleFieldAdd, relevance, BigDecimalFieldProposal.class, asList("numberfield", "doublefield", "floatfield")));
    templates.put(api.IBooleanField().fqn(), new TemplateProposalDescriptor(
        api.IBooleanField().fqn(),
        api.AbstractBooleanField().fqn(),
        "MyBoolean", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, FormFieldProposal.class, asList("checkboxfield", "tristatefield")));
    templates.put(api.IButton().fqn(), new TemplateProposalDescriptor(
        api.IButton().fqn(),
        api.AbstractButton().fqn(),
        "My", SUFFIX_BUTTON, ButtonAdd, relevance, ButtonProposal.class));
    templates.put(api.ICalendarField().fqn(), new TemplateProposalDescriptor(
        api.ICalendarField().fqn(),
        api.AbstractCalendarField().fqn(),
        "MyCalendar", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, CalendarFieldProposal.class));
    templates.put(api.IDateField().fqn(), new TemplateProposalDescriptor(
        api.IDateField().fqn(),
        api.AbstractDateField().fqn(),
        "MyDate", SUFFIX_FORM_FIELD, DateFieldAdd, relevance, DateFieldProposal.class, singletonList("datetimefield")));
    templates.put(api.IFileChooserField().fqn(), new TemplateProposalDescriptor(
        api.IFileChooserField().fqn(),
        api.AbstractFileChooserField().fqn(),
        "MyFileChooser", SUFFIX_FORM_FIELD, FileChooserFieldAdd, relevance, FormFieldProposal.class));
    templates.put(api.IGroupBox().fqn(), new TemplateProposalDescriptor(
        api.IGroupBox().fqn(),
        api.AbstractGroupBox().fqn(),
        "MyGroup", SUFFIX_COMPOSITE_FIELD, GroupBoxAdd, relevance, FormFieldProposal.class));
    templates.put(api.IHtmlField().fqn(), new TemplateProposalDescriptor(
        api.IHtmlField().fqn(),
        api.AbstractHtmlField().fqn(),
        "MyHtml", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, FormFieldProposal.class));
    templates.put(api.ILabelField().fqn(), new TemplateProposalDescriptor(
        api.ILabelField().fqn(),
        api.AbstractLabelField().fqn(),
        "MyLabel", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, LabelFieldProposal.class));
    templates.put(api.IListBox().fqn(), new TemplateProposalDescriptor(
        api.IListBox().fqn(),
        api.AbstractListBox().fqn(),
        "MyList", SUFFIX_COMPOSITE_FIELD, FormFieldAdd, relevance, ListBoxFieldProposal.class));
    templates.put(api.IProposalField().fqn(), new TemplateProposalDescriptor(
        api.IProposalField().fqn(),
        api.AbstractProposalField().fqn(),
        "MyProposal", SUFFIX_FORM_FIELD, SmartFieldAdd, relevance, ValueTypeFieldProposal.class));
    templates.put(api.ISmartField().fqn(), new TemplateProposalDescriptor(
        api.ISmartField().fqn(),
        api.AbstractSmartField().fqn(),
        "MySmart", SUFFIX_FORM_FIELD, SmartFieldAdd, relevance, ValueTypeFieldProposal.class));
    templates.put(api.ILongField().fqn(), new TemplateProposalDescriptor(
        api.ILongField().fqn(),
        api.AbstractLongField().fqn(),
        "MyLong", SUFFIX_FORM_FIELD, IntegerFieldAdd, relevance, LongFieldProposal.class, asList("integerfield", "numberfield")));
    templates.put(api.IRadioButtonGroup().fqn(), new TemplateProposalDescriptor(
        api.IRadioButtonGroup().fqn(),
        api.AbstractRadioButtonGroup().fqn(),
        "MyRadioButtonGroup", "", RadioButtonGroupAdd, relevance, ValueTypeFieldProposal.class));
    templates.put(api.ISequenceBox().fqn(), new TemplateProposalDescriptor(
        api.ISequenceBox().fqn(),
        api.AbstractSequenceBox().fqn(),
        "MySequence", SUFFIX_COMPOSITE_FIELD, SequenceBoxAdd, relevance, SequenceBoxProposal.class));
    templates.put(api.ITabBox().fqn(), new TemplateProposalDescriptor(
        api.ITabBox().fqn(),
        api.AbstractTabBox().fqn(),
        "MyTab", SUFFIX_COMPOSITE_FIELD, TabBoxAdd, relevance, TabBoxProposal.class));
    templates.put(api.ITableField().fqn(), new TemplateProposalDescriptor(
        api.ITableField().fqn(),
        api.AbstractTableField().fqn(),
        "MyTable", SUFFIX_FORM_FIELD, TableFieldAdd, relevance, TableFieldProposal.class));
    templates.put(api.ITreeField().fqn(), new TemplateProposalDescriptor(
        api.ITreeField().fqn(),
        api.AbstractTreeField().fqn(),
        "MyTree", SUFFIX_FORM_FIELD, TreeFieldAdd, relevance, TreeFieldProposal.class));
    templates.put(api.IRadioButton().fqn(), new TemplateProposalDescriptor(
        api.IRadioButton().fqn(),
        api.AbstractRadioButton().fqn(),
        "MyRadio", SUFFIX_BUTTON, RadioButtonAdd, relevance, RadioButtonProposal.class));
    templates.put(api.IMenu().fqn(), new TemplateProposalDescriptor(
        api.IMenu().fqn(),
        api.AbstractMenu().fqn(),
        "MyMenu", SUFFIX_MENU, MenuAdd, relevance, MenuProposal.class));
    templates.put(api.IKeyStroke().fqn(), new TemplateProposalDescriptor(
        api.IKeyStroke().fqn(),
        api.AbstractKeyStroke().fqn(),
        "My", SUFFIX_KEY_STROKE, KeyStrokeAdd, relevance, KeyStrokeProposal.class));
    templates.put(api.ICode().fqn(), new TemplateProposalDescriptor(
        api.ICode().fqn(),
        api.AbstractCode().fqn(),
        "My", SUFFIX_CODE, CodeAdd, relevance, CodeProposal.class));
    templates.put(api.IFormHandler().fqn(), new TemplateProposalDescriptor(
        api.IFormHandler().fqn(),
        api.AbstractFormHandler().fqn(),
        "My", SUFFIX_FORM_HANDLER, FormHandler, relevance, FormHandlerProposal.class));
    templates.put(api.IColumn().fqn(), new TemplateProposalDescriptor(
        api.IColumn().fqn(),
        api.AbstractStringColumn().fqn(),
        "My", SUFFIX_COLUMN, ColumnAdd, relevance, ColumnProposal.class));
    templates.put(api.IExtension().fqn(), new TemplateProposalDescriptor(
        api.IExtension().fqn(),
        api.AbstractExtension().fqn(),
        "My", SUFFIX_EXTENSION, ExtensionsAdd, relevance, ExtensionProposal.class));
    templates.put(api.IImageField().fqn(), new TemplateProposalDescriptor(
        api.IImageField().fqn(),
        api.AbstractImageField().fqn(),
        "MyImage", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, ImageFieldProposal.class));

    participants().forEach(part -> part.accept(templates, hierarchy));

    return templates;
  }

  public static List<ICompletionProposal> createTemplateProposals(IType declaringType, int offset, String prefix, SourceViewer viewer) {
    var javaProject = declaringType.getJavaProject();
    if (!JdtUtils.exists(javaProject)) {
      return emptyList();
    }

    var optScoutApi = ApiHelper.scoutApiFor(javaProject);
    if (optScoutApi.isEmpty()) {
      return emptyList();
    }

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
    if (superTypesOfDeclaringType.isEmpty()) {
      return emptyList();
    }

    var scoutModelHierarchy = optScoutApi.orElseThrow().hierarchy();
    Collection<String> possibleChildrenIfcFqn = scoutModelHierarchy.possibleChildrenFor(superTypesOfDeclaringType);
    if (possibleChildrenIfcFqn.isEmpty()) {
      return emptyList();
    }

    var compilationUnit = declaringType.getCompilationUnit();

    // start java environment creation
    RunnableFuture<EclipseEnvironment> javaEnvProviderCreator = new FutureTask<>(new P_JavaEnvironmentPreloader(compilationUnit, prefix != null, offset, viewer));
    Job javaEnvCreatorJob = new RunnableJob("Init Java Environment", javaEnvProviderCreator);
    javaEnvCreatorJob.setUser(false);
    javaEnvCreatorJob.setSystem(true);
    javaEnvCreatorJob.setPriority(Job.SHORT);
    javaEnvCreatorJob.schedule();

    // create proposals
    return descriptors(scoutModelHierarchy).values().stream()
        .filter(candidate -> candidate.isActiveFor(possibleChildrenIfcFqn, scoutModelHierarchy, prefix))
        .map(candidate -> candidate.createProposal(compilationUnit, offset, scoutModelHierarchy, surroundingTypeNameRange, javaEnvProviderCreator, prefix))
        .collect(toList());
  }

  private static Set<String> getAllSuperTypesOf(IType declaringType) throws JavaModelException {
    var supertypeHierarchy = declaringType.newSupertypeHierarchy(null);
    var allTypes = supertypeHierarchy.getAllTypes();
    return Arrays.stream(allTypes)
        .map(IType::getFullyQualifiedName)
        .collect(toSet());
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
      var pck = JdtUtils.getPackage(m_icu);
      if (Strings.isBlank(pck)) {
        pck = null;
      }
      var env = m_provider.toScoutJavaEnvironment(m_icu.getJavaProject());

      var buf = new StringBuilder(m_icu.getSource());
      if (m_hasSearchString) {
        buf.insert(m_pos, AbstractTypeProposal.SEARCH_STRING_END_FIX);
      }
      env.registerCompilationUnitOverride(buf, pck, m_icu.getElementName());

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
      var closeJob = new AbstractJob("close content assist environment") {
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
