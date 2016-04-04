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
package org.eclipse.scout.sdk.core.sourcebuilder.comment;

import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link ICommentSourceBuilderSpi}</h3> The workspace delegate that creates default comments on types,
 * methods, fields, ...
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-07-12
 */
public interface ICommentSourceBuilderSpi {

  ISourceBuilder createCompilationUnitComment(ICompilationUnitSourceBuilder target);

  ISourceBuilder createTypeComment(ITypeSourceBuilder target);

  ISourceBuilder createMethodComment(IMethodSourceBuilder target);

  ISourceBuilder createOverrideMethodComment(IMethodSourceBuilder target, final String interfaceFqn);

  ISourceBuilder createGetterMethodComment(IMethodSourceBuilder target);

  ISourceBuilder createSetterMethodComment(IMethodSourceBuilder target);

  ISourceBuilder createFieldComment(IFieldSourceBuilder target);
}
