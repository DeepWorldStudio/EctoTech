package ectotech;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import ectotech.content.*;
import ectotech.game.EctoAttributes;
import ectotech.graphics.EctoUiPlanetReader;
import ectotech.graphics.EctoVanillaSpritesSwapper;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.mod.Mod;
import ectotech.game.EctoTeams;
import mindustry.type.Planet;

import static ectotech.EctoVars.pressureExplosionsEnabled;

public class EctoTech extends Mod {

    public static Team ectoTeam;

    public EctoTech() {
        Log.info("Loaded EctoTech constructor.");
    }

    @Override
    public void init() {
        EctoTeams.init();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> Log.info("EctoTech: Client loaded"));

            EctoVanillaSpritesSwapper.register(Items.sand, "ectotech-sand");
            EctoVanillaSpritesSwapper.register(Items.graphite, "ectotech-graphite");
            EctoVanillaSpritesSwapper.register(Items.silicon, "ectotech-silicon");

            Runnable restoreFromGame = () -> {
                boolean ectorum = Vars.state.isPlaying()
                        && Vars.state.rules.planet == EctoPlanets.ectorum;

                EctoVanillaSpritesSwapper.apply(ectorum);
            };

            Vars.ui.database.update(() -> {
                if (!Vars.ui.database.isShown()) return;
                Planet p = EctoUiPlanetReader.fromDatabase(Vars.ui.database);
                EctoVanillaSpritesSwapper.apply(p == EctoPlanets.ectorum);
            });
            Vars.ui.database.hidden(restoreFromGame);

            Vars.ui.research.update(() -> {
                if (!Vars.ui.research.isShown()) return;
                Planet p = EctoUiPlanetReader.fromResearch(Vars.ui.research);
                EctoVanillaSpritesSwapper.apply(p == EctoPlanets.ectorum);
            });
            Vars.ui.research.hidden(restoreFromGame);
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            EctoVanillaSpritesSwapper.apply(Vars.state.rules.planet == EctoPlanets.ectorum);

            pressureExplosionsEnabled = Vars.state.rules.tags.getBool("ectotech-pressure-explosions");



        });

        Events.on(EventType.ResetEvent.class, e -> EctoVanillaSpritesSwapper.apply(false));

        Log.info("EctoTech initialized.");
    }

    @Override
    public void loadContent() {
        Log.info("Loading EctoTech content...");

        EctoAttributes.load();
        EctoItems.load();
        EctoLiquids.load();
        EctoUnitTypes.load();
        EctoBlocks.load();
        EctoPlanets.load();
        EctoSectorPresets.load();
        EctoTechTree.load();

        Log.info("EctoTech content loaded successfully.");
    }
}