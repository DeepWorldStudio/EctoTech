package ectotech.type.weapons;

import arc.Events;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.part.DrawPart;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Weapon;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;

public class CoreProximityWeapon extends Weapon {

    protected static final ObjectMap<CoreBuild, PriorityCoreEffectState> coreEffects = new ObjectMap<>();
    protected static final Seq<CoreBuild> removeQueue = new Seq<>();
    private static boolean registered = false;


    /** If true, weapon deactivates in core zone */
    public boolean invert = false;
    /** Speed of weapon activation / deactivation */
    public float zoneWarmupSpeed = 0.08f;
    /** The color applies on the sprite of the weapon in inactive phase*/
    public Color inactiveColor = Color.valueOf("6B615D");
    /** Max inactiveColor alpha */
    public float inactiveColorAlpha = 0.6f;
    /** backwards weapon offset in inactive phase */
    public float inactiveWeaponOffset = 0.08f;
    /** Main core effect color */
    public @Nullable Color coreEffectColor = null;

    public CoreProximityWeapon() {
        this("");
    }

    public CoreProximityWeapon(String name) {
        super(name);
        mountType = CoreProximityWeaponMount::new;

        if (!registered) {
            registered = true;
            Events.run(EventType.Trigger.update, CoreProximityWeapon::updatePriorityCoreEffects);
            Events.run(EventType.Trigger.draw, CoreProximityWeapon::drawPriorityCoreEffects);
            Events.on(EventType.ResetEvent.class, e -> coreEffects.clear());
        }
    }

    /**Returns closest allied core, ignore build radius*/
    protected @Nullable CoreBuild priorityCore(Unit unit) {
        if (!Vars.state.rules.polygonCoreProtection) return Vars.state.teams.closestCore(unit.x, unit.y, unit.team);

        CoreBuild closest = null;
        float closestDst = Float.MAX_VALUE;

        for (var data : Vars.state.teams.active) {
            if (!data.team.rules().protectCores) continue;

            for (CoreBuild core : data.cores) {

                float dst = core.dst2(unit.x, unit.y);

                if (dst < closestDst) {
                    closest = core;
                    closestDst = dst;
                }
            }
        }

        return (closest != null && closest.team == unit.team) ? closest : null;
    }

    protected boolean insideCoreZone(Unit unit, @Nullable CoreBuild core) {
        if (core == null) return false;

        if (Vars.state.rules.polygonCoreProtection) return true;

        float radius = Vars.state.rules.buildRadius(core.team);

        return radius > 0f && unit.within(core, radius);
    }

    @Override
    public void update(Unit unit, WeaponMount wMount) {
        CoreProximityWeaponMount mount = (CoreProximityWeaponMount) wMount;

        CoreBuild core = priorityCore(unit);
        boolean inside = insideCoreZone(unit, core);

        mount.keyCore = inside ? core : null;
        mount.zoneAllowed = invert != inside;
        mount.zoneWarmup = Mathf.lerpDelta(mount.zoneWarmup, mount.zoneAllowed ? 0f : 1f, zoneWarmupSpeed);

        if (core != null && inside && !headless) applyPriorityCoreEffect(core, unit);

        if (!mount.zoneAllowed) {
            mount.shoot = false;
            mount.target = null;

            if (mount.bullet != null) {
                mount.bullet.time = mount.bullet.lifetime;
            }
        }

        super.update(unit, mount);
    }

    @Override
    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
        boolean inside = insideCoreZone(unit, priorityCore(unit));
        if (invert == inside) return null;

