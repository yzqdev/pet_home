package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.NotNull;

public class NetEntity extends ThrowableItemProjectile {

    private String entityNbt = "itemNbt";
    private ItemStack itemStack = ItemStack.EMPTY;
    private boolean hasItemStack = false;

    public NetEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public NetEntity(double x, double y, double z, Level world, ItemStack newStack) {
        super(PHEntityRegistry.NET_ENTITY, x, y, z, world, newStack);

        setItemStack(newStack);
    }

    @NotNull
    @Override
    protected Item getDefaultItem() {

        // 如果有自定义物品堆，则使用其逻辑
        if (hasItemStack && !itemStack.isEmpty()) {
            return NetItem.containsEntity(itemStack)
                    ? PHItemRegistry.NET_HAS_ITEM
                    : PHItemRegistry.NET_ITEM;
        }
        // 默认情况（通常用于序列化/反序列化）
        return PHItemRegistry.NET_ITEM;

    }


    /**
     * Called when this EntityThrowable hits a block or entity.
     *
     * @param result
     */
    @Override
    protected void onHit(@NotNull HitResult result) {
        if (level().isClientSide() || !this.isAlive()) {
            return;
        }
        HitResult.Type type = result.getType();
        boolean containsEntity = NetItem.containsEntity(itemStack);
        if (containsEntity) {
            Entity entity = NetItem.getEntityFromStack(itemStack, level(), true);
            BlockPos pos;
            if (type == HitResult.Type.ENTITY) {
                pos = ((EntityHitResult) result).getEntity().blockPosition();
            } else {
                pos = ((BlockHitResult) result).getBlockPos();
            }
            entity.snapTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);

            level().addFreshEntity(entity);


        } else {
            if (type == HitResult.Type.ENTITY) {
                EntityHitResult entityRayTrace = (EntityHitResult) result;
                Entity target = entityRayTrace.getEntity();
                if (!target.isAlive() || (!NetItem.canCatchMob(target))) {
                    return;
                }

                CompoundTag nbt = NetItem.getNBTfromEntity(target);
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM);
                newStack.set(PHDataComponents.ENTITY_HOLDER, nbt);
                ItemEntity itemEntity = createDroppedItemAtEntity(target, newStack);
                level().addFreshEntity(itemEntity);
                target.discard();
            } else {
                ItemEntity emptynet = createDroppedItemAtEntity(this, itemStack.copy());
                level().addFreshEntity(emptynet);

            }
        }

        this.discard();
    }

    // 设置自定义物品堆并标记
    public void setItemStack(ItemStack stack) {
        // 确保使用副本
        this.itemStack = stack.copy();
        this.hasItemStack = true;
        // 更新父类中的物品引用
        this.setItem(this.itemStack);
    }

    protected ItemEntity createDroppedItemAtEntity(Entity entity, ItemStack stack) {
        return new ItemEntity(this.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.saveWithoutId(output);
        if (hasItemStack) {
            output.store(entityNbt, ItemStack.CODEC, itemStack);

        }

    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.load(input);
        var stackOpt = input.read(entityNbt, ItemStack.CODEC);
        if (stackOpt.isEmpty()) {
            hasItemStack = false;
        } else {
            setItemStack(stackOpt.get());
        }
    }


}
