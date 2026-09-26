package ectotech.entities.bullet;

import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.LongMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import ectotech.world.geometry.ArcWaveGeometry;
import ectotech.world.geometry.ArcWaveGeometry.Cut;
import ectotech.world.geometry.ArcWaveGeometry.FaceHit;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;

public class ArcWaveBulletType extends BulletType {
    public float arcAngle = 60f, hitWidth = 5f, stroke = 2.5f;
    public float drawStartRadius = 0f, drawFadeLength = 6f;
    public Color waveColor = Color.valueOf("7ad4ff"), innerColor = Color.white;
    public float fadeFraction = 0.35f;
    public Interp fadeInterp = Interp.pow2Out;
    public float glowStrokeScl = 3f, glowAlpha = 0.4f;

    public boolean reflect = true;
    public int maxReflections = 2;
    public float reflectDamageScale = 0.7f;

    public boolean reflectedDamageAll = false;
    public Team reflectedAllTeam = Team.get(255);
    public boolean immuneShooter = false, immuneShooterTeam = false, immuneReflectorTeam = true;

    public boolean applyStatusThroughUnitShield = true;
    public boolean shieldAbsorb = true;

    private final BulletType probeType;
    public float probeSpacing = 6f, probeLeadScl = 1.5f;
    public float contactEffectSpacing = 10f;

    private static final float angleEpsilon = 0.0001f;

    public ArcWaveBulletType(float speed, float damage) {
        super(speed, damage);
        collides = collidesTiles = hittable = absorbable = reflectable = keepVelocity = false;
        pierce = true;
        pierceBuilding = false;

        lifetime = 40f;

        setDefaults = false;
        despawnHit = false;
        fragOnHit = false;
        fragOnDespawn = false;

        hitEffect = Fx.hitLaserBlast;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        smokeEffect = Fx.none;

        probeType = createProbeType();
    }

    @Override
    public void init() {
        super.init();
        drawSize = Math.max(drawSize, range * 3f);
        probeType.shieldDamageMultiplier = shieldDamageMultiplier;
        if (homingPower > 0f || weaveMag != 0f || drag != 0f || accel != 0f || rotateSpeed != 0f) {
            throw new IllegalArgumentException("ArcWave: expansion logic is only compatible with straight radial movement.");
        }
    }

    public static class Segment {
        public float start, length, pierceUsed;
        public final IntSet piercedBuilds = new IntSet();

        public Segment(float s, float l, float p, IntSet prevPierced) {
            start = Mathf.mod(s, 360f); length = l; pierceUsed = p;
            if (prevPierced != null) piercedBuilds.addAll(prevPierced);
        }

        public Segment copy(float s, float l) { return new Segment(s, l, pierceUsed, piercedBuilds); }
    }

    public static class ArcWaveData {
        public final float originX, originY;
        public final Seq<Segment> segments = new Seq<>();
        public final Seq<Cut> absorbedCuts = new Seq<>();
        public final IntSet hitUnits = new IntSet(), hitBuilds = new IntSet();
        public int pendingProbes, generation;
        public float baseArc = 1f, lastRadius = -1f, damageScale = 1f;
        public Object rootOwner;
        public Team rootTeam, reflectorTeam;

        public ArcWaveData(float x, float y) { originX = x; originY = y; }
    }

    public record ProbeData(ArcWaveData wave, float start, float length, long createdUpdate) {}

    @Override
    public void init(Bullet b) {
        if (b.data instanceof ArcWaveData) return;
        super.init(b);
        ArcWaveData d = new ArcWaveData(b.x, b.y);
        d.rootOwner = b.shooter != null ? b.shooter : b.owner;
        d.rootTeam = b.team;
        d.baseArc = arcAngle;
        d.segments.add(new Segment(b.rotation() - arcAngle / 2f, arcAngle, 0f, null));
        b.data = d;
    }

    @Override
    public void update(Bullet b){
        updateTrail(b);
        updateTrailEffects(b);
        updateBulletInterval(b);

        if(!(b.data instanceof ArcWaveData d) || d.segments.isEmpty()){
            b.remove();
            return;
        }

        float radius = Math.max(b.dst(d.originX, d.originY), 0f);
        float previous = Math.max(d.lastRadius, 0f);

        applyShieldCuts(d);

        if(collidesGround && !d.segments.isEmpty() && radius > previous){
            scanAndReflect(b, d, previous, radius);
        }

        if(!b.isAdded()) return;

        if(d.segments.isEmpty()){
            b.remove();
            return;
        }

        damageUnits(b, d, radius);
        if(!b.isAdded()) return;

        if(shieldAbsorb && d.pendingProbes <= 0){
            spawnProbes(b, d, radius);
        }

        d.lastRadius = radius;
    }

