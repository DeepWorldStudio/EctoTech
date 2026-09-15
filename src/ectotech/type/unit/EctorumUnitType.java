package ectotech.type.unit;

import mindustry.type.UnitType;
import mindustry.world.meta.Env;

public class EctorumUnitType extends UnitType {

    public EctorumUnitType(String name){
        super(name);

        envDisabled = Env.space;
        researchCostMultiplier = 10f;
    }
}
