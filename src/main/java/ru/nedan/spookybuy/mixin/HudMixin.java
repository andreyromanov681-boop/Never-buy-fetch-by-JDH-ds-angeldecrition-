package ru.nedan.spookybuy.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nedan.spookybuy.hud.WatermarkRenderer;

@Mixin(InGameHud.class)
public class HudMixin {

    private final WatermarkRenderer watermarkRenderer = new WatermarkRenderer();

    @Inject(method = "render", at = @At("HEAD"))
    private void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null && !mc.options.hudHidden) {
            watermarkRenderer.render(matrices);
        }
    }
}