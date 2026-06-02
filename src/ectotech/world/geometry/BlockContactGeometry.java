package ectotech.world.geometry;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;

public class BlockContactGeometry {

    public Building source;
    public int side;

    public final Seq<Port> ports = new Seq<>();

    public int sideScale;
    public int contactTilesCount;
    public int airTilesCount;

    public float airFraction;

    public record Port(Building building, int targetSide, int contactTilesCount, float fraction) {
    }

    public void clear() {
        source = null;
        side = -1;
        ports.clear();
        sideScale = 0;
        contactTilesCount = 0;
        airTilesCount = 0;
        airFraction = 0f;
    }

    public static int opposite(int side) {
        return (side + 2) & 3;
    }

    public static int calculateContactTiles(Building source, Building other, int side) {
        side &= 3;

        float sourceHalf = source.block.size * Vars.tilesize / 2f;
        float otherHalf = other.block.size * Vars.tilesize / 2f;

        float sourceLeft = source.x - sourceHalf;
        float sourceRight = source.x + sourceHalf;
        float sourceBottom = source.y - sourceHalf;
        float sourceTop = source.y + sourceHalf;

        float otherLeft = other.x - otherHalf;
        float otherRight = other.x + otherHalf;
        float otherBottom = other.y - otherHalf;
        float otherTop = other.y + otherHalf;

        float epsilon = 0.01f;

        float overlap;

        switch (side) {
            case 0 -> { // right
                if (Math.abs(otherLeft - sourceRight) > epsilon) return 0;
                overlap = Math.min(sourceTop, otherTop) - Math.max(sourceBottom, otherBottom);
            }
            case 1 -> { // top
                if (Math.abs(otherBottom - sourceTop) > epsilon) return 0;
                overlap = Math.min(sourceRight, otherRight) - Math.max(sourceLeft, otherLeft);
            }
            case 2 -> { // left
                if (Math.abs(otherRight - sourceLeft) > epsilon) return 0;
                overlap = Math.min(sourceTop, otherTop) - Math.max(sourceBottom, otherBottom);
            }
            case 3 -> { // bottom
                if (Math.abs(otherTop - sourceBottom) > epsilon) return 0;
                overlap = Math.min(sourceRight, otherRight) - Math.max(sourceLeft, otherLeft);
            }
            default -> {
                return 0;
            }
        }

        if (overlap <= 0f) return 0;

        int tiles = Mathf.round(overlap / Vars.tilesize);
        return Math.max(tiles, 0);
    }

    public void rebuild(Building source, int side) {
        clear();

        this.source = source;
        this.side = side & 3;
        this.sideScale = source.block.size;

        for (Building other : source.proximity) {
            if (other == null || other == source) continue;

            int thisContactTiles = calculateContactTiles(source, other, this.side);
            if (thisContactTiles <= 0) continue;

            contactTilesCount += thisContactTiles;

            ports.add(new Port(other, opposite(this.side), thisContactTiles, thisContactTiles / (float) sideScale));
        }

        contactTilesCount = Math.min(contactTilesCount, sideScale);
        airTilesCount = Math.max(sideScale - contactTilesCount, 0);
        airFraction = sideScale == 0 ? 0f : airTilesCount / (float) sideScale;
    }

    public static boolean hasEdgeContact(Building a, Building b) {
        for (int side = 0; side < 4; side++) {
            if (calculateContactTiles(a, b, side) > 0) {
                return true;
            }
        }
        return false;
    }
}
