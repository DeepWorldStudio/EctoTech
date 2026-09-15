package ectotech.content;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import ectotech.utilities.EctoMathf;
import mindustry.entities.*;
import mindustry.graphics.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static arc.math.Interp.*;

public class EctoFx {
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();

    public static final Effect

    quicksandPulverizeSmall = new Effect(30, e -> {
        randLenVectors(e.id, 3, e.fin() * 5f, (x, y) -> {
            color(e.color);
            Fill.square(e.x + x, e.y + y, e.fout() + 0.5f, 45);
        });
    }),

    regenParticleColor = new Effect(100f, e -> {
        color(e.color);

        Fill.square(e.x, e.y, e.fslope() * 1.5f + 0.14f, 45f);
    }),

    shootTurretBigColor = new Effect(9, e->{
        float offset = e.fin() * 16 + 2;
        float offsetX = Mathf.cosDeg(e.rotation) * offset, offsetY = Mathf.sinDeg(e.rotation) * offset;
        color(e.color);
        Drawf.tri(e.x + offsetX, e.y + offsetY, 5 * e.fout() + 1, 9 * e.fout() + 3, e.rotation);
        Drawf.tri(e.x + offsetX, e.y + offsetY, 5 * e.fout() + 1,  9 * e.fin() + 3, e.rotation + 180);
        stroke(e.fout() * 2.75f);
        Lines.ellipse(e.x + (offsetX / 3), e.y + (offsetY / 3), 1.45f * e.fin() + 0.75f, 1.5f, 3, e.rotation);
    }),

    geyserBurst = new Effect(60, e->{
        for(int i = 0; i < Mathf.round(Mathf.randomSeed(e.id, 2)); i++){
            //Дальность частички воды
            float range = Mathf.randomSeed(e.id + i, 70, 88);
            //Высота частицы
            float height = Mathf.randomSeed(e.id * 2L + i, 30, 34);
            //Направление частицы
            float rotation = Mathf.randomSeed(e.id + 3, 360);

            //Скаляр увеличения размера
            float growFactor = 0.05f;
            //Скаляр увеличения размера тени
            float shadowGrowFactor = 0.25f;

            float pX = getOffsetVec(e.x, e.y, range * e.fin(), rotation).x, pY = getOffsetVec(e.x, e.y, range * e.fin(), rotation).y;

            Vec2 pos = EctoMathf.toIsometric(height * Mathf.sinDeg(e.fin() * 180), pX, pY);

            Draw.z(Layer.effect);
            color(e.color);
            Fill.circle(pos.x, pos.y, (1 + 2 * e.fout() + height * Mathf.sinDeg(e.fin() * 180) * growFactor));

            Draw.z(Layer.darkness);
            color(Color.valueOf("000000").a((1- Mathf.sinDeg(e.fin() * 180) * 0.75f) * 0.5f));
            Fill.circle(pX, pY,  2 + Mathf.sinDeg(e.fin() * 180) * shadowGrowFactor);
        }
    });

    //Returns offset coordinates with cos/sin formula
    public static Vec2 getOffsetVec(float stX, float stY, float offset, float rotation){
        return v.set(offset * Mathf.cosDeg(rotation) + stX, offset * Mathf.sinDeg(rotation) + stY);
    }


}
