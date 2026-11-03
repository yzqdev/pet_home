package com.github.yzqdev.pethome.server.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/**
 * 水生宠物（驯服美西螈/青蛙等）的移动控制器：MOVE_TO 时把身体朝目标方向平滑转向，
 * 水平速度按 0.4 倍缩放并按目标高度差附加垂直速度分量，实现平滑游动；非移动时速度归零。
 */
public class AquaticMoveControl extends MoveControl {
    private final Mob entity;

    public AquaticMoveControl(Mob entity) {
        super(entity);
        this.entity = entity;
    }

    /** 每 tick 移动控制：朝目标方向平滑转向，并按比例设置水平与垂直速度。 */
    public void tick() {
        if (this.operation == Operation.MOVE_TO && !entity.getNavigation().isDone()) {
            double d0 = this.wantedX - this.entity.getX();
            double d1 = this.wantedY - this.entity.getY();
            double d2 = this.wantedZ - this.entity.getZ();
            double d3 = (double) Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
            // 高度差归一化，得到垂直方向的游动分量
            d1 /= d3;
            float f = (float)(Mth.atan2(d2, d0) * 57.2957763671875D) - 90.0F;
            this.entity.setYRot(this.rotlerp(this.entity.getYRot(), f, this.entity.getMaxHeadYRot()));
            this.entity.yBodyRot = this.entity.getYRot();
            float speed =(float)(this.speedModifier * this.entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 3.0F);
            // 水平速度按 0.4 倍缩放
            this.entity.setSpeed(speed * 0.4F);
            // 垂直速度按 0.6 倍叠加，实现水中爬升/下潜
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0.0D, (double)this.entity.getSpeed() * d1 * 0.6D, 0.0D));
        } else {
            this.entity.setSpeed(0.0F);
        }
    }
}
