package org.eclipse.scout.sdk.util.typecache;

import org.eclipse.jdt.core.IType;

public interface ITypeCache {
  boolean existsType(String fullyQualifiedName);

  IType getType(String fullyQualifiedName);

  IType[] getTypes(String fullyQualifiedName);

  void dispose();
}
