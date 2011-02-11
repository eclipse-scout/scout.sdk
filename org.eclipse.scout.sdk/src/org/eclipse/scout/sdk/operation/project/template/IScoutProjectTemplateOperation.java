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
package org.eclipse.scout.sdk.operation.project.template;

import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link IScoutProjectTemplateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2011
 */
public interface IScoutProjectTemplateOperation extends IOperation {

  String getTemplateName();

  String getDescription();

  /**
   * @param scoutProject
   */
  void setScoutProject(IScoutProject scoutProject);
}