        return super.findTarget(unit, x, y, range, air, ground);
    }

    @Override
    protected boolean checkTarget(Unit unit, Teamc target, float x, float y, float range) {
        boolean inside = insideCoreZone(unit, priorityCore(unit));
        if (invert == inside) return true;

        return super.checkTarget(unit, target, x, y, range);
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        if (!((CoreProximityWeaponMount) mount).zoneAllowed) return;

        super.shoot(unit, mount, shootX, shootY, rotation);
    }

    protected void applyPriorityCoreEffect(CoreBuild core, Unit unit) {
        PriorityCoreEffectState state = coreEffects.get(core, PriorityCoreEffectState::new);

        state.active = true;
        state.speed = zoneWarmupSpeed;

        state.color.set(invert ? inactiveColor : (coreEffectColor != null ? coreEffectColor : unit.team.color));
    }

    protected static void updatePriorityCoreEffects() {
        if (!Vars.state.isGame() || Vars.state.isPaused() || Vars.state.isEditor() || coreEffects.isEmpty()) return;

        removeQueue.clear();

        for (var entry : coreEffects.entries()) {
            CoreBuild core = entry.key;
            PriorityCoreEffectState state = entry.value;

            if (core == null || !core.isValid()) {
                removeQueue.add(core);
                continue;
            }

            boolean wasActive = state.active;

            state.warmup = Mathf.lerpDelta(state.warmup, wasActive ? 1f : 0f, state.speed);
            state.active = false;

            if (!wasActive && state.warmup <= 0.001f) {
                removeQueue.add(core);
            }
        }

        for (CoreBuild core : removeQueue) {
            coreEffects.remove(core);
        }
    }


    protected static void drawPriorityCoreEffects() {
        if (headless || !Vars.state.isGame() || coreEffects.isEmpty()) return;

        for (var entry : coreEffects.entries()) {
            CoreBuild core = entry.key;
            PriorityCoreEffectState state = entry.value;

            if (core == null || !core.isValid() || state.warmup <= 0.001f) continue;

            drawPriorityCoreEffect(core, state);
        }
    }

    /**One core effect draw method*/
    protected static void drawPriorityCoreEffect(CoreBuild core, PriorityCoreEffectState state) {
        float warmup = state.warmup;
        float baseRadius = core.block.size * tilesize / 2f;
        float ringRadius = baseRadius + 3f + Mathf.absin(Time.time + core.id, 8f, 1.5f);

        Draw.z(Layer.effect);
        Draw.color(state.color, warmup * 0.8f);

        // Central field
        Fill.circle(core.x, core.y, baseRadius * 0.2f * warmup + 1f);

        // Main ring
        Lines.stroke(1.5f * warmup);
        Lines.circle(core.x, core.y, ringRadius);

        // Rotating chevrons
        for (int i = 0; i < 4; i++) {
            float angle = i * 90f + Time.time * 0.6f + core.id * 7f;

            Drawf.tri(
                    core.x + Angles.trnsx(angle, ringRadius),
                    core.y + Angles.trnsy(angle, ringRadius),
                    4f * warmup,
                    6f * warmup,
                    angle
            );
        }

        Drawf.light(core.x, core.y, ringRadius * 1.5f, state.color, warmup * 0.45f);

        Draw.reset();
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        CoreProximityWeaponMount m = (CoreProximityWeaponMount)mount;

        //сила тонировки и сдвига в этом кадре
        float tint = m.zoneWarmup * inactiveColorAlpha;
        float zoneOffset = m.zoneWarmup * inactiveWeaponOffset;

        float z = Draw.z();
        Draw.z(z + layerOffset);

        float
                rotation = unit.rotation - 90,
                realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil + zoneOffset,
                weaponRotation = rotation + (rotate ? mount.rotation : baseRotation),
                wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
                wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

        if (shadow > 0) {
            Drawf.shadow(wx, wy, shadow);
        }

        if (top) {
            drawOutline(unit, mount);
        }

        if (parts.size > 0) {
            DrawPart.params.set(mount.warmup, mount.reload / reload, mount.smoothReload, mount.heat, mount.recoil, mount.charge, wx, wy, weaponRotation + 90);
            DrawPart.params.sideMultiplier = flipSprite ? -1 : 1;

            for (int i = 0; i < parts.size; i++) {
                var part = parts.get(i);
                DrawPart.params.setRecoil(part.recoilIndex >= 0 && mount.recoils != null ? mount.recoils[part.recoilIndex] : mount.recoil);
                if(part.under){
                    unit.type.applyColor(unit);
                    if(tint > 0.001f) Draw.mixcol(inactiveColor, tint);
                    part.draw(DrawPart.params);
                }
            }
        }

        float prevXscl = Draw.xscl;
        Draw.xscl *= -Mathf.sign(flipSprite);

        unit.type.applyColor(unit);
        if (tint > 0.001f) Draw.mixcol(inactiveColor, tint);

        if (region.found()) Draw.rect(region, wx, wy, weaponRotation);

        if (cellRegion.found()) {
            Draw.color(unit.type.cellColor(unit));
            Draw.rect(cellRegion, wx, wy, weaponRotation);
            Draw.color();
        }

        Draw.mixcol();

        if (heatRegion.found() && mount.heat > 0) {
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, wx, wy, weaponRotation);
            Draw.blend();
            Draw.color();
        }

        Draw.xscl = prevXscl;

        if (parts.size > 0) {
            for (int i = 0; i < parts.size; i++) {
                var part = parts.get(i);
                DrawPart.params.setRecoil(part.recoilIndex >= 0 && mount.recoils != null ? mount.recoils[part.recoilIndex] : mount.recoil);
                if (!part.under) {
                    unit.type.applyColor(unit);
                    if (tint > 0.001f) Draw.mixcol(inactiveColor, tint);
                    part.draw(DrawPart.params);
                }
            }
        }

        Draw.xscl = 1f;
        Draw.mixcol();
        Draw.z(z);
    }

    @Override
    public void drawOutline(Unit unit, WeaponMount mount){
        if(!outlineRegion.found()) return;

        CoreProximityWeaponMount m = (CoreProximityWeaponMount)mount;

        float
                rotation = unit.rotation - 90,
                realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil + m.zoneWarmup * inactiveWeaponOffset,
                weaponRotation = rotation + (rotate ? mount.rotation : baseRotation),
                wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
                wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

        Draw.xscl = -Mathf.sign(flipSprite);
        if(m.zoneWarmup > 0.001f) Draw.mixcol(inactiveColor, m.zoneWarmup * inactiveColorAlpha);
        Draw.rect(outlineRegion, wx, wy, weaponRotation);
        Draw.mixcol();
        Draw.xscl = 1f;
    }

    public static class CoreProximityWeaponMount extends WeaponMount {
        public float zoneWarmup = 0f;
        public boolean zoneAllowed = true;
        public @Nullable CoreBuild keyCore = null;

        public CoreProximityWeaponMount(Weapon weapon){
            super(weapon);
        }
    }

    protected static class PriorityCoreEffectState {
        float warmup = 0f;
        float speed = 0.08f;
        boolean active = false;
        final Color color = new Color(Color.white);
    }
}
