package ectotech.ui.dialogs;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.scene.Element;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import ectotech.game.EctoRules;
import ectotech.game.EctoTeams;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.editor.MapEditorDialog;
import mindustry.editor.MapInfoDialog;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.PausedDialog;

/**
 * Добавляет правила EctoTech в существующие ванильные CustomRulesDialog.
 * Сейчас расширяются:
 * 1. редактор карты -> информация о карте -> правила;
 * 2. меню паузы -> изменение правил текущей игры.
 */
public final class EctoCustomRulesDialog {

    private static final Seq<CustomRulesDialog> installedDialogs = new Seq<>();

    private EctoCustomRulesDialog() {
    }

    public static void install() {
        installEditorRulesDialog();
        installPausedRulesDialog();
    }

    private static void installEditorRulesDialog() {
        try {
            MapInfoDialog infoDialog = Reflect.get(MapEditorDialog.class, Vars.ui.editor, "infoDialog");
            CustomRulesDialog rulesDialog = Reflect.get(MapInfoDialog.class, infoDialog, "ruleInfo");

            extend(rulesDialog);

        } catch (Throwable error) {
            Log.err("EctoTech: failed to extend the map editor rules dialog.");
            Log.err(error);
        }
    }

    private static void installPausedRulesDialog() {
        try {
            CustomRulesDialog rulesDialog = Reflect.get(PausedDialog.class, Vars.ui.paused, "rulesDialog");

            extend(rulesDialog);

        } catch (Throwable error) {
            Log.err("EctoTech: failed to extend the paused rules dialog.");
            Log.err(error);
        }
    }

    private static void extend(CustomRulesDialog dialog){
        if(dialog == null || installedDialogs.contains(dialog, true)) return;

        installedDialogs.add(dialog);

        dialog.additionalSetup.insert(0, () -> {
            int previous = dialog.categoryNames.size - dialog.categories.size;

            if(previous > 0){
                dialog.categoryNames.removeRange(0, previous - 1);
            }
        });

        dialog.additionalSetup.add(() -> buildEctoRules(dialog));
    }

    private static void buildEctoRules(CustomRulesDialog dialog){
        Rules rules = Reflect.get(CustomRulesDialog.class, dialog, "rules");
        if(rules == null) return;

        Table previous = dialog.current;
        String previousName = dialog.currentName;

        try{
            int index = dialog.categoryNames.indexOf("teams");

            if(index >= 0 && index < dialog.categories.size
                    && EctoTeams.ectorumTeam != null){
                Table teams = dialog.categories.get(index);

                addTeamChoice(teams, "rules.playerteam",
                        team -> rules.defaultTeam = team,
                        () -> rules.defaultTeam);

                addTeamChoice(teams, "rules.enemyteam",
                        team -> rules.waveTeam = team,
                        () -> rules.waveTeam);

                addAwakenedTeamRules(dialog, rules, teams);
            }

            EctoRules ecto = EctoRules.of(rules);

            dialog.category("ectotech");

            dialog.check("@rules.ectotech.pressureexplosions", value -> {
                ecto.pressureExplosionsEnabled = value;
                EctoRules.save(rules, ecto);
            }, () -> ecto.pressureExplosionsEnabled);

            dialog.number("@rules.ectotech.pressurecriticaldamagemultiplier", value -> {
                ecto.pressureCriticalDamageMultiplier = value;
                EctoRules.save(rules, ecto);
            }, () -> ecto.pressureCriticalDamageMultiplier, 0f, Float.MAX_VALUE);

            dialog.check("@rules.ectotech.clearfogoncapture", value -> {
                ecto.clearFogOnCapture = value;
                EctoRules.save(rules, ecto);
            }, () -> ecto.clearFogOnCapture);
        }finally{
            dialog.current = previous;
            dialog.currentName = previousName;
        }
    }

    private static void addTeamChoice(Table category, String key, Cons<Team> setter, Prov<Team> getter) {

        Team team = EctoTeams.ectorumTeam;
        String title = Core.bundle.get(key);
        String name = "ectotech-" + key;

        for (Element child : category.getChildren()) {
            if(!(child instanceof Table row)) continue;

            boolean labeled = row.getChildren().contains(element ->
                    element instanceof Label label
                            && title.contentEquals(label.getText()));

            boolean buttons = row.getChildren().contains(element ->
                    element instanceof ImageButton);

            if(!labeled || !buttons) continue;
            if(row.find(name) != null) return;

            row.button(Tex.whiteui, Styles.squareTogglei, 38f,
                            () -> setter.get(team))
                    .size(60f)
                    .pad(1f)
                    .name(name)
                    .checked(button -> getter.get() == team)
                    .tooltip(team.coloredName())
                    .with(button -> button.getStyle().imageUpColor = team.color);

            return;
        }
    }

