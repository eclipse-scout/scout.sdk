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
   * Set any source to the end of the {@link IResourceBuilder} after the last resource fragment.
   *
   * @param builder
   *          The source to add.
   */
  void setFooter(IResourceFragmentBuilder builder);
}
