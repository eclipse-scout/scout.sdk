/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.builder.java.body;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;

/**
 * <h3>{@link IScoutMethodBodyBuilder}</h3>
 *
 * @since 6.1.0
 */
public interface IScoutMethodBodyBuilder<TYPE extends IScoutMethodBodyBuilder<TYPE>> extends IMethodBodyBuilder<TYPE> {

  TYPE appendBeansGetVariable(CharSequence bean, CharSequence varName);

  TYPE appendExportFormData(CharSequence formDataVarName);

  TYPE appendImportFormData(CharSequence formDataVarName);

  TYPE appendBeansGet(CharSequence bean);

  TYPE appendGetFieldByClass(CharSequence fieldFqn);

  TYPE appendGetPropertyByClass(CharSequence propName);

  TYPE appendTextsGet(CharSequence textKey);

  TYPE appendPermissionCheck(CharSequence permission);

}
