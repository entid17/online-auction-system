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

 
