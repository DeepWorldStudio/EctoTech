package ectotech.world.blocks.defense.utils;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import ectotech.world.blocks.defense.CasedWall;
import ectotech.world.blocks.defense.WallCasing;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.defense.Wall;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class CasedWallFactory {
    private static final ObjectMap<Block, ObjectMap<WallCasing, CasedWall>> pairs = new ObjectMap<>();
    private static final Seq<WallCasing> casings = new Seq<>();

    private static final IntMap<Op> ops = new IntMap<>();
    private static boolean hooked;

    private CasedWallFactory() {}

    public static void register(WallCasing casing) {
        casings.add(casing);
    }

    public static @Nullable CasedWall get(Block wall, WallCasing casing) {
        var map = pairs.get(wall);
        return map == null ? null : map.get(casing);
    }

    /** Вызывать на ModContentLoadEvent: стены всех модов уже созданы, init() ещё не был. */
    public static void generate() {
        int count = 0;
        for (Block block : Vars.content.blocks().copy()) {
            if (!(block instanceof Wall wall) || wall.subclass != Wall.class || wall.autotile) continue;

            for (WallCasing casing : casings) {
                if (casing.size != wall.size || get(wall, casing) != null) continue;

                CasedWall result = new CasedWall(wall, casing);
                result.minfo.mod = casing.minfo.mod;
                result.minfo.sourceFile = casing.minfo.sourceFile;

                pairs.get(wall, ObjectMap::new).put(casing, result);
                count++;
            }
        }
        Log.info("EctoTech: generated @ cased wall variants.", count);

        installHooks();
    }

    private static void installHooks() {
        if (hooked) return;
        hooked = true;

        SaveVersion.addCustomChunk("ectotech-casing-ops",
                new SaveFileReader.CustomChunk() {
            @Override
            public boolean shouldWrite() {
                prune();
                return ops.size > 0;
            }

            @Override
            public void write(DataOutput stream) throws IOException {
                prune();
                stream.writeByte(0); //версия формата
                stream.writeInt(ops.size);
                for(var e : ops.entries()){
                    stream.writeInt(e.key);
                    stream.writeShort(e.value.previous.id);
                    stream.writeByte(e.value.team.id);
                    stream.writeFloat(e.value.health);
                }
            }

            @Override
            public void read(DataInput stream) throws IOException {
                ops.clear();
                stream.readByte();
                int n = stream.readInt();
                for(int i = 0; i < n; i++){
                    int pos = stream.readInt();
                    Block prev = Vars.content.block(stream.readShort());
                    Team team = Team.get(stream.readUnsignedByte());
                    float hp = stream.readFloat();
                    if(prev != null) ops.put(pos, new Op(prev, team, hp));
                }
            }
        });

        Events.on(WorldLoadBeginEvent.class, e -> ops.clear());
        Events.on(ResetEvent.class, e -> ops.clear());

        Events.on(BlockBuildBeginEvent.class, e -> {
            if (!(e.tile.build instanceof ConstructBuild cb)) return;

            if (!e.breaking) {
                if (!(cb.current instanceof WallCasing casing) || cb.prevBuild == null) return;
                Building under = cb.prevBuild.find(b -> b.block instanceof Wall && !(b.block instanceof WallCasing));
                if (under == null) return;
                Block base = under.block instanceof CasedWall c ? c.wall : under.block;
                if (get(base, casing) == null) return;

                ops.put(e.tile.pos(), new Op(under.block, under.team, under.health));
            } else {
                if (cb.prevBuild == null || cb.prevBuild.isEmpty()) return;
                Building prev = cb.prevBuild.first();
                if (!(prev.block instanceof CasedWall cased)) return;

                ops.put(e.tile.pos(), new Op(cased, prev.team, prev.health));
                cb.setDeconstruct(cased.casing);
                cb.previous = cased.wall;
            }
        });

        Events.on(BlockBuildEndEvent.class, e -> {
            if (e.breaking && e.tile.build instanceof ConstructBuild cb) restore(e.tile, cb);
        });

        Events.on(BlockDestroyEvent.class, e -> {
            if (e.tile.build instanceof ConstructBuild cb) restore(e.tile, cb);
        });
    }

    public static void complete(Tile tile, WallCasing casing, int rotation){
        Op op = ops.remove(tile.pos());
        if(op == null || !(tile.build instanceof WallCasing.WallCasingBuild)) return;

        Block base = op.previous instanceof CasedWall c ? c.wall : op.previous;
        CasedWall result = get(base, casing);
        if(result == null) return;

        float hp = op.previous instanceof CasedWall old
                ? wallHealthAfterRemoval(old, op.health) + casing.health
                : op.health + casing.health;

        tile.setBlock(result, op.team, rotation);
        tile.build.health = Mathf.clamp(hp, 1f, tile.build.maxHealth);
        Vars.indexer.notifyHealthChanged(tile.build);
    }

    /** Снятие корпуса завершено, либо установка/замена/разборка сорвана — вернуть исходник. */
    private static void restore(Tile tile, ConstructBuild cb) {
        if (!(cb.current instanceof WallCasing casing)) return;

        Op op = ops.remove(tile.pos());
        Block source = op != null ? op.previous : cb.previous;
        if (source == null || source == Blocks.air || source instanceof WallCasing) return;

        Block result;
        float hp;

        if (source instanceof CasedWall old) {
            if (old.casing == casing) {
                result = old.wall;
                hp = op != null ? wallHealthAfterRemoval(old, op.health) : old.wall.health * cb.healthf();
            } else {
                result = old;
                hp = op != null ? op.health : old.health * cb.healthf();
            }
        } else if (get(source, casing) != null) {
            result = source;
            hp = op != null ? op.health : source.health * cb.healthf();
        } else {
            return;
        }

        Team team = op != null ? op.team : cb.team;
        float finalHp = Math.max(1f, hp);
        int rotation = cb.rotation;

        Core.app.post( () -> {
            if (tile.build != null || tile.block() != Blocks.air) return;
            tile.setBlock(result, team, rotation);
            tile.build.health = finalHp;
            Vars.indexer.notifyHealthChanged(tile.build);
        });
    }

    /** Убрать записи, чей construct уже не существует (редактор, чужие подмены). */
    private static void prune() {
        IntSeq dead = new IntSeq();
        for (var e : ops.entries()) {
            if (!(Vars.world.build(e.key) instanceof ConstructBuild cb) || !(cb.current instanceof WallCasing)) dead.add(e.key);
        }
        for (int i = 0; i < dead.size; i++) ops.remove(dead.get(i));
    }

    public static float wallHealthAfterRemoval(CasedWall cased, float currentHealth) {
        float damage = (cased.health - currentHealth) * (1f - Mathf.clamp(cased.casing.damageAbsorption));
        return Mathf.clamp(cased.wall.health - damage, 1f, cased.wall.health);
    }

    /**
     * @param previous Wall или CasedWall
     * @param team     исходная команда (derelict остаётся derelict)
     * @param health   HP исходной постройки на момент начала
     */
    private record Op(Block previous, Team team, float health) {}
}