package net.tucas.sculkeritegreatsword.entity.custom;

/**
 * Todos los valores ajustables del Peeker en un solo sitio.
 * Cambia balance/comportamiento aquí sin tocar la lógica de las entidades.
 */
public final class PeekerConstants {

    private PeekerConstants() {}

    // Textura por defecto de cada variante (debe coincidir con el .png en textures/entity/)
    public static final String TEXTURE_WILD = "peeker";
    public static final String TEXTURE_SADDLED = "peeker_saddle";
    public static final String TEXTURE_EXPLODE_WILD = "peeker_explode";
    public static final String TEXTURE_EXPLODE_SADDLED = "peeker_explode_saddle";

    // Movimiento / atributos
    public static final double MOVEMENT_SPEED = 0.1D;
    public static final double MAX_HEALTH = 25.0D;
    public static final double FOLLOW_RANGE = 16.0D;
    public static final double ATTACK_KNOCKBACK = 0.5D;
    public static final float BASE_ENTITY_SCALE = 1.0F;
    public static final float MAX_STEP_HEIGHT = 4.0F; // puede subir hasta 4 bloques de golpe

    // IA
    public static final float AVOID_PLAYER_DISTANCE = 20.0F;
    public static final double AVOID_PLAYER_SPEED_WALK = 1.0D;
    public static final double AVOID_PLAYER_SPEED_SPRINT = 1.0D;
    public static final float LOOK_AT_PLAYER_RANGE = 100.0F;
    public static final double PANIC_SPEED = 1.2D;
    public static final double STROLL_SPEED = 1.0D;

    // Muerte / explosión
    public static final int DEATH_ANIMATION_TICKS = 25;
    public static final int EXPLOSION_PARTICLE_MIN = 6;
    public static final int EXPLOSION_PARTICLE_EXTRA = 10;
    public static final int CRIT_PARTICLE_MIN = 10;
    public static final int CRIT_PARTICLE_EXTRA = 10;
    public static final double EXPLOSION_DAMAGE_RADIUS = 3.0D;
    public static final float EXPLOSION_DAMAGE_MIN = 8.0F;
    public static final float EXPLOSION_DAMAGE_MAX = 14.0F;

    // Partículas de baja vida
    public static final float LOW_HEALTH_THRESHOLD = 8.0F;
    public static final int LOW_HEALTH_CHANCE_ONE_IN = 10; // 1 de cada 10 ticks elegibles
    public static final int LOW_HEALTH_PARTICLE_MIN = 4;
    public static final int LOW_HEALTH_PARTICLE_EXTRA = 6;

    // Sonidos (nombres de RegistryObject<SoundEvent>, deben existir registrados)
    public static final String SOUND_IDLE = "peeker_idle";
    public static final String SOUND_HURT = "peeker_hurt";
    public static final String SOUND_EXPLODE = "peeker_explode";
    public static final String SOUND_IDLE_BABY = "baby_peeker_idle";
    public static final String SOUND_HURT_BABY = "baby_peeker_hurt";

    // Animaciones GeckoLib (deben llamarse igual en el .animation.json)
    public static final String ANIM_IDLE = "idle";
    public static final String ANIM_WALK = "walk";
    public static final String ANIM_EXPLODE = "explode";

    // Montado (solo variante ensillada)
    public static final float SADDLED_RIDER_PITCH_FACTOR = 0.5F;
    public static final double SADDLE_RIDER_Y_OFFSET = 1.1D;

    // Domesticación (tú me dirás el ítem exacto más adelante; de momento queda
    // como punto de extensión en PeekerEntity#isTameItem)
    public static final int TAME_CHANCE_ONE_IN = 10; // 1 de cada 3 intentos domestica (igual que el lobo vanilla)

    // Silla
    public static final int SADDLE_EQUIP_SOUND_PITCH_VARIANCE = 0; // 1.0F fijo, sin variación aleatoria por ahora
}