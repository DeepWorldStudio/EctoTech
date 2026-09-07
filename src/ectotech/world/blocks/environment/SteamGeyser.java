package ectotech.world.blocks.environment;

import arc.Events;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Time;
import ectotech.content.EctoAttributes;
import ectotech.content.EctoShaders;
import ectotech.content.EctoSounds;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.SteamVent;

import static mindustry.Vars.tilesize;

public class SteamGeyser extends SteamVent {

    public float activeTime = 900f;
    public float passiveTime = 1800f;

    public float activeEfficiency = 1f;
    public float passiveEfficiency = 0.04f;

    public float unitDamageTaken = 0.168f;

    public Color geyserWaterColor = Color.valueOf("4a9eff");

    private static final float activeEffectSpacing = 4f;
    private static final float passiveEffectSpacing = 25f;

    private static final float passiveVolume = 0.01f;
    private static final float activeVolume = 0.04f;

    private static final IntSet damagedUnits = new IntSet();
    private static final Seq<Tile> centers = new Seq<>();

    public TextureRegion[] waterRegions;

    private static boolean registered = false;

    public SteamGeyser(String name) {
        super(name);
        statusDuration = 120f;
        placeableOn = false;
        variants = 3;
        attributes.set(EctoAttributes.geyser, 1f);

        if (!registered) {
            registered = true;

            Events.run(EventType.Trigger.update, () -> {
                if (Vars.state.isGame() && !Vars.state.isEditor() && !Vars.state.isPaused()) {
                    if (!Vars.net.client()) updateGeysers();
                    if (!Vars.headless) updateGeysersSounds();
                }
            });

            Events.run(EventType.Trigger.draw, SteamGeyser::drawWaterOverlays);

            Events.on(EventType.WorldLoadEvent.class, e -> {
                centers.clear();
                for (int x = 0; x < Vars.world.width(); x++) {
                    for (int y = 0; y < Vars.world.height(); y++) {
                        Tile tile = Vars.world.tile(x, y);
                        if (tile != null && tile.floor() instanceof SteamGeyser g && g.isCenterVent(tile)) {
                            centers.add(tile);
                        }
                    }
                }
            });

            Events.on(EventType.ResetEvent.class, e -> centers.clear());
        }
    }

    @Override
    public void load() {
        super.load();
        waterRegions = new TextureRegion[variantRegions.length];

        for (int i = 0; i < waterRegions.length; i++) {
            waterRegions[i] = arc.Core.atlas.find(name + "-water" + (i + 1));
        }
    }

    @Override
    public void renderUpdate(UpdateRenderState state) {
        Tile center = state.tile.nearby(-1, -1);
        if (center == null) return;

        boolean active = isActivePhase(center);
        boolean hasBuilding = center.build != null;

        if (!hasBuilding) {
            float spacing = active ? activeEffectSpacing : passiveEffectSpacing;
            Color steamColor = Color.valueOf("476269");

            if ((state.data += Time.delta) >= spacing) {
                if (active) {
                    Fx.ventSteam.at(center.worldx(), center.worldy(), steamColor);
                    if (Mathf.chance(0.4f)) {
                        Fx.hitLiquid.at(center.worldx(), center.worldy(), Color.valueOf("4a9eff"));
                    }
                } else {
                    Fx.vapor.at(center.worldx(), center.worldy(), steamColor);
                }
                state.data = 0f;
            }
        }
    }

