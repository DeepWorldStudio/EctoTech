package ectotech;

import arc.graphics.Color;

public class EctoVars {
    /**default pressure, in atmospheres*/
    public static final float defaultPressure = 1f;
    /**absolute min pressure, in atmospheres. Don't change unless you know what are you doing*/
    public static final float absMinPressure = 0.0001f;

    /**default tanh coefficient for pressure outflow, used if isn't override or in a negative pressure change*/
    public static final float defaultTanhFactor = 7.0f;
    /**default exponent coefficient for pressure outflow, used if isn't override or in a negative pressure change*/
    public static final float defaultExponentCoefficient = 1.25f;
    /**default pressure bar color, used in setBars() methods*/
    public static final Color defaultPressureBarColor = Color.valueOf("ABC4C9");

}
