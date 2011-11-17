package org.eclipse.scout.sdk.workspace.type;

import java.util.Comparator;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class ScoutTypeComparators extends TypeComparators {

  public static Comparator<IType> getOrderAnnotationComparator() {
    return new Comparator<IType>() {
      @Override
      public int compare(IType t1, IType t2) {
        Double val1 = getOrderAnnotation(t1);
        Double val2 = getOrderAnnotation(t2);
        if (val1 == null && val2 == null) {
          return t1.getElementName().compareTo(t2.getElementName());
        }
        else if (val1 == null) {
          return -1;
        }
        else if (val2 == null) {
          return 1;
        }
        else if (val1.equals(val2)) {
          return t1.getElementName().compareTo(t2.getElementName());
        }
        else {
          return val1.compareTo(val2);
        }
      }

      private Double getOrderAnnotation(IType type) {
        Double sortNo = null;
        if (TypeUtility.exists(type)) {
          try {
            IAnnotation annotation = type.getAnnotation(NamingUtility.getSimpleName(RuntimeClasses.Order));
            if (annotation.exists()) {
              IMemberValuePair[] memberValues = annotation.getMemberValuePairs();
              for (IMemberValuePair p : memberValues) {
                if ("value".equals(p.getMemberName())) {
                  switch (p.getValueKind()) {
                    case IMemberValuePair.K_DOUBLE:
                      sortNo = ((Double) p.getValue()).doubleValue();
                      break;
                    case IMemberValuePair.K_FLOAT:
                      sortNo = ((Float) p.getValue()).doubleValue();
                      break;
                    case IMemberValuePair.K_INT:
                      sortNo = ((Integer) p.getValue()).doubleValue();
                      break;
                    default:
                      ScoutSdk.logError("could not find order annotation of '" + type.getFullyQualifiedName() + "'. ");
                      break;
                  }
                  break;
                }
              }
            }
            return sortNo;
          }
          catch (Throwable t) {
            ScoutSdk.logWarning("no @Order annotation found on '" + type.getFullyQualifiedName() + "'.", t);
          }
        }
        return sortNo;
      }
    };
  }
}
