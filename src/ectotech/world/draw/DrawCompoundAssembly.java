package ectotech.world.draw;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawCompoundAssembly extends DrawBlock {

    protected static final Rand rand = new Rand();

    public TextureRegion softCircle;

    public float x = 0f, y = 0f;
    public float layer = Layer.blockOver;

    // Кольцо
    public float ringRadius = 8f;
    public float ringStroke = 1.6f;
    public float ringPulseScl = 8f;
    public float ringPulseMag = 0.8f;
    public Color ringColor = Color.valueOf("7a8f92");

    // Ядро
    public float coreRadius = 8f;
    public float coreMidScl = 0.33f;
    public float corePulseScl = 8f;
    public float corePulseMag = 0.8f;
    public Color coreColor = Color.valueOf("7a9fb5");

    // Частицы внутри
    public int particles = 10;
    public float particleSize = 2.4f;
    public float particleLen = 7f;
    public float rotateScl = 3f;
    public float particleLife = 110f;
    public Interp particleInterp = f -> Interp.circleOut.apply(Interp.slope.apply(f));
    public Color[] particleColors = {
            Color.valueOf("5db56a"),
            Color.valueOf("53565c")
    };

    public float glowAlpha = 0.15f;
    public float glowScl = 1.7f;

    @Override
    public void draw(Building build) {
        float warmup = build.warmup();
        if (warmup <= 0.001f) return;

        Draw.z(layer);

        float progress = build.totalProgress();

        float ringRad = ringRadius + Mathf.absin(progress, ringPulseScl, ringPulseMag);
        float coreRad = coreRadius + Mathf.absin(progress, corePulseScl, corePulseMag);

        float rx = build.x + x;
        float ry = build.y + y;

        Draw.blend(Blending.additive);

        // Мягкий ореол
        Draw.color(ringColor);
        Draw.alpha(glowAlpha * warmup);
        float glowSize = ringRad * glowScl * 2f;
        Draw.rect(softCircle, rx, ry, glowSize, glowSize);

        // Частицы внутри кольца (мягкие)
        float base = progress / particleLife;
        rand.setSeed(build.id);

        for (int i = 0; i < particles; i++) {
            float fin = (rand.random(1f) + base) % 1f;
            float fout = 1f - fin;
            float angle = rand.random(360f) + (progress / rotateScl) % 360f;
            float len = particleLen * particleInterp.apply(fout);

            Color pColor = particleColors.length == 0
                    ? Color.white
                    : particleColors[i % particleColors.length];

            Draw.color(pColor);
            Draw.alpha(warmup * Mathf.slope(fin));

            // Мягкий круг
            float r = particleSize * Mathf.slope(fin) * warmup * 1.5f;
            Draw.rect(softCircle,
                    rx + Angles.trnsx(angle, len),
                    ry + Angles.trnsy(angle, len),
                    r, r
            );
        }

        // Кольцо (чёткое)
        Draw.color(ringColor);
        Draw.alpha(warmup * 0.85f);
        Lines.stroke(ringStroke * warmup);
        Lines.circle(rx, ry, ringRad);

        // Ядро: мягкий glow
        Draw.color(coreColor);
        Draw.alpha(0.35f * warmup);
        float coreGlowSize = coreRad * coreMidScl * 3.2f;
        Draw.rect(softCircle, rx, ry, coreGlowSize, coreGlowSize);

        // Ядро: чёткий центр
        Draw.color(coreColor);
        Draw.alpha(warmup * 0.9f);
        Fill.circle(rx, ry, coreRad * coreMidScl);

        Draw.blend();
        Draw.reset();
    }

    @Override
    public void load(Block block) {
        softCircle = Core.atlas.find("circle-shadow");
    }

    @Override
    public TextureRegion[] icons(Block block) {
        return new TextureRegion[0];
    }
}