package com.github.yzqdev.pethome.util;

import net.minecraft.nbt.CompoundTag;

public interface IPetbedDataEntity {

    CompoundTag getCitadelEntityData();

    void setCitadelEntityData(CompoundTag nbt);
}
