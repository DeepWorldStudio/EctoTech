package ectotech.world.gen;

import arc.Core;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.noise.Simplex;
import ectotech.content.EctoBlocks;
import ectotech.content.EctoItems;
import ectotech.content.EctoLoadouts;
import ectotech.world.gen.utils.EctorumBiome;
import ectotech.world.gen.utils.SurfacePoint;
import mindustry.ai.Astar;
import mindustry.ai.BaseRegistry.BasePart;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.content.Weathers;
import mindustry.game.*;
import mindustry.gen.Iconc;
import mindustry.maps.Map;
import mindustry.maps.generators.BaseGenerator;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Item;
import mindustry.type.Sector;
import mindustry.type.Weather.WeatherEntry;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.environment.Floor;

import java.util.Arrays;

import static arc.math.Mathf.clamp;
import static mindustry.Vars.*;

public class EctorumPlanetGenerator extends PlanetGenerator {

    public static final int surfaceSeed = 0xEC70;
    public static final int genVersion = 1;
    public static final float waterLevel = 0.26f, coastLevel = 0.32f;

    // Параметры физики поверхности.
    public float continentScale = 0.85f, reliefScale = 2.4f, ridgeScale = 2.6f, tectonicScale = 0.9f, humidityScale = 1.3f, climateWarpScale = 1.5f;
    public float ridgeHeight = 0.45f, heightLow = 0.28f, heightHigh = 0.83f, climateWarp = 0.12f, lapseRate = 0.22f, tectonicSharpness = 3f, noiseSpread = 2.6f;

    // Палитры и границы.
    public float biomeBorderWidth = 0.12f, ditherScaleGlobe = 6f, ditherScaleTile = 240f, paletteScale = 155f, patchColorBlend = 0.65f;
    /** Размер сетки точек для автоматической погоды. */
    public int weatherSamples = 16;
    /** Высота меша планеты. Сетка секторов жёстко на 1.17 радиуса, облака ниже неё, поэтому
     *  диапазон держим как у Серпуло: примерно 0.15…0.45. */
    public float minMeshHeight = 0.15f, maxMeshHeight = 0.4f;
    /** Кривая рельефа: больше единицы — равнины ровнее, пики острее и реже. У Серпуло это pow(..., 2.3f). */
    public float terrainCurve = 2.3f;
    /** Затенение суши по высоте: низины темнее, вершины светлее. Делает климатические зоны читаемее. */
    public float shadeLow = 0.85f, shadeHigh = 1f;
    /** Альбедо суши, уходит в альфу цвета — как block.albedo в ванили. */
    public float landAlbedo = 0.1f;

    // Сектор.
    /** Проходы сглаживания стен. 0 — без сглаживания. */
    public int wallSmoothing = 2;
    /** Поляны для строительства, связанные с точкой высадки. 0 — открытость задают только биомы. */
    public int buildClearings = 2;
    public float navalWaterShare = 0.22f, decorationChance = 0.012f, structureChance = 0.0012f, ruinChance = 0.00045f;
    /** Патроны турелей вражеской базы. */
    public Item[] enemyTurretAmmo = {};

    /** none, height, continent, temperature, humidity, tectonic, roughness, biome, patch. После смены — пересобрать меш планеты. */
    public String debugView = "none";

    protected final Seq<EctorumBiome> biomes = new Seq<>();
    /** Основа. Находится в validateBiomes(). */
    protected EctorumBiome baseBiome;
    /** Руды, которые гарантируются у точки высадки. */
    protected Block[] starterOres = {};
    /** Руда → блоки, на которых она допустима (пол для обычной, стена для настенной). Нет записи — без ограничений. */
    protected final ObjectMap<Block, ObjectSet<Block>> oreHosts = new ObjectMap<>();
    /** Таблица «пол → своя стена». Заполняется из Floor.wall, уточняется matchWall(). */
    protected final ObjectMap<Floor, Block> wallMatch = new ObjectMap<>();

    private volatile boolean biomesReady;
    private final ThreadLocal<SurfacePoint> surfacePoints = ThreadLocal.withInitial(SurfacePoint::new);
    /** Биомы тайлов текущего сектора. Существуют только во время generate(). */
    private EctorumBiome[] tileBiomes, tilePatches;
    private boolean campaignDone, campaignChecked;

    /** Отладка: любой сектор доступен для высадки. Не для релизных сборок. */
    public boolean debugUnlockAll = false;

    public EctorumPlanetGenerator() {
        baseSeed = surfaceSeed ^ (genVersion * 0x6D2B79F5);
        defaultLoadout = EctoLoadouts.basicSpark;
    }


    /** Ленивая инициализация: к первому использованию контент уже загружен. */
    protected void ensureBiomes() {
        if (biomesReady) return;
        synchronized (this) {
            if (biomesReady) return;
            if (EctoBlocks.charoit == null) throw new IllegalStateException("[Ectorum] Генератор используется до EctoBlocks.load().");
            biomes.clear();
            oreHosts.clear();
            wallMatch.clear();
            initBiomes();
            validateBiomes();
            buildWallTable();
            biomesReady = true;
        }
    }

    protected EctorumBiome add(EctorumBiome biome) {
        biomes.add(biome);
        return biome;
    }

    /** Руда появляется только на перечисленных блоках. */
    protected void restrictOre(Block ore, Block... hosts) {
        ObjectSet<Block> set = oreHosts.get(ore);
        if (set == null) oreHosts.put(ore, set = new ObjectSet<>());
        set.addAll(hosts);
    }

    protected boolean oreAllowed(Block ore, Block host) {
        ObjectSet<Block> hosts = oreHosts.get(ore);
        return hosts == null || hosts.contains(host);
    }

    /** Своя стена пола. Переопределяет Floor.wall только для генерации. */
    protected void matchWall(Block floorBlock, Block wall) {
        wallMatch.put(floor(floorBlock), wall);
    }

    protected Block wallOf(Floor floor) {
        return wallMatch.get(floor, floor.wall);
    }

    private static Floor floor(Block block) {
        if (block == null) throw new IllegalStateException("[Ectorum] В палитре есть незагруженный блок.");
        return block.asFloor();
    }

    private static Floor[] floorsOf(Block... blocks) {
        Floor[] out = new Floor[blocks.length];
        for (int i = 0; i < blocks.length; i++) out[i] = floor(blocks[i]);
        return out;
    }

