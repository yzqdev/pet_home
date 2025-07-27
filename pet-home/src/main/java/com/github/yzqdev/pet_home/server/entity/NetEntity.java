package com.github.yzqdev.pet_home.server.entity;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.PHDataComponents;
import com.github.yzqdev.pet_home.server.item.NetItem;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
 

import javax.annotation.Nonnull;

public class NetEntity extends ThrowableItemProjectile {

private String entityNbt="itemNbt";
    private ItemStack itemStack = ItemStack.EMPTY;
    private boolean hasItemStack = false;
    public NetEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public NetEntity(double x, double y, double z, Level world, ItemStack newStack) {
        super(PHEntityRegistry.NET_ENTITY.get(), x, y, z, world);

        setItemStack(newStack);
    }

    @Nonnull
    @Override
    protected Item getDefaultItem() {

        // 如果有自定义物品堆，则使用其逻辑
        if (hasItemStack && !itemStack.isEmpty()) {
            return NetItem.containsEntity(itemStack)
                    ? PHItemRegistry.NET_HAS_ITEM.get()
                    : PHItemRegistry.NET_ITEM.get();
        }
        // 默认情况（通常用于序列化/反序列化）
        return PHItemRegistry.NET_ITEM.get();

    }


    /**
     * Called when this EntityThrowable hits a block or entity.
     *
     * @param result
     */
    @Override
    protected void onHit(@Nonnull HitResult result) {
        if (level().isClientSide || !this.isAlive()) return;
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
            entity.absMoveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);

            level().addFreshEntity(entity);


        } else {
            if (type == HitResult.Type.ENTITY) {
                EntityHitResult entityRayTrace = (EntityHitResult) result;
                Entity target = entityRayTrace.getEntity();
                if (!target.isAlive() || (!NetItem.canCatchMob(target))) {
                    return;
                }

                CompoundTag nbt = NetItem.getNBTfromEntity(target);
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM.get());
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
        this.itemStack = stack.copy(); // 确保使用副本
        this.hasItemStack = true;
        // 更新父类中的物品引用
        this.setItem(this.itemStack);
    }
    protected ItemEntity createDroppedItemAtEntity(Entity entity, ItemStack stack) {
        return new ItemEntity(this.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
    }

    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
//        if (!stack.isEmpty()) {
//            nbt.put(MobCatcher.MODID, stack.save(level().registryAccess()));
//        }
        if (hasItemStack) {
            nbt.put(entityNbt, itemStack.save(level().registryAccess()));
             
        }

    }

    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        itemStack = ItemStack.parse(level().registryAccess(), nbt.getCompound(PetHomeMod.MODID)).get();
        if ( nbt.contains(entityNbt, Tag.TAG_COMPOUND)) {
            ItemStack savedStack = ItemStack.parse(level().registryAccess(),nbt.getCompound(entityNbt)).get();
            setItemStack(savedStack);
        } else {
            hasItemStack = false;
        }
    }


}