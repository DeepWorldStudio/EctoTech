package ectotech.game;

import mindustry.game.CampaignRules;
import mindustry.game.Rules;
import mindustry.type.Planet;

/**
 * Правила кампании EctoTech. Расширяет ванильный CampaignRules, чтобы
 * Planet.applyRules() и CampaignRulesDialog применяли и сохраняли наши поля
 * через штатный код, без дополнительных хуков.
 */
public class EctoCampaignRules extends CampaignRules {
    public boolean clearFogOnCapture = false;

    @Override
    public void apply(Planet planet, Rules rules) {
        super.apply(planet, rules);

        EctoRules ecto = EctoRules.of(rules);

        ecto.clearFogOnCapture = clearFogOnCapture;

        EctoRules.save(rules, ecto);
    }
}