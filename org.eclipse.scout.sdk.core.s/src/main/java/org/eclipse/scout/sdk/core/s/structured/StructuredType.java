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
package org.eclipse.scout.sdk.core.s.structured;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.model.api.Flags.isDeprecated;
import static org.eclipse.scout.sdk.core.model.api.Flags.isEnum;
import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.s.util.ScoutTypeComparators.orderAnnotationComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.util.ScoutTypeComparators;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Strings;

public class StructuredType implements IStructuredType {

  private static final Pattern PROPERTY_BEAN_REGEX = Pattern.compile("^(get|set|is|add|remove|clear|delete)(.*)$");
  private static final Pattern START_HANDLER_REGEX = Pattern.compile("^start(.*)$");
  private static final Pattern METHOD_INNER_TYPE_GETTER_REGEX = Pattern.compile("^get(.*)$");

  private static final Predicate<IType> CLASS_FILTER = type -> {
    if (Strings.isBlank(type.elementName())) {
      return false; // anonymous type
    }
    var flags = type.flags();
    return !isAbstract(flags) && !isInterface(flags) && !isDeprecated(flags);
  };

  private final IType m_type;
  private final IScoutApi m_scoutApi;
  private final EnumSet<Categories> m_enabledCategories;
  private final EnumSet<Categories> m_visitedCategories;
  private final Map<Categories, List<? extends IJavaElement>> m_elements;

  protected StructuredType(IType type, EnumSet<Categories> enabledCategories) {
    m_type = type;
    m_scoutApi = type.javaEnvironment().api(IScoutApi.class).orElse(null);
    m_enabledCategories = EnumSet.copyOf(enabledCategories);
    m_visitedCategories = EnumSet.noneOf(Categories.class);
    m_elements = new EnumMap<>(Categories.class);

    // initially put all into unknown categories
    var fields = type.fields().stream().collect(toList());
    var enums = type.innerTypes().withFlags(Flags.AccEnum).stream().collect(toList());
    var methods = type.methods().stream().collect(toList());
    var types = type.innerTypes().stream().filter(element -> !isEnum(element.flags())).collect(toList());

    m_elements.put(Categories.FIELD_UNKNOWN, fields);
    m_elements.put(Categories.ENUM, enums);
    m_elements.put(Categories.METHOD_UNCATEGORIZED, methods);
    m_elements.put(Categories.TYPE_UNCATEGORIZED, types);
  }

  public Optional<IScoutApi> scoutApi() {
    return Optional.ofNullable(m_scoutApi);
  }

  public static IStructuredType of(IType type) {
    var enabled = EnumSet.of(Categories.FIELD_LOGGER, Categories.FIELD_STATIC, Categories.FIELD_MEMBER, Categories.FIELD_UNKNOWN, Categories.METHOD_CONSTRUCTOR, Categories.METHOD_CONFIG_PROPERTY,
        Categories.METHOD_CONFIG_EXEC, Categories.METHOD_FORM_DATA_BEAN, Categories.METHOD_OVERRIDDEN, Categories.METHOD_START_HANDLER, Categories.METHOD_INNER_TYPE_GETTER, Categories.METHOD_LOCAL_BEAN, Categories.METHOD_UNCATEGORIZED,
        Categories.TYPE_FORM_FIELD, Categories.TYPE_COLUMN, Categories.TYPE_CODE, Categories.TYPE_FORM, Categories.TYPE_TABLE, Categories.TYPE_TREE, Categories.TYPE_CALENDAR,
        Categories.TYPE_CALENDAR_ITEM_PROVIDER, Categories.TYPE_WIZARD, Categories.TYPE_WIZARD_STEP, Categories.TYPE_MENU, Categories.TYPE_VIEW_BUTTON, Categories.TYPE_KEYSTROKE,
        Categories.TYPE_COMPOSER_ATTRIBUTE, Categories.TYPE_COMPOSER_ENTRY, Categories.TYPE_FORM_HANDLER, Categories.TYPE_UNCATEGORIZED);
    return new StructuredType(type, enabled);
  }

