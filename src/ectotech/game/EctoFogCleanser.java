package ectotech.game;

import arc.Events;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.EventType.SectorCaptureEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.type.Sector;

import static mindustry.Vars.*;

public final class EctoFogCleanser {

    private EctoFogCleanser(){}

    public static void install() {

        Events.on(SectorCaptureEvent.class, e -> {
            if (!net.client() && e.sector == state.getSector() && shouldClear())
                clearFog(true);
        });

        Events.on(GameOverEvent.class, e -> {
            if (!net.client() && !state.isCampaign() && e.winner != Team.derelict && shouldClear())
                clearFog(true);
        });

        Events.on(WorldLoadEvent.class, e -> {
            Sector sector = state.rules.sector;
            if (net.client() && state.isCampaign() && sector != null && sector.info.wasCaptured && sector.info.hasCore && shouldClear())
                clearFog(false);
        });
    }

    static boolean shouldClear(){
        return !state.isEditor()
                && state.rules.mode() != Gamemode.sandbox
                && EctoRules.of(state.rules).clearFogOnCapture;
    }

    static void clearFog(boolean sync){
        state.rules.fog = state.rules.staticFog = false;
        fogControl.resetFog();

        if (sync) Call.setRules(state.rules);
    }
}
