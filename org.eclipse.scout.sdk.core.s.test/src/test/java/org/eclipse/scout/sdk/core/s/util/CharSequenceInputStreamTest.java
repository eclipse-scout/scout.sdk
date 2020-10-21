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

package org.eclipse.scout.sdk.core.s.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

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
    var buffer = new StringBuilder();
    buffer.append(TEST_STRING.repeat(100));
    LARGE_TEST_STRING = buffer.toString();
  }

  @SuppressWarnings("UnsecureRandomNumberGeneration")
  private final Random m_random = new Random();

  private static Iterable<String> getRequiredCharsetNames() {
    return Set.of(StandardCharsets.ISO_8859_1.name(),
        StandardCharsets.US_ASCII.name(),
        StandardCharsets.UTF_16.name(),
        StandardCharsets.UTF_16BE.name(),
        StandardCharsets.UTF_16LE.name(),
        StandardCharsets.UTF_8.name());
  }

  private void testBufferedRead(String testString, String charsetName) throws IOException {
    var expected = testString.getBytes(charsetName);
    try (InputStream in = new CharSequenceInputStream(testString, charsetName, 512)) {
      var buffer = new byte[128];
      var offset = 0;
      while (true) {
        var bufferOffset = m_random.nextInt(64);
        var bufferLength = m_random.nextInt(64);
        var read = in.read(buffer, bufferOffset, bufferLength);
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
    for (var csName : Charset.availableCharsets().keySet()) {
      // prevent java.lang.UnsupportedOperationException at sun.nio.cs.ext.ISO2022_CN.newEncoder.
      if (isAvailabilityTestableForCharset(csName)) {
        testBufferedRead(TEST_STRING, csName);
      }
    }
  }

  @Test
  public void testBufferedReadRequiredCharset() throws IOException {
    for (var csName : getRequiredCharsetNames()) {
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
    var charset = Charset.forName(csName); // infinite loop for US-ASCII, UTF-8 OK
    try (InputStream stream = new CharSequenceInputStream(new String(inputChars), charset, 512)) {
      //noinspection StatementWithEmptyBody
      while (stream.read() != -1) {
      }
    }
  }

  @Test
  public void testCharsetMismatchInfiniteLoopRequiredCharsets() throws IOException {
    for (var csName : getRequiredCharsetNames()) {
      testCharsetMismatchInfiniteLoop(csName);
    }
  }

  // Test is broken if readFirst > 0
  // This is because the initial read fills the buffer from the CharSequence
  // so data1 gets the first buffer full; data2 will get the next buffer full
  private static void testIO_356(int bufferSize, int dataSize, int readFirst, String csName) throws IOException {
    try (var is = new CharSequenceInputStream(ALPHABET, csName, bufferSize)) {

      for (var i = 0; i < readFirst; i++) {
        var ch = is.read();
        assertNotEquals(ch, -1);
      }

      is.mark(dataSize);

      var data1 = new byte[dataSize];
      var readCount1 = is.read(data1);
      assertEquals(dataSize, readCount1);

      is.reset(); // should allow data to be re-read

      var data2 = new byte[dataSize];
      var readCount2 = is.read(data2);
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
    for (var bufferSize = maxBytesPerChar; bufferSize <= 10; bufferSize++) {
      for (var dataSize = 1; dataSize <= 20; dataSize++) {
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
    for (var csName : getRequiredCharsetNames()) {
      testBufferedRead(LARGE_TEST_STRING, csName);
    }
  }

  @Test
  public void testLargeBufferedReadUtf8() throws IOException {
    testBufferedRead(LARGE_TEST_STRING, "UTF-8");
  }

  @Test
  public void testLargeSingleByteReadRequiredCharsets() throws IOException {
    for (var csName : getRequiredCharsetNames()) {
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
      var bytes = new byte[30];
      assertEquals(0, r.read(bytes, 0, 0));
    }
  }

  @Test
  public void testReadZeroEmptyString() throws IOException {
    try (InputStream r = new CharSequenceInputStream("", "UTF-8")) {
      var bytes = new byte[30];
      assertEquals(0, r.read(bytes, 0, 0));
    }
  }

  @Test
  public void testReadZeroRequiredCharsets() throws IOException {
    for (var csName : getRequiredCharsetNames()) {
      testReadZero(csName);
    }
  }

  private static void testSingleByteRead(String testString, String charsetName) throws IOException {
    var bytes = testString.getBytes(charsetName);
    try (InputStream in = new CharSequenceInputStream(testString, charsetName, 512)) {
      for (var b : bytes) {
        var read = in.read();
        assertTrue(read >= 0, "read " + read + " >=0 ");
        //noinspection ConstantConditions
        assertTrue(read <= 255, "read " + read + " <= 255");
        //noinspection NumericCastThatLosesPrecision
        assertEquals(b, (byte) read, "Should agree with input");
      }
      assertEquals(-1, in.read());
    }
  }

  @Test
  public void testSingleByteReadRequiredCharsets() throws IOException {
    for (var csName : getRequiredCharsetNames()) {
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
      //noinspection ResultOfMethodCallIgnored
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
    var available = is.available();
    assertTrue(available >= min, "avail should be >= " + min + ", but was " + available);
    return available;
  }

  private static void testAvailableSkip(String csName) throws IOException {
    var input = "test";
    try (InputStream r = new CharSequenceInputStream(input, csName)) {
      var available = checkAvail(r, input.length());
      assertEquals(available - 1, r.skip(available - 1)); // skip all but one
      checkAvail(r, 1);
      assertEquals(1, r.skip(1));
      checkAvail(r, 0);
    }
  }

  private static void testAvailableRead(String csName) throws IOException {
    var input = "test";
    try (InputStream r = new CharSequenceInputStream(input, csName)) {
      var available = checkAvail(r, input.length());
      assertEquals(available - 1, r.skip(available - 1)); // skip all but one
      available = checkAvail(r, 1);
      var buff = new byte[available];
      assertEquals(available, r.read(buff, 0, available));
    }
  }

  @Test
  public void testAvailable() throws IOException {
    for (var csName : Charset.availableCharsets().keySet()) {
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
    return Stream.of("x-IBM1388", "ISO-2022-CN", "ISO-2022-JP", "Shift_JIS").anyMatch(s -> s.equalsIgnoreCase(csName));
  }
}
