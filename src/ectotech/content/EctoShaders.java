package ectotech.content;

import arc.Core;
import arc.graphics.gl.Shader;
import arc.util.Time;
import mindustry.Vars;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Shaders;

public class EctoShaders {

    public static CacheLayer quicksandHeat;

    public static GeyserWaterShader geyserWater;

    public static void load() {
        if (Vars.headless) return;

        quicksandHeat = new CacheLayer.ShaderLayer(
                new Shaders.SurfaceShader("quicksand")
        );

        CacheLayer.add(quicksandHeat);

        arc.util.Log.info("[GeyserShader] default.vert exists: @", Vars.tree.get("shaders/default.vert").exists());
        arc.util.Log.info("[GeyserShader] screenspace.vert exists: @", Vars.tree.get("shaders/screenspace.vert").exists());
        arc.util.Log.info("[GeyserShader] geyser-water.frag exists: @", Vars.tree.get("shaders/geyser-water.frag").exists());

        geyserWater = new GeyserWaterShader();

        arc.util.Log.info("[GeyserShader] compiled successfully: @", geyserWater.isCompiled());
        arc.util.Log.info("[GeyserShader] log: @", geyserWater.getLog());
    }

    public static class GeyserWaterShader extends Shader {
        public arc.graphics.Color waterColor = arc.graphics.Color.valueOf("4a9eff");

        public GeyserWaterShader() {
            super(
                    Vars.tree.get("shaders/default.vert"),
                    Vars.tree.get("shaders/geyser-water.frag")
            );
        }

        @Override
        public void apply() {
            setUniformf("u_time", Time.time);
            setUniformf("u_waterColor", waterColor.r, waterColor.g, waterColor.b);
            setUniformf("u_campos",
                    Core.camera.position.x - Core.camera.width / 2f,
                    Core.camera.position.y - Core.camera.height / 2f
            );
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_screenSize", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }
}