    protected void scanAndReflect(
            Bullet bullet, ArcWaveData data, float from, float to
    ){
        ArcWaveGeometry.Scene scene = ArcWaveGeometry.capture(
                data.originX, data.originY, to,
                build -> canCollideBuilding(bullet, data, build)
        );

        Seq<Segment> pending = new Seq<>();
        pending.addAll(data.segments);

        Seq<Segment> result = new Seq<>();

        boolean allowReflection = reflect
                && data.generation < maxReflections
                && !reflectionDisabledByImmunity();

        LongMap<Seq<Cut>> reflections =
                allowReflection ? new LongMap<>() : null;

        boolean anyHit = false;

        for(int task = 0; task < pending.size; task++){
            Segment segment = pending.get(task);
            Seq<FaceHit> hits = new Seq<>();

            ArcWaveGeometry.collectFaceHits(
                    scene,
                    data.originX, data.originY, from, to,
                    segment.start, segment.length,
                    segment.piercedBuilds,
                    hits
            );

            if(hits.isEmpty()){
                result.add(segment);
                continue;
            }

            Seq<Cut> occupied = new Seq<>();
            for(FaceHit hit : hits){
                occupied.add(new Cut(hit.start, hit.end));
            }

            //Untouched intervals keep their original piercing history.
            appendRemainder(segment, occupied, result);

            for(FaceHit hit : hits){
                if(!bullet.isAdded()){
                    data.segments.clear();
                    return;
                }

                Segment touched = segment.copy(hit.start, hit.length());

                if(data.hitBuilds.add(hit.build.id)){
                    double angle = ((double)hit.start + hit.end) * 0.5d;
                    double distance = ArcWaveGeometry.distanceToPlane(
                            data.originX, data.originY,
                            angle, hit.axis, hit.plane
                    );

                    float x = (float)(data.originX
                            + Math.cos(Math.toRadians(angle)) * distance);
                    float y = (float)(data.originY
                            + Math.sin(Math.toRadians(angle)) * distance);

                    hitBuilding(bullet, hit.build, x, y);
                    anyHit = true;

                    if(!bullet.isAdded()){
                        data.segments.clear();
                        return;
                    }
                }

                spawnContactEffects(data, hit);

                boolean passed = pierceBuilding
                        && (pierceCap < 0 || touched.pierceUsed < pierceCap);

                if(passed){
                    touched.pierceUsed++;
                    touched.piercedBuilds.add(hit.build.id);

                    //Re-evaluate only this interval with the pierced building removed.
                    pending.add(touched);
                }else if(reflections != null){
                    long key = hit.key();
                    Seq<Cut> cuts = reflections.get(key);

                    if(cuts == null){
                        cuts = new Seq<>();
                        reflections.put(key, cuts);
                    }

                    cuts.add(new Cut(hit.start, hit.end));
                }
            }
        }

        data.segments.set(result);

        if(anyHit){
            hitSound.at(
                    bullet.x, bullet.y,
                    hitSoundPitch + Mathf.range(hitSoundPitchRange),
                    hitSoundVolume
            );
        }

        if(reflections == null) return;

        for(var entry : reflections.entries()){
            Seq<Cut> cuts = entry.value;
            cuts.sort((a, b) -> Float.compare(a.start, b.start));

            float start = cuts.first().start;
            float end = cuts.first().end;

            for(int i = 1; i < cuts.size; i++){
                Cut next = cuts.get(i);

                if(next.start <= end + angleEpsilon){
                    end = Math.max(end, next.end);
                }else{
                    spawnReflection(
                            bullet, data, to, entry.key,
                            new Cut(start, end)
                    );

                    start = next.start;
                    end = next.end;
                }
            }

            spawnReflection(
                    bullet, data, to, entry.key,
                    new Cut(start, end)
            );
        }
    }


    protected void applyShieldCuts(ArcWaveData data){
        if(data.absorbedCuts.isEmpty()) return;

        Seq<Segment> remaining = new Seq<>();

        for(Segment segment : data.segments){
            appendRemainder(segment, data.absorbedCuts, remaining);
        }

        data.segments.set(remaining);
        data.absorbedCuts.clear();
    }

