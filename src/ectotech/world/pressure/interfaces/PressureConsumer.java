package ectotech.world.pressure.interfaces;

/**
 * Постройка, работа которой зависит от внутреннего давления.
 * Сам интерфейс не определяет, как именно используется pressureEfficiency:
 * множитель можно применить к скорости производства, энергопотреблению,
 * генерации, дальности или другой характеристике.
 */
public interface PressureConsumer extends Pressurized {

    /** Давление, соответствующее коэффициенту эффективности 1. */
    float operatingPressure();

    /** Граница рабочей области давления. */
    float thresholdPressure();

    /** Должна ли постройка полностью остановиться вне рабочей области. */
    boolean isPressureRequired();

    /** Минимальный коэффициент эффективности. */
    float minEfficiencyCoeff();

    /** Максимальный коэффициент эффективности. */
    float maxEfficiencyCoeff();

    /** Рассчитанный PressureModule коэффициент давления. */
    default float pressureEfficiency() {
        return pressureModule().efficiency;
    }
}