package ectotech.world.blocks.defense;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Bullet;
import mindustry.type.ItemSeq;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BuildVisibility;

/**Wall class synthesizes wall and wall casing*/
public class CasedWall extends Wall {
    public final Wall wall;
    public final WallCasing casing;

    public CasedWall(Wall wall, WallCasing casing) {
        super(casing.name + "-" + wall.name);
        this.wall = wall;
        this.casing = casing;

        size = wall.size;
        group = BlockGroup.none;

        ItemSeq total = new ItemSeq();
        total.add(wall.requirements);
        total.add(casing.requirements);
        requirements(casing.category, BuildVisibility.hidden, total.toArray());

        inEditor = true;
        alwaysUnlocked = true;
        rebuildable = true;
        replaceable = false;

        buildType = CasedWallBuild::new;
    }

    @Override
    public void init() {
        health = wall.health + casing.health;
        armor = wall.armor + casing.armor;
        insulated = wall.insulated || casing.insulated;

        absorbLasers = wall.absorbLasers || casing.absorbLasers;
        chanceDeflect = Math.max(wall.chanceDeflect, casing.chanceDeflect);

        lightningChance = Math.max(wall.lightningChance, casing.lightningChance);
        lightningDamage = wall.lightningChance >= casing.lightningChance ? wall.lightningDamage : casing.lightningDamage;
        lightningLength = wall.lightningChance >= casing.lightningChance ? wall.lightningLength : casing.lightningLength;

        flashHit = wall.flashHit || casing.flashHit;

        crushDamageMultiplier = wall.crushDamageMultiplier;
        floating = wall.floating;
        placeableLiquid = wall.placeableLiquid;

        buildTime = wall.buildTime + casing.buildTime;
        buildCostMultiplier = 1f;

        super.init();

        update = wall.update || casing.casingTileUpdate();
        sync = wall.sync || casing.casingTileUpdate();

        if (casing.drawUpdate()) drawDynamic = true;

        localizedName = Core.bundle.format("block.ectotech-cased.name", wall.localizedName, casing.localizedName);
        description = casing.description;
    }

    @Override
    public void setBars() {
        super.setBars();
        casing.setBarsc(this);
    }

    @Override
    public void setStats() {
        super.setStats();
        casing.setStatsc(this, stats);
    }

    @Override
    public TextureRegion[] icons(){
        //стена + корпус; ванилла склеит их в -full иконку
        return new TextureRegion[] {
                Core.atlas.find(Core.atlas.has(wall.name) ? wall.name : wall.name + "1"),
                Core.atlas.find(casing.name)
        };
    }

    @Override
    public void onPicked(Tile tile){
        Vars.control.input.block = wall;
        wall.onPicked(tile);
    }

    @Override
    public boolean isPlaceable() {
        return supportsEnv(Vars.state.rules.env);
    }

    public class CasedWallBuild extends WallBuild {
        public Object casingData;

        @Override
        public void created(){
            super.created();
            casingData = casing.createDatac(this);
        }

        @Override
        public void updateTile(){
            super.updateTile();
            casing.updateTilec(this);
        }

        @Override
        public void draw(){
            super.draw();
            casing.drawc(this);
        }

        @Override
        public boolean collision(Bullet bullet){
            boolean collided = super.collision(bullet);
            return casing.collisionc(this, bullet, collided);
        }

        @Override
        public void drawCached(){
            Draw.rect(wall.variants == 0 ? wall.region : wall.variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, wall.variantRegions.length - 1))], x, y);
            Draw.rect(casing.region, x, y);
        }

        /** Заменять корпусированную стену может только другой корпус того же размера. */
        @Override
        public boolean canBeReplaced(Block other){
            return other instanceof WallCasing wc && wc != casing && other.size == block.size;
        }

        /** Призрак после разрушения — базовая стена, а не скрытый CasedWall (его нельзя построить). */
        @Override
        public void addPlan(boolean checkPrevious){
            if(team == Team.derelict || !CasedWall.this.rebuildable) return;
            var data = team.data();
            if (checkPrevious) {
                data.plans.remove(p -> p.x == tile.x && p.y == tile.y);
            }
            data.plans.addFirst(new Teams.BlockPlan(tile.x, tile.y, (short)rotation, CasedWall.this, null));
        }

        @Override
        public void write(Writes write){
            super.write(write);
            casing.writec(this, write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(casingData == null) casingData = casing.createDatac(this);
            casing.readc(this, read, revision);
        }
    }
}
