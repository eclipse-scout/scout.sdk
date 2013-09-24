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
package org.eclipse.scout.sdk.rap.var;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableListener;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;

/**
 * <h3>{@link RapTargetVariable}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 14.01.2013
 */
public final class RapTargetVariable {

  public final static String RAP_TARGET_KEY = "scout_rap_target";

  private final static RapTargetVariable instance = new RapTargetVariable();

  private final EventListenerList m_listeners;
  private final P_TargetFileListener m_targetFileListener;

  private final IValueVariable m_scoutRapTargetVariable;
  private final IRapTargetVariableListener m_valueChangeListener;
  private final IValueVariableListener m_variableListener;

  private String m_oldVal;

  private RapTargetVariable() {
    m_listeners = new EventListenerList();
    m_scoutRapTargetVariable = VariablesPlugin.getDefault().getStringVariableManager().getValueVariable(RAP_TARGET_KEY);
    m_oldVal = null;
    m_targetFileListener = new P_TargetFileListener();
    m_valueChangeListener = new RapTargetVariableListenerAdapter() {
      @Override
      public void valueChanged(String oldVal, String newVal) {
        refreshResourceListener();
      }
    };
    m_variableListener = new P_VariableListener();
  }

  private void refreshResourceListener() {
    if (getValue() == null) {
      // no value: register listener that waits for target files using the variable
      ResourcesPlugin.getWorkspace().addResourceChangeListener(m_targetFileListener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_REFRESH);
    }
    else {
      // variable has a value: no need to listen anymore: remove
      ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_targetFileListener);
    }
  }

  public void start() {
    addListener(m_valueChangeListener);
    VariablesPlugin.getDefault().getStringVariableManager().addValueVariableListener(m_variableListener);
    refreshResourceListener();
  }

  public void stop() {
    removeListener(m_valueChangeListener);
    VariablesPlugin.getDefault().getStringVariableManager().removeValueVariableListener(m_variableListener);
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_targetFileListener);
  }

  public void addListener(IRapTargetVariableListener listener) {
    m_listeners.add(IRapTargetVariableListener.class, listener);
  }

  public void removeListener(IRapTargetVariableListener listener) {
    m_listeners.remove(IRapTargetVariableListener.class, listener);
  }

  private void fireValueChanged(String oldVal, String newVal) {
    for (IRapTargetVariableListener listener : m_listeners.getListeners(IRapTargetVariableListener.class)) {
      try {
        listener.valueChanged(oldVal, newVal);
      }
      catch (Exception e) {
        ScoutSdkRap.logWarning("RAP Target Variable Listener could not be completed.", e);
      }
    }
  }

  private void fireEmptyVariableInUse(IFile targetFile) {
    for (IRapTargetVariableListener listener : m_listeners.getListeners(IRapTargetVariableListener.class)) {
      try {
        listener.emptyVariableInUse(targetFile);
      }
      catch (Exception e) {
        ScoutSdkRap.logWarning("RAP Target Variable Listener could not be completed.", e);
      }
    }
  }

  /**
   * Sets a new value for the scout_rap_target variable.<br>
   * The new value is directly propagated to the environment variable.
   * 
   * @param val
   *          The new value
   */
  public void setValue(String val) {
    setValue(val, false);
  }

  void initialize(String val) {
    setValue(val, true);
  }

  private synchronized void setValue(String val, boolean isInitialSet) {
    String oldVal = isInitialSet ? null : m_scoutRapTargetVariable.getValue(); // not possible to call getValue() when we are initializing
    String newVal = StringUtility.hasText(val) ? val.trim() : null;

    if (CompareUtility.notEquals(oldVal, newVal)) {
      m_scoutRapTargetVariable.setValue(newVal);
    }
  }

  /**
   * gets the current value of the scout_rap_target variable
   * 
   * @return
   */
  public synchronized String getValue() {
    return m_scoutRapTargetVariable.getValue();
  }

  public static RapTargetVariable get() {
    return instance;
  }

  private class P_TargetFileListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      if (getValue() == null) {
        HashSet<IFile> collector = new HashSet<IFile>();
        try {
          collectTargetFiles(event.getDelta(), collector);
          if (!collector.isEmpty()) {
            for (IFile f : collector) {
              try {
                if (isScoutRapTargetVarPresent(f)) {
                  fireEmptyVariableInUse(f);
                  break;
                }
              }
              catch (CoreException e) {
                ScoutSdkRap.logWarning(e);
              }
            }
          }
        }
        catch (CoreException e) {
          ScoutSdkRap.logWarning(e);
        }
      }
    }

    private boolean isScoutRapTargetVarPresent(IFile r) throws CoreException {
      String s = ResourceUtility.getContent(r);
      return s.contains(RAP_TARGET_KEY);
    }

    private void collectTargetFiles(IResourceDelta delta, Set<IFile> res) throws CoreException {
      if (delta == null) {
        return;
      }

      for (IResource r : ResourceUtility.getAllResources(delta.getResource(), ResourceFilters.getTargetFileFilter())) {
        res.add((IFile) r);
      }
    }
  }

  private class P_VariableListener implements IValueVariableListener {
    @Override
    public void variablesRemoved(IValueVariable[] variables) {
    }

    @Override
    public void variablesChanged(IValueVariable[] variables) {
      for (IValueVariable var : variables) {
        if (RAP_TARGET_KEY.equals(var.getName())) {
          String newVal = var.getValue();
          fireValueChanged(m_oldVal, newVal);
          m_oldVal = newVal;
        }
      }
    }

    @Override
    public void variablesAdded(IValueVariable[] variables) {
    }
  }
}
