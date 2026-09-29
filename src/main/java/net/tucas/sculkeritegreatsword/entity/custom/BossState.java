package net.tucas.sculkeritegreatsword.entity.custom;

import software.bernie.geckolib.core.animation.RawAnimation;

/** Estado sincronizado del jefe. Cada estado apunta a una animación de GeckoLib. */
public enum BossState {
    SLEEP("sleep", true, 0),
    WAKING_UP("roar", false, 40),
    IDLE("idle", true, 0),
    DAZED("dazed", true, 100),
    PULLING("roar", false, 40),
    CANNON_SHOT("cannon_shot", false, 35),
    SPIN_START("spin_start", false, 23),
    SPIN("spin", true, 70),          // ~4 loops de 0.875 s
    SPIN_END("spin_end", false, 20),
    EARTHQUAKE("earthquake", false, 30),
    BEAM_START("beam_start", false, 25),
    BEAM("beam", true, 40),          // 2 loops de 1 s
    BEAM_END("beam_end", false, 25);

    private final RawAnimation animation;
    private final int baseTicks;

    BossState(String name, boolean loop, int baseTicks) {
        String full = ForgottenConstructEntity.ANIM_PREFIX + name;
        // Las animaciones "play once" se quedan en el último frame hasta que cambie el estado.
        this.animation = loop ? RawAnimation.begin().thenLoop(full) : RawAnimation.begin().thenPlayAndHold(full);
        this.baseTicks = baseTicks;
    }

    public RawAnimation getAnimation() { return animation; }
    public int getBaseTicks() { return baseTicks; }

    public boolean isMelee() { return this == EARTHQUAKE || this == SPIN_START || this == SPIN || this == SPIN_END; }
    public boolean isRanged() { return this == CANNON_SHOT || this == BEAM_START || this == BEAM || this == BEAM_END; }
    public boolean isAttack() { return isMelee() || isRanged(); }
    /** Estados que encienden la capa "overpower". */
    public boolean usesOverpower() { return isRanged() || this == PULLING; }

    public static BossState byId(int id) {
        BossState[] v = values();
        return id >= 0 && id < v.length ? v[id] : SLEEP;
    }
}
