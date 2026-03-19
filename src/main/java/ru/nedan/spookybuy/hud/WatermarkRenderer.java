package ru.nedan.spookybuy.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import ru.nedan.neverapi.shader.Rounds;

import java.awt.Color;

public class WatermarkRenderer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public void render(MatrixStack ms) {
        float posY = 10f;
        float height = 26f;
        int ping = getPing();
        int fps = getFPS();
        String domainText = "Never SpookyBuy";
        String slashText = "  /  ";
        String pingText = ping + " ms";
        String fpsText = fps + " FPS";
        float NeverWidth = 16f;
        float padding = 12f;
        float NeverPadding = 6f;
        float domainWidth = mc.textRenderer.getWidth(domainText);
        float slashWidth = mc.textRenderer.getWidth(slashText);
        float pingWidth = mc.textRenderer.getWidth(pingText);
        float fpsWidth = mc.textRenderer.getWidth(fpsText);
        float totalWidth = padding + NeverWidth + NeverPadding + domainWidth + slashWidth + pingWidth + slashWidth + fpsWidth + padding;
        float posX = (mc.getWindow().getScaledWidth() - totalWidth) / 2.0f;
        drawStyled(posX, posY, totalWidth, height, 4f);
        float currentX = posX + padding;
        float NeverY = posY + (height / 2f);
        float iconOffsetX = -2f;
        draw(currentX + iconOffsetX, NeverY);
        currentX += NeverWidth + NeverPadding;
        float textY = posY + (height / 2f) - (mc.textRenderer.fontHeight / 2f) + 1f;
        mc.textRenderer.draw(ms, domainText, currentX, textY, new Color(255, 255, 255).getRGB());
        currentX += domainWidth;
        mc.textRenderer.draw(ms, slashText, currentX, textY, new Color(80, 80, 80).getRGB());
        currentX += slashWidth;
        mc.textRenderer.draw(ms, pingText, currentX, textY, new Color(209, 209, 209).getRGB());
        currentX += pingWidth;
        mc.textRenderer.draw(ms, slashText, currentX, textY, new Color(80, 80, 80).getRGB());
        currentX += slashWidth;
        mc.textRenderer.draw(ms, fpsText, currentX, textY, new Color(209, 209, 209).getRGB());
    }

    private void drawStyled(float x, float y, float width, float height, float radius) {
        Rounds.drawRound(x - 0.5f, y - 0.5f, width + 1, height + 1, radius, new Color(0, 0, 0, 255));
        Rounds.drawRound(x, y, width, height, radius, new Color(18, 18, 22, 230));
    }

    private void draw(float x, float y) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.5f);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        float scale = 6.0f + (float) Math.sin(time * Math.PI * 2) * 1.5f;
        float angle = time * 360f;
        float r = 107 / 255f;
        float g = 70 / 255f;
        float b = 193 / 255f;
        float alpha = 0.9f;
        GL11.glColor4f(r, g, b, alpha);
        float cx = x + 8;
        float cy = y;
        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0);
        GL11.glRotatef(angle, 0, 0, 1);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(0, -scale);
        GL11.glVertex2f(scale, 0);
        GL11.glVertex2f(0, scale);
        GL11.glVertex2f(-scale, 0);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private int getPing() {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                return entry.getLatency();
            }
        }
        return 0;
    }

    private int getFPS() {
        try {
            String fpsStr = mc.fpsDebugString.split(" ")[0];
            return Integer.parseInt(fpsStr);
        } catch (Exception e) {
            return 0;
        }
    }
}
