/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi;

/**
 * <h3>{@link ClasspathSpi}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public interface ClasspathSpi {

  boolean isSource();

  String getPath();

  String getEncoding();
}
