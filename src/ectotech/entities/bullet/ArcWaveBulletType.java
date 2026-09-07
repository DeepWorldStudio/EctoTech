package ectotech.entities.bullet;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.IntSet;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ArcWaveBulletType extends BulletType {
    public float arcAngle = 60f;
    public float hitWidth = 5f;
    public float stroke = 2.5f;

    /** Радиус, начиная с которого волна отображается. До него дуга невидима. */
    public float drawStartRadius = 0f;
    /** Длина плавного проявления после drawStartRadius, в мировых единицах. 0 = резкое появление. */
    public float drawFadeLength = 6f;

    public Color waveColor = Color.valueOf("7ad4ff");
    public Color innerColor = Color.white;

    /** Доля конца жизни, в течение которой волна растворяется (0..1). Меньше = позже и быстрее. */
    public float fadeFraction = 0.35f;
    /** Кривая растворения. pow2Out — мягко; pow3In — почти вся прозрачность падает в самом конце. */
    public Interp fadeInterp = Interp.pow2Out;
    /** Множитель толщины нижнего слоя. 0 = слоя нет. */
    public float glowStrokeScl = 3f;
    /** Прозрачность нижнего слоя относительно waveColor. */
    public float glowAlpha = 0.4f;
    /** Радиус точек на концах дуги. 0 = отключены. */
    public float tipRadius = 1.4f;

    public ArcWaveBulletType(float speed, float damage) {
        super(speed, damage);

        collides = false;
        collidesTiles = false;

        hittable = false;
        absorbable = false;
        reflectable = false;
        keepVelocity = false;
        pierce = true;
        pierceBuilding = true;

        lifetime = 40f;
        hitEffect = Fx.none;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        smokeEffect = Fx.none;
    }

    public static class ArcWaveData {
        public final float originX, originY;
        public final IntSet hitUnits = new IntSet();
        public final IntSet hitBuilds = new IntSet();

        public ArcWaveData(float x, float y) {
            originX = x;
            originY = y;
        }
    }

    @Override
    public void init() {
        super.init();
        drawSize = Math.max(drawSize, range * 2.2f);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        b.data = new ArcWaveData(b.x, b.y);
    }

    protected float radius(Bullet b, ArcWaveData d) {
        return Math.max(b.dst(d.originX, d.originY), 0.001f);
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        if (!(b.data instanceof ArcWaveData d)) return;

        float radius = radius(b, d);
        float half = arcAngle / 2f;
        float rot = b.rotation();

        Units.nearbyEnemies(b.team, d.originX, d.originY, radius + hitWidth + 12f, unit -> {
            if (unit.dead || d.hitUnits.contains(unit.id) || !unit.type.hittable) return;
            if (!unit.checkTarget(collidesAir, collidesGround)) return;

            float dst = unit.dst(d.originX, d.originY);
            if (Math.abs(dst - radius) > hitWidth + unit.hitSize / 2f) return;
            if (!Angles.within(rot, Angles.angle(d.originX, d.originY, unit.x, unit.y), half)) return;

            d.hitUnits.add(unit.id);
            hitEntity(b, unit, unit.health);
            hitEffect.at(unit.x, unit.y, rot, hitColor);
        });

        if (collidesGround && buildingDamageMultiplier > 0f) {
            float arcLength = radius * arcAngle * Mathf.degRad;
            int steps = Math.max(2, (int) (arcLength / (tilesize / 2f)));

            for (int i = 0; i <= steps; i++) {
                float angle = rot - half + arcAngle * i / steps;
                float px = d.originX + Angles.trnsx(angle, radius);
                float py = d.originY + Angles.trnsy(angle, radius);

                Building build = world.buildWorld(px, py);
                if (build == null || build.team == b.team || build.dead()) continue;
                if (build.block.underBullets || !build.collide(b)) continue;
                if (!d.hitBuilds.add(build.id)) continue;

                build.damage(b, b.team, b.damage * buildingDamageMultiplier);
                hitEffect.at(px, py, angle, hitColor);
            }
        }
    }

    @Override
    public void draw(Bullet b) {
        if (!(b.data instanceof ArcWaveData d)) return;

        float radius = radius(b, d);

        float appear = drawFadeLength <= 0f ? (radius >= drawStartRadius ? 1f : 0f) : Mathf.curve(radius, drawStartRadius, drawStartRadius + drawFadeLength);
        if (appear <= 0.001f) return;

        float life = fadeFraction <= 0f ? 1f : 1f - fadeInterp.apply(Mathf.curve(b.fin(), 1f - fadeFraction, 1f));

        float alpha = appear * life;
        if (alpha <= 0.001f) return;

        float start = b.rotation() - arcAngle / 2f;
        float fraction = arcAngle / 360f;

        Draw.z(layer);

        if (glowStrokeScl > 0f && glowAlpha > 0f) {
            Draw.color(waveColor, waveColor.a * glowAlpha * alpha);
            Lines.stroke(stroke * glowStrokeScl);
            Lines.arc(d.originX, d.originY, radius, fraction, start);
        }

        Draw.color(waveColor, waveColor.a * alpha);
        Lines.stroke(stroke);
        Lines.arc(d.originX, d.originY, radius, fraction, start);

        Draw.color(innerColor, waveColor.a * 0.6f * alpha);
        Lines.stroke(stroke * 0.4f);
        Lines.arc(d.originX, d.originY, radius, fraction, start);

        if (tipRadius > 0f) {
            Draw.color(innerColor, alpha);
            for (int s : Mathf.signs) {
                float a = b.rotation() + s * arcAngle / 2f;
                Fill.circle(
                        d.originX + Angles.trnsx(a, radius),
                        d.originY + Angles.trnsy(a, radius),
                        tipRadius
                );
            }
        }

        Draw.reset();
    }

    @Override
    public void drawLight(Bullet b) {
        if (lightOpacity <= 0f || lightRadius <= 0f) return;
        Drawf.light(b.x, b.y, lightRadius, lightColor, lightOpacity * b.fout());
    }
}
