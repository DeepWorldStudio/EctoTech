package ectotech.world.blocks.production;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Lod;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.production.BeamDrill;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class BurstBeamDrill extends BeamDrill {

    public float shake = 2f;

    /**Items multiplier for 1 beam for 1 burst*/
    public int burstMultiplier = 10;

    public Interp speedCurve = Interp.pow2In;

    public float invertedTime = 100f;

    public Effect drillEffect = Fx.shockwaveSmaller;

    public Sound drillSound = Sounds.shootAfflict;
    public float drillSoundVolume = 0.6f, drillSoundPitchRand = 0.1f;

    public BurstBeamDrill(String name) {
        super(name);

        ambientSound = Sounds.drillCharge;
        ambientSoundVolume = 0.18f;

        buildType = BurstBeamDrillBuild::new;
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("drillspeed");

        addBar("drillspeed", (BurstBeamDrillBuild e) ->
                new Bar(() -> Core.bundle.format("bar.drillspeed", Strings.fixed(e.lastDrillSpeed * 60f * e.timeScale(), 2)), () -> Pal.ammo, () -> e.time / getDrillTime(e.lastItem)));
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.remove(Stat.drillSpeed);
        stats.add(Stat.drillSpeed, 60f / drillTime * size * burstMultiplier, StatUnit.itemsSecond);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Item item = null, invalidItem = null;
        boolean multiple = false;
        int count = 0;

        for (int i = 0; i < size; i++) {
            nearbySide(x, y, rotation, i, Tmp.p1);

            int j = 0;
            Item found = null;
            for (; j < range; j++) {
                int rx = Tmp.p1.x + Geometry.d4x(rotation)*j, ry = Tmp.p1.y + Geometry.d4y(rotation)*j;
                Tile other = world.tile(rx, ry);
                if (other != null && other.solid()) {
                    Item drop = other.wallDrop();
                    if (drop != null) {
                        if (drop.hardness <= tier && (blockedItems == null || !blockedItems.contains(drop))) {
                            found = drop;
                            count++;
                        } else {
                            invalidItem = drop;
                        }
                    }
                    break;
                }
            }

            if (found != null) {
                //check if multiple items will be drilled
                if(item != found && item != null){
                    multiple = true;
                }
                item = found;
            }

            int len = Math.min(j, range - 1);
            Drawf.dashLine(found == null ? Pal.remove : Pal.placing,
                    Tmp.p1.x * tilesize,
                    Tmp.p1.y *tilesize,
                    (Tmp.p1.x + Geometry.d4x(rotation)*len) * tilesize,
                    (Tmp.p1.y + Geometry.d4y(rotation)*len) * tilesize
            );
        }

        if (item != null) {
            float width = drawPlaceText(Core.bundle.formatFloat("bar.drillspeed", 60f / getDrillTime(item) * count * burstMultiplier, 2), x, y, valid);
            if (!multiple) {
                float dx = x * tilesize + offset - width/2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5, s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(item.fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(item.fullIcon, dx, dy, s, s);
            }
        } else if (invalidItem != null) {
            drawPlaceText(Core.bundle.get("bar.drilltierreq"), x, y, false);
        }
    }

    @Override
    public void init() {
        itemCapacity = Math.max(itemCapacity, size * burstMultiplier);
        super.init();
    }

    public class BurstBeamDrillBuild extends BeamDrillBuild {
        public float smoothProgress = 0f;
        public float invertTime = 0f;

        public float dotPhase = 0f;

        @Override
        public boolean shouldConsume() {
            int burstAmount = facingAmount * burstMultiplier;

            return lastItem != null && burstAmount > 0 && items.total() <= itemCapacity - burstAmount && enabled;
        }

        @Override
        public void updateTile() {
            if (lasers[0] == null) updateLasers();

            updateFacing();

            if (invertTime > 0f) invertTime = Math.max(invertTime - delta() / Math.max(invertedTime, 0.0001f), 0f);

            warmup = Mathf.approachDelta(warmup, Mathf.num(efficiency > 0 && facingAmount > 0), 1f / 60f);
            boostWarmup = Mathf.lerpDelta(boostWarmup, optionalEfficiency, 0.1f);

            float drillTime = getDrillTime(lastItem);

            int burstAmount = facingAmount * burstMultiplier;
            boolean canWork = lastItem != null && burstAmount > 0 && items.total() <= itemCapacity - burstAmount && efficiency > 0f && enabled;

            if (canWork) {
                float multiplier = Mathf.lerp(1f, optionalBoostIntensity, optionalEfficiency);
                float speed = multiplier * efficiency;

                time += delta() * speed;

                float chargeNow = drillTime <= 0f ? 0f : Mathf.clamp(time / drillTime);
                float dotRamp = Interp.pow2In.apply(chargeNow);
                float currentDotSpeed = Mathf.lerp(0.05f, 1.3f, dotRamp);
                dotPhase += delta() * currentDotSpeed;

                lastDrillSpeed = (facingAmount * burstMultiplier * multiplier * timeScale) / drillTime * efficiency;
            } else {
                lastDrillSpeed = 0f;
            }

            float charge = drillTime <= 0f ? 0f : Mathf.clamp(time / drillTime);
            smoothProgress = Mathf.lerpDelta(smoothProgress, canWork ? speedCurve.apply(charge) : 0f, canWork ? 0.1f : 0.3f);

            if (canWork && time >= drillTime) {
                burst();
                time %= drillTime;
            }

            if (timer(timerDump, dumpTime / timeScale)) {
                dump();
            }
        }

        protected void burst() {
            int burstAmount = facingAmount * burstMultiplier;

            for (int i = 0; i < burstAmount; i++) {
                offload(lastItem);
            }

            invertTime = 1f;

            if (wasVisible) {
                Effect.shake(shake, shake, this);
                drillSound.at(x, y, 1f + Mathf.range(drillSoundPitchRand), drillSoundVolume);

                var dir = Geometry.d4(rotation);
                for (int i = 0; i < size; i++) {
                    Tile face = facing[i];
                    if (face == null) continue;
                    Item drop = face.wallDrop();
                    if (drop == null) continue;

                    float lx = face.worldx() - (dir.x / 2f) * tilesize;
                    float ly = face.worldy() - (dir.y / 2f) * tilesize;
                    drillEffect.at(lx, ly, drop.color);
                }
            }
        }

        @Override
        public float ambientVolume() {
            float drillTime = getDrillTime(lastItem);
            float charge = drillTime <= 0f ? 0f : Mathf.clamp(time / drillTime);
            return super.ambientVolume() * Mathf.pow(charge, 4f);
        }

        @Override
        public void draw() {
            Draw.rect(block.region, x, y);
            Draw.rect(topRegion, x, y, rotdeg());

            if (isPayload()) return;

            var dir = Geometry.d4(rotation);

            int ddx = Geometry.d4x(rotation + 1);
            int ddy = Geometry.d4y(rotation + 1);

            float visualCharge = Mathf.clamp(smoothProgress);
            float burstFlash = Interp.pow3Out.apply(Mathf.clamp(invertTime));

            float beamCharge = Mathf.clamp(visualCharge + burstFlash * 0.15f);
            float dotCharge = Mathf.clamp(visualCharge);
            float sparkCharge = Mathf.clamp(Mathf.curve(visualCharge, 0.4f, 1f) + burstFlash * 0.45f);
            float glowCharge = Mathf.clamp(visualCharge * visualCharge + burstFlash * 0.65f);

            for (int i = 0; i < size; i++) {
                Tile face = facing[i];

                if (face == null) continue;

                Item drop = face.wallDrop();

                if (drop == null) continue;

                Point2 start = lasers[i];

                float lx = face.worldx() - dir.x * tilesize / 2f;
                float ly = face.worldy() - dir.y * tilesize / 2f;

                boolean adjacent = Math.abs(start.x - face.x) + Math.abs(start.y - face.y) == 0;

                float beamWidth = laserWidth * warmup * Mathf.lerp(0.6f, 1.15f, beamCharge);
                float beamAlpha = warmup * Mathf.lerp(0.14f, 1f, beamCharge);

                Draw.z(Layer.power - 1f);

                if (adjacent) {
                    Draw.scl(beamWidth);

                    if (boostWarmup < 0.99f) {
                        Draw.alpha(beamAlpha * (1f - boostWarmup));

                        Draw.rect(laserCenter, lx, ly);
                    }

                    if (boostWarmup > 0.01f) {
                        Draw.alpha(beamAlpha * boostWarmup);

                        Draw.rect(laserCenterBoost, lx, ly);
                    }

                    Draw.scl();
                } else {
                    float lsx = (start.x - dir.x / 2f) * tilesize;
                    float lsy = (start.y - dir.y / 2f) * tilesize;

                    if (boostWarmup < 0.99f) {
                        Draw.alpha(beamAlpha * (1f - boostWarmup));

                        Drawf.laser(laser, laserEnd, lsx, lsy, lx, ly, beamWidth);
                    }

                    if (boostWarmup > 0.01f) {
                        Draw.alpha(beamAlpha * boostWarmup);

                        Drawf.laser(laserBoost, laserEndBoost, lsx, lsy, lx, ly, beamWidth);
                    }

                    Draw.color();

                    if (Lod.l2 && dotCharge > 0.001f) {
                        float beamLength = Mathf.dst(lsx, lsy, lx, ly);

                        if (beamLength > 0.001f) {
                            Draw.z(Layer.power - 0.999f);

                            float dotRamp = Interp.pow2In.apply(dotCharge);

                            int maxDots = size * size;
                            float dotDensity = Mathf.lerp(1f, maxDots, dotRamp);

                            float linePhase = Mathf.randomSeed((long)id * 31L + i * 17L, 0f, 1f);

                            float whiteMix = Mathf.lerp(0.45f, 1f, Mathf.curve(visualCharge, 0.35f, 1f));
                            float lateCharge = Math.max(Mathf.curve(visualCharge, 0.7f, 1f), burstFlash);
                            Color dotColor = Tmp.c2.set(heatColor).lerp(boostHeatColor, boostWarmup).lerp(Color.white, whiteMix);

                            for (int j = 0; j < maxDots; j++) {
                                float visibility = Mathf.clamp(dotDensity - j);

                                if (visibility <= 0.001f) continue;

                                float pointPhase = dotPhase / beamLength + j / (float)maxDots + linePhase;
                                float progress = pointPhase % 1f;

                                float px = Mathf.lerp(lsx, lx, progress);
                                float py = Mathf.lerp(lsy, ly, progress);

                                float pulse = Mathf.lerp(1f, 0.9f + Mathf.absin(Time.time + id * 7f + i * 11f + j * 5f, 3f, 0.12f), lateCharge);

                                float travelGrowth = Mathf.lerp(0.85f, 1.8f, Interp.pow3In.apply(progress));
                                float dotRadius = Mathf.lerp(0.75f, 1.3f, dotCharge) * travelGrowth * pulse;
                                float dotAlpha = warmup * visibility * Mathf.lerp(0.42f, 1f, dotCharge) * Lod.alpha2;

                                Draw.color(dotColor, dotAlpha);
                                Fill.circle(px, py, dotRadius);

                                float coreAlpha = dotAlpha * Mathf.curve(visualCharge, 0.6f, 1f) * 0.95f;
                                if (coreAlpha > 0.001f) {
                                    Draw.color(Color.white, coreAlpha);
                                    Fill.circle(px, py, dotRadius * 0.42f);
                                }
                            }
                            Draw.color();
                        }
                    }
                }

                Draw.color();

                if (Lod.l2 && sparkCharge > 0.001f) {
                    Draw.z(Layer.effect);

                    Lines.stroke(warmup * Mathf.lerp(0.35f, 1f, sparkCharge));

                    rand.setState(i, id);

                    Color spark = Tmp.c3.set(sparkColor).lerp(boostHeatColor, boostWarmup);

                    for (int j = 0; j < sparks; j++) {
                        float visibility = Mathf.clamp(sparkCharge * sparks - j);

                        float fin = (Time.time / sparkLife + rand.random(sparkRecurrence + 1f)) % sparkRecurrence;
                        float offset = rand.range(2f);

                        Tmp.v1.set(sparkRange * fin, 0f).rotate(rotdeg() + rand.range(sparkSpread));

                        if (visibility <= 0.001f || fin > 1f) continue;

                        Color result = Tmp.c1.set(spark).lerp(drop.color, fin);

                        Draw.color(result.r, result.g, result.b, result.a * visibility * warmup * Lod.alpha2);

                        float px = Tmp.v1.x;
                        float py = Tmp.v1.y;

                        Lines.lineAngle(lx + px + offset * ddx, ly + py + offset * ddy, Angles.angle(px, py), Mathf.slope(fin) * sparkSize * Mathf.lerp(0.65f, 1f, sparkCharge));
                    }
                    Draw.reset();
                }
            }

            if (glowRegion.found()) {
                Color glow = Tmp.c1.set(heatColor).lerp(boostHeatColor, boostWarmup);

                float baseGlowAlpha = glow.a;

                glow.lerp(Color.white, Math.max(burstFlash * 0.7f, Mathf.curve(visualCharge, 0.75f, 1f) * 0.35f));

                Draw.z(Layer.blockAdditive);
                Draw.blend(Blending.additive);

                Draw.color(glow, warmup * glowCharge * baseGlowAlpha);

                Draw.rect(glowRegion, x, y, rotdeg());

                Draw.blend();
                Draw.color();
            }

            Draw.blend();
            Draw.reset();
        }
    }
}
