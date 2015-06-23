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
package org.eclipse.scout.sdk.core.sourcebuilder.comment;

import org.eclipse.scout.sdk.core.sourcebuilder.ICommentSourceBuilder;

/**
 * <h3>{@link IJavaElementCommentBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 12.07.2013
 */
public interface IJavaElementCommentBuilder {

  ICommentSourceBuilder createPreferencesCompilationUnitCommentBuilder();

  ICommentSourceBuilder createPreferencesMethodOverrideComment(final String interfaceFqn);

  ICommentSourceBuilder createPreferencesTypeCommentBuilder();

  ICommentSourceBuilder createPreferencesMethodCommentBuilder();

  ICommentSourceBuilder createPreferencesFieldCommentBuilder();

  ICommentSourceBuilder createPreferencesMethodGetterCommentBuilder();

  ICommentSourceBuilder createPreferencesMethodSetterCommentBuilder();

}
