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
package org.eclipse.scout.sdk.ws.jaxws.executor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

/**
 * <h3>{@link SessionFactoryNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class SessionFactoryNewExecutor extends AbstractTypeNewExecutor {

  @Override
  protected void init(IScoutBundle bundle) {
    setPackageFragment(JaxWsSdkUtility.getRecommendedSessionPackageName(bundle), true);
    List<String> interfaceTypeSignatures = new ArrayList<>();
    interfaceTypeSignatures.add(SignatureCache.createTypeSignature(TypeUtility.getType(JaxWsRuntimeClasses.IServerSessionFactory).getFullyQualifiedName()));
    setInterfaceTypeSignatures(interfaceTypeSignatures, false);
    setSuperTypeSignature(null, false);
    setTypeName("ServerSessionFactory");
  }
}
