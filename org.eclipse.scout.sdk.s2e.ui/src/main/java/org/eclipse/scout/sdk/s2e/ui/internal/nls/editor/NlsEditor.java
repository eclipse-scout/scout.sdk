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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.ITranslationManagerListener;
import org.eclipse.scout.sdk.core.s.nls.Translations;
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.EditorPart;

/**
 * <h3>{@link NlsEditor}</h3>
 *
 * @since 7.0.0
 */
public class NlsEditor extends EditorPart {

  public static final String EDITOR_ID = "org.eclipse.scout.sdk.s2e.ui.nlsEditor";

  private Path m_path;
  private NlsTablePage m_nlsTablePage;

  private ITranslationManagerListener m_listener;

  @Override
  public void init(IEditorSite site, IEditorInput input) {
    setInput(input);
    setSite(site);

    var resource = input.getAdapter(IResource.class);
    if (resource != null && resource.exists()) {
      m_path = resource.getProject().getLocation().toFile().toPath();
    }
  }

  @Override
  public boolean isDirty() {
    if (m_nlsTablePage == null) {
      return false;
    }
    return m_nlsTablePage.translationManager().isDirty();
  }

  @Override
  public void createPartControl(Composite parent) {
    setPartName("Translations");
    if (path() == null) {
      return;
    }

    // open monitor async so that the IDE startup is not blocked or slowed down because the NLS editor is starting
    parent.getDisplay().asyncExec(() -> showMonitorDialogAndLoadContentAsync(parent));
  }

  protected void showMonitorDialogAndLoadContentAsync(Composite parent) {
    try {
      new ProgressMonitorDialog(parent.getShell()).run(true, true, monitor -> loadAndCreateContentAsync(parent, path(), monitor));
    }
    catch (InvocationTargetException | InterruptedException e) {
      SdkLog.error("Error while loading the translations for path '{}'.", path(), e);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    if (m_nlsTablePage != null && m_listener != null) {
      m_nlsTablePage.translationManager().removeListener(m_listener);
    }
  }

  private void loadAndCreateContentAsync(Composite parent, Path modulePath, IProgressMonitor monitor) {
    runInEclipseEnvironment((e, p /* do not use this monitor in here. instead use the monitor from the ProgressMonitorDialog */) -> {
      var progress = toScoutProgress(monitor);
      progress.init(1000, "Loading translation editor...");

      try {
        Translations.createManager(modulePath, e, progress.newChild(1000), DependencyScope.ALL)
            .ifPresent(manager -> {
              progress.monitor().setTaskName("Creating table...");
              parent.getDisplay().syncExec(() -> createPageAsync(manager, parent));
            });
      }
      catch (OperationCanceledException ex) {
        SdkLog.debug("Translation manager creation has been canceled.", ex);
      }
      catch (RuntimeException ex) {
        SdkLog.error("Error loading translation manager.", ex);
      }
    }).awaitDoneThrowingOnErrorOrCancel();
  }

  private void createPageAsync(TranslationManager manager, Composite parent) {
    parent.setRedraw(false);
    try {
      m_nlsTablePage = new NlsTablePage(parent, manager);
      m_listener = events -> parent.getDisplay().asyncExec(() -> firePropertyChange(PROP_DIRTY));
      manager.addListener(m_listener);
    }
    finally {
      parent.setRedraw(true);
    }
  }

  public Path path() {
    return m_path;
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    if (m_nlsTablePage == null) {
      return;
    }

    runInEclipseEnvironment((env, progress) -> m_nlsTablePage.translationManager().flush(env, progress));
  }

  @Override
  public void doSaveAs() {
    // nop
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void setFocus() {
    // nop
  }
}
