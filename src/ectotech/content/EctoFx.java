package ectotech.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.units.UnitAssembler.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;

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

    shootTurretBigColor = new Effect(9, e->{
        float offset = e.fin() * 16 + 2;
        float offsetX = Mathf.cosDeg(e.rotation) * offset, offsetY = Mathf.sinDeg(e.rotation) * offset;
        color(e.color);
        Drawf.tri(e.x + offsetX, e.y + offsetY, 5 * e.fout() + 1, 9 * e.fout() + 3, e.rotation);
        Drawf.tri(e.x + offsetX, e.y + offsetY, 5 * e.fout() + 1,  9 * e.fin() + 3, e.rotation + 180);
        stroke(e.fout() * 2.75f);
        Lines.ellipse(e.x + (offsetX / 3), e.y + (offsetY / 3), 1.45f * e.fin() + 0.75f, 1.5f, 3, e.rotation);
    });
}
