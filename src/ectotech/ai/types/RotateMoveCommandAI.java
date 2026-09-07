package ectotech.ai.types;

import arc.math.Angles;
import mindustry.ai.types.CommandAI;
import mindustry.entities.Predict;
import mindustry.gen.Teamc;

public class RotateMoveCommandAI extends CommandAI {
    public float rotateManeuverSpeedScl = 0.35f;
    public float aimTolerance = 3f;

    private boolean maneuver;

    @Override
    public void faceTarget() {
        if (!unit.isFlying() || unit.type.omniMovement) {
            super.faceTarget();
            return;
        }

        if (invalid(target) || !unit.hasWeapons() || !unit.type.faceTarget || !shouldFire()) {
            maneuver = false;
            return;
        }

        float angle = unit.angleTo(Predict.intercept(unit, target, unit.type.weapons.first().bullet));

        maneuver = !Angles.within(unit.rotation, angle, aimTolerance) || !unit.within(target, unit.range() * 0.95f);

        if (maneuver) {
            unit.movePref(vec.trns(angle, unit.speed() * rotateManeuverSpeedScl));
        }

        if (maneuver || unit.vel.len2() > unit.speed()) {
            unit.controlWeapons(false);
        }
    }

    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground) {
        if (maneuver && targetPos == null && attackTarget == null && !invalid(target) && unit.within(target, range * 1.5f)) {
            return target;
        }

        return super.findMainTarget(x, y, range, air, ground);
    }
}