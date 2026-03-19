package ru.nedan.spookybuy.screen.setting.page.inst;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.ItemStorage;
import ru.nedan.spookybuy.screen.setting.ItemRenderer;
import ru.nedan.spookybuy.screen.setting.page.Page;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;

import java.util.ArrayList;
import java.util.List;

public class ItemsPage extends Page {

    private static ItemsPage instance;
    private final List<ItemRenderer> renderers = new ArrayList<>();
    public float scroll, animatedScroll;

    public ItemsPage() {
        instance = this;
    }

    public static Page getInstance() {
        if (instance == null) new ItemsPage();
        return instance;
    }

    @Override
    public void init() {
        renderers.clear();
        float offset = 0;
        for (CollectItem collectItem : ItemStorage.ALL) {
            ItemRenderer itemRenderer = new ItemRenderer(new FloatRectangle(0, 0, 240, 22), collectItem, offset);
            renderers.add(itemRenderer);
            offset += itemRenderer.getPosition().height + 2;
        }
    }

    @Override
    public void render(MatrixStack matrixStack) {
        FloatRectangle window = gui.window;
        if (window == null) return;

        animatedScroll = MathUtils.lerp(animatedScroll, scroll, 8);

        float listY = window.y + 25;
        float listHeight = window.height - 85;

        Scissor.push();
        Scissor.setFromComponentCoordinates(window.x, listY, window.width, listHeight);

        float offset = 0;
        for (ItemRenderer renderer : renderers) {
            renderer.setPosition(new FloatRectangle(window.x + 10, window.y + 25, window.width - 20, 22));
            renderer.offset = offset;
            renderer.scroll = animatedScroll;
            renderer.render(matrixStack);
            offset += renderer.getHeight() + 2;
        }

        Scissor.pop();
        scroll = MathHelper.clamp(scroll, -Math.max(0, offset - listHeight), 0);
    }

    @Override
    public void mouseClicked(int button) {
        FloatRectangle window = gui.window;
        if (window == null) return;
        double mouseX = MathUtils.getMousePos().x;
        double mouseY = MathUtils.getMousePos().y;

        if (mouseY >= window.y + 25 && mouseY <= window.y + window.height - 60) {
            for (ItemRenderer renderer : renderers) {
                renderer.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(int button) {}

    @Override
    public void charTyped(char codePoint) {
        for (ItemRenderer renderer : renderers) {
            renderer.charTyped(codePoint);
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        for (ItemRenderer renderer : renderers) {
            renderer.keyPressed(keyCode);
        }
    }

    @Override
    public void mouseScrolled(double amount) {
        scroll += (float) (amount * 16);
    }

    @Override
    public IconRenderer getIconRenderer() {
        return new IconRenderer("АвтоБай");
    }
}