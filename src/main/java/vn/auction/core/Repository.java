package vn.auction.core;

import vn.auction.core.Domain.Snapshot;

/** Server-only persistence port. save must commit all data or throw without partial writes. */
public interface Repository extends AutoCloseable {
    Snapshot load();

    void save(Snapshot snapshot);

    @Override
    default void close() {}
}
