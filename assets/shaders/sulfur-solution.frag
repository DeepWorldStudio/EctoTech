#define HIGHP
#define NSCALE 100.0

uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

/* Вычисляет воспринимаемую яркость исходного цвета. */
float luminance(vec3 color){
    return dot(color, vec3(0.299, 0.587, 0.114));
}

/* Уменьшает насыщенность, не подменяя исходный оттенок. */
vec3 desaturate(vec3 color, float amount){
    float value = luminance(color);
    return mix(color, vec3(value), amount);
}

void main(){
    /*
     * Мировые координаты пикселя.
     * Благодаря этому разводы остаются привязаны к поверхности мира,
     * а не двигаются вместе с камерой.
     */
    vec2 coords = v_texCoords * u_resolution + u_campos;

    /*
     * Очень медленное движение noise-текстуры.
     * Значение взято из логики ванильного slag.frag.
     */
    float btime = u_time / 5000.0;

    /* Неискажённая выборка нужна для сохранения границ. */
    vec4 original = texture2D(u_texture, v_texCoords);

    /*
     * Два noise-слоя движутся в противоположных направлениях.
     * Их среднее образует крупные подвижные области раствора.
     */
    float noiseA = texture2D(
            u_noise,
            coords / NSCALE + vec2(btime) * vec2(-0.9, 0.8)
    ).r;

    float noiseB = texture2D(
            u_noise,
            coords / NSCALE + vec2(btime * 1.1) * vec2(0.8, -1.0)
    ).r;

    float noise = (noiseA + noiseB) / 2.0;

    /*
     * Слабое вязкое искажение поверхности.
     * Это почти оригинальная формула slag.frag.
     */
    vec2 distortedCoords = v_texCoords + (
    vec2(
            texture2D(
                    u_noise,
                    coords / 170.0 + vec2(btime) * vec2(-0.9, 0.8)
            ).r,

            texture2D(
                    u_noise,
                    coords / 170.0 + vec2(btime * 1.1) * vec2(0.8, -1.0)
            ).r
    ) - vec2(0.5)
    ) * 8.0 / u_resolution;

    vec4 sampled = texture2D(u_texture, distortedCoords);

    /*
     * Если искажение вышло на прозрачный край слоя,
     * используем исходный пиксель.
     * Это предотвращает тёмные разрывы по границам.
     */
    if(sampled.a < 0.95){
        sampled = original;
    }

    vec3 baseColor = sampled.rgb;
    float brightness = luminance(baseColor);

    /*
     * Широкая внешняя область развода.
     * Она темнее и менее насыщенная, но сохраняет оттенок исходного тайла.
     *
     * Чем исходный цвет светлее, тем сильнее его можно затемнить,
     * не превращая тёмные варианты пола в чёрные пятна.
     */
    float outerBrightness = mix(0.88, 0.78, brightness);
    vec3 outerColor = desaturate(baseColor, 0.35) * outerBrightness;

    /*
     * Внутренняя область.
     * Она высветляется в сторону белого.
     * Тёмные варианты пола высветляются сильнее, светлые — слабее.
     */
    float whitening = mix(0.28, 0.14, brightness);
    vec3 innerColor = mix(baseColor, vec3(1.0), whitening);
    innerColor = min(innerColor * 1.06, vec3(1.0));

    /*
     * Мягкие маски вместо резких порогов.
     *
     * outerMask создаёт широкое тусклое пятно.
     * innerMask — более узкую светлую сердцевину.
     */
    float outerMask = smoothstep(0.50, 0.59, noise);
    float innerMask = smoothstep(0.62, 0.70, noise);

    /*
     * Числа 0.55 и 0.72 — непрозрачность цветовых слоёв.
     *
     * Это эквивалентно наложению полупрозрачных пятен
     * поверх исходного цвета тайла.
     */
    vec3 color = mix(baseColor, outerColor, outerMask * 0.55);
    color = mix(color, innerColor, innerMask * 0.72);

    /*
     * Сохраняем исходную альфу поверхности.
     * Полупрозрачность разводов уже обеспечена функцией mix().
     */
    gl_FragColor = vec4(color, sampled.a);
}