    /**
     * Реестр биомов. Удалить можно любой, кроме based.
     * Численные веса доводятся по calibrate() и отладочному глобусу.
     */
    protected void initBiomes() {
        starterOres = new Block[]{EctoBlocks.oreBismuth, EctoBlocks.oreZinc};
        enemyTurretAmmo = new Item[]{EctoItems.bismuth, Items.graphite};
        restrictOre(Blocks.wallOreGraphite, EctoBlocks.eboniteWall);

        matchWall(EctoBlocks.sulfurFloor, EctoBlocks.sulfurWall);
        matchWall(EctoBlocks.sulfurCrater, EctoBlocks.sulfurWall);
        matchWall(EctoBlocks.smoothSulfurFloor, EctoBlocks.pyritedSulfurWall);

        add(new EctorumBiome("common") {{
            based = true;
            floors = floorsOf(EctoBlocks.charoit, EctoBlocks.smoothCharoit, EctoBlocks.ebonite, EctoBlocks.smoothEbonite, EctoBlocks.clivelite, EctoBlocks.smoothClivelite, EctoBlocks.crushedClivelite);
            liquidFloors = floorsOf(EctoBlocks.boricWater, EctoBlocks.deepBoricWater);
            ores = new Block[]{EctoBlocks.oreBismuth, EctoBlocks.oreZinc};
            wallOres = new Block[]{EctoBlocks.wallOreBismuth, EctoBlocks.wallOreZinc, Blocks.wallOreGraphite};
            color = Color.valueOf("7a7288");
        }});

        add(new EctorumBiome("polar") {{
            floors = floorsOf(EctoBlocks.charoit, EctoBlocks.boricIce, EctoBlocks.smoothCharoit);
            walls = new Block[]{EctoBlocks.boricIceWall};
            ores = new Block[]{EctoBlocks.oreEctorumThorium};
            wallOres = new Block[]{EctoBlocks.thoriumCrystalWall};
            weather = new WeatherEntry[]{new WeatherEntry(Weathers.snow)};
            color = Color.valueOf("9fb3c8");
            baseMix = 0.15f;
            weigher = p -> 1.35f * smooth(0.6f, 0.9f, p.latitude) * smooth(0.35f, 0.75f, 1f - p.temperature);
        }});

        add(new EctorumBiome("desert") {{
            floors = floorsOf(EctoBlocks.clivelite, EctoBlocks.ectorumSandstone, EctoBlocks.ectorumSand, EctoBlocks.ectorumQuicksand);
            ores = new Block[]{EctoBlocks.oreLithium};
            wallOres = new Block[]{EctoBlocks.wallOreLithium};
            weather = new WeatherEntry[]{new WeatherEntry(Weathers.sandstorm)};
            color = Color.valueOf("b09a6e");
            baseMix = 0.1f;
            wallThreshold = 0.7f;
            weigher = p -> 1.55f * smooth(0.36f, 0.69f, p.temperature) * smooth(0.3f, 0.68f, 1f - p.humidity);
        }});

        add(new EctorumBiome("moss") {{
            floors = floorsOf(EctoBlocks.crushedClivelite, EctoBlocks.bluishMoss, EctoBlocks.smoothClivelite);
            walls = new Block[]{EctoBlocks.mossyCliveliteWall};
            decorations = new Block[]{EctoBlocks.bluishMossShoots, EctoBlocks.bluishMossBush, EctoBlocks.bluishMossTree};
            weather = new WeatherEntry[]{new WeatherEntry(Weathers.rain), new WeatherEntry(Weathers.fog)};
            color = Color.valueOf("5f8a72");
            weigher = p -> 1.25f * smooth(0.57f, 0.87f, p.humidity) * smooth(0.22f, 0.58f, p.temperature);
        }});

        add(new EctorumBiome("mountains") {{
            floors = floorsOf(EctoBlocks.ebonite, EctoBlocks.kyanicStone, EctoBlocks.kyanicStoneNugget);
            wallOres = new Block[]{EctoBlocks.kyaniteWall};
            color = Color.valueOf("6a6472");
            baseMix = 0.4f;
            wallThreshold = 0.52f;
            wallScale = 55f;
            weigher = p -> 1.3f * smooth(0.5f, 0.82f, p.roughness) * smooth(0.6f, 0.88f, p.height);
        }});

        add(new EctorumBiome("plains") {{
            floors = floorsOf(EctoBlocks.smoothEbonite, EctoBlocks.ebonite, EctoBlocks.clivelite);
            derelict = true;
            color = Color.valueOf("7f7688");
            baseMix = 0.35f;
            weigher = p -> 0.2f;
        }});

        add(new EctorumBiome("coast") {{
            floors = floorsOf(EctoBlocks.ectorumSandstone, EctoBlocks.ectorumSand);
            liquidFloors = floorsOf(EctoBlocks.boricSandWater);
            color = Color.valueOf("ad9b76");
            baseMix = 0f;
            wallThreshold = 0.72f;
            weigher = p -> 1.4f * (p.water > 0f ? 1f - smooth(0f, 0.15f, p.water) : 1f - smooth(waterLevel, coastLevel, p.height));
        }});

        add(new EctorumBiome("ocean") {{
            floors = floorsOf(EctoBlocks.smoothCharoit, EctoBlocks.ectorumSand);
            liquidFloors = floorsOf(EctoBlocks.boricWater, EctoBlocks.deepBoricWater);
            weather = new WeatherEntry[]{new WeatherEntry(Weathers.fog)};
            weatherMinShare = 0.45f;
            color = Color.valueOf("8a8177");
            wallThreshold = 0.7f;
            weigher = p -> 1.2f * (1f - smooth(0.38f, 0.47f, p.continent));
        }});

        add(new EctorumBiome("marble") {{
            patch = true;
            floors = floorsOf(EctoBlocks.crackedMarble, EctoBlocks.smoothMarble);
            ores = new Block[]{};
            structureFloor = floor(EctoBlocks.polishedMarble);
            structureWall = EctoBlocks.polishedMarbleWall;
            derelict = true;
            color = Color.valueOf("b5aeb2");
            baseMix = 0.1f;
            noiseScale = 4.5f;
            noiseThreshold = 0.55f;
            solidFrom = 0.51f;
            solidTo = 0.87f;
            minWeight = 0.2f;
            weigher = p -> Mathf.clamp(0.05f + 0.43f * p.roughness + 0.43f * smooth(0.4f, 0.79f, p.height) - 0.32f * p.tectonic);
        }});

        add(new EctorumBiome("sulfur") {{
            patch = true;
            floors = floorsOf(EctoBlocks.sulfurCrater, EctoBlocks.sulfurFloor, EctoBlocks.smoothSulfurFloor);
            liquidFloors = floorsOf(EctoBlocks.smoothSulfurFloorWater, EctoBlocks.sulfurSolution);
            decorations = new Block[]{EctoBlocks.sulfurLayering, EctoBlocks.largeSulfurLayering, EctoBlocks.pyriteCluster};
            derelict = true;
            color = Color.valueOf("b7ad65");
            baseMix = 0.08f;
            wallMix = 0.4f;
            noiseScale = 4f;
            noiseThreshold = 0.58f;
            solidFrom = 0.5f;
            solidTo = 0.86f;
            weigher = p -> Mathf.clamp(p.tectonic * 1.3f + p.roughness * 0.16f - 0.14f);
        }});
    }


    protected void validateBiomes() {
        baseBiome = null;
        ObjectSet<String> names = new ObjectSet<>();
        for (int i = 0; i < biomes.size; i++) {
            EctorumBiome b = biomes.get(i);
            if (!names.add(b.name)) throw new IllegalStateException("[Ectorum] Повтор имени биома: " + b.name);
            if (b.floors.length == 0 && b.liquidFloors.length == 0) throw new IllegalStateException("[Ectorum] У биома " + b.name + " нет полов.");
            if (b.patch && b.based) throw new IllegalStateException("[Ectorum] Биом " + b.name + " не может быть одновременно patch и based.");
            checkBlocks(b, b.floors, "floors");
            checkBlocks(b, b.liquidFloors, "liquidFloors");
            checkBlocks(b, b.walls, "walls");
            checkBlocks(b, b.ores, "ores");
            checkBlocks(b, b.wallOres, "wallOres");
            checkBlocks(b, b.decorations, "decorations");
            for (WeatherEntry entry : b.weather) {
                if (entry == null || entry.weather == null) throw new IllegalStateException("[Ectorum] Пустая погода в биоме " + b.name);
            }
            if (b.based) {
                if (baseBiome != null) throw new IllegalStateException("[Ectorum] Два based-биома: " + baseBiome.name + " и " + b.name);
                baseBiome = b;
            }
        }
        if (baseBiome == null || baseBiome.floors.length == 0 || baseBiome.liquidFloors.length == 0) {
            throw new IllegalStateException("[Ectorum] Нужен based-биом с floors и liquidFloors: он занимает точки без биома.");
        }
    }

