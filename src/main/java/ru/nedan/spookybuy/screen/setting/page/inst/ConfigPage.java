package ru.nedan.spookybuy.screen.setting.page.inst;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import ru.nedan.neverapi.async.AsyncRunManager;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.event.EventServerResponse;
import ru.nedan.spookybuy.screen.setting.page.Page;
import ru.nedan.spookybuy.screen.setting.page.Pages;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;
import ru.nedan.spookybuy.util.ws.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigPage extends Page {

    private static ConfigPage instance;

    public static Page getInstance() {
        if (instance == null) instance = new ConfigPage();
        return instance;
    }

    @Getter
    private final List<ConfigData> configs = new ArrayList<>();
    private final Map<String, String> keys = new HashMap<>();
    private static final HashMap<String, Boolean> autoLoadFlags = new HashMap<>();
    private float scroll;
    private boolean loading = false;

    @Override
    public void init() {
        if (loading) return;
        loading = true;

        keys.clear();

        AsyncRunManager.once(EventServerResponse.class, e -> {
            e.setUsed(true);
            configs.clear();
            JsonArray array = e.asJson().getAsJsonArray("message");

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                String name = obj.get("itemName").getAsString();
                String desc = obj.get("description").getAsString();
                String discord = obj.get("discordLink").getAsString();
                String cloudKey = obj.get("cloudKey").getAsString();
                int users = obj.get("usersSize").getAsInt();

                String localKey = UUID.randomUUID().toString();

                configs.add(new ConfigData(name, desc, discord, cloudKey, users, localKey));
                keys.put(localKey, cloudKey);
            }
            loading = false;
        }, e -> e.asJson().has("type") && e.asJson().get("type").getAsString().equalsIgnoreCase("configs"));

        JsonObject obj = new JsonObject();
        obj.addProperty("action", "askConfigs");
        Client.getInstance().sendMessage(obj.toString());
    }

    @Override
    public void render(MatrixStack matrices) {
        FloatRectangle window = gui.window;
        if (window == null) return;

        float x = window.x + 10;
        float y = window.y + 30;
        float w = window.width - 20;
        float h = window.height - 75;

        Scissor.push();
        Scissor.setFromComponentCoordinates(window.x, y, window.width, h);

        float offset = 0;
        for (ConfigData cfg : configs) {
            float posY = y + offset + scroll;

            Rounds.drawRound(x, posY, w, 40, 6, new Color(25, 25, 30, 255));

            client.textRenderer.drawWithShadow(matrices, cfg.name, x + 8, posY + 5, 0x55FFFF);

            String shortDesc = cfg.description.length() > 30 ? cfg.description.substring(0, 27) + "..." : cfg.description;
            client.textRenderer.draw(matrices, shortDesc, x + 8, posY + 17, 0xAAAAAA);

            client.textRenderer.draw(matrices, "Игроков: " + cfg.users, x + 8, posY + 28, 0x888888);

            client.textRenderer.drawWithShadow(matrices, "[ЗАГРУЗИТЬ]", x + w - 75, posY + 5, 0x55FF55);

            boolean isAuto = isAutoLoad(cfg.name);
            client.textRenderer.drawWithShadow(matrices, "[АВТО: " + (isAuto ? "§aВКЛ" : "§сВЫКЛ") + "§r]", x + w - 75, posY + 16, 0xFFCC00);

            client.textRenderer.drawWithShadow(matrices, "[ДС]", x + w - 30, posY + 28, 0x5599FF);

            offset += 45;
        }

        Scissor.pop();
    }

    @Override
    public void mouseClicked(int button) {
        FloatRectangle window = gui.window;
        if (window == null) return;

        var mouse = MathUtils.getMousePos();
        float yLimitTop = window.y + 30;
        float yLimitBottom = window.y + window.height - 45;

        if (mouse.y < yLimitTop || mouse.y > yLimitBottom) return;

        float x = window.x + 10;
        float w = window.width - 20;
        float offset = 0;

        for (ConfigData cfg : configs) {
            float posY = yLimitTop + offset + scroll;

            if (mouse.x >= x + w - 80 && mouse.x <= x + w - 10 &&
                    mouse.y >= posY + 4 && mouse.y <= posY + 14) {
                String cloudKey = keys.get(cfg.localKey);
                Client.getInstance().sendLoadConfigResponse(cloudKey);
            }

            if (mouse.x >= x + w - 80 && mouse.x <= x + w - 10 &&
                    mouse.y >= posY + 15 && mouse.y <= posY + 25) {
                toggleAutoLoad(cfg.name);
            }

            if (mouse.x >= x + w - 35 && mouse.x <= x + w - 5 &&
                    mouse.y >= posY + 27 && mouse.y <= posY + 38) {
                net.minecraft.util.Util.getOperatingSystem().open(cfg.discord);
            }

            offset += 45;
        }
    }

    public static void toggleAutoLoad(String name) {
        autoLoadFlags.put(name, !isAutoLoad(name));
    }

    public static boolean isAutoLoad(String name) {
        return autoLoadFlags.getOrDefault(name, false);
    }

    @Override
    public void mouseScrolled(double amount) {
        scroll += (float) (amount * 15);

        float contentHeight = configs.size() * 45;
        float viewHeight = gui.window.height - 80;
        float maxScroll = viewHeight - contentHeight;

        if (maxScroll > 0) maxScroll = 0;

        if (scroll > 0) scroll = 0;
        if (scroll < maxScroll) scroll = maxScroll;
    }

    @Override public void mouseReleased(int button) {}
    @Override public void charTyped(char codePoint) {}
    @Override public void keyPressed(int keyCode) {}

    @Override
    public IconRenderer getIconRenderer() {
        return new IconRenderer("CFG") {
            @Override
            public void mouseClicked(int button) {
                if (getPositions().contains(MathUtils.getMousePos().x, MathUtils.getMousePos().y)) {
                    Pages.setCurrent(ConfigPage.getInstance());
                }
            }
        };
    }

    private static class ConfigData {
        String name, description, discord, cloudKey, localKey;
        int users;

        public ConfigData(String name, String description, String discord, String cloudKey, int users, String localKey) {
            this.name = name;
            this.description = description;
            this.discord = discord;
            this.cloudKey = cloudKey;
            this.users = users;
            this.localKey = localKey;
        }
    }
}