package vn.auction.network;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** One reader multiplexes correlated replies and push events. No periodic network polling. */
public final class AuctionClient implements AutoCloseable {
  private final Socket socket = new Socket();
  private final Writer writer;
  private final ConcurrentMap<Long, CompletableFuture<JsonElement>> pending =
      new ConcurrentHashMap<>();
  private final AtomicLong ids = new AtomicLong();
  private volatile Runnable onChange = () -> {};
  private volatile Consumer<String> onDisconnect = message -> {};

  public AuctionClient(String host, int port) throws IOException {
    socket.connect(new InetSocketAddress(host, port), 5000);
    writer =
        new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    Thread thread = new Thread(this::read, "client-reader");
    thread.setDaemon(true);
    thread.start();
  }

  public void onChange(Runnable callback) {
    onChange = callback;
  }

  public void onDisconnect(Consumer<String> callback) {
    onDisconnect = callback;
  }

  public synchronized CompletableFuture<JsonElement> request(JsonObject message) {
    long id = ids.incrementAndGet();
    message.addProperty("id", id);
    CompletableFuture<JsonElement> result = new CompletableFuture<>();
    pending.put(id, result);
    result.orTimeout(10, TimeUnit.SECONDS).whenComplete((v, e) -> pending.remove(id));
    try {
      writer.write(message.toString());
      writer.write('\n');
      writer.flush();
    } catch (IOException e) {
      result.completeExceptionally(e);
    }
    return result;
  }

   private void read() {
    try (Reader reader =
        new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = JsonLines.read(reader, 8_000_000)) != null) {
        JsonObject message = JsonParser.parseString(line).getAsJsonObject();
        if (message.has("event")) {
          onChange.run();
          continue;
        }
        if (!message.has("id") || message.get("id").isJsonNull()) {
          continue;
        }
        CompletableFuture<JsonElement> result = pending.remove(message.get("id").getAsLong());
        if (result == null) {
          continue;
        }
        if (message.get("ok").getAsBoolean()) {
          result.complete(message.get("data"));
        } else {
          result.completeExceptionally(new IOException(message.get("error").getAsString()));
        }
      }
    } catch (Exception e) {
      /* Closing also completes waiting requests below. */
    } finally {
      close();
      onDisconnect.accept("Disconnected. Connect and log in again.");
    }
  }

  @Override
  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
    pending.values().forEach(f -> f.completeExceptionally(new IOException("Connection closed")));
    pending.clear();
  }
}

 
