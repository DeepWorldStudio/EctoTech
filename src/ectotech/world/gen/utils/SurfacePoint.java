package ectotech.world.gen.utils;

/**
 * Физические параметры одной точки поверхности Ectorum. Все значения 0..1.
 * ВАЖНО: здесь нет биомов. «Пустынность», «полярность» и т.п. —
 * это интерпретации, они вычисляются весами EctorumBiome из этих чисел.
 * Экземпляр из EctorumPlanetGenerator.sample() переиспользуется (ThreadLocal):
 * для хранения между вызовами копируйте через set().
 */
public final class SurfacePoint {
    /** Высота рельефа. 0 — дно впадин, 1 — пики. */
    public float height;
    /** Изрезанность рельефа. Высокое = хребты, скалы. */
    public float roughness;
    /** Широта с климатическим возмущением: 0 — экватор, 1 — полюс. */
    public float latitude;
    /** Температура: 0 — мороз, 1 — жара. */
    public float temperature;
    /** Влажность: 0 — сушь, 1 — насыщение. */
    public float humidity;
    /** Глубина воды: 0 — суша (или линия берега), 1 — самое глубокое дно. */
    public float water;
    /** Тектоническая активность: 0 — спокойно, 1 — граница плит, вулканизм. */
    public float tectonic;
    /** Континентальность: низко — океаническая область (вода и острова), высоко — глубь материка. Сырой шум, примерно 0.25..0.75. */
    public float continent;

    public SurfacePoint set(SurfacePoint o){
        height = o.height;
        roughness = o.roughness;
        latitude = o.latitude;
        temperature = o.temperature;
        humidity = o.humidity;
        water = o.water;
        tectonic = o.tectonic;
        continent = o.continent;
        return this;
    }

    public SurfacePoint copy(){
        return new SurfacePoint().set(this);
    }

    @Override
    public String toString() {
        return String.format("h=%.2f r=%.2f lat=%.2f t=%.2f hum=%.2f w=%.2f tec=%.2f c=%.2f", height, roughness, latitude, temperature, humidity, water, tectonic, continent);
    }
}