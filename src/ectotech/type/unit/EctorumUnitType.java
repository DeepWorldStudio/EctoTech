package ectotech.type.unit;

import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.world.meta.Env;

public class EctorumUnitType extends UnitType {

    public EctorumUnitType(String name){
        super(name);
        outlineColor = Pal.darkOutline;
        envDisabled = Env.space;
        researchCostMultiplier = 10f;
    }
}
