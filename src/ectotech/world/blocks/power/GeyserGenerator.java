package ectotech.world.blocks.power;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.content.EctoAttributes;
import ectotech.world.blocks.environment.SteamGeyser;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.power.ThermalGenerator;

public class GeyserGenerator extends ThermalGenerator {
    public float productionChangeSpeed = 0.02f;

    public GeyserGenerator(String name) {
        super(name);
        size = 3;
        ignoreBuildDarkness = true;

        squareSprite = false;

        noUpdateDisabled = false;
        attribute = EctoAttributes.geyser;

        buildType = GeyserGeneratorBuild::new;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return tile.floor() instanceof SteamGeyser geyser && geyser.isCenterVent(tile);
    }

    public class GeyserGeneratorBuild extends GeneratorBuild {

        public float spinWarmup = 0f;
        public float spinProgress = Time.time;

        @Override
        public void updateTile() {
            float targetEfficiency  = getPhaseEfficiency();
            productionEfficiency = Mathf.approachDelta(productionEfficiency, targetEfficiency , productionChangeSpeed);

            super.updateTile();

            updateGenerateEffect();

            float targetSpinSpeed = enabled ? targetEfficiency : 0f;
            spinWarmup = Mathf.approachDelta(spinWarmup, targetSpinSpeed, productionChangeSpeed);
            spinProgress += spinWarmup * delta();
        }

        private float getPhaseEfficiency() {
            if (tile == null || !(tile.floor() instanceof SteamGeyser geyser)) return 0f;

            return geyser.phaseEfficiency(tile);
        }

        private void updateGenerateEffect() {
            if (tile == null || !(tile.floor() instanceof SteamGeyser geyser)) return;
            if (!enabled || productionEfficiency <= 0.001f) return;

            float chance = effectChance * (geyser.isActivePhase(tile) ? 2f : 1f);

            if (Mathf.chanceDelta(chance)) {
                generateEffect.at(
                        x + Mathf.range(size * 3f),
                        y + Mathf.range(size * 3f)
                );
            }
        }

        @Override
        public float warmup() {
            return spinWarmup;
        }

        @Override
        public float totalProgress() {
            return spinProgress;
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            write.f(spinWarmup);
            write.f(spinProgress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            if (revision >= 2) {
                spinWarmup = read.f();
                spinProgress = read.f();
            } else {
                spinWarmup = super.warmup();
                spinProgress = Time.time;
            }
        }
    }
}
