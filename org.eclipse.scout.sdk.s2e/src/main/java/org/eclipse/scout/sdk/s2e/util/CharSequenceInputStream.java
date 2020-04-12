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

package org.eclipse.scout.sdk.s2e.util;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * {@link InputStream} implementation that can read from {@link String}, {@link StringBuffer}, {@link StringBuilder} or
 * {@link CharBuffer}.
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 */
public class CharSequenceInputStream extends InputStream {

  private static final int BUFFER_SIZE = 2048;
  private static final int EOF = -1;
  private static final int NO_MARK = -1;

  private final CharsetEncoder m_encoder;
  private final CharBuffer m_cbuf;
  private final ByteBuffer m_bbuf;

  private int m_markCbuf; // position in cbuf
  private int m_markBbuf; // position in bbuf

  /**
   * Constructor.
   *
   * @param cs
   *          the input character sequence
   * @param charset
   *          the character set name to use
   * @param bufferSize
   *          the buffer size to use.
   * @throws IllegalArgumentException
   *           if the buffer is not large enough to hold a complete character
   */
  public CharSequenceInputStream(CharSequence cs, Charset charset, int bufferSize) {
    m_encoder = charset.newEncoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    // Ensure that buffer is long enough to hold a complete character
    float maxBytesPerChar = m_encoder.maxBytesPerChar();
    if (bufferSize < maxBytesPerChar) {
      throw new IllegalArgumentException("Buffer size " + bufferSize + " is less than maxBytesPerChar " +
          maxBytesPerChar);
    }
    m_bbuf = ByteBuffer.allocate(bufferSize);
    m_bbuf.flip();
    m_cbuf = CharBuffer.wrap(cs);
    m_markCbuf = NO_MARK;
    m_markBbuf = NO_MARK;
  }

  /**
   * Constructor, calls {@link #CharSequenceInputStream(CharSequence, Charset, int)}.
   *
   * @param cs
   *          the input character sequence
   * @param charset
   *          the character set name to use
   * @param bufferSize
   *          the buffer size to use.
   * @throws IllegalArgumentException
   *           if the buffer is not large enough to hold a complete character
   */
  public CharSequenceInputStream(CharSequence cs, String charset, int bufferSize) {
    this(cs, Charset.forName(charset), bufferSize);
  }

  /**
   * Constructor, calls {@link #CharSequenceInputStream(CharSequence, Charset, int)} with a buffer size of 2048.
   *
   * @param cs
   *          the input character sequence
   * @param charset
   *          the character set name to use
   * @throws IllegalArgumentException
   *           if the buffer is not large enough to hold a complete character
   */
  public CharSequenceInputStream(CharSequence cs, Charset charset) {
    this(cs, charset, BUFFER_SIZE);
  }

  /**
   * Constructor, calls {@link #CharSequenceInputStream(CharSequence, String, int)} with a buffer size of 2048.
   *
   * @param cs
   *          the input character sequence
   * @param charset
   *          the character set name to use
   * @throws IllegalArgumentException
   *           if the buffer is not large enough to hold a complete character
   */
  public CharSequenceInputStream(CharSequence cs, String charset) {
    this(cs, charset, BUFFER_SIZE);
  }

  /**
   * Fills the byte output buffer from the input char buffer.
   *
   * @throws CharacterCodingException
   *           an error encoding data
   */
  private void fillBuffer() throws CharacterCodingException {
    m_bbuf.compact();
    CoderResult result = m_encoder.encode(m_cbuf, m_bbuf, true);
    if (result.isError()) {
      result.throwException();
    }
    m_bbuf.flip();
  }

