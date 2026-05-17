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
        float sourceHalf = source.block.size * Vars.tilesize / 2f;
        float otherHalf = other.block.size * Vars.tilesize / 2f;

        float sourceMin, sourceMax;
        float otherMin, otherMax;

        // Для лево/право считаем перекрытие по Y
        if (side == 0 || side == 2) {
            sourceMin = source.y - sourceHalf;
            sourceMax = source.y + sourceHalf;
            otherMin = other.y - otherHalf;
            otherMax = other.y + otherHalf;
        } else {
            // Для верх/низ считаем перекрытие по X
            sourceMin = source.x - sourceHalf;
            sourceMax = source.x + sourceHalf;
            otherMin = other.x - otherHalf;
            otherMax = other.x + otherHalf;
        }

        float overlap = Math.min(sourceMax, otherMax) - Math.max(sourceMin, otherMin);
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

            if (source.relativeTo(other) != this.side) continue;

            int thisContactTiles = calculateContactTiles(source, other, this.side);
            if (thisContactTiles <= 0) continue;

            contactTilesCount += thisContactTiles;

            ports.add(new Port(other, opposite(this.side), thisContactTiles, thisContactTiles / (float) sideScale));
        }

        contactTilesCount = Math.min(contactTilesCount, sideScale);
        airTilesCount = Math.max(sideScale - contactTilesCount, 0);
        airFraction = sideScale == 0 ? 0f : airTilesCount / (float) sideScale;
    }
}
