package org.eclipse.scout.sdk.util.typecache;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;

public interface IJavaResourceChangedEmitter {
  void addInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener);

  void removeInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener);

  void addJavaResourceChangedListener(IJavaResourceChangedListener listener);

  void removeJavaResourceChangedListener(IJavaResourceChangedListener listener);

  void addMethodChangedListener(IType type, IJavaResourceChangedListener listener);

  void removeMethodChangedListener(IType type, IJavaResourceChangedListener listener);

  void dispose();
}
