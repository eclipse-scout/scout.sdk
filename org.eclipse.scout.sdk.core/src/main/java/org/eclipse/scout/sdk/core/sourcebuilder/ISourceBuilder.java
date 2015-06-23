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
package org.eclipse.scout.sdk.core.sourcebuilder;

import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link ISourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public interface ISourceBuilder {

  /**
   * validates the receiver source builder.
   *
   * @throws
   */
  void validate();

  /**
   * appends the source to the given builder.
   *
   * @param source
   *          The builder to append the source to.
   * @param lineDelimiter
   *          the line delimiter to use.
   * @param context
   *          context information in which the source is being created.
   * @param validator
   *          the import validator to use.
   */
  void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator);

}