    protected void appendRemainder(
            Segment source, Seq<Cut> cuts, Seq<Segment> out
    ){
        Seq<Cut> intervals = new Seq<>();

        ArcWaveGeometry.subtractCuts(
                source.start, source.length, cuts, intervals
        );

        for(Cut interval : intervals){
            out.add(source.copy(interval.start, interval.length()));
        }
    }

    protected boolean canCollideBuilding(
            Bullet bullet, ArcWaveData data, Building build
    ){
        if(build == null || build.dead()) return false;
        if(!canDamage(bullet, data, build.team, build)) return false;
        if(!build.collide(bullet) || !testCollision(bullet, build)) return false;

        if(!build.block.underBullets || hitUnder) return true;
        if(bullet.aimTile != null && bullet.aimTile.build == build) return true;

        return bullet.aimX >= 0f && bullet.aimY >= 0f
                && Vars.world.buildWorld(bullet.aimX, bullet.aimY) == build;
    }

    protected void hitBuilding(Bullet bullet, Building build, float x, float y){
        float oldX = bullet.x, oldY = bullet.y;
        float oldVx = bullet.vel.x, oldVy = bullet.vel.y;
        float oldTime = bullet.time;

        Team oldTeam = bullet.team;
        Entityc oldOwner = bullet.owner;
        Entityc oldShooter = bullet.shooter;

        float initialHealth = build.health;

        try {
            bullet.set(x, y);
            build.collision(bullet);
        } finally {
            bullet.set(oldX, oldY);
            bullet.vel.set(oldVx, oldVy);
            bullet.time = oldTime;

            bullet.team = oldTeam;
            bullet.owner = oldOwner;
            bullet.shooter = oldShooter;
        }

        if (bullet.isAdded()) {
            handlePierce(bullet, initialHealth, x, y);
        }
    }

    protected void spawnReflection(Bullet parent, ArcWaveData data, float radius, long key, Cut cut) {
        if(!parent.isAdded()
                || parent.time >= parent.lifetime
                || cut.end - cut.start <= angleEpsilon) return;

        float plane = Float.intBitsToFloat((int)key);
        int axis = (int)((key >>> 32) & 1L);
        Team reflectorTeam = Team.get((int)((key >>> 34) & 0xffL));

        float ox = axis == 0 ? 2f * plane - data.originX : data.originX;
        float oy = axis == 1 ? 2f * plane - data.originY : data.originY;

        float start = axis == 0 ? 180f - cut.end : -cut.end;
        float end = axis == 0 ? 180f - cut.start : -cut.start;
        float angle = Mathf.mod((start + end) * 0.5f, 360f);

        ArcWaveData reflected = new ArcWaveData(ox, oy);
        reflected.generation = data.generation + 1;
        reflected.damageScale = data.damageScale * reflectDamageScale;
        reflected.rootOwner = data.rootOwner;
        reflected.rootTeam = data.rootTeam;
        reflected.reflectorTeam = reflectorTeam;
        reflected.baseArc = data.baseArc;
        reflected.lastRadius = radius;

        reflected.segments.add(new Segment(start, end - start, 0f, null));

        Team team = reflectedDamageAll
                ? reflectedAllTeam
                : immuneReflectorTeam ? reflectorTeam : parent.team;

        float x = ox + Angles.trnsx(angle, radius);
        float y = oy + Angles.trnsy(angle, radius);

        Bullet child = Bullet.create();
        child.type = this;
        child.owner = parent.owner;
        child.shooter = parent.shooter;
        child.team = team;

        child.set(x, y);
        child.lastX = x;
        child.lastY = y;

        //Keep the shot origin used by vanilla targeting helpers.
        //The virtual wave origin is stored separately in ArcWaveData.
        child.originX = parent.originX;
        child.originY = parent.originY;

        child.initVel(angle, parent.vel.len());
        child.time = parent.time;
        child.lifetime = parent.lifetime;

        child.damage = parent.damage * reflectDamageScale;
        child.buildingDamageMultiplier = parent.buildingDamageMultiplier;
        child.hitSize = parent.hitSize;
        child.data = reflected;

        child.aimX = parent.aimX;
        child.aimY = parent.aimY;
        child.aimTile = parent.aimTile;
        child.mover = null;

        if(child.trail != null){
            child.trail.clear();
        }

        child.add();
    }

    protected boolean reflectionDisabledByImmunity() {
        return reflectedDamageAll && immuneShooter && immuneShooterTeam && immuneReflectorTeam;
    }

