/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class SessionFactoryNewAction extends AbstractTypeNewAction {

  public SessionFactoryNewAction() {
    super(Texts.get("ServerSessionFactory"));
  }

  @Override
  public void init(IScoutBundle bundle) {
    super.init(bundle);
    setPackageFragment(JaxWsSdkUtility.getRecommendedSessionPackageName(bundle), true);
    List<String> interfaceTypeSignatures = new ArrayList<String>();
    interfaceTypeSignatures.add(SignatureCache.createTypeSignature(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory).getFullyQualifiedName()));
    setInterfaceTypeSignatures(interfaceTypeSignatures, false);
    setSuperTypeSignature(null, false);
    setTypeName("ServerSessionFactory");
  }
}
