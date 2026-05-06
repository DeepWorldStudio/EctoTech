package ectotech.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;

import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

import mindustry.world.meta.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;


public class EctoUnitTypes {
    public static UnitType
            glare;

    public static void load() {

        glare = new UnitType("glare") {{
            constructor = UnitEntity::create;
            flying = true;

            speed = 1.2f;
            accel = 0.08f;
            drag = 0.02f;

            hitSize = 8f;
            health = 170f;

            itemCapacity = 45;

            hittable = false;
            buildSpeed = 0.6f;
            mineTier = 1;
            mineSpeed = 2.4f;

            //TODO: юнит может лечить юнитов вокруг своей аурой. Юнит умеет стрелять рядом с ядром (в его радиусе защиты).

        }};
    }
}

