package ectotech.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Nullable;
import ectotech.world.pressure.interfaces.Pressurized;
import ectotech.world.pressure.interfaces.PressurizedNetworkMember;
import ectotech.world.pressure.utils.PressureModule;
import ectotech.world.pressure.utils.PressureNetworkTypes;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import mindustry.graphics.Lod;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.DirectionBridge;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.blocks.distribution.DuctBridge;

import static mindustry.Vars.tilesize;

public class PneumaticDuctBridge extends DuctBridge {

    public @Nullable TextureRegion connectorRegion, bridgeConnectorRegion;

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

    public boolean explodesOnSuperCritical = true;

    public PneumaticDuctBridge(String name) {
        super(name);

        squareSprite = false;
    }

    @Override
    public void load() {
        super.load();

        connectorRegion = Core.atlas.find(name + "-connector");
        bridgeConnectorRegion = Core.atlas.find(name + "-bridge-connector");
    }

    @Override
    public void drawBridge(int rotation, float x1, float y1, float x2, float y2, @Nullable Color liquidColor) {
        Draw.alpha(Renderer.bridgeOpacity);

        float
                angle = Angles.angle(x1, y1, x2, y2),
                cx = (x1 + x2)/2f,
                cy = (y1 + y2)/2f,
                len = Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) - size * tilesize;

        Draw.rect(bridgeRegion, cx, cy, len, bridgeRegion.height * bridgeRegion.scl(), angle);

        if (liquidColor != null) {
            Draw.color(liquidColor, liquidColor.a * Renderer.bridgeOpacity);
            Draw.rect(bridgeLiquidRegion, cx, cy, len, bridgeLiquidRegion.height * bridgeLiquidRegion.scl(), angle);
            Draw.color();
            Draw.alpha(Renderer.bridgeOpacity);
        }

        if (bridgeBotRegion.found()) {
            Draw.color(0.4f, 0.4f, 0.4f, 0.4f * Renderer.bridgeOpacity);
            Draw.rect(bridgeBotRegion, cx, cy, len, bridgeBotRegion.height * bridgeBotRegion.scl(), angle);
            Draw.reset();
        }

        Draw.alpha(Renderer.bridgeOpacity);

        if (bridgeConnectorRegion.found()) {
            Draw.rect(bridgeConnectorRegion, x1, y1, rotation * 90f);
            Draw.rect(bridgeConnectorRegion, x2, y2, ((rotation + 2) & 3) * 90f);
            Draw.reset();
        }

        Draw.alpha(Renderer.bridgeOpacity);

        if(Lod.l1){
            Draw.alpha(Lod.alpha1);
            for (float i = 8f; i <= len + size * tilesize - 6.6f; i += 8f) {
                Draw.rect(arrowRegion, x1 + Geometry.d4x(rotation) * i, y1 + Geometry.d4y(rotation) * i, angle);
            }
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        // Добавляем полоску давления в UI блока
        addBar("pressure", (PneumaticDuctBridgeBuild b) -> new mindustry.ui.Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    public class PneumaticDuctBridgeBuild extends DuctBridgeBuild implements Pressurized, PressurizedNetworkMember {

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
        public void getPressureConnections(Seq<PressurizedNetworkMember> out) {
            PressurizedNetworkMember.super.getPressureConnections(out);

            Building linked = findLink();
            if (linked instanceof PressurizedNetworkMember other
                    && !out.contains(other, true)) {
                out.add(other);
            }

            for (int dir = 0; dir < 4; dir++) {
                int dx = Geometry.d4x(dir);
                int dy = Geometry.d4y(dir);

                for (int i = 1; i <= range; i++) {
                    Tile otherTile = tile.nearby(-dx * i, -dy * i);
                    if (otherTile == null || otherTile.build == null) continue;

                    if (otherTile.build instanceof DirectionBridge.DirectionBridgeBuild bridge
                            && bridge.block == block
                            && bridge.team == team) {

                        if (bridge.rotation == dir
                                && bridge.findLink() == this
                                && bridge instanceof PressurizedNetworkMember member
                                && !out.contains(member, true)) {
                            out.add(member);
                        }
                        break;
                    }
                }
            }
        }

        @Override
        public boolean canPressureOutputTo(Building target, int side) {
            // Терминальный мост: выводит вперёд как duct
            if (findLink() == null) return side == rotation;
            // Есть link: локально не выводит — только удалённо через getPressureConnections
            return false;
        }

        @Override
        public boolean canPressureInputFrom(Building source, int side) {
            if (findLink() == null) return false;

            return side != rotation;
        }

        @Override
        public void update() {
            updatePressure();
            super.update();

            keepAwakeIfPressurized();
        }

        @Override
        public void draw() {
            super.draw();

            if (connectorRegion == null || !connectorRegion.found()) return;

            for (Building other : proximity) {
                if (other.block instanceof Duct) continue;

                int side = relativeToEdge(other.tile);
                if (side < 0) continue;

                Draw.rect(connectorRegion, x, y, side * 90f);
            }
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && (!isPressureRequired() || pressureEfficiency() > 0f);
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
        public void write(arc.util.io.Writes write) {
            super.write(write);
            writePressure(write);
        }

        @Override
        public void read(arc.util.io.Reads read, byte revision) {
            super.read(read, revision);
            readPressure(read, revision);
        }
    }
}
