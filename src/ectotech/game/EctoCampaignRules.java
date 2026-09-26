package ectotech.game;

import mindustry.game.CampaignRules;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.type.Planet;
import mindustry.type.Sector;

public class EctoCampaignRules extends CampaignRules{
    public boolean clearFogOnCapture = false;

    @Override
    public void apply(Planet planet, Rules rules){
        super.apply(planet, rules);

        EctoRules ecto = EctoRules.of(rules);
        ecto.clearFogOnCapture = clearFogOnCapture;
        EctoRules.save(rules, ecto);

        Sector sector = rules.sector;

        //Apply the captured-sector exception after vanilla campaign rules.
        if(clearFogOnCapture && !rules.editor && rules.mode() != Gamemode.sandbox && sector != null && sector.planet == planet && sector.info.wasCaptured && sector.info.hasCore){
            rules.fog = false;
            rules.staticFog = false;
        }
    }
}