    private static void checkBlocks(EctorumBiome b, Block[] blocks, String field) {
        for (Block block : blocks) {
            if (block == null) throw new IllegalStateException("[Ectorum] " + b.name + "." + field + " содержит незагруженный блок (null).");
        }
    }

    /** Дополняет таблицу стенами полов из EctoBlocks и вливает стены полов в набор каждого биома без повторов. */
    protected void buildWallTable() {
        for (int i = 0; i < biomes.size; i++) {
            for (Floor f : biomes.get(i).floors) {
                if (!wallMatch.containsKey(f)) wallMatch.put(f, f.wall);
            }
        }
        for (int i = 0; i < biomes.size; i++) {
            EctorumBiome b = biomes.get(i);
            Seq<Block> merged = new Seq<>();
            for (Block wall : b.walls) addUnique(merged, wall);
            for (Floor f : b.floors) addUnique(merged, wallOf(f));
            b.walls = merged.toArray(Block.class);
        }
    }

    private static void addUnique(Seq<Block> out, Block block) {
        if (block != null && block != Blocks.air && !out.contains(block, true)) out.add(block);
    }

    // Физика.

    protected static int surfaceNoiseSeed(int offset) {
        return surfaceSeed + (genVersion - 1) * 0x9E3779B9 + offset;
    }

    protected static float inverseLength(Vec3 p) {
        float length2 = p.len2();
        if (!Float.isFinite(length2) || length2 <= 1e-10f) throw new IllegalArgumentException("[Ectorum] Ожидалась конечная ненулевая точка поверхности.");
        return 1f / (float)Math.sqrt(length2);
    }

    /** Шум поверхности в точке, приведённой на единичную сферу. */
    protected static float surfaceNoise(int offset, int octaves, float scale, Vec3 p) {
        float inv = inverseLength(p);
        return Simplex.noise3d(surfaceNoiseSeed(offset), octaves, 0.5f, scale, p.x * inv, p.y * inv, p.z * inv);
    }

    /** Растягивает узкий многооктавный шум ближе к 0..1. */
    protected float spread(float value) {
        return clamp((value - 0.5f) * noiseSpread + 0.5f);
    }

    /** Физика точки. Возвращает переиспользуемый объект потока. */
    protected SurfacePoint sample(Vec3 position) {
        SurfacePoint p = surfacePoints.get();
        float inv = inverseLength(position);
        float x = position.x * inv, y = position.y * inv, z = position.z * inv;

        float continent = Simplex.noise3d(surfaceNoiseSeed(0), 3, 0.5f, continentScale, x, y, z);
        float detail = Simplex.noise3d(surfaceNoiseSeed(1), 6, 0.5f, reliefScale, x, y, z);
        float ridge = ridged(surfaceNoiseSeed(2), 4, ridgeScale, x, y, z);
        float land = EctorumBiome.smooth(0.45f, 0.6f, continent);
        float raw = continent * 0.65f + detail * 0.35f + ridge * ridgeHeight * land;

        p.continent = continent;
        p.height = clamp((raw - heightLow) / (heightHigh - heightLow));
        p.water = p.height < waterLevel ? clamp((waterLevel - p.height) / waterLevel) : 0f;

        float plates = ridged(surfaceNoiseSeed(3), 2, tectonicScale, x, y, z);
        p.tectonic = clamp((float)Math.pow(plates, tectonicSharpness));
        p.roughness = clamp(ridge * (0.5f + 0.5f * land) + p.tectonic * 0.25f);

        float warp = (Simplex.noise3d(surfaceNoiseSeed(4), 3, 0.5f, climateWarpScale, x, y, z) - 0.5f) * 2f * climateWarp;
        p.latitude = clamp(Math.abs(y) + warp);

        float altitude = Math.max(p.height - waterLevel, 0f) / (1f - waterLevel);
        float tempNoise = (Simplex.noise3d(surfaceNoiseSeed(5), 2, 0.5f, 2f, x, y, z) - 0.5f) * 0.12f;
        p.temperature = clamp(1f - p.latitude - altitude * lapseRate + p.tectonic * 0.1f + tempNoise);

        float humidityNoise = spread(Simplex.noise3d(surfaceNoiseSeed(6), 4, 0.5f, humidityScale, x, y, z));
        float lowland = 1f - EctorumBiome.smooth(0.4f, 0.65f, continent);
        p.humidity = clamp(Mathf.lerp(humidityNoise, lowland, 0.4f));
        return p;
    }

    /** Шум хребтов: 1 на гребне, около 0 вдали. */
    protected static float ridged(int seed, int octaves, float scale, float x, float y, float z) {
        float sum = 0f, amplitude = 1f, normalization = 0f, frequency = scale;
        for (int i = 0; i < octaves; i++) {
            float n = Simplex.noise3d(seed + i * 31, 1, 0.5f, frequency, x, y, z);
            float ridge = 1f - Math.abs(n * 2f - 1f);
            sum += ridge * ridge * amplitude;
            normalization += amplitude;
            amplitude *= 0.5f;
            frequency *= 2f;
        }
        return sum / normalization;
    }

    // Выбор биома.

    /** Обычный биом точки. Никогда не null: без претендентов — основа. */
    protected EctorumBiome biomeAt(Vec3 point, SurfacePoint p, float ditherScale) {
        ensureBiomes();
        EctorumBiome first = null, second = null;
        float w1 = 0f, w2 = 0f;
        for (int i = 0; i < biomes.size; i++) {
            EctorumBiome b = biomes.get(i);
            if (b.patch || b.based || !b.accepts(p)) continue;
            float w = b.weight(p);
            if (w > w1) {
                second = first; w2 = w1; first = b; w1 = w;
            } else if (w > w2) {
                second = b; w2 = w;
            }
        }
        return first == null ? baseBiome : dither(first, second, w1, w2, point, ditherScale, 90);
    }

    /** Пятно точки или null. */
    protected EctorumBiome patchAt(Vec3 point, SurfacePoint p, float ditherScale) {
        ensureBiomes();
        EctorumBiome first = null, second = null;
        float w1 = 0f, w2 = 0f;
        for (int i = 0; i < biomes.size; i++) {
            EctorumBiome b = biomes.get(i);
            if (!b.patch || !b.accepts(p)) continue;
            float w = patchWeight(b, point, p);
            if (w < b.minWeight || w <= 0f) continue;
            if (w > w1) {
                second = first; w2 = w1; first = b; w1 = w;
            } else if (w > w2) {
                second = b; w2 = w;
            }
        }
        return first == null ? null : dither(first, second, w1, w2, point, ditherScale, 91);
    }

    /** Вес пятна: сила × lerp(маска пятен, 1, сплошность). */
    protected float patchWeight(EctorumBiome b, Vec3 point, SurfacePoint p) {
        float strength = b.weight(p);
        if (strength <= 0f || !b.hasOwnNoise()) return strength;
        float solid = b.solidity(strength);
        if (solid >= 1f) return strength;
        float mask = EctorumBiome.smooth(b.noiseThreshold, b.noiseThreshold + b.noiseFeather, surfaceNoise(b.noiseSeed(), b.noiseOctaves, b.noiseScale, point));
        return strength * Mathf.lerp(mask, 1f, solid);
    }

    /** При близких весах шум иногда отдаёт точку второму претенденту: граница становится рваной. */
    protected EctorumBiome dither(EctorumBiome first, EctorumBiome second, float w1, float w2, Vec3 point, float scale, int offset) {
        if (scale <= 0f || second == null || w1 <= 0f || biomeBorderWidth <= 0f) return first;
        float gap = (w1 - w2) / w1;
        if (gap >= biomeBorderWidth) return first;
        return spread(surfaceNoise(offset, 2, scale, point)) > 0.5f + 0.5f * gap / biomeBorderWidth ? second : first;
    }

    // Материализация.

    /** Выбор из массива по шуму: середина массива выпадает чаще краёв. Массив не должен быть пустым. */
    protected <T> T pick(T[] options, EctorumBiome biome, Vec3 point, int channel) {
        float v = spread(surfaceNoise(channel + biome.noiseSeed(), 3, paletteScale, point));
        return options[Math.min((int)(v * options.length), options.length - 1)];
    }

    /** Чья сухая палитра действует в точке: основы или хозяина (пятна, если есть, иначе биома). */
    protected EctorumBiome paletteSource(Vec3 point, EctorumBiome biome, EctorumBiome patch) {
        EctorumBiome owner = patch != null ? patch : biome;
        return owner != baseBiome && spread(surfaceNoise(210, 3, paletteScale, point)) < owner.baseMix ? baseBiome : owner;
    }

    protected Floor floorFor(Vec3 point, SurfacePoint p, EctorumBiome biome, EctorumBiome patch) {
        if (p.water > 0f) {
            Floor[] depth = (patch != null ? patch : biome).liquidFloors;
            return depth[Math.min((int)(p.water * depth.length), depth.length - 1)];
        }
        EctorumBiome source = paletteSource(point, biome, patch);
        return pick(source.floors, source, point, 200);
    }

    /** Своя стена пола; доля wallMix — другая стена из набора биома. Нет стен — стены основы. */
    protected Block wallFor(Vec3 point, EctorumBiome biome, EctorumBiome patch, Floor floor) {
        EctorumBiome source = paletteSource(point, biome, patch);
        if (source.walls.length == 0) source = baseBiome;
        Block matched = wallOf(floor);
        boolean hasMatch = matched != null && matched != Blocks.air;
        if (source.walls.length == 0) return hasMatch ? matched : Blocks.air;
        if (hasMatch && spread(surfaceNoise(260 + source.noiseSeed(), 3, paletteScale, point)) >= source.wallMix) return matched;
        return pick(source.walls, source, point, 250);
    }

    // Глобус.

    @Override
    public float getHeight(Vec3 position) {
        SurfacePoint p = sample(position);
        //вода — плоская поверхность, как max(height, water) у Серпуло
        if (p.water > 0f) return minMeshHeight;
        float land = clamp((p.height - waterLevel) / (1f - waterLevel));
        return Mathf.lerp(minMeshHeight, maxMeshHeight, Mathf.pow(land, terrainCurve));
    }

    @Override
    public void getColor(Vec3 position, Color out) {
        ensureBiomes();
        SurfacePoint p = sample(position);
        if (debugView != null && !debugView.equals("none")) {
            debugColor(position, p, out);
            out.a = 1f;
            return;
        }
        EctorumBiome biome = biomeAt(position, p, ditherScaleGlobe), patch = patchAt(position, p, ditherScaleGlobe);
        if (p.water > 0f) {
            Floor liquid = floorFor(position, p, biome, patch);
            //как в ванили: альфа несёт альбедо блока, а не прозрачность
            out.set(liquid.mapColor).a(1f - liquid.albedo);
            return;
        }
        out.set(biome.color);
        if (patch != null) out.lerp(patch.color, patchColorBlend);
        out.mul(Mathf.lerp(shadeLow, shadeHigh, p.height));
        out.a = 1f - landAlbedo;
    }

    protected void debugColor(Vec3 position, SurfacePoint p, Color out) {
        switch (debugView) {
            case "height" -> {
                if (p.water > 0f) out.set(0.1f, 0.25f, 0.6f, 1f).mul(1f - p.water * 0.6f);
                else out.set(Color.darkGray).lerp(Color.white, (p.height - waterLevel) / (1f - waterLevel));
            }
            case "continent" -> out.set(Color.navy).lerp(Color.tan, clamp((p.continent - 0.25f) * 2f));
            case "temperature" -> out.set(Color.royal).lerp(Color.scarlet, p.temperature);
            case "humidity" -> out.set(Color.brown).lerp(Color.forest, p.humidity);
            case "tectonic" -> out.set(Color.black).lerp(Color.orange, p.tectonic);
            case "roughness" -> out.set(Color.black).lerp(Color.white, p.roughness);
            case "biome" -> out.set(biomeAt(position, p, ditherScaleGlobe).color);
            case "patch" -> {
                EctorumBiome patch = patchAt(position, p, ditherScaleGlobe);
                out.set(patch == null ? Color.darkGray : patch.color);
            }
            default -> out.set(Color.magenta);
        }
    }

    // Тайлы.

    @Override
    protected void genTile(Vec3 position, TileGen tile) {
        ensureBiomes();
        SurfacePoint p = sample(position);
        EctorumBiome biome = biomeAt(position, p, ditherScaleTile), patch = patchAt(position, p, ditherScaleTile);
        EctorumBiome owner = patch != null ? patch : biome;
        Floor floor = floorFor(position, p, biome, patch);
        tile.floor = floor;
        if (p.water <= 0f && surfaceNoise(300, 3, owner.wallScale, position) > owner.wallThreshold - p.roughness * 0.17f) {
            tile.block = wallFor(position, biome, patch, floor);
        }
    }

    protected Vec3 project(int x, int y) {
        return sector.rect.project(x / (float)width, y / (float)height);
    }

