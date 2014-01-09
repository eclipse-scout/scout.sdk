/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryModifyAction;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerationContext;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.annotation.AnnotationNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>{@link DocumentationPresenter}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 08.01.2014
 */
public class DocumentationPresenter extends AbstractPresenter {
  private final INlsProject m_docNlsProject;
  private final IType m_type;

  private Text m_textComponent;
  private ImageHyperlink m_editText;

  public DocumentationPresenter(PropertyViewFormToolkit toolkit, Composite parent, AbstractScoutTypePage page) {
    super(toolkit, parent);
    m_type = page.getType();
    m_docNlsProject = page.getScoutBundle().getDocsNlsProject();

    createContent(getContainer());
    refresh();
  }

  public INlsProject getNlsProject() {
    return m_docNlsProject;
  }

  public IType getType() {
    return m_type;
  }

  protected void createContent(Composite container) {
    m_textComponent = getToolkit().createText(container, "", SWT.BORDER | SWT.LEFT | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
    m_textComponent.setEnabled(false);

    m_editText = getToolkit().createImageHyperlink(container, SWT.PUSH);
    m_editText.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolEdit));
    m_editText.setToolTipText(Texts.get("EditContent"));
    m_editText.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        handleLinkClicked();
      }
    });
    m_editText.setEnabled(!getType().isBinary());

    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 3;
    layout.marginWidth = 0;
    container.setLayout(layout);

    GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL);
    data.heightHint = 100;
    m_textComponent.setLayoutData(data);

    data = new GridData(GridData.FILL_VERTICAL);
    data.verticalAlignment = SWT.CENTER;
    m_editText.setLayoutData(data);
  }

  protected String generateNlsKey(String classId) {
    return getNlsProject().generateKey(classId);
  }

  protected void createNew() {
    String classId = null;
    try {
      classId = ScoutTypeUtility.getClassIdAnnotationValue(getType());
    }
    catch (JavaModelException e1) {
      ScoutSdkUi.logWarning("Unable to get @ClassId annotation value from type '" + getType() + "'.", e1);
    }

    boolean createClassIdAnnotation = classId == null;
    if (createClassIdAnnotation) {
      classId = ClassIdGenerators.generateNewId(new ClassIdGenerationContext(getType()));
    }

    String key = generateNlsKey(classId);
    NlsEntry entry = new NlsEntry(key, getNlsProject());

    NlsEntryNewAction action = new NlsEntryNewAction(getContainer().getShell(), getNlsProject(), entry, true);
    action.run();
    try {
      action.join();
    }
    catch (InterruptedException e) {
      ScoutSdkUi.logWarning(e);
    }

    entry = action.getEntry();
    if (entry != null) {
      // ok pressed
      boolean keyModified = CompareUtility.notEquals(key, entry.getKey());
      if (createClassIdAnnotation || keyModified) {
        updateClassIdAnnotation(entry, classId, keyModified);
      }
      else {
        refresh();
      }
    }
  }

  protected void updateClassIdAnnotation(NlsEntry entry, String classId, boolean keyModified) {
    if (keyModified) {
      classId = entry.getKey();
    }
    IAnnotationSourceBuilder sourceBuilder = AnnotationSourceBuilderFactory.createClassIdAnnotation(classId);
    OperationJob j = new OperationJob(new AnnotationNewOperation(sourceBuilder, getType()));
    j.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        getContainer().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            refresh();
          }
        });
      }
    });
    j.schedule();
  }

  protected void editExisting(INlsEntry nlsEntry) {
    NlsEntryModifyAction action = new NlsEntryModifyAction(getContainer().getShell(), nlsEntry, nlsEntry.getProject());
    action.run();
    try {
      action.join();
    }
    catch (InterruptedException e) {
      ScoutSdkUi.logWarning(e);
    }

    INlsEntry e = action.getEntry();
    if (e != null) {
      // ok pressed
      refresh();
    }
  }

  protected void handleLinkClicked() {
    INlsEntry nlsEntry = getNlsEntry();
    if (nlsEntry == null) {
      createNew();
    }
    else {
      editExisting(nlsEntry);
    }
  }

  protected INlsEntry getNlsEntry() {
    try {
      String classId = ScoutTypeUtility.getClassIdAnnotationValue(getType());
      if (StringUtility.hasText(classId)) {
        String key = generateNlsKey(classId);
        return getNlsProject().getEntry(key);
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("Unable to get the documentation text for type '" + getType().getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  protected void refresh() {
    INlsEntry entry = getNlsEntry();
    if (entry != null) {
      String text = entry.getTranslation(entry.getProject().getDevelopmentLanguage(), true);
      if (StringUtility.hasText(text)) {
        m_textComponent.setText(text);
      }
      else {
        m_textComponent.setText("");
      }
    }
  }
}
