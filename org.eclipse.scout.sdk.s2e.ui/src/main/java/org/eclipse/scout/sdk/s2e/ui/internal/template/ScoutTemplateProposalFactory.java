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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.model.ScoutModelHierarchy;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.trigger.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.trigger.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ast.AstUtils;

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
    TEMPLATES.put(IScoutRuntimeTypes.IStringField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IStringField, IScoutRuntimeTypes.AbstractStringField, "MyString",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.StringFieldAdd, 1000, StringFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IBigDecimalField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IBigDecimalField, IScoutRuntimeTypes.AbstractBigDecimalField, "MyBigDecimal",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.DoubleFieldAdd, 1000, BigDecimalFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IBooleanField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IBooleanField, IScoutRuntimeTypes.AbstractBooleanField, "MyBoolean",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, 1000, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IButton, new TemplateProposalDescriptor(IScoutRuntimeTypes.IButton, IScoutRuntimeTypes.AbstractButton, "My",
        ISdkProperties.SUFFIX_BUTTON, ISdkIcons.ButtonAdd, 1000, ButtonProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ICalendarField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ICalendarField, IScoutRuntimeTypes.AbstractCalendarField, "MyCalendar",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, 1000, CalendarFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IDateField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IDateField, IScoutRuntimeTypes.AbstractDateField, "MyDate",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.DateFieldAdd, 1000, DateFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IFileChooserField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IFileChooserField, IScoutRuntimeTypes.AbstractFileChooserField, "MyFileChooser",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FileChooserFieldAdd, 1000, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IGroupBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.IGroupBox, IScoutRuntimeTypes.AbstractGroupBox, "MyGroup",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.GroupBoxAdd, 1000, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IHtmlField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IHtmlField, IScoutRuntimeTypes.AbstractHtmlField, "MyHtml",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, 1000, FormFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ILabelField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ILabelField, IScoutRuntimeTypes.AbstractLabelField, "MyLabel",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.FormFieldAdd, 1000, LabelFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IListBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.IListBox, IScoutRuntimeTypes.AbstractListBox, "MyList",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.FormFieldAdd, 1000, ListBoxFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IProposalField, new TemplateProposalDescriptor(IScoutRuntimeTypes.IProposalField, IScoutRuntimeTypes.AbstractProposalField, "MyProposal",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.SmartFieldAdd, 1000, ValueTypeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ISmartField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ISmartField, IScoutRuntimeTypes.AbstractSmartField, "MySmart",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.SmartFieldAdd, 1000, ValueTypeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ILongField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ILongField, IScoutRuntimeTypes.AbstractLongField, "MyLong",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.IntegerFieldAdd, 1000, LongFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IRadioButtonGroup, new TemplateProposalDescriptor(IScoutRuntimeTypes.IRadioButtonGroup, IScoutRuntimeTypes.AbstractRadioButtonGroup, "MyRadioButtonGroup",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.RadioButtonGroupAdd, 1000, ValueTypeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ISequenceBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.ISequenceBox, IScoutRuntimeTypes.AbstractSequenceBox, "MySequence",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.SequenceBoxAdd, 1000, SequenceBoxProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ITabBox, new TemplateProposalDescriptor(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.AbstractTabBox, "MyTab",
        ISdkProperties.SUFFIX_COMPOSITE_FIELD, ISdkIcons.TabBoxAdd, 1000, TabBoxProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ITableField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ITableField, IScoutRuntimeTypes.AbstractTableField, "MyTable",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.TableFieldAdd, 1000, TableFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ITreeField, new TemplateProposalDescriptor(IScoutRuntimeTypes.ITreeField, IScoutRuntimeTypes.AbstractTreeField, "MyTree",
        ISdkProperties.SUFFIX_FORM_FIELD, ISdkIcons.TreeFieldAdd, 1000, TreeFieldProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IRadioButton, new TemplateProposalDescriptor(IScoutRuntimeTypes.IRadioButton, IScoutRuntimeTypes.AbstractRadioButton, "MyRadio",
        ISdkProperties.SUFFIX_BUTTON, ISdkIcons.RadioButtonAdd, 1000, RadioButtonProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IMenu, new TemplateProposalDescriptor(IScoutRuntimeTypes.IMenu, IScoutRuntimeTypes.AbstractMenu, "MyMenu",
        ISdkProperties.SUFFIX_MENU, ISdkIcons.MenuAdd, 1000, MenuProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IKeyStroke, new TemplateProposalDescriptor(IScoutRuntimeTypes.IKeyStroke, IScoutRuntimeTypes.AbstractKeyStroke, "My",
        ISdkProperties.SUFFIX_KEY_STROKE, ISdkIcons.KeyStrokeAdd, 1000, KeyStrokeProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.ICode, new TemplateProposalDescriptor(IScoutRuntimeTypes.ICode, IScoutRuntimeTypes.AbstractCode, "My",
        ISdkProperties.SUFFIX_CODE, ISdkIcons.CodeAdd, 1000, CodeProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IFormHandler, new TemplateProposalDescriptor(IScoutRuntimeTypes.IFormHandler, IScoutRuntimeTypes.AbstractFormHandler, "My",
        ISdkProperties.SUFFIX_FORM_HANDLER, ISdkIcons.FormHandler, 1000, FormHandlerProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IColumn, new TemplateProposalDescriptor(IScoutRuntimeTypes.IColumn, IScoutRuntimeTypes.AbstractStringColumn, "My",
        ISdkProperties.SUFFIX_COLUMN, ISdkIcons.ColumnAdd, 1000, ColumnProposal.class));
    TEMPLATES.put(IScoutRuntimeTypes.IExtension, new TemplateProposalDescriptor(IScoutRuntimeTypes.IExtension, IScoutRuntimeTypes.AbstractExtension, "My",
        ISdkProperties.SUFFIX_EXTENSION, ISdkIcons.ExtensionsAdd, 1000, ExtensionProposal.class));
  }

  public static List<ICompletionProposal> createTemplateProposals(IType surroundingType, int offset) {
    ITypeRoot typeRoot = surroundingType.getTypeRoot();
    CompilationUnit cu = SharedASTProvider.getAST(typeRoot, SharedASTProvider.WAIT_ACTIVE_ONLY, null);
    if (cu == null) {
      return Collections.emptyList();
    }

    NodeFinder finder = new NodeFinder(cu, offset, 0);
    ASTNode coveringNode = finder.getCoveringNode();
    if (!(coveringNode instanceof TypeDeclaration)) {
      return Collections.emptyList();
    }
    TypeDeclaration declaringType = (TypeDeclaration) coveringNode;
    ITypeBinding resolveBinding = declaringType.resolveBinding();
    if (resolveBinding == null) {
      return Collections.emptyList();
    }

    Set<String> possibleChildrenIfcFqn = new HashSet<>();
    if (AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractTabBox)
        || AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractTabBoxExtension)) {
      // special case for tab boxes
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IGroupBox);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IMenu);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IKeyStroke);
    }
    else if (AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractListBox)
        || AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractTreeBox)
        || AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractListBoxExtension)
        || AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractTreeBoxExtension)) {
      // special case for list boxes & tree boxes
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IMenu);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IKeyStroke);
    }
    else if (AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractRadioButtonGroup)
        || AstUtils.isInstanceOf(resolveBinding, IScoutRuntimeTypes.AbstractRadioButtonGroupExtension)) {
      // special case for radio button groups
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IRadioButton);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IMenu);
      possibleChildrenIfcFqn.add(IScoutRuntimeTypes.IKeyStroke);
    }
    else {
      for (ITypeBinding superType : AstUtils.getAllSuperTypes(resolveBinding)) {
        Set<String> possibleChildren = ScoutModelHierarchy.getPossibleChildren(superType.getTypeDeclaration().getQualifiedName());
        if (!possibleChildren.isEmpty()) {
          possibleChildrenIfcFqn.addAll(possibleChildren);
        }
      }
    }
    if (possibleChildrenIfcFqn.isEmpty()) {
      return Collections.emptyList();
    }

    // create env
    ICompilationUnit compilationUnit = surroundingType.getCompilationUnit();
    IJavaEnvironmentProvider provider = new CachingJavaEnvironmentProvider();
    CountDownLatch l = new CountDownLatch(1); // latch to wait until the P_JavaEnvInitJob has started an acquired the lock on the provider
    new P_JavaEnvInitJob(declaringType, provider, compilationUnit, l).schedule();
    try {
      l.await(100, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      SdkLog.debug("Interrupted while waiting for the java environemnt preparation latch.", e);
      return Collections.emptyList();
    }

    // create proposals
    IJavaProject javaProject = surroundingType.getJavaProject();
    List<ICompletionProposal> result = new ArrayList<>();
    TemplateProposalDescriptor[] templates = TEMPLATES.values().toArray(new TemplateProposalDescriptor[TEMPLATES.size()]);
    for (TemplateProposalDescriptor candidate : templates) {
      if (candidate.isActiveFor(possibleChildrenIfcFqn, javaProject)) {
        result.add(candidate.createProposal(compilationUnit, declaringType, offset, resolveBinding, provider));
      }
    }
    return result;
  }

  private static final class P_JavaEnvInitJob extends AbstractJob {

    private final TypeDeclaration m_decl;
    private final IJavaEnvironmentProvider m_envProvider;
    private final ICompilationUnit m_icu;
    private final CountDownLatch m_notifyLatch;

    private P_JavaEnvInitJob(TypeDeclaration decl, IJavaEnvironmentProvider envProvider, ICompilationUnit icu, CountDownLatch notifyLatch) {
      super("Init Java Environment");
      m_decl = decl;
      m_envProvider = envProvider;
      m_icu = icu;
      m_notifyLatch = notifyLatch;
      setUser(false);
      setSystem(true);
      setPriority(Job.INTERACTIVE);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        synchronized (m_envProvider) {
          m_notifyLatch.countDown();

          String pck = S2eUtils.getPackage(m_icu);
          if (StringUtils.isBlank(pck)) {
            pck = null;
          }
          IJavaEnvironment env = m_envProvider.get(m_icu.getJavaProject());
          env.registerCompilationUnitOverride(pck, m_icu.getElementName(), new StringBuilder(m_icu.getSource()));

          TypeDeclaration primary = AstUtils.getDeclaringTypes(m_decl).getLast();
          String qualifiedName = AstUtils.getFullyQualifiedName(primary);
          env.findType(qualifiedName);
        }
      }
      catch (Exception e) {
        SdkLog.info("Unable to preload java environment.", e);
      }
      return Status.OK_STATUS;
    }
  }
}
