/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import static java.util.Arrays.asList;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.model.ScoutModelHierarchy;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.job.RunnableJob;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link ScoutTemplateProposalFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class ScoutTemplateProposalFactory {

  private ScoutTemplateProposalFactory() {
  }

  public static final Map<String, TemplateProposalDescriptor> TEMPLATES = new ConcurrentHashMap<>();

  static {
    int relevance = 10000;
    TEMPLATES.put(IStringField,
        new TemplateProposalDescriptor(IStringField, AbstractStringField, "MyString", SUFFIX_FORM_FIELD, StringFieldAdd, relevance, StringFieldProposal.class, asList("textfield")));
    TEMPLATES.put(IBigDecimalField,
        new TemplateProposalDescriptor(IBigDecimalField, AbstractBigDecimalField, "MyBigDecimal", SUFFIX_FORM_FIELD, DoubleFieldAdd, relevance, BigDecimalFieldProposal.class, asList("numberfield", "doublefield", "floatfield")));
    TEMPLATES.put(IBooleanField,
        new TemplateProposalDescriptor(IBooleanField, AbstractBooleanField, "MyBoolean", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, FormFieldProposal.class, asList("checkboxfield", "tristatefield")));
    TEMPLATES.put(IButton,
        new TemplateProposalDescriptor(IButton, AbstractButton, "My", SUFFIX_BUTTON, ButtonAdd, relevance, ButtonProposal.class));
    TEMPLATES.put(ICalendarField,
        new TemplateProposalDescriptor(ICalendarField, AbstractCalendarField, "MyCalendar", SUFFIX_FORM_FIELD, FormFieldAdd, relevance, CalendarFieldProposal.class));
    TEMPLATES.put(IDateField,
        new TemplateProposalDescriptor(IDateField, AbstractDateField, "MyDate", SUFFIX_FORM_FIELD, DateFieldAdd, relevance, DateFieldProposal.class, asList("datetimefield")));
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

  public static List<ICompletionProposal> createTemplateProposals(IType declaringType, int offset, String prefix) {
    Set<String> possibleChildrenIfcFqn = new HashSet<>();
    Set<String> superTypesOfDeclaringType = null;
    ISourceRange surroundingTypeNameRange = null;
    try {
      ITypeHierarchy supertypeHierarchy = declaringType.newSupertypeHierarchy(null);
      IType[] allTypes = supertypeHierarchy.getAllTypes();
      superTypesOfDeclaringType = new HashSet<>(allTypes.length);
      for (IType superType : allTypes) {
        superTypesOfDeclaringType.add(superType.getFullyQualifiedName());
      }
      surroundingTypeNameRange = declaringType.getNameRange();
    }
    catch (JavaModelException e) {
      SdkLog.error("Unable to calculate supertype hierarchy for '{}'.", declaringType.getFullyQualifiedName(), e);
      return Collections.emptyList();
    }

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
    if (possibleChildrenIfcFqn.isEmpty()) {
      return Collections.emptyList();
    }

    ICompilationUnit compilationUnit = declaringType.getCompilationUnit();

    // start java environment creation
    RunnableFuture<IJavaEnvironmentProvider> javaEnvProviderCreator = new FutureTask<>(new P_JavaEnvironmentInitCallable(compilationUnit, prefix != null, offset));
    RunnableJob javaEnvCreatorJob = new RunnableJob("Init Java Environment", javaEnvProviderCreator);
    javaEnvCreatorJob.setUser(false);
    javaEnvCreatorJob.setSystem(true);
    javaEnvCreatorJob.setPriority(Job.SHORT);
    javaEnvCreatorJob.schedule();

    // create proposals
    IJavaProject javaProject = declaringType.getJavaProject();
    List<ICompletionProposal> result = new ArrayList<>();
    TemplateProposalDescriptor[] templates = null;
    synchronized (ScoutTemplateProposalFactory.TEMPLATES) {
      templates = TEMPLATES.values().toArray(new TemplateProposalDescriptor[TEMPLATES.size()]);
    }
    for (TemplateProposalDescriptor candidate : templates) {
      if (candidate.isActiveFor(possibleChildrenIfcFqn, javaProject, prefix)) {
        result.add(candidate.createProposal(compilationUnit, offset, surroundingTypeNameRange, javaEnvProviderCreator, prefix));
      }
    }
    return result;
  }

  private static final class P_JavaEnvironmentInitCallable implements Callable<IJavaEnvironmentProvider> {

    private final ICompilationUnit m_icu;
    private final boolean m_hasSearchString;
    private final int m_pos;

    private P_JavaEnvironmentInitCallable(ICompilationUnit icu, boolean hasSearchString, int pos) {
      m_icu = icu;
      m_hasSearchString = hasSearchString;
      m_pos = pos;
    }

    @Override
    public IJavaEnvironmentProvider call() throws Exception {
      IJavaEnvironmentProvider provider = new CachingJavaEnvironmentProvider();
      String pck = S2eUtils.getPackage(m_icu);
      if (StringUtils.isBlank(pck)) {
        pck = null;
      }
      IJavaEnvironment env = provider.get(m_icu.getJavaProject());

      StringBuilder buf = new StringBuilder(m_icu.getSource());
      if (m_hasSearchString) {
        buf.insert(m_pos, AbstractTypeProposal.SEARCH_STRING_END_FIX);
      }
      env.registerCompilationUnitOverride(pck, m_icu.getElementName(), buf);

      env.findType(m_icu.findPrimaryType().getFullyQualifiedName());
      return provider;
    }
  }
}
