#define HIGHP

uniform sampler2D u_texture;
uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform vec2 u_screenSize;
uniform float u_time;
uniform vec3 u_waterColor;

varying vec4 v_color;
varying vec2 v_texCoords;

const float mscl = 40.0;
const float mth = 7.0;

void main(){
    // 1. Маска: сэмплим ровно по UV спрайта, без сдвига — края не искажаются
    vec4 mask = texture2D(u_texture, v_texCoords);
    if(mask.a < 0.02) discard;

    // 2. Мировые координаты для бликов
    //    gl_FragCoord — экранные пиксели
    //    переводим в мировые через камеру (как это делает ванильный water.frag)
    vec2 screenUV = gl_FragCoord.xy / u_screenSize;
    vec2 coords = screenUV * u_resolution + u_campos;

    float stime = u_time / 5.0;

    // 3. Цвет воды с учётом теней маски
    vec3 color = u_waterColor * mask.rgb;

    // 4. Блики — точная копия формулы из water.frag
    float tester = mod(
            (coords.x + coords.y * 1.1
            + sin(stime / 8.0 + coords.x / 5.0 - coords.y / 100.0) * 2.0
            + sin(stime / 20.0 + coords.y / 3.0) * 1.0
            + sin(stime / 10.0 - coords.y / 2.0) * 2.0
            + sin(stime / 7.0 + coords.y / 1.0) * 0.5
            + sin(coords.x / 3.0 + coords.y / 2.0)
            + sin(stime / 20.0 + coords.x / 4.0) * 1.0),
            mscl
    );

    if(tester < mth){
        color *= 1.2;
    }

    // 5. Итог: mask.a = форма, v_color.a = динамическая прозрачность
    gl_FragColor = vec4(color, mask.a * v_color.a);
}