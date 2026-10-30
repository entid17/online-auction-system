package vn.auction.core;

import java.util.List;

/** Immutable values prevent callers from bypassing service validation. Money is whole VND. */
public final class Domain {
    private Domain() {}

    public enum Role {
        BIDDER,
        SELLER,
        ADMIN
    }

    public enum Status {
        OPEN,
        RUNNING,
        FINISHED,
        PAID,
        CANCELED
    }

    public abstract static class Entity {
        private final String id;

        protected Entity(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public abstract String describe();
    }

    public abstract static class User extends Entity {
        private final String name;

        protected User(String id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract Role role();