    /** Биомы всех тайлов сектора. Та же проекция, что у движка, поэтому совпадает с genTile при любом порядке обхода. */
    protected void mapTileBiomes() {
        tileBiomes = new EctorumBiome[width * height];
        tilePatches = new EctorumBiome[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Vec3 point = project(x, y);
                SurfacePoint p = sample(point);
                tileBiomes[x + y * width] = biomeAt(point, p, ditherScaleTile);
                tilePatches[x + y * width] = patchAt(point, p, ditherScaleTile);
            }
        }
    }

    /** Биом, чью палитру (сухую или жидкую) использовать для тайла после его изменения. */
    protected EctorumBiome tileSource(int index, boolean liquid) {
        EctorumBiome patch = tilePatches[index], biome = tileBiomes[index];
        if (patch != null && (liquid ? patch.liquidFloors : patch.floors).length > 0) return patch;
        return (liquid ? biome.liquidFloors : biome.floors).length > 0 ? biome : baseBiome;
    }

    protected Block wallForTile(int x, int y) {
        int i = x + y * width;
        return wallFor(project(x, y), tileBiomes[i], tilePatches[i], tiles.getn(x, y).floor());
    }

    /** Сглаживание стен клеточным автоматом. В отличие от cells(), сохраняет стены биомов и не строит стены на воде. */
    protected void smoothWalls() {
        if (wallSmoothing <= 0) return;
        int size = width * height;
        boolean[] current = new boolean[size], next = new boolean[size];
        for (int i = 0; i < size; i++) current[i] = tiles.geti(i).block() != Blocks.air;
        for (int iteration = 0; iteration < wallSmoothing; iteration++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = x + y * width;
                    if (tiles.geti(index).floor().isLiquid) {
                        next[index] = false;
                        continue;
                    }
                    int nearby = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx, ny = y + dy;
                            if (nx < 0 || ny < 0 || nx >= width || ny >= height || current[nx + ny * width]) nearby++;
                        }
                    }
                    next[index] = nearby >= (current[index] ? 4 : 5);
                }
            }
            boolean[] swap = current;
            current = next;
            next = swap;
        }
        for (int i = 0; i < size; i++) {
            Tile tile = tiles.geti(i);
            boolean hasWall = tile.block() != Blocks.air;
            if (current[i] == hasWall) continue;
            if (!current[i]) {
                tile.setBlock(Blocks.air);
            } else {
                Block wall = wallForTile(tile.x, tile.y);
                if (wall != Blocks.air) tile.setBlock(wall);
            }
        }
    }

    // Слой гарантий: поляны, коридоры, замуровка.
    protected record Room(int x, int y, int radius) {}

    /** Точка высадки: место с наибольшим количеством суши на кольце вокруг центра. */
    protected Room choosePlayerRoom() {
        int margin = Math.max(8, Math.min(width, height) / 12);
        float length = Math.min(width, height) / 2.65f - margin;
        Room best = null;
        int bestScore = -1, offset = rand.random(359);
        for (int step = 0; step < 72; step++) {
            float angle = offset + step * 5f;
            int x = clamp((int)(width / 2f + Angles.trnsx(angle, length)), margin, width - margin - 1);
            int y = clamp((int)(height / 2f + Angles.trnsy(angle, length)), margin, height - margin - 1);
            int score = 0;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dy = -5; dy <= 5; dy++) {
                    Tile tile = tiles.get(x + dx, y + dy);
                    if (tile != null && tile.floor().hasSurface()) score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = new Room(x, y, 14);
            }
            if (score >= 110) break;
        }
        return best == null ? new Room(width / 2, height / 2, 14) : best;
    }

    /** Вражеские поляны напротив игрока, в пределах ±60° и на расстоянии друг от друга. */
    protected Seq<Room> placeEnemyRooms(Room player, int count) {
        Seq<Room> out = new Seq<>();
        float cx = width / 2f, cy = height / 2f;
        float angle = Angles.angle(cx, cy, player.x, player.y) + 180f;
        float length = Math.max(Mathf.dst(cx, cy, player.x, player.y), Math.min(width, height) / 3f);
        for (int attempt = 0; attempt < count * 20 && out.size < count; attempt++) {
            float a = angle + rand.range(60f);
            int r = rand.random(11, 15);
            int x = clamp((int)(cx + Angles.trnsx(a, length)), r + 3, width - r - 4);
            int y = clamp((int)(cy + Angles.trnsy(a, length)), r + 3, height - r - 4);
            if (!nearAnyRoom(x, y, out, r + 27f)) out.add(new Room(x, y, r));
        }
        if (out.isEmpty()) out.add(new Room(clamp(width - 1 - player.x, 17, width - 18), clamp(height - 1 - player.y, 17, height - 18), 12));
        return out;
    }

    /** Поляны для строительства. */
    protected Seq<Room> placeClearings(Room player, Seq<Room> enemies) {
        Seq<Room> out = new Seq<>();
        int margin = 14;
        for (int attempt = 0; attempt < buildClearings * 10 && out.size < buildClearings; attempt++) {
            int r = rand.random(9, 15);
            int x = rand.random(margin, width - margin - 1), y = rand.random(margin, height - margin - 1);
            if (Mathf.within(x, y, player.x, player.y, player.radius + r + 6f) || nearAnyRoom(x, y, enemies, r + 20f)) continue;
            out.add(new Room(x, y, r));
        }
        return out;
    }

    protected boolean nearAnyRoom(int x, int y, Seq<Room> rooms, float distance) {
        for (int i = 0; i < rooms.size; i++) {
            if (Mathf.within(x, y, rooms.get(i).x, rooms.get(i).y, distance)) return true;
        }
        return false;
    }

    /** Вырезает круг. dry = true — жидкий пол заменяется сухим полом биома тайла. */
    protected void carve(int centerX, int centerY, int radius, boolean dry) {
        int radius2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy > radius2) continue;
                int x = centerX + dx, y = centerY + dy;
                Tile tile = tiles.get(x, y);
                if (tile == null) continue;
                tile.setBlock(Blocks.air);
                if (dry && !tile.floor().hasSurface()) {
                    EctorumBiome source = tileSource(x + y * width, false);
                    tile.setFloor(pick(source.floors, source, project(x, y), 200));
                    tile.clearOverlay();
                }
            }
        }
    }

    /** Сухой коридор между полянами. Путь ищется в обход стен и воды; если не найден — прямая линия. */
    protected void connect(Room from, Room to, int radius) {
        Seq<Tile> path = pathfind(from.x, from.y, to.x, to.y, tile -> 1f + (tile.solid() ? 8f : 0f) + (tile.floor().hasSurface() ? 0f : 12f) + Simplex.noise2d(seed, 2, 0.5f, 1f / 65f, tile.x, tile.y) * 3f, Astar.manhattan);
        if (!path.isEmpty()) {
            for (int i = 0; i < path.size; i++) carve(path.get(i).x, path.get(i).y, radius, true);
            return;
        }
        int steps = Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float)Math.max(steps, 1);
            carve(Math.round(Mathf.lerp(from.x, to.x, t)), Math.round(Mathf.lerp(from.y, to.y, t)), radius, true);
        }
    }

    protected float waterFraction() {
        int total = 0, water = 0;
        for (Tile tile : tiles) {
            if (tile.block() != Blocks.air) continue;
            total++;
            if (tile.floor().isLiquid && tile.floor().liquidDrop == Liquids.water) water++;
        }
        return total == 0 ? 0f : water / (float)total;
    }

    protected void drawWater(int centerX, int centerY, int radius, Room player) {
        int radius2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy > radius2) continue;
                int x = centerX + dx, y = centerY + dy;
                Tile tile = tiles.get(x, y);
                if (tile == null || Mathf.within(x, y, player.x, player.y, 20f)) continue;
                tile.setBlock(Blocks.air);
                tile.setFloor(baseBiome.liquidFloors[0]);
                tile.clearOverlay();
            }
        }
    }

    /** Водный канал от врага к игроку для морских волн. */
    protected void connectWater(Room enemy, Room player) {
        int steps = Math.max(Math.abs(enemy.x - player.x), Math.abs(enemy.y - player.y));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float)Math.max(steps, 1);
            drawWater(Math.round(Mathf.lerp(enemy.x, player.x, t)), Math.round(Mathf.lerp(enemy.y, player.y, t)), 4, player);
        }
        drawWater(enemy.x, enemy.y, 7, player);
    }

    /** Глубокая вода у суши становится мелкой водой своего биома. */
    protected void shapeCoasts() {
        for (Tile tile : tiles) {
            if (tile.block() != Blocks.air || !tile.floor().isDeep()) continue;
            boolean nearLand = false;
            for (int dx = -2; dx <= 2 && !nearLand; dx++) {
                for (int dy = -2; dy <= 2 && !nearLand; dy++) {
                    Tile other = tiles.get(tile.x + dx, tile.y + dy);
                    nearLand = other != null && other.floor().hasSurface();
                }
            }
            if (!nearLand) continue;
            Floor shallow = tileSource(tile.x + tile.y * width, true).liquidFloors[0];
            if (!shallow.isDeep()) tile.setFloor(shallow);
        }
    }

    /** Недостижимые от точки высадки сухие карманы замуровываются стенами своего биома. */
    protected void sealDisconnected(Room spawn) {
        int size = width * height;
        boolean[] visited = new boolean[size];
        int[] queue = new int[size];
        int head = 0, tail = 0, start = spawn.x + spawn.y * width;
        visited[start] = true;
        queue[tail++] = start;
        while (head < tail) {
            int current = queue[head++], x = current % width, y = current / width;
            for (int d = 0; d < 4; d++) {
                int nx = x + (d == 0 ? 1 : d == 1 ? -1 : 0), ny = y + (d == 2 ? 1 : d == 3 ? -1 : 0);
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                int next = nx + ny * width;
                if (visited[next]) continue;
                Tile tile = tiles.getn(nx, ny);
                if (tile.block() == Blocks.air && tile.floor().hasSurface()) {
                    visited[next] = true;
                    queue[tail++] = next;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            Tile tile = tiles.geti(i);
            if (visited[i] || tile.block() != Blocks.air || !tile.floor().hasSurface()) continue;
            Block wall = wallForTile(tile.x, tile.y);
            if (wall != Blocks.air) tile.setBlock(wall);
        }
    }

    // Руды.

    protected Block oreFor(EctorumBiome biome, int x, int y, boolean wall, Block host) {
        if (biome == null) return null;
        Block[] candidates = wall ? biome.wallOres : biome.ores;
        for (int i = candidates.length - 1; i >= 0; i--) {
            Block candidate = candidates[i];
            if (!oreAllowed(candidate, host)) continue;
            int noiseSeed = seed + biome.noiseSeed() + i * 97 + (wall ? 13007 : 0);
            float vein = Simplex.noise2d(noiseSeed, 3, 0.56f, 1f / (wall ? 37f : 51f), x, y);
            float variation = Simplex.noise2d(noiseSeed + 31, 2, 0.5f, 1f / 17f, x, y);
            if (vein > (wall ? 0.63f : 0.61f) + i * 0.018f && variation > 0.47f) return candidate;
        }
        return null;
    }

    /** Руды: сначала пятна, затем биома, затем основы. */
    protected void generateOres() {
        for (Tile tile : tiles) {
            int i = tile.x + tile.y * width;
            boolean wall = tile.block() != Blocks.air;
            if (tile.overlay() != Blocks.air) continue;
            if (wall ? !tile.block().isStatic() || !nearAir(tile.x, tile.y) : !tile.floor().hasSurface()) continue;
            Block host = wall ? tile.block() : tile.floor();
            Block ore = oreFor(tilePatches[i], tile.x, tile.y, wall, host);
            if (ore == null) ore = oreFor(tileBiomes[i], tile.x, tile.y, wall, host);
            if (ore == null && tileBiomes[i] != baseBiome) ore = oreFor(baseBiome, tile.x, tile.y, wall, host);
            if (ore == null) continue;
            if (!wall || (ore instanceof Floor f && f.wallOre)) tile.setOverlay(ore);
            else tile.setBlock(ore);
        }
    }

    protected static boolean contains(Block[] array, Block target) {
        for (Block block : array) {
            if (block == target) return true;
        }
        return false;
    }

    /** Гарантирует немного руды вокруг точки высадки, если её там почти нет. */
    protected void ensureStarterOre(Room player, Block target) {
        int available = 0;
        for (int dx = -26; dx <= 26; dx++) {
            for (int dy = -26; dy <= 26; dy++) {
                Tile tile = tiles.get(player.x + dx, player.y + dy);
                if (tile != null && tile.overlay() == target) available++;
            }
        }
        if (available >= 8) return;
        int placed = 0;
        for (int attempt = 0; attempt < 400 && placed < 12; attempt++) {
            float angle = rand.random(360f), dst = rand.random(10f, 23f);
            int x = player.x + (int)Angles.trnsx(angle, dst), y = player.y + (int)Angles.trnsy(angle, dst);
            Tile tile = tiles.get(x, y);
            if (tile == null || tile.block() != Blocks.air || tile.overlay() != Blocks.air || !tile.floor().hasSurface()) continue;
            int i = x + y * width;
            if (oreAllowed(target, tile.floor()) && (contains(baseBiome.ores, target) || contains(tileBiomes[i].ores, target) || (tilePatches[i] != null && contains(tilePatches[i].ores, target)))) {
                tile.setOverlay(target);
                placed++;
            }
        }
        if (available + placed == 0) Log.warn("[Ectorum] У точки высадки нет руды @", target.name);
    }

    // Структуры, руины, декорации.

    /** Квадрат свободен, сух и весь принадлежит биому. */
    protected boolean sameBiomeArea(EctorumBiome biome, int cx, int cy, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int x = cx + dx, y = cy + dy;
                Tile tile = tiles.get(x, y);
                if (tile == null || !tile.floor().hasSurface() || tile.block() != Blocks.air) return false;
                int i = x + y * width;
                if (tilePatches[i] != biome && tileBiomes[i] != biome) return false;
            }
        }
        return true;
    }

    protected void generateStructures(Room player, Seq<Room> enemies) {
        for (Tile tile : tiles) {
            if (tile.block() != Blocks.air || !tile.floor().hasSurface() || tile.overlay() != Blocks.air || !rand.chance(structureChance)) continue;
            if (Mathf.within(tile.x, tile.y, player.x, player.y, 22f) || nearAnyRoom(tile.x, tile.y, enemies, 16f)) continue;
            int i = tile.x + tile.y * width;
            EctorumBiome owner = tilePatches[i] != null && tilePatches[i].structureFloor != null ? tilePatches[i] : tileBiomes[i].structureFloor != null ? tileBiomes[i] : null;
            if (owner == null || !sameBiomeArea(owner, tile.x, tile.y, 3)) continue;
            buildBiomeStructure(owner, tile.x, tile.y);
        }
    }

    /** Открытая площадка из structureFloor с угловыми structureWall. Своё сооружение — переопредели метод. */
    protected void buildBiomeStructure(EctorumBiome biome, int cx, int cy) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                Tile tile = tiles.getn(cx + dx, cy + dy);
                tile.setFloor(biome.structureFloor);
                tile.clearOverlay();
                if (biome.structureWall != null && Math.abs(dx) == 2 && Math.abs(dy) == 2) tile.setBlock(biome.structureWall);
            }
        }
    }

    /** Схема руин из реестра баз, если все её блоки доступны на планете. Иначе null. */
    protected BasePart chooseDerelictPart(Tile tile) {
        BasePart candidate = null;
        if (tile.drop() != null) candidate = bases.forResource(tile.drop()).getFrac(clamp(sector.threat));
        if (candidate == null) candidate = bases.parts.getFrac(clamp(sector.threat));
        if (candidate == null) return null;
        for (var entry : candidate.schematic.tiles) {
            if (!entry.block.isOnPlanet(sector.planet)) return null;
        }
        return candidate;
    }

    protected void buildScrapRuin(int cx, int cy) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                Tile tile = tiles.getn(cx + dx, cy + dy);
                if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) tile.setFloor(floor(EctoBlocks.metalPlates1));
                if ((Math.abs(dx) == 2 || Math.abs(dy) == 2) && rand.chance(0.24)) tile.setBlock(EctoBlocks.darkScrapWall, Team.derelict);
                else if (tile.overlay() == Blocks.air && rand.chance(0.35)) tile.setOverlay(Blocks.oreScrap);
            }
        }
    }

    protected void generateDerelict(Room player, Seq<Room> enemies) {
        int limit = Math.max(1, Math.min(5, 1 + (int)(sector.threat * 4f))), placed = 0;
        for (Tile tile : tiles) {
            if (placed >= limit) break;
            if (tile.block() != Blocks.air || !tile.floor().hasSurface() || !rand.chance(ruinChance)) continue;
            if (Mathf.within(tile.x, tile.y, player.x, player.y, 27f) || nearAnyRoom(tile.x, tile.y, enemies, 22f)) continue;
            int i = tile.x + tile.y * width;
            EctorumBiome owner = tilePatches[i] != null && tilePatches[i].derelict ? tilePatches[i] : tileBiomes[i].derelict ? tileBiomes[i] : null;
            if (owner == null) continue;
            BasePart part = chooseDerelictPart(tile);
            if (part != null) {
                //радиус с запасом на повороты схемы и многоклеточные блоки
                int radius = Math.max(part.schematic.width, part.schematic.height) + 4;
                if (!sameBiomeArea(owner, tile.x, tile.y, radius)) continue;
                boolean success = BaseGenerator.tryPlace(part, tile.x, tile.y, Team.derelict, rand, (x, y) -> {
                    Tile at = tiles.get(x, y);
                    if (at == null || !at.floor().hasSurface()) return;
                    int index = x + y * width;
                    if (tilePatches[index] == owner || tileBiomes[index] == owner) at.setOverlay(Blocks.oreScrap);
                });
                if (success) placed++;
            } else if (sameBiomeArea(owner, tile.x, tile.y, 3)) {
                buildScrapRuin(tile.x, tile.y);
                placed++;
            }
        }
    }

    /** Декорации пятна (или биома, если пятна нет); без своих — Floor.decoration. */
    protected void decorate(Room player, Seq<Room> enemies) {
        for (Tile tile : tiles) {
            if (tile.block() != Blocks.air || !tile.floor().hasSurface() || tile.overlay() != Blocks.air || !rand.chance(decorationChance)) continue;
            if (Mathf.within(tile.x, tile.y, player.x, player.y, 18f) || nearAnyRoom(tile.x, tile.y, enemies, 11f)) continue;
            int i = tile.x + tile.y * width;
            Block[] options = (tilePatches[i] != null ? tilePatches[i] : tileBiomes[i]).decorations;
            Block decoration = options.length > 0 ? options[rand.random(options.length - 1)] : tile.floor().decoration;
            if (decoration == null || decoration == Blocks.air) continue;
            if (decoration.isOverlay()) tile.setOverlay(decoration);
            else tile.setBlock(decoration);
        }
    }

    // Враг и правила.

    /** Временная вражеская база из блоков EctoTech: ядро, кольцо стен с проходом, турель. */
    protected void placeEnemyBase(Room room) {
        carve(room.x, room.y, Math.max(room.radius, 11), true);
        Tile center = tiles.getn(room.x, room.y);
        center.clearOverlay();
        center.setBlock(EctoBlocks.coreSpark, state.rules.waveTeam, 0);
        for (int dx = -9; dx <= 9; dx++) {
            for (int dy = -9; dy <= 9; dy++) {
                int distance2 = dx * dx + dy * dy;
                if (distance2 < 64 || distance2 > 81 || (dx > 0 && Math.abs(dy) <= 2)) continue;
                Tile tile = tiles.get(room.x + dx, room.y + dy);
                if (tile != null && tile.block() == Blocks.air && tile.floor().hasSurface() && rand.chance(0.82)) tile.setBlock(EctoBlocks.bismuthWall, state.rules.waveTeam);
            }
        }
        Tile turret = tiles.get(room.x - 4, room.y + 3);
        if (turret == null || turret.block() != Blocks.air || !turret.floor().hasSurface()) return;
        turret.setBlock(EctoBlocks.sentinel, state.rules.waveTeam, 0);
        if (turret.build == null) return;
        //как конвейер: турель сама решает, принимать ли предмет как патрон
        for (Item item : enemyTurretAmmo) {
            for (int i = 0; i < 10 && turret.build.acceptItem(null, item); i++) turret.build.handleItem(null, item);
        }
    }

    protected void setupSectorRules(boolean attack, boolean naval) {
        Rules rules = state.rules;
        float difficulty = sector.threat;
        rules.attackMode = sector.info.attack = attack;
        if (!attack) rules.winWave = sector.info.winWave = 10 + 5 * (int)Math.max(difficulty * 10f, 1f);
        rules.waves = true;
        rules.waveSpacing = Mathf.lerp(60f * 65f * 2f, 60f * 60f, clamp((difficulty - 0.4f) / 0.8f));
        rules.enemyCoreBuildRadius = 600f;
        rules.spawns = createWaves(difficulty, attack, naval);
    }

    /** Переопредели здесь ванильные волны на EctoUnitTypes. */
    protected Seq<SpawnGroup> createWaves(float difficulty, boolean attack, boolean naval) {
        return Waves.generate(difficulty, new Rand(sector.id), attack, false, naval);
    }

    @Override
    protected void generate() {
        ensureBiomes();
        mapTileBiomes();
        try {
            smoothWalls();
            boolean attack = sector.hasEnemyBase();
            boolean naval = !attack && waterFraction() >= navalWaterShare;

            Room player = choosePlayerRoom();
            Seq<Room> enemies = placeEnemyRooms(player, Math.max(1, Math.min(3, 1 + (int)(sector.threat * 2f))));
            Seq<Room> clearings = placeClearings(player, enemies);

            carve(player.x, player.y, player.radius, true);
            for (Room room : clearings) {
                carve(room.x, room.y, room.radius, false);
                connect(player, room, rand.random(3, 4));
            }
            //гарантия: каждая волна может дойти до игрока
            for (Room enemy : enemies) {
                carve(enemy.x, enemy.y, enemy.radius, !naval);
                if (naval) connectWater(enemy, player);
                else connect(player, enemy, rand.random(4, 6));
            }

            shapeCoasts();
            sealDisconnected(player);

            generateOres();
            for (Block ore : starterOres) ensureStarterOre(player, ore);
            generateStructures(player, enemies);
            generateDerelict(player, enemies);
            decorate(player, enemies);

            placeCore(player);

            for (Room enemy : enemies) {
                //в режиме атаки волны идут от вражеских ядер, точка появления не нужна
                if (attack) placeEnemyBase(enemy);
                else tiles.getn(enemy.x, enemy.y).setOverlay(Blocks.spawn);
            }

            setupSectorRules(attack, naval);
        } finally {
            tileBiomes = tilePatches = null;
        }
    }

    // Кампания.

    @Override
    public void generateSector(Sector sector) {
        if (sector.preset != null || sector.id == sector.planet.startSector) return;
        //threat не трогаем: его позже пересчитывает сама планета
        SurfacePoint p = sample(sector.tile.v);
        Rand selector = new Rand((long)sector.id * 0x9E3779B9L + surfaceNoiseSeed(700));
        float activity = clamp(p.tectonic * 0.4f + p.roughness * 0.6f);
        sector.generateEnemyBase |= selector.chance(Mathf.lerp(0.08f, 0.29f, activity));
    }

    /** Кампания пройдена: захвачен пресет с isLastSector. Результат кешируется: метод зовётся из интерфейса. */
    public boolean campaignComplete(Sector sector) {
        if (!campaignChecked) {
            campaignDone = sector.planet.sectors.contains(s -> s.preset != null && s.preset.isLastSector && s.isCaptured());
            campaignChecked = true;
        }
        return campaignDone;
    }

    /** Открыты ли numbered-сектора: сборка разработчика либо пройденная кампания. */
    public boolean numberedUnlocked(Sector sector) {
        return sector.planet.allowLaunchToNumbered || campaignComplete(sector);
    }

    @Override
    public boolean allowLanding(Sector sector) {
        if (debugUnlockAll || sector.hasBase()) return true;
        if (sector.preset != null) return sector.preset.alwaysUnlocked || sector.preset.unlocked();
        return numberedUnlocked(sector) && sector.near().contains(Sector::hasBase);
    }

    protected void placeCore(Room player) {
        Schematic loadout = universe.getLastLoadout();
        if (!loadout.tiles.contains(t -> t.block == sector.planet.defaultCore)) loadout = EctoLoadouts.basicSpark;
        Schematics.placeLoadout(loadout, player.x, player.y, state.rules.defaultTeam);
        Tile core = tiles.getn(player.x, player.y);
        if (core.build == null) throw new IllegalStateException("[Ectorum] Ядро не установлено в точке высадки.");
        core.build.items.add(universe.getLaunchResources());
    }

    @Override
    public @Nullable Sector findLaunchCandidate(Sector destination, @Nullable Sector selected) {
        if (debugUnlockAll) return selected != null && selected.hasBase() ? selected : destination.planet.sectors.find(Sector::hasBase);
        if (destination.preset != null || !numberedUnlocked(destination)) return super.findLaunchCandidate(destination, selected);
        if (selected != null && selected.isNear(destination) && selected.hasBase()) return selected;
        return destination.near().find(Sector::hasBase);
    }

    @Override
    public void getLockedText(Sector hovered, StringBuilder out) {
        if (hovered.preset == null && !numberedUnlocked(hovered) && hovered.near().contains(Sector::hasBase)) {
            out.append("[red]").append(Iconc.cancel).append("[]").append(Core.bundle.get("ectotech.sector.campaignrequired"));
        } else {
            super.getLockedText(hovered, out);
        }
    }

    @Override
    public void onSectorCaptured(Sector sector) {
        campaignChecked = false;
        sector.planet.reloadMeshAsync();
    }

    @Override
    public void onSectorLost(Sector sector) {
        campaignChecked = false;
        sector.planet.reloadMeshAsync();
    }

    @Override
    public void beforeSaveWrite(Sector sector) {
        sector.planet.reloadMeshAsync();
    }

    // Погода.

    @Override
    public void addWeather(Sector sector, Rules rules) {
        ensureBiomes();
        Seq<WeatherEntry> result = new Seq<>();
        Seq<WeatherEntry> explicit = explicitWeather(sector);
        if (explicit != null) {
            for (WeatherEntry entry : explicit) result.add(copyWeather(entry));
        } else {
            addBiomeWeather(sector, result);
        }
        //новый список, а не clear(): пресет может хранить погоду в общем Seq
        rules.weather = result;
    }

    /** 1) погода из rules пресета; 2) погода из правил файла карты; null — нигде не задана. */
    protected Seq<WeatherEntry> explicitWeather(Sector sector) {
        if (sector.preset == null) return null;
        //rules пресета — функция: применяем её к пустым правилам и смотрим, что она записала в weather
        Rules probe = new Rules();
        sector.preset.rules.get(probe);
        if (!probe.weather.isEmpty()) return probe.weather;
        Map map = sector.preset.generator == null ? null : sector.preset.generator.map;
        if (map == null) return null;
        Seq<WeatherEntry> fromMap = map.rules().weather;
        return fromMap.isEmpty() ? null : fromMap;
    }

    /** 3) погода по долям биомов на поверхности под сектором. */
    protected void addBiomeWeather(Sector sector, Seq<WeatherEntry> out) {
        int[] counts = new int[biomes.size];
        for (int gx = 0; gx < weatherSamples; gx++) {
            for (int gy = 0; gy < weatherSamples; gy++) {
                Vec3 point = sector.rect.project((gx + 0.5f) / weatherSamples, (gy + 0.5f) / weatherSamples);
                SurfacePoint p = sample(point);
                counts[biomes.indexOf(biomeAt(point, p, 0f), true)]++;
                EctorumBiome patch = patchAt(point, p, 0f);
                if (patch != null) counts[biomes.indexOf(patch, true)]++;
            }
        }
        float total = weatherSamples * weatherSamples;
        for (int i = 0; i < biomes.size; i++) {
            EctorumBiome b = biomes.get(i);
            if (counts[i] / total < b.weatherMinShare) continue;
            for (WeatherEntry entry : b.weather) {
                if (out.find(e -> e.weather == entry.weather) == null) out.add(copyWeather(entry));
            }
        }
    }

    /** Полная копия записи. При обновлении Mindustry сверить список полей WeatherEntry. */
    protected WeatherEntry copyWeather(WeatherEntry source) {
        WeatherEntry copy = new WeatherEntry();
        copy.weather = source.weather;
        copy.minFrequency = source.minFrequency;
        copy.maxFrequency = source.maxFrequency;
        copy.minDuration = source.minDuration;
        copy.maxDuration = source.maxDuration;
        copy.cooldown = source.cooldown;
        copy.intensity = source.intensity;
        copy.always = source.always;
        return copy;
    }

    // Отладка.

    /** Распределения полей и доли биомов по планете. Вызывать вручную из консоли. */
    public void calibrate(int count) {
        ensureBiomes();
        if (count <= 0) throw new IllegalArgumentException("count должен быть > 0");
        String[] names = {"height", "roughness", "latitude", "temperature", "humidity", "water", "tectonic", "continent"};
        float[][] fields = new float[names.length][count];
        int[] hits = new int[biomes.size];
        int water = 0;
        Vec3 point = new Vec3();
        for (int i = 0; i < count; i++) {
            SurfacePoint p = sample(fibonacci(i, count, point));
            fields[0][i] = p.height;
            fields[1][i] = p.roughness;
            fields[2][i] = p.latitude;
            fields[3][i] = p.temperature;
            fields[4][i] = p.humidity;
            fields[5][i] = p.water;
            fields[6][i] = p.tectonic;
            fields[7][i] = p.continent;
            if (p.water > 0f) water++;
            hits[biomes.indexOf(biomeAt(point, p, 0f), true)]++;
            EctorumBiome patch = patchAt(point, p, 0f);
            if (patch != null) hits[biomes.indexOf(patch, true)]++;
        }
        for (int i = 0; i < names.length; i++) {
            Arrays.sort(fields[i]);
            Log.info("[Ectorum] @: p5=@ p50=@ p95=@", names[i], percentile(fields[i], 0.05f), percentile(fields[i], 0.5f), percentile(fields[i], 0.95f));
        }
        Log.info("[Ectorum] Вода: @%", Mathf.round(water * 100f / count));
        for (int i = 0; i < biomes.size; i++) Log.info("[Ectorum] @: @% поверхности", biomes.get(i).name, Mathf.round(hits[i] * 100f / count));
    }

    protected static float percentile(float[] sorted, float fraction) {
        return sorted[clamp((int)(fraction * (sorted.length - 1)), 0, sorted.length - 1)];
    }

    /** i-я точка равномерной сетки Фибоначчи на единичной сфере. */
    protected static Vec3 fibonacci(int index, int count, Vec3 out) {
        float y = 1f - 2f * (index + 0.5f) / count;
        float radius = (float)Math.sqrt(1f - y * y);
        float angle = index * 2.3999632f;
        return out.set((float)Math.cos(angle) * radius, y, (float)Math.sin(angle) * radius);
    }
}