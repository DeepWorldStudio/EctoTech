package ectotech.world.blocks.power;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.Trigger;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.power.BeamNode;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.Stat;

import java.util.Arrays;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class OctoBeamNode extends BeamNode {

    private int diagRange;

    private TextureRegion powerBeamSphere;

    private static boolean handlerRegistered = false;

    public OctoBeamNode(String name) {
        super(name);

        buildType = OctoBeamNodeBuild::new;
        enableDrawStatus = false;

        allowDiagonal = true;
        swapDiagonalPlacement = true;

        consumePowerBuffered(0);

        if (!handlerRegistered) {
            handlerRegistered = true;
            Events.run(Trigger.draw, OctoBeamNode::drawNodeLinksHandler);
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("batteries");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.powerCapacity);
    }

    @Override
    public void load() {
        super.load();
        powerBeamSphere = Core.atlas.find(name + "-beam-sphere", "power-beam-sphere");
    }

    @Override
    public void init(){
        super.init();

        diagRange = (int) Math.ceil(range / Mathf.sqrt(2));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        for (int i = 0; i < 4; i++) {
            int maxLen = diagRange + size / 2;
            Building dest = null;
            var dir = Geometry.d8edge[i];
            int dx = dir.x, dy = dir.y;
            int offset = size / 2;

            for (int j = 1 + offset; j <= diagRange + offset; j++) {
                var other = world.build(x + j * dx, y + j * dy);

                if (other != null && other.isInsulated()) break;

                if (other != null && other.block.hasPower && other.team == Vars.player.team() && !(other.block instanceof PowerNode)) {
                    maxLen = j;
                    dest = other;
                    break;
                }
            }

            Drawf.dashLine(Pal.placing,
                    x * tilesize + dx * (tilesize * size / 2f + 2),
                    y * tilesize + dy * (tilesize * size / 2f + 2),
                    x * tilesize + dx * maxLen * tilesize,
                    y * tilesize + dy * maxLen * tilesize
            );

            if (dest != null) {
                Drawf.square(dest.x, dest.y, dest.block.size * tilesize / 2f + 2.5f, 0f);
            }
        }
    }


    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation, boolean diagonal) {
        if (diagonal) {
            if (points.size < 2) return;

            Point2 first = points.first();
            Point2 last = points.peek();

            float angle = Angles.angle(first.x, first.y, last.x, last.y);
            int dirIndex = (int) ((angle + 22.5f) / 45f) % 8;
            var step = Geometry.d8[dirIndex];

            int dist = Math.max(Math.abs(last.x - first.x), Math.abs(last.y - first.y));

            points.clear();
            for (int i = 0; i <= dist; i++) {
                points.add(new Point2(first.x + i * step.x, first.y + i * step.y));
            }

            int actualRange = rangeForDirection(dirIndex);

            Placement.calculateNodes(points, this, rotation, (p, o) -> Math.max(Math.abs(p.x - o.x), Math.abs(p.y - o.y)) <= actualRange + size - 1);
        }
    }

    public static void drawNodeLinksHandler() {
        if (Vars.headless || Vars.player == null || Vars.control == null || Vars.control.input == null) return;
        if (!Vars.state.isGame()) return;

        Block block = Vars.control.input.block;
        if (block == null) return;

        if (!(block.consumesPower || block.outputsPower) || !block.hasPower || !block.connectedPower) return;

        if (block instanceof OctoBeamNode) return;

        float mx = Core.input.mouseX();
        float my = Core.input.mouseY();

        float wx = Core.input.mouseWorld(mx, 0).x - block.offset;
        float wy = Core.input.mouseWorld(0, my).y - block.offset;

        int tx = mindustry.core.World.toTile(wx);
        int ty = mindustry.core.World.toTile(wy);

        Tile tile = world.tile(tx, ty);
        if (tile == null) return;

        Draw.z(Layer.overlayUI);

        drawPotentialDiagonalLinks(block, tx, ty);

        Draw.reset();
    }

    public static void drawPotentialDiagonalLinks(Block block, int x, int y){
        if (block == null || block instanceof OctoBeamNode) return;

        if ((block.consumesPower || block.outputsPower) && block.hasPower && block.connectedPower) {
            Tile tile = world.tile(x, y);
            if(tile == null) return;

            getDiagonalNodeLinks(tile, block, Vars.player.team(), other -> {
                OctoBeamNode node = (OctoBeamNode)other.block;

                Draw.color(node.laserColor1, Renderer.laserOpacity * 0.5f);
                node.drawDiagonalLaser(other.x, other.y, x * tilesize + block.offset, y * tilesize + block.offset, other.block.size, block.size);

                Drawf.square(other.x, other.y, other.block.size * tilesize / 2f + 2f, Pal.place);
            });
        }
    }

    public static void getDiagonalNodeLinks(Tile tile, Block block, Team team, Cons<Building> others) {
        var tree = team.data().buildingTree;
        if (tree == null) return;

        float cx = tile.worldx() + block.offset;
        float cy = tile.worldy() + block.offset;
        float s = block.size * tilesize / 2f;

        // Максимально ожидаемый диагональный range для превью.
        // Аналог maxRange у BeamNode, но приведённый к диагонали.
        int maxDiagRange = (int) Math.ceil(30f / Mathf.sqrt(2f));
        float r = maxDiagRange * tilesize;

        for (int i = 0; i < 4; i++) {
            switch (i) {
                // 0 = вправо-вверх
                case 0 -> Tmp.r1.set(cx - s, cy - s, r + s * 2f, r + s * 2f);
                // 1 = влево-вверх
                case 1 -> Tmp.r1.set(cx + s, cy - s, -r - s * 2f, r + s * 2f).normalize();
                // 2 = влево-вниз
                case 2 -> Tmp.r1.set(cx + s, cy + s, -r - s * 2f, -r - s * 2f).normalize();
                // 3 = вправо-вниз
                case 3 -> Tmp.r1.set(cx - s, cy + s, r + s * 2f, -r - s * 2f).normalize();
            }

            tempBuilds.clear();
            tree.intersect(Tmp.r1, tempBuilds);

            int fi = i;
            Building closest = tempBuilds.min(
                    b -> b instanceof OctoBeamNodeBuild node
                            && node.couldDiagonalConnect((fi + 2) & 3, block, tile.x, tile.y),
                    b -> b.dst2(cx, cy)
            );

            tempBuilds.clear();

            if (closest != null) {
                others.get(closest);
            }
        }
    }


    public void drawDiagonalLaser(float x1, float y1, float x2, float y2, int size1, int size2) {
        float w = laserWidth;

        float dx = x2 - x1;
        float dy = y2 - y1;

        // Только для диагоналей
        if (Mathf.zero(dx) || Mathf.zero(dy)) return;

        int sx = Mathf.sign(dx);
        int sy = Mathf.sign(dy);

        float half1 = size1 * tilesize / 2f;
        float half2 = size2 * tilesize / 2f;

        // 1. Точка старта — угол нашего узла
        float startX = x1 + half1 * sx;
        float startY = y1 + half1 * sy;

        float t = Math.max(Math.abs(x2 - x1) - half2, Math.abs(y2 - y1) - half2) - half1;

        // 3. Если блоки соприкасаются (дистанция почти 0 или отрицательная)
        if(t <= 1f){
            if(powerBeamSphere != null && powerBeamSphere.found()){
                Draw.rect(powerBeamSphere, startX, startY);
            }
            return;
        }

        // 4. Точка конца — старт + пройденный по диагонали путь
        float endX = startX + t * sx;
        float endY = startY + t * sy;

        Drawf.laser(laser, laserEnd, startX, startY, endX, endY, w);
    }

    public int rangeForDirection(int direction) {
        return (direction & 1) == 1 ? diagRange : range;
    }

    public class OctoBeamNodeBuild extends BeamNodeBuild {

        public Building[] links = new Building[8]; // linked buildings
        public Tile[] dests = new Tile[8]; // beam end tiles

        public boolean couldDiagonalConnect(int direction, Block target, int targetX, int targetY) {
            int offset = -(target.size - 1) / 2;

            int minX = targetX + offset, minY = targetY + offset, maxX = minX + target.size - 1, maxY = minY + target.size - 1;
            var dir = Geometry.d8edge[direction];

            int rangeOffset = size / 2;

            for (int j = 1 + rangeOffset; j <= diagRange + rangeOffset; j++) {
                Tile other = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);

                if (other == null) return false;

                if ((other.build != null && other.build.isInsulated()) || (other.block().hasPower && other.block().connectedPower && other.team() == team)) {
                    return false;
                }

                if (other.x >= minX && other.x <= maxX && other.y >= minY && other.y <= maxY) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void draw(){
            super.draw();

            if(Mathf.zero(Renderer.laserOpacity) || team == Team.derelict) return;

            Draw.z(Layer.power);
            Draw.color(laserColor1, laserColor2, (1f - power.graph.getSatisfaction()) * 0.86f + Mathf.absin(3f, 0.1f));
            Draw.alpha(Renderer.laserOpacity);

            float w = laserWidth + Mathf.absin(pulseScl, pulseMag);

            for (int i = 0; i < 8; i ++) {
                if (links[i] == null || dests[i] == null || !links[i].wasVisible) continue;
                if (!shouldDrawBeam(i)) continue;

                int dst = Math.max(Math.abs(dests[i].x - tile.x), Math.abs(dests[i].y - tile.y));
                var point = Geometry.d8[i];
                float poff = tilesize / 2f;
                //don't draw lasers for adjacent blocks (excluding diagonal)
                if ((dst > 1 + size / 2)) {
                    Drawf.laser(laser, laserEnd, x + poff * size * point.x, y + poff * size * point.y, dests[i].worldx() - poff * point.x, dests[i].worldy() - poff * point.y, w);
                }
                else if ((i & 1) == 1) {
                    Building target = links[i];
                    boolean shouldDraw = true;

                    for (int j = 0; j < 8; j += 2) {
                        if (links[j] == target) {
                            shouldDraw = false;
                            break;
                        }
                    }

                    if (shouldDraw) {
                        for (Building neighbour : proximity) {
                            if (neighbour.block.hasPower && neighbour.block.outputsPower) {
                                if (neighbour.proximity.contains(target)) {
                                    shouldDraw = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (shouldDraw) {
                        Draw.rect(powerBeamSphere, x + poff * size * point.x, y + poff * size * point.y);
                    }
                }
            }

            Draw.reset();
        }

        private boolean shouldDrawBeam(int direction) {
            Building link = links[direction];

            if (!(link.block instanceof BeamNode beam)) return true;

            int thisRange = OctoBeamNode.this.rangeForDirection(direction);

            boolean reciprocal = false;
            int otherRange = beam.range;

            if (link instanceof OctoBeamNodeBuild other) {
                int otherDirection = (direction + 4) & 7;

                otherRange = ((OctoBeamNode) other.block).rangeForDirection(otherDirection);
                reciprocal = other.links[otherDirection] == this;
            } else {
                if ((direction & 1) == 1) return true;

                int otherDirection4 = ((direction >> 1) + 2) & 3;

                if (link instanceof BeamNodeBuild other) {
                    reciprocal = other.links[otherDirection4] == this;
                }
            }

            if (!reciprocal) return true;

            return (link.id > id && thisRange >= otherRange) || thisRange > otherRange;
        }

        @Override
        public void pickedUp(){
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
        }

        @Override
        public void updateDirections() {
            for(int i = 0; i < 8; i ++) {
                var prev = links[i];
                var dir = Geometry.d8[i];
                links[i] = null;
                dests[i] = null;
                int offset = size / 2;
                int actualRange = rangeForDirection(i);
                //find first block with power in range
                for (int j = 1 + offset; j <= actualRange + offset; j++) {
                    var other = world.build(tile.x + j * dir.x, tile.y + j * dir.y);

                    //hit insulated wall
                    if (other != null && other.isInsulated()) {
                        break;
                    }

                    //power nodes do NOT play nice with beam nodes, do not touch them as that forcefully modifies their links
                    if (other != null && other.block.hasPower && other.block.connectedPower && other.team == team && !(other.block instanceof PowerNode)) {
                        links[i] = other;
                        dests[i] = world.tile(tile.x + j * dir.x, tile.y + j * dir.y);
                        break;
                    }
                }

                var next = links[i];

                if (next != prev) {
                    //unlinked, disconnect and reflow
                    if (prev != null && prev.isAdded() && !hasAnotherBeamTo(prev, i)) {
                        prev.power.links.removeValue(pos());
                        power.links.removeValue(prev.pos());

                        PowerGraph newgraph = new PowerGraph();
                        newgraph.reflow(this);

                        if (prev.power.graph != newgraph) {
                            PowerGraph og = new PowerGraph();
                            og.reflow(prev);
                        }
                    }

                    //linked to a new one, connect graphs
                    if (next != null) {
                        power.links.addUnique(next.pos());
                        next.power.links.addUnique(pos());

                        power.graph.addGraph(next.power.graph);
                    }
                }
            }
        }

        /**Returns whether another directional beam still targets this building.*/
        private boolean hasAnotherBeamTo(Building target, int exceptDirection) {
            for (int i = 0; i < links.length; i++) {
                if (i != exceptDirection && links[i] == target) {
                    return true;
                }
            }
            return false;
        }

    }
}
