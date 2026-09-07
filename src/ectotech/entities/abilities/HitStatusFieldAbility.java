package ectotech.entities.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

/** Chose weapon bullet hits buffs nearest ally unit without this status */
public class HitStatusFieldAbility extends Ability {
    private static boolean registered = false;

    /** Chosen weapon */
    public Weapon weapon;
    public StatusEffect status = StatusEffects.overclock;
    public float range = 6f * 8f;
    /** Ability reload time, in frames */
    public float reload = 30f;
    /** Status duration, in frames */
    public float duration = 120f;
    /** If true, status will be applied to ability owner */
    public boolean includeSelf = false;

    public Effect activeEffect = Fx.dynamicWave;
    public Effect applyEffect = Fx.shieldApply;
    public Color effectColor = Pal.accent;

    /** Подходящее попадание произошло с прошлого обновления. */
    protected boolean pending = false;

    public HitStatusFieldAbility() {
        if (!registered) {
            registered = true;
            Events.on(EventType.UnitDamageEvent.class, e -> onHit(e.bullet, e.unit));
            Events.on(EventType.BuildDamageEvent.class, e -> onHit(e.source, e.build));
        }
    }

    /** Единая точка входа: любое оружие (даже без пуль) может вызвать её напрямую. */
    public static void notifyHit(Unit owner, Weapon source) {
        if (owner == null || source == null || !owner.isValid()) return;

        for (Ability a : owner.abilities) {
            if (a instanceof HitStatusFieldAbility f && f.weapon == source && f.data <= 0f) {
                f.pending = true;
            }
        }
    }

    private static void onHit(Bullet bullet, Teamc target) {
        if (Vars.net.client() || bullet == null || target == null || bullet.type == null) return;
        if (!(bullet.owner instanceof Unit owner) || !owner.isValid()) return;
        if (bullet.team != owner.team || target.team() == bullet.team) return;

        for (Ability a : owner.abilities) {
            if (a instanceof HitStatusFieldAbility f
                    && f.weapon != null
                    && f.weapon.bullet == bullet.type
                    && f.data <= 0f) {
                f.pending = true;
            }
        }
    }

    @Override
    public void init(UnitType type) {
        super.init(type);

        if (weapon == null || !type.weapons.contains(weapon, true)) {
            throw new IllegalArgumentException("HitStatusFieldAbility on '" + type.name + "' must reference one of the unit's own weapons.");
        }
        if (weapon.bullet == null) {
            throw new IllegalArgumentException("HitStatusFieldAbility on '" + type.name + "': selected weapon has no bullet.");
        }
        if (status == null || status == StatusEffects.none) {
            throw new IllegalArgumentException("HitStatusFieldAbility on '" + type.name + "' requires a status effect.");
        }
    }

    @Override
    public HitStatusFieldAbility copy() {
        HitStatusFieldAbility c = (HitStatusFieldAbility) super.copy();
        c.data = 0f;
        c.pending = false;
        return c;
    }

    @Override
    public void update(Unit unit) {
        data = Math.max(data - Time.delta, 0f);

        boolean requested = pending;
        pending = false;

        if (Vars.net.client() || !requested || data > 0f || !unit.isValid()) return;

        Unit target = Units.closest(unit.team, unit.x, unit.y, range,
                other -> (includeSelf || other != unit)
                        && other.within(unit, range)
                        && !other.hasEffect(status)
                        && !other.isImmune(status)
        );

        if (target == null) return;

        target.apply(status, duration);
        if (!target.hasEffect(status)) return;

        data = reload;

        float effectRange = Math.max(range - 4f, 0f);
        
        activeEffect.at(unit.x, unit.y, effectRange, effectColor);
        applyEffect.at(target.x, target.y, 0f, effectColor);

        if (Vars.net.server()) {
            Call.effect(activeEffect, unit.x, unit.y, effectRange, effectColor);
            Call.effect(applyEffect, target.x, target.y, 0f, effectColor);
        }
    }

    @Override
    public void addStats(Table t) {
        super.addStats(t);
        t.add(Core.bundle.format("bullet.range", Strings.autoFixed(range / Vars.tilesize, 1)));
        t.row();
        t.add(abilityStat("cooldown", Strings.autoFixed(reload / 60f, 2)));
        t.row();
        t.add(abilityStat("duration", Strings.autoFixed(duration / 60f, 2)));
        t.row();
        t.add("[stat]" + status.localizedName);
    }
}