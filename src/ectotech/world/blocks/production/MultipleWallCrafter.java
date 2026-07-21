package ectotech.world.blocks.production;

import arc.Core;
import arc.func.Cons;
import arc.func.Intc2;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.production.WallCrafter;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class MultipleWallCrafter extends WallCrafter {

    public Seq<AttributeWall> wallAttributes = new Seq<>();

    public MultipleWallCrafter(String name) {
        super(name);

        buildType = MultipleWallCrafter.MultipleWallCrafterBuild::new;
    }

    public record AttributeWall(Attribute attribute, Item item) { }

    @Override
    public void setStats() {
        super.setStats();

        stats.remove(Stat.output);
        stats.remove(Stat.tiles);

        Seq<Item> addedItems = new Seq<>();

        // Показываем все возможные ресурсы
        for (AttributeWall resource : wallAttributes) {
            if (!addedItems.contains(resource.item, true)) {
                addedItems.add(resource.item);
                stats.add(Stat.output, resource.item);
            }

            stats.add(Stat.tiles, StatValues.blocks(resource.attribute, floating, 1f, true, false));
        }

        stats.remove(Stat.drillSpeed);
        stats.add(Stat.drillSpeed, 60f / drillTime * size, StatUnit.itemsSecond);

        boolean consItems = itemConsumer != null;

        if (consItems) stats.timePeriod = boostItemUseTime;

        if (consItems && itemConsumer instanceof ConsumeItems coni) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster, StatValues.itemBoosters("{0}" + StatUnit.timesSpeed.localized(), stats.timePeriod, itemBoostIntensity, 0f, coni.items));
        }

        if (liquidBoostIntensity != 1f &&
                findConsumer(f -> f instanceof ConsumeLiquidBase && f.booster) instanceof ConsumeLiquidBase consBase) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster,
                    StatValues.speedBoosters("{0}" + StatUnit.timesSpeed.localized(),
                            consBase.amount,
                            liquidBoostIntensity, false, consBase::consumes)
            );
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        float eff = getEfficiency(x, y, rotation, null, null);
        drawPlaceText(Core.bundle.formatFloat("bar.drillspeed", 60f / drillTime * eff, 2), x, y, valid);
    }

    @Override
    public boolean canPlaceOn(Tile tile, mindustry.game.Team team, int rotation) {
        return getEfficiency(tile.x, tile.y, rotation, null, null) > 0f;
    }

    public float getEfficiency(int tx, int ty, int rotation, @Nullable Cons<Tile> ctile, @Nullable Intc2 cpos) {
        AttributeWall dominant = dominantOre(tx, ty, rotation);

        if (dominant == null) {
            if (cpos != null) {
                eachFacingPosition(tx, ty, rotation, cpos);
            }
            return 0f;
        }

        return sumAttribute(tx, ty, rotation, dominant.attribute, ctile, cpos);
    }

    public @Nullable AttributeWall dominantOre(int tx, int ty, int rotation) {
        AttributeWall best = null;
        float bestValue = 0f;

        for (AttributeWall entry : wallAttributes) {
            float sum = sumAttribute(tx, ty, rotation, entry.attribute, null, null);

            if (sum > bestValue) {
                bestValue = sum;
                best = entry;
            }
        }

        return best;
    }

    private float sumAttribute(int tx, int ty, int rotation, Attribute attribute, @Nullable Cons<Tile> ctile, @Nullable Intc2 cpos) {
        float sum = 0f;

        int cornerX = tx - (size - 1) / 2;
        int cornerY = ty - (size - 1) / 2;

        for (int i = 0; i < size; i++) {
            int rx = 0, ry = 0;

            switch (rotation) {
                case 0 -> {
                    rx = cornerX + size;
                    ry = cornerY + i;
                }
                case 1 -> {
                    rx = cornerX + i;
                    ry = cornerY + size;
                }
                case 2 -> {
                    rx = cornerX - 1;
                    ry = cornerY + i;
                }
                case 3 -> {
                    rx = cornerX + i;
                    ry = cornerY - 1;
                }
            }

            if (cpos != null) {
                cpos.get(rx, ry);
            }

            Tile other = world.tile(rx, ry);
            if (other != null && other.solid()) {
                float value = other.block().attributes.get(attribute);
                if (value > 0f) {
                    sum += value;
                    if (ctile != null) {
                        ctile.get(other);
                    }
                }
            }
        }

        return sum;
    }

    private void eachFacingPosition(int tx, int ty, int rotation, Intc2 cpos) {
        int cornerX = tx - (size - 1) / 2;
        int cornerY = ty - (size - 1) / 2;

        for (int i = 0; i < size; i++) {
            int rx = 0, ry = 0;

            switch (rotation) {
                case 0 -> {
                    rx = cornerX + size;
                    ry = cornerY + i;
                }
                case 1 -> {
                    rx = cornerX + i;
                    ry = cornerY + size;
                }
                case 2 -> {
                    rx = cornerX - 1;
                    ry = cornerY + i;
                }
                case 3 -> {
                    rx = cornerX + i;
                    ry = cornerY - 1;
                }
            }

            cpos.get(rx, ry);
        }
    }


    public class MultipleWallCrafterBuild extends WallCrafterBuild {

        public Item currentOutput = null;

        @Override
        public void updateTile() {
            AttributeWall dominant = dominantOre(tile.x, tile.y, rotation);
            currentOutput = dominant == null ? null : dominant.item;

            boolean cons = shouldConsume();
            boolean itemValid = itemConsumer != null && itemConsumer.efficiency(this) > 0;

            warmup = Mathf.approachDelta(warmup, Mathf.num(currentOutput != null && efficiency > 0f), 1f / 40f);

            float dx = Geometry.d4x(rotation) * 0.5f;
            float dy = Geometry.d4y(rotation) * 0.5f;

            float eff = currentOutput == null
                    ? 0f
                    : getEfficiency(tile.x, tile.y, rotation, dest -> {
                if (wasVisible && cons && Mathf.chanceDelta(updateEffectChance * warmup)) {
                    updateEffect.at(
                            dest.worldx() + Mathf.range(3f) - dx * tilesize,
                            dest.worldy() + Mathf.range(3f) - dy * tilesize,
                            dest.block().mapColor
                    );
                }
            }, null);

            eff *= Mathf.lerp(1f, liquidBoostIntensity, hasLiquidBooster ? optionalEfficiency : 0f);
            eff *= itemValid ? itemBoostIntensity : 1f;

            if (itemValid && eff * efficiency > 0f && timer(timerUse, boostItemUseTime / timeScale)) {
                consume();
            }

            lastEfficiency = eff * timeScale * efficiency;

            if (cons && currentOutput != null && (time += edelta() * eff) >= drillTime) {
                offload(currentOutput);
                time %= drillTime;
            }

            totalTime += edelta() * warmup * (eff <= 0f ? 0f : 1f);

            if (timer(timerDump, dumpTime / timeScale)) {
                dumpOutputs();
            }
        }

        @Override
        public boolean shouldConsume() {
            return currentOutput != null && items.get(currentOutput) < itemCapacity;
        }

        private void dumpOutputs() {
            for (AttributeWall entry : wallAttributes) {
                dump(entry.item);
            }
        }

        @Override
        public void draw() {
            Draw.rect(block.region, x, y);
            Draw.rect(topRegion, x, y, rotdeg());

            float ds = 0.6f;
            float dx = Geometry.d4x(rotation) * ds;
            float dy = Geometry.d4y(rotation) * ds;

            int bs = (rotation == 0 || rotation == 3) ? 1 : -1;
            final int[] idx = {0};

            getEfficiency(tile.x, tile.y, rotation, null, (cx, cy) -> {
                int sign = idx[0]++ >= size / 2 && size % 2 == 0 ? -1 : 1;
                float vx = (cx - dx) * tilesize;
                float vy = (cy - dy) * tilesize;

                Draw.z(Layer.blockOver);
                Draw.rect(rotatorBottomRegion, vx, vy, totalTime * rotateSpeed * sign * bs);
                Draw.rect(rotatorRegion, vx, vy);
            });
        }
    }
}


