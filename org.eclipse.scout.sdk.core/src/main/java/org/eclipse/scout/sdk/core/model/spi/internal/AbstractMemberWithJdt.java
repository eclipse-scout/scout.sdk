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
package org.eclipse.scout.sdk.core.model.spi.internal;

import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;

public abstract class AbstractMemberWithJdt<API extends IMember> extends AbstractJavaElementWithJdt<API>implements MemberSpi {

  AbstractMemberWithJdt(JavaEnvironmentWithJdt env) {
    super(env);
  }

}
