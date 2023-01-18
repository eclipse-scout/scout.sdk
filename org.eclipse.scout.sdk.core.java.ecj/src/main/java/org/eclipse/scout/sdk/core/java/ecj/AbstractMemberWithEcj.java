/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import org.eclipse.scout.sdk.core.java.model.api.IMember;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.MemberSpi;

public abstract class AbstractMemberWithEcj<API extends IMember> extends AbstractJavaElementWithEcj<API> implements MemberSpi {

  protected AbstractMemberWithEcj(AbstractJavaEnvironment env) {
    super(env);
  }
}
