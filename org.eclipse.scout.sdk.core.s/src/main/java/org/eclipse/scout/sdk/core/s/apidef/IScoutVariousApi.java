/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.apidef;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;

@SuppressWarnings({"squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // method naming conventions
public interface IScoutVariousApi {

  CalendarMenuType CalendarMenuType();

  interface CalendarMenuType extends IClassNameSupplier {
    String CalendarComponent();

    String EmptySpace();
  }

  ImageFieldMenuType ImageFieldMenuType();

  interface ImageFieldMenuType extends IClassNameSupplier {
    String Null();

    String ImageId();

    String ImageUrl();

    String Image();
  }

  TabBoxMenuType TabBoxMenuType();

  interface TabBoxMenuType extends IClassNameSupplier {
    String Header();
  }

  TableMenuType TableMenuType();

  interface TableMenuType extends IClassNameSupplier {
    String EmptySpace();

    String Header();

    String MultiSelection();

    String SingleSelection();
  }

  TileGridMenuType TileGridMenuType();

  interface TileGridMenuType extends IClassNameSupplier {
    String EmptySpace();

    String SingleSelection();

    String MultiSelection();
  }

  TreeMenuType TreeMenuType();

  interface TreeMenuType extends IClassNameSupplier {
    String EmptySpace();

    String MultiSelection();

    String SingleSelection();

    String Header();
  }

  ValueFieldMenuType ValueFieldMenuType();

  interface ValueFieldMenuType extends IClassNameSupplier {
    String NotNull();

    String Null();
  }

  WebService WebService();

  interface WebService extends IClassNameSupplier {
    String nameElementName();

    String targetNamespaceElementName();
  }

  WebServiceClient WebServiceClient();

  interface WebServiceClient extends IClassNameSupplier {
    String nameElementName();

    String targetNamespaceElementName();
  }

  ACCESS ACCESS();

  interface ACCESS extends IClassNameSupplier {
    String checkMethodName();

    String checkAndThrowMethodName();
  }

  BEANS BEANS();

  interface BEANS extends IClassNameSupplier {
    String getMethodName();
  }

  BasicAuthenticationMethod BasicAuthenticationMethod();

  interface BasicAuthenticationMethod extends IClassNameSupplier {
  }

  BinaryResource BinaryResource();

  interface BinaryResource extends IClassNameSupplier {
  }

  BooleanUtility BooleanUtility();

  interface BooleanUtility extends IClassNameSupplier {
  }

  ClientTestRunner ClientTestRunner();

  interface ClientTestRunner extends IClassNameSupplier {
  }

  CollectionUtility CollectionUtility();

  interface CollectionUtility extends IClassNameSupplier {
    String hashSetMethodName();

    String hashSetWithoutNullElementsMethodName();
  }

  ConfigFileCredentialVerifier ConfigFileCredentialVerifier();

  interface ConfigFileCredentialVerifier extends IClassNameSupplier {
  }

  LogHandler LogHandler();

  interface LogHandler extends IClassNameSupplier {
  }

  LookupCall LookupCall();

  interface LookupCall extends IClassNameSupplier {
    String getConfiguredServiceMethodName();

    String getDataByAllMethodName();

    String getDataByKeyMethodName();

    String getDataByTextMethodName();
  }

  NullClazz NullClazz();

  interface NullClazz extends IClassNameSupplier {
  }

  SearchFilter SearchFilter();

  interface SearchFilter extends IClassNameSupplier {
    String getFormDataMethodName();
  }

  ServerTestRunner ServerTestRunner();

  interface ServerTestRunner extends IClassNameSupplier {
  }

  TEXTS TEXTS();

  interface TEXTS extends IClassNameSupplier {
    String getMethodName();

    String getWithFallbackMethodName();
  }

  TestEnvironmentClientSession TestEnvironmentClientSession();

  interface TestEnvironmentClientSession extends IClassNameSupplier {
  }

  TriState TriState();

  interface TriState extends IClassNameSupplier {
  }

  UiServlet UiServlet();

  interface UiServlet extends IClassNameSupplier {
  }

  UiTextContributor UiTextContributor();

  interface UiTextContributor extends IClassNameSupplier {
    String contributeUiTextKeysMethodName();
  }

  VetoException VetoException();

  interface VetoException extends IClassNameSupplier {
  }

  WsConsumerCorrelationIdHandler WsConsumerCorrelationIdHandler();

  interface WsConsumerCorrelationIdHandler extends IClassNameSupplier {
  }

  WsProviderCorrelationIdHandler WsProviderCorrelationIdHandler();

  interface WsProviderCorrelationIdHandler extends IClassNameSupplier {
  }

  Logger Logger();

  interface Logger extends IClassNameSupplier {
  }

  Mockito Mockito();

  interface Mockito extends IClassNameSupplier {
    String whenMethodName();
  }

  ArgumentMatchers ArgumentMatchers();

  interface ArgumentMatchers extends IClassNameSupplier {
    String anyMethodName();
  }

  ColumnSet ColumnSet();

  interface ColumnSet extends IClassNameSupplier {
    String getColumnByClassMethodName();
  }

  JaxWsConstants JaxWsConstants();

  interface JaxWsConstants {
    String mavenPluginGroupId();

    String codeModelFactoryPath();

    String servletFactoryPath();

    String slf4jFactoryPath();

    String jwsFactoryPath();
  }

  DoValue DoValue();

  interface DoValue extends IClassNameSupplier {
  }

  DoList DoList();

  interface DoList extends IClassNameSupplier {
    String updateAllMethodName();
  }

  DoNode DoNode();

  interface DoNode extends IClassNameSupplier {
    int valueTypeParamIndex();

    String getMethodName();

    String setMethodName();
  }

  MessageBoxes MessageBoxes();

  interface MessageBoxes extends IClassNameSupplier {
    String createOkMethodName();

    String showDeleteConfirmationMessageMethodName();
  }
}
