package vn.auction.core;

import vn.auction.core.Domain.*;

/** Factory Method: each concrete creator overrides object construction. */
public abstract class ItemFactory {
    public abstract Item create(String id, String name);

    public static ItemFactory forType(String type) {
        return switch (type) {
            case "ELECTRONICS" -> new ElectronicsFactory();
            case "ART" -> new ArtFactory();
            case "VEHICLE" -> new VehicleFactory();
            default -> throw new AuctionException("Invalid item type");
        };
    }

    public static final class ElectronicsFactory extends ItemFactory {
        @Override
        public Item create(String id, String name) {
            return new Electronics(id, name);
        }
    }

    public static final class ArtFactory extends ItemFactory {
        @Override
        public Item create(String id, String name) {
            return new Art(id, name);
        }
    }

    public static final class VehicleFactory extends ItemFactory {
        @Override
        public Item create(String id, String name) {
            return new Vehicle(id, name);
        }
    }
}
