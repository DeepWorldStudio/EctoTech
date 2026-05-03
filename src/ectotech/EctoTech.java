package ectotech;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import ectotech.content.*;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.mod.Mod;
import ectotech.game.EctoTeams;

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

        Log.info("EctoTech initialized.");
    }

    @Override
    public void loadContent(){
        Log.info("Loading EctoTech content...");

        EctoItems.load();
        EctoLiquids.load();
        EctoBlocks.load();
        EctoPlanets.load();
        EctoTechTree.load();

        Log.info("EctoTech content loaded successfully.");
    }
}