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

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.java.generator.method.IScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

@MaxApiLevel(10)
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout10Api extends IScoutApi {

  @Override
  default int[] supportedJavaVersions() {
    return new int[]{8, 11};
  }

  IScoutAnnotationApi.ApplicationScoped APPLICATION_SCOPED_ANNOTATION = new ApplicationScoped();

  @Override
  default IScoutAnnotationApi.ApplicationScoped ApplicationScoped() {
    return APPLICATION_SCOPED_ANNOTATION;
  }

  class ApplicationScoped implements IScoutAnnotationApi.ApplicationScoped {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.ApplicationScoped";
    }
  }

  IScoutAnnotationApi.Authentication AUTHENTICATION_ANNOTATION = new Authentication();

  @Override
  default IScoutAnnotationApi.Authentication Authentication() {
    return AUTHENTICATION_ANNOTATION;
  }

  class Authentication implements IScoutAnnotationApi.Authentication {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.provider.annotation.Authentication";
    }

    @Override
    public String methodElementName() {
      return "method";
    }

    @Override
    public String verifierElementName() {
      return "verifier";
    }
  }

  IScoutAnnotationApi.BeanMock BEAN_MOCK_ANNOTATION = new BeanMock();

  @Override
  default IScoutAnnotationApi.BeanMock BeanMock() {
    return BEAN_MOCK_ANNOTATION;
  }

  class BeanMock implements IScoutAnnotationApi.BeanMock {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.platform.mock.BeanMock";
    }
  }

  IScoutAnnotationApi.Before BEFORE_ANNOTATION = new Before();

  @Override
  default IScoutAnnotationApi.Before Before() {
    return BEFORE_ANNOTATION;
  }

  class Before implements IScoutAnnotationApi.Before {
    @Override
    public String fqn() {
      return "org.junit.Before";
    }
  }

  IScoutAnnotationApi.ClassId CLASS_ID_ANNOTATION = new ClassId();

  @Override
  default IScoutAnnotationApi.ClassId ClassId() {
    return CLASS_ID_ANNOTATION;
  }

  class ClassId implements IScoutAnnotationApi.ClassId {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.classid.ClassId";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.Clazz CLAZZ_ANNOTATION = new Clazz();

  @Override
  default IScoutAnnotationApi.Clazz Clazz() {
    return CLAZZ_ANNOTATION;
  }

  class Clazz implements IScoutAnnotationApi.Clazz {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz";
    }

    @Override
    public String valueElementName() {
      return "value";
    }

    @Override
    public String qualifiedNameElementName() {
      return "qualifiedName";
    }
  }

  IScoutAnnotationApi.ColumnData COLUMN_DATA_ANNOTATION = new ColumnData();

  @Override
  default IScoutAnnotationApi.ColumnData ColumnData() {
    return COLUMN_DATA_ANNOTATION;
  }

  class ColumnData implements IScoutAnnotationApi.ColumnData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.dto.ColumnData";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.Data DATA_ANNOTATION = new Data();

  @Override
  default IScoutAnnotationApi.Data Data() {
    return DATA_ANNOTATION;
  }

  class Data implements IScoutAnnotationApi.Data {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.dto.Data";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.DtoRelevant DTO_RELEVANT_ANNOTATION = new DtoRelevant();

  @Override
  default IScoutAnnotationApi.DtoRelevant DtoRelevant() {
    return DTO_RELEVANT_ANNOTATION;
  }

  class DtoRelevant implements IScoutAnnotationApi.DtoRelevant {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.annotations.DtoRelevant";
    }
  }

  IScoutAnnotationApi.Extends EXTENDS_ANNOTATION = new Extends();

  @Override
  default IScoutAnnotationApi.Extends Extends() {
    return EXTENDS_ANNOTATION;
  }

  class Extends implements IScoutAnnotationApi.Extends {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.extension.Extends";
    }

    @Override
    public String valueElementName() {
      return "value";
    }

    @Override
    public String pathToContainerElementName() {
      return "pathToContainer";
    }
  }

  IScoutAnnotationApi.FormData FORMDATA_ANNOTATION = new FormData();

  @Override
  default IScoutAnnotationApi.FormData FormData() {
    return FORMDATA_ANNOTATION;
  }

  class FormData implements IScoutAnnotationApi.FormData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.dto.FormData";
    }

    @Override
    public String valueElementName() {
      return "value";
    }

    @Override
    public String interfacesElementName() {
      return "interfaces";
    }

    @Override
    public String genericOrdinalElementName() {
      return "genericOrdinal";
    }

    @Override
    public String defaultSubtypeSdkCommandElementName() {
      return "defaultSubtypeSdkCommand";
    }

    @Override
    public String sdkCommandElementName() {
      return "sdkCommand";
    }
  }

  IScoutAnnotationApi.Handler HANDLER_ANNOTATION = new Handler();

  @Override
  default IScoutAnnotationApi.Handler Handler() {
    return HANDLER_ANNOTATION;
  }

  class Handler implements IScoutAnnotationApi.Handler {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.provider.annotation.Handler";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.Order ORDER_ANNOTATION = new Order();

  @Override
  default IScoutAnnotationApi.Order Order() {
    return ORDER_ANNOTATION;
  }

  class Order implements IScoutAnnotationApi.Order {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.Order";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.PageData PAGE_DATA_ANNOTATION = new PageData();

  @Override
  default IScoutAnnotationApi.PageData PageData() {
    return PAGE_DATA_ANNOTATION;
  }

  class PageData implements IScoutAnnotationApi.PageData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.dto.PageData";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.Replace REPLACE_ANNOTATION = new Replace();

  @Override
  default IScoutAnnotationApi.Replace Replace() {
    return REPLACE_ANNOTATION;
  }

  class Replace implements IScoutAnnotationApi.Replace {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.Replace";
    }
  }

  IScoutAnnotationApi.RunWith RUN_WITH_ANNOTATION = new RunWith();

  @Override
  default IScoutAnnotationApi.RunWith RunWith() {
    return RUN_WITH_ANNOTATION;
  }

  class RunWith implements IScoutAnnotationApi.RunWith {
    @Override
    public String fqn() {
      return "org.junit.runner.RunWith";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.RunWithClientSession RUN_WITH_CLIENT_SESSION_ANNOTATION = new RunWithClientSession();

  @Override
  default IScoutAnnotationApi.RunWithClientSession RunWithClientSession() {
    return RUN_WITH_CLIENT_SESSION_ANNOTATION;
  }

  class RunWithClientSession implements IScoutAnnotationApi.RunWithClientSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.client.runner.RunWithClientSession";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.RunWithServerSession RUN_WITH_SERVER_SESSION_ANNOTATION = new RunWithServerSession();

  @Override
  default IScoutAnnotationApi.RunWithServerSession RunWithServerSession() {
    return RUN_WITH_SERVER_SESSION_ANNOTATION;
  }

  class RunWithServerSession implements IScoutAnnotationApi.RunWithServerSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.server.runner.RunWithServerSession";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.RunWithSubject RUN_WITH_SUBJECT_ANNOTATION = new RunWithSubject();

  @Override
  default IScoutAnnotationApi.RunWithSubject RunWithSubject() {
    return RUN_WITH_SUBJECT_ANNOTATION;
  }

  class RunWithSubject implements IScoutAnnotationApi.RunWithSubject {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.platform.runner.RunWithSubject";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.Test TEST_ANNOTATION = new Test();

  @Override
  default IScoutAnnotationApi.Test Test() {
    return TEST_ANNOTATION;
  }

  class Test implements IScoutAnnotationApi.Test {
    @Override
    public String fqn() {
      return "org.junit.Test";
    }
  }

  IScoutAnnotationApi.TunnelToServer TUNNEL_TO_SERVER_ANNOTATION = new TunnelToServer();

  @Override
  default IScoutAnnotationApi.TunnelToServer TunnelToServer() {
    return TUNNEL_TO_SERVER_ANNOTATION;
  }

  class TunnelToServer implements IScoutAnnotationApi.TunnelToServer {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.TunnelToServer";
    }
  }

  IScoutAnnotationApi.WebServiceEntryPoint WEB_SERVICE_ENTRY_POINT_ANNOTATION = new WebServiceEntryPoint();

  @Override
  default IScoutAnnotationApi.WebServiceEntryPoint WebServiceEntryPoint() {
    return WEB_SERVICE_ENTRY_POINT_ANNOTATION;
  }

  class WebServiceEntryPoint implements IScoutAnnotationApi.WebServiceEntryPoint {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.provider.annotation.WebServiceEntryPoint";
    }

    @Override
    public String endpointInterfaceElementName() {
      return "endpointInterface";
    }

    @Override
    public String entryPointNameElementName() {
      return "entryPointName";
    }

    @Override
    public String serviceNameElementName() {
      return "serviceName";
    }

    @Override
    public String portNameElementName() {
      return "portName";
    }

    @Override
    public String entryPointPackageElementName() {
      return "entryPointPackage";
    }

    @Override
    public String authenticationElementName() {
      return "authentication";
    }

    @Override
    public String handlerChainElementName() {
      return "handlerChain";
    }
  }

  IScoutInterfaceApi.IAccordion I_ACCORDION = new IAccordion();

  @Override
  default IScoutInterfaceApi.IAccordion IAccordion() {
    return I_ACCORDION;
  }

  class IAccordion implements IScoutInterfaceApi.IAccordion {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.accordion.IAccordion";
    }
  }

  IScoutInterfaceApi.IAccordionField I_ACCORDION_FIELD = new IAccordionField();

  @Override
  default IScoutInterfaceApi.IAccordionField IAccordionField() {
    return I_ACCORDION_FIELD;
  }

  class IAccordionField implements IScoutInterfaceApi.IAccordionField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.accordionfield.IAccordionField";
    }
  }

  IScoutInterfaceApi.IAction I_ACTION = new IAction();

  @Override
  default IScoutInterfaceApi.IAction IAction() {
    return I_ACTION;
  }

  class IAction implements IScoutInterfaceApi.IAction {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.IAction";
    }
  }

  IScoutInterfaceApi.IActionNode I_ACTION_NODE = new IActionNode();

  @Override
  default IScoutInterfaceApi.IActionNode IActionNode() {
    return I_ACTION_NODE;
  }

  class IActionNode implements IScoutInterfaceApi.IActionNode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.tree.IActionNode";
    }
  }

  IScoutInterfaceApi.IBigDecimalField I_BIG_DECIMAL_FIELD = new IBigDecimalField();

  @Override
  default IScoutInterfaceApi.IBigDecimalField IBigDecimalField() {
    return I_BIG_DECIMAL_FIELD;
  }

  class IBigDecimalField implements IScoutInterfaceApi.IBigDecimalField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField";
    }
  }

  IScoutInterfaceApi.IBooleanField I_BOOLEAN_FIELD = new IBooleanField();

  @Override
  default IScoutInterfaceApi.IBooleanField IBooleanField() {
    return I_BOOLEAN_FIELD;
  }

  class IBooleanField implements IScoutInterfaceApi.IBooleanField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField";
    }
  }

  IScoutInterfaceApi.IBrowserField I_BROWSER_FIELD = new IBrowserField();

  @Override
  default IScoutInterfaceApi.IBrowserField IBrowserField() {
    return I_BROWSER_FIELD;
  }

  class IBrowserField implements IScoutInterfaceApi.IBrowserField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField";
    }
  }

  IScoutInterfaceApi.IButton I_BUTTON = new IButton();

  @Override
  default IScoutInterfaceApi.IButton IButton() {
    return I_BUTTON;
  }

  class IButton implements IScoutInterfaceApi.IButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.button.IButton";
    }
  }

  IScoutInterfaceApi.ICalendar I_CALENDAR = new ICalendar();

  @Override
  default IScoutInterfaceApi.ICalendar ICalendar() {
    return I_CALENDAR;
  }

  class ICalendar implements IScoutInterfaceApi.ICalendar {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar";
    }
  }

  IScoutInterfaceApi.ICalendarField I_CALENDAR_FIELD = new ICalendarField();

  @Override
  default IScoutInterfaceApi.ICalendarField ICalendarField() {
    return I_CALENDAR_FIELD;
  }

  class ICalendarField implements IScoutInterfaceApi.ICalendarField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField";
    }
  }

  IScoutInterfaceApi.ICalendarItemProvider I_CALENDAR_ITEM_PROVIDER = new ICalendarItemProvider();

  @Override
  default IScoutInterfaceApi.ICalendarItemProvider ICalendarItemProvider() {
    return I_CALENDAR_ITEM_PROVIDER;
  }

  class ICalendarItemProvider implements IScoutInterfaceApi.ICalendarItemProvider {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider";
    }
  }

  IScoutInterfaceApi.IClientSession I_CLIENT_SESSION = new IClientSession();

  @Override
  default IScoutInterfaceApi.IClientSession IClientSession() {
    return I_CLIENT_SESSION;
  }

  class IClientSession implements IScoutInterfaceApi.IClientSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.IClientSession";
    }

    @Override
    public String getDesktopMethodName() {
      return "getDesktop";
    }
  }

  IScoutInterfaceApi.ICode I_CODE = new ICode();

  @Override
  default IScoutInterfaceApi.ICode ICode() {
    return I_CODE;
  }

  class ICode implements IScoutInterfaceApi.ICode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.code.ICode";
    }

    @Override
    public String getIdMethodName() {
      return "getId";
    }
  }

  IScoutInterfaceApi.ICodeType I_CODE_TYPE = new ICodeType();

  @Override
  default IScoutInterfaceApi.ICodeType ICodeType() {
    return I_CODE_TYPE;
  }

  class ICodeType implements IScoutInterfaceApi.ICodeType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.code.ICodeType";
    }

    @Override
    public int codeTypeIdTypeParamIndex() {
      return 0;
    }

    @Override
    public int codeIdTypeParamIndex() {
      return 1;
    }

    @Override
    public String getIdMethodName() {
      return "getId";
    }
  }

  IScoutInterfaceApi.ITableBeanHolder I_TABLE_BEAN_HOLDER = new ITableBeanHolder();

  @Override
  default IScoutInterfaceApi.ITableBeanHolder ITableBeanHolder() {
    return I_TABLE_BEAN_HOLDER;
  }

  class ITableBeanHolder implements IScoutInterfaceApi.ITableBeanHolder {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.holders.ITableBeanHolder";
    }

    @Override
    public String addRowMethodName() {
      return "addRow";
    }

    @Override
    public String getRowTypeMethodName() {
      return "getRowType";
    }

    @Override
    public String getRowsMethodName() {
      return "getRows";
    }
  }

  IScoutInterfaceApi.IColumn I_COLUMN = new IColumn();

  @Override
  default IScoutInterfaceApi.IColumn IColumn() {
    return I_COLUMN;
  }

  class IColumn implements IScoutInterfaceApi.IColumn {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn";
    }

    @Override
    public int valueTypeParamIndex() {
      return 0;
    }
  }

  IScoutInterfaceApi.ICompositeField I_COMPOSITE_FIELD = new ICompositeField();

  @Override
  default IScoutInterfaceApi.ICompositeField ICompositeField() {
    return I_COMPOSITE_FIELD;
  }

  class ICompositeField implements IScoutInterfaceApi.ICompositeField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.ICompositeField";
    }

    @Override
    public String getFieldByClassMethodName() {
      return "getFieldByClass";
    }
  }

  IScoutInterfaceApi.IPropertyHolder I_PROPERTY_HOLDER = new IPropertyHolder();

  @Override
  default IScoutInterfaceApi.IPropertyHolder IPropertyHolder() {
    return I_PROPERTY_HOLDER;
  }

  class IPropertyHolder implements IScoutInterfaceApi.IPropertyHolder {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.form.IPropertyHolder";
    }

    @Override
    public String getPropertyByClassMethodName() {
      return "getPropertyByClass";
    }
  }

  IScoutInterfaceApi.ICompositeFieldExtension I_COMPOSITE_FIELD_EXTENSION = new ICompositeFieldExtension();

  @Override
  default IScoutInterfaceApi.ICompositeFieldExtension ICompositeFieldExtension() {
    return I_COMPOSITE_FIELD_EXTENSION;
  }

  class ICompositeFieldExtension implements IScoutInterfaceApi.ICompositeFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension";
    }
  }

  IScoutInterfaceApi.IContextMenuOwner I_CONTEXT_MENU_OWNER = new IContextMenuOwner();

  @Override
  default IScoutInterfaceApi.IContextMenuOwner IContextMenuOwner() {
    return I_CONTEXT_MENU_OWNER;
  }

  class IContextMenuOwner implements IScoutInterfaceApi.IContextMenuOwner {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner";
    }
  }

  IScoutInterfaceApi.IDataChangeObserver I_DATA_CHANGE_OBSERVER = new IDataChangeObserver();

  @Override
  default IScoutInterfaceApi.IDataChangeObserver IDataChangeObserver() {
    return I_DATA_CHANGE_OBSERVER;
  }

  class IDataChangeObserver implements IScoutInterfaceApi.IDataChangeObserver {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeObserver";
    }

    @Override
    public String registerDataChangeListenerMethodName() {
      return "registerDataChangeListener";
    }
  }

  IScoutInterfaceApi.IDataModelAttribute I_DATA_MODEL_ATTRIBUTE = new IDataModelAttribute();

  @Override
  default IScoutInterfaceApi.IDataModelAttribute IDataModelAttribute() {
    return I_DATA_MODEL_ATTRIBUTE;
  }

  class IDataModelAttribute implements IScoutInterfaceApi.IDataModelAttribute {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.model.IDataModelAttribute";
    }
  }

  IScoutInterfaceApi.IDataModelEntity I_DATA_MODEL_ENTITY = new IDataModelEntity();

  @Override
  default IScoutInterfaceApi.IDataModelEntity IDataModelEntity() {
    return I_DATA_MODEL_ENTITY;
  }

  class IDataModelEntity implements IScoutInterfaceApi.IDataModelEntity {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.model.IDataModelEntity";
    }
  }

  IScoutInterfaceApi.IDataObject I_DATA_OBJECT = new IDataObject();

  @Override
  default IScoutInterfaceApi.IDataObject IDataObject() {
    return I_DATA_OBJECT;
  }

  class IDataObject implements IScoutInterfaceApi.IDataObject {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.IDataObject";
    }
  }

  IScoutInterfaceApi.IDoEntity I_DO_ENTITY = new IDoEntity();

  @Override
  default IScoutInterfaceApi.IDoEntity IDoEntity() {
    return I_DO_ENTITY;
  }

  class IDoEntity implements IScoutInterfaceApi.IDoEntity {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.IDoEntity";
    }

    @Override
    public String computeGetterPrefixFor(CharSequence dataTypeRef) {
      // for DOs the "is" prefix is used for boxed booleans (java.lang.Boolean)
      // because there is no getter with a primitive boolean
      if (Strings.equals(JavaTypes.Boolean, dataTypeRef)) {
        return PropertyBean.GETTER_BOOL_PREFIX;
      }
      return PropertyBean.GETTER_PREFIX;
    }

    @Override
    public Stream<IScoutMethodGenerator<?, ?>> getAdditionalDoNodeGetters(CharSequence name, CharSequence dataTypeRef, IType ownerType) {
      return Stream.empty();
    }
  }

  IScoutInterfaceApi.IDateField I_DATE_FIELD = new IDateField();

  @Override
  default IScoutInterfaceApi.IDateField IDateField() {
    return I_DATE_FIELD;
  }

  class IDateField implements IScoutInterfaceApi.IDateField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField";
    }
  }

  IScoutInterfaceApi.IDesktop I_DESKTOP = new IDesktop();

  @Override
  default IScoutInterfaceApi.IDesktop IDesktop() {
    return I_DESKTOP;
  }

  class IDesktop implements IScoutInterfaceApi.IDesktop {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.IDesktop";
    }

    @Override
    public String dataChangedMethodName() {
      return "dataChanged";
    }
  }

  IScoutInterfaceApi.IDesktopExtension I_DESKTOP_EXTENSION = new IDesktopExtension();

  @Override
  default IScoutInterfaceApi.IDesktopExtension IDesktopExtension() {
    return I_DESKTOP_EXTENSION;
  }

  class IDesktopExtension implements IScoutInterfaceApi.IDesktopExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension";
    }
  }

  IScoutInterfaceApi.IExtension I_EXTENSION = new IExtension();

  @Override
  default IScoutInterfaceApi.IExtension IExtension() {
    return I_EXTENSION;
  }

  class IExtension implements IScoutInterfaceApi.IExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.extension.IExtension";
    }

    @Override
    public int ownerTypeParamIndex() {
      return 0;
    }

    @Override
    public String getOwnerMethodName() {
      return "getOwner";
    }
  }

  IScoutInterfaceApi.IFileChooserButton I_FILE_CHOOSER_BUTTON = new IFileChooserButton();

  @Override
  default IScoutInterfaceApi.IFileChooserButton IFileChooserButton() {
    return I_FILE_CHOOSER_BUTTON;
  }

  class IFileChooserButton implements IScoutInterfaceApi.IFileChooserButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.IFileChooserButton";
    }
  }

  IScoutInterfaceApi.IFileChooserField I_FILE_CHOOSER_FIELD = new IFileChooserField();

  @Override
  default IScoutInterfaceApi.IFileChooserField IFileChooserField() {
    return I_FILE_CHOOSER_FIELD;
  }

  class IFileChooserField implements IScoutInterfaceApi.IFileChooserField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField";
    }
  }

  IScoutInterfaceApi.IForm I_FORM = new IForm();

  @Override
  default IScoutInterfaceApi.IForm IForm() {
    return I_FORM;
  }

  class IForm implements IScoutInterfaceApi.IForm {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.IForm";
    }

    @Override
    public String getFieldByClassMethodName() {
      return "getFieldByClass";
    }

    @Override
    public String exportFormDataMethodName() {
      return "exportFormData";
    }

    @Override
    public String importFormDataMethodName() {
      return "importFormData";
    }

    @Override
    public String startMethodName() {
      return "start";
    }
  }

  IScoutInterfaceApi.IFormExtension I_FORM_EXTENSION = new IFormExtension();

  @Override
  default IScoutInterfaceApi.IFormExtension IFormExtension() {
    return I_FORM_EXTENSION;
  }

  class IFormExtension implements IScoutInterfaceApi.IFormExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.IFormExtension";
    }
  }

  IScoutInterfaceApi.IFormField I_FORM_FIELD = new IFormField();

  @Override
  default IScoutInterfaceApi.IFormField IFormField() {
    return I_FORM_FIELD;
  }

  class IFormField implements IScoutInterfaceApi.IFormField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.IFormField";
    }
  }

  IScoutInterfaceApi.IFormFieldExtension I_FORM_FIELD_EXTENSION = new IFormFieldExtension();

  @Override
  default IScoutInterfaceApi.IFormFieldExtension IFormFieldExtension() {
    return I_FORM_FIELD_EXTENSION;
  }

  class IFormFieldExtension implements IScoutInterfaceApi.IFormFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension";
    }
  }

  IScoutInterfaceApi.IFormFieldMenu I_FORM_FIELD_MENU = new IFormFieldMenu();

  @Override
  default IScoutInterfaceApi.IFormFieldMenu IFormFieldMenu() {
    return I_FORM_FIELD_MENU;
  }

  class IFormFieldMenu implements IScoutInterfaceApi.IFormFieldMenu {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.form.fields.IFormFieldMenu";
    }
  }

  IScoutInterfaceApi.IFormHandler I_FORM_HANDLER = new IFormHandler();

  @Override
  default IScoutInterfaceApi.IFormHandler IFormHandler() {
    return I_FORM_HANDLER;
  }

  class IFormHandler implements IScoutInterfaceApi.IFormHandler {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.IFormHandler";
    }
  }

  IScoutInterfaceApi.IGroup I_GROUP = new IGroup();

  @Override
  default IScoutInterfaceApi.IGroup IGroup() {
    return I_GROUP;
  }

  class IGroup implements IScoutInterfaceApi.IGroup {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.group.IGroup";
    }
  }

  IScoutInterfaceApi.IGroupBox I_GROUP_BOX = new IGroupBox();

  @Override
  default IScoutInterfaceApi.IGroupBox IGroupBox() {
    return I_GROUP_BOX;
  }

  class IGroupBox implements IScoutInterfaceApi.IGroupBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox";
    }
  }

  IScoutInterfaceApi.IHtmlField I_HTML_FIELD = new IHtmlField();

  @Override
  default IScoutInterfaceApi.IHtmlField IHtmlField() {
    return I_HTML_FIELD;
  }

  class IHtmlField implements IScoutInterfaceApi.IHtmlField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField";
    }
  }

  IScoutInterfaceApi.IImageField I_IMAGE_FIELD = new IImageField();

  @Override
  default IScoutInterfaceApi.IImageField IImageField() {
    return I_IMAGE_FIELD;
  }

  class IImageField implements IScoutInterfaceApi.IImageField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField";
    }
  }

  IScoutInterfaceApi.IKeyStroke I_KEY_STROKE = new IKeyStroke();

  @Override
  default IScoutInterfaceApi.IKeyStroke IKeyStroke() {
    return I_KEY_STROKE;
  }

  class IKeyStroke implements IScoutInterfaceApi.IKeyStroke {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke";
    }
  }

  IScoutInterfaceApi.ILabelField I_LABEL_FIELD = new ILabelField();

  @Override
  default IScoutInterfaceApi.ILabelField ILabelField() {
    return I_LABEL_FIELD;
  }

  class ILabelField implements IScoutInterfaceApi.ILabelField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField";
    }
  }

  IScoutInterfaceApi.IListBox I_LIST_BOX = new IListBox();

  @Override
  default IScoutInterfaceApi.IListBox IListBox() {
    return I_LIST_BOX;
  }

  class IListBox implements IScoutInterfaceApi.IListBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox";
    }
  }

  IScoutInterfaceApi.ILongField I_LONG_FIELD = new ILongField();

  @Override
  default IScoutInterfaceApi.ILongField ILongField() {
    return I_LONG_FIELD;
  }

  class ILongField implements IScoutInterfaceApi.ILongField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.longfield.ILongField";
    }
  }

  IScoutInterfaceApi.ILookupCall I_LOOKUP_CALL = new ILookupCall();

  @Override
  default IScoutInterfaceApi.ILookupCall ILookupCall() {
    return I_LOOKUP_CALL;
  }

  class ILookupCall implements IScoutInterfaceApi.ILookupCall {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.lookup.ILookupCall";
    }
  }

  IScoutInterfaceApi.ILookupRow I_LOOKUP_ROW = new ILookupRow();

  @Override
  default IScoutInterfaceApi.ILookupRow ILookupRow() {
    return I_LOOKUP_ROW;
  }

  class ILookupRow implements IScoutInterfaceApi.ILookupRow {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.lookup.ILookupRow";
    }
  }

  IScoutInterfaceApi.ILookupService I_LOOKUP_SERVICE = new ILookupService();

  @Override
  default IScoutInterfaceApi.ILookupService ILookupService() {
    return I_LOOKUP_SERVICE;
  }

  class ILookupService implements IScoutInterfaceApi.ILookupService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.lookup.ILookupService";
    }

    @Override
    public int keyTypeTypeParamIndex() {
      return 0;
    }

    @Override
    public String getDataByKeyMethodName() {
      return "getDataByKey";
    }

    @Override
    public String getDataByTextMethodName() {
      return "getDataByText";
    }
  }

  IScoutInterfaceApi.IMenu I_MENU = new IMenu();

  @Override
  default IScoutInterfaceApi.IMenu IMenu() {
    return I_MENU;
  }

  class IMenu implements IScoutInterfaceApi.IMenu {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.IMenu";
    }
  }

  IScoutInterfaceApi.IMenuType I_MENU_TYPE = new IMenuType();

  @Override
  default IScoutInterfaceApi.IMenuType IMenuType() {
    return I_MENU_TYPE;
  }

  class IMenuType implements IScoutInterfaceApi.IMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.IMenuType";
    }
  }

  IScoutInterfaceApi.IMessageBox I_MESSAGE_BOX = new IMessageBox();

  @Override
  default IScoutInterfaceApi.IMessageBox IMessageBox() {
    return I_MESSAGE_BOX;
  }

  class IMessageBox implements IScoutInterfaceApi.IMessageBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.messagebox.IMessageBox";
    }

    @Override
    public String showMethodName() {
      return "show";
    }

    @Override
    public String withHeaderMethodName() {
      return "withHeader";
    }
  }

  IScoutInterfaceApi.IMode I_MODE = new IMode();

  @Override
  default IScoutInterfaceApi.IMode IMode() {
    return I_MODE;
  }

  class IMode implements IScoutInterfaceApi.IMode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.mode.IMode";
    }
  }

  IScoutInterfaceApi.IModeSelectorField I_MODE_SELECTOR_FIELD = new IModeSelectorField();

  @Override
  default IScoutInterfaceApi.IModeSelectorField IModeSelectorField() {
    return I_MODE_SELECTOR_FIELD;
  }

  class IModeSelectorField implements IScoutInterfaceApi.IModeSelectorField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.modeselector.IModeSelectorField";
    }
  }

  IScoutInterfaceApi.IOrdered I_ORDERED = new IOrdered();

  @Override
  default IScoutInterfaceApi.IOrdered IOrdered() {
    return I_ORDERED;
  }

  class IOrdered implements IScoutInterfaceApi.IOrdered {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.IOrdered";
    }
  }

  IScoutInterfaceApi.IOutline I_OUTLINE = new IOutline();

  @Override
  default IScoutInterfaceApi.IOutline IOutline() {
    return I_OUTLINE;
  }

  class IOutline implements IScoutInterfaceApi.IOutline {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.IOutline";
    }
  }

  IScoutInterfaceApi.IPage I_PAGE = new IPage();

  @Override
  default IScoutInterfaceApi.IPage IPage() {
    return I_PAGE;
  }

  class IPage implements IScoutInterfaceApi.IPage {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage";
    }

    @Override
    public String initPageMethodName() {
      return "initPage";
    }
  }

  IScoutInterfaceApi.IPageWithNodes I_PAGE_WITH_NODES = new IPageWithNodes();

  @Override
  default IScoutInterfaceApi.IPageWithNodes IPageWithNodes() {
    return I_PAGE_WITH_NODES;
  }

  class IPageWithNodes implements IScoutInterfaceApi.IPageWithNodes {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes";
    }
  }

  IScoutInterfaceApi.IPageWithTable I_PAGE_WITH_TABLE = new IPageWithTable();

  @Override
  default IScoutInterfaceApi.IPageWithTable IPageWithTable() {
    return I_PAGE_WITH_TABLE;
  }

  class IPageWithTable implements IScoutInterfaceApi.IPageWithTable {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable";
    }
  }

  IScoutInterfaceApi.IPageWithTableExtension I_PAGE_WITH_TABLE_EXTENSION = new IPageWithTableExtension();

  @Override
  default IScoutInterfaceApi.IPageWithTableExtension IPageWithTableExtension() {
    return I_PAGE_WITH_TABLE_EXTENSION;
  }

  class IPageWithTableExtension implements IScoutInterfaceApi.IPageWithTableExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.IPageWithTableExtension";
    }
  }

  IScoutInterfaceApi.IPrettyPrintDataObjectMapper I_PRETTY_PRINT_DATA_OBJECT_MAPPER = new IPrettyPrintDataObjectMapper();

  @Override
  default IScoutInterfaceApi.IPrettyPrintDataObjectMapper IPrettyPrintDataObjectMapper() {
    return I_PRETTY_PRINT_DATA_OBJECT_MAPPER;
  }

  class IPrettyPrintDataObjectMapper implements IScoutInterfaceApi.IPrettyPrintDataObjectMapper {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper";
    }
  }

  IScoutInterfaceApi.IProposalField I_PROPOSAL_FIELD = new IProposalField();

  @Override
  default IScoutInterfaceApi.IProposalField IProposalField() {
    return I_PROPOSAL_FIELD;
  }

  class IProposalField implements IScoutInterfaceApi.IProposalField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField";
    }
  }

  IScoutInterfaceApi.IRadioButton I_RADIO_BUTTON = new IRadioButton();

  @Override
  default IScoutInterfaceApi.IRadioButton IRadioButton() {
    return I_RADIO_BUTTON;
  }

  class IRadioButton implements IScoutInterfaceApi.IRadioButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton";
    }
  }

  IScoutInterfaceApi.IRadioButtonGroup I_RADIO_BUTTON_GROUP = new IRadioButtonGroup();

  @Override
  default IScoutInterfaceApi.IRadioButtonGroup IRadioButtonGroup() {
    return I_RADIO_BUTTON_GROUP;
  }

  class IRadioButtonGroup implements IScoutInterfaceApi.IRadioButtonGroup {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup";
    }
  }

  IScoutInterfaceApi.ISequenceBox I_SEQUENCE_BOX = new ISequenceBox();

  @Override
  default IScoutInterfaceApi.ISequenceBox ISequenceBox() {
    return I_SEQUENCE_BOX;
  }

  class ISequenceBox implements IScoutInterfaceApi.ISequenceBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox";
    }
  }

  IScoutInterfaceApi.IServerSession I_SERVER_SESSION = new IServerSession();

  @Override
  default IScoutInterfaceApi.IServerSession IServerSession() {
    return I_SERVER_SESSION;
  }

  class IServerSession implements IScoutInterfaceApi.IServerSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.IServerSession";
    }
  }

  IScoutInterfaceApi.IService I_SERVICE = new IService();

  @Override
  default IScoutInterfaceApi.IService IService() {
    return I_SERVICE;
  }

  class IService implements IScoutInterfaceApi.IService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.service.IService";
    }
  }

  IScoutInterfaceApi.ISession I_SESSION = new ISession();

  @Override
  default IScoutInterfaceApi.ISession ISession() {
    return I_SESSION;
  }

  class ISession implements IScoutInterfaceApi.ISession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.ISession";
    }
  }

  IScoutInterfaceApi.ISmartField I_SMART_FIELD = new ISmartField();

  @Override
  default IScoutInterfaceApi.ISmartField ISmartField() {
    return I_SMART_FIELD;
  }

  class ISmartField implements IScoutInterfaceApi.ISmartField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField";
    }
  }

  IScoutInterfaceApi.IStringField I_STRING_FIELD = new IStringField();

  @Override
  default IScoutInterfaceApi.IStringField IStringField() {
    return I_STRING_FIELD;
  }

  class IStringField implements IScoutInterfaceApi.IStringField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField";
    }
  }

  IScoutInterfaceApi.ITabBox I_TAB_BOX = new ITabBox();

  @Override
  default IScoutInterfaceApi.ITabBox ITabBox() {
    return I_TAB_BOX;
  }

  class ITabBox implements IScoutInterfaceApi.ITabBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox";
    }
  }

  IScoutInterfaceApi.ITable I_TABLE = new ITable();

  @Override
  default IScoutInterfaceApi.ITable ITable() {
    return I_TABLE;
  }

  class ITable implements IScoutInterfaceApi.ITable {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.ITable";
    }

    @Override
    public String getColumnSetMethodName() {
      return "getColumnSet";
    }

    @Override
    public String getSelectedRowCountMethodName() {
      return "getSelectedRowCount";
    }
  }

  IScoutInterfaceApi.ITableControl I_TABLE_CONTROL = new ITableControl();

  @Override
  default IScoutInterfaceApi.ITableControl ITableControl() {
    return I_TABLE_CONTROL;
  }

  class ITableControl implements IScoutInterfaceApi.ITableControl {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl";
    }
  }

  IScoutInterfaceApi.ITableExtension I_TABLE_EXTENSION = new ITableExtension();

  @Override
  default IScoutInterfaceApi.ITableExtension ITableExtension() {
    return I_TABLE_EXTENSION;
  }

  class ITableExtension implements IScoutInterfaceApi.ITableExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension";
    }
  }

  IScoutInterfaceApi.ITableField I_TABLE_FIELD = new ITableField();

  @Override
  default IScoutInterfaceApi.ITableField ITableField() {
    return I_TABLE_FIELD;
  }

  class ITableField implements IScoutInterfaceApi.ITableField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField";
    }
  }

  IScoutInterfaceApi.ITagField I_TAG_FIELD = new ITagField();

  @Override
  default IScoutInterfaceApi.ITagField ITagField() {
    return I_TAG_FIELD;
  }

  class ITagField implements IScoutInterfaceApi.ITagField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tagfield.ITagField";
    }
  }

  IScoutInterfaceApi.ITextProviderService I_TEXT_PROVIDER_SERVICE = new ITextProviderService();

  @Override
  default IScoutInterfaceApi.ITextProviderService ITextProviderService() {
    return I_TEXT_PROVIDER_SERVICE;
  }

  class ITextProviderService implements IScoutInterfaceApi.ITextProviderService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.text.ITextProviderService";
    }
  }

  IScoutInterfaceApi.ITile I_TILE = new ITile();

  @Override
  default IScoutInterfaceApi.ITile ITile() {
    return I_TILE;
  }

  class ITile implements IScoutInterfaceApi.ITile {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.tile.ITile";
    }
  }

  IScoutInterfaceApi.ITileField I_TILE_FIELD = new ITileField();

  @Override
  default IScoutInterfaceApi.ITileField ITileField() {
    return I_TILE_FIELD;
  }

  class ITileField implements IScoutInterfaceApi.ITileField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tilefield.ITileField";
    }
  }

  IScoutInterfaceApi.ITileGrid I_TILE_GRID = new ITileGrid();

  @Override
  default IScoutInterfaceApi.ITileGrid ITileGrid() {
    return I_TILE_GRID;
  }

  class ITileGrid implements IScoutInterfaceApi.ITileGrid {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.tile.ITileGrid";
    }
  }

  IScoutInterfaceApi.ITree I_TREE = new ITree();

  @Override
  default IScoutInterfaceApi.ITree ITree() {
    return I_TREE;
  }

  class ITree implements IScoutInterfaceApi.ITree {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.tree.ITree";
    }
  }

  IScoutInterfaceApi.ITreeField I_TREE_FIELD = new ITreeField();

  @Override
  default IScoutInterfaceApi.ITreeField ITreeField() {
    return I_TREE_FIELD;
  }

  class ITreeField implements IScoutInterfaceApi.ITreeField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField";
    }
  }

  IScoutInterfaceApi.ITreeNode I_TREE_NODE = new ITreeNode();

  @Override
  default IScoutInterfaceApi.ITreeNode ITreeNode() {
    return I_TREE_NODE;
  }

  class ITreeNode implements IScoutInterfaceApi.ITreeNode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode";
    }

    @Override
    public String setVisibleGrantedMethodName() {
      return "setVisibleGranted";
    }
  }

  IScoutInterfaceApi.ITypeWithClassId I_TYPE_WITH_CLASS_ID = new ITypeWithClassId();

  @Override
  default IScoutInterfaceApi.ITypeWithClassId ITypeWithClassId() {
    return I_TYPE_WITH_CLASS_ID;
  }

  class ITypeWithClassId implements IScoutInterfaceApi.ITypeWithClassId {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.classid.ITypeWithClassId";
    }
  }

  IScoutInterfaceApi.IUuId I_UU_ID = new IUuId();

  @Override
  default IScoutInterfaceApi.IUuId IUuId() {
    return I_UU_ID;
  }

  class IUuId implements IScoutInterfaceApi.IUuId {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.id.IUuId";
    }
  }

  IScoutInterfaceApi.IValueField I_VALUE_FIELD = new IValueField();

  @Override
  default IScoutInterfaceApi.IValueField IValueField() {
    return I_VALUE_FIELD;
  }

  class IValueField implements IScoutInterfaceApi.IValueField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.IValueField";
    }

    @Override
    public int valueTypeParamIndex() {
      return 0;
    }
  }

  IScoutInterfaceApi.IViewButton I_VIEW_BUTTON = new IViewButton();

  @Override
  default IScoutInterfaceApi.IViewButton IViewButton() {
    return I_VIEW_BUTTON;
  }

  class IViewButton implements IScoutInterfaceApi.IViewButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.view.IViewButton";
    }
  }

  IScoutInterfaceApi.IWidget I_WIDGET = new IWidget();

  @Override
  default IScoutInterfaceApi.IWidget IWidget() {
    return I_WIDGET;
  }

  class IWidget implements IScoutInterfaceApi.IWidget {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.IWidget";
    }

    @Override
    public String setEnabledPermissionMethodName() {
      return "setEnabledPermission";
    }

    @Override
    public String setEnabledGrantedMethodName() {
      return "setEnabledGranted";
    }
  }

  IScoutInterfaceApi.IWizard I_WIZARD = new IWizard();

  @Override
  default IScoutInterfaceApi.IWizard IWizard() {
    return I_WIZARD;
  }

  class IWizard implements IScoutInterfaceApi.IWizard {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.wizard.IWizard";
    }
  }

  IScoutInterfaceApi.IWizardStep I_WIZARD_STEP = new IWizardStep();

  @Override
  default IScoutInterfaceApi.IWizardStep IWizardStep() {
    return I_WIZARD_STEP;
  }

  class IWizardStep implements IScoutInterfaceApi.IWizardStep {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.wizard.IWizardStep";
    }
  }

  IScoutInterfaceApi.IConfigProperty I_CONFIG_PROPERTY = new IConfigProperty();

  @Override
  default IScoutInterfaceApi.IConfigProperty IConfigProperty() {
    return I_CONFIG_PROPERTY;
  }

  class IConfigProperty implements IScoutInterfaceApi.IConfigProperty {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.config.IConfigProperty";
    }

    @Override
    public String getKeyMethodName() {
      return "getKey";
    }

    @Override
    public String descriptionMethodName() {
      return "description";
    }
  }

  IScoutAbstractApi.AbstractAccordion ABSTRACT_ACCORDION = new AbstractAccordion();

  @Override
  default IScoutAbstractApi.AbstractAccordion AbstractAccordion() {
    return ABSTRACT_ACCORDION;
  }

  class AbstractAccordion implements IScoutAbstractApi.AbstractAccordion {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion";
    }
  }

  IScoutAbstractApi.AbstractAccordionField ABSTRACT_ACCORDION_FIELD = new AbstractAccordionField();

  @Override
  default IScoutAbstractApi.AbstractAccordionField AbstractAccordionField() {
    return ABSTRACT_ACCORDION_FIELD;
  }

  class AbstractAccordionField implements IScoutAbstractApi.AbstractAccordionField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.accordionfield.AbstractAccordionField";
    }
  }

  IScoutAbstractApi.AbstractActionNode ABSTRACT_ACTION_NODE = new AbstractActionNode();

  @Override
  default IScoutAbstractApi.AbstractActionNode AbstractActionNode() {
    return ABSTRACT_ACTION_NODE;
  }

  class AbstractActionNode implements IScoutAbstractApi.AbstractActionNode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.tree.AbstractActionNode";
    }
  }

  IScoutAbstractApi.AbstractBigDecimalField ABSTRACT_BIG_DECIMAL_FIELD = new AbstractBigDecimalField();

  @Override
  default IScoutAbstractApi.AbstractBigDecimalField AbstractBigDecimalField() {
    return ABSTRACT_BIG_DECIMAL_FIELD;
  }

  class AbstractBigDecimalField implements IScoutAbstractApi.AbstractBigDecimalField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField";
    }
  }

  IScoutAbstractApi.AbstractBooleanField ABSTRACT_BOOLEAN_FIELD = new AbstractBooleanField();

  @Override
  default IScoutAbstractApi.AbstractBooleanField AbstractBooleanField() {
    return ABSTRACT_BOOLEAN_FIELD;
  }

  class AbstractBooleanField implements IScoutAbstractApi.AbstractBooleanField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField";
    }
  }

  IScoutAbstractApi.AbstractBrowserField ABSTRACT_BROWSER_FIELD = new AbstractBrowserField();

  @Override
  default IScoutAbstractApi.AbstractBrowserField AbstractBrowserField() {
    return ABSTRACT_BROWSER_FIELD;
  }

  class AbstractBrowserField implements IScoutAbstractApi.AbstractBrowserField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField";
    }
  }

  IScoutAbstractApi.AbstractButton ABSTRACT_BUTTON = new AbstractButton();

  @Override
  default IScoutAbstractApi.AbstractButton AbstractButton() {
    return ABSTRACT_BUTTON;
  }

  class AbstractButton implements IScoutAbstractApi.AbstractButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton";
    }

    @Override
    public String execClickActionMethodName() {
      return "execClickAction";
    }
  }

  IScoutAbstractApi.AbstractCalendar ABSTRACT_CALENDAR = new AbstractCalendar();

  @Override
  default IScoutAbstractApi.AbstractCalendar AbstractCalendar() {
    return ABSTRACT_CALENDAR;
  }

  class AbstractCalendar implements IScoutAbstractApi.AbstractCalendar {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar";
    }
  }

  IScoutAbstractApi.AbstractCalendarField ABSTRACT_CALENDAR_FIELD = new AbstractCalendarField();

  @Override
  default IScoutAbstractApi.AbstractCalendarField AbstractCalendarField() {
    return ABSTRACT_CALENDAR_FIELD;
  }

  class AbstractCalendarField implements IScoutAbstractApi.AbstractCalendarField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField";
    }
  }

  IScoutAbstractApi.AbstractCalendarItemProvider ABSTRACT_CALENDAR_ITEM_PROVIDER = new AbstractCalendarItemProvider();

  @Override
  default IScoutAbstractApi.AbstractCalendarItemProvider AbstractCalendarItemProvider() {
    return ABSTRACT_CALENDAR_ITEM_PROVIDER;
  }

  class AbstractCalendarItemProvider implements IScoutAbstractApi.AbstractCalendarItemProvider {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider";
    }
  }

  IScoutAbstractApi.AbstractCancelButton ABSTRACT_CANCEL_BUTTON = new AbstractCancelButton();

  @Override
  default IScoutAbstractApi.AbstractCancelButton AbstractCancelButton() {
    return ABSTRACT_CANCEL_BUTTON;
  }

  class AbstractCancelButton implements IScoutAbstractApi.AbstractCancelButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton";
    }
  }

  IScoutAbstractApi.AbstractCode ABSTRACT_CODE = new AbstractCode();

  @Override
  default IScoutAbstractApi.AbstractCode AbstractCode() {
    return ABSTRACT_CODE;
  }

  class AbstractCode implements IScoutAbstractApi.AbstractCode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.code.AbstractCode";
    }

    @Override
    public String getConfiguredTextMethodName() {
      return "getConfiguredText";
    }
  }

  IScoutAbstractApi.AbstractCodeType ABSTRACT_CODE_TYPE = new AbstractCodeType();

  @Override
  default IScoutAbstractApi.AbstractCodeType AbstractCodeType() {
    return ABSTRACT_CODE_TYPE;
  }

  class AbstractCodeType implements IScoutAbstractApi.AbstractCodeType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType";
    }
  }

  IScoutAbstractApi.AbstractCodeTypeWithGeneric ABSTRACT_CODE_TYPE_WITH_GENERIC = new AbstractCodeTypeWithGeneric();

  @Override
  default IScoutAbstractApi.AbstractCodeTypeWithGeneric AbstractCodeTypeWithGeneric() {
    return ABSTRACT_CODE_TYPE_WITH_GENERIC;
  }

  class AbstractCodeTypeWithGeneric implements IScoutAbstractApi.AbstractCodeTypeWithGeneric {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric";
    }
  }

  IScoutAbstractApi.AbstractComposerField ABSTRACT_COMPOSER_FIELD = new AbstractComposerField();

  @Override
  default IScoutAbstractApi.AbstractComposerField AbstractComposerField() {
    return ABSTRACT_COMPOSER_FIELD;
  }

  class AbstractComposerField implements IScoutAbstractApi.AbstractComposerField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField";
    }
  }

  IScoutAbstractApi.AbstractCompositeField ABSTRACT_COMPOSITE_FIELD = new AbstractCompositeField();

  @Override
  default IScoutAbstractApi.AbstractCompositeField AbstractCompositeField() {
    return ABSTRACT_COMPOSITE_FIELD;
  }

  class AbstractCompositeField implements IScoutAbstractApi.AbstractCompositeField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField";
    }
  }

  IScoutAbstractApi.AbstractDataModel ABSTRACT_DATA_MODEL = new AbstractDataModel();

  @Override
  default IScoutAbstractApi.AbstractDataModel AbstractDataModel() {
    return ABSTRACT_DATA_MODEL;
  }

  class AbstractDataModel implements IScoutAbstractApi.AbstractDataModel {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.model.AbstractDataModel";
    }
  }

  IScoutAbstractApi.AbstractDataModelEntity ABSTRACT_DATA_MODEL_ENTITY = new AbstractDataModelEntity();

  @Override
  default IScoutAbstractApi.AbstractDataModelEntity AbstractDataModelEntity() {
    return ABSTRACT_DATA_MODEL_ENTITY;
  }

  class AbstractDataModelEntity implements IScoutAbstractApi.AbstractDataModelEntity {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity";
    }
  }

  IScoutAbstractApi.AbstractDateField ABSTRACT_DATE_FIELD = new AbstractDateField();

  @Override
  default IScoutAbstractApi.AbstractDateField AbstractDateField() {
    return ABSTRACT_DATE_FIELD;
  }

  class AbstractDateField implements IScoutAbstractApi.AbstractDateField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField";
    }
  }

  IScoutAbstractApi.AbstractDesktop ABSTRACT_DESKTOP = new AbstractDesktop();

  @Override
  default IScoutAbstractApi.AbstractDesktop AbstractDesktop() {
    return ABSTRACT_DESKTOP;
  }

  class AbstractDesktop implements IScoutAbstractApi.AbstractDesktop {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop";
    }
  }

  IScoutAbstractApi.AbstractDesktopExtension ABSTRACT_DESKTOP_EXTENSION = new AbstractDesktopExtension();

  @Override
  default IScoutAbstractApi.AbstractDesktopExtension AbstractDesktopExtension() {
    return ABSTRACT_DESKTOP_EXTENSION;
  }

  class AbstractDesktopExtension implements IScoutAbstractApi.AbstractDesktopExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopExtension";
    }
  }

  IScoutAbstractApi.AbstractDynamicNlsTextProviderService ABSTRACT_DYNAMIC_NLS_TEXT_PROVIDER_SERVICE = new AbstractDynamicNlsTextProviderService();

  @Override
  default IScoutAbstractApi.AbstractDynamicNlsTextProviderService AbstractDynamicNlsTextProviderService() {
    return ABSTRACT_DYNAMIC_NLS_TEXT_PROVIDER_SERVICE;
  }

  class AbstractDynamicNlsTextProviderService implements IScoutAbstractApi.AbstractDynamicNlsTextProviderService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.text.AbstractDynamicNlsTextProviderService";
    }

    @Override
    public String getDynamicNlsBaseNameMethodName() {
      return "getDynamicNlsBaseName";
    }
  }

  IScoutAbstractApi.AbstractExtension ABSTRACT_EXTENSION = new AbstractExtension();

  @Override
  default IScoutAbstractApi.AbstractExtension AbstractExtension() {
    return ABSTRACT_EXTENSION;
  }

  class AbstractExtension implements IScoutAbstractApi.AbstractExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.extension.AbstractExtension";
    }
  }

  IScoutAbstractApi.AbstractFileChooserButton ABSTRACT_FILE_CHOOSER_BUTTON = new AbstractFileChooserButton();

  @Override
  default IScoutAbstractApi.AbstractFileChooserButton AbstractFileChooserButton() {
    return ABSTRACT_FILE_CHOOSER_BUTTON;
  }

  class AbstractFileChooserButton implements IScoutAbstractApi.AbstractFileChooserButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.AbstractFileChooserButton";
    }
  }

  IScoutAbstractApi.AbstractFileChooserField ABSTRACT_FILE_CHOOSER_FIELD = new AbstractFileChooserField();

  @Override
  default IScoutAbstractApi.AbstractFileChooserField AbstractFileChooserField() {
    return ABSTRACT_FILE_CHOOSER_FIELD;
  }

  class AbstractFileChooserField implements IScoutAbstractApi.AbstractFileChooserField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.AbstractFileChooserField";
    }
  }

  IScoutAbstractApi.AbstractForm ABSTRACT_FORM = new AbstractForm();

  @Override
  default IScoutAbstractApi.AbstractForm AbstractForm() {
    return ABSTRACT_FORM;
  }

  class AbstractForm implements IScoutAbstractApi.AbstractForm {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.AbstractForm";
    }

    @Override
    public String getConfiguredTitleMethodName() {
      return "getConfiguredTitle";
    }

    @Override
    public String setHandlerMethodName() {
      return "setHandler";
    }

    @Override
    public String startInternalExclusiveMethodName() {
      return "startInternalExclusive";
    }

    @Override
    public String startInternalMethodName() {
      return "startInternal";
    }
  }

  IScoutAbstractApi.AbstractFormData ABSTRACT_FORM_DATA = new AbstractFormData();

  @Override
  default IScoutAbstractApi.AbstractFormData AbstractFormData() {
    return ABSTRACT_FORM_DATA;
  }

  class AbstractFormData implements IScoutAbstractApi.AbstractFormData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.form.AbstractFormData";
    }
  }

  IScoutAbstractApi.AbstractFormField ABSTRACT_FORM_FIELD = new AbstractFormField();

  @Override
  default IScoutAbstractApi.AbstractFormField AbstractFormField() {
    return ABSTRACT_FORM_FIELD;
  }

  class AbstractFormField implements IScoutAbstractApi.AbstractFormField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField";
    }

    @Override
    public String getConfiguredLabelMethodName() {
      return "getConfiguredLabel";
    }

    @Override
    public String getConfiguredGridHMethodName() {
      return "getConfiguredGridH";
    }

    @Override
    public String getConfiguredLabelVisibleMethodName() {
      return "getConfiguredLabelVisible";
    }

    @Override
    public String getConfiguredMandatoryMethodName() {
      return "getConfiguredMandatory";
    }
  }

  IScoutAbstractApi.AbstractFormFieldData ABSTRACT_FORM_FIELD_DATA = new AbstractFormFieldData();

  @Override
  default IScoutAbstractApi.AbstractFormFieldData AbstractFormFieldData() {
    return ABSTRACT_FORM_FIELD_DATA;
  }

  class AbstractFormFieldData implements IScoutAbstractApi.AbstractFormFieldData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData";
    }
  }

  IScoutAbstractApi.AbstractFormHandler ABSTRACT_FORM_HANDLER = new AbstractFormHandler();

  @Override
  default IScoutAbstractApi.AbstractFormHandler AbstractFormHandler() {
    return ABSTRACT_FORM_HANDLER;
  }

  class AbstractFormHandler implements IScoutAbstractApi.AbstractFormHandler {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.AbstractFormHandler";
    }

    @Override
    public String execStoreMethodName() {
      return "execStore";
    }

    @Override
    public String execLoadMethodName() {
      return "execLoad";
    }
  }

  IScoutAbstractApi.AbstractGroup ABSTRACT_GROUP = new AbstractGroup();

  @Override
  default IScoutAbstractApi.AbstractGroup AbstractGroup() {
    return ABSTRACT_GROUP;
  }

  class AbstractGroup implements IScoutAbstractApi.AbstractGroup {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.group.AbstractGroup";
    }
  }

  IScoutAbstractApi.AbstractGroupBox ABSTRACT_GROUP_BOX = new AbstractGroupBox();

  @Override
  default IScoutAbstractApi.AbstractGroupBox AbstractGroupBox() {
    return ABSTRACT_GROUP_BOX;
  }

  class AbstractGroupBox implements IScoutAbstractApi.AbstractGroupBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox";
    }
  }

  IScoutAbstractApi.AbstractHtmlField ABSTRACT_HTML_FIELD = new AbstractHtmlField();

  @Override
  default IScoutAbstractApi.AbstractHtmlField AbstractHtmlField() {
    return ABSTRACT_HTML_FIELD;
  }

  class AbstractHtmlField implements IScoutAbstractApi.AbstractHtmlField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField";
    }
  }

  IScoutAbstractApi.AbstractImageField ABSTRACT_IMAGE_FIELD = new AbstractImageField();

  @Override
  default IScoutAbstractApi.AbstractImageField AbstractImageField() {
    return ABSTRACT_IMAGE_FIELD;
  }

  class AbstractImageField implements IScoutAbstractApi.AbstractImageField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.imagefield.AbstractImageField";
    }

    @Override
    public String getConfiguredAutoFitMethodName() {
      return "getConfiguredAutoFit";
    }
  }

  IScoutAbstractApi.AbstractKeyStroke ABSTRACT_KEY_STROKE = new AbstractKeyStroke();

  @Override
  default IScoutAbstractApi.AbstractKeyStroke AbstractKeyStroke() {
    return ABSTRACT_KEY_STROKE;
  }

  class AbstractKeyStroke implements IScoutAbstractApi.AbstractKeyStroke {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke";
    }
  }

  IScoutAbstractApi.AbstractLabelField ABSTRACT_LABEL_FIELD = new AbstractLabelField();

  @Override
  default IScoutAbstractApi.AbstractLabelField AbstractLabelField() {
    return ABSTRACT_LABEL_FIELD;
  }

  class AbstractLabelField implements IScoutAbstractApi.AbstractLabelField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField";
    }
  }

  IScoutAbstractApi.AbstractListBox ABSTRACT_LIST_BOX = new AbstractListBox();

  @Override
  default IScoutAbstractApi.AbstractListBox AbstractListBox() {
    return ABSTRACT_LIST_BOX;
  }

  class AbstractListBox implements IScoutAbstractApi.AbstractListBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox";
    }
  }

  IScoutAbstractApi.AbstractLongField ABSTRACT_LONG_FIELD = new AbstractLongField();

  @Override
  default IScoutAbstractApi.AbstractLongField AbstractLongField() {
    return ABSTRACT_LONG_FIELD;
  }

  class AbstractLongField implements IScoutAbstractApi.AbstractLongField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField";
    }
  }

  IScoutAbstractApi.AbstractLookupService ABSTRACT_LOOKUP_SERVICE = new AbstractLookupService();

  @Override
  default IScoutAbstractApi.AbstractLookupService AbstractLookupService() {
    return ABSTRACT_LOOKUP_SERVICE;
  }

  class AbstractLookupService implements IScoutAbstractApi.AbstractLookupService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.services.lookup.AbstractLookupService";
    }
  }

  IScoutAbstractApi.AbstractMenu ABSTRACT_MENU = new AbstractMenu();

  @Override
  default IScoutAbstractApi.AbstractMenu AbstractMenu() {
    return ABSTRACT_MENU;
  }

  class AbstractMenu implements IScoutAbstractApi.AbstractMenu {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu";
    }

    @Override
    public String getConfiguredMenuTypesMethodName() {
      return "getConfiguredMenuTypes";
    }
  }

  IScoutAbstractApi.AbstractMode ABSTRACT_MODE = new AbstractMode();

  @Override
  default IScoutAbstractApi.AbstractMode AbstractMode() {
    return ABSTRACT_MODE;
  }

  class AbstractMode implements IScoutAbstractApi.AbstractMode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.mode.AbstractMode";
    }
  }

  IScoutAbstractApi.AbstractModeSelectorField ABSTRACT_MODE_SELECTOR_FIELD = new AbstractModeSelectorField();

  @Override
  default IScoutAbstractApi.AbstractModeSelectorField AbstractModeSelectorField() {
    return ABSTRACT_MODE_SELECTOR_FIELD;
  }

  class AbstractModeSelectorField implements IScoutAbstractApi.AbstractModeSelectorField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.modeselector.AbstractModeSelectorField";
    }
  }

  IScoutAbstractApi.AbstractOkButton ABSTRACT_OK_BUTTON = new AbstractOkButton();

  @Override
  default IScoutAbstractApi.AbstractOkButton AbstractOkButton() {
    return ABSTRACT_OK_BUTTON;
  }

  class AbstractOkButton implements IScoutAbstractApi.AbstractOkButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton";
    }
  }

  IScoutAbstractApi.AbstractPage ABSTRACT_PAGE = new AbstractPage();

  @Override
  default IScoutAbstractApi.AbstractPage AbstractPage() {
    return ABSTRACT_PAGE;
  }

  class AbstractPage implements IScoutAbstractApi.AbstractPage {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage";
    }

    @Override
    public String getConfiguredTitleMethodName() {
      return "getConfiguredTitle";
    }
  }

  IScoutAbstractApi.AbstractPageWithNodes ABSTRACT_PAGE_WITH_NODES = new AbstractPageWithNodes();

  @Override
  default IScoutAbstractApi.AbstractPageWithNodes AbstractPageWithNodes() {
    return ABSTRACT_PAGE_WITH_NODES;
  }

  class AbstractPageWithNodes implements IScoutAbstractApi.AbstractPageWithNodes {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes";
    }

    @Override
    public String execCreateChildPagesMethodName() {
      return "execCreateChildPages";
    }
  }

  IScoutAbstractApi.AbstractPageWithTable ABSTRACT_PAGE_WITH_TABLE = new AbstractPageWithTable();

  @Override
  default IScoutAbstractApi.AbstractPageWithTable AbstractPageWithTable() {
    return ABSTRACT_PAGE_WITH_TABLE;
  }

  class AbstractPageWithTable implements IScoutAbstractApi.AbstractPageWithTable {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable";
    }

    @Override
    public String importPageDataMethodName() {
      return "importPageData";
    }

    @Override
    public String execLoadDataMethodName() {
      return "execLoadData";
    }
  }

  IScoutAbstractApi.AbstractPermission ABSTRACT_PERMISSION = new AbstractPermission();

  @Override
  default IScoutAbstractApi.AbstractPermission AbstractPermission() {
    return ABSTRACT_PERMISSION;
  }

  class AbstractPermission implements IScoutAbstractApi.AbstractPermission {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.security.AbstractPermission";
    }

    @Override
    public String getAccessCheckFailedMessageMethodName() {
      return "getAccessCheckFailedMessage";
    }
  }

  IScoutAbstractApi.AbstractPropertyData ABSTRACT_PROPERTY_DATA = new AbstractPropertyData();

  @Override
  default IScoutAbstractApi.AbstractPropertyData AbstractPropertyData() {
    return ABSTRACT_PROPERTY_DATA;
  }

  class AbstractPropertyData implements IScoutAbstractApi.AbstractPropertyData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData";
    }

    @Override
    public String getValueMethodName() {
      return "getValue";
    }

    @Override
    public String setValueMethodName() {
      return "setValue";
    }
  }

  IScoutAbstractApi.AbstractProposalField ABSTRACT_PROPOSAL_FIELD = new AbstractProposalField();

  @Override
  default IScoutAbstractApi.AbstractProposalField AbstractProposalField() {
    return ABSTRACT_PROPOSAL_FIELD;
  }

  class AbstractProposalField implements IScoutAbstractApi.AbstractProposalField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField";
    }
  }

  IScoutAbstractApi.AbstractRadioButton ABSTRACT_RADIO_BUTTON = new AbstractRadioButton();

  @Override
  default IScoutAbstractApi.AbstractRadioButton AbstractRadioButton() {
    return ABSTRACT_RADIO_BUTTON;
  }

  class AbstractRadioButton implements IScoutAbstractApi.AbstractRadioButton {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton";
    }

    @Override
    public String getConfiguredRadioValueMethodName() {
      return "getConfiguredRadioValue";
    }
  }

  IScoutAbstractApi.AbstractRadioButtonGroup ABSTRACT_RADIO_BUTTON_GROUP = new AbstractRadioButtonGroup();

  @Override
  default IScoutAbstractApi.AbstractRadioButtonGroup AbstractRadioButtonGroup() {
    return ABSTRACT_RADIO_BUTTON_GROUP;
  }

  class AbstractRadioButtonGroup implements IScoutAbstractApi.AbstractRadioButtonGroup {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup";
    }
  }

  IScoutAbstractApi.AbstractSequenceBox ABSTRACT_SEQUENCE_BOX = new AbstractSequenceBox();

  @Override
  default IScoutAbstractApi.AbstractSequenceBox AbstractSequenceBox() {
    return ABSTRACT_SEQUENCE_BOX;
  }

  class AbstractSequenceBox implements IScoutAbstractApi.AbstractSequenceBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox";
    }

    @Override
    public String getConfiguredAutoCheckFromToMethodName() {
      return "getConfiguredAutoCheckFromTo";
    }
  }

  IScoutAbstractApi.AbstractSmartField ABSTRACT_SMART_FIELD = new AbstractSmartField();

  @Override
  default IScoutAbstractApi.AbstractSmartField AbstractSmartField() {
    return ABSTRACT_SMART_FIELD;
  }

  class AbstractSmartField implements IScoutAbstractApi.AbstractSmartField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField";
    }
  }

  IScoutAbstractApi.AbstractColumn ABSTRACT_COLUMN = new AbstractColumn();

  @Override
  default IScoutAbstractApi.AbstractColumn AbstractColumn() {
    return ABSTRACT_COLUMN;
  }

  class AbstractColumn implements IScoutAbstractApi.AbstractColumn {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn";
    }

    @Override
    public String getConfiguredHeaderTextMethodName() {
      return "getConfiguredHeaderText";
    }

    @Override
    public String getConfiguredWidthMethodName() {
      return "getConfiguredWidth";
    }

    @Override
    public String getConfiguredDisplayableMethodName() {
      return "getConfiguredDisplayable";
    }

    @Override
    public String getConfiguredPrimaryKeyMethodName() {
      return "getConfiguredPrimaryKey";
    }

    @Override
    public String getSelectedValueMethodName() {
      return "getSelectedValue";
    }

    @Override
    public String getSelectedValuesMethodName() {
      return "getSelectedValues";
    }
  }

  IScoutAbstractApi.AbstractStringColumn ABSTRACT_STRING_COLUMN = new AbstractStringColumn();

  @Override
  default IScoutAbstractApi.AbstractStringColumn AbstractStringColumn() {
    return ABSTRACT_STRING_COLUMN;
  }

  class AbstractStringColumn implements IScoutAbstractApi.AbstractStringColumn {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn";
    }
  }

  IScoutAbstractApi.AbstractStringConfigProperty ABSTRACT_STRING_CONFIG_PROPERTY = new AbstractStringConfigProperty();

  @Override
  default IScoutAbstractApi.AbstractStringConfigProperty AbstractStringConfigProperty() {
    return ABSTRACT_STRING_CONFIG_PROPERTY;
  }

  class AbstractStringConfigProperty implements IScoutAbstractApi.AbstractStringConfigProperty {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty";
    }
  }

  IScoutAbstractApi.AbstractStringField ABSTRACT_STRING_FIELD = new AbstractStringField();

  @Override
  default IScoutAbstractApi.AbstractStringField AbstractStringField() {
    return ABSTRACT_STRING_FIELD;
  }

  class AbstractStringField implements IScoutAbstractApi.AbstractStringField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField";
    }

    @Override
    public String getConfiguredMaxLengthMethodName() {
      return "getConfiguredMaxLength";
    }
  }

  IScoutAbstractApi.AbstractTabBox ABSTRACT_TAB_BOX = new AbstractTabBox();

  @Override
  default IScoutAbstractApi.AbstractTabBox AbstractTabBox() {
    return ABSTRACT_TAB_BOX;
  }

  class AbstractTabBox implements IScoutAbstractApi.AbstractTabBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox";
    }
  }

  IScoutAbstractApi.AbstractTable ABSTRACT_TABLE = new AbstractTable();

  @Override
  default IScoutAbstractApi.AbstractTable AbstractTable() {
    return ABSTRACT_TABLE;
  }

  class AbstractTable implements IScoutAbstractApi.AbstractTable {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.AbstractTable";
    }
  }

  IScoutAbstractApi.AbstractTableField ABSTRACT_TABLE_FIELD = new AbstractTableField();

  @Override
  default IScoutAbstractApi.AbstractTableField AbstractTableField() {
    return ABSTRACT_TABLE_FIELD;
  }

  class AbstractTableField implements IScoutAbstractApi.AbstractTableField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField";
    }
  }

  IScoutAbstractApi.AbstractTableFieldBeanData ABSTRACT_TABLE_FIELD_BEAN_DATA = new AbstractTableFieldBeanData();

  @Override
  default IScoutAbstractApi.AbstractTableFieldBeanData AbstractTableFieldBeanData() {
    return ABSTRACT_TABLE_FIELD_BEAN_DATA;
  }

  class AbstractTableFieldBeanData implements IScoutAbstractApi.AbstractTableFieldBeanData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData";
    }

    @Override
    public String rowAtMethodName() {
      return "rowAt";
    }

    @Override
    public String setRowsMethodName() {
      return "setRows";
    }

    @Override
    public String createRowMethodName() {
      return "createRow";
    }
  }

  IScoutAbstractApi.AbstractTablePageData ABSTRACT_TABLE_PAGE_DATA = new AbstractTablePageData();

  @Override
  default IScoutAbstractApi.AbstractTablePageData AbstractTablePageData() {
    return ABSTRACT_TABLE_PAGE_DATA;
  }

  class AbstractTablePageData implements IScoutAbstractApi.AbstractTablePageData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.page.AbstractTablePageData";
    }
  }

  IScoutAbstractApi.AbstractTableRowData ABSTRACT_TABLE_ROW_DATA = new AbstractTableRowData();

  @Override
  default IScoutAbstractApi.AbstractTableRowData AbstractTableRowData() {
    return ABSTRACT_TABLE_ROW_DATA;
  }

  class AbstractTableRowData implements IScoutAbstractApi.AbstractTableRowData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData";
    }
  }

  IScoutAbstractApi.AbstractTagField ABSTRACT_TAG_FIELD = new AbstractTagField();

  @Override
  default IScoutAbstractApi.AbstractTagField AbstractTagField() {
    return ABSTRACT_TAG_FIELD;
  }

  class AbstractTagField implements IScoutAbstractApi.AbstractTagField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tagfield.AbstractTagField";
    }
  }

  IScoutAbstractApi.AbstractTile ABSTRACT_TILE = new AbstractTile();

  @Override
  default IScoutAbstractApi.AbstractTile AbstractTile() {
    return ABSTRACT_TILE;
  }

  class AbstractTile implements IScoutAbstractApi.AbstractTile {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.tile.AbstractTile";
    }
  }

  IScoutAbstractApi.AbstractTileField ABSTRACT_TILE_FIELD = new AbstractTileField();

  @Override
  default IScoutAbstractApi.AbstractTileField AbstractTileField() {
    return ABSTRACT_TILE_FIELD;
  }

  class AbstractTileField implements IScoutAbstractApi.AbstractTileField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField";
    }
  }

  IScoutAbstractApi.AbstractTileGrid ABSTRACT_TILE_GRID = new AbstractTileGrid();

  @Override
  default IScoutAbstractApi.AbstractTileGrid AbstractTileGrid() {
    return ABSTRACT_TILE_GRID;
  }

  class AbstractTileGrid implements IScoutAbstractApi.AbstractTileGrid {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid";
    }
  }

  IScoutAbstractApi.AbstractTree ABSTRACT_TREE = new AbstractTree();

  @Override
  default IScoutAbstractApi.AbstractTree AbstractTree() {
    return ABSTRACT_TREE;
  }

  class AbstractTree implements IScoutAbstractApi.AbstractTree {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree";
    }
  }

  IScoutAbstractApi.AbstractTreeBox ABSTRACT_TREE_BOX = new AbstractTreeBox();

  @Override
  default IScoutAbstractApi.AbstractTreeBox AbstractTreeBox() {
    return ABSTRACT_TREE_BOX;
  }

  class AbstractTreeBox implements IScoutAbstractApi.AbstractTreeBox {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox";
    }
  }

  IScoutAbstractApi.AbstractTreeField ABSTRACT_TREE_FIELD = new AbstractTreeField();

  @Override
  default IScoutAbstractApi.AbstractTreeField AbstractTreeField() {
    return ABSTRACT_TREE_FIELD;
  }

  class AbstractTreeField implements IScoutAbstractApi.AbstractTreeField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField";
    }
  }

  IScoutAbstractApi.AbstractTreeNode ABSTRACT_TREE_NODE = new AbstractTreeNode();

  @Override
  default IScoutAbstractApi.AbstractTreeNode AbstractTreeNode() {
    return ABSTRACT_TREE_NODE;
  }

  class AbstractTreeNode implements IScoutAbstractApi.AbstractTreeNode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode";
    }

    @Override
    public String getConfiguredLeafMethodName() {
      return "getConfiguredLeaf";
    }
  }

  IScoutAbstractApi.AbstractValueField ABSTRACT_VALUE_FIELD = new AbstractValueField();

  @Override
  default IScoutAbstractApi.AbstractValueField AbstractValueField() {
    return ABSTRACT_VALUE_FIELD;
  }

  class AbstractValueField implements IScoutAbstractApi.AbstractValueField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField";
    }
  }

  IScoutAbstractApi.AbstractValueFieldData ABSTRACT_VALUE_FIELD_DATA = new AbstractValueFieldData();

  @Override
  default IScoutAbstractApi.AbstractValueFieldData AbstractValueFieldData() {
    return ABSTRACT_VALUE_FIELD_DATA;
  }

  class AbstractValueFieldData implements IScoutAbstractApi.AbstractValueFieldData {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData";
    }

    @Override
    public String getValueMethodName() {
      return "getValue";
    }

    @Override
    public String setValueMethodName() {
      return "setValue";
    }
  }

  IScoutAbstractApi.AbstractWebServiceClient ABSTRACT_WEB_SERVICE_CLIENT = new AbstractWebServiceClient();

  @Override
  default IScoutAbstractApi.AbstractWebServiceClient AbstractWebServiceClient() {
    return ABSTRACT_WEB_SERVICE_CLIENT;
  }

  class AbstractWebServiceClient implements IScoutAbstractApi.AbstractWebServiceClient {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.consumer.AbstractWebServiceClient";
    }

    @Override
    public String getConfiguredEndpointUrlPropertyMethodName() {
      return "getConfiguredEndpointUrlProperty";
    }

    @Override
    public String execInstallHandlersMethodName() {
      return "execInstallHandlers";
    }

    @Override
    public String newInvocationContextMethodName() {
      return "newInvocationContext";
    }
  }

  IScoutAbstractApi.AbstractWizard ABSTRACT_WIZARD = new AbstractWizard();

  @Override
  default IScoutAbstractApi.AbstractWizard AbstractWizard() {
    return ABSTRACT_WIZARD;
  }

  class AbstractWizard implements IScoutAbstractApi.AbstractWizard {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.wizard.AbstractWizard";
    }
  }

  IScoutAbstractApi.AbstractNumberField ABSTRACT_NUMBER_FIELD = new AbstractNumberField();

  @Override
  default IScoutAbstractApi.AbstractNumberField AbstractNumberField() {
    return ABSTRACT_NUMBER_FIELD;
  }

  class AbstractNumberField implements IScoutAbstractApi.AbstractNumberField {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField";
    }

    @Override
    public String getConfiguredMinValueMethodName() {
      return "getConfiguredMinValue";
    }

    @Override
    public String getConfiguredMaxValueMethodName() {
      return "getConfiguredMaxValue";
    }
  }

  IScoutAbstractApi.AbstractAction ABSTRACT_ACTION = new AbstractAction();

  @Override
  default IScoutAbstractApi.AbstractAction AbstractAction() {
    return ABSTRACT_ACTION;
  }

  class AbstractAction implements IScoutAbstractApi.AbstractAction {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.AbstractAction";
    }

    @Override
    public String combineKeyStrokesMethodName() {
      return "combineKeyStrokes";
    }

    @Override
    public String execActionMethodName() {
      return "execAction";
    }

    @Override
    public String execInitActionMethodName() {
      return "execInitAction";
    }

    @Override
    public String getConfiguredKeyStrokeMethodName() {
      return "getConfiguredKeyStroke";
    }

    @Override
    public String getConfiguredTextMethodName() {
      return "getConfiguredText";
    }

    @Override
    public String setVisibleGrantedMethodName() {
      return "setVisibleGranted";
    }
  }

  IScoutExtensionApi.AbstractAccordionFieldExtension ABSTRACT_ACCORDION_FIELD_EXTENSION = new AbstractAccordionFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractAccordionFieldExtension AbstractAccordionFieldExtension() {
    return ABSTRACT_ACCORDION_FIELD_EXTENSION;
  }

  class AbstractAccordionFieldExtension implements IScoutExtensionApi.AbstractAccordionFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield.AbstractAccordionFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractActionExtension ABSTRACT_ACTION_EXTENSION = new AbstractActionExtension();

  @Override
  default IScoutExtensionApi.AbstractActionExtension AbstractActionExtension() {
    return ABSTRACT_ACTION_EXTENSION;
  }

  class AbstractActionExtension implements IScoutExtensionApi.AbstractActionExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.action.AbstractActionExtension";
    }
  }

  IScoutExtensionApi.AbstractActionNodeExtension ABSTRACT_ACTION_NODE_EXTENSION = new AbstractActionNodeExtension();

  @Override
  default IScoutExtensionApi.AbstractActionNodeExtension AbstractActionNodeExtension() {
    return ABSTRACT_ACTION_NODE_EXTENSION;
  }

  class AbstractActionNodeExtension implements IScoutExtensionApi.AbstractActionNodeExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.action.tree.AbstractActionNodeExtension";
    }
  }

  IScoutExtensionApi.AbstractButtonExtension ABSTRACT_BUTTON_EXTENSION = new AbstractButtonExtension();

  @Override
  default IScoutExtensionApi.AbstractButtonExtension AbstractButtonExtension() {
    return ABSTRACT_BUTTON_EXTENSION;
  }

  class AbstractButtonExtension implements IScoutExtensionApi.AbstractButtonExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.button.AbstractButtonExtension";
    }
  }

  IScoutExtensionApi.AbstractCalendarExtension ABSTRACT_CALENDAR_EXTENSION = new AbstractCalendarExtension();

  @Override
  default IScoutExtensionApi.AbstractCalendarExtension AbstractCalendarExtension() {
    return ABSTRACT_CALENDAR_EXTENSION;
  }

  class AbstractCalendarExtension implements IScoutExtensionApi.AbstractCalendarExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.basic.calendar.AbstractCalendarExtension";
    }
  }

  IScoutExtensionApi.AbstractCalendarFieldExtension ABSTRACT_CALENDAR_FIELD_EXTENSION = new AbstractCalendarFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractCalendarFieldExtension AbstractCalendarFieldExtension() {
    return ABSTRACT_CALENDAR_FIELD_EXTENSION;
  }

  class AbstractCalendarFieldExtension implements IScoutExtensionApi.AbstractCalendarFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.calendarfield.AbstractCalendarFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractCalendarItemProviderExtension ABSTRACT_CALENDAR_ITEM_PROVIDER_EXTENSION = new AbstractCalendarItemProviderExtension();

  @Override
  default IScoutExtensionApi.AbstractCalendarItemProviderExtension AbstractCalendarItemProviderExtension() {
    return ABSTRACT_CALENDAR_ITEM_PROVIDER_EXTENSION;
  }

  class AbstractCalendarItemProviderExtension implements IScoutExtensionApi.AbstractCalendarItemProviderExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.AbstractCalendarItemProviderExtension";
    }
  }

  IScoutExtensionApi.AbstractCodeExtension ABSTRACT_CODE_EXTENSION = new AbstractCodeExtension();

  @Override
  default IScoutExtensionApi.AbstractCodeExtension AbstractCodeExtension() {
    return ABSTRACT_CODE_EXTENSION;
  }

  class AbstractCodeExtension implements IScoutExtensionApi.AbstractCodeExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeExtension";
    }
  }

  IScoutExtensionApi.AbstractCodeTypeWithGenericExtension ABSTRACT_CODE_TYPE_WITH_GENERIC_EXTENSION = new AbstractCodeTypeWithGenericExtension();

  @Override
  default IScoutExtensionApi.AbstractCodeTypeWithGenericExtension AbstractCodeTypeWithGenericExtension() {
    return ABSTRACT_CODE_TYPE_WITH_GENERIC_EXTENSION;
  }

  class AbstractCodeTypeWithGenericExtension implements IScoutExtensionApi.AbstractCodeTypeWithGenericExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.extension.services.common.code.AbstractCodeTypeWithGenericExtension";
    }
  }

  IScoutExtensionApi.AbstractComposerFieldExtension ABSTRACT_COMPOSER_FIELD_EXTENSION = new AbstractComposerFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractComposerFieldExtension AbstractComposerFieldExtension() {
    return ABSTRACT_COMPOSER_FIELD_EXTENSION;
  }

  class AbstractComposerFieldExtension implements IScoutExtensionApi.AbstractComposerFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.composer.AbstractComposerFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractCompositeFieldExtension ABSTRACT_COMPOSITE_FIELD_EXTENSION = new AbstractCompositeFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractCompositeFieldExtension AbstractCompositeFieldExtension() {
    return ABSTRACT_COMPOSITE_FIELD_EXTENSION;
  }

  class AbstractCompositeFieldExtension implements IScoutExtensionApi.AbstractCompositeFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractDataModelEntityExtension ABSTRACT_DATA_MODEL_ENTITY_EXTENSION = new AbstractDataModelEntityExtension();

  @Override
  default IScoutExtensionApi.AbstractDataModelEntityExtension AbstractDataModelEntityExtension() {
    return ABSTRACT_DATA_MODEL_ENTITY_EXTENSION;
  }

  class AbstractDataModelEntityExtension implements IScoutExtensionApi.AbstractDataModelEntityExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.extension.data.model.AbstractDataModelEntityExtension";
    }
  }

  IScoutExtensionApi.AbstractFormExtension ABSTRACT_FORM_EXTENSION = new AbstractFormExtension();

  @Override
  default IScoutExtensionApi.AbstractFormExtension AbstractFormExtension() {
    return ABSTRACT_FORM_EXTENSION;
  }

  class AbstractFormExtension implements IScoutExtensionApi.AbstractFormExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension";
    }
  }

  IScoutExtensionApi.AbstractFormFieldExtension ABSTRACT_FORM_FIELD_EXTENSION = new AbstractFormFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractFormFieldExtension AbstractFormFieldExtension() {
    return ABSTRACT_FORM_FIELD_EXTENSION;
  }

  class AbstractFormFieldExtension implements IScoutExtensionApi.AbstractFormFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractGroupBoxExtension ABSTRACT_GROUP_BOX_EXTENSION = new AbstractGroupBoxExtension();

  @Override
  default IScoutExtensionApi.AbstractGroupBoxExtension AbstractGroupBoxExtension() {
    return ABSTRACT_GROUP_BOX_EXTENSION;
  }

  class AbstractGroupBoxExtension implements IScoutExtensionApi.AbstractGroupBoxExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension";
    }
  }

  IScoutExtensionApi.AbstractGroupExtension ABSTRACT_GROUP_EXTENSION = new AbstractGroupExtension();

  @Override
  default IScoutExtensionApi.AbstractGroupExtension AbstractGroupExtension() {
    return ABSTRACT_GROUP_EXTENSION;
  }

  class AbstractGroupExtension implements IScoutExtensionApi.AbstractGroupExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.group.AbstractGroupExtension";
    }
  }

  IScoutExtensionApi.AbstractImageFieldExtension ABSTRACT_IMAGE_FIELD_EXTENSION = new AbstractImageFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractImageFieldExtension AbstractImageFieldExtension() {
    return ABSTRACT_IMAGE_FIELD_EXTENSION;
  }

  class AbstractImageFieldExtension implements IScoutExtensionApi.AbstractImageFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.AbstractImageFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractListBoxExtension ABSTRACT_LIST_BOX_EXTENSION = new AbstractListBoxExtension();

  @Override
  default IScoutExtensionApi.AbstractListBoxExtension AbstractListBoxExtension() {
    return ABSTRACT_LIST_BOX_EXTENSION;
  }

  class AbstractListBoxExtension implements IScoutExtensionApi.AbstractListBoxExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.AbstractListBoxExtension";
    }
  }

  IScoutExtensionApi.AbstractPageWithTableExtension ABSTRACT_PAGE_WITH_TABLE_EXTENSION = new AbstractPageWithTableExtension();

  @Override
  default IScoutExtensionApi.AbstractPageWithTableExtension AbstractPageWithTableExtension() {
    return ABSTRACT_PAGE_WITH_TABLE_EXTENSION;
  }

  class AbstractPageWithTableExtension implements IScoutExtensionApi.AbstractPageWithTableExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension";
    }
  }

  IScoutExtensionApi.AbstractRadioButtonGroupExtension ABSTRACT_RADIO_BUTTON_GROUP_EXTENSION = new AbstractRadioButtonGroupExtension();

  @Override
  default IScoutExtensionApi.AbstractRadioButtonGroupExtension AbstractRadioButtonGroupExtension() {
    return ABSTRACT_RADIO_BUTTON_GROUP_EXTENSION;
  }

  class AbstractRadioButtonGroupExtension implements IScoutExtensionApi.AbstractRadioButtonGroupExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroupExtension";
    }
  }

  IScoutExtensionApi.AbstractTabBoxExtension ABSTRACT_TAB_BOX_EXTENSION = new AbstractTabBoxExtension();

  @Override
  default IScoutExtensionApi.AbstractTabBoxExtension AbstractTabBoxExtension() {
    return ABSTRACT_TAB_BOX_EXTENSION;
  }

  class AbstractTabBoxExtension implements IScoutExtensionApi.AbstractTabBoxExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.tabbox.AbstractTabBoxExtension";
    }
  }

  IScoutExtensionApi.AbstractTableExtension ABSTRACT_TABLE_EXTENSION = new AbstractTableExtension();

  @Override
  default IScoutExtensionApi.AbstractTableExtension AbstractTableExtension() {
    return ABSTRACT_TABLE_EXTENSION;
  }

  class AbstractTableExtension implements IScoutExtensionApi.AbstractTableExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension";
    }
  }

  IScoutExtensionApi.AbstractTableFieldExtension ABSTRACT_TABLE_FIELD_EXTENSION = new AbstractTableFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractTableFieldExtension AbstractTableFieldExtension() {
    return ABSTRACT_TABLE_FIELD_EXTENSION;
  }

  class AbstractTableFieldExtension implements IScoutExtensionApi.AbstractTableFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.AbstractTableFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractTileFieldExtension ABSTRACT_TILE_FIELD_EXTENSION = new AbstractTileFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractTileFieldExtension AbstractTileFieldExtension() {
    return ABSTRACT_TILE_FIELD_EXTENSION;
  }

  class AbstractTileFieldExtension implements IScoutExtensionApi.AbstractTileFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.tilefield.AbstractTileFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractTileGridExtension ABSTRACT_TILE_GRID_EXTENSION = new AbstractTileGridExtension();

  @Override
  default IScoutExtensionApi.AbstractTileGridExtension AbstractTileGridExtension() {
    return ABSTRACT_TILE_GRID_EXTENSION;
  }

  class AbstractTileGridExtension implements IScoutExtensionApi.AbstractTileGridExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.tile.AbstractTileGridExtension";
    }
  }

  IScoutExtensionApi.AbstractTreeBoxExtension ABSTRACT_TREE_BOX_EXTENSION = new AbstractTreeBoxExtension();

  @Override
  default IScoutExtensionApi.AbstractTreeBoxExtension AbstractTreeBoxExtension() {
    return ABSTRACT_TREE_BOX_EXTENSION;
  }

  class AbstractTreeBoxExtension implements IScoutExtensionApi.AbstractTreeBoxExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.treebox.AbstractTreeBoxExtension";
    }
  }

  IScoutExtensionApi.AbstractTreeExtension ABSTRACT_TREE_EXTENSION = new AbstractTreeExtension();

  @Override
  default IScoutExtensionApi.AbstractTreeExtension AbstractTreeExtension() {
    return ABSTRACT_TREE_EXTENSION;
  }

  class AbstractTreeExtension implements IScoutExtensionApi.AbstractTreeExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeExtension";
    }
  }

  IScoutExtensionApi.AbstractTreeFieldExtension ABSTRACT_TREE_FIELD_EXTENSION = new AbstractTreeFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractTreeFieldExtension AbstractTreeFieldExtension() {
    return ABSTRACT_TREE_FIELD_EXTENSION;
  }

  class AbstractTreeFieldExtension implements IScoutExtensionApi.AbstractTreeFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.treefield.AbstractTreeFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractTreeNodeExtension ABSTRACT_TREE_NODE_EXTENSION = new AbstractTreeNodeExtension();

  @Override
  default IScoutExtensionApi.AbstractTreeNodeExtension AbstractTreeNodeExtension() {
    return ABSTRACT_TREE_NODE_EXTENSION;
  }

  class AbstractTreeNodeExtension implements IScoutExtensionApi.AbstractTreeNodeExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeNodeExtension";
    }
  }

  IScoutExtensionApi.AbstractValueFieldExtension ABSTRACT_VALUE_FIELD_EXTENSION = new AbstractValueFieldExtension();

  @Override
  default IScoutExtensionApi.AbstractValueFieldExtension AbstractValueFieldExtension() {
    return ABSTRACT_VALUE_FIELD_EXTENSION;
  }

  class AbstractValueFieldExtension implements IScoutExtensionApi.AbstractValueFieldExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension";
    }
  }

  IScoutExtensionApi.AbstractWizardExtension ABSTRACT_WIZARD_EXTENSION = new AbstractWizardExtension();

  @Override
  default IScoutExtensionApi.AbstractWizardExtension AbstractWizardExtension() {
    return ABSTRACT_WIZARD_EXTENSION;
  }

  class AbstractWizardExtension implements IScoutExtensionApi.AbstractWizardExtension {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.extension.ui.wizard.AbstractWizardExtension";
    }
  }

  IScoutVariousApi.ScoutTextProviderService SCOUT_TEXT_PROVIDER_SERVICE = new ScoutTextProviderService();

  @Override
  default IScoutVariousApi.ScoutTextProviderService ScoutTextProviderService() {
    return SCOUT_TEXT_PROVIDER_SERVICE;
  }

  class ScoutTextProviderService implements IScoutVariousApi.ScoutTextProviderService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.text.ScoutTextProviderService";
    }
  }

  IScoutVariousApi.CalendarMenuType CALENDAR_MENU_TYPE = new CalendarMenuType();

  @Override
  default IScoutVariousApi.CalendarMenuType CalendarMenuType() {
    return CALENDAR_MENU_TYPE;
  }

  class CalendarMenuType implements IScoutVariousApi.CalendarMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.CalendarMenuType";
    }

    @Override
    public String CalendarComponent() {
      return "CalendarComponent";
    }

    @Override
    public String EmptySpace() {
      return "EmptySpace";
    }
  }

  IScoutVariousApi.ImageFieldMenuType IMAGE_FIELD_MENU_TYPE = new ImageFieldMenuType();

  @Override
  default IScoutVariousApi.ImageFieldMenuType ImageFieldMenuType() {
    return IMAGE_FIELD_MENU_TYPE;
  }

  class ImageFieldMenuType implements IScoutVariousApi.ImageFieldMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.ImageFieldMenuType";
    }

    @Override
    public String Null() {
      return "Null";
    }

    @Override
    public String ImageId() {
      return "ImageId";
    }

    @Override
    public String ImageUrl() {
      return "ImageUrl";
    }

    @Override
    public String Image() {
      return "Image";
    }
  }

  IScoutVariousApi.TabBoxMenuType TAB_BOX_MENU_TYPE = new TabBoxMenuType();

  @Override
  default IScoutVariousApi.TabBoxMenuType TabBoxMenuType() {
    return TAB_BOX_MENU_TYPE;
  }

  class TabBoxMenuType implements IScoutVariousApi.TabBoxMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.TabBoxMenuType";
    }

    @Override
    public String Header() {
      return "Header";
    }
  }

  IScoutVariousApi.TableMenuType TABLE_MENU_TYPE = new TableMenuType();

  @Override
  default IScoutVariousApi.TableMenuType TableMenuType() {
    return TABLE_MENU_TYPE;
  }

  class TableMenuType implements IScoutVariousApi.TableMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.TableMenuType";
    }

    @Override
    public String EmptySpace() {
      return "EmptySpace";
    }

    @Override
    public String Header() {
      return "Header";
    }

    @Override
    public String MultiSelection() {
      return "MultiSelection";
    }

    @Override
    public String SingleSelection() {
      return "SingleSelection";
    }
  }

  IScoutVariousApi.TileGridMenuType TILE_GRID_MENU_TYPE = new TileGridMenuType();

  @Override
  default IScoutVariousApi.TileGridMenuType TileGridMenuType() {
    return TILE_GRID_MENU_TYPE;
  }

  class TileGridMenuType implements IScoutVariousApi.TileGridMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.TileGridMenuType";
    }

    @Override
    public String EmptySpace() {
      return "EmptySpace";
    }

    @Override
    public String SingleSelection() {
      return "SingleSelection";
    }

    @Override
    public String MultiSelection() {
      return "MultiSelection";
    }
  }

  IScoutVariousApi.TreeMenuType TREE_MENU_TYPE = new TreeMenuType();

  @Override
  default IScoutVariousApi.TreeMenuType TreeMenuType() {
    return TREE_MENU_TYPE;
  }

  class TreeMenuType implements IScoutVariousApi.TreeMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType";
    }

    @Override
    public String EmptySpace() {
      return "EmptySpace";
    }

    @Override
    public String MultiSelection() {
      return "MultiSelection";
    }

    @Override
    public String SingleSelection() {
      return "SingleSelection";
    }

    @Override
    public String Header() {
      return "Header";
    }
  }

  IScoutVariousApi.ValueFieldMenuType VALUE_FIELD_MENU_TYPE = new ValueFieldMenuType();

  @Override
  default IScoutVariousApi.ValueFieldMenuType ValueFieldMenuType() {
    return VALUE_FIELD_MENU_TYPE;
  }

  class ValueFieldMenuType implements IScoutVariousApi.ValueFieldMenuType {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType";
    }

    @Override
    public String NotNull() {
      return "NotNull";
    }

    @Override
    public String Null() {
      return "Null";
    }
  }

  IScoutVariousApi.WebService WEB_SERVICE = new WebService();

  @Override
  default IScoutVariousApi.WebService WebService() {
    return WEB_SERVICE;
  }

  class WebService implements IScoutVariousApi.WebService {
    @Override
    public String fqn() {
      return "javax.jws.WebService";
    }

    @Override
    public String nameElementName() {
      return "name";
    }

    @Override
    public String targetNamespaceElementName() {
      return "targetNamespace";
    }
  }

  IScoutVariousApi.WebServiceClient WEB_SERVICE_CLIENT = new WebServiceClient();

  @Override
  default IScoutVariousApi.WebServiceClient WebServiceClient() {
    return WEB_SERVICE_CLIENT;
  }

  class WebServiceClient implements IScoutVariousApi.WebServiceClient {
    @Override
    public String fqn() {
      return "javax.xml.ws.WebServiceClient";
    }

    @Override
    public String nameElementName() {
      return "name";
    }

    @Override
    public String targetNamespaceElementName() {
      return "targetNamespace";
    }
  }

  IScoutVariousApi.ACCESS ACCESS = new ACCESS();

  @Override
  default IScoutVariousApi.ACCESS ACCESS() {
    return ACCESS;
  }

  class ACCESS implements IScoutVariousApi.ACCESS {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.security.ACCESS";
    }

    @Override
    public String checkMethodName() {
      return "check";
    }

    @Override
    public String checkAndThrowMethodName() {
      return "checkAndThrow";
    }
  }

  IScoutVariousApi.BEANS BEANS = new BEANS();

  @Override
  default IScoutVariousApi.BEANS BEANS() {
    return BEANS;
  }

  class BEANS implements IScoutVariousApi.BEANS {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.BEANS";
    }

    @Override
    public String getMethodName() {
      return "get";
    }
  }

  IScoutVariousApi.BasicAuthenticationMethod BASIC_AUTHENTICATION_METHOD = new BasicAuthenticationMethod();

  @Override
  default IScoutVariousApi.BasicAuthenticationMethod BasicAuthenticationMethod() {
    return BASIC_AUTHENTICATION_METHOD;
  }

  class BasicAuthenticationMethod implements IScoutVariousApi.BasicAuthenticationMethod {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.provider.auth.method.BasicAuthenticationMethod";
    }
  }

  IScoutVariousApi.BinaryResource BINARY_RESOURCE = new BinaryResource();

  @Override
  default IScoutVariousApi.BinaryResource BinaryResource() {
    return BINARY_RESOURCE;
  }

  class BinaryResource implements IScoutVariousApi.BinaryResource {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.resource.BinaryResource";
    }
  }

  IScoutVariousApi.BooleanUtility BOOLEAN_UTILITY = new BooleanUtility();

  @Override
  default IScoutVariousApi.BooleanUtility BooleanUtility() {
    return BOOLEAN_UTILITY;
  }

  class BooleanUtility implements IScoutVariousApi.BooleanUtility {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.util.BooleanUtility";
    }
  }

  IScoutVariousApi.ClientTestRunner CLIENT_TEST_RUNNER = new ClientTestRunner();

  @Override
  default IScoutVariousApi.ClientTestRunner ClientTestRunner() {
    return CLIENT_TEST_RUNNER;
  }

  class ClientTestRunner implements IScoutVariousApi.ClientTestRunner {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.client.runner.ClientTestRunner";
    }
  }

  IScoutVariousApi.CollectionUtility COLLECTION_UTILITY = new CollectionUtility();

  @Override
  default IScoutVariousApi.CollectionUtility CollectionUtility() {
    return COLLECTION_UTILITY;
  }

  class CollectionUtility implements IScoutVariousApi.CollectionUtility {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.util.CollectionUtility";
    }

    @Override
    public String hashSetMethodName() {
      return "hashSet";
    }

    @Override
    public String hashSetWithoutNullElementsMethodName() {
      return "hashSetWithoutNullElements";
    }
  }

  IScoutVariousApi.ConfigFileCredentialVerifier CONFIG_FILE_CREDENTIAL_VERIFIER = new ConfigFileCredentialVerifier();

  @Override
  default IScoutVariousApi.ConfigFileCredentialVerifier ConfigFileCredentialVerifier() {
    return CONFIG_FILE_CREDENTIAL_VERIFIER;
  }

  class ConfigFileCredentialVerifier implements IScoutVariousApi.ConfigFileCredentialVerifier {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier";
    }
  }

  IScoutVariousApi.LogHandler LOG_HANDLER = new LogHandler();

  @Override
  default IScoutVariousApi.LogHandler LogHandler() {
    return LOG_HANDLER;
  }

  class LogHandler implements IScoutVariousApi.LogHandler {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.handler.LogHandler";
    }
  }

  IScoutVariousApi.LookupCall LOOKUP_CALL = new LookupCall();

  @Override
  default IScoutVariousApi.LookupCall LookupCall() {
    return LOOKUP_CALL;
  }

  class LookupCall implements IScoutVariousApi.LookupCall {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.lookup.LookupCall";
    }

    @Override
    public String getConfiguredServiceMethodName() {
      return "getConfiguredService";
    }

    @Override
    public String getDataByAllMethodName() {
      return "getDataByAll";
    }

    @Override
    public String getDataByKeyMethodName() {
      return "getDataByKey";
    }

    @Override
    public String getDataByTextMethodName() {
      return "getDataByText";
    }
  }

  IScoutVariousApi.NullClazz NULL_CLAZZ = new NullClazz();

  @Override
  default IScoutVariousApi.NullClazz NullClazz() {
    return NULL_CLAZZ;
  }

  class NullClazz implements IScoutVariousApi.NullClazz {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz$NullClazz";
    }
  }

  IScoutVariousApi.SearchFilter SEARCH_FILTER = new SearchFilter();

  @Override
  default IScoutVariousApi.SearchFilter SearchFilter() {
    return SEARCH_FILTER;
  }

  class SearchFilter implements IScoutVariousApi.SearchFilter {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter";
    }

    @Override
    public String getFormDataMethodName() {
      return "getFormData";
    }
  }

  IScoutVariousApi.ServerTestRunner SERVER_TEST_RUNNER = new ServerTestRunner();

  @Override
  default IScoutVariousApi.ServerTestRunner ServerTestRunner() {
    return SERVER_TEST_RUNNER;
  }

  class ServerTestRunner implements IScoutVariousApi.ServerTestRunner {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.testing.server.runner.ServerTestRunner";
    }
  }

  IScoutVariousApi.TEXTS TEXTS = new TEXTS();

  @Override
  default IScoutVariousApi.TEXTS TEXTS() {
    return TEXTS;
  }

  class TEXTS implements IScoutVariousApi.TEXTS {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.text.TEXTS";
    }

    @Override
    public String getMethodName() {
      return "get";
    }

    @Override
    public String getWithFallbackMethodName() {
      return "getWithFallback";
    }
  }

  IScoutVariousApi.TestEnvironmentClientSession TEST_ENVIRONMENT_CLIENT_SESSION = new TestEnvironmentClientSession();

  @Override
  default IScoutVariousApi.TestEnvironmentClientSession TestEnvironmentClientSession() {
    return TEST_ENVIRONMENT_CLIENT_SESSION;
  }

  class TestEnvironmentClientSession implements IScoutVariousApi.TestEnvironmentClientSession {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession";
    }
  }

  IScoutVariousApi.TriState TRI_STATE = new TriState();

  @Override
  default IScoutVariousApi.TriState TriState() {
    return TRI_STATE;
  }

  class TriState implements IScoutVariousApi.TriState {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.util.TriState";
    }
  }

  IScoutVariousApi.UiServlet UI_SERVLET = new UiServlet();

  @Override
  default IScoutVariousApi.UiServlet UiServlet() {
    return UI_SERVLET;
  }

  class UiServlet implements IScoutVariousApi.UiServlet {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.ui.html.UiServlet";
    }
  }

  IScoutVariousApi.UiTextContributor UI_TEXT_CONTRIBUTOR = new UiTextContributor();

  @Override
  default IScoutVariousApi.UiTextContributor UiTextContributor() {
    return UI_TEXT_CONTRIBUTOR;
  }

  class UiTextContributor implements IScoutVariousApi.UiTextContributor {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.ui.html.UiTextContributor";
    }

    @Override
    public String contributeUiTextKeysMethodName() {
      return "contributeUiTextKeys";
    }
  }

  IScoutVariousApi.VetoException VETO_EXCEPTION = new VetoException();

  @Override
  default IScoutVariousApi.VetoException VetoException() {
    return VETO_EXCEPTION;
  }

  class VetoException implements IScoutVariousApi.VetoException {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.exception.VetoException";
    }
  }

  IScoutVariousApi.WsConsumerCorrelationIdHandler WS_CONSUMER_CORRELATION_ID_HANDLER = new WsConsumerCorrelationIdHandler();

  @Override
  default IScoutVariousApi.WsConsumerCorrelationIdHandler WsConsumerCorrelationIdHandler() {
    return WS_CONSUMER_CORRELATION_ID_HANDLER;
  }

  class WsConsumerCorrelationIdHandler implements IScoutVariousApi.WsConsumerCorrelationIdHandler {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.handler.WsConsumerCorrelationIdHandler";
    }
  }

  IScoutVariousApi.WsProviderCorrelationIdHandler WS_PROVIDER_CORRELATION_ID_HANDLER = new WsProviderCorrelationIdHandler();

  @Override
  default IScoutVariousApi.WsProviderCorrelationIdHandler WsProviderCorrelationIdHandler() {
    return WS_PROVIDER_CORRELATION_ID_HANDLER;
  }

  class WsProviderCorrelationIdHandler implements IScoutVariousApi.WsProviderCorrelationIdHandler {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.server.jaxws.handler.WsProviderCorrelationIdHandler";
    }
  }

  IScoutVariousApi.Logger LOGGER = new Logger();

  @Override
  default IScoutVariousApi.Logger Logger() {
    return LOGGER;
  }

  class Logger implements IScoutVariousApi.Logger {
    @Override
    public String fqn() {
      return "org.slf4j.Logger";
    }
  }

  IScoutVariousApi.Mockito MOCKITO = new Mockito();

  @Override
  default IScoutVariousApi.Mockito Mockito() {
    return MOCKITO;
  }

  class Mockito implements IScoutVariousApi.Mockito {
    @Override
    public String fqn() {
      return "org.mockito.Mockito";
    }

    @Override
    public String whenMethodName() {
      return "when";
    }
  }

  IScoutVariousApi.ArgumentMatchers ARGUMENT_MATCHERS = new ArgumentMatchers();

  @Override
  default IScoutVariousApi.ArgumentMatchers ArgumentMatchers() {
    return ARGUMENT_MATCHERS;
  }

  class ArgumentMatchers implements IScoutVariousApi.ArgumentMatchers {
    @Override
    public String fqn() {
      return "org.mockito.ArgumentMatchers";
    }

    @Override
    public String anyMethodName() {
      return "any";
    }
  }

  IScoutVariousApi.ColumnSet COLUMN_SET = new ColumnSet();

  @Override
  default IScoutVariousApi.ColumnSet ColumnSet() {
    return COLUMN_SET;
  }

  class ColumnSet implements IScoutVariousApi.ColumnSet {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.basic.table.ColumnSet";
    }

    @Override
    public String getColumnByClassMethodName() {
      return "getColumnByClass";
    }
  }

  IScoutAnnotationApi.NlsKey NLS_KEY = new NlsKey();

  @Override
  default IScoutAnnotationApi.NlsKey NlsKey() {
    return NLS_KEY;
  }

  class NlsKey implements IScoutAnnotationApi.NlsKey {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.platform.text.NlsKey";
    }
  }

  IScoutInterfaceApi.IUiTextContributor I_UI_TEXT_CONTRIBUTOR = new IUiTextContributor();

  @Override
  default IScoutInterfaceApi.IUiTextContributor IUiTextContributor() {
    return I_UI_TEXT_CONTRIBUTOR;
  }

  class IUiTextContributor implements IScoutInterfaceApi.IUiTextContributor {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.ui.html.IUiTextContributor";
    }
  }

  IScoutVariousApi.JaxWsConstants JAX_WS_CONSTANTS = new JaxWsConstants();

  @Override
  default IScoutVariousApi.JaxWsConstants JaxWsConstants() {
    return JAX_WS_CONSTANTS;
  }

  class JaxWsConstants implements IScoutVariousApi.JaxWsConstants {
    @Override
    public String mavenPluginGroupId() {
      return "com.helger.maven";
    }

    @Override
    public String codeModelFactoryPath() {
      return "com/unquietcode/tools/jcodemodel/codemodel/1.0.3/codemodel-1.0.3.jar";
    }

    @Override
    public String servletFactoryPath() {
      return "jakarta/servlet/jakarta.servlet-api/4.0.3/jakarta.servlet-api-4.0.3.jar";
    }

    @Override
    public String slf4jFactoryPath() {
      return "org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar";
    }

    @Override
    public String jwsFactoryPath() {
      return "jakarta/jws/jakarta.jws-api/1.1.1/jakarta.jws-api-1.1.1.jar";
    }
  }

  IScoutVariousApi.DoValue DO_VALUE = new DoValue();

  @Override
  default IScoutVariousApi.DoValue DoValue() {
    return DO_VALUE;
  }

  class DoValue implements IScoutVariousApi.DoValue {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoValue";
    }
  }

  IScoutVariousApi.DoList DO_LIST = new DoList();

  @Override
  default IScoutVariousApi.DoList DoList() {
    return DO_LIST;
  }

  class DoList implements IScoutVariousApi.DoList {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoList";
    }
  }

  @Override
  default String DoUpdateAllMethodName() {
    return "updateAll";
  }

  IScoutVariousApi.DoNode DO_NODE = new DoNode();

  @Override
  default IScoutVariousApi.DoNode DoNode() {
    return DO_NODE;
  }

  class DoNode implements IScoutVariousApi.DoNode {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoNode";
    }

    @Override
    public int valueTypeParamIndex() {
      return 0;
    }

    @Override
    public String getMethodName() {
      return "get";
    }

    @Override
    public String setMethodName() {
      return "set";
    }
  }

  IScoutAnnotationApi.IgnoreConvenienceMethodGeneration IGNORE_CONVENIENCE_METHOD_GENERATION = new IgnoreConvenienceMethodGeneration();

  @Override
  default IScoutAnnotationApi.IgnoreConvenienceMethodGeneration IgnoreConvenienceMethodGeneration() {
    return IGNORE_CONVENIENCE_METHOD_GENERATION;
  }

  class IgnoreConvenienceMethodGeneration implements IScoutAnnotationApi.IgnoreConvenienceMethodGeneration {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.IgnoreConvenienceMethodGeneration";
    }
  }

  IScoutAnnotationApi.TypeVersion TYPE_VERSION = new TypeVersion();

  @Override
  default IScoutAnnotationApi.TypeVersion TypeVersion() {
    return TYPE_VERSION;
  }

  class TypeVersion implements IScoutAnnotationApi.TypeVersion {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.TypeVersion";
    }

    @Override
    public String valueElementName() {
      return "value";
    }

    @Override
    public void buildValue(IExpressionBuilder<?> builder, String typeVersion) {
      builder.stringLiteral(typeVersion);
    }
  }

  IScoutAnnotationApi.TypeName TYPE_NAME = new TypeName();

  @Override
  default IScoutAnnotationApi.TypeName TypeName() {
    return TYPE_NAME;
  }

  class TypeName implements IScoutAnnotationApi.TypeName {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.TypeName";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutAnnotationApi.ValueFormat VALUE_FORMAT = new ValueFormat();

  @Override
  default IScoutAnnotationApi.ValueFormat ValueFormat() {
    return VALUE_FORMAT;
  }

  class ValueFormat implements IScoutAnnotationApi.ValueFormat {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.ValueFormat";
    }

    @Override
    public String patternElementName() {
      return "pattern";
    }
  }

  IScoutAnnotationApi.AttributeName ATTRIBUTE_NAME = new AttributeName();

  @Override
  default IScoutAnnotationApi.AttributeName AttributeName() {
    return ATTRIBUTE_NAME;
  }

  class AttributeName implements IScoutAnnotationApi.AttributeName {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.AttributeName";
    }

    @Override
    public String valueElementName() {
      return "value";
    }
  }

  IScoutVariousApi.MessageBoxes MESSAGE_BOXES = new MessageBoxes();

  @Override
  default IScoutVariousApi.MessageBoxes MessageBoxes() {
    return MESSAGE_BOXES;
  }

  class MessageBoxes implements IScoutVariousApi.MessageBoxes {

    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes";
    }

    @Override
    public String createOkMethodName() {
      return "createOk";
    }

    @Override
    public String showDeleteConfirmationMessageMethodName() {
      return "showDeleteConfirmationMessage";
    }
  }

  IScoutInterfaceApi.DoEntity DO_ENTITY = new DoEntity();

  @Override
  default IScoutInterfaceApi.DoEntity DoEntity() {
    return DO_ENTITY;
  }

  class DoEntity implements IScoutInterfaceApi.DoEntity {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.dataobject.DoEntity";
    }

    @Override
    public String doValueMethodName() {
      return "doValue";
    }

    @Override
    public String doListMethodName() {
      return "doList";
    }
  }
}