    private static void addAwakenedTeamRules(CustomRulesDialog dialog, Rules rules, Table parent){

        Team team = EctoTeams.ectorumTeam;
        Rules.TeamRule teams = rules.teams.get(team);

        Table fields = new Table();
        fields.left().defaults().fillX().left().pad(5);

        Table previous = dialog.current;
        dialog.current = fields;

        try{
            dialog.number("@rules.blockhealthmultiplier", f -> teams.blockHealthMultiplier = f, () -> teams.blockHealthMultiplier);
            dialog.number("@rules.blockdamagemultiplier", f -> teams.blockDamageMultiplier = f, () -> teams.blockDamageMultiplier);

            dialog.check("@rules.rtsai", b -> teams.rtsAi = b, () -> teams.rtsAi, () -> team != rules.defaultTeam);
            dialog.numberi("@rules.rtsminsquadsize", f -> teams.rtsMinSquad = f, () -> teams.rtsMinSquad, () -> teams.rtsAi, 0, 100);
            dialog.numberi("@rules.rtsmaxsquadsize", f -> teams.rtsMaxSquad = f, () -> teams.rtsMaxSquad, () -> teams.rtsAi, 1, 1000);
            dialog.number("@rules.rtsminattackweight", f -> teams.rtsMinWeight = f, () -> teams.rtsMinWeight, () -> teams.rtsAi);

            dialog.check("@rules.buildai", b -> teams.buildAi = b, () -> teams.buildAi, () -> team != rules.defaultTeam && rules.env != Planets.erekir.defaultEnv && !rules.pvp);
            dialog.number("@rules.buildaitier", false, f -> teams.buildAiTier = f, () -> teams.buildAiTier, () -> teams.buildAi && rules.env != Planets.erekir.defaultEnv && !rules.pvp, 0f, 1f);

            dialog.check("@rules.protectcores", b -> teams.protectCores = b, () -> teams.protectCores);
            dialog.number("@rules.extracorebuildradius", f -> teams.extraCoreBuildRadius = f * Vars.tilesize, () -> Math.min(teams.extraCoreBuildRadius / Vars.tilesize, 200f), () -> !rules.polygonCoreProtection && teams.protectCores);
            dialog.check("@rules.checkplacement", b -> teams.checkPlacement = b, () -> teams.checkPlacement);

            dialog.check("@rules.infiniteresources", b -> teams.infiniteResources = b, () -> teams.infiniteResources);
            dialog.check("@rules.fillitems", b -> teams.fillItems = b, () -> teams.fillItems);
            dialog.number("@rules.buildspeedmultiplier", f -> teams.buildSpeedMultiplier = f, () -> teams.buildSpeedMultiplier, 0.001f, 50f);

            dialog.number("@rules.unitfactoryactivation", f -> teams.unitFactoryActivationDelay = f * 60f, () -> teams.unitFactoryActivationDelay / 60f);
            dialog.number("@rules.unitdamagemultiplier", f -> teams.unitDamageMultiplier = f, () -> teams.unitDamageMultiplier);
            dialog.number("@rules.unitcrashdamagemultiplier", f -> teams.unitCrashDamageMultiplier = f, () -> teams.unitCrashDamageMultiplier);
            dialog.number("@rules.unitminespeedmultiplier", f -> teams.unitMineSpeedMultiplier = f, () -> teams.unitMineSpeedMultiplier);
            dialog.number("@rules.unitbuildspeedmultiplier", f -> teams.unitBuildSpeedMultiplier = f, () -> teams.unitBuildSpeedMultiplier, 0.001f, 50f);
            dialog.number("@rules.unitcostmultiplier", f -> teams.unitCostMultiplier = f, () -> teams.unitCostMultiplier);
            dialog.number("@rules.unithealthmultiplier", f -> teams.unitHealthMultiplier = f, () -> teams.unitHealthMultiplier);

            var mu = Vars.mods.getMod("mapping-utilities");
            if (mu != null && mu.enabled()
                    && Core.settings.getBool("mu_rules_mod", true)
                    && Core.settings.getBool("editor_hidden_rules", true)) {

                if (Core.bundle.has("rules.cheat")) {
                    dialog.check("@rules.cheat", b -> teams.cheat = b, () -> teams.cheat);
                }

                if (Core.bundle.has("rules.coresspawnships")) {
                    dialog.check("@rules.coresspawnships", b -> teams.aiCoreSpawn = b, () -> teams.aiCoreSpawn);
                }
            }
        } finally {
            dialog.current = previous;
        }

        if (!fields.hasChildren()) return;

        boolean[] shown = {false};
        Table teamRules = new Table();

        teamRules.button(team.coloredName(), Icon.downOpen, Styles.togglet,
                        () -> shown[0] = !shown[0])
                .marginLeft(14f).width(260f).height(55f)
                .update(button -> {
                    ((Image)button.getChildren().get(1))
                            .setDrawable(shown[0] ? Icon.upOpen : Icon.downOpen);
                    button.setChecked(shown[0]);
                })
                .left().padBottom(2f).row();

        teamRules.collapser(fields, () -> shown[0]).left().growX().row();

        parent.table(wrapper -> wrapper.add(teamRules).left().growX())
                .left().growX().row();
    }
}