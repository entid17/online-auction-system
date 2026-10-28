package vn.auction.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class AuctionServer implements AutoCloseable {
    private static final int DEFAULT_PORT = 5000; //Khai báo 1 cổng 5000

    private final ServerSocket serverSocket;

    public AuctionServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println(
                "Auction server listening on port " + serverSocket.getLocalPort()
        );

        while (!serverSocket.isClosed()) {
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println(
                        "Client connected: " + clientSocket.getRemoteSocketAddress()
                );

                System.out.println(
                        "Connection closed: message handling is not implemented yet."
                );
            } catch (SocketException e) {
                if (!serverSocket.isClosed()) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args) {
        try (AuctionServer server = new AuctionServer(DEFAULT_PORT)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.close();
                } catch (IOException e) {
                    System.err.println(
                            "Could not close server: " + e.getMessage()
                    );
                }
            }));

            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(1);
        }
    }
}
