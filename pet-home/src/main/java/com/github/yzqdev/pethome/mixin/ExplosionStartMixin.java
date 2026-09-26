package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.event.ServerEventContexts;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 爆炸拦截（替代 NeoForge 的 {@code ExplosionEvent.Start}）。
 *
 * <p>语义：宠物身上的「拆除（defusal）」附魔会在爆炸开始前把它整个取消掉。</p>
 *
 * <p><b>26.1 注意</b>：{@code net.minecraft.world.level.Explosion} 在 26.1 已经变成<b>接口</b>，
 * {@code @Mixin} 不能以接口为目标（PREPARE 阶段会直接报 target type mismatch 并让模组加载失败），
 * 因此这里改为注入其唯一的具体实现 {@link ServerExplosion}。</p>
 *
 * <p><b>26.1 注意（签名）</b>：{@code ServerExplosion#explode()} 返回 <b>{@code int}</b>
 * （不再是 1.21 的 {@code void}）。因此回调必须声明为
 * {@code CallbackInfoReturnable<Integer>} 并用 {@code setReturnValue(0)} 取消
 * ——写成 {@code CallbackInfo} 会在该类首次被加载时抛
 * {@code InvalidInjectionException: CallbackInfoReturnable is required}，
 * 且 {@code require = 0} 对此**无效**（描述符错误与是否必须命中无关）。</p>
 *
 * <p>由于 Mixin 的 APPLY 发生在目标类首次加载时，这类错误**不会**在启动阶段暴露：
 * 本工程此前 {@code runServer} 能跑通，就是因为服务端没有发生爆炸、{@code ServerExplosion} 从未被加载，
 * 直到客户端里凋灵骷髅头炸了一下才崩。排查同类问题时记住这一点。</p>
 */
@Mixin(ServerExplosion.class)
public abstract class ExplosionStartMixin {

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true, require = 0)
    private void pethome$onExplosionStart(CallbackInfoReturnable<Integer> cir) {
        ServerExplosion self = (ServerExplosion) (Object) this;
        ServerEventContexts.ExplosionEvent.Start event = new ServerEventContexts.ExplosionEvent.Start(self);
        ServerEvent.onExplosion(event);
        if (event.isCanceled()) {
            cir.setReturnValue(0);
        }
    }
}
