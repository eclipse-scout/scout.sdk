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
package org.eclipse.scout.sdk.jobs;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * <h3>{@link OptionalWorkspaceBlockingRule}</h3>Scheduling rule that always conflicts instances of itself and optionally
 * the full workspace.
 * 
 * @author Matthias Villiger
 * @since 4.1.0 04.06.2014
 */
public class OptionalWorkspaceBlockingRule implements ISchedulingRule {

  private final boolean m_blockWithWorkspaceResources;

  public OptionalWorkspaceBlockingRule(boolean blockWithWorkspaceResources) {
    m_blockWithWorkspaceResources = blockWithWorkspaceResources;
  }

  @Override
  public boolean contains(ISchedulingRule rule) {
    if (rule == this) {
      return true;
    }
    if (rule instanceof OptionalWorkspaceBlockingRule) {
      return false;
    }
    if (m_blockWithWorkspaceResources) {
      return ResourcesPlugin.getWorkspace().getRoot().contains(rule);
    }
    return false;
  }

  @Override
  public boolean isConflicting(ISchedulingRule rule) {
    if (rule == this) {
      return true;
    }
    if (rule instanceof OptionalWorkspaceBlockingRule) {
      return true;
    }
    if (m_blockWithWorkspaceResources) {
      return ResourcesPlugin.getWorkspace().getRoot().isConflicting(rule);
    }
    else {
      return false;
    }
  }
}