    public static void drawWaterOverlays() {
        if (Vars.headless || centers.isEmpty()) return;
        if (EctoShaders.geyserWater == null) return;

        centers.sort(t -> -t.y);

        for (Tile center : centers) {
            if (!(center.floor() instanceof SteamGeyser geyser)) continue;
            if (geyser.isNaturallyBlocked(center)) continue;

            Tile topRight = center.nearby(1, 1);
            if (topRight == null) continue;

            int variant = Mathf.randomSeed(topRight.pos(), 0, Math.max(0, geyser.variantRegions.length - 1));

            if (center.build == null) {
                float alpha = geyser.calcWaterAlpha(center);
                if (alpha > 0.01f) {
                    TextureRegion waterRegion = geyser.waterRegions[variant];
                    if (waterRegion != null && waterRegion.found()) {
                        Draw.draw(Layer.floor + 0.011f + (center.y * 0.00001f), () -> {
                            EctoShaders.geyserWater.waterColor.set(geyser.geyserWaterColor);
                            Draw.shader(EctoShaders.geyserWater);
                            Draw.color(Color.white, alpha);
                            Draw.rect(waterRegion, center.worldx(), center.worldy());
                            Draw.shader();
                            Draw.color();
                        });
                    }
                }
            }
        }
        }

    public float calcWaterAlpha(Tile center) {
        float pt = phaseTime(center);

        if (pt < activeTime) {
            float progress = pt / activeTime;
            return Mathf.pow(1f - progress, 2.5f);
        } else {
            float progress = (pt - activeTime) / passiveTime;
            return Mathf.pow(progress, 2.5f);
        }
    }

    public static void updateGeysers() {
        if (Vars.net.client()) return;

        damagedUnits.clear();

        for (Tile tile : centers) {
            if (!(tile.floor() instanceof SteamGeyser geyser)) continue;

            if (!geyser.isActivePhase(tile)) continue;
            if (tile.build != null) continue;

            // Гейзер свободен! Дамажим юнитов сразу в радиусе 3х3 (1.5 тайла от центра)
            float radius = tilesize * 1.5f;
            Groups.unit.intersect(tile.worldx() - radius, tile.worldy() - radius, radius * 2, radius * 2, u -> {
                if (u.isGrounded() && !u.type.hovering && damagedUnits.add(u.id)) {
                    u.damage(geyser.unitDamageTaken * Time.delta);
                    u.apply(StatusEffects.wet, geyser.statusDuration);
                }
            });
        }
    }


    public static void updateGeysersSounds() {
        if (Vars.headless) return;

        for (Tile center : centers) {
            if (!(center.floor() instanceof SteamGeyser geyser)) continue;
            if (geyser.isNaturallyBlocked(center)) continue;

            boolean active = geyser.isActivePhase(center);
            boolean hasBuilding = center.build != null;

            // Loop-звук через агрегатор
            Sound loopSound = active ? EctoSounds.loopGeyserActive : EctoSounds.loopGeyserInactive;
            float volume = active ? activeVolume : passiveVolume;
            if (hasBuilding) volume *= 0.35f;

            Vars.control.sound.loop(loopSound, center, volume);

            // Одноразовый звук перехода passive -> active
            float pt = geyser.phaseTime(center);
            if (pt < Time.delta && active) {
                float eruptVol = hasBuilding ? 0.003f : 0.04f;
                EctoSounds.geyserEruption.at(center.worldx(), center.worldy(), 1f, eruptVol);
            }
        }
    }

    public float phaseTime(Tile center) {
        if (center == null || !isCenterVent(center)) return 0f;

        float cycle = activeTime + passiveTime;
        if (cycle <= 0f) return 0f;

        float offset = Mathf.randomSeed(center.pos(), 0f, cycle);
        return (Time.time + offset) % cycle;
    }

    public boolean isActivePhase(Tile center) {
        return center != null &&
                isCenterVent(center) &&
                !isNaturallyBlocked(center) &&
                phaseTime(center) < activeTime;
    }

    public float phaseEfficiency(Tile center) {
        if (center == null || !isCenterVent(center) || isNaturallyBlocked(center)) return 0f;
        return isActivePhase(center) ? activeEfficiency : passiveEfficiency;
    }

    public boolean isNaturallyBlocked(Tile center) {
        return center != null && center.block().solid && center.build == null;
    }
}
