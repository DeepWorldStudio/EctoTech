#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

const float mscl = 52.0;
const float mth = 5.0;

void main(){
    vec2 c = v_texCoords;
    vec2 px = vec2(1.0 / u_resolution.x, 1.0 / u_resolution.y);

    vec2 coords = vec2(
            c.x / px.x + u_campos.x,
            c.y / px.y + u_campos.y
    );

    /*
     * Медленнее воды Anuken'а:
     * у воды stime = u_time / 5.0,
     * здесь делаем заметно спокойнее.
     */
    float stime = u_time / 18.0;

    /*
     * Ослабленное смещение, в основном по X.
     * Это буквально "water.frag, но слабее".
     */
    vec4 sampled = texture2D(
            u_texture,
            c + vec2(
                    sin(stime / 3.2 + coords.y / 1.35) * px.x * 0.42 +
                    sin(stime / 8.0 + coords.x / 9.0) * px.x * 0.18,
                    0.0
            )
    );

    /*
     * Базовое затемнение и песочный тон.
     * Делает пол визуально отличимым от обычного песка издалека.
     */
    vec3 color = sampled.rgb * vec3(0.98, 1, 0.99);

    /*
     * Слабые "переломы" поверхности по образцу water.frag,
     * но без водяного блеска и без синевы.
     */
    float tester = mod(
            (coords.x + coords.y * 1.08
            + sin(stime / 7.0 + coords.x / 6.0 - coords.y / 120.0) * 1.3
            + sin(stime / 16.0 + coords.y / 4.2) * 0.9
            + sin(stime / 11.0 - coords.y / 2.6) * 1.1
            + sin(coords.x / 4.0 + coords.y / 2.4) * 0.7),
            mscl
    );

    if(tester < mth){
        color *= 1.08;
    }else if(tester > mscl - 3.0){
        color *= 0.93;
    }

    gl_FragColor = vec4(color.rgb, sampled.a);
}