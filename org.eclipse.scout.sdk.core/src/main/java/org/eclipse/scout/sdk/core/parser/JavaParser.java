package org.eclipse.scout.sdk.core.parser;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.scout.sdk.core.model.CompilationUnit;
import org.eclipse.scout.sdk.core.model.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.JavaModelUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.CoreUtils;

public class JavaParser implements ILookupEnvironment {

  private final AstCompiler m_compiler;
  private final boolean m_allowErrors;

  public static ILookupEnvironment create(List<File> classpath, boolean allowErrors) {
    return new JavaParser(classpath, allowErrors);
  }

  private JavaParser(List<File> classpath, boolean allowErrors) {
    m_allowErrors = allowErrors;
    Validate.notNull(classpath);
    INameEnvironment cp = createClasspath(classpath);
    m_compiler = new AstCompiler(cp);
  }

  public void reset() {
    m_compiler.reset();
  }

  @Override
  public IType findType(String fqn) {
    String[] parts = splitToPrimaryType(fqn);
    ReferenceBinding binding = findTypeInternal(parts[0], m_allowErrors);
    return getType(binding, parts);
  }

  @Override
  public boolean existsType(String fqn) {
    String[] parts = splitToPrimaryType(fqn);
    ReferenceBinding binding = findTypeInternal(parts[0], true);
    if (parts.length < 2) {
      // no inner types: directly return answer
      return binding != null;
    }
    return getType(binding, parts) != null;
  }

  public ICompilationUnit parse(File javaFile, Charset fileCharset) {
    Validate.notNull(javaFile);
    FileCompilationUnit cu = new FileCompilationUnit(javaFile, fileCharset);
    return parse(cu);
  }

  public ICompilationUnit parse(String javaSource, String mainTypeName, String fileName) {
    Validate.isTrue(StringUtils.isNotBlank(javaSource));
    return parse(javaSource.toCharArray(), mainTypeName, fileName);
  }

  private ICompilationUnit parse(char[] javaSource, String mainTypeName, String fileName) {
    Validate.notNull(mainTypeName);
    Validate.notNull(fileName);
    StringCompilationUnit cu = new StringCompilationUnit(javaSource, mainTypeName.toCharArray(), fileName.toCharArray());
    return parse(cu);
  }

  public ICompilationUnit parse(StringBuilder javaSource, String mainTypeName, String fileName) {
    Validate.notNull(javaSource);
    Validate.notNull(mainTypeName);
    Validate.notNull(fileName);
    char[] buf = new char[javaSource.length()];
    javaSource.getChars(0, javaSource.length(), buf, 0);
    return parse(buf, mainTypeName, fileName);

  }

  public ICompilationUnit parse(InputStream javaSource, String mainTypeName, String fileName, Charset charset) {
    Validate.notNull(javaSource);
    Validate.notNull(mainTypeName);
    Validate.notNull(fileName);

    InputStreamCompilationUnit cu = new InputStreamCompilationUnit(javaSource, mainTypeName.toCharArray(), fileName.toCharArray(), charset);
    return parse(cu);
  }

  protected ICompilationUnit parse(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu) {
    CompilationUnitDeclaration ast = m_compiler.createAst(cu);
    if (!isAllowErrors()) {
      m_compiler.throwOnErrors();
    }
    return new CompilationUnit(ast, this);
  }

  protected IType getType(ReferenceBinding primaryTypeBinding, String[] parts) {
    if (primaryTypeBinding == null) {
      return null;
    }

    IType result = JavaModelUtils.bindingToType(primaryTypeBinding, this);
    if (parts.length < 2) {
      // no inner types: directly return answer
      return result;
    }

    // it is an inner type: step into
    StringTokenizer st = new StringTokenizer(parts[1], String.valueOf(Signature.C_DOLLAR), false);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();
      result = CoreUtils.getInnerType(result, name);
      if (result == null) {
        return null;
      }
    }
    return result;
  }

  protected String[] splitToPrimaryType(String fqn) {
    // check for inner types
    int firstDollarPos = fqn.indexOf(Signature.C_DOLLAR);
    if (firstDollarPos > 0) {
      String primaryType = fqn.substring(0, firstDollarPos);
      String innerTypePart = fqn.substring(firstDollarPos + 1);
      return new String[]{primaryType, innerTypePart};
    }
    return new String[]{fqn};
  }

  protected ReferenceBinding findTypeInternal(String fqn, boolean isAllowErrors) {
    Validate.notNull(fqn);
    char[][] lookupName = CharOperation.splitOn('.', fqn.toCharArray());
    m_compiler.getProblemFactory().reset();
    ReferenceBinding binding = m_compiler.lookupEnvironment.getType(lookupName);
    if (!isAllowErrors) {
      m_compiler.throwOnErrors();
    }
    return binding;
  }

  private static INameEnvironment createClasspath(List<File> cp) {
    if (CollectionUtils.isEmpty(cp)) {
      return null;
    }

    String[] cpEntries = new String[cp.size()];
    for (int i = 0; i < cp.size(); i++) {
      cpEntries[i] = cp.get(i).getAbsolutePath();
    }
    return new FileSystem(cpEntries, null, StandardCharsets.UTF_8.name());
  }

  public boolean isAllowErrors() {
    return m_allowErrors;
  }
}
