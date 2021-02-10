package org.eclipse.scout.sdk.doc;

import static java.util.stream.Collectors.joining;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;

@SuppressWarnings("MethodMayBeStatic")
public class PrintMethodParamTypes {

  public static void main(String[] args) {
    new PrintMethodParamTypes().printMethodParamTypes();
  }

  // tag::printMethodParamTypes[]
  public void printMethodParamTypes() {
    new EmptyJavaEnvironmentFactory().accept(this::printMethodParamTypes); // <1>
  }

  public void printMethodParamTypes(IJavaEnvironment javaEnvironment) {
    var methodName = "getChars";
    var argTypeNames = javaEnvironment
        .requireType(String.class.getName())
        .methods()
        .withName(methodName) // <2>
        .withFlags(Flags.AccPublic) // <3>
        .first().orElseThrow(() -> newFail("Cannot find method '{}' in {}.",
            methodName, String.class))
        .parameters().stream() // <4>
        .map(IMethodParameter::dataType) // <5>
        .map(IType::reference) // <6>
        .collect(joining(", "));
    SdkLog.warning(argTypeNames);
  }
  // end::printMethodParamTypes[]
}
