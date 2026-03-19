package ru.nedan.spookybuy.screen.setting;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import ru.nedan.neverapi.etc.KeyUtil;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.screen.setting.page.Pages;

import java.awt.Color;

public class SpookyBuyGui extends Screen {

    public FloatRectangle window;
    public boolean abBinding, autoSetupBinding;

    public SpookyBuyGui() {
        super(new LiteralText("SpookyBuyGui"));
    }

    @Override
    protected void init() {
        super.init();
        window = MathUtils.getCenteredPosition(260, 280);
        Pages.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        window = MathUtils.getCenteredPosition(260, 280);

        Rounds.drawRound(window.x, window.y, window.width, window.height, 6, new Color(15, 15, 20, 240));

        Text header = Text.of("◇ Never SpookyBuy ◇");
        client.textRenderer.drawWithShadow(matrices, header, window.x + window.width / 2f - client.textRenderer.getWidth(header) / 2f, window.y + 10, -1);

        Pages.render(matrices);

        float bottomY = window.y + window.height - 35;
        float btnWidth = 80;
        float centerBtnWidth = 70;
        float btnHeight = 20;

        String abBind = "АвтоБай: " + (abBinding ? "..." : KeyUtil.getKey(SpookyBuy.getInstance().getAbKey()));
        String asBind = "АвтоСетап: " + (autoSetupBinding ? "..." : KeyUtil.getKey(SpookyBuy.getInstance().getAutoSetupKey()));

        client.textRenderer.drawWithShadow(matrices, abBind, window.x + 10 + btnWidth / 2f - client.textRenderer.getWidth(abBind) / 2f, bottomY - 12, 0xAAAAAA);
        client.textRenderer.drawWithShadow(matrices, asBind, window.x + window.width - 10 - btnWidth + btnWidth / 2f - client.textRenderer.getWidth(asBind) / 2f, bottomY - 12, 0xAAAAAA);

        Rounds.drawRound(window.x + 10, bottomY, btnWidth, btnHeight, 4, new Color(25, 25, 30, 255));
        Text abText = Text.of("АвтоБай");
        int abColor = Pages.getCurrent() == Pages.ITEMS.getPage() ? 0x55FF55 : 0xAAAAAA;
        client.textRenderer.drawWithShadow(matrices, abText, window.x + 10 + btnWidth / 2f - client.textRenderer.getWidth(abText) / 2f, bottomY + 6, abColor);

        float centerX = window.x + window.width / 2f - centerBtnWidth / 2f;
        Rounds.drawRound(centerX, bottomY, centerBtnWidth, btnHeight, 4, new Color(25, 25, 30, 255));
        Text cfgText = Text.of("Конфиги");
        int cfgColor = Pages.getCurrent() == Pages.CONFIGS.getPage() ? 0x55FF55 : 0xAAAAAA;
        client.textRenderer.drawWithShadow(matrices, cfgText, centerX + centerBtnWidth / 2f - client.textRenderer.getWidth(cfgText) / 2f, bottomY + 6, cfgColor);

        Rounds.drawRound(window.x + window.width - 10 - btnWidth, bottomY, btnWidth, btnHeight, 4, new Color(25, 25, 30, 255));
        Text asText = Text.of("АвтоСетап");
        int asColor = Pages.getCurrent() == Pages.PARSER.getPage() ? 0x55FF55 : 0xAAAAAA;
        client.textRenderer.drawWithShadow(matrices, asText, window.x + window.width - 10 - btnWidth + btnWidth / 2f - client.textRenderer.getWidth(asText) / 2f, bottomY + 6, asColor);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float bottomY = window.y + window.height - 35;
        float btnWidth = 80;
        float centerBtnWidth = 70;
        float btnHeight = 20;
        float centerX = window.x + window.width / 2f - centerBtnWidth / 2f;

        if (mouseY >= bottomY && mouseY <= bottomY + btnHeight) {
            if (mouseX >= window.x + 10 && mouseX <= window.x + 10 + btnWidth) {
                Pages.setCurrent(Pages.ITEMS.getPage());
            } else if (mouseX >= window.x + window.width - 10 - btnWidth && mouseX <= window.x + window.width - 10) {
                Pages.setCurrent(Pages.PARSER.getPage());
            } else if (mouseX >= centerX && mouseX <= centerX + centerBtnWidth) {
                Pages.setCurrent(Pages.CONFIGS.getPage());
            }
        }

        if (mouseY >= bottomY - 15 && mouseY <= bottomY) {
            if (mouseX >= window.x + 10 && mouseX <= window.x + 10 + btnWidth) {
                abBinding = true;
                autoSetupBinding = false;
            } else if (mouseX >= window.x + window.width - 10 - btnWidth && mouseX <= window.x + window.width - 10) {
                autoSetupBinding = true;
                abBinding = false;
            }
        } else {
            Pages.mouseClicked(button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Pages.mouseReleased(button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        Pages.charTyped(chr);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (abBinding) {
            abBinding = false;
            SpookyBuy.getInstance().setAbKey(keyCode);
            return true;
        }
        if (autoSetupBinding) {
            autoSetupBinding = false;
            SpookyBuy.getInstance().setAutoSetupKey(keyCode);
            return true;
        }
        Pages.keyPressed(keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        Pages.mouseScrolled(amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}