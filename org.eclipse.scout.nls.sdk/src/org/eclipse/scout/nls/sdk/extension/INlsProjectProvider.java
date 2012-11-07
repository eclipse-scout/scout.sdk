package org.eclipse.scout.nls.sdk.extension;

import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * Defines a provider that is capable to create <code>INlsProject</code> hierarchies for implementation specific input
 * parameters.
 */
public interface INlsProjectProvider {
  /**
   * Return the <code>INlsProject</code> hierarchy for the given parameters or null if the implementation does not
   * understand the given parameters.
   * 
   * @param args
   * @return The <code>INlsProject</code> hierarchy.
   */
  INlsProject getProject(Object[] args);
}
