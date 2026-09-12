package ectotech.world.blocks.defense;

import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.world.blocks.defense.utils.CasedWallFactory;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.Stats;

/**
 * Корпус — блок, который игрок выбирает в меню, но в мир попадает только
 * как часть CasedWall. Сам WallCasingBuild существует лишь как заглушка
 * при прямой установке через редактор.
 */
public class WallCasing extends Wall {
    /** Доля урона, которую корпус берёт на себя; при снятии корпуса стена теряет (1 - это) от урона. 0..1 */
    public float damageAbsorption = 0f;

    public WallCasing(String name) {
        super(name);
        scaledHealth = 10f;
        CasedWallFactory.register(this);

        buildType = WallCasingBuild::new;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile.build == null || tile.team() != team) return false;

        if (tile.build instanceof ConstructBuild cb) return cb.current == this;

        Block target = tile.build.block;
        if (target instanceof CasedWall cased) {
            if (cased.casing == this) return false;
            target = cased.wall;
        }

        return target.size == size && CasedWallFactory.get(target, this) != null;
    }

    @Override
    public boolean canReplace(Block other) {
        if (other instanceof CasedWall cased) return cased.casing != this && other.size == size;
        return super.canReplace(other);
    }

    /** Нужен ли per-tick updateTile у корпусированной стены. */
    public boolean casingTileUpdate(){ return false; }

    /** Вызывается каждый тик после updateTile() стены. */
    public void updateTilec(CasedWall.CasedWallBuild build){}

    /** Нужна ли динамическая (не кэшированная) отрисовка поверх стены. */
    public boolean drawUpdate() { return false; }

    /** Вызывается из draw() после отрисовки стены. */
    public void drawc(CasedWall.CasedWallBuild build) {}

    /**
     * Вызывается после collision() стены. @param collided — результат стены
     * (false = пуля уже отражена). Вернуть false, чтобы отменить столкновение.
     */
    public boolean collisionc(CasedWall.CasedWallBuild build, Bullet bullet, boolean collided){ return collided; }

    /** Бары корпуса. Использовать cased.addBar(...). */
    public void setBarsc(CasedWall cased){}

    /** Статы корпуса в общей таблице пары. */
    public void setStatsc(CasedWall cased, Stats stats) {}

    /** Per-постройка состояние корпуса (таймеры и т.п.). null, если не нужно. */
    public Object createDatac(CasedWall.CasedWallBuild build){ return null; }

    public void writec(CasedWall.CasedWallBuild build, Writes write) {}
    public void readc(CasedWall.CasedWallBuild build, Reads read, byte revision) {}

    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        super.placeEnded(tile, builder, rotation, config);
        CasedWallFactory.complete(tile, this, rotation);
    }

    public class WallCasingBuild extends WallBuild {}
}