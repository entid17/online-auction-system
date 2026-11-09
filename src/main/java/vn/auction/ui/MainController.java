package vn.auction.ui;

import com.google.gson.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import vn.auction.core.Domain.Auction;
import vn.auction.network.AuctionClient;

/** View controller only: all authorization and auction rules live on the server. */
public final class MainController implements AutoCloseable {
  @FXML private TextField host, port, username, amount, itemName, price, start, end;
  @FXML private PasswordField password;
  @FXML private ComboBox<String> role, type;
  @FXML private TextArea description, details;
  @FXML private Label status;
  @FXML private TableView<Auction> auctions;
  @FXML private LineChart<Number, Number> chart;
  private final Gson gson = new Gson();
  private final ExecutorService io =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r, "ui-network");
            t.setDaemon(true);
            return t;
          });
  private AuctionClient client;
  private boolean loggedIn;
  private boolean refreshing;
  private boolean refreshAgain;
  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");   
