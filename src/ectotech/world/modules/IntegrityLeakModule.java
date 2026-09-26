package ectotech.world.modules;

import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.Puddles;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Tile;

import static mindustry.Vars.world;

/** Состояние и логика утечки конкретного здания. Параметры копируются из блока при создании. */
public class IntegrityLeakModule {
    public final Building build;

    public float threshold, maxFraction, curve;
    public boolean isLeakDamages;
    public float damageThreshold, damagePerUnit;
    public float leakScale;

    /** Запомненная точка утечки; null — блок сейчас не течёт. */
    public @Nullable Tile leakSource, leakDest;
    private float flowLeaked, flowAmount;

    private final IntSeq openEdges = new IntSeq(), allEdges = new IntSeq();

    public IntegrityLeakModule(Building build, float threshold, float maxFraction, float curve, float leakScale,
                               boolean isLeakDamages, float damageThreshold, float damagePerUnit){
        this.build = build;
        this.threshold = threshold;
        this.maxFraction = maxFraction;
        this.curve = curve;
        this.leakScale = Math.max(leakScale, 0f);
        this.isLeakDamages = isLeakDamages;
        this.damageThreshold = damageThreshold;
        this.damagePerUnit = damagePerUnit;
    }

    /** 0 — на пороге или выше, 1 — при нулевом HP. */
    public float factor() {
        float limit = Mathf.clamp(threshold);
        if (limit <= 0f) return 0f;

        float severity = Mathf.clamp((limit - build.healthf()) / limit);
        return curve == 1f ? severity : Mathf.pow(severity, Math.max(curve, 0.01f));
    }

    /** Доля входящего потока, которая утечёт сейчас. */
    public float fraction() {
        return Mathf.clamp(maxFraction) * factor();
    }

    public boolean active() {
        return fraction() > 0f;
    }

    /**
     * Проточная утечка: выливает долю пакета и копит её для урона в update().
     * Урон здесь НЕ наносится.
     * @return остаток, который блок должен обработать как обычно
     */
    public float handleSplit(Liquid liquid, float amount) {
        if(amount <= 0f || build.tile == null || !build.isValid()) return amount;

        float frac = fraction();
        if(frac <= 0f) return amount;

        float leaked = amount * frac;
        if(leaked <= 0f) return amount;

        deposit(liquid, leaked);

        flowLeaked += leaked;
        flowAmount += amount;

        return amount - leaked;
    }

    /** Вызывать в updateTile: сбрасывает точку, когда блок перестал течь. */
    public float update() {
        if (!active()) {
            leakSource = leakDest = null;
            flowLeaked = flowAmount = 0f;
            return 0f;
        }

        if (flowAmount > 0f) {
            float leaked = flowLeaked, amount = flowAmount;
            flowLeaked = flowAmount = 0f;
            return damageFor(leaked, leaked / amount);
        }

        if(build.liquids == null || build.tile == null || !build.isValid()) return 0f;

        float stored = build.liquids.currentAmount();
        if(stored <= 0.0001f) return 0f;

        Liquid liquid = build.liquids.current();
        float frac = fraction();

        float leaked = Math.min(stored, stored * frac * build.delta() / 60f);
        if (leaked <= 0f) return 0f;

        build.liquids.remove(liquid, leaked);
        deposit(liquid, leaked);

        return damageFor(leaked, frac);
    }

    protected void deposit(Liquid liquid, float leaked) {
        if (leakSource == null || leakDest == null) findLeakEdge();

        float put = leaked * leakScale;
        if (put <= 0f) return;

        Puddles.deposit(leakDest, leakSource, liquid, put, true, true);
    }

    protected float damageFor(float leaked, float frac) {
        if(!isLeakDamages || Vars.net.client() || damagePerUnit <= 0f || leaked <= 0f || build.dead()) return 0f;
        return frac >= Mathf.clamp(damageThreshold) ? leaked * damagePerUnit : 0f;
    }

    protected void findLeakEdge(){
        openEdges.clear();
        allEdges.clear();

        int size = build.block.size, off = build.block.sizeOffset;
        int x1 = build.tile.x + off, y1 = build.tile.y + off;
        int x2 = x1 + size - 1, y2 = y1 + size - 1;

        for(int i = 0; i < size; i++){
            addEdge(x1 + i, y1, x1 + i, y1 - 1);
            addEdge(x1 + i, y2, x1 + i, y2 + 1);
            addEdge(x1, y1 + i, x1 - 1, y1 + i);
            addEdge(x2, y1 + i, x2 + 1, y1 + i);
        }

        IntSeq from = openEdges.size > 0 ? openEdges : allEdges;

        if(from.isEmpty()){
            leakSource = leakDest = build.tile;
            return;
        }

        int idx = Mathf.random(from.size / 2 - 1) * 2;
        leakSource = world.tile(from.get(idx));
        leakDest = world.tile(from.get(idx + 1));
    }

    private void addEdge(int ex, int ey, int nx, int ny) {
        Tile edge = world.tile(ex, ey), neighbor = world.tile(nx, ny);
        if (edge == null || neighbor == null) return;

        allEdges.add(edge.pos(), neighbor.pos());

        if (neighbor.block() == Blocks.air && !neighbor.floor().solid) {
            openEdges.add(edge.pos(), neighbor.pos());
        }
    }
}