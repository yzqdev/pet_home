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

    /** 读取结构池实际模板列表（含去重后的最终权重表）。 */
    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> ph_getTemplates();

    /** 读取结构池原始模板-权重对（注入村民商店建筑用的写入源）。 */
    @Accessor("rawTemplates")
    List<com.mojang.datafixers.util.Pair<StructurePoolElement, Integer>> ph_getRawTemplates();

    /** 写回实际模板列表（配合 {@code @Mutable} 绕过 final 限制）。 */
    @Mutable
    @Accessor("templates")
    void ph_setTemplates(ObjectArrayList<StructurePoolElement> templates);

    /** 写回原始模板-权重对（追加商店建筑时按权重并入）。 */
    @Mutable
    @Accessor("rawTemplates")
    void ph_setRawTemplates(List<com.mojang.datafixers.util.Pair<StructurePoolElement, Integer>> rawTemplates);
}
