package ectotech.world.gen.utils;

import arc.func.Floatf;
import arc.graphics.Color;
import mindustry.type.Weather.WeatherEntry;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

/**
 * Палитра и правила одной климатической либо геологической области.
 * Принадлежность к слою задаётся реестром генератора: addClimate/addGeology.
 * Профиль не хранит состояние конкретной точки.
 */
public class EctorumBiome {
    public final String name;

    /** Сухие полы. Основной — в середину массива, редкие — по краям. */
    public Floor[] floors = {};
    /** Жидкие полы от берега к глубине. Пусто — биом не встречается на воде. */
    public Floor[] liquidFloors = {};
    /** Стены. При инициализации дополняются стенами полов (Floor.wall) без повторов. */
    public Block[] walls = {};
    public Block[] ores = {};
    public Block[] wallOres = {};
    public Block[] decorations = {};

    /** Шаблоны погоды. В правила сектора попадают только копии. */
    public WeatherEntry[] weather = {};
    /** Доля сектора, начиная с которой включается погода биома. */
    public float weatherMinShare = 0.25f;

    /** Цвет суши на глобусе. Вода красится цветом своего жидкого пола. */
    public Color color = Color.gray.cpy();

    public boolean patch, based, derelict;
    /** Примерная доля основы, проступающей сквозь биом. */
    public float baseMix = 0.2f;
    /** Примерная доля стен, взятых из набора биома, а не сопоставленных полу. */
    public float wallMix = 0.25f;
    /** Порог стен: меньше — больше скал (горы), больше — открытая местность. */
    public float wallThreshold = 0.65f;
    /** Масштаб шума скал: больше — мельче и чаще скалы. */
    public float wallScale = 70f;
    /** Пол и угловая стена площадок. null — площадок нет. */


    /** Пол и угловая стена площадок. null — площадок нет. */
    public Floor structureFloor;
    public Block structureWall;

    public Floatf<SurfacePoint> weigher = p -> 0f;

    /** Только для patch: пятна и переход к сплошной области. */
    public float noiseScale, noiseThreshold = 0.56f, noiseFeather = 0.05f;
    public int noiseOctaves = 3;
    public float solidFrom = 0.55f, solidTo = 0.9f, minWeight = 0.12f;

    public EctorumBiome(String name) {
        this.name = name;
    }

    public boolean accepts(SurfacePoint p) {
        return p.water > 0f ? liquidFloors.length > 0 : floors.length > 0;
    }

    public float weight(SurfacePoint p) {
        return Math.max(weigher.get(p), 0f);
    }

    public boolean hasOwnNoise() {
        return noiseScale > 0f;
    }

    public int noiseSeed() {
        return name.hashCode();
    }

    public float solidity(float strength) {
        return smooth(solidFrom, solidTo, strength);
    }

    public static float smooth(float start, float end, float value) {
        if (end <= start) return value < start ? 0f : 1f;
        float t = Math.max(0f, Math.min(1f, (value - start) / (end - start)));
        return t * t * (3f - 2f * t);
    }

    @Override
    public String toString() {
        return "EctorumBiome{" + name + "}";
    }
}