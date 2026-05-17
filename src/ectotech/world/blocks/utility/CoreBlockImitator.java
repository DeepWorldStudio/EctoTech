package ectotech.world.blocks.utility;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.EnumSet;
import arc.util.Nullable;
import mindustry.content.Blocks;
import mindustry.entities.TargetPriority;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.*;

public class CoreBlockImitator extends StorageBlock {

    /**
     * Копируемое ядро
     */
    public CoreBlock mimicCore;

    public @Nullable Floor fakeCoreFloor = Blocks.coreZone.asFloor();

    protected TextureRegion[][] edgeRegions;

    public CoreBlockImitator(String name, CoreBlock mimicCore) {
        super(name);
        this.mimicCore = mimicCore;

        // Копируем размер ядра автоматически
        this.size = mimicCore.size;
        buildType = CoreBlockImitatorBuild::new;

        this.solid = true;
        this.destructible = true;
        this.hasItems = true;
        this.acceptsItems = true;
        this.coreMerge = false;

        // Группа и приоритет как у ядра
        this.group = mimicCore.group;
        this.priority = TargetPriority.core;
        this.flags = EnumSet.of(BlockFlag.core, BlockFlag.storage);

        this.placeableLiquid = false;
        this.itemCapacity = 500;
        this.allowResupply = false;
    }

    protected boolean hasLiquidUnder(Tile tile) {
        if (tile == null) return false;
        tile.getLinkedTilesAs(this, tempTiles);
        return tempTiles.contains(other -> other.floor().isLiquid);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return tile != null && !hasLiquidUnder(tile);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Tile tile = world.tile(x, y);
        if (tile == null) return;

        if (hasLiquidUnder(tile)) {
            drawPlaceText(Core.bundle.get("bar.ectotech-cant-place-on-liquid", "Cannot be placed on liquid tiles"), x, y, false);
        }
    }

    /** Экземпляр Имитатора в мире */
    public class CoreBlockImitatorBuild extends StorageBuild {

        @Override
        public boolean acceptItem(Building source, Item item){
            return block.acceptsItems && (state.rules.coreIncinerates || items.get(item) < itemCapacity);
        }

        @Override
        public int getMaximumAccepted(Item item){
            return state.rules.coreIncinerates ? Integer.MAX_VALUE / 2 : itemCapacity;
        }

        @Override
        public void handleItem(Building source, Item item){
            if(items.get(item) >= itemCapacity){
                StorageBlock.incinerateEffect(this, source);
            }else{
                super.handleItem(source, item);
            }
        }

        @Override
        public boolean canUnload() {
            return block.unloadable && state.rules.allowCoreUnloaders;
        }

        public boolean drawAsMimic() {
            // Если мы не в редакторе и команда игрока враждебна — включаем маскировку
            return !(state.isEditor()) && player != null && player.team() != team;
        }

        @Override
        public void draw() {
            if (drawAsMimic()) {
                drawFakeFloor();

                // 2. Рисуем само ядро
                Draw.rect(mimicCore.region, x, y);

                // 3. Рисуем командный цвет ядра
                if (mimicCore.teamRegions != null && team.id < mimicCore.teamRegions.length) {
                    TextureRegion teamRegion = mimicCore.teamRegions[team.id];
                    if (teamRegion.found()) {
                        if (teamRegion == mimicCore.teamRegion) Draw.color(team.color);
                        Draw.rect(teamRegion, x, y);
                        Draw.color();
                    }
                }

                drawCracks(); // Трещины при уроне
            } else {
                // Если свой — рисуем обычную текстуру имитатора
                super.draw();
            }
        }

        protected void drawFakeFloor() {
            if (fakeCoreFloor == null) return;

            // Ленивая загрузка краев (только один раз при первой отрисовке)
            if (edgeRegions == null && !headless) {
                String edgeName = fakeCoreFloor.name + "-edge";
                if (Core.atlas.has(edgeName)) {
                    int tsize = (int)(tilesize / Draw.scl);
                    edgeRegions = Core.atlas.find(edgeName).split(tsize, tsize);
                }
            }

            float oldZ = Draw.z();

            // 1. Рисуем основное заполнение на слое пола
            Draw.z(Layer.floor);
            tile.getLinkedTilesAs(block, tempTiles);
            for (Tile t : tempTiles) {
                fakeCoreFloor.drawMain(t);
            }

            // 2. Рисуем края для эффекта смешивания
            if (edgeRegions != null) {
                Draw.z(Layer.floor + 0.01f); // Чуть выше, чтобы нахлёстывало на соседние плитки
                for (Tile t : tempTiles) {
                    for (int i = 0; i < 8; i++) {
                        Point2 side = Geometry.d8[i];
                        Tile other = world.tile(t.x + side.x, t.y + side.y);

                        // Если соседний тайл НЕ занят этим же имитатором — рисуем край
                        if (other == null || other.build != this) {
                            // Выбираем нужный фрагмент края (угол или прямая) из массива split
                            TextureRegion edge = edgeRegions[Math.max(0, 1 - side.x)][Math.max(0, 1 + side.y)];
                            if (edge != null) Draw.rect(edge, t.drawx(), t.drawy());
                        }
                    }
                }
            }

            Draw.z(oldZ);
        }

        @Override
        public TextureRegion getDisplayIcon() {
            return drawAsMimic() ? mimicCore.uiIcon : super.getDisplayIcon();
        }

        @Override
        public String getDisplayName() {
            return drawAsMimic() ? mimicCore.localizedName : super.getDisplayName();
        }
    }
}