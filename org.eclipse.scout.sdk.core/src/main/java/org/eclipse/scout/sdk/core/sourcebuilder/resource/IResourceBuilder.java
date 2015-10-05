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
package org.eclipse.scout.sdk.core.sourcebuilder.resource;

import java.util.List;

/**
 * <h3>{@link IResourceBuilder}</h3>
 *
 * @author Ivan Motsch
 */
public interface IResourceBuilder extends IResourceFragmentBuilder {

  String getPackageName();

  String getFileName();

  void addFragment(IResourceFragmentBuilder fragment);

  List<IResourceFragmentBuilder> getFragments();

  /**
   * Add an error message that is appended to the end of the compilation unit as triple-X comment
   * <p>
   * Typically this is a code generation error or semantic check issue
   *
   * @param taskType
   *          such as uppercase of todo, fixme, ...
   * @param msg
   * @param exceptions
   */
  void addErrorMessage(String taskType, String msg, Throwable... exceptions);
}
