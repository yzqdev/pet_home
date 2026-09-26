package com.github.yzqdev.pethome.client.render;

import com.github.yzqdev.pethome.server.entity.ChainLightningEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Matrix4f;

import java.util.*;

public class LightningRender {
    private static final float REFRESH_TIME = 3.0F;
    private static final double MAX_OWNER_TRACK_TIME = (double) 100.0F;
    private Timestamp refreshTimestamp = new Timestamp();
    private final Random random = new Random();
    private final net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
    private final Map<Object, BoltOwnerData> boltOwners = new Object2ObjectOpenHashMap();

    public void render(float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Matrix4f matrix = poseStack.last().pose();
        Timestamp timestamp = new Timestamp(this.minecraft.level.getGameTime(), partialTicks);
        boolean refresh = timestamp.isPassed(this.refreshTimestamp, (double) 0.33333334F);
        if (refresh) {
            this.refreshTimestamp = timestamp;
        }

        Iterator<Map.Entry<Object, BoltOwnerData>> iter = this.boltOwners.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Object, BoltOwnerData> entry = (Map.Entry) iter.next();
            BoltOwnerData data = (BoltOwnerData) entry.getValue();
            if (refresh) {
                data.bolts.removeIf((bolt) -> bolt.tick(timestamp));
            }

            if (data.bolts.isEmpty() && data.lastBolt != null && data.lastBolt.getSpawnFunction().isConsecutive()) {
                data.addBolt(new BoltInstance(data.lastBolt, timestamp), timestamp);
            }

            // Render all bolts as a single submitCustomGeometry
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
                data.bolts.forEach((bolt) -> bolt.render(matrix, buffer, timestamp));
            });
            if (data.bolts.isEmpty() && timestamp.isPassed(data.lastUpdateTimestamp, (double) 100.0F)) {
                iter.remove();
            }
        }
    }

    public void update(Object owner, LightningBoltData newBoltData, float partialTicks) {
        if (this.minecraft.level != null) {
            BoltOwnerData data = (BoltOwnerData) this.boltOwners.computeIfAbsent(owner, (o) -> new BoltOwnerData());
            data.lastBolt = newBoltData;
            Timestamp timestamp = new Timestamp(this.minecraft.level.getGameTime(), partialTicks);
            if ((!data.lastBolt.getSpawnFunction().isConsecutive() || data.bolts.isEmpty()) && timestamp.isPassed(data.lastBoltTimestamp, data.lastBoltDelay)) {
                data.addBolt(new BoltInstance(newBoltData, timestamp), timestamp);
            }

            data.lastUpdateTimestamp = timestamp;
        }
    }

    /**
     * 只渲染指定 owner 的闪电。多个连锁闪电实体共享一个 LightningRender 时，
     * 各实体必须只画自己的 bolt，否则 bolt 会被其他实体的位移矩阵重复绘制到错误位置。
     */
    public void renderOwner(Object owner, float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        BoltOwnerData data = (BoltOwnerData) this.boltOwners.get(owner);
        if (data == null || this.minecraft.level == null) {
            return;
        }
        Timestamp timestamp = new Timestamp(this.minecraft.level.getGameTime(), partialTicks);
        boolean refresh = timestamp.isPassed(this.refreshTimestamp, (double) 0.33333334F);
        if (refresh) {
            this.refreshTimestamp = timestamp;
            data.bolts.removeIf((bolt) -> bolt.tick(timestamp));
        }

        if (data.bolts.isEmpty() && data.lastBolt != null && data.lastBolt.getSpawnFunction().isConsecutive()) {
            data.addBolt(new BoltInstance(data.lastBolt, timestamp), timestamp);
        }

        if (data.bolts.isEmpty()) {
            if (timestamp.isPassed(data.lastUpdateTimestamp, (double) 100.0F)) {
                this.boltOwners.remove(owner);
            }
            return;
        }

        Matrix4f matrix = poseStack.last().pose();
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            data.bolts.forEach((bolt) -> bolt.render(matrix, buffer, timestamp));
        });
    }

    public class BoltOwnerData {
        private final Set<BoltInstance> bolts = new ObjectOpenHashSet();
        private LightningBoltData lastBolt;
        private Timestamp lastBoltTimestamp = LightningRender.this.new Timestamp();
        private Timestamp lastUpdateTimestamp = LightningRender.this.new Timestamp();
        private double lastBoltDelay;

        private void addBolt(BoltInstance instance, Timestamp timestamp) {
            this.bolts.add(instance);
            this.lastBoltDelay = (double) instance.bolt.getSpawnFunction().getSpawnDelay(LightningRender.this.random);
            this.lastBoltTimestamp = timestamp;
        }
    }

    public class BoltInstance {
        private final LightningBoltData bolt;
        private final List<LightningBoltData.BoltQuads> renderQuads;
        private Timestamp createdTimestamp;

        public BoltInstance(LightningBoltData bolt, Timestamp timestamp) {
            this.bolt = bolt;
            this.renderQuads = bolt.generate();
            this.createdTimestamp = timestamp;
        }

        public void render(Matrix4f matrix, VertexConsumer buffer, Timestamp timestamp) {
            float lifeScale = timestamp.subtract(this.createdTimestamp).value() / (float) this.bolt.getLifespan();
            org.apache.commons.lang3.tuple.Pair<Integer, Integer> bounds = this.bolt.getFadeFunction().getRenderBounds(this.renderQuads.size(), lifeScale);

            for (int i = (Integer) bounds.getLeft(); i < (Integer) bounds.getRight(); ++i) {
                ((LightningBoltData.BoltQuads) this.renderQuads.get(i)).getVecs().forEach((v) -> buffer.addVertex(matrix, (float) v.x, (float) v.y, (float) v.z).setColor(this.bolt.getColor().x(), this.bolt.getColor().y(), this.bolt.getColor().z(), this.bolt.getColor().w()));
            }
        }

        public boolean tick(Timestamp timestamp) {
            return timestamp.isPassed(this.createdTimestamp, (double) this.bolt.getLifespan());
        }
    }

    public class Timestamp {
        private long ticks;
        private float partial;

        public Timestamp() {
            this(0L, 0.0F);
        }

        public Timestamp(long ticks, float partial) {
            this.ticks = ticks;
            this.partial = partial;
        }

        public Timestamp subtract(Timestamp other) {
            long newTicks = this.ticks - other.ticks;
            float newPartial = this.partial - other.partial;
            if (newPartial < 0.0F) {
                ++newPartial;
                --newTicks;
            }

            return LightningRender.this.new Timestamp(newTicks, newPartial);
        }

        public float value() {
            return (float) this.ticks + this.partial;
        }

        public boolean isPassed(Timestamp prev, double duration) {
            long ticksPassed = this.ticks - prev.ticks;
            if ((double) ticksPassed > duration) {
                return true;
            } else {
                duration -= (double) ticksPassed;
                if (duration >= (double) 1.0F) {
                    return false;
                } else {
                    return (double) (this.partial - prev.partial) >= duration;
                }
            }
        }
    }
}