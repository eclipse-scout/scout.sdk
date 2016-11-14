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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.model.ScoutModelHierarchy;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.job.RunnableJob;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
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
    TEMPLATES.put(IScoutRuntimeTypes.IStringField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IStringField, IScoutRuntimeTypes.AbstractStringField, "MyString",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.StringFieldAdd, relevance, StringFieldProposal.class, Arrays.asList("textfield")));
    TEMPLATES.put(IScoutRuntimeTypes.IBigDecimalField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IBigDecimalField, IScoutRuntimeTypes.AbstractBigDecimalField, "MyBigDecimal",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.DoubleFieldAdd, relevance, BigDecimalFieldProposal.class, Arrays.asList("numberfield", "doublefield", "floatfield")));
    TEMPLATES.put(IScoutRuntimeTypes.IBooleanField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IBooleanField, IScoutRuntimeTypes.AbstractBooleanField, "MyBoolean",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, relevance, FormFieldProposal.class, Arrays.asList("checkboxfield", "tristatefield")));
    TEMPLATES.put(IScoutRuntimeTypes.IButton, new TemplateProposalDescriptor(IScoutRuntimeTypes.IButton, IScoutRuntimeTypes.AbstractButton, "My",
        ISdkProperties.SUFFIX_BUTTON, ISdkIcons.ButtonAdd, relevance, ButtonProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ICalendarField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ICalendarField, IScoutRuntimeTypes.AbstractCalendarField, "MyCalendar",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, relevance, CalendarFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IDateField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IDateField, IScoutRuntimeTypes.AbstractDateField, "MyDate",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.DateFieldAdd, relevance, DateFieldProposal.class, Arrays.asList("datetimefield")));
    TEMPLATES.put(IScoutRuntimeTypes.IFileChooserField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IFileChooserField, IScoutRuntimeTypes.AbstractFileChooserField, "MyFileChooser",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FileChooserFieldAdd, relevance, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IGroupBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.IGroupBox, IScoutRuntimeTypes.AbstractGroupBox, "MyGroup",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.GroupBoxAdd, relevance, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IHtmlField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IHtmlField, IScoutRuntimeTypes.AbstractHtmlField, "MyHtml",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, relevance, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ILabelField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ILabelField, IScoutRuntimeTypes.AbstractLabelField, "MyLabel",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, relevance, LabelFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IListBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.IListBox, IScoutRuntimeTypes.AbstractListBox, "MyList",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.FormFieldAdd, relevance, ListBoxFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IProposalField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IProposalField, IScoutRuntimeTypes.AbstractProposalField, "MyProposal",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.SmartFieldAdd, relevance, ValueTypeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ISmartField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ISmartField, IScoutRuntimeTypes.AbstractSmartField, "MySmart",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.SmartFieldAdd, relevance, ValueTypeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ILongField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ILongField, IScoutRuntimeTypes.AbstractLongField, "MyLong",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.IntegerFieldAdd, relevance, LongFieldProposal.class, Arrays.asList("integerfield", "numberfield")));
    TEMPLATES.put(IScoutRuntimeTypes.IRadioButtonGroup, new TemplateProposalDescriptor(IScoutRuntimeTypes.IRadioButtonGroup, IScoutRuntimeTypes.AbstractRadioButtonGroup, "MyRadioButtonGroup",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.RadioButtonGroupAdd, relevance, ValueTypeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ISequenceBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.ISequenceBox, IScoutRuntimeTypes.AbstractSequenceBox, "MySequence",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.SequenceBoxAdd, relevance, SequenceBoxProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ITabBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.AbstractTabBox, "MyTab",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.TabBoxAdd, relevance, TabBoxProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ITableField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ITableField, IScoutRuntimeTypes.AbstractTableField, "MyTable",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.TableFieldAdd, relevance, TableFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ITreeField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ITreeField, IScoutRuntimeTypes.AbstractTreeField, "MyTree",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.TreeFieldAdd, relevance, TreeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IRadioButton, new TemplateProposalDescriptor(IScoutRuntimeTypes.IRadioButton, IScoutRuntimeTypes.AbstractRadioButton, "MyRadio",
        ISdkProperties.SUFFIX_BUTTON, ISdkIcons.RadioButtonAdd, relevance, RadioButtonProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IMenu, new TemplateProposalDescriptor(IScoutRuntimeTypes.IMenu, IScoutRuntimeTypes.AbstractMenu, "MyMenu",
        ISdkProperties.SUFFIX_MENU, ISdkIcons.MenuAdd, relevance, MenuProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IKeyStroke, new TemplateProposalDescriptor(IScoutRuntimeTypes.IKeyStroke, IScoutRuntimeTypes.AbstractKeyStroke, "My",
        ISdkProperties.SUFFIX_KEY_STROKE, ISdkIcons.KeyStrokeAdd, relevance, KeyStrokeProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ICode, new TemplateProposalDescriptor(IScoutRuntimeTypes.ICode, IScoutRuntimeTypes.AbstractCode, "My",
        ISdkProperties.SUFFIX_CODE, ISdkIcons.CodeAdd, relevance, CodeProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IFormHandler, new TemplateProposalDescriptor(IScoutRuntimeTypes.IFormHandler, IScoutRuntimeTypes.AbstractFormHandler, "My",
        ISdkProperties.SUFFIX_FORM_HANDLER, ISdkIcons.FormHandler, relevance, FormHandlerProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IColumn, new TemplateProposalDescriptor(IScoutRuntimeTypes.IColumn, IScoutRuntimeTypes.AbstractStringColumn, "My",
        ISdkProperties.SUFFIX_COLUMN, ISdkIcons.ColumnAdd, relevance, ColumnProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IExtension, new TemplateProposalDescriptor(IScoutRuntimeTypes.IExtension, IScoutRuntimeTypes.AbstractExtension, "My",
        ISdkProperties.SUFFIX_EXTENSION, ISdkIcons.ExtensionsAdd, relevance, ExtensionProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IImageField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IImageField, IScoutRuntimeTypes.AbstractImageField, "MyImage",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, relevance, ImageFieldProposal.class));
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

    if (superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractTabBox)
        || superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractTabBoxExtension)) {
      // special case for tab boxes
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IGroupBox);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IMenu);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IKeyStroke);
    }
    else if (superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractListBox)
        || superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractTreeBox)
        || superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractListBoxExtension)
        || superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractTreeBoxExtension)) {
      // special case for list boxes & tree boxes
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IMenu);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IKeyStroke);
    }
    else if (superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractRadioButtonGroup)
        || superTypesOfDeclaringType.contains(IScoutRuntimeTypes.AbstractRadioButtonGroupExtension)) {
      // special case for radio button groups
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IRadioButton);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IMenu);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IKeyStroke);
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