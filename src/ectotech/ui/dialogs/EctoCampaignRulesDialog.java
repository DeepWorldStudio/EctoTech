package ectotech.ui.dialogs;

import arc.Core;
import arc.func.Boolc;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import ectotech.content.EctoPlanets;
import ectotech.game.EctoCampaignRules;
import mindustry.type.Planet;
import mindustry.ui.dialogs.CampaignRulesDialog;

/**
 * Кампанийный диалог, дополняющий ванильный секцией EctoTech.
 * Сохранение и живое применение делает ванильный CampaignRulesDialog:
 * planet.saveRules() сериализует по рантайм-классу объекта, а
 * planet.campaignRules.apply(...) — виртуальный вызов, попадающий в
 * EctoCampaignRules.apply. Поэтому этот класс отвечает только за UI.
 */
public class EctoCampaignRulesDialog extends CampaignRulesDialog {

    private Planet currentPlanet;

    public EctoCampaignRulesDialog() {
        super();

        onResize(this::appendEctoRules);
    }

    @Override
    public void show(Planet planet) {
        this.currentPlanet = planet;
        super.show(planet);
        appendEctoRules();
    }

    private void appendEctoRules(){
        if(currentPlanet != EctoPlanets.ectorum) return;
        if(!(currentPlanet.campaignRules instanceof EctoCampaignRules rules)) return;

        if(cont.getChildren().isEmpty()) return;
        if(!(cont.getChildren().first() instanceof ScrollPane pane)) return;
        if(!(pane.getWidget() instanceof Table inner)) return;

        String text = "@rules.ectotech.clearfogoncapture";
        var cell = inner.check(text, value -> rules.clearFogOnCapture = value)
                .checked(rules.clearFogOnCapture);

        if(Core.bundle.has(text.substring(1) + ".info")){
            cell.tooltip(text + ".info");
        }

        cell.get().left();
        inner.row();
    }
}