package ectotech.type.unit;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.UnitType;

import static arc.math.Mathf.clamp;
import static java.lang.Math.round;

/**Custom class with an engine type choosing support. Supports vanilla engine.*/
public class AlternateEnginedUnitType extends EctorumUnitType {

    public interface EngineProv {
        UnitEngine create(float x, float y, float radius, float rotation);
    }
    /**Unused in a base UnitEngine class*/
    public @Nullable Color engineColorOuter = null;
    /**Main engine type*/
    public EngineProv engineType = UnitEngine::new;

    public AlternateEnginedUnitType(String name) {
        super(name);
    }

    @Override
    public void init() {
        float savedEngineSize = engineSize;
        engineSize = -1f;

        super.init();

        engineSize = savedEngineSize;

        if (engineSize > 0 && engineType != null) {
            engines.add(engineType.create(0f, -engineOffset, engineSize, -90f));
        }
    }

    public static class FlameJetEngine extends UnitEngine {

        public FlameJetEngine(float x, float y, float radius, float rotation) {
            super(x, y, radius, rotation);
        }

        public FlameJetEngine() {
        }

        @Override
        public void draw(Unit unit) {
            UnitType type = unit.type;
            float scale = type.useEngineElevation ? unit.elevation : 1f;

            if (scale <= 0.0001f) return;

            float rot = unit.rotation - 90;
            Color midColor = type.engineColor == null ? unit.team.color : type.engineColor;
            Color outerColor;

            if (type instanceof AlternateEnginedUnitType alt) {
                outerColor = alt.engineColorOuter != null ? alt.engineColorOuter :
                        Tmp.c1.set(midColor).mul(0.7f, 0.7f, 0.7f, 1f);
            } else {
                outerColor = Tmp.c1.set(midColor).mul(0.7f, 0.7f, 0.7f, 1f);
            }

            Tmp.v1.set(x, y).rotate(rot);
            float ex = unit.x + Tmp.v1.x, ey = unit.y + Tmp.v1.y;

            float rad = (radius + Mathf.absin(Time.time, 2f, radius / 4f)) * scale;

            float baseWidth = rad;
            float baseLength = rad * 3.5f;

            float flameAngle = rot + rotation;

            int divisions = Mathf.clamp(Mathf.round(radius * 4f), 8, 20);

            Draw.color(outerColor, 0.45f * scale);
            Drawf.flame(ex, ey, divisions, flameAngle, baseLength * 1.15f, baseWidth * 1.2f, 0.32f);

            Draw.color(midColor, 0.85f * scale);
            Drawf.flame(ex, ey, divisions, flameAngle, baseLength * 0.85f, baseWidth * 0.9f, 0.22f);

            Draw.color(type.engineColorInner, scale);
            Drawf.flame(ex, ey, divisions, flameAngle, baseLength * 0.45f, baseWidth * 0.6f, 0.12f);
        }

        @Override
        public FlameJetEngine copy() {
            try {
                return (FlameJetEngine)clone();
            } catch (CloneNotSupportedException awful) {
                throw new RuntimeException("fantastic", awful);
            }
        }
    }
}
