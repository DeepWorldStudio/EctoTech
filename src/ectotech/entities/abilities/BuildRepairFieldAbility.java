package ectotech.entities.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static mindustry.Vars.indexer;
import static mindustry.Vars.tilesize;

public class BuildRepairFieldAbility extends Ability {
    static final float refreshInterval = 12f;

    /** Maximum % of the maxHealth of the target for one pulse (close to the unit). */
    public float maxHealPercent = 4f;
    /** One pulse period, personal for each target */
    public float reload = 3 * 60f;
    /** Heal range (1 tile = 8f) */
    public float range = 8 * 8f;
    /** Max heal percent range */
    public float fullPowerRange = 8f;
    /** Heal multiplier for buildings that were damaged in the last ~500 ms. */
    public float recentDamageMultiplier = 0.1f;

    public Color healColor = Pal.accent;
    public Effect healEffect = Fx.healBlockFull;

    public float circleSpeed = 120f, circleStroke = 3f, squareRad = 6f, squareSpinScl = 0.8f;
    public float warmupSpeed = 0.08f;

    protected Seq<Building> targets = new Seq<>();
    protected float scanTimer = 0f;
    protected float warmup = 0f;
    protected float totalProgress = 0f;

    @Override
    public Ability copy() {
        BuildRepairFieldAbility a = (BuildRepairFieldAbility) super.copy();

        a.targets = new Seq<>();
        a.scanTimer = Mathf.random(refreshInterval);
        a.warmup = 0f;
        a.totalProgress = Mathf.random(circleSpeed);

        return a;
    }

    @Override
    public void addStats(Table t) {
        super.addStats(t);

        t.add(Core.bundle.format("bullet.range", Strings.autoFixed(range / tilesize, 1)));
        t.row();
        t.add(abilityStat("firingrate", Strings.autoFixed(60f / reload, 2)));
        t.row();
        t.add(abilityStat("buildrepair.heal", Strings.autoFixed(maxHealPercent, 1)));
    }

    @Override
    public void update(Unit unit) {
        if (reload > 0f) {
            data = (data + Time.delta) % reload;
        }

        if ((scanTimer += Time.delta) >= refreshInterval) {
            scanTimer = 0f;
            targets.clear();

            indexer.eachBlock(unit.team, unit.x, unit.y, range,
                    b -> b.damaged() && !b.isHealSuppressed(),
                    targets::add
            );
        }

        boolean anyTarget = false;

        for (Building b : targets) {
            if (!b.isValid() || !b.damaged() || b.isHealSuppressed()) continue;

            float dst = Math.max(unit.dst(b) - b.hitSize() / 2f, 0f);
            if (dst >= range) continue;

            float innerRange = Mathf.clamp(fullPowerRange, 0f, range);
            float falloff = dst <= innerRange
                    ? 1f
                    : 1f - Mathf.clamp((dst - innerRange) / Math.max(range - innerRange, 0.0001f));

            if (falloff <= 0f) continue;

            anyTarget = true;

            float phase = Mathf.randomSeed(b.pos(), 0f, reload);

            if ((data + phase) % reload < Time.delta) {
                float mul = b.wasRecentlyDamaged() ? recentDamageMultiplier : 1f;

                b.heal(b.maxHealth() * maxHealPercent / 100f * falloff * mul);
                b.recentlyHealed();

                healEffect.at(b.x, b.y, b.block.size, healColor, b.block);
            }
        }

        warmup = Mathf.lerpDelta(warmup, anyTarget ? 1f : 0f, warmupSpeed);
        totalProgress += Time.delta / circleSpeed;
    }

    @Override
    public void draw(Unit unit) {
        if (warmup <= 0.001f) return;

        Draw.z(Layer.effect);

        float mod = totalProgress % 1f;

        Draw.color(healColor);
        Lines.stroke(circleStroke * (1f - mod) * warmup);
        Lines.circle(unit.x, unit.y, range * mod);

        Fill.square(unit.x, unit.y, squareRad * warmup, Time.time / squareSpinScl);

        Draw.reset();
    }
}
