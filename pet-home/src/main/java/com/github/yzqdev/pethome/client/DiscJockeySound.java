package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** disc_jockey 唱片机循环音效（自 1.20 移植）：跟随实体位置、不衰减、循环播放；实体移除时停止并清理登记。 */
public class DiscJockeySound extends AbstractTickableSoundInstance {

    private final FollowingJukeboxEntity box;
    private final SoundEvent recordSound;

    public DiscJockeySound(SoundEvent record, FollowingJukeboxEntity box) {
        super(record, SoundSource.RECORDS, box.level().getRandom());
        this.box = box;
        this.attenuation = Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.x = this.box.getX();
        this.y = this.box.getY();
        this.z = this.box.getZ();
        this.recordSound = record;
    }

    public boolean canPlaySound() {
        return !this.box.isSilent()
                && ClientGameEvents.DISC_JOCKEY_SOUND_MAP.get(this.box.getId()) == this;
    }

    public boolean isNearest() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.box.isRemoved() && this.box.isAlive()) {
            this.volume = 1;
            this.pitch = 1;
            this.x = this.box.getX();
            this.y = this.box.getY();
            this.z = this.box.getZ();
        } else {
            this.stop();
            ClientGameEvents.DISC_JOCKEY_SOUND_MAP.remove(this.box.getId());
        }
    }

    public SoundEvent getRecordSound() {
        return recordSound;
    }
}
