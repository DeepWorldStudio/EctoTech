package ectotech.ui.dialogs;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import ectotech.game.EctoRules;
import mindustry.Vars;
import mindustry.editor.MapEditorDialog;
import mindustry.editor.MapInfoDialog;
import mindustry.game.Rules;
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

    private static void extend(CustomRulesDialog dialog) {
        if (dialog == null || installedDialogs.contains(dialog, true)) return;

        installedDialogs.add(dialog);
        dialog.additionalSetup.add(() -> buildEctoRules(dialog));
    }

    private static void buildEctoRules(CustomRulesDialog dialog) {
        Rules rules = Reflect.get(CustomRulesDialog.class, dialog, "rules");

        if (rules == null) return;

        EctoRules ectoRules = EctoRules.of(rules);

        dialog.category("ectotech");

        dialog.check("@rules.ectotech.pressureexplosions", value -> {
            ectoRules.pressureExplosionsEnabled = value;EctoRules.save(rules, ectoRules);
            }, () -> ectoRules.pressureExplosionsEnabled
        );

        dialog.number("@rules.ectotech.pressurecriticaldamagemultiplier", value -> {
                    ectoRules.pressureCriticalDamageMultiplier = value;
                    EctoRules.save(rules, ectoRules);
                },
                () -> ectoRules.pressureCriticalDamageMultiplier, 0f, Float.MAX_VALUE
        );

        dialog.check("@rules.ectotech.clearfogoncapture", value -> {
                    ectoRules.clearFogOnCapture = value;
                    EctoRules.save(rules, ectoRules);
                },
                () -> ectoRules.clearFogOnCapture
        );
    }
}