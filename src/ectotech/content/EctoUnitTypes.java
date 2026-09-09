package ectotech.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;

import ectotech.ai.types.RotateMoveCommandAI;
import ectotech.entities.abilities.BuildRepairFieldAbility;
import ectotech.entities.abilities.HitStatusFieldAbility;
import ectotech.entities.bullet.ArcWaveBulletType;
import ectotech.graphics.EctoPal;
import ectotech.type.unit.AlternateEnginedUnitType;
import ectotech.type.unit.EctorumUnitType;
import ectotech.type.weapons.CoreProximityWeapon;
import mindustry.ai.types.BuilderAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
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


public class EctoUnitTypes {
    public static UnitType

            // mech
            fist,

            // legs
            echo,

            // air
            spasm,

            // air, core
            glare;

    public static void load() {

        fist = new EctorumUnitType("fist") {{
            constructor = MechUnit::create;

            researchCostMultiplier = 0.5f;

            speed = 0.5f;
            hitSize = 9f;
            health = 450;
            stepSoundVolume = 0.4f;

            alwaysCreateOutline = false;

            weapons.add(new Weapon("ectotech-fist-weapon"){{
                reload = 0.4f * 60f;
                x = 4.25f;
                y = -1.25f;
                layerOffset = -0.001f;

                shootX = 1.1f;
                shootY = 6f;

                top = false;

                ejectEffect = Fx.casing1;

                bullet = new BasicBulletType(2.5f, 30){{
                    width = 8f;
                    height = 10f;

                    lifetime = 60f;
                }};
            }});
        }};

        echo = new EctorumUnitType("echo") {{
            constructor = LegsUnit::create;

            speed = 1f;
            drag = 0.4f;
            hitSize = 10f;
            rotateSpeed = 2.6f;
            health = 320;

            stepSound = Sounds.walkerStepSmall;
            stepSoundPitch = 1f;
            stepSoundVolume = 0.25f;

            legCount = 4;
            legLength = 8f;
            legGroupSize = 3;

            legForwardScl = 0.8f;
            legMoveSpace = 1.4f;
            hovering = true;

            shadowElevation = 0.2f;
            groundLayer = Layer.legUnit - 1f;

            weapons.add(new Weapon() {{
                x = 0f;
                y = 3f;
                shootY = -3.2f;

                mirror = false;
                rotate = false;
                reload = 45f;
                shootCone = 25f;
                shootSound = EctoSounds.shootEcho;

                shoot.shots = 3;
                shoot.shotDelay = 6;

                bullet = new ArcWaveBulletType(1.5f, 15f) {{
                    lifetime = 60f;

                    arcAngle = 28f;
                    hitWidth = 5f;

                    hitColor = waveColor.cpy().lerp(Color.white, 0.25f);
                    hitEffect = new MultiEffect(Fx.hitLiquid, Fx.regenSuppressSeek);

                    drawStartRadius = 6f;
                    drawFadeLength = 2f;

                    tipRadius = 0.4f;
                    stroke = 1.4f;

                    fadeFraction = 0.2f;
                    fadeInterp = Interp.pow3In;

                    waveColor = hitColor = Color.valueOf("82568F");

                    buildingDamageMultiplier = 0.25f;
                    status = StatusEffects.slow;
                    statusDuration = 120f;

                    collidesAir = true;
                    collidesGround = true;
                }};
            }});

            abilities.add(new HitStatusFieldAbility() {{
                weapon = weapons.get(0);
                status = StatusEffects.overclock;
                range = 6f * 8f;
                reload = 30f;
                duration = 120f;
                includeSelf = false;
                effectColor = Color.valueOf("4D3873").lerp(Color.white, 0.2f);
            }});
        }};

        spasm = new AlternateEnginedUnitType("spasm") {{


            researchCostMultiplier = 0.5f;
            speed = 2.7f;
            accel = 0.08f;
            drag = 0.04f;
            flying = true;
            health = 270;

            engineType = FlameJetEngine::new;
            engineSize = 1.13f;
            engineOffset = 5.3f;

            setEnginesMirror(
                    new FlameJetEngine(3.4f,  -3.6f, 1.08f, 315f)
            );

            faceTarget = true;
            controller = u -> !playerControllable || (u.team.isAI() && !u.team.rules().rtsAi) ? aiController.get() : new RotateMoveCommandAI();
            lowAltitude = true;
            omniMovement = false;

            targetFlags = new BlockFlag[]{BlockFlag.drill, null};

            hitSize = 9;
            itemCapacity = 10;
            rotateSpeed = 5f;
            wreckSoundVolume = 0.7f;

            moveSound = Sounds.loopThruster;
            moveSoundPitchMin = 0.3f;
            moveSoundPitchMax = 1.5f;
            moveSoundVolume = 0.2f;

            weapons.add(new Weapon() {{
                shootSound = Sounds.shootLancer;
                shootSoundVolume = 0.6f;

                y = 3f;
                x = 0f;
                shootY = 2.5f;

                shootCone = 22f;
                reload = 40f;
                shoot.shots = 3;
                shoot.shotDelay = 5f;
                mirror = false;

                bullet = new LaserBulletType() {{
                    damage = 14f;

                    inaccuracy = 4f;
                    width = 10f;
                    length = 96f;
                    lifetime = 8f;

                    shootEffect = Fx.hitLaserBlast;
                    colors = new Color[]{
                            EctoPal.spasmLaser.cpy().mul(1f, 1f, 1f, 0.4f),
                            EctoPal.spasmLaser,
                            Color.white
                    };

                    smokeEffect = Fx.none;
                    ammoMultiplier = 2;

                    pierceCap = 4;
                }};
            }});
        }};

        float coreFleeRange = 400f;

        glare = new EctorumUnitType("glare") {{
            coreUnitDock = true;
            controller = u -> new BuilderAI(true, coreFleeRange);
            isEnemy = false;
            envDisabled = 0;

            constructor = UnitEntity::create;
            flying = true;

            faceTarget = true;
            targetPriority = -2;
            lowAltitude = false;
            fogRadius = 4;

            speed = 5f;
            rotateSpeed = 8f;
            accel = 0.08f;
            drag = 0.04f;

            hitSize = 9f;
            health = 350f;
            armor = 1f;

            itemCapacity = 45;

            buildSpeed = 0.8f;

            mineTier = 1;
            mineSpeed = 4f;
            mineWalls = true;
            mineFloor = false;

            fogRadius = 5f;
            targetable = true;
            hittable = true;

            engineOffset = 7f;

            buildBeamOffset = 2f;
            mineBeamOffset = 2f;

            alwaysCreateOutline = true;

            weapons.add(new CoreProximityWeapon("ectotech-glare-weapon") {{
                top = false;
                reload = 15f;
                layerOffset = -0.001f;

                x = 4f;
                y = 2.8f;

                inactiveWeaponOffset = 0.6f;
                inactiveColor = Color.valueOf("78726F");
                inactiveColorAlpha = 0.5f;
                zoneWarmupSpeed = 0.03f;

                mirror = true;
                invert = false;

                shoot = new ShootSpread(){{
                    shots = 2;
                    shotDelay = 3f;
                    spread = 2f;
                }};

                inaccuracy = 3f;
                shootSound = Sounds.shootAlpha;

                bullet = new BasicBulletType(3.5f, 28) {{
                    scaleKeepVelocity = true;
                    width = 1.5f;
                    height = 5f;
                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    trailWidth = 1.2f;
                    trailLength = 4;
                    shootEffect = Fx.shootSmallColor;
                    smokeEffect = Fx.hitLaserColor;
                    backColor = trailColor = Pal.yellowBoltFront;
                    hitColor = Pal.yellowBoltFront;
                    frontColor = Color.white;
                    lightColor = Pal.yellowBoltFront;

                    lifetime = 70f;
                    buildingDamageMultiplier = 0.01f;
                    homingPower = 0.04f;
                }};
            }});

            abilities.add(new BuildRepairFieldAbility() {{
                maxHealPercent = 10f;
                reload = 90f;
                range = 8 * 8f;
                fullPowerRange = 8f;

                squareRad = 4.3f;
            }});
        }
            @Override
            public void drawOutline(Unit unit) {
                float z = Draw.z();

                Draw.z(z - 0.002f);
                super.drawOutline(unit);

                Draw.z(z);
            }
        };
    }
}

