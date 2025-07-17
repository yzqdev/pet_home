package com.github.yzqdev.pet_home.util;

import net.minecraft.nbt.CompoundTag;

public interface IPetbedDataEntity {

    CompoundTag getCitadelEntityData();

    void setCitadelEntityData(CompoundTag nbt);
}
