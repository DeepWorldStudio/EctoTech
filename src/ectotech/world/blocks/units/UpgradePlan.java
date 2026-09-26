package ectotech.world.blocks.units;

import arc.util.*;
import mindustry.game.*;
import mindustry.type.*;

/** Один вариант апгрейда: вход -> выход, со своей стоимостью. Порядок аргументов: вход, выход, стоимость, время, энергия. */
public class UpgradePlan{
    public final UnitType input, output;

    /** null — использовать defaultRequirements блока. */
    public @Nullable ItemStack[] requirements;
    /** Время в тиках; < 0 — использовать constructTime блока. */
    public float time = -1f;
    /** Энергия/тик; < 0 — использовать defaultPowerConsumption блока. */
    public float power = -1f;

    public UpgradePlan(UnitType input, UnitType output){
        this.input = input;
        this.output = output;
    }

    public UpgradePlan(UnitType input, UnitType output, @Nullable ItemStack[] requirements){
        this(input, output);
        this.requirements = requirements;
    }

    public UpgradePlan(UnitType input, UnitType output, float time) {
        this(input, output);
        this.time = time;
    }

    public UpgradePlan(UnitType input, UnitType output, float time, @Nullable ItemStack[] requirements){
        this(input, output, requirements);
        this.time = time;
    }

    public UpgradePlan(UnitType input, UnitType output, float time, @Nullable ItemStack[] requirements, float power){
        this(input, output, time, requirements);
        this.power = power;
    }

    //fluent-сеттеры

    public UpgradePlan requirements(ItemStack... requirements){
        this.requirements = requirements;
        return this;
    }

    public UpgradePlan time(float time){
        this.time = time;
        return this;
    }

    public UpgradePlan power(float power){
        this.power = power;
        return this;
    }

    //резолверы с дефолтами блока

    public ItemStack[] resolveRequirements(ItemStack[] def){
        return requirements != null ? requirements : def;
    }

    public float resolveTime(float def){
        return Math.max(time >= 0f ? time : def, 1f);
    }

    public float resolvePower(float def){
        return power >= 0f ? power : def;
    }

    /** Может ли команда реально получить этот выход. */
    public boolean valid(Team team){
        return (output.unlockedNowHost() || team.isAI()) && !output.isBanned();
    }

    /** Показывать ли в статах блока. */
    public boolean shown(){
        return input.unlockedNow() && output.unlockedNow() && !output.isHidden();
    }

    @Override
    public String toString(){
        return "UpgradePlan{" + input.name + " -> " + output.name + "}";
    }
}