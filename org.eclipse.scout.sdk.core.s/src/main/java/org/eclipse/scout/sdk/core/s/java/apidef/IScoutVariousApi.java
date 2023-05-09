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

@SuppressWarnings({"squid:S00100", "squid:S2166", "squid:S2176", "squid:S00118", "findbugs:NM_METHOD_NAMING_CONVENTION"}) // method naming conventions
public interface IScoutVariousApi {

  ScoutTextProviderService ScoutTextProviderService();

  interface ScoutTextProviderService extends ITypeNameSupplier {
  }

  CalendarMenuType CalendarMenuType();

  interface CalendarMenuType extends ITypeNameSupplier {
    String CalendarComponent();

    String EmptySpace();
  }

  ImageFieldMenuType ImageFieldMenuType();

  interface ImageFieldMenuType extends ITypeNameSupplier {
    String Null();

    String ImageId();

    String ImageUrl();

    String Image();
  }

  TabBoxMenuType TabBoxMenuType();

  interface TabBoxMenuType extends ITypeNameSupplier {
    String Header();
  }

  TableMenuType TableMenuType();

  interface TableMenuType extends ITypeNameSupplier {
    String EmptySpace();

    String Header();

    String MultiSelection();

    String SingleSelection();
  }

  TileGridMenuType TileGridMenuType();

  interface TileGridMenuType extends ITypeNameSupplier {
    String EmptySpace();

    String SingleSelection();

    String MultiSelection();
  }

  TreeMenuType TreeMenuType();

  interface TreeMenuType extends ITypeNameSupplier {
    String EmptySpace();

    String MultiSelection();

    String SingleSelection();

    String Header();
  }

  ValueFieldMenuType ValueFieldMenuType();

  interface ValueFieldMenuType extends ITypeNameSupplier {
    String NotNull();

    String Null();
  }

  WebService WebService();

  interface WebService extends ITypeNameSupplier {
    String nameElementName();

    String targetNamespaceElementName();
  }

  WebServiceClient WebServiceClient();

  interface WebServiceClient extends ITypeNameSupplier {
    String nameElementName();

    String targetNamespaceElementName();
  }

  ACCESS ACCESS();

  interface ACCESS extends ITypeNameSupplier {
    String checkMethodName();

    String checkAndThrowMethodName();
  }

  BEANS BEANS();

  interface BEANS extends ITypeNameSupplier {
    String getMethodName();
  }

  BasicAuthenticationMethod BasicAuthenticationMethod();

  interface BasicAuthenticationMethod extends ITypeNameSupplier {
  }

  BinaryResource BinaryResource();

  interface BinaryResource extends ITypeNameSupplier {
  }

  BooleanUtility BooleanUtility();

  interface BooleanUtility extends ITypeNameSupplier {
  }

  ClientTestRunner ClientTestRunner();

  interface ClientTestRunner extends ITypeNameSupplier {
  }

  CollectionUtility CollectionUtility();

  interface CollectionUtility extends ITypeNameSupplier {
    String hashSetMethodName();

    String hashSetWithoutNullElementsMethodName();
  }

  ConfigFileCredentialVerifier ConfigFileCredentialVerifier();

  interface ConfigFileCredentialVerifier extends ITypeNameSupplier {
  }

  LogHandler LogHandler();

  interface LogHandler extends ITypeNameSupplier {
  }

  LookupCall LookupCall();

  interface LookupCall extends ITypeNameSupplier {
    String getConfiguredServiceMethodName();

    String getDataByAllMethodName();

    String getDataByKeyMethodName();

    String getDataByTextMethodName();
  }

  NullClazz NullClazz();

  interface NullClazz extends ITypeNameSupplier {
  }

  SearchFilter SearchFilter();

  interface SearchFilter extends ITypeNameSupplier {
    String getFormDataMethodName();
  }

  ServerTestRunner ServerTestRunner();

  interface ServerTestRunner extends ITypeNameSupplier {
  }

  TEXTS TEXTS();

  interface TEXTS extends ITypeNameSupplier {
    String getMethodName();

    String getWithFallbackMethodName();
  }

  TestEnvironmentClientSession TestEnvironmentClientSession();

  interface TestEnvironmentClientSession extends ITypeNameSupplier {
  }

  TriState TriState();

  interface TriState extends ITypeNameSupplier {
  }

  UiServlet UiServlet();

  interface UiServlet extends ITypeNameSupplier {
  }

  UiTextContributor UiTextContributor();

  interface UiTextContributor extends ITypeNameSupplier {
    String contributeUiTextKeysMethodName();
  }

  VetoException VetoException();

  interface VetoException extends ITypeNameSupplier {
  }

  WsConsumerCorrelationIdHandler WsConsumerCorrelationIdHandler();

  interface WsConsumerCorrelationIdHandler extends ITypeNameSupplier {
  }

  WsProviderCorrelationIdHandler WsProviderCorrelationIdHandler();

  interface WsProviderCorrelationIdHandler extends ITypeNameSupplier {
  }

  Logger Logger();

  interface Logger extends ITypeNameSupplier {
  }

  Mockito Mockito();

  interface Mockito extends ITypeNameSupplier {
    String whenMethodName();
  }

  ArgumentMatchers ArgumentMatchers();

  interface ArgumentMatchers extends ITypeNameSupplier {
    String anyMethodName();
  }

  ColumnSet ColumnSet();

  interface ColumnSet extends ITypeNameSupplier {
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

  interface DoValue extends ITypeNameSupplier {
  }

  DoList DoList();

  interface DoList extends ITypeNameSupplier {
  }

  String DoUpdateAllMethodName();

  DoNode DoNode();

  interface DoNode extends ITypeNameSupplier {
    int valueTypeParamIndex();

    String getMethodName();

    String setMethodName();
  }

  MessageBoxes MessageBoxes();

  interface MessageBoxes extends ITypeNameSupplier {
    String createOkMethodName();

    String showDeleteConfirmationMessageMethodName();
  }
}
