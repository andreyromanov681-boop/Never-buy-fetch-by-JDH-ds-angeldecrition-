package ru.nedan.spookybuy.screen.setting;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.animation.Animation;
import ru.nedan.neverapi.animation.util.Easings;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.gui.TextField;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.Coefficient;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.CoefficientType;
import ru.nedan.spookybuy.util.ImageRenderer;
import ru.nedan.spookybuy.screen.setting.page.inst.ParserPage;

import java.awt.Color;
import java.math.BigDecimal;

public class CoefficientRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Getter @Setter private FloatRectangle positions;
    public float offset, scroll;
    @Getter private final Coefficient coefficient;

    private boolean extended;
    private final Animation height = new Animation();
    private final TextField priceField, buyCoefField, sellCoefField;
    private final Button left, right, delete;

    public CoefficientRenderer(FloatRectangle positions, Coefficient coefficient, float offset) {
        this.positions = positions;
        this.coefficient = coefficient;
        this.offset = offset;
        height.setToValue(positions.height).setValue(positions.height);

        priceField = new TextField(0, 0, 0, 0, "Цена", (s) -> {
            coefficient.edit(new BigDecimal(s.isEmpty() ? "0" : s), null, null, null);
        }, Character::isDigit);

        buyCoefField = new TextField(0, 0, 0, 0, "Куп. Коэф", (s) -> {
            coefficient.edit(null, Double.parseDouble(s.isEmpty() ? "0" : s), null, null);
        }, (c) -> Character.isDigit(c) || c == '.');

        sellCoefField = new TextField(0, 0, 0, 0, "Прод. Коэф", (s) -> {
            coefficient.edit(null, null, Double.parseDouble(s.isEmpty() ? "0" : s), null);
        }, (c) -> Character.isDigit(c) || c == '.');

        left = new Button(Text.of(""), new FloatRectangle(0,0,0,0), (b) -> {
            coefficient.edit(null, null, null, CoefficientType.previous(coefficient.getType()));
        }, null) {
            @Override
            public void render(MatrixStack matrices) {
                Identifier tex = ImageRenderer.loadOrGetTexture(null, "left");
                ImageRenderer.render(tex, getPositions().x, getPositions().y, getPositions().width, getPositions().height, -1);
            }
        };

        right = new Button(Text.of(""), new FloatRectangle(0,0,0,0), (b) -> {
            coefficient.edit(null, null, null, CoefficientType.next(coefficient.getType()));
        }, null) {
            @Override
            public void render(MatrixStack matrices) {
                Identifier tex = ImageRenderer.loadOrGetTexture(null, "right");
                ImageRenderer.render(tex, getPositions().x, getPositions().y, getPositions().width, getPositions().height, -1);
            }
        };

        delete = new Button(Text.of("Удалить"), new FloatRectangle(0,0,0,0), (b) -> {
            mc.openScreen(new AcceptScreen(SpookyBuy.getInstance().getGui(), Text.of("Удалить коэф?"), new Runnable[]{
                    () -> {
                        Coefficient.getAll().removeIf(c -> c == coefficient);
                        ParserPage.getInstance().init();
                    },
                    () -> {}
            }));
        }, null);

        priceField.setText(coefficient.getForPrice().toPlainString());
        buyCoefField.setText(String.valueOf(coefficient.getDecimalPair().getLeft()));
        sellCoefField.setText(String.valueOf(coefficient.getDecimalPair().getRight()));
    }

    public float getHeight() {
        return (float) height.getValue();
    }

    public void render(MatrixStack matrixStack) {
        height.update();
        FloatRectangle currentPos = this.positions.offset(0, offset + scroll);

        Rounds.drawRound(currentPos.x, currentPos.y, currentPos.width, getHeight(), 4, new Color(20, 20, 25, 255));
        mc.textRenderer.draw(matrixStack, "Коэф: " + CoefficientType.toShortString(coefficient.getType()) + " " + coefficient.getForPrice().toPlainString(), currentPos.x + 8, currentPos.y + 7, 0xDDDDDD);
        mc.textRenderer.draw(matrixStack, "⚙", currentPos.x + currentPos.width - 16, currentPos.y + 7, extended ? 0x55FF55 : 0x555555);

        if (extended || height.getValue() > positions.height) {
            Scissor.push();
            Scissor.setFromComponentCoordinates(currentPos.x, currentPos.y + 22, currentPos.width, height.getValue() - 22);

            priceField.updatePositions(currentPos.x + 8, currentPos.y + 25, currentPos.width - 16, 12);
            buyCoefField.updatePositions(currentPos.x + 8, currentPos.y + 40, (currentPos.width - 20) / 2, 12);
            sellCoefField.updatePositions(currentPos.x + 8 + (currentPos.width - 20) / 2 + 4, currentPos.y + 40, (currentPos.width - 20) / 2, 12);

            left.updatePositions(currentPos.x + 8, currentPos.y + 55, 12, 12);
            right.updatePositions(currentPos.x + currentPos.width - 20, currentPos.y + 55, 12, 12);

            delete.updatePositions(currentPos.x + 8, currentPos.y + 70, currentPos.width - 16, 12);

            priceField.render(matrixStack);
            buyCoefField.render(matrixStack);
            sellCoefField.render(matrixStack);
            left.render(matrixStack);
            right.render(matrixStack);
            delete.render(matrixStack);

            String typeText = "Режим: " + coefficient.getType();
            mc.textRenderer.draw(matrixStack, typeText, currentPos.x + currentPos.width / 2f - mc.textRenderer.getWidth(typeText) / 2f, currentPos.y + 57, -1);

            Scissor.pop();
        }
    }

    public void mouseClicked(int button) {
        Vec2f m = MathUtils.getMousePos();
        FloatRectangle currentPos = this.positions.offset(0, scroll + offset);

        if (extended) {
            priceField.mouseClicked(m.x, m.y, button);
            buyCoefField.mouseClicked(m.x, m.y, button);
            sellCoefField.mouseClicked(m.x, m.y, button);
            left.mouseClicked(m.x, m.y, button);
            right.mouseClicked(m.x, m.y, button);
            delete.mouseClicked(m.x, m.y, button);
        }

        if (currentPos.contains(m.x, m.y) && m.y <= currentPos.y + 22) {
            extended = !extended;
            height.animate(extended ? positions.height + 70 : positions.height, 0.2, Easings.QUAD_OUT);
        }
    }

    public void charTyped(char codePoint) {
        if (extended) {
            priceField.charTyped(codePoint);
            buyCoefField.charTyped(codePoint);
            sellCoefField.charTyped(codePoint);
        }
    }

    public void keyPressed(int keyCode) {
        if (extended) {
            priceField.keyPressed(keyCode);
            buyCoefField.keyPressed(keyCode);
            sellCoefField.keyPressed(keyCode);
        }
    }
}