package ectotech.content;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
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
import mindustry.entities.Effect;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.meta.BlockFlag;


public class EctoUnitTypes {
    public static UnitType

            // mech
            fist, sickle, hammer,

            // legs
            echo, whisper,

            // air
            discharge, crisis,

            // air, core
            glare, flake;

    public static void load() {

        fist = new EctorumUnitType("fist") {{
            constructor = MechUnit::create;

            researchCostMultiplier = 0f;

            speed = 0.5f;
            hitSize = 9f;
            health = 550;
            stepSoundVolume = 0.4f;

            alwaysCreateOutline = false;

            weapons.add(new Weapon("ectotech-fist-weapon") {{
                reload = 0.4f * 60f;
                x = 4.25f;
                y = -1.25f;
                layerOffset = -0.001f;

                shootX = 1.1f;
                shootY = 4.4f;

                top = false;

                ejectEffect = Fx.casing1;

                bullet = new BasicBulletType(4f, 30) {{
                    width = 5f;
                    height = 12f;

                    trailLength = 4;
                    trailWidth = 1f;

                    shootEffect = Fx.shootSmall;
                    despawnEffect = Fx.hitBulletColor;

                    backColor = trailColor = Color.valueOf("ca5e65");
                    frontColor = Color.valueOf("e28d87");
                    lightColor = Pal.powerLight;

                    lifetime = 33f;
                }};
            }});

            outlineColor = EctoPal.ectorumMechOutline;
        }};

        sickle = new EctorumUnitType("sickle") {{
            constructor = MechUnit::create;

            researchCostMultiplier = 0.3f;

            speed = 0.35f;
            hitSize = 11f;
            health = 1350;
            stepSoundVolume = 0.4f;

            alwaysCreateOutline = false;

            weapons.add(new Weapon("ectotech-sickle-weapon") {{
                reload = 60f;
                recoil = 2f;
                shootSound = Sounds.shootScepter;
                shootSoundVolume = 0.9f;
                soundPitchMin = 0.7f;
                soundPitchMax = 0.82f;
                x = 5.5f;
                layerOffset = -0.001f;

                shootX = 0.4f;
                shootY = 5f;

                top = false;
                alternate = false;

                ejectEffect = Fx.casing2;

                bullet = new BasicBulletType(8f, 100, "mine-bullet") {{
                    width = height = 11f;

                    trailLength = 7;
                    trailWidth = 2.2f;

                    shrinkY = 0f;
                    spin = 480f / 60f;
                    trailSinMag = 0.18f;
                    trailSinScl = 2f;

                    trailInterval = 3f;
                    trailEffect = Fx.none;
                    trailRotation = true;
                    trailSpread = 1f;

                    shootEffect = Fx.sparkShoot;

                    hitColor = Color.valueOf("e28d87");
                    hitEffect = despawnEffect = new MultiEffect(Fx.hitSquaresColor, Fx.hitBulletSmall);
                    pierce = true;
                    pierceCap = 3;
                    pierceBuilding = true;

                    backColor = trailColor = Color.valueOf("ca5e65");
                    frontColor = Color.valueOf("e28d87");
                    lightColor = Pal.powerLight;

                    hitShake = 2f;
                    knockback = 1.5f;

                    lifetime = 18f;
                }};
            }});


            outlineColor = EctoPal.ectorumMechOutline;
        }};

        echo = new EctorumUnitType("echo") {{
            constructor = LegsUnit::create;

            speed = 1f;
            drag = 0.4f;
            hitSize = 10f;
            rotateSpeed = 2.6f;
            health = 350;

            stepSound = Sounds.walkerStepTiny;
            stepSoundPitch = 1f;
            stepSoundVolume = 0.25f;

            legCount = 4;
            legLength = 8f;
            legGroupSize = 2;

            legForwardScl = 0.8f;
            legMoveSpace = 1.4f;
            hovering = true;

            shadowElevation = 0.2f;
            groundLayer = Layer.legUnit - 1f;

            weapons.add(new Weapon() {{
                x = 0f;
                y = 0f;

                shootX = 0f;
                shootY = 0f;

                targetAir = false;
                mirror = false;
                rotate = false;
                reload = 45f;
                shootCone = 25f;
                shootSound = EctoSounds.shootEcho;

                shoot.shots = 3;
                shoot.shotDelay = 6;

                bullet = new ArcWaveBulletType(1.5f, 10f) {{
                    keepVelocity = scaleKeepVelocity = false;

                    lifetime = 60f;

                    arcAngle = 28f;
                    hitWidth = 5f;

                    waveColor = Color.valueOf("82568F");
                    hitColor = waveColor.cpy().lerp(Color.white, 0.25f);
                    hitEffect = new MultiEffect(Fx.hitLiquid, Fx.regenSuppressSeek);
                    despawnEffect = Fx.none;
                    shootEffect = Fx.none;
                    smokeEffect = Fx.none;

                    drawStartRadius = 6f;
                    drawFadeLength = 2f;

                    stroke = 1.4f;

                    fadeFraction = 0.2f;
                    fadeInterp = Interp.pow3In;

                    reflect = true;
                    shieldAbsorb = true;
                    applyStatusThroughUnitShield = false;
                    immuneShooter = true;
                    immuneReflectorTeam = false;

                    status = StatusEffects.slow;
                    statusDuration = 120f;

                    collidesAir = false;
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

            outlineColor = EctoPal.ectorumSpiderOutline;
        }};

        whisper = new EctorumUnitType("whisper") {{
            constructor = LegsUnit::create;

            speed = 0.6f;
            drag = 0.4f;
            hitSize = 13f;
            rotateSpeed = 4f;
            health = 1050;

            stepSound = Sounds.walkerStepSmall;
            stepSoundPitch = 1f;
            stepSoundVolume = 0.25f;

            legCount = 6;
            legLength = 11f;
            legGroupSize = 3;

            legForwardScl = 0.8f;
            legMoveSpace = 1.4f;
            hovering = true;
            faceTarget = true;

            shadowElevation = 0.2f;
            groundLayer = Layer.legUnit - 1f;

            weapons.add(new Weapon() {{
                x = 5.5f;
                y = -3.5f;

                targetAir = false;
                mirror = true;
                alternate = false;
                rotate = false;
                baseRotation = 240f;
                shootCone = 180f;
                reload = 1.2f * 60f;
                recoil = 2f;

                shoot = new ShootSpread() {{
                    shots = 3;
                    shotDelay = 0.04f * 60f;
                    spread = 3f;
                }};

                inaccuracy = 8f;
                velocityRnd = 0.25f;

                bullet = new BasicBulletType(5f, 45f, "large-orb") {{
                    width = 10f;
                    height = 10f;
                    shrinkY = shrinkX = 0f;
                    shootEffect = smokeEffect = despawnEffect = Fx.none;

                    frontColor = Color.valueOf("f1d4ff");
                    backColor = Color.valueOf("9b5cb2");
                    trailColor = Color.valueOf("9b5cb2");
                    trailLength = 3;
                    trailWidth = 2.5f;

                    drag = 0.08f;
                    lifetime = 75f;

                    homingPower = 0.04f;
                    homingDelay = 0f;
                    followAimSpeed = 1f;

                    pierce = false;
                    hitEffect = Fx.hitLaserBlast;

                    shootSound = Sounds.shootMerui;
                    hitShake = 1.5f;

                    fragOnHit = fragOnAbsorb = false;
                    setDefaults = false;

                    collidesAir = false;

                    fragVelocityMin = fragVelocityMax = 0f;

                    fragOnDespawn = true;
                    fragBullets = 1;
                    fragOffsetMax = fragOffsetMin = 0f;

                    fragBullet = new BasicBulletType(0f, 0f, "large-orb") {{
                        width = 10f;
                        height = 10f;
                        shrinkY = shrinkX = 0f;

                        frontColor = Color.valueOf("f1d4ff");
                        backColor = Color.valueOf("9b5cb2");

                        lifetime = 4f * 60f;

                        collidesAir = false;
                        despawnHit = true;
                        splashDamage = 110f;
                        splashDamageRadius = 3f * 8f;
                        hitEffect = Fx.sapExplosion;
                        despawnSound = Sounds.explosionArtilleryShock;
                        hitShake = 1.5f;

                        shootEffect = smokeEffect = despawnEffect = Fx.none;

                        bulletInterval = 65f;
                        intervalDelay = 60f;
                        intervalBullets = 1;

                        intervalBullet = new ArcWaveBulletType(1.6f, 15f) {{
                            arcAngle = 360f;
                            lifetime = 20f;

                            fadeFraction = 0.3f;
                            fadeInterp = Interp.pow2In;

                            waveColor = Color.valueOf("82568F");
                            hitColor = waveColor.cpy().lerp(Color.white, 0.25f);

                            hitEffect = new MultiEffect(Fx.hitLiquid, Fx.regenSuppressSeek);
                            despawnEffect = Fx.none;
                            shootEffect = Fx.none;
                            smokeEffect = Fx.none;

                            shootSoundVolume = 0.7f;

                            reflect = false;
                            shieldAbsorb = true;

                            hitWidth = 2f;
                            drawFadeLength = 2f;

                            collidesAir = false;
                            collidesGround = true;
                        }};
                    }};
                }};
            }});

            parts.addAll(new RegionPart("") {{
                drawRegion = false;
                heatColor = Color.valueOf("e0afff");
                heatProgress = PartProgress.warmup.mul(0.7f).add(PartProgress.smoothReload);
                heatLayerOffset = 0.001f;
            }});

            outlineColor = EctoPal.ectorumSpiderOutline;
        }};

        discharge = new AlternateEnginedUnitType("discharge") {{
            researchCostMultiplier = 0.5f;
            speed = 2.7f;
            accel = 0.08f;
            drag = 0.04f;
            flying = true;
            health = 400;

            engineType = FlameJetEngine::new;
            engineSize = 1.13f;
            engineOffset = 5.3f;

            setEnginesMirror(
                    new FlameJetEngine(3.4f, -3.6f, 1.08f, 315f)
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

                shootCone = 45f;
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
                    colors = new Color[] {
                            EctoPal.spasmLaser.cpy().mul(1f, 1f, 1f, 0.4f),
                            EctoPal.spasmLaser,
                            Color.white
                    };

                    smokeEffect = Fx.none;
                    ammoMultiplier = 2;

                    pierceCap = 4;
                    pierceDamageFactor = 0.75f;
                }};
            }});

            outlineColor = EctoPal.ectorumAirOutline;
        }};

        crisis = new AlternateEnginedUnitType("crisis") {{
            Effect hfx = Fx.flakExplosion;
            Effect dfx = Fx.legDestroy;
            Sound dsd = Sounds.explosion;

            researchCostMultiplier = 0.5f;
            speed = 2f;
            rotateSpeed = 2.4f;
            hitSize = 11f;

            accel = 0.12f;
            drag = 0.1f;
            flying = true;
            health = 1150;
            armor = 2f;

            circleTarget = true;
            omniMovement = false;
            circleTargetRadius = 70f;

            autoDropBombs = true;
            targetAir = false;
            range = 140f;

            itemCapacity = 0;
            targetFlags = new BlockFlag[]{BlockFlag.factory, null};

            moveSound = Sounds.loopThruster;
            moveSoundPitchMin = 0.6f;
            moveSoundVolume = 0.4f;

            engineOffset = 6.5f;
            engineSize = 3f;

            weapons.add(new Weapon() {{
                x = -6f;
                y = 4f;

                minShootVelocity = 1f;

                mirror = true;
                rotate = false;
                reload = 0.4f * 60f;

                baseRotation = 180f;
                shootCone = 180f;
                inaccuracy = 4f;

                shootSound = Sounds.shootHorizon;
                ejectEffect = Fx.none;
                soundPitchMax = 1.2f;

                bullet = new BasicBulletType(0.3f, 15f, "large-bomb") {{
                    keepVelocity = false;

                    trailEffect = Fx.artilleryTrail;
                    trailInterval = 4f;
                    trailColor = backColor;
                    shrinkX = 0.15f;
                    shrinkY = 0.7f;
                    shrinkInterp = Interp.slope;
                    hitShake = 1f;
                    collidesTiles = false;
                    collides = false;
                    collidesAir = false;
                    trailLength = 22;
                    trailWidth = 1.8f;

                    width = 10f;
                    height = 14f;

                    fragOnDespawn = true;
                    fragOnHit = false;

                    fragAngle = 0f;
                    fragSpread = 0f;
                    fragRandomSpread = 5f;

                    hitEffect = hfx;

                    despawnEffect = dfx;
                    despawnSound = dsd;

                    shootEffect = smokeEffect = Fx.none;

                    lifetime = 0.5f * 60f;

                    status = StatusEffects.blasted;
                    statusDuration = 60f;

                    fragVelocityMin = 0.6f;
                    fragVelocityMax = 0.8f;

                    splashDamage = 70f;
                    splashDamageRadius = 2f * 8f;

                    fragBullets = 1;
                    fragBullet = new ArtilleryBulletType(0.8f, 15f, "large-bomb") {{
                        scaleKeepVelocity = true;

                        width = 10f;
                        height = 14f;

                        fragOnDespawn = true;
                        fragOnHit = false;

                        fragAngle = 0f;
                        fragSpread = 0f;
                        fragRandomSpread = 2f;

                        hitEffect = hfx;

                        despawnEffect = dfx;
                        despawnSound = dsd;

                        lifetime = 0.9f * 60f;

                        status = StatusEffects.blasted;
                        statusDuration = 60f;

                        fragVelocityMin = 0.6f;
                        fragVelocityMax = 0.8f;

                        splashDamage = 55f;
                        splashDamageRadius = 1.1f * 8f;

                        fragBullets = 1;
                        fragBullet = new ArtilleryBulletType(0.6f, 15f, "large-bomb") {{
                            scaleKeepVelocity = true;
                            velocityScaleRandMax = 0.9f;
                            velocityScaleRandMin = 0.65f;

                            width = 10f;
                            height = 14f;

                            fragOnDespawn = false;
                            fragOnHit = false;

                            hitEffect = hfx;

                            fragAngle = 0f;
                            fragSpread = 0f;
                            fragRandomSpread = 2f;

                            despawnEffect = dfx;
                            despawnSound = dsd;

                            lifetime = 0.7f * 60f;

                            status = StatusEffects.blasted;
                            statusDuration = 60f;

                            splashDamage = 45f;
                            splashDamagePierce = true;
                            splashDamageRadius = 3f * 8f;
                        }};
                    }};
                }};

            }});

            outlineColor = EctoPal.ectorumAirOutline;
        }};

        float coreFleeRange = 400f;

        glare = new EctorumUnitType("glare") {
            {
                coreUnitDock = true;
                controller = u -> new BuilderAI(true, coreFleeRange);
                isEnemy = false;
                envDisabled = 0;

                constructor = UnitEntity::create;
                flying = true;

                faceTarget = true;
                targetPriority = -2;
                lowAltitude = false;
                fogRadius = 0;

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
                targetable = true;
                hittable = true;

                engineOffset = 7f;

                buildBeamOffset = 2f;
                mineBeamOffset = 2f;

                alwaysCreateOutline = true;

                weapons.add(new CoreProximityWeapon("ectotech-glare-weapon") {{
                    top = false;
                    reload = 45f;
                    layerOffset = -0.001f;

                    x = 4f;
                    y = 2.8f;

                    shootX = -1f;
                    shootY = 2.75f;

                    inactiveWeaponOffset = 0.6f;
                    inactiveColor = Color.valueOf("78726F");
                    inactiveColorAlpha = 0.5f;
                    zoneWarmupSpeed = 0.03f;

                    mirror = true;
                    invert = false;

                    shoot = new ShootSpread() {{
                        shots = 3;
                        shotDelay = 5f;
                        spread = 2f;
                    }};

                    inaccuracy = 3f;
                    shootSound = Sounds.shootAlpha;

                    bullet = new BasicBulletType(3.5f, 20) {{
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

                        lifetime = 50f;
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

                outlineColor = EctoPal.ectorumCoreUnitOutline;
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

