package ectotech.world.blocks.environment;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

public class ParticledFloor extends Floor {
    public @Nullable Effect updateEffect;
    public float updateEffectChance = 0.0001f;
    public Color effectColor = Color.valueOf("8A8A8A");
    public float effectSpread = 3.5f;

    public ParticledFloor(String name) {
        this(name, 3);
    }

    public ParticledFloor(String name, int variants) {
        super(name, variants);
    }

    @Override
    public boolean updateRender(Tile tile) {
        return super.updateRender(tile) || updateEffect != null && updateEffect != Fx.none;
    }

    @Override
    public void renderUpdate(UpdateRenderState state) {
        if (updateEffect == null || updateEffect == Fx.none) return;

        if (Mathf.chanceDelta(updateEffectChance)) {
            updateEffect.at(
                    state.tile.worldx() + Mathf.range(effectSpread),
                    state.tile.worldy() + Mathf.range(effectSpread),
                    0f, effectColor);
        }
    }
}