  @Override
  public int read(byte[] b, int off, int len) throws CharacterCodingException {
    if (len < 0 || (off + len) > Ensure.notNull(b).length) {
      throw new IndexOutOfBoundsException("Array Size=" + b.length +
          ", offset=" + off + ", length=" + len);
    }
    if (len == 0) {
      return 0; // must return 0 for zero length read
    }
    if (!m_bbuf.hasRemaining() && !m_cbuf.hasRemaining()) {
      return EOF;
    }
    int bytesRead = 0;
    while (len > 0) {
      if (m_bbuf.hasRemaining()) {
        int chunk = Math.min(m_bbuf.remaining(), len);
        m_bbuf.get(b, off, chunk);
        off += chunk;
        len -= chunk;
        bytesRead += chunk;
      }
      else {
        fillBuffer();
        if (!m_bbuf.hasRemaining() && !m_cbuf.hasRemaining()) {
          break;
        }
      }
    }
    return bytesRead == 0 && !m_cbuf.hasRemaining() ? EOF : bytesRead;
  }

  @Override
  public int read() throws CharacterCodingException {
    for (;;) {
      if (m_bbuf.hasRemaining()) {
        return m_bbuf.get() & 0xFF;
      }
      fillBuffer();
      if (!m_bbuf.hasRemaining() && !m_cbuf.hasRemaining()) {
        return EOF;
      }
    }
  }

  @Override
  public int read(byte[] b) throws CharacterCodingException {
    return read(b, 0, b.length);
  }

  @Override
  public long skip(long n) throws CharacterCodingException {
    /*
     * This could be made more efficient by using position to skip within the current buffer.
     */
    long skipped = 0;
    while (n > 0 && available() > 0) {
      read();
      n--;
      skipped++;
    }
    return skipped;
  }

  /**
   * Return an estimate of the number of bytes remaining in the byte stream.
   *
   * @return the count of bytes that can be read without blocking (or returning EOF).
   */
  @Override
  public int available() {
    // The cached entries are in bbuf; since encoding always creates at least one byte
    // per character, we can add the two to get a better estimate (e.g. if bbuf is empty)
    // Note that the previous implementation (2.4) could return zero even though there were
    // encoded bytes still available.
    return m_bbuf.remaining() + m_cbuf.remaining();
  }

  @Override
  public void close() {
    // nop
  }

  /**
   * @param readlimit
   *          max read limit (ignored)
   */
  @Override
  public synchronized void mark(int readlimit) {
    m_markCbuf = m_cbuf.position();
    m_markBbuf = m_bbuf.position();
    m_cbuf.mark();
    m_bbuf.mark();
    // It would be nice to be able to use mark & reset on the cbuf and bbuf
    // however the bbuf is re-used so that won't work
  }

  @Override
  public synchronized void reset() throws CharacterCodingException {
    /*
     * This is not the most efficient implementation, as it re-encodes from the beginning.
     *
     * Since the bbuf is re-used, in general it's necessary to re-encode the data.
     *
     * It should be possible to apply some optimisations however:
     * + use mark/reset on the cbuf and bbuf. This would only work if the buffer had not been (re)filled since
     * the mark. The code would have to catch InvalidMarkException - does not seem possible to check if mark is
     * valid otherwise. + Try saving the state of the cbuf before each fillBuffer; it might be possible to
     * restart from there.
     */
    if (m_markCbuf != NO_MARK) {
      // if cbuf is at 0, we have not started reading anything, so skip re-encoding
      if (m_cbuf.position() != 0) {
        m_encoder.reset();
        m_cbuf.rewind();
        m_bbuf.rewind();
        m_bbuf.limit(0); // rewind does not clear the buffer
        while (m_cbuf.position() < m_markCbuf) {
          m_bbuf.rewind(); // empty the buffer (we only refill when empty during normal processing)
          m_bbuf.limit(0);
          fillBuffer();
        }
      }
      if (m_cbuf.position() != m_markCbuf) {
        throw new IllegalStateException("Unexpected CharBuffer postion: actual=" + m_cbuf.position() + ' ' +
            "expected=" + m_markCbuf);
      }
      m_bbuf.position(m_markBbuf);
      m_markCbuf = NO_MARK;
      m_markBbuf = NO_MARK;
    }
  }

  @Override
  public boolean markSupported() {
    return true;
  }
}
