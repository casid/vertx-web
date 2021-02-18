package io.vertx.ext.web.templ.jte.impl;

import gg.jte.output.Utf8ByteOutput;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.UncheckedIOException;

public class JteBufferUtil {

  public static Buffer toBuffer(Utf8ByteOutput output) {
    Buffer buffer = Buffer.buffer(output.getContentLength());

    try {
      output.writeTo(buffer::appendBytes);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return buffer;
  }
}
