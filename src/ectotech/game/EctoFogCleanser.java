package ectotech.game;

import arc.Events;
import mindustry.game.EventType.GameOverEvent;
import mindustry.game.EventType.SectorCaptureEvent;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Call;

import static mindustry.Vars.*;

public final class EctoFogCleanser{
    private static boolean installed;

    private EctoFogCleanser(){}

    public static void install(){
        if (installed) return;
        installed = true;

        Events.on(SectorCaptureEvent.class, e -> {
            if (net.client() || e.sector != state.getSector() || !shouldClear()) return;
            clearFog();
        });

        Events.on(GameOverEvent.class, e -> {
            if (net.client() || state.isCampaign() || e.winner == null || e.winner == Team.derelict || !shouldClear()) return;
            clearFog();
        });
    }

    private static boolean shouldClear(){
        return !state.isEditor() && state.rules.mode() != Gamemode.sandbox && EctoRules.of(state.rules).clearFogOnCapture;
    }

    private static void clearFog(){
        if(!state.rules.fog && !state.rules.staticFog) return;

        state.rules.fog = false;
        state.rules.staticFog = false;

        Call.setRules(state.rules);
    }
}