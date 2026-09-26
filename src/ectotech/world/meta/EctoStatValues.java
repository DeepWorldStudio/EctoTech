package ectotech.world.meta;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import ectotech.world.aspects.BuildAspect;
import ectotech.world.blocks.environment.SteamGeyser;
import ectotech.world.blocks.units.UpgradePlan;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.maps.Map;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.iconSmall;
import static mindustry.Vars.tilesize;

/**Вспомогательный класс для отображения кастомных статов*/
public class EctoStatValues extends StatValues {


    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map) {
        return ammo(map, 0, false);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean showUnit) {
        return ammo(map, 0, showUnit);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent, boolean showUnit) {
        return table -> {

            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for (T t : orderedKeys) {
                boolean compact = t instanceof UnitType && !showUnit || indent > 0;

                BulletType type = map.get(t);

                if (type.spawnUnit != null && type.spawnUnit.weapons.size > 0) {
                    ammo(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), indent, false).display(table);
                    continue;
                }

                table.table(Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();
                    //no point in displaying unit icon twice
                    if (!compact && !(t instanceof Turret)) {
                        bt.table(title -> {
                            title.image(icon(t)).size(3 * 8).padRight(4).right().scaling(Scaling.fit).top();
                            title.add(t.localizedName).padRight(10).left().top();
                        });
                        bt.row();
                    }
                    if(type.damage > 0){
                        sep(bt, Core.bundle.format("bullet.damage", type.damage));
                    }

                    if (type.buildingDamageMultiplier != 1) {
                        int val = (int) (type.buildingDamageMultiplier * 100 - 100);
                        sep(bt, Core.bundle.format("bullet.buildingdamage", ammoStat(val)));
                    }

                    if (type.rangeChange != 0 && !compact) {
                        sep(bt, Core.bundle.format("bullet.range", ammoStat(type.rangeChange / tilesize)));
                    }

                    if (type.splashDamage > 0) {
                        sep(bt, Core.bundle.format("bullet.splashdamage", type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
                    }

                    if (!compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier && (!(t instanceof Turret turret) || turret.displayAmmoMultiplier)) {
                        sep(bt, Core.bundle.format("bullet.multiplier", (int) type.ammoMultiplier));
                    }

                    if (!compact && !Mathf.equal(type.reloadMultiplier, 1f)) {
                        int val = (int) (type.reloadMultiplier * 100 - 100);
                        sep(bt, Core.bundle.format("bullet.reload", ammoStat(val)));
                    }

                    if (type.knockback > 0) {
                        sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
                    }

                    if (type.healPercent > 0f) {
                        sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
                    }

                    if (type.healAmount > 0f) {
                        sep(bt, Core.bundle.format("bullet.healamount", Strings.autoFixed(type.healAmount, 2)));
                    }

                    if (type.pierce || type.pierceCap != -1) {
                        sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
                    }

                    if (type.incendAmount > 0) {
                        sep(bt, "@bullet.incendiary");
                    }

                    if (type.homingPower > 0.01f) {
                        sep(bt, "@bullet.homing");
                    }

                    if (type.lightning > 0) {
                        sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
                    }

                    if (type.pierceArmor) {
                        sep(bt, "@bullet.armorpierce");
                    }

                    if (type.collidesAir) {
                        sep(bt, Core.bundle.get("bullet.reachAir"));
                    }

                    if (type.collidesGround) {
                        sep(bt, Core.bundle.get("bullet.reachGround"));
                    }

                    if (type.suppressionRange > 0) {
                        sep(bt, Core.bundle.format("bullet.suppression", Strings.autoFixed(type.suppressionDuration / 60f, 2), Strings.fixed(type.suppressionRange / tilesize, 1)));
                    }

                    if (type.status != StatusEffects.none) {
                        sep(bt, (type.status.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" + ((int) (type.statusDuration / 60f)) + "[lightgray] " + Core.bundle.get("unit.seconds")));
                    }

                    if (type.intervalBullet != null) {
                        bt.row();

                        Table ic = new Table();
                        ammo(ObjectMap.of(t, type.intervalBullet), indent + 1, false).display(ic);
                        Collapser coll = new Collapser(ic, true);
                        coll.setDuration(0.1f);

                        bt.table(it -> {
                            it.left().defaults().left();

                            it.add(Core.bundle.format("bullet.interval", Strings.autoFixed(type.intervalBullets / type.bulletInterval * 60, 2)));
                            it.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type.intervalBullet != null){
                        bt.row();

                        Table ic = new Table();
                        ammo(ObjectMap.of(t, type.intervalBullet), true, false).display(ic);
                        Collapser coll = new Collapser(ic, true);
                        coll.setDuration(0.1f);

                        bt.table(it -> {
                            it.left().defaults().left();

                            it.add(Core.bundle.format("bullet.interval", Strings.autoFixed(type.intervalBullets / type.bulletInterval * 60, 2)));
                            it.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type.fragBullet != null){
                        bt.row();

                        Table fc = new Table();
                        ammo(ObjectMap.of(t, type.fragBullet), true, false).display(fc);
                        Collapser coll = new Collapser(fc, true);
                        coll.setDuration(0.1f);

                        bt.table(ft -> {
                            ft.left().defaults().left();

                            ft.add(Core.bundle.format("bullet.frags", type.fragBullets));
                            ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                    if(type.spawnBullets != null && type.spawnBullets.size > 0){
                        bt.row();

                        Table sc = new Table();
                        for(BulletType spawn : type.spawnBullets){
                            if(spawn.showStats) ammo(ObjectMap.of(t, spawn), true, false).display(sc);
                        }
                        Collapser coll = new Collapser(sc, true);
                        coll.setDuration(0.1f);

                        bt.table(st -> {
                            st.left().defaults().left();

                            st.add(Core.bundle.format("bullet.spawnBullets", type.spawnBullets.size));
                            if(sc.getChildren().size > 0) st.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                        });
                        bt.row();
                        bt.add(coll);
                    }

                }).padLeft(indent * 5).padTop(5).padBottom(compact ? 0 : 5).growX().margin(compact ? 0 : 10);
                table.row();
            }
        };
    }

    private static void sep(Table table, String text) {
        table.row();
        table.add(text);
    }

    //for AmmoListValue
    private static String ammoStat(float val) {
        return (val > 0 ? "[stat]+" : "[negstat]") + Strings.autoFixed(val, 1);
    }

    private static TextureRegion icon(UnlockableContent t) {
        return t.uiIcon;
    }

    public static StatValue geysers() {
        return table -> table.table(c -> {
            Runnable[] rebuild = {null};
            Map[] lastMap = {null};

            rebuild[0] = () -> {
                c.clearChildren();
                c.left();

                if (!Vars.state.isGame()) {
                    c.add("@stat.showinmap");
                    return;
                }

                var geysers = Vars.content.blocks()
                        .select(block -> block instanceof SteamGeyser && Vars.indexer.isBlockPresent(block))
                        .sort(block -> ((SteamGeyser)block).activeEfficiency);

                if (geysers.isEmpty()) {
                    c.add("@none.inmap");
                    return;
                }

                int i = 0;
                for (var block : geysers) {
                    SteamGeyser geyser = (SteamGeyser)block;

                    c.table(Styles.grayPanel, entry -> {
                        entry.margin(4f);
                        entry.left();

                        entry.image(block.uiIcon).size(40f).pad(5f).left();

                        entry.table(info -> {
                            info.left();
                            info.add(block.localizedName).left().row();
                            info.add(Core.bundle.format("stat.ectotech-geyser-efficiency", percent(geyser.passiveEfficiency), percent(geyser.activeEfficiency))).color(Color.lightGray).left();
                        }).left().pad(5f);

                    }).growX().pad(5f);

                    // По 2 элемента в строку
                    if (++i % 2 == 0) {
                        c.row();
                    }
                }
            };

            rebuild[0].run();

            c.update(() -> {
                Map current = Vars.state.isGame() ? Vars.state.map : null;

                if (current != lastMap[0]) {
                    rebuild[0].run();
                    lastMap[0] = current;
                }
            });
        });
    }

    private static String percent(float value) {
        return Strings.autoFixed(value * 100f, 1) + "%";
    }

    public static StatValue aspects(Seq<BuildAspect> aspects){
        return table -> {
            for(BuildAspect a : aspects){
                table.row();
                table.table(info -> {
                    info.left().defaults().left().pad(3f);
                    info.add(a.localized()).color(a.color()).colspan(2).row();

                    info.add("@stat.input");
                    info.table(v -> {
                        StatValues.number(a.start, a.unit()).display(v);
                        v.add(" → ");
                        StatValues.number(a.full, a.unit()).display(v);
                    }).row();

                    info.add("@stat.poweruse");
                    info.add("-" + Strings.autoFixed(a.powerReduction * 100f, 1) + "%").row();

                    if(a.maxBoost > 0f){
                        info.add(Core.bundle.format("bar.boost", Strings.autoFixed(a.maxBoost * 100f, 1))).colspan(2).row();
                        info.add("@stat.ectotech-booststart");
                        info.table(v -> StatValues.number(a.boostThresholdValue(), a.unit()).display(v)).row();
                    }
                }).left().padBottom(6f);
            }
        };
    }

    public static StatValue upgradePlans(Seq<UpgradePlan> plans, float defTime, ItemStack[] defReqs, float defPower){
        return table -> {
            table.row();

            for (UpgradePlan plan : plans) {
                float time = plan.resolveTime(defTime);
                ItemStack[] reqs = plan.resolveRequirements(defReqs);
                float power = plan.resolvePower(defPower);

                table.table(Styles.grayPanel, t -> {

                    if(plan.input.isBanned() || plan.output.isBanned()){
                        t.image(Icon.cancel).color(Pal.remove).size(40);
                        return;
                    }

                    if(!plan.input.unlockedNow() || !plan.output.unlockedNow()){
                        t.image(Icon.lock).color(Pal.darkerGray).size(40);
                        return;
                    }

                    t.image(plan.input.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit)
                            .with(i -> StatValues.withTooltip(i, plan.input));

                    t.image(Icon.right).color(Pal.darkishGray).size(24f).pad(6f);

                    t.image(plan.output.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit)
                            .with(i -> StatValues.withTooltip(i, plan.output));

                    t.table(info -> {
                        info.left().defaults().left();

                        info.add(plan.output.localizedName);
                        info.row();
                        info.add(Strings.autoFixed(time / 60f, 1) + " " + Core.bundle.get("unit.seconds"))
                                .color(Color.lightGray);

                        if (power > 0f) {
                            info.row();
                            info.table(p -> {
                                p.left();
                                p.image(Icon.power).size(iconSmall).color(Pal.powerBar).padRight(3f);
                                p.add(Strings.autoFixed(power * 60f, 1) + " " + StatUnit.powerSecond.localized())
                                        .color(Color.lightGray);
                            });
                        }
                    }).left().pad(6f);

                    t.table(req -> {
                        req.right();
                        for(int i = 0; i < reqs.length; i++){
                            if(i % 6 == 0) req.row();

                            ItemStack stack = reqs[i];
                            req.add(StatValues.displayItem(stack.item, stack.amount, time, true)).pad(5);
                        }
                    }).right().grow().pad(10f);

                }).growX().pad(5);

                table.row();
            }
        };
    }
}

