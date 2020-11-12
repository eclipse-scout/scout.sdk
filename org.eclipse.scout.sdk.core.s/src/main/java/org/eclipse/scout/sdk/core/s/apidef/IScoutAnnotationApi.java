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
package org.eclipse.scout.sdk.core.s.apidef;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;

public interface IScoutAnnotationApi {
  ApplicationScoped ApplicationScoped();

  Authentication Authentication();

  BeanMock BeanMock();

  Before Before();

  ClassId ClassId();

  Clazz Clazz();

  ColumnData ColumnData();

  Data Data();

  DtoRelevant DtoRelevant();

  Extends Extends();

  FormData FormData();

  Handler Handler();

  Order Order();

  PageData PageData();

  Replace Replace();

  RunWith RunWith();

  RunWithClientSession RunWithClientSession();

  RunWithServerSession RunWithServerSession();

  RunWithSubject RunWithSubject();

  Test Test();

  TunnelToServer TunnelToServer();

  WebServiceEntryPoint WebServiceEntryPoint();

  NlsKey NlsKey();

  interface NlsKey extends IClassNameSupplier {
  }

  interface ApplicationScoped extends IClassNameSupplier {
  }

  interface Authentication extends IClassNameSupplier {

    String methodElementName();

    String verifierElementName();
  }

  interface BeanMock extends IClassNameSupplier {

  }

  interface Before extends IClassNameSupplier {

  }

  interface ClassId extends IClassNameSupplier {

    String valueElementName();
  }

  interface Clazz extends IClassNameSupplier {

    String valueElementName();

    String qualifiedNameElementName();
  }

  interface ColumnData extends IClassNameSupplier {

    String valueElementName();
  }

  interface Data extends IClassNameSupplier {

    String valueElementName();
  }

  interface DtoRelevant extends IClassNameSupplier {

  }

  interface Extends extends IClassNameSupplier {

    String valueElementName();

    String pathToContainerElementName();
  }

  interface FormData extends IClassNameSupplier {

    String valueElementName();

    String interfacesElementName();

    String genericOrdinalElementName();

    String defaultSubtypeSdkCommandElementName();

    String sdkCommandElementName();
  }

  interface Handler extends IClassNameSupplier {

    String valueElementName();
  }

  interface Order extends IClassNameSupplier {

    String valueElementName();
  }

  interface PageData extends IClassNameSupplier {

    String valueElementName();
  }

  interface Replace extends IClassNameSupplier {

  }

  interface RunWith extends IClassNameSupplier {

    String valueElementName();
  }

  interface RunWithClientSession extends IClassNameSupplier {

    String valueElementName();
  }

  interface RunWithServerSession extends IClassNameSupplier {

    String valueElementName();
  }

  interface RunWithSubject extends IClassNameSupplier {

    String valueElementName();
  }

  interface Test extends IClassNameSupplier {

  }

  interface TunnelToServer extends IClassNameSupplier {

  }

  interface WebServiceEntryPoint extends IClassNameSupplier {

    String endpointInterfaceElementName();

    String entryPointNameElementName();

    String serviceNameElementName();

    String portNameElementName();

    String entryPointPackageElementName();

    String authenticationElementName();

    String handlerChainElementName();
  }
}
