package ru.nedan.spookybuy.screen.setting.page.inst;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.Coefficient;
import ru.nedan.spookybuy.screen.setting.CoefficientRenderer;
import ru.nedan.spookybuy.screen.setting.page.Page;
import ru.nedan.spookybuy.screen.setting.page.Pages;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParserPage extends Page {

    private static ParserPage instance;
    private final List<CoefficientRenderer> renderers = new ArrayList<>();
    public float scroll;
    private Button addButton;

    public ParserPage() {
        instance = this;
    }

    public static Page getInstance() {
        if (instance == null) instance = new ParserPage();
        return instance;
    }

    @Override
    public void init() {
        renderers.clear();
        float offset = 0;
        for (Coefficient coefficient : Coefficient.getAll()) {
            CoefficientRenderer renderer = new CoefficientRenderer(new FloatRectangle(0, 0, 200, 18), coefficient, offset);
            renderers.add(renderer);
            offset += renderer.getHeight() + 3;
        }

        addButton = new Button(Text.of("Добавить"), new FloatRectangle(0, 0, 0, 0), (b) -> {
            Coefficient.getAll().add(Coefficient.createDefault());
            init();
        }, Collections.emptyList());
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if ((renderers.isEmpty() && !Coefficient.getAll().isEmpty()) || addButton == null) {
            init();
        }

        FloatRectangle window = gui.window;
        if (window == null) return;

        float x = window.x + 5;
        float y = window.y + 25;
        float w = window.width - 10;
        float h = window.height - 55;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y, w, h);

        float currentOffset = 0;
        for (CoefficientRenderer renderer : renderers) {
            renderer.setPositions(new FloatRectangle(x, y, w, 18));
            renderer.offset = currentOffset;
            renderer.scroll = scroll;
            renderer.render(matrixStack);
            currentOffset += renderer.getHeight() + 3;
        }

        float buttonY = y + currentOffset + scroll;
        addButton.updatePositions(x, buttonY, w, 18);
        addButton.render(matrixStack);
        currentOffset += 21;

        Scissor.pop();

        float maxScroll = Math.max(0, currentOffset - h);
        scroll = MathHelper.clamp(scroll, -maxScroll, 0);
    }

    @Override
    public void mouseClicked(int button) {
        for (CoefficientRenderer renderer : renderers) {
            renderer.mouseClicked(button);
        }

        FloatRectangle window = gui.window;
        if (window == null || addButton == null) return;

        float y = window.y + 25;
        float h = window.height - 55;
        double mouseY = MathUtils.getMousePos().y;

        if (mouseY >= y && mouseY <= y + h) {
            Vec2f mouse = MathUtils.getMousePos();
            addButton.mouseClicked(mouse.x, mouse.y, button);
        }
    }

    @Override
    public void mouseReleased(int button) {}

    @Override
    public void charTyped(char codePoint) {
        for (CoefficientRenderer renderer : renderers) {
            renderer.charTyped(codePoint);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        for (CoefficientRenderer renderer : renderers) {
            renderer.keyPressed(keyCode);
        }
    }

    @Override
    public void mouseScrolled(double amount) {
        scroll += (float) (amount * 15);
    }

    @Override
    public IconRenderer getIconRenderer() {
        return iconRenderer;
    }

    private final IconRenderer iconRenderer = new IconRenderer("АвтоСетап") {
        @Override
        public void render(MatrixStack matrixStack) {
            FloatRectangle pos = getPositions();
            Rounds.drawRound(pos.x, pos.y, pos.width, pos.height, 4, new Color(0, 0, 0, 255));
            Vec2f textPos = MathUtils.getCenteredTextPosition(Text.of(getTitle()), pos);
            client.textRenderer.drawWithShadow(matrixStack, getTitle(), textPos.x, textPos.y,
                    Pages.getCurrent() == Pages.PARSER.getPage() ? Color.GREEN.getRGB() : -1);
        }

        @Override
        public void mouseClicked(int button) {
            if (getPositions().contains(MathUtils.getMousePos().x, MathUtils.getMousePos().y)) {
                Pages.setCurrent(Pages.PARSER.getPage());
                scroll = 0;
            }
        }
    };
}
