package ectotech.world.blocks.pressure.interfaces;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Strings;
import ectotech.EctoVars;
import ectotech.world.blocks.pressure.utils.PressureModule;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.world.blocks.pressure.utils.PressurizedNetwork;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.gen.Building;
import mindustry.graphics.Pal;

import static arc.Core.bundle;

public interface Pressurized {

    Building self();

    /** доступ к модулю */
    PressureModule pressureModule();

    /** Удобный доступ к текущему давлению */
    default float pressure() {
        return pressureModule().pressure;
    }

    // Достаём параметры Block-конструктора

    /** Операционное давление (100% эффективности) */
    float operatingPressure();

    /** Пороговое давление (порог входа или точка минимума) */
    float thresholdPressure();

    /** Требуется ли давление для работы */
    boolean isPressureRequired();

    /** Минимальная эффективность */
    float minEfficiencyCoeff();

    /** Максимальная эффективность */
    float maxEfficiencyCoeff();

    /** Коэффициенты утечки */
    float outflowLinearFactor();
    float outflowExponentCoefficient();

    /** Критические давления */
    float criticalPressure();
    float superCriticalPressure();

    /** Собственное изменение давления (+производство, -потребление) */
    float pressureFlow();

    boolean explodesOnSuperCritical();


    /** Главный метод тика - вызывать из updateTile() */
    default void updatePressure() {
        pressureModule().update(this);
    }

    /** for turrets */
    default boolean tryConsumePressure(float amount) {
        if (pressure() >= amount) {
            pressureModule().applyPressureChange(-amount);
            return true;
        }
        return false;
    }

    /** Текущая эффективность от давления */
    default float pressureEfficiency() {
        return pressureModule().efficiency;
    }

    default float pressureFlowScale() {
        return self().efficiency;
    }

    /** Внешнее изменение давления (от нагнетателя/клапана) */
    default void applyExternalPressureChange(float pressureChange) {
        self().noSleep();
        pressureModule().applyPressureChange(pressureChange / (self().block.size * self().block.size));
    }

    /** Является ли ключевой стороной для подвода давления */
    default boolean isPressureInputSide(int side) { return true; }

    /** Является ли ключевой стороной для отвода давления */
    default boolean isPressureOutputSide(int side) {
        return true;
    }

    /** Вызывается при критическом давлении */
    default void onPressureCritical() {}

    /** Вызывается при сверхкритическом давлении */
    default void onPressureSuperCritical() {
        Building self = self();
        if (explodesOnSuperCritical()) {
            Damage.damage(self.x, self.y, 80f, 500f);
            Fx.explosion.at(self.x, self.y);
        }
        self.kill();
    }

    /** Если не пусто, блок является частью сети данного string-типа */
    default String pressureNetworkType() {
        return null;
    }

    /** Ссылка на сеть (null если не сетевой) */
    default PressurizedNetwork pressureNetwork() {
        return pressureModule().network;
    }

    // СЕРИАЛИЗАЦИЯ

    default void writePressure(Writes write) {
        write.f(pressureModule().pressure);
    }

    default void readPressure(Reads read, byte revision) {
        pressureModule().pressure = read.f();
    }

    //UI

    /** Текст для бара давления */
    default String pressureBarText() {
        return bundle.format("bar.ectotech-pressurepercent", Strings.fixed(pressure(), pressure() >= 1f ? 1 : 3), Strings.fixed(pressureBarFraction() * 100f, 0));
    }

    /** Заполненность бара (0..1) */
    default float pressureBarFraction() {
        return Math.min(pressure() / superCriticalPressure(), 1.0f);
    }

    /** Цвет бара */
    default Color pressureBarColor() {
        if (pressure() >= superCriticalPressure() * 0.9f) return Color.scarlet;
        if (pressure() >= criticalPressure()) return Color.orange;
        if (Math.abs(pressure() - 1.0f) < 0.2f) return Pal.darkerGray;
        return EctoVars.defaultPressureBarColor;
    }
}