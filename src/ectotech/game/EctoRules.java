package ectotech.game;

import arc.util.Log;
import arc.util.serialization.Json;
import arc.util.serialization.JsonWriter;
import mindustry.game.Rules;

import java.util.Objects;
import java.util.WeakHashMap;

public class EctoRules {
    /** Множитель урона от критического давления. */
    public float pressureCriticalDamageMultiplier = 2f;
    /** Взрываются ли блоки при сверхкритическом показателе давления */
    public boolean pressureExplosionsEnabled = true;
    /** Отключать ли туман после захвата сектора. */
    public boolean clearFogOnCapture = true;

    private static final String tag = "ectotech-rules";
    private static final Json json = new Json(JsonWriter.OutputType.json);
    private static final WeakHashMap<Rules, CacheEntry> cache = new WeakHashMap<>();

    private record CacheEntry(String stored, EctoRules data) { }

    static {
        // Записываем все значения, даже совпадающие с дефолтами.
        json.setUsePrototypes(false);

        // Формат содержит данные, а не имена Java-классов.
        json.setTypeName(null);

        // Неизвестное поле не мешает прочитать известные настройки.
        json.setIgnoreUnknownFields(true);
    }

    /**
     * Возвращает модовые правила данного Rules.
     * Пока содержимое тега не изменилось, возвращается кэшированный объект.
     * Если тега нет, используются дефолты без автоматической записи в tags.
     * Вызывать из игрового потока.
     */
    public static EctoRules of(Rules rules) {
        Objects.requireNonNull(rules, "rules");

        String stored = rules.tags.get(tag);
        CacheEntry entry = cache.get(rules);

        if (entry != null && Objects.equals(entry.stored, stored)) {
            return entry.data;
        }

        EctoRules data = read(stored);
        cache.put(rules, new CacheEntry(stored, data));
        return data;
    }

    /**
     * Записывает текущие модовые правила в Rules.tags.
     * Не сохраняет файл карты на диск и не отправляет сетевой пакет.
     * Вызывать сразу после изменения полей объекта, полученного через of().
     */
    public static void save(Rules rules) {
        save(rules, of(rules));
    }

    public static void save(Rules rules, EctoRules data) {
        Objects.requireNonNull(rules, "rules");
        validate(data);

        String stored = json.toJson(data, EctoRules.class);

        rules.tags.put(tag, stored);
        cache.put(rules, new CacheEntry(stored, data));
    }

    private static EctoRules read(String stored) {
        if (stored == null) {
            return new EctoRules();
        }

        try {
            EctoRules data = json.fromJson(EctoRules.class, stored);
            validate(data);
            return data;
        } catch (RuntimeException error) {
            Log.warn("EctoTech: invalid '@' tag; using defaults. @", tag, error.getMessage());

            return new EctoRules();
        }
    }

    private static void validate(EctoRules data) {
        if (data == null) {
            throw new IllegalArgumentException("Expected an EctoRules JSON object.");
        }

        float multiplier = data.pressureCriticalDamageMultiplier;

        if (Float.isNaN(multiplier) || Float.isInfinite(multiplier) || multiplier < 0f) {
            throw new IllegalArgumentException("pressureCriticalDamageMultiplier must be finite and >= 0.");
        }
    }
}