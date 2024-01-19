/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;

@SuppressWarnings({"squid:S00100", "squid:S2166", "squid:S2176", "squid:S00118", "findbugs:NM_METHOD_NAMING_CONVENTION"}) // method naming conventions
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

  IgnoreConvenienceMethodGeneration IgnoreConvenienceMethodGeneration();

  TypeVersion TypeVersion();

  TypeName TypeName();

  ValueFormat ValueFormat();

  AttributeName AttributeName();

  interface NlsKey extends ITypeNameSupplier {
  }

  interface ApplicationScoped extends ITypeNameSupplier {
  }

  interface Authentication extends ITypeNameSupplier {
    String methodElementName();

    String verifierElementName();
  }

  interface BeanMock extends ITypeNameSupplier {
  }

  interface Before extends ITypeNameSupplier {
  }

  interface ClassId extends ITypeNameSupplier {
    String valueElementName();
  }

  interface Clazz extends ITypeNameSupplier {
    String valueElementName();

    String qualifiedNameElementName();
  }

  interface ColumnData extends ITypeNameSupplier {
    String valueElementName();
  }

  interface Data extends ITypeNameSupplier {
    String valueElementName();
  }

  interface DtoRelevant extends ITypeNameSupplier {

  }

  interface Extends extends ITypeNameSupplier {
    String valueElementName();

    String pathToContainerElementName();
  }

  interface FormData extends ITypeNameSupplier {
    String valueElementName();

    String interfacesElementName();

    String genericOrdinalElementName();

    String defaultSubtypeSdkCommandElementName();

    String sdkCommandElementName();
  }

  interface Handler extends ITypeNameSupplier {
    String valueElementName();
  }

  interface Order extends ITypeNameSupplier {
    String valueElementName();
  }

  interface PageData extends ITypeNameSupplier {
    String valueElementName();
  }

  interface Replace extends ITypeNameSupplier {
  }

  interface RunWith extends ITypeNameSupplier {
    String valueElementName();
  }

  interface RunWithClientSession extends ITypeNameSupplier {
    String valueElementName();
  }

  interface RunWithServerSession extends ITypeNameSupplier {
    String valueElementName();
  }

  interface RunWithSubject extends ITypeNameSupplier {
    String valueElementName();
  }

  interface Test extends ITypeNameSupplier {
  }

  interface TunnelToServer extends ITypeNameSupplier {
  }

  interface WebServiceEntryPoint extends ITypeNameSupplier {
    String endpointInterfaceElementName();

    String entryPointNameElementName();

    String serviceNameElementName();

    String portNameElementName();

    String entryPointPackageElementName();

    String authenticationElementName();

    String handlerChainElementName();
  }

  interface IgnoreConvenienceMethodGeneration extends ITypeNameSupplier {
  }

  interface TypeVersion extends ITypeNameSupplier {
    String valueElementName();

    void buildValue(IExpressionBuilder<?> builder, String typeVersion);
  }

  interface TypeName extends ITypeNameSupplier {
    String valueElementName();
  }

  interface ValueFormat extends ITypeNameSupplier {
    String patternElementName();
  }

  interface AttributeName extends ITypeNameSupplier {
    String valueElementName();
  }

  Generated Generated();

  interface Generated extends ITypeNameSupplier {

    String valueElementName();

    String dateElementName();

    String commentsElementName();
  }
}
