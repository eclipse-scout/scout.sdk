package org.eclipse.scout.sdk.core.parser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

class FileCompilationUnit implements ICompilationUnit {

  public static final String EXTENSION = "java";
  public static final String FILE_ENDING = "." + EXTENSION;

  private final File m_javaFile;
  private final Charset m_charset;

  FileCompilationUnit(File jf) {
    this(jf, StandardCharsets.UTF_8);
  }

  FileCompilationUnit(File jf, Charset charset) {
    m_javaFile = Validate.notNull(jf);
    if (charset == null) {
      m_charset = StandardCharsets.UTF_8;
    }
    else {
      m_charset = charset;
    }
  }

  @Override
  public char[] getFileName() {
    return m_javaFile.getAbsolutePath().toCharArray();
  }

  @Override
  public char[] getContents() {
    try {
      byte[] allBytes = Files.readAllBytes(Paths.get(m_javaFile.toURI()));
      CharsetDecoder cd = m_charset.newDecoder();
      CharBuffer chars = cd.decode(ByteBuffer.wrap(allBytes));
      return chars.array();
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to read contents of file '" + m_javaFile.getAbsolutePath() + "'.", e);
    }
  }

  @Override
  public char[] getMainTypeName() {
    return getMainTypeNameFromFileName(m_javaFile.getName());
  }

  public static char[] getMainTypeNameFromFileName(String name) {
    int typeNameLen = name.length() - FILE_ENDING.length();
    char[] mainTypeName = new char[typeNameLen];
    name.getChars(0, typeNameLen, mainTypeName, 0);
    return mainTypeName;
  }

  @Override
  public char[][] getPackageName() {
    // ignore package consistency checks
    return null;
  }

  @Override
  public boolean ignoreOptionalProblems() {
    return false;
  }
}
