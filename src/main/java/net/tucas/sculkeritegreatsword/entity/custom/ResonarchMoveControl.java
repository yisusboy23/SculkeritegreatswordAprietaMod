package net.tucas.sculkeritegreatsword.entity.custom;

import net.minecraft.world.entity.ai.control.MoveControl;

public class ResonarchMoveControl extends MoveControl {

    private final ResonarchEntity resonarch;

    public ResonarchMoveControl(ResonarchEntity resonarch) {
        super(resonarch);
        this.resonarch = resonarch;
    }

    @Override
    public void tick() {

        if (this.operation != MoveControl.Operation.MOVE_TO) {
            this.resonarch.setDeltaMovement(
                    this.resonarch.getDeltaMovement().scale(0.8D)
            );
            return;
        }

        double dx = this.wantedX - this.resonarch.getX();
        double dy = this.wantedY - this.resonarch.getY();
        double dz = this.wantedZ - this.resonarch.getZ();

        double distanceSq = dx * dx + dy * dy + dz * dz;

        if (distanceSq < 0.0225D) {
            this.operation = MoveControl.Operation.WAIT;

            this.resonarch.setDeltaMovement(
                    this.resonarch.getDeltaMovement().scale(0.7D)
            );

            return;
        }

        double distance = Math.sqrt(distanceSq);

        dx /= distance;
        dy /= distance;
        dz /= distance;

        double speed = 0.12D * this.speedModifier;

        this.resonarch.setDeltaMovement(
                dx * speed,
                dy * speed,
                dz * speed
        );
    }
}