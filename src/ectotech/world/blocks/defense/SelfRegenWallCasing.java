package ectotech.world.blocks.defense;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Nullable;
import ectotech.content.EctoFx;
import ectotech.graphics.EctoPal;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;

import static mindustry.Vars.tilesize;

public class SelfRegenWallCasing extends WallCasing {
    /** CasedWall's maxHealth percent per frame*/
    public float healPercent = 2f / 60f;

    public float effectChance = 0.003f;
    public Effect healEffect = EctoFx.regenParticleColor;
    public Color healColor = EctoPal.casingSelfRegen;

    public SelfRegenWallCasing(String name) {
        super(name);
    }

    @Override
    public boolean casingTileUpdate() {
        return true;
    }

    @Override
    public void updateTilec(CasedWall.CasedWallBuild build){
        if (!build.damaged()) return;

        if (!Vars.net.client()) {
            build.heal(healPercent * build.edelta() * build.maxHealth / 100f);
            build.recentlyHealed();
        }

        if (Mathf.chanceDelta(effectChance * build.block.size * build.block.size)) {
            float r = build.block.size * tilesize / 2f - 1f;
            healEffect.at(build.x + Mathf.range(r), build.y + Mathf.range(r), healColor);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.repairTime, (int)(100f / healPercent), StatUnit.seconds);
    }

    @Override
    public void setStatsc(@Nullable CasedWall cased, Stats stats){
        stats.add(Stat.repairTime, (int)(1f / (healPercent / 100f) / 60f), StatUnit.seconds);
    }
}
