package ectotech.world.blocks.defense.turrets;

import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.meta.Stat;

import static mindustry.Vars.iconSmall;

public class AmmoTargetItemTurret extends ItemTurret {

    public AmmoTargetItemTurret(String name) {
        super(name);
        buildType = AmmoTargetItemTurretBuild::new;
    }

    @Override
    public void init() {
        targetAir = false;
        targetGround = false;
        ammoTypes.each((item, bullet) -> {
            targetAir |= bullet.collidesAir;
            targetGround |= bullet.collidesGround;
        });

        super.init();
    }

    @Override
    public void setStats() {
        super.setStats();

        // Убираем блочные "да/нет" — они вводят в заблуждение.
        stats.remove(Stat.targetsAir);
        stats.remove(Stat.targetsGround);

        // Показываем, КАКИЕ патроны бьют воздух и КАКИЕ — землю.
        stats.add(Stat.targetsAir, table -> table.table(t -> {
            t.left();
            for (Item item : ammoTypes.keys()) {
                if (ammoTypes.get(item).collidesAir) {
                    t.image(item.uiIcon).size(iconSmall).padRight(4f);
                }
            }
        }).left());

        stats.add(Stat.targetsGround, table -> table.table(t -> {
            t.left();
            for (Item item : ammoTypes.keys()) {
                if (ammoTypes.get(item).collidesGround) {
                    t.image(item.uiIcon).size(iconSmall).padRight(4f);
                }
            }
        }).left());
    }

    public class AmmoTargetItemTurretBuild extends ItemTurretBuild {

        @Override
        protected void findTarget() {
            BulletType b = peekAmmo();

            if (b == null) {
                target = null;
                return;
            }

            AmmoTargetItemTurret t = (AmmoTargetItemTurret) block;
            boolean prevAir = t.targetAir, prevGround = t.targetGround;

            t.targetAir = b.collidesAir;
            t.targetGround = b.collidesGround;

            super.findTarget();

            t.targetAir = prevAir;
            t.targetGround = prevGround;
        }

        @Override
        protected boolean validateTarget() {
            BulletType b = peekAmmo();

            if (b != null && target != null && !isControlled() && !logicControlled()) {
                if (target instanceof Unit u && (!u.isGrounded() ? !b.collidesAir : !b.collidesGround)) return false;
                if (target instanceof Building && !b.collidesGround) return false;
            }

            return super.validateTarget();
        }
    }
}
