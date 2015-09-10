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

import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link IResourceFragmentBuilder}</h3>
 *
 * @author imo
 */
public interface IResourceFragmentBuilder {

  /**
   * appends the text to the given builder.
   *
   * @param source
   *          The builder to append the resource to.
   * @param lineDelimiter
   *          the line delimiter to use.
   * @param context
   *          context information in which the source is being created.
   */
  void createResource(StringBuilder source, String lineDelimiter, PropertyMap context);

}
