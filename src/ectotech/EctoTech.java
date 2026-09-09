package ectotech;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import ectotech.content.*;
import ectotech.game.EctoFogCleanser;
import ectotech.graphics.EctoVanillaSpritesSwapper;
import ectotech.ui.EctoTeamsUI;
import ectotech.ui.dialogs.EctoCampaignRulesDialog;
import ectotech.ui.dialogs.EctoCustomRulesDialog;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.mod.Mod;
import ectotech.game.EctoTeams;

public class EctoTech extends Mod {

    public static Team ectorumTeam;

    public EctoTech() {
        Log.info("Loaded EctoTech constructor.");
    }

    @Override
    public void init() {
        EctoTeams.init();

        EctoFogCleanser.install();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            Vars.ui.campaignRules = new EctoCampaignRulesDialog();

            EctoCustomRulesDialog.install();
            EctoTeamsUI.install();

            EctoVanillaSpritesSwapper.register(Items.sand, "sand");
            EctoVanillaSpritesSwapper.register(Items.graphite, "graphite");
            EctoVanillaSpritesSwapper.register(Items.silicon, "silicon");
            EctoVanillaSpritesSwapper.register(Items.thorium, "thorium");

            EctoVanillaSpritesSwapper.updateUI();

            Time.runTask(10f, () -> Log.info("EctoTech: Client loaded"));
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            EctoVanillaSpritesSwapper.apply(Vars.state.rules.planet == EctoPlanets.ectorum);

        });

        Events.on(EventType.SectorCaptureEvent.class, e -> {

        });

        Events.on(EventType.ResetEvent.class, e -> EctoVanillaSpritesSwapper.apply(false));

        Log.info("EctoTech initialized.");
    }

    @Override
    public void loadContent() {
        Log.info("Loading EctoTech content...");

        EctoAttributes.load();
        EctoSounds.load();
        EctoItems.load();
        EctoLiquids.load();
        EctoStatusEffects.load();
        EctoUnitTypes.load();
        EctoShaders.load();
        EctoBlocks.load();
        EctoPlanets.load();
        EctoSectorPresets.load();
        EctorumTechTree.load();

        Log.info("EctoTech content loaded successfully.");
    }
}