package ectotech.world.blocks.power;

import ectotech.content.EctoAttributes;
import ectotech.world.blocks.environment.SteamGeyser;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.power.ThermalGenerator;

public class GeyserGenerator extends ThermalGenerator {

    public float activeOutput = 70f / 60f;
    public float passiveOutput = 3f / 60f;

    public GeyserGenerator(String name) {
        super(name);
        size = 3;
        ignoreBuildDarkness = true;
        attribute = EctoAttributes.geyser;
        powerProduction = activeOutput;

        buildType = GeyserGeneratorBuild::new;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return tile.floor() instanceof SteamGeyser geyser && geyser.isCenterVent(tile);
    }

    public class GeyserGeneratorBuild extends GeneratorBuild {
        @Override
        public void updateTile() {
            productionEfficiency = getPhaseEfficiency();
            super.updateTile();
        }

        private float getPhaseEfficiency() {
            if (tile == null || !(tile.floor() instanceof SteamGeyser geyser)) return 0f;

            return geyser.isActivePhase(tile) ? 1f : passiveOutput / activeOutput;
        }
    }
}
