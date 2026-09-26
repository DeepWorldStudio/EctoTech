package ectotech.world.aspects;

import arc.Core;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Strings;
import ectotech.world.blocks.production.BalancedCrafter;
import ectotech.world.blocks.production.BalancedCrafter.BalancedCrafterBuild;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;

/**
 * Внешний фактор, снижающий запрос энергии и/или ускоряющий работу BalancedCrafter.
 * Только читает состояние постройки; расход ресурса обеспечивают ванильные механизмы.
 */
public abstract class BuildAspect {

    /** Значение, при котором эффект ещё нулевой. */
    public float start;
    /** Значение, при котором скидка и boost достигают потолка. Выше — роста нет. */
    public float full = 1f;

    /** Макс. снижение запроса энергии при полной обеспеченности. Доля 0..1 от basePowerDraw. */
    public float powerReduction;
    public Interp curve = Interp.linear;

    /** Доля пути start → full, с которой начинает расти boost. */
    public float boostStart = 0.8f;
    /** Добавка к скорости: 0.5 = +50% при полном boost. */
    public float maxBoost = 0f;
    public Interp boostCurve = Interp.linear;

    public boolean showBar = true;

    protected BuildAspect(float start, float full, float powerReduction) {
        this.start = start;
        this.full = full;
        this.powerReduction = powerReduction;
    }

    /** Ключ бандла с @ или обычная строка. */
    public abstract String name();
    public abstract StatUnit unit();
    public abstract Color color();

    /** Текущее значение ресурса на постройке. */
    public abstract float value(BalancedCrafterBuild build);

    /** Вызывается каждый тик до чтения value(); здесь аспект обновляет своё состояние на постройке. */
    public void updateOwner(BalancedCrafterBuild build){}

    /** Вызывается один раз в init() блока; аспект настраивает блок под себя. */
    public void attachTo(BalancedCrafter block){}

    public Bar createBar(BalancedCrafterBuild build){
        return new Bar(
                () -> localized() + ": " + Strings.autoFixed(value(build), 1) + " / " + Strings.autoFixed(full, 1),
                this::color,
                () -> fraction(build)
        );
    }

    public String localized() {
        String n = name();
        return n.charAt(0) == '@' ? Core.bundle.get(n.substring(1)) : n;
    }

    public void validate(String owner) {
        String type = getClass().getSimpleName();
        if (curve == null || boostCurve == null) throw new IllegalArgumentException(owner + ": " + type + " missing curve");
        if (start == full) throw new IllegalArgumentException(owner + ": " + type + " start == full");
        if (powerReduction < 0f || powerReduction > 1f || boostStart < 0f || boostStart > 1f || maxBoost < 0f){
            throw new IllegalArgumentException(owner + ": " + type + " out of range");
        }
    }

    public float fraction(BalancedCrafterBuild build) {
        return Mathf.clamp((value(build) - start) / (full - start));
    }

    public float saving(float fraction){
        return powerReduction * Mathf.clamp(curve.apply(fraction));
    }

    public float boost(float fraction){
        if(maxBoost <= 0f) return 0f;
        float t = boostStart >= 1f ? (fraction >= 1f ? 1f : 0f) : Mathf.clamp((fraction - boostStart) / (1f - boostStart));
        return maxBoost * Mathf.clamp(boostCurve.apply(t));
    }

    public float boostThresholdValue() {
        return Mathf.lerp(start, full, boostStart);
    }
}