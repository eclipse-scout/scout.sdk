/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.scout.sdk.s2e.environment;

import static java.util.Collections.unmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.eclipse.scout.sdk.s2e.util.CharSequenceInputStream;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link CharSequenceInputStreamTest}</h3>
 *
 * @since 7.0.0
 */
public class CharSequenceInputStreamTest {
  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LARGE_TEST_STRING;

  private static final String TEST_STRING = "\u00e0 peine arriv\u00e9s nous entr\u00e2mes dans sa chambre";

  static {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      buffer.append(TEST_STRING);
    }
    LARGE_TEST_STRING = buffer.toString();
  }

  private final Random m_random = new Random();

  private static Iterable<String> getRequiredCharsetNames() {
    Set<String> m = new HashSet<>();
    m.add(StandardCharsets.ISO_8859_1.name());
    m.add(StandardCharsets.US_ASCII.name());
    m.add(StandardCharsets.UTF_16.name());
    m.add(StandardCharsets.UTF_16BE.name());
    m.add(StandardCharsets.UTF_16LE.name());
    m.add(StandardCharsets.UTF_8.name());
    return unmodifiableSet(m);
  }

  private void testBufferedRead(String testString, String charsetName) throws IOException {
    byte[] expected = testString.getBytes(charsetName);
    try (InputStream in = new CharSequenceInputStream(testString, charsetName, 512)) {
      byte[] buffer = new byte[128];
      int offset = 0;
      while (true) {
        int bufferOffset = m_random.nextInt(64);
        int bufferLength = m_random.nextInt(64);
        int read = in.read(buffer, bufferOffset, bufferLength);
        if (read == -1) {
          assertEquals(expected.length, offset, "EOF: offset should equal length for charset " + charsetName);
          break;
        }
        assertTrue(read <= bufferLength, "Read " + read + " <= " + bufferLength);
        while (read > 0) {
          assertTrue(offset < expected.length, "offset for " + charsetName + ' ' + offset + " < " + expected.length);
          assertEquals(expected[offset], buffer[bufferOffset], "bytes should agree for " + charsetName);
          offset++;
          bufferOffset++;
          read--;
        }
      }
    }
  }

  @Test
  public void testBufferedReadAvailableCharset() throws IOException {
    for (String csName : Charset.availableCharsets().keySet()) {
      // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
      if (isAvailabilityTestableForCharset(csName)) {
        testBufferedRead(TEST_STRING, csName);
      }
    }
  }

  @Test
  public void testBufferedReadRequiredCharset() throws IOException {
    for (String csName : getRequiredCharsetNames()) {
      testBufferedRead(TEST_STRING, csName);
    }
  }

  @Test
  public void testBufferedReadUtf8() throws IOException {
    testBufferedRead(TEST_STRING, "UTF-8");
  }

  private static void testCharsetMismatchInfiniteLoop(String csName) throws IOException {
    // Input is UTF-8 bytes: 0xE0 0xB2 0xA0
    char[] inputChars = {(char) 0xE0, (char) 0xB2, (char) 0xA0};
    Charset charset = Charset.forName(csName); // infinite loop for US-ASCII, UTF-8 OK
    try (InputStream stream = new CharSequenceInputStream(new String(inputChars), charset, 512)) {
      while (stream.read() != -1) {
      }
    }
  }

  @Test
  public void testCharsetMismatchInfiniteLoopRequiredCharsets() throws IOException {
    for (String csName : getRequiredCharsetNames()) {
      testCharsetMismatchInfiniteLoop(csName);
    }
  }

  // Test is broken if readFirst > 0
  // This is because the initial read fills the buffer from the CharSequence
  // so data1 gets the first buffer full; data2 will get the next buffer full
  private static void testIO_356(int bufferSize, int dataSize, int readFirst, String csName) throws IOException {
    try (CharSequenceInputStream is = new CharSequenceInputStream(ALPHABET, csName, bufferSize)) {

      for (int i = 0; i < readFirst; i++) {
        int ch = is.read();
        assertFalse(ch == -1);
      }

      is.mark(dataSize);

      byte[] data1 = new byte[dataSize];
      int readCount1 = is.read(data1);
      assertEquals(dataSize, readCount1);

      is.reset(); // should allow data to be re-read

      byte[] data2 = new byte[dataSize];
      int readCount2 = is.read(data2);
      assertEquals(dataSize, readCount2);

      // data buffers should be identical
      assertArrayEquals(data1, data2, "bufferSize=" + bufferSize + " dataSize=" + dataSize);
    }
  }

  @Test
  public void testIO356B10D10S0Utf16() throws IOException {
    testIO_356(10, 10, 0, "UTF-16");
  }

  @Test
  public void testIO356B10D10S0Utf8() throws IOException {
    testIO_356(10, 10, 0, "UTF-8");
  }

  @Test
  public void testIO356B10D10S1Utf8() throws IOException {
    testIO_356(10, 10, 1, "UTF-8");
  }

  @Test
  public void testIO356B10D10S2Utf8() throws IOException {
    testIO_356(10, 10, 2, "UTF-8");
  }

  @Test
  public void testIO356B10D13S0Utf8() throws IOException {
    testIO_356(10, 13, 0, "UTF-8");
  }

  @Test
  public void testIO356B10D13S1Utf8() throws IOException {
    testIO_356(10, 13, 1, "UTF-8");
  }

  @Test
  public void testIO356B10D20S0Utf8() throws IOException {
    testIO_356(10, 20, 0, "UTF-8");
  }

  private static void testIO_356_Loop(String csName, int maxBytesPerChar) throws IOException {
    for (int bufferSize = maxBytesPerChar; bufferSize <= 10; bufferSize++) {
      for (int dataSize = 1; dataSize <= 20; dataSize++) {
        testIO_356(bufferSize, dataSize, 0, csName);
      }
    }
  }

  @Test
  public void testIO356LoopUtf16() throws IOException {
    testIO_356_Loop("UTF-16", 4);
  }

  @Test
  public void testIO356LoopUtf8() throws IOException {
    testIO_356_Loop("UTF-8", 4);
  }

  @Test
  public void testLargeBufferedReadRequiredCharsets() throws IOException {
    for (String csName : getRequiredCharsetNames()) {
      testBufferedRead(LARGE_TEST_STRING, csName);
    }
  }

  @Test
  public void testLargeBufferedReadUtf8() throws IOException {
    testBufferedRead(LARGE_TEST_STRING, "UTF-8");
  }

  @Test
  public void testLargeSingleByteReadRequiredCharsets() throws IOException {
    for (String csName : getRequiredCharsetNames()) {
      testSingleByteRead(LARGE_TEST_STRING, csName);
    }
  }

  @Test
  public void testLargeSingleByteReadUtf8() throws IOException {
    testSingleByteRead(LARGE_TEST_STRING, "UTF-8");
  }

  // This test is broken for charsets that don't create a single byte for each char
  private static void testMarkReset(String csName) throws IOException {
    try (InputStream r = new CharSequenceInputStream("test", csName)) {
      assertEquals(2, r.skip(2));
      r.mark(0);
      assertEquals('s', r.read(), csName);
      assertEquals('t', r.read(), csName);
      assertEquals(-1, r.read(), csName);
      r.reset();
      assertEquals('s', r.read(), csName);
      assertEquals('t', r.read(), csName);
      assertEquals(-1, r.read(), csName);
      r.reset();
      r.reset();
    }
  }

  @Test
  public void testMarkResetUSASCII() throws IOException {
    testMarkReset("US-ASCII");
  }

  @Test
  public void testMarkResetUtf8() throws IOException {
    testMarkReset("UTF-8");
  }

  @Test
  public void testMarkSupported() throws IOException {
    try (InputStream r = new CharSequenceInputStream("test", "UTF-8")) {
      assertTrue(r.markSupported());
    }
  }

  private static void testReadZero(String csName) throws IOException {
    try (InputStream r = new CharSequenceInputStream("test", csName)) {
      byte[] bytes = new byte[30];
      assertEquals(0, r.read(bytes, 0, 0));
    }
  }

  @Test
  public void testReadZeroEmptyString() throws IOException {
    try (InputStream r = new CharSequenceInputStream("", "UTF-8")) {
      byte[] bytes = new byte[30];
      assertEquals(0, r.read(bytes, 0, 0));
    }
  }

  @Test
  public void testReadZeroRequiredCharsets() throws IOException {
    for (String csName : getRequiredCharsetNames()) {
      testReadZero(csName);
    }
  }

  private static void testSingleByteRead(String testString, String charsetName) throws IOException {
    byte[] bytes = testString.getBytes(charsetName);
    try (InputStream in = new CharSequenceInputStream(testString, charsetName, 512)) {
      for (byte b : bytes) {
        int read = in.read();
        assertTrue(read >= 0, "read " + read + " >=0 ");
        assertTrue(read <= 255, "read " + read + " <= 255");
        assertEquals(b, (byte) read, "Should agree with input");
      }
      assertEquals(-1, in.read());
    }
  }

  @Test
  public void testSingleByteReadRequiredCharsets() throws IOException {
    for (String csName : getRequiredCharsetNames()) {
      testSingleByteRead(TEST_STRING, csName);
    }
  }

  @Test
  public void testSingleByteReadUtf16() throws IOException {
    testSingleByteRead(TEST_STRING, "UTF-16");
  }

  @Test
  public void testSingleByteReadUtf8() throws IOException {
    testSingleByteRead(TEST_STRING, "UTF-8");
  }

  // This is broken for charsets that don't map each char to a byte
  private static void testSkip(String csName) throws IOException {
    try (InputStream r = new CharSequenceInputStream("test", csName)) {
      assertEquals(1, r.skip(1));
      assertEquals(2, r.skip(2));
      assertEquals('t', r.read(), csName);
      r.skip(100);
      assertEquals(-1, r.read(), csName);
    }
  }

  @Test
  public void testSkipUSASCII() throws IOException {
    testSkip("US-ASCII");
  }

  @Test
  public void testSkipUtf8() throws IOException {
    testSkip("UTF-8");
  }

  private static int checkAvail(InputStream is, int min) throws IOException {
    int available = is.available();
    assertTrue(available >= min, "avail should be >= " + min + ", but was " + available);
    return available;
  }

  private static void testAvailableSkip(String csName) throws IOException {
    String input = "test";
    try (InputStream r = new CharSequenceInputStream(input, csName)) {
      int available = checkAvail(r, input.length());
      assertEquals(available - 1, r.skip(available - 1)); // skip all but one
      checkAvail(r, 1);
      assertEquals(1, r.skip(1));
      checkAvail(r, 0);
    }
  }

  private static void testAvailableRead(String csName) throws IOException {
    String input = "test";
    try (InputStream r = new CharSequenceInputStream(input, csName)) {
      int available = checkAvail(r, input.length());
      assertEquals(available - 1, r.skip(available - 1)); // skip all but one
      available = checkAvail(r, 1);
      byte[] buff = new byte[available];
      assertEquals(available, r.read(buff, 0, available));
    }
  }

  @Test
  public void testAvailable() throws IOException {
    for (String csName : Charset.availableCharsets().keySet()) {
      // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
      // also try and avoid the following Effor on Continuum
//          java.lang.UnsupportedOperationException: null
//          at java.nio.CharBuffer.array(CharBuffer.java:940)
//          at sun.nio.cs.ext.COMPOUND_TEXT_Encoder.encodeLoop(COMPOUND_TEXT_Encoder.java:75)
//          at java.nio.charset.CharsetEncoder.encode(CharsetEncoder.java:544)
//          at org.apache.commons.io.input.CharSequenceInputStream.fillBuffer(CharSequenceInputStream.java:120)
//          at org.apache.commons.io.input.CharSequenceInputStream.read(CharSequenceInputStream.java:151)
//          at org.apache.commons.io.input.CharSequenceInputStreamTest.testAvailableRead(CharSequenceInputStreamTest.java:412)
//          at org.apache.commons.io.input.CharSequenceInputStreamTest.testAvailable(CharSequenceInputStreamTest.java:424)

      if (isAvailabilityTestableForCharset(csName)) {
        testAvailableSkip(csName);
        testAvailableRead(csName);
      }
    }
  }

  private static boolean isAvailabilityTestableForCharset(String csName) {
    return Charset.forName(csName).canEncode()
        && !"COMPOUND_TEXT".equalsIgnoreCase(csName) && !"x-COMPOUND_TEXT".equalsIgnoreCase(csName)
        && !isOddBallLegacyCharsetThatDoesNotSupportFrenchCharacters(csName);
  }

  private static boolean isOddBallLegacyCharsetThatDoesNotSupportFrenchCharacters(String csName) {
    return "x-IBM1388".equalsIgnoreCase(csName) ||
        "ISO-2022-CN".equalsIgnoreCase(csName) ||
        "ISO-2022-JP".equalsIgnoreCase(csName) ||
        "Shift_JIS".equalsIgnoreCase(csName);
  }
}
