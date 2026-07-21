package ectotech.world.blocks.environment;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Time;
import ectotech.content.EctoFx;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

public class EctorumQuicksand extends Floor {

    public float buildingDamageTaken = 0.08f;

    private static final IntSet processedBuilds = new IntSet();
    private static final Seq<Tile> tmpTiles = new Seq<>();

    private static boolean registered = false;

    public EctorumQuicksand(String name) {
        super(name);

        speedMultiplier = 0.35f;
        dragMultiplier = 2.25f;
        damageTaken = 1.25f;

        isLiquid = false;
        drownTime = 450f;

        albedo = 0.05f;
        allowCorePlacement = false;
        variants = 0;

        if (!registered) {
            registered = true;

            Events.run(EventType.Trigger.update, () -> {
                if (!Vars.net.client() && !Vars.state.isEditor()) updateBuildings();
            });
        }
    }

    @Override
    public boolean isDeep() {
        return false;
    }

    private static void updateBuildings() {
        processedBuilds.clear();

        for (int x = 0; x < Vars.world.width(); x++) {
            for (int y = 0; y < Vars.world.height(); y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null) continue;
                if (!(tile.floor() instanceof EctorumQuicksand)) continue;

                var b = tile.build;
                if (b == null || b.dead()) continue;
                if (!processedBuilds.add(b.id)) continue;

                float damage = 0f;
                int totalTiles = 0;

                for (Tile t : b.tile.getLinkedTiles(tmpTiles)) {
                    totalTiles++;

                    if (t.floor() instanceof EctorumQuicksand q) {
                        damage += q.buildingDamageTaken;
                    }
                }

                tmpTiles.clear();

                if (damage > 0f && totalTiles > 0) {
                    if (!Vars.headless && Mathf.chanceDelta(0.06f * b.block.size)) {
                        float radius = b.block.size * Vars.tilesize / 2f;
                        float px = b.x + Mathf.range(radius);
                        float py = b.y + Mathf.range(radius);

                        if (Mathf.chance(0.6f)) {
                            EctoFx.quicksandPulverizeSmall.at(px, py, 1f, Color.valueOf("DED981"));
                        }
                    }

                    float variation = Mathf.randomSeed(b.id + (long)(Time.time / 20), 0.6f, 1.4f);
                    b.damage(damage / totalTiles * variation * Time.delta);
                }
            }
        }
    }
}