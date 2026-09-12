package ectotech.world.blocks.defense;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.Radar;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.*;

public class UnitSignatureRadar extends Radar {

    public TextureRegion bottomRegion;
    public TextureRegion rotatorRegion;

    /**Enemy unit signature radius, in tiles*/
    public float extraRadius = 20f;
    /**Signature reload period, ticks*/
    public float signatureCycle = 2f * 60f;
    public float boostedRadiusScl = 1.5f;
    /**Use time of booster-item*/
    public float useTime = 60f * 8f;
    /**Signature animation stroke*/
    public float signatureStroke = 11f;
    /**Scale modifier for unit signature, 1 to use original unit hitSize*/
    public float signatureSizeScl = 1.4f;

    protected final int timerUse = timers++;

    public UnitSignatureRadar(String name) {
        super(name);

        hasItems = true;
        itemCapacity = 10;
        squareSprite = false;
        outlineIcon = false;

        buildType = UnitSignatureRadarBuild::new;
    }

    @Override
    public void setStats() {
        stats.timePeriod = useTime;
        super.setStats();

        stats.add(Stat.range, fogRadius, StatUnit.blocks);

        stats.add(Stat.abilities, table -> {
            table.add(Core.bundle.format("stat.ectotech-signature-range", (int)(fogRadius + extraRadius), (int)extraRadius)).left().wrap();
        });

        if (findConsumer(c -> c instanceof ConsumeItems) instanceof ConsumeItems cons) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster, StatValues.itemBoosters(
                    "{0}" + StatUnit.timesSpeed.localized(),
                    stats.timePeriod,
                    1f,
                    (boostedRadiusScl - 1f) * (fogRadius + extraRadius) * tilesize,
                    cons.items
            ));
        }
    }

    @Override
    public void init() {
        clipSize = Math.max(clipSize, (fogRadius + extraRadius) * 2f * tilesize);
        super.init();
    }

    @Override
    public void load() {
        super.load();
        bottomRegion = Core.atlas.find(name + "-bottom");
        rotatorRegion = Core.atlas.find(name + "-rotator");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, rotatorRegion, region};
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, (fogRadius + extraRadius) * tilesize, Pal.remove);
    }

    public class UnitSignatureRadarBuild extends RadarBuild {

        public Seq<Signature> signatures = new Seq<>();
        public float boostWarmup;
        public int lastCycle = -1;

        @Override
        public void updateTile() {
            boostWarmup = Mathf.lerpDelta(boostWarmup, optionalEfficiency, 0.1f);

            if (optionalEfficiency > 0f && timer(timerUse, useTime / timeScale)) {
                consume();
            }

            super.updateTile();

            int cycle = (int)(Time.globalTime / signatureCycle);
            if (cycle != lastCycle) {
                lastCycle = cycle;
                refreshSignatures();
            }
        }

        public float boostf(){
            return Mathf.lerp(1f, boostedRadiusScl, boostWarmup);
        }

        @Override
        public float fogRadius(){
            return fogRadius * progress * smoothEfficiency * boostf();
        }

        public float signatureDetectingRange() {
            return (fogRadius + extraRadius) * progress * smoothEfficiency * boostf();
        }

        public float signatureVisualRadius(Unit unit) {
            return unit.type.hitSize / tilesize * signatureSizeScl;
        }

        void refreshSignatures() {
            signatures.clear();

            float range = signatureDetectingRange() * tilesize;
            if (range <= 0f) return;

            float rangeSq = range * range;

            for (Unit u : Groups.unit) {
                if (u == null || u.dead || u.team == team) continue;
                if (u.dst2(x, y) > rangeSq) continue;
                if (fogControl.isVisible(team, u.x, u.y)) continue;

                signatures.add(new Signature(u.x, u.y, signatureVisualRadius(u), u.team.color.cpy()));
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            float z = Draw.z();
            Draw.z(Layer.fogOfWar + 1f);
            Drawf.dashCircle(x, y, signatureDetectingRange() * tilesize, Pal.remove);
            Draw.z(z);
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            Draw.rect(rotatorRegion, x, y, rotateSpeed * totalProgress);

            if (glowRegion.found()) {
                Drawf.additive(glowRegion, glowColor, glowColor.a * (1f - glowMag + Mathf.absin(glowScl, glowMag)), x, y, rotateSpeed * totalProgress, Layer.blockAdditive);
            }

            Draw.rect(region, x, y);

            // маркеры видны только команде радара
            if (player == null || player.team() != team) return;
            if (signatures.isEmpty()) return;

            // фаза цикла — глобально общая для всех радаров через globalTime
            float phase = (Time.globalTime / signatureCycle) % 1f;
            float fin = Interp.pow2Out.apply(phase);

            Draw.z(Layer.fogOfWar + 1f);
            Lines.stroke(Scl.scl((1f - fin) * signatureStroke + 0.1f));

            for (Signature s : signatures) {
                Draw.color(s.color);
                Lines.circle(s.x, s.y, s.radius * tilesize * fin);
            }

            Draw.reset();
        }

        public static class Signature {
            public float x, y, radius;
            public Color color;

            public Signature(float x, float y, float radius, Color color) {
                this.x = x;
                this.y = y;
                this.radius = radius;
                this.color = color;
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(boostWarmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boostWarmup = read.f();
        }
    }
}
