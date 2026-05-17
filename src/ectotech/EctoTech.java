package ectotech;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import ectotech.content.*;
import ectotech.game.EctoAttributes;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.mod.Mod;
import ectotech.game.EctoTeams;

import static ectotech.EctoVars.pressureExplosionsEnabled;

public class EctoTech extends Mod {

    public static Team ectoTeam;

    public EctoTech(){
        Log.info("Loaded EctoTech constructor.");
    }

    @Override
    public void init(){
        EctoTeams.init();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> Log.info("EctoTech: Client loaded"));
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            pressureExplosionsEnabled = Vars.state.rules.tags.getBool("ectotech-pressure-explosions");
        });

        Log.info("EctoTech initialized.");
    }

    @Override
    public void loadContent(){
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