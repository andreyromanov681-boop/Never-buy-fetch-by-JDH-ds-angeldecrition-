package ru.nedan.spookybuy.screen.setting.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import ru.nedan.spookybuy.screen.setting.page.inst.ConfigPage;
import ru.nedan.spookybuy.screen.setting.page.inst.ItemsPage;
import ru.nedan.spookybuy.screen.setting.page.inst.ParserPage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Pages {
    ITEMS(ItemsPage.getInstance()),
    PARSER(ParserPage.getInstance()),
    CONFIGS(ConfigPage.getInstance());

    public static final List<Page> ALL = Arrays.stream(values()).map(Pages::getPage).collect(Collectors.toList());

    final Page page;

    @Getter
    @Setter
    static Page current = ITEMS.getPage();

    public static boolean isCurrent(Page page) {
        return current == page;
    }

    public static void init() {
        ALL.forEach(Page::init);
    }

    public static void render(MatrixStack matrices) {
        if (current != null) {
            current.render(matrices);
        }
    }

    public static void mouseReleased(int button) {
        if (current != null) {
            current.mouseReleased(button);
        }
    }

    public static void mouseClicked(int button) {
        if (current != null) {
            current.mouseClicked(button);
        }
    }

    public static void charTyped(char chr) {
        if (current != null) {
            current.charTyped(chr);
        }
    }

    public static void keyPressed(int keyCode) {
        if (current != null) {
            current.keyPressed(keyCode);
        }
    }

    public static void mouseScrolled(double amount) {
        if (current != null) {
            current.mouseScrolled(amount);
        }
    }
}