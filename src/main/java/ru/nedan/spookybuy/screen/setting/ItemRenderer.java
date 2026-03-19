package ru.nedan.spookybuy.screen.setting;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import ru.nedan.neverapi.animation.Animation;
import ru.nedan.neverapi.animation.util.Easings;
import ru.nedan.neverapi.gl.Render2D;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.TextField;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.items.CollectItem;

import java.awt.Color;
import java.math.BigDecimal;

public class ItemRenderer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Getter @Setter private FloatRectangle position;
    public float offset, scroll;
    @Getter private final CollectItem collectItem;

    private boolean extended;
    private final Animation height = new Animation();
    private final TextField buyPriceField, sellPriceField;
    public boolean allocated = false;

    public ItemRenderer(FloatRectangle position, CollectItem collectItem, float offset) {
        this.position = position;
        this.collectItem = collectItem;
        this.offset = offset;
        height.setToValue(position.height).setValue(position.height);

        buyPriceField = new TextField(position.x + 3, position.y + offset + 25, position.width - 6, 12, "Цена покупки", (string) -> {
            if (string.isEmpty()) string = "0";
            SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(collectItem, new BigDecimal(string), false);
        }, Character::isDigit);

        sellPriceField = new TextField(position.x + 3, position.y + offset + 40, position.width - 6, 12, "Цена продажи", (string) -> {
            if (string.isEmpty()) string = "0";
            SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(collectItem, new BigDecimal(string), true);
        }, Character::isDigit);

        buyPriceField.setText(SpookyBuy.getInstance().getAutoBuy().getPriceMap().getPrice(collectItem, false).toString());
        sellPriceField.setText(SpookyBuy.getInstance().getAutoBuy().getPriceMap().getPrice(collectItem, true).toString());
    }

    public float getHeight() {
        return (float) height.getValue();
    }

    public void render(MatrixStack matrixStack) {
        height.update();
        FloatRectangle currentPos = this.position.offset(0, offset + scroll);

        Rounds.drawRound(currentPos.x, currentPos.y, currentPos.width, getHeight(), 4, new Color(20, 20, 25, 255));
        Render2D.renderItem(collectItem.getStack(), currentPos.x + 4, currentPos.y + 3);
        mc.textRenderer.draw(matrixStack, collectItem.getName(), currentPos.x + 24, currentPos.y + 7, 0xDDDDDD);

        mc.textRenderer.draw(matrixStack, "◆", currentPos.x + currentPos.width - 32, currentPos.y + 7, 0x555555);
        mc.textRenderer.draw(matrixStack, "⚙", currentPos.x + currentPos.width - 16, currentPos.y + 7, 0x555555);

        buyPriceField.updatePositions(currentPos.x + 3, currentPos.y + 25, currentPos.width - 6, 12);
        sellPriceField.updatePositions(currentPos.x + 3, currentPos.y + 40, currentPos.width - 6, 12);

        if (extended || height.getValue() > position.height) {
            Scissor.push();
            Scissor.setFromComponentCoordinates(currentPos.x, currentPos.y + 22, currentPos.width, height.getValue() - 22);
            buyPriceField.render(matrixStack);
            sellPriceField.render(matrixStack);
            Scissor.pop();
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (extended) {
            buyPriceField.mouseClicked(mouseX, mouseY, button);
            sellPriceField.mouseClicked(mouseX, mouseY, button);
        }
        FloatRectangle currentPos = this.position.offset(0, scroll + offset);
        if (button == 0 || button == 1) {
            if (currentPos.contains((float) mouseX, (float) mouseY) && mouseY <= currentPos.y + 22) {
                extended = !extended;
                if (extended) height.animate(position.height + 36, 0.2, Easings.QUAD_OUT);
                else height.animate(position.height, 0.2, Easings.QUAD_OUT);
            }
        }
    }

    public void charTyped(char chr) {
        if (extended) {
            buyPriceField.charTyped(chr);
            sellPriceField.charTyped(chr);
        }
    }

    public void keyPressed(int keyCode) {
        if (extended) {
            buyPriceField.keyPressed(keyCode);
            sellPriceField.keyPressed(keyCode);
        }
    }
}