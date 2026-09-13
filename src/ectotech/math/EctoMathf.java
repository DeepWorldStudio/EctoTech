package ectotech.math;

import arc.math.Mathf;
import arc.math.geom.Vec2;

public class EctoMathf {
    private static final Vec2 outputVec = new Vec2();
    //Pseudo3d functions
    public static Vec2 toIsometric(float height, float x, float y){
        return  outputVec.set(x, y + height);
    }

    //Returns offset coordinates with cos/sin formula
    public static Vec2 getOffsetVec(float stX, float stY, float offset, float rotation){
        return outputVec.set(offset * Mathf.cosDeg(rotation) + stX, offset * Mathf.sinDeg(rotation) + stY);
    }
}
