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

  @FXML
  public void initialize() {
    role.getItems().setAll("BIDDER", "SELLER");
    role.setValue("BIDDER");
    type.getItems().setAll("ELECTRONICS", "ART", "VEHICLE");
    type.setValue("ELECTRONICS");
    addColumn("Product", a -> a.item().describe(), 240);
    addColumn("Price (VND)", a -> String.valueOf(a.currentPrice()), 140);
    addColumn("State", a -> a.status().name(), 100);
    addColumn("Ends (local time)", a -> time(a.endAt()), 180);
    auctions
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((o, before, after) -> show(after));
    start.setText(LocalDateTime.now().plusMinutes(2).format(TIME));
    end.setText(LocalDateTime.now().plusMinutes(15).format(TIME));
    chart.setAnimated(false);
  }

  private void addColumn(
      String label, java.util.function.Function<Auction, String> value, int width) {
    TableColumn<Auction, String> column = new TableColumn<>(label);
    column.setCellValueFactory(v -> new SimpleStringProperty(value.apply(v.getValue())));
    column.setPrefWidth(width);
    auctions.getColumns().add(column);
  }

  private static String time(long value) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
        .format(TIME);
  }