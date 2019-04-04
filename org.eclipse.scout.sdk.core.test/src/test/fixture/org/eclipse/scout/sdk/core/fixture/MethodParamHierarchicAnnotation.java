package org.eclipse.scout.sdk.core.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class MethodParamHierarchicAnnotation {

  @Target(ElementType.PARAMETER)
  public @interface ParamMarkerAnnotation {

  }

  @Target(ElementType.PARAMETER)
  public @interface ParamAnnotationWithValue {
    String msg();
  }

  public interface ParameterAnnotationIfc {
    void methodWithAnnotatedParams(String firstParam, @ParamMarkerAnnotation int secondParam);
  }

  public static class ParamAnnotationSuperClass implements ParameterAnnotationIfc {
    @Override
    public void methodWithAnnotatedParams(@ParamMarkerAnnotation @ParamAnnotationWithValue(msg = "test") String firstParam, int secondParam) {
      // nop
    }
  }

  public static class ParamAnnotationChildClass extends ParamAnnotationSuperClass {
    @Override
    public void methodWithAnnotatedParams(String firstParam, int secondParam) {
      // nop
    }
  }
}