  private static CompositeObject createPropertyMethodKey(IMethod method) {
    if (method != null) {
      var matcher = PROPERTY_BEAN_REGEX.matcher(method.elementName());
      if (matcher.find()) {
        var getSetOrder = 20;
        if (PropertyBean.GETTER_PREFIX.equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 1;
        }
        else if (PropertyBean.GETTER_BOOL_PREFIX.equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 2;
        }
        else if (PropertyBean.SETTER_PREFIX.equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 3;
        }
        else if ("add".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 4;
        }
        else if ("remove".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 5;
        }
        else if ("clear".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 6;
        }
        else if ("delete".equalsIgnoreCase(matcher.group(1))) {
          getSetOrder = 7;
        }
        var propName = matcher.group(2);
        return new CompositeObject(propName, getSetOrder, method.elementName(), method.parameters().stream().count(), method);
      }
    }
    return null;
  }

  private static IMethod getOverwrittenMethod(IMethod method) {
    var refSig = method.identifier();
    return method.requireDeclaringType().methods()
        .withSuperClasses(true).stream()
        .filter(element -> !method.equals(element) && refSig.equals(element.identifier()))
        .findAny()
        .orElse(null);
  }

  protected static CompositeObject createConstructorKey(Collection<IMethodParameter> list) {
    if (list == null) {
      return new CompositeObject(0, "");
    }
    var b = list.stream().map(p -> p.dataType().name()).collect(joining());
    return new CompositeObject(list.size(), b);
  }

  /**
   * Creates and gets a {@link Predicate} that evaluates to {@code true} for all {@link IType}s that are
   * {@code instanceof} the given fully qualified name.
   *
   * @param type
   *          The fully qualified type name the candidates must be {@code instanceof}.
   * @return The created {@link Predicate}
   */
  public static Predicate<IType> instanceOf(String type) {
    return candidate -> candidate.isInstanceOf(type);
  }

  public IType getType() {
    return m_type;
  }

  protected List<? extends IJavaElement> getElementsInternal(Categories category) {
    cache(category);
    return m_elements.get(category);
  }

