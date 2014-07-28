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
package org.eclipse.scout.sdk.ui.extensions.quickassist;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryModifyAction;
import org.eclipse.scout.nls.sdk.ui.action.NlsEntryNewAction;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerationContext;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.annotation.AnnotationNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link ClassIdDocumentationSupport}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 28.03.2014
 */
public class ClassIdDocumentationSupport {

  private final IType m_type;
  private final INlsProject m_project;
  private final EventListenerList m_eventListeners;

  public ClassIdDocumentationSupport(IType type) {
    this(type, computeNlsProject(type));
  }

  public ClassIdDocumentationSupport(IType type, INlsProject nlsProject) {
    m_type = type;
    m_project = nlsProject;
    m_eventListeners = new EventListenerList();
  }

  public void addModifiedListener(IClassIdDocumentationListener l) {
    m_eventListeners.add(IClassIdDocumentationListener.class, l);
  }

  public void removeModifiedListener(IClassIdDocumentationListener l) {
    m_eventListeners.remove(IClassIdDocumentationListener.class, l);
  }

  private static INlsProject computeNlsProject(IType t) {
    if (TypeUtility.exists(t)) {
      IScoutBundle scoutBundle = ScoutTypeUtility.getScoutBundle(t);
      if (scoutBundle != null) {
        return scoutBundle.getDocsNlsProject();
      }
    }
    return null;
  }

  /**
   * Starts editing the type that belongs to this instance. Any UI interaction is executed on the given shell.
   *
   * @param shell
   */
  public void editDocumentation(Shell shell) {
    INlsEntry nlsEntry = getNlsEntry();
    if (nlsEntry == null) {
      createNew(shell);
    }
    else {
      editExisting(nlsEntry, shell);
    }
  }

  /**
   * Gets the documentation entry that belongs to the type of this instance.
   *
   * @return The documentation entry or null if there is no entry for the type.
   */
  public INlsEntry getNlsEntry() {
    INlsProject nlsProject = getNlsProject();
    if (nlsProject != null) {
      try {
        String classId = ScoutTypeUtility.getClassIdAnnotationValue(getType());
        if (StringUtility.hasText(classId)) {
          String key = generateNlsKey(classId);
          return nlsProject.getEntry(key);
        }
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logError("Unable to get the documentation text for type '" + getType().getFullyQualifiedName() + "'.", e);
      }
    }
    return null;
  }

  protected void editExisting(INlsEntry nlsEntry, Shell shell) {
    NlsEntryModifyAction action = new NlsEntryModifyAction(shell, nlsEntry, nlsEntry.getProject());
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
      fireModified(e, IClassIdDocumentationListener.TYPE_NLS_VALUE_EDITED);
    }
  }

  protected void createNew(Shell shell) {
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

    NlsEntryNewAction action = new NlsEntryNewAction(shell, getNlsProject(), entry, true);
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
        updateClassIdAnnotation(entry, classId, keyModified, shell);
      }
      else {
        fireModified(entry, IClassIdDocumentationListener.TYPE_NLS_VALUE_CREATED_EXISTING_CLASS_ID);
      }
    }
  }

  protected void fireModified(INlsEntry documentationEntry, int type) {
    for (IClassIdDocumentationListener l : m_eventListeners.getListeners(IClassIdDocumentationListener.class)) {
      if (l != null) {
        try {
          l.modified(type, documentationEntry, getType());
        }
        catch (Exception e) {
          ScoutSdkUi.logError("Error invoking class id documentation listener '" + l.getClass().getName() + "'.", e);
        }
      }
    }
  }

  protected void updateClassIdAnnotation(final NlsEntry entry, String classId, boolean keyModified, final Shell shell) {
    if (keyModified) {
      classId = entry.getKey();
    }
    IAnnotationSourceBuilder sourceBuilder = AnnotationSourceBuilderFactory.createClassIdAnnotation(classId);
    OperationJob j = new OperationJob(new AnnotationNewOperation(sourceBuilder, getType()));
    j.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        shell.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            fireModified(entry, IClassIdDocumentationListener.TYPE_NLS_VALUE_CREATED_NEW_CLASS_ID);
          }
        });
      }
    });
    j.schedule();
  }

  protected String generateNlsKey(String classId) {
    return getNlsProject().generateKey(classId);
  }

  /**
   * @return the type of this instance
   */
  public IType getType() {
    return m_type;
  }

  /**
   * Gets the {@link INlsProject} that belongs to the type of this instance.
   *
   * @return the nls project or null.
   */
  public INlsProject getNlsProject() {
    return m_project;
  }
}