    protected void damageUnits(Bullet b, ArcWaveData d, float radius){
        float margin = hitWidth + 12f;

        Cons<Unit> receiver = unit -> {
            if(!b.isAdded() || unit.dead || d.hitUnits.contains(unit.id)) return;
            if(!unit.type.hittable) return;
            if(!unit.checkTarget(collidesAir, collidesGround)) return;
            if(!canDamage(b, d, unit.team, unit)) return;

            float distance = unit.dst(d.originX, d.originY);
            if(Math.abs(distance - radius) > hitWidth + unit.hitSize / 2f) return;

            float angle = Angles.angle(d.originX, d.originY, unit.x, unit.y);
            boolean inside = false;

            for(Segment segment : d.segments){
                if(Angles.within(
                        angle,
                        Mathf.mod(segment.start + segment.length * 0.5f, 360f),
                        segment.length * 0.5f
                )){
                    inside = true;
                    break;
                }
            }

            if(!inside) return;

            d.hitUnits.add(unit.id);
            hitUnit(b, unit);

            if(hitEffect != Fx.none){
                hitEffect.at(unit.x, unit.y, angle, hitColor);
            }
        };

        Groups.unit.intersect(
                d.originX - radius - margin,
                d.originY - radius - margin,
                (radius + margin) * 2f,
                (radius + margin) * 2f,
                receiver
        );
    }

    protected boolean canDamage(Bullet b, ArcWaveData d, Team targetTeam, Object target) {
        if (d.generation > 0 && reflectedDamageAll) {
            if (immuneShooterTeam && targetTeam == d.rootTeam) return false;
            if (immuneReflectorTeam && targetTeam == d.reflectorTeam) return false;
            return !immuneShooter || target != d.rootOwner;
        }
        return targetTeam != b.team && (!immuneShooter || d.generation <= 0 || target != d.rootOwner);
    }

    protected void hitUnit(Bullet b, Unit unit){
        boolean wasDead = unit.dead;
        boolean allowStatus = applyStatusThroughUnitShield || unit.shield <= 0f;

        float previousHealth = unit.health;
        float previousShield = Math.max(unit.shield, 0f);
        float amount = b.damage;
        float pierceHealth;

        if(maxDamageFraction > 0f){
            float cap = unit.maxHealth * maxDamageFraction + previousShield;
            amount = Math.min(amount, cap);
            pierceHealth = Math.min(previousHealth, cap);
        }else{
            pierceHealth = previousHealth + previousShield;
        }

        if(lifesteal > 0f && b.owner instanceof Healthc owner){
            owner.heal(Math.max(Math.min(previousHealth, amount), 0f) * lifesteal);
        }

        if(pierceArmor){
            unit.damagePierce(amount);
        }else if(armorMultiplier != 1f){
            unit.damageArmorMult(amount, armorMultiplier);
        }else{
            unit.damage(amount);
        }

        Tmp.v3.set(unit).sub(b).nor().scl(knockback * 80f);
        if(impact){
            Tmp.v3.setAngle(b.rotation() + (knockback < 0f ? 180f : 0f));
        }
        unit.impulse(Tmp.v3);

        if(allowStatus){
            unit.apply(status, statusDuration);
        }

        Events.fire(new EventType.UnitDamageEvent().set(unit, b));

        if(!wasDead && unit.dead){
            Events.fire(new EventType.UnitBulletDestroyEvent(unit, b));
        }

        handlePierce(b, pierceHealth, unit.x, unit.y);
    }

    protected BulletType createProbeType() {
        return new BulletType(0f, 0f){
            {
                collides = false;
                collidesTiles = false;
                hittable = false;
                absorbable = true;
                reflectable = false;
                keepVelocity = false;

                lifetime = 1f;
                hitSize = 1f;
                lightRadius = 0f;
                lightOpacity = 0f;

                despawnHit = false;
                setDefaults = false;
                fragOnHit = false;
                fragOnDespawn = false;

                hitEffect = Fx.none;
                despawnEffect = Fx.none;
                shootEffect = Fx.none;
                smokeEffect = Fx.none;
            }

            @Override
            public void update(Bullet b){
                if(b.data instanceof ProbeData data
                        && data.createdUpdate() == Vars.state.updateId){
                    //Allow shields to process this probe on the next world update.
                    b.keepAlive = true;
                }
            }

            @Override
            public void draw(Bullet b){}

            @Override
            public void drawLight(Bullet b){}

            @Override
            public void despawned(Bullet b){}

            @Override
            public void removed(Bullet b){
                if(!(b.data instanceof ProbeData data)) return;

                ArcWaveData wave = data.wave();
                wave.pendingProbes = Math.max(0, wave.pendingProbes - 1);

                if(b.absorbed){
                    wave.absorbedCuts.add(
                            new Cut(data.start(), data.start() + data.length())
                    );
                }
            }
        };
    }

