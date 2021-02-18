package io.vertx.ext.web.templ.jte.impl;

import gg.jte.TemplateOutput;
import io.vertx.core.buffer.Buffer;

import java.io.Writer;

public class VertxUtf8BufferOutput extends Writer implements TemplateOutput {
  private final char[] tempBuffer;
  private final int tempBufferSize;

  private final Buffer buffer;

  private char highSurrogate;

  /**
   * Constructs an output with sane defaults
   */
  public VertxUtf8BufferOutput() {
    this(Buffer.buffer(8 * 1024), 512);
  }

  /**
   * Constructs an output with custom settings.
   * This output maintains a vert.x {@link Buffer}
   * @param buffer The size in bytes for chunks of dynamic data.
   * @param tempBufferSize The size for the temporary buffer used for intermediate String encoding.
   */
  public VertxUtf8BufferOutput(Buffer buffer, int tempBufferSize) {
    this.buffer = buffer;

    this.tempBufferSize = tempBufferSize;
    tempBuffer = new char[tempBufferSize];
  }

  /**
   * @return buffer that contains the template output.
   */
  public Buffer getBuffer() {
    return buffer;
  }

  @Override
  public Writer getWriter() {
    return this;
  }

  @Override
  public void writeContent(String s) {
    int len = s.length();
    for (int i = 0; i < len; i += tempBufferSize) {
      int size = Math.min(tempBufferSize, len - i);
      s.getChars(i, i + size, tempBuffer, 0);
      write(tempBuffer, 0, size);
    }
  }

  @Override
  public void writeBinaryContent(byte[] value) {
    buffer.appendBytes(value);
  }

  @Override
  public void writeUserContent(boolean value) {
    appendLatin1(String.valueOf(value));
  }

  @Override
  public void writeUserContent(byte value) {
    appendLatin1(Byte.toString(value));
  }

  @Override
  public void writeUserContent(char value) {
    appendUtf8Char(value);
  }

  @Override
  public void writeUserContent(int value) {
    appendLatin1(Integer.toString(value));
  }

  @Override
  public void writeUserContent(long value) {
    appendLatin1(Long.toString(value));
  }

  @Override
  public void writeUserContent(float value) {
    appendLatin1(Float.toString(value));
  }

  @Override
  public void writeUserContent(double value) {
    appendLatin1(Double.toString(value));
  }

  // Writer interface

  @Override
  public void write(char[] buffer, int off, int len) {
    int i = off;
    len += off;

    while (i < len) {
      write(buffer[i++]);
    }
  }

  public void write(char c) {
    if (highSurrogate != 0) {
      if (Character.isLowSurrogate(c)) {
        appendUtf8CodePoint(Character.toCodePoint(highSurrogate, c));
      } else {
        buffer.appendByte((byte)('�'));
        appendUtf8Char(c);
      }
      highSurrogate = 0;
    } else if (Character.isHighSurrogate(c)) {
      highSurrogate = c;
    } else {
      appendUtf8Char(c);
    }
  }

  @Override
  public void write(int c) {
    write((char) c);
  }

  @Override
  public void write(char[] buffer) {
    write(buffer, 0, buffer.length);
  }

  @Override
  public void write(String str) {
    writeContent(str);
  }

  @Override
  public void flush() {
    // nothing to do
  }

  @Override
  public void close() {
    // nothing to do
  }

  private void appendLatin1(String s) {
    int len = s.length();

    for (int i = 0; i < len; ++i) {
      buffer.appendByte((byte) s.charAt(i));
    }
  }

  private void appendUtf8Char(char c) {
    if (c < 0x80) {
      buffer.appendByte((byte) c);
    } else if (c < 0x800) {
      buffer.appendByte((byte) (0xc0 | c >> 6));
      buffer.appendByte((byte) (0x80 | c & 0x3f));
    } else {
      buffer.appendByte((byte)(0xe0 | (c >> 12)));
      buffer.appendByte((byte)(0x80 | ((c >> 6) & 0x3f)));
      buffer.appendByte((byte)(0x80 | (c & 0x3f)));
    }
  }

  private void appendUtf8CodePoint(int c) {
    buffer.appendByte((byte)(0xf0 | (c >> 18)));
    buffer.appendByte((byte)(0x80 | ((c >> 12) & 0x3f)));
    buffer.appendByte((byte)(0x80 | ((c >> 6) & 0x3f)));
    buffer.appendByte((byte)(0x80 | (c & 0x3f)));
  }

}