  @Override
  public List<IJavaElement> getElements(Categories category) {
    return new ArrayList<>(getElementsInternal(category));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IJavaElement> List<T> getElements(Categories category, Class<T> clazz) {
    var elements = getElementsInternal(category);
    if (elements == null) {
      return emptyList();
    }
    return elements.stream()
        .map(e -> (T) e)
        .collect(toList());
  }

  @Override
  public IJavaElement getSiblingMethodFieldGetter(String methodName) {
    return getSibling(methodName, Categories.METHOD_INNER_TYPE_GETTER);
  }

  protected IJavaElement getSibling(String siblingName, Categories... categories) {
    for (var cat : categories) {
      var references = getElementsInternal(cat);
      if (references != null && !references.isEmpty()) {
        for (IJavaElement reference : references) {
          if (reference.elementName().compareTo(siblingName) > 0) {
            return reference;
          }
        }
        return references.get(references.size() - 1);
      }
      var sibling = getSibling(cat);
      if (sibling != null) {
        return sibling;
      }
    }

    return null;
  }

  @Override
  public IJavaElement getSibling(Categories category) {
    var search = false;
    var methodCategories = Categories.values();
    for (var methodCategory : methodCategories) {
      cache(methodCategory);
      if (search) {
        var elements = getElementsInternal(methodCategory);
        if (elements != null && !elements.isEmpty()) {
          return elements.get(0);
        }
      }
      else if (methodCategory == category) {
        search = true;
      }
    }
    return null;
  }

  protected final void cache(Categories category) {
    if (m_enabledCategories.contains(category) && !m_visitedCategories.contains(category)) {
      List<IJavaElement> unknownMethods = new ArrayList<>(m_elements.get(Categories.METHOD_UNCATEGORIZED));
      List<IJavaElement> unknownTypes = new ArrayList<>(m_elements.get(Categories.TYPE_UNCATEGORIZED));
      switch (category) {
        case FIELD_LOGGER:
        case FIELD_STATIC:
        case FIELD_MEMBER:
          visitFields(new ArrayList<>(m_elements.get(Categories.FIELD_UNKNOWN)));
          m_visitedCategories.add(Categories.FIELD_LOGGER);
          m_visitedCategories.add(Categories.FIELD_STATIC);
          m_visitedCategories.add(Categories.FIELD_MEMBER);
          break;
        case METHOD_CONSTRUCTOR:
          visitMethodConstructors(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_CONSTRUCTOR);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_CONFIG_PROPERTY:
          visitMethodConfigProperty(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_CONFIG_PROPERTY);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_CONFIG_EXEC:
          visitMethodConfigExec(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_CONFIG_EXEC);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_FORM_DATA_BEAN:
          m_visitedCategories.add(Categories.METHOD_FORM_DATA_BEAN);
          visitMethodFormDataBean(unknownMethods);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_OVERRIDDEN:
          visitMethodOverridden(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_OVERRIDDEN);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_START_HANDLER:
          visitMethodStartHandler(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_START_HANDLER);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_INNER_TYPE_GETTER:
          visitMethodInnerTypeGetter(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_INNER_TYPE_GETTER);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case METHOD_LOCAL_BEAN:
          visitMethodLocalBean(unknownMethods);
          m_visitedCategories.add(Categories.METHOD_LOCAL_BEAN);
          m_elements.put(Categories.METHOD_UNCATEGORIZED, unknownMethods);
          break;
        case TYPE_FORM_FIELD:
          visitTypeFormFields(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_FORM_FIELD);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_COLUMN:
          visitTypeColumns(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_COLUMN);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_CODE:
          visitTypeCodes(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_CODE);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_FORM:
          visitTypeForms(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_FORM);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_TABLE:
          visitTypeTables(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_TABLE);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_TREE:
          visitTypeTrees(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_TREE);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_CALENDAR:
          visitTypeCalendar(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_CALENDAR);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_CALENDAR_ITEM_PROVIDER:
          visitTypeCalendarItemProvider(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_CALENDAR_ITEM_PROVIDER);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_WIZARD:
          visitTypeWizards(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_WIZARD);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_WIZARD_STEP:
          visitTypeWizardSteps(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_WIZARD_STEP);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_MENU:
          visitTypeMenus(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_MENU);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_VIEW_BUTTON:
          visitTypeViewButtons(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_VIEW_BUTTON);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_KEYSTROKE:
          visitTypeKeystrokes(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_KEYSTROKE);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_COMPOSER_ATTRIBUTE:
          visitTypeComposerAttribute(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_COMPOSER_ATTRIBUTE);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_COMPOSER_ENTRY:
          visitTypeDataModelEntry(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_COMPOSER_ENTRY);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        case TYPE_FORM_HANDLER:
          visitTypeFormHandlers(unknownTypes);
          m_visitedCategories.add(Categories.TYPE_FORM_HANDLER);
          m_elements.put(Categories.TYPE_UNCATEGORIZED, unknownTypes);
          break;
        default:
          break;
      }
    }
  }

  /**
   * can be overwritten. Overwrites must ensure to super call after processing the working set.
   */
  protected void visitFields(List<IJavaElement> workingSet) {
    List<IJavaElement> loggers = new ArrayList<>(2);
    List<IJavaElement> statics = new ArrayList<>();
    List<IJavaElement> members = new ArrayList<>();

    var logger = scoutApi().map(IScoutApi::Logger).map(IClassNameSupplier::fqn);
    for (var it = workingSet.iterator(); it.hasNext();) {
      var f = (IField) it.next();
      // static
      if ((f.flags() & Flags.AccStatic) != 0) {
        var fieldDataType = f.dataType().reference();
        if (logger.isPresent() && logger.get().equals(fieldDataType)) {
          loggers.add(f);
        }
        else {
          statics.add(f);
        }
      }
      else {
        members.add(f);
      }
      it.remove();
    }
    m_elements.put(Categories.FIELD_LOGGER, loggers);
    m_elements.put(Categories.FIELD_STATIC, statics);
    m_elements.put(Categories.FIELD_MEMBER, members);
    m_elements.put(Categories.FIELD_UNKNOWN, workingSet);
  }

  protected void visitMethodConstructors(Iterable<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> constructors = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = (IMethod) it.next();
      if (method.isConstructor()) {
        var key = createConstructorKey(method.parameters().stream().collect(toList()));
        constructors.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_CONSTRUCTOR, new ArrayList<>(constructors.values()));
  }

  protected void visitMethodConfigExec(Iterable<IJavaElement> workingSet) {
    visitMethodWithPrefix(workingSet, "exec", Categories.METHOD_CONFIG_EXEC);
  }

  protected void visitMethodConfigProperty(Iterable<IJavaElement> workingSet) {
    visitMethodWithPrefix(workingSet, "getConfigured", Categories.METHOD_CONFIG_PROPERTY);
  }

  protected void visitMethodWithPrefix(Iterable<IJavaElement> workingSet, String prefix, Categories category) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = (IMethod) it.next();
      var visitedMethod = method;
      while (visitedMethod != null) {
        var methodName = visitedMethod.elementName();
        if (methodName.length() > prefix.length() && methodName.startsWith(prefix)) {
          var key = new CompositeObject(method.elementName(), method.parameters().stream().count(), method);
          methods.put(key, method);
          it.remove();
          break;
        }
        visitedMethod = getOverwrittenMethod(visitedMethod);
      }
    }
    m_elements.put(category, new ArrayList<>(methods.values()));
  }

  protected void visitMethodFormDataBean(Iterable<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    var scoutApi = scoutApi();
    if (scoutApi.isPresent()) {
      var api = scoutApi.get();
      var formDataFqn = api.FormData().fqn();
      for (var it = workingSet.iterator(); it.hasNext();) {
        var method = (IMethod) it.next();
        if (method.annotations().withName(formDataFqn).existsAny()) {
          var methodKey = createPropertyMethodKey(method);
          if (methodKey != null) {
            methods.put(methodKey, method);
            it.remove();
          }
          else {
            SdkLog.warning("could not parse property method '{}'.", method.elementName());
          }
        }
      }
    }
    m_elements.put(Categories.METHOD_FORM_DATA_BEAN, new ArrayList<>(methods.values()));
  }

  protected void visitMethodOverridden(Iterable<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> overriddenMethods = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = (IMethod) it.next();
      if (getOverwrittenMethod(method) != null) {
        var key = new CompositeObject(method.elementName(), method.parameters().stream().count(), method);
        overriddenMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_OVERRIDDEN, new ArrayList<>(overriddenMethods.values()));
  }

  protected void visitMethodStartHandler(Iterable<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> startHandlerMethods = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = (IMethod) it.next();
      var matcher = START_HANDLER_REGEX.matcher(method.elementName());
      if (matcher.find()) {
        var fieldName = matcher.group(1);
        if (getType().innerTypes().withRecursiveInnerTypes(true).withSimpleName(fieldName + ISdkConstants.SUFFIX_FORM_HANDLER).existsAny()) {
          var key = new CompositeObject(method.elementName(), method.parameters().stream().count(), method);
          startHandlerMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(Categories.METHOD_START_HANDLER, new ArrayList<>(startHandlerMethods.values()));
  }

  protected void visitMethodInnerTypeGetter(Iterable<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> fieldGetterMethods = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = (IMethod) it.next();
      var matcher = METHOD_INNER_TYPE_GETTER_REGEX.matcher(method.elementName());
      if (matcher.find()) {
        var fieldName = matcher.group(1);
        if (getType().innerTypes().withRecursiveInnerTypes(true).withSimpleName(fieldName).existsAny()) {
          var key = new CompositeObject(method.elementName(), method.parameters().stream().count(), method);
          fieldGetterMethods.put(key, method);
          it.remove();
        }
      }
    }
    m_elements.put(Categories.METHOD_INNER_TYPE_GETTER, new ArrayList<>(fieldGetterMethods.values()));
  }

  protected void visitMethodLocalBean(Iterable<IJavaElement> workingSet) {
    Map<CompositeObject, IJavaElement> localPropertyMethods = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = (IMethod) it.next();
      var key = createPropertyMethodKey(method);
      if (key != null) {
        localPropertyMethods.put(key, method);
        it.remove();
      }
    }
    m_elements.put(Categories.METHOD_LOCAL_BEAN, new ArrayList<>(localPropertyMethods.values()));
  }

  protected void visitMethodUncategorized(Iterable<IMethod> workingSet) {
    Map<CompositeObject, IJavaElement> methods = new TreeMap<>();
    for (var it = workingSet.iterator(); it.hasNext();) {
      var method = it.next();
      var key = new CompositeObject(method.elementName(), method.parameters().stream().count(), method);
      methods.put(key, method);
      it.remove();
    }
    m_elements.put(Categories.METHOD_UNCATEGORIZED, new ArrayList<>(methods.values()));
  }

  protected void visitTypeFormFields(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IFormField, Categories.TYPE_FORM_FIELD, orderAnnotationComparator(false));
  }

  protected void visitTypeColumns(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IColumn, Categories.TYPE_COLUMN, orderAnnotationComparator(false));
  }

  protected void visitTypeCodes(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::ICode, Categories.TYPE_CODE, orderAnnotationComparator(false));
  }

  protected void visitTypeForms(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IForm, Categories.TYPE_FORM, orderAnnotationComparator(false));
  }

  protected void visitTypeTables(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::ITable, Categories.TYPE_TABLE, orderAnnotationComparator(false));
  }

  protected void visitTypeTrees(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::ITree, Categories.TYPE_TREE, orderAnnotationComparator(false));
  }

  protected void visitTypeCalendar(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::ICalendar, Categories.TYPE_CALENDAR, orderAnnotationComparator(false));
  }

  protected void visitTypeCalendarItemProvider(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::ICalendarItemProvider, Categories.TYPE_CALENDAR_ITEM_PROVIDER, orderAnnotationComparator(false));
  }

  protected void visitTypeWizards(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IWizard, Categories.TYPE_WIZARD, orderAnnotationComparator(false));
  }

  protected void visitTypeWizardSteps(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IWizardStep, Categories.TYPE_WIZARD_STEP, orderAnnotationComparator(false));
  }

  protected void visitTypeMenus(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IMenu, Categories.TYPE_MENU, orderAnnotationComparator(false));
  }

  protected void visitTypeViewButtons(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IViewButton, Categories.TYPE_VIEW_BUTTON, orderAnnotationComparator(false));
  }

  protected void visitTypeKeystrokes(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IKeyStroke, Categories.TYPE_KEYSTROKE, ScoutTypeComparators.BY_NAME);
  }

  protected void visitTypeComposerAttribute(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IDataModelAttribute, Categories.TYPE_COMPOSER_ATTRIBUTE, ScoutTypeComparators.BY_NAME);
  }

  protected void visitTypeDataModelEntry(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IDataModelEntity, Categories.TYPE_COMPOSER_ENTRY, ScoutTypeComparators.BY_NAME);
  }

  protected void visitTypeFormHandlers(Iterable<IJavaElement> workingSet) {
    consumeType(workingSet, IScoutApi::IFormHandler, Categories.TYPE_FORM_HANDLER, ScoutTypeComparators.BY_NAME);
  }

  protected void consumeType(Iterable<IJavaElement> workingSet, Function<IScoutApi, IClassNameSupplier> typeInterface, Categories category, Comparator<IType> comparator) {
    Set<IType> types = new TreeSet<>(comparator);
    var scoutApi = scoutApi().orElse(null);
    if (scoutApi != null) {
      var fqn = typeInterface.apply(scoutApi).fqn();
      var filter = CLASS_FILTER.and(instanceOf(fqn));
      var it = workingSet.iterator();
      while (it.hasNext()) {
        var candidate = (IType) it.next();
        if (filter.test(candidate)) {
          types.add(candidate);
          it.remove();
        }
      }
    }
    m_elements.put(category, new ArrayList<>(types));
  }
}
