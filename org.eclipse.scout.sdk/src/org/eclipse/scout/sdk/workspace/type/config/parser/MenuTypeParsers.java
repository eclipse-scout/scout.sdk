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
package org.eclipse.scout.sdk.workspace.type.config.parser;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link MenuTypeParsers}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 03.06.2014
 */
public class MenuTypeParsers implements IPropertySourceParser<MenuTypesConfig> {

  @Override
  public MenuTypesConfig parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    ICachedTypeHierarchy menuTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(IRuntimeClasses.IMenuType));
    Set<IType> menuTypeEnums = menuTypeHierarchy.getAllTypes(TypeFilters.getEnumTypesFilter(), TypeComparators.getTypeNameComparator());
    MenuTypesConfig ret = new MenuTypesConfig();
    for (IType menuType : menuTypeEnums) {
      String fqn = menuType.getFullyQualifiedName('.');
      String simpleName = Signature.getSimpleName(fqn);
      String qualifier = Signature.getQualifier(fqn);

      Pattern p = Pattern.compile("(?:" + qualifier.replace(".", "\\.") + "\\.)?" + simpleName + "\\.([a-zA-Z0-9_]+)");
      Matcher m = p.matcher(source);
      while (m.find()) {
        String curVal = m.group(1);
        ret.add(menuType, curVal);
      }
    }

    return ret;
  }

  @Override
  public String formatSourceValue(MenuTypesConfig value, String lineDelimiter, IImportValidator validator) throws CoreException {
    StringBuilder source = new StringBuilder();
    if (validator != null) {
      String iMenuTypeName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.IMenuType));
      String collUtilityName = validator.getTypeName(SignatureCache.createTypeSignature(IRuntimeClasses.CollectionUtility));
      source.append(collUtilityName).append(".<").append(iMenuTypeName).append("> hashSet(");
    }

    boolean itemFound = false;
    Map<IType, Set<String>> all = value.getAll();
    for (Entry<IType, Set<String>> entry : all.entrySet()) {
      for (String enumVal : entry.getValue()) {
        itemFound = true;
        String typeName = null;
        if (validator != null) {
          typeName = validator.getTypeName(SignatureCache.createTypeSignature(entry.getKey().getFullyQualifiedName()));
        }
        else {
          typeName = entry.getKey().getElementName();
        }
        source.append(typeName).append('.').append(enumVal).append(", ");
      }
    }

    if (itemFound) {
      source.delete(source.length() - 2, source.length());
    }
    if (validator != null) {
      source.append(")");
    }

    return source.toString();
  }

}
