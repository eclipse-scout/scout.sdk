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
package org.eclipse.scout.sdk.core.util.apidef;

import java.util.Optional;

public interface IApiSpecification {

  ApiVersion level();

  Optional<ApiVersion> version();

  <A extends IApiSpecification> Optional<A> optApi(Class<A> apiDefinition);

  <A extends IApiSpecification> A api(Class<A> apiDefinition);
}
