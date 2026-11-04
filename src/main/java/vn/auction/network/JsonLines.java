package vn.auction.network;

import java.io.*;

/** Limits a request before allocating an unbounded line buffer. */
public final class JsonLines {
  private JsonLines() {}

  public static String read(Reader reader, int max) throws IOException {
    StringBuilder line = new StringBuilder();
    int c;
    while ((c = reader.read()) != -1) {
      if (c == '\n') {
        return line.toString();
      }
      if (c != '\r') {
        line.append((char) c);
      }
      if (line.length() > max) {
        throw new IOException("Frame too large");
      }
    }
    return line.isEmpty() ? null : line.toString();
  }
}
