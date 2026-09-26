package com.github.yzqdev.pethome.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * 替代 Forge 的 accesstransformer：允许向村庄结构池追加自定义建筑。
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> ph_getTemplates();

    @Accessor("rawTemplates")
    List<com.mojang.datafixers.util.Pair<StructurePoolElement, Integer>> ph_getRawTemplates();

    @Mutable
    @Accessor("templates")
    void ph_setTemplates(ObjectArrayList<StructurePoolElement> templates);

    @Mutable
    @Accessor("rawTemplates")
    void ph_setRawTemplates(List<com.mojang.datafixers.util.Pair<StructurePoolElement, Integer>> rawTemplates);
}
