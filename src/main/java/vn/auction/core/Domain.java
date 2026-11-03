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

        @Override
        public String describe() {
            return role() + ": " + name;
        }
    }

    public static final class Bidder extends User {
        public Bidder(String id, String name) {
            super(id, name);
        }

        @Override
        public Role role() {
            return Role.BIDDER;
        }
    }

    public static final class Seller extends User {
        public Seller(String id, String name) {
            super(id, name);
        }

        @Override
        public Role role() {
            return Role.SELLER;
        }
    }

    public static final class Admin extends User {
        public Admin(String id, String name) {
            super(id, name);
        }

        @Override
        public Role role() {
            return Role.ADMIN;
        }
    }

    public abstract static class Item extends Entity {
        private final String name;

        protected Item(String id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class Electronics extends Item {
        public Electronics(String id, String name) {
            super(id, name);
        }

        @Override
        public String describe() {
            return "Electronics: " + getName();
        }
    }

    public static final class Art extends Item {
        public Art(String id, String name) {
            super(id, name);
        }

        @Override
        public String describe() {
            return "Art: " + getName();
        }
    }

    public static final class Vehicle extends Item {
        public Vehicle(String id, String name) {
            super(id, name);
        }

        @Override
        public String describe() {
            return "Vehicle: " + getName();
        }
    }

    public record Account(String id, String name, Role role, String passwordHash) {
        public User user() {
            return switch (role) {
                case BIDDER -> new Bidder(id, name);
                case SELLER -> new Seller(id, name);
                case ADMIN -> new Admin(id, name);
            };
        }
    }

    public record BidTransaction(String bidderId, long amount, long timestamp) {}

    public record Auction(
            String id,
            String sellerId,
            String type,
            String name,
            String description,
            long startPrice,
            long currentPrice,
            long startAt,
            long endAt,
            Status status,
            String leaderId,
            List<BidTransaction> bids) {
        public Auction {
            bids = List.copyOf(bids);
        }

        public Item item() {
            return ItemFactory.forType(type).create(id, name);
        }

        public Auction withState(Status state) {
            return new Auction(
                    id,
                    sellerId,
                    type,
                    name,
                    description,
                    startPrice,
                    currentPrice,
                    startAt,
                    endAt,
                    state,
                    leaderId,
                    bids);
        }
    }

    public record Snapshot(List<Account> accounts, List<Auction> auctions) {
        public Snapshot {
            accounts = List.copyOf(accounts);
            auctions = List.copyOf(auctions);
        }

        public static Snapshot empty() {
            return new Snapshot(List.of(), List.of());
        }
    }
}
