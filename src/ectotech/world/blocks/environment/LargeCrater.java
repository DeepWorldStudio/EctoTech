package ectotech.world.blocks.environment;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.tilesize;

public class LargeCrater extends Floor {
    public static final Point2[] offsets = {
            new Point2(0, 0),
            new Point2(1, 0),
            new Point2(1, 1),
            new Point2(0, 1),
    };

    public Block parent = Blocks.air;

    public LargeCrater(String name) {
        super(name);
    }

    static {
        for (var p : offsets) {
            p.sub(1, 1);
        }
    }

    @Override
    public void drawMain(Tile tile) {
        if (parent instanceof Floor floor) {
            floor.drawMain(tile);
        }

        if (checkAdjacent(tile)) {
            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx() - tilesize / 2f, tile.worldy() - tilesize / 2f);
        }
    }

    public boolean checkAdjacent(Tile tile) {
        for (var point : offsets) {
            Tile other = Vars.world.tile(tile.x + point.x, tile.y + point.y);
            if (other == null || other.floor() != this) {
                return false;
            }
        }
        return true;
    }
}
