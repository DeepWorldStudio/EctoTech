package ectotech.world.blocks.distribution;

import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.world.pressure.interfaces.Pressurized;
import ectotech.world.pressure.utils.PressureModule;
import ectotech.world.pressure.utils.PressureNetworkTypes;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.distribution.OverflowDuct;

import static mindustry.Vars.content;

public class PneumaticOverflowDuct extends OverflowDuct {

    public float operatingPressure = 1f;
    public float thresholdPressure = 2.8f;

    public boolean isPressureRequired = false;

    public float minEfficiencyCoeff = 1f;
    public float maxEfficiencyCoeff = 1f;

    public float outflowTanhFactor = 1000f;
    public float outflowExponentCoefficient = 1f;

    public float criticalPressure = Float.MAX_VALUE;
    public float superCriticalPressure = Float.MAX_VALUE;

    public final float pressureFlow = 0f; // doesn't produce pressure when working

    public boolean explodesOnSuperCritical = false;

    public PneumaticOverflowDuct(String name) {
        super(name);
        sync = true;

        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;

        config(Item.class, (PneumaticOverflowDuctBuild tile, Item item) -> tile.sortItem = item);
        configClear((PneumaticOverflowDuctBuild tile) -> tile.sortItem = null);
    }

    @Override
    public void setBars() {
        super.setBars();
        // Добавляем полоску давления в UI блока
        addBar("pressure", (PneumaticOverflowDuctBuild b) -> new mindustry.ui.Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    @Override
    public int minimapColor(mindustry.world.Tile tile){
        var build = (PneumaticOverflowDuctBuild)tile.build;
        return build == null || build.sortItem == null ? 0 : build.sortItem.color.rgba();
    }

    public class PneumaticOverflowDuctBuild extends OverflowDuctBuild implements Pressurized {

        public @Nullable Item sortItem;

        @Override public Building self() { return this; }

        public PressureModule pressureModule = new PressureModule();

        @Override public PressureModule pressureModule() { return pressureModule; }

        @Override public float operatingPressure() { return operatingPressure; }
        @Override public float thresholdPressure() { return thresholdPressure; }
        @Override public boolean isPressureRequired() { return isPressureRequired; }
        @Override public float minEfficiencyCoeff() { return minEfficiencyCoeff; }
        @Override public float maxEfficiencyCoeff() { return maxEfficiencyCoeff; }

        @Override public float outflowTanhFactor() { return outflowTanhFactor; }
        @Override public float outflowExponentCoefficient() { return outflowExponentCoefficient; }

        @Override public float criticalPressure() { return criticalPressure; }
        @Override public float superCriticalPressure() { return superCriticalPressure; }

        @Override public float pressureFlow() { return pressureFlow; }

        @Override public boolean explodesOnSuperCritical() { return explodesOnSuperCritical; }
        @Override public String pressureNetworkType() { return PressureNetworkTypes.PneumaticDuctsNetwork; }

        @Override
        public void draw() {
            arc.graphics.g2d.Draw.rect(region, x, y);
            if (sortItem != null) {
                arc.graphics.g2d.Draw.color(sortItem.color);
                arc.graphics.g2d.Draw.rect("center", x, y);
                arc.graphics.g2d.Draw.color();
            } else {
                arc.graphics.g2d.Draw.rect(topRegion, x, y, rotdeg());
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawItemSelection(sortItem);
        }

        @Override
        public void buildConfiguration(Table table){
            ItemSelection.buildTable(PneumaticOverflowDuct.this, table, content.items(), () -> sortItem, this::configure);
        }

        @Override
        public void configured(mindustry.gen.Unit player, Object value) {
            super.configured(player, value);
            if (!mindustry.Vars.headless) {
                mindustry.Vars.renderer.minimap.update(tile);
            }
        }

        @Override
        public mindustry.type.Item config() {
            return sortItem;
        }

        @Override
        public void update() {
            updatePressure();
            super.update();
        }

        @Override
        public @Nullable Building target() {
            if (current == null) return null;

            if (sortItem == null) {
                return super.target();
            }

            boolean matches = current == sortItem;

            Building front = front();
            Building left = left();
            Building right = right();

            boolean frontAccept = front != null && front.team == team && front.acceptItem(this, current);
            boolean leftAccept = left != null && left.team == team && left.acceptItem(this, current);
            boolean rightAccept = right != null && right.team == team && right.acceptItem(this, current);

            Building side = sideTarget(leftAccept, rightAccept, left, right);

            if (!invert) {
                return matches ? (frontAccept ? front : null) : side;
            } else {
                return matches ? side : (frontAccept ? front : null);
            }
        }

        private @Nullable Building sideTarget(boolean leftAccept, boolean rightAccept, Building left, Building right) {
            if (leftAccept && !rightAccept) return left;
            if (rightAccept && !leftAccept) return right;
            if (leftAccept) return cdump == 0 ? left : right;
            return null;
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume()
                    && (!isPressureRequired() || pressureEfficiency() > 0f);
        }

        @Override
        public float efficiencyScale() {
            return super.efficiencyScale() * pressureEfficiency();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            rebuildNetwork();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            disconnectNetwork();
        }

        // SERIALIZATION

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            writePressure(write);
            write.s(sortItem == null ? -1 : sortItem.id);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            readPressure(read, revision);
            if(revision >= 1){
                sortItem = content.item(read.s());
            }
        }
    }
}
