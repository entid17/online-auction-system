package vn.auction.storage;

import com.google.gson.Gson;
import java.sql.*;
import vn.auction.core.Domain.Snapshot;
import vn.auction.core.Repository;

/** Transactional snapshot DAO. Small classroom dataset; no client may instantiate this DAO. */
public final class SqliteRepository implements Repository {
  private final Connection connection;
  private final Gson gson = new Gson();

  public SqliteRepository(String path) {
    try {
      connection = DriverManager.getConnection("jdbc:sqlite:" + path);
      try (Statement s = connection.createStatement()) {
        s.execute("PRAGMA journal_mode=WAL");
        s.execute("PRAGMA busy_timeout=5000");
        s.execute(
            "CREATE TABLE IF NOT EXISTS app_state (id INTEGER PRIMARY KEY CHECK(id=1), payload TEXT"
                + " NOT NULL)");
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot open database", e);
    }
  }

  @Override
  public synchronized Snapshot load() {
    try (Statement s = connection.createStatement();
        ResultSet r = s.executeQuery("SELECT payload FROM app_state WHERE id=1")) {
      return r.next() ? gson.fromJson(r.getString(1), Snapshot.class) : Snapshot.empty();
    } catch (SQLException e) {
      throw new IllegalStateException("Cannot load database", e);
    }
  }