    protected void spawnProbes(Bullet b, ArcWaveData d, float radius){
        if(d.pendingProbes > 0 || probeSpacing <= 0f) return;

        float lead = radius
                + Math.max(b.vel.len() * Time.delta * probeLeadScl, 1f);

        for(Segment segment : d.segments){
            float arcLength = lead * segment.length * Mathf.degRad;
            int count = Math.max(1, Mathf.ceil(arcLength / probeSpacing));
            float length = segment.length / count;

            for(int i = 0; i < count; i++){
                float start = segment.start + length * i;
                float angle = start + length * 0.5f;

                ProbeData data = new ProbeData(
                        d, start, length, Vars.state.updateId
                );

                Bullet probe = probeType.create(
                        b.owner, b.shooter, b.team,
                        d.originX + Angles.trnsx(angle, lead),
                        d.originY + Angles.trnsy(angle, lead),
                        angle,
                        0f, 1f, 1f,
                        data, null, -1f, -1f
                );

                if(probe != null){
                    probe.damage = b.damage
                            * length / Math.max(d.baseArc, angleEpsilon);

                    d.pendingProbes++;
                }
            }
        }
    }

    protected void spawnContactEffects(ArcWaveData data, FaceHit hit){
        if(hitEffect == Fx.none || contactEffectSpacing <= 0f) return;

        double r0 = ArcWaveGeometry.distanceToPlane(data.originX, data.originY, hit.start, hit.axis, hit.plane);
        double r1 = ArcWaveGeometry.distanceToPlane(data.originX, data.originY, hit.end, hit.axis, hit.plane);

        double a0 = Math.toRadians(hit.start);
        double a1 = Math.toRadians(hit.end);

        double v0 = hit.axis == 0 ? data.originY + Math.sin(a0) * r0 : data.originX + Math.cos(a0) * r0;
        double v1 = hit.axis == 0 ? data.originY + Math.sin(a1) * r1 : data.originX + Math.cos(a1) * r1;

        if(!Double.isFinite(v0) || !Double.isFinite(v1)) return;

        float length = (float)Math.abs(v1 - v0);
        float spacing = Math.max(contactEffectSpacing, 2f);
        float ratio = length / spacing;
        int count = Mathf.floor(ratio);
        if(Mathf.chance(ratio - count)) count++;

        for (int i = 0; i < count; i++) {
            float f = (i + 0.5f) / count;
            float variable = (float)(v0 + (v1 - v0) * f);

            float x = hit.axis == 0 ? hit.plane : variable;
            float y = hit.axis == 1 ? hit.plane : variable;

            hitEffect.at(x, y, Angles.angle(data.originX, data.originY, x, y), hitColor);
        }
    }

    @Override
    public void draw(Bullet b) {
        if (!(b.data instanceof ArcWaveData d) || d.segments.isEmpty()) return;

        float r = Math.max(b.dst(d.originX, d.originY), 0.001f);
        float alpha = Mathf.curve(r, drawStartRadius, drawStartRadius + drawFadeLength) * (fadeFraction <= 0f ? 1f : 1f - fadeInterp.apply(Mathf.curve(b.fin(), 1f - fadeFraction, 1f))) * d.damageScale;
        if (alpha <= 0.001f) return;
        float z = Draw.z();
        Draw.z(layer);
        for (Segment s : d.segments) {
            int sides = Math.max(2, Mathf.ceil(Math.max(s.length / 4f, r * s.length * Mathf.degRad / 4f)));
            if (glowStrokeScl > 0) {
                Draw.color(waveColor, glowAlpha * alpha);
                Lines.stroke(stroke * glowStrokeScl);
                Lines.poly(d.originX, d.originY, sides, r, s.start, s.start + s.length);
            }
            Draw.color(waveColor, alpha);
            Lines.stroke(stroke);
            Lines.poly(d.originX, d.originY, sides, r, s.start, s.start + s.length);
            Draw.color(innerColor, 0.6f * alpha);
            Lines.stroke(stroke * 0.4f); Lines.poly(d.originX, d.originY, sides, r, s.start, s.start + s.length);
        }

        Draw.reset();
        Draw.z(z);
    }

    @Override
    public void despawned(Bullet b) {}

    @Override
    public void removed(Bullet b) {}
}