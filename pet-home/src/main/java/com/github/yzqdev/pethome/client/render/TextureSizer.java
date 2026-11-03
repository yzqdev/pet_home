package com.github.yzqdev.pethome.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

/** 贴图尺寸查询工具：按 ResourceLocation 读取贴图真实宽高并缓存（特效渲染按贴图比例布顶点用）。 */
@Environment(EnvType.CLIENT)
public class TextureSizer {

    private static Map<ResourceLocation, Pair<Integer, Integer>> TEXTURE_TO_DIMENSIONS = new HashMap<>();

    public static Pair<Integer, Integer> getTextureWidth(ResourceLocation texture) {
        if (TEXTURE_TO_DIMENSIONS.get(texture) != null) {
            return TEXTURE_TO_DIMENSIONS.get(texture);
        } else {
            int height = 16;
            int width = 16;
            try {
                Resource res = Minecraft.getInstance().getResourceManager().getResource(texture).get();
                NativeImage nativeimage = NativeImage.read(res.open());
                width = nativeimage.getWidth();
                height = nativeimage.getHeight();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Pair pair = new Pair<>(width, height);
            TEXTURE_TO_DIMENSIONS.put(texture, pair);
            return pair;
        }
    }
}
