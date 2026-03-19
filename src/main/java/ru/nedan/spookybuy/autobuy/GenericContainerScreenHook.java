package ru.nedan.spookybuy.autobuy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ChatUtil;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.TimerUtility;
import ru.nedan.neverapi.shader.Blur;
import ru.nedan.neverapi.shader.ColorUtility;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.util.Utils;
import ru.nedan.spookybuy.autobuy.history.HistoryManager;
import ru.nedan.spookybuy.items.CollectItem;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GenericContainerScreenHook extends GenericContainerScreen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static float rotationSpeed = 5.0f;
    private static float currentAngle = 0f;
    private long lastMs = System.currentTimeMillis();

    private Button button = null;
    private Button speedUp = null;
    private Button speedDown = null;
    private final TimerUtility afterInit = new TimerUtility();
    public Slot minPrice;

    public GenericContainerScreenHook(GenericContainerScreen old) {
        super(old.getScreenHandler(), mc.player.inventory, old.getTitle());
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);
    }

    public static void fill(List<Slot> slots) {
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || stack.getCount() == 1 || Utils.getPrice(stack) == -1) continue;

            NbtCompound compound = stack.getOrCreateTag();
            NbtCompound display = compound.getCompound("display");
            NbtList lore = display.contains("Lore", 9) ? display.getList("Lore", 8) : new NbtList();

            boolean next = true;
            for (int i = 0; i < lore.size(); i++) {
                if (lore.getString(i).contains("Цена за 1 шт.")) {
                    next = false;
                    break;
                }
            }
            if (!next) continue;

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
            String priceText = "[\"\",{\"italic\":false,\"color\":\"green\",\"text\":\"$\"}," +
                    "{\"italic\":false,\"color\":\"white\",\"text\":\" Цена за 1 шт.: \"}," +
                    "{\"italic\":false,\"color\":\"green\",\"text\":\"$" + numberFormat.format(Utils.getPrice(stack) / stack.getCount()) + "\"}]";

            boolean inserted = false;
            for (int i = 0; i < lore.size(); i++) {
                String line = ChatUtil.stripTextFormat(lore.getString(i));
                if (line.contains("Цена: ")) {
                    lore.add(i + 1, NbtString.of(priceText));
                    inserted = true;
                    break;
                }
            }
            if (!inserted) lore.add(NbtString.of(priceText));

            display.put("Lore", lore);
            compound.put("display", display);
            stack.setTag(compound);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HistoryManager historyManager = HistoryManager.getInstance();
        super.render(matrices, mouseX, mouseY, delta);
        fill(handler.slots);

        if (button == null) {
            button = new Button(new LiteralText("Очистить"), new FloatRectangle(this.x - 150, this.y + this.backgroundHeight + 4, 145, 18), (btn) -> {
                historyManager.clear();
            }, Collections.singletonList(new LiteralText("Нажмите чтобы очистить историю")));

            speedUp = new Button(new LiteralText("+"), new FloatRectangle(this.x - 150, this.y + this.backgroundHeight + 24, 40, 18), (btn) -> {
                rotationSpeed += 1.0f;
            }, Collections.singletonList(new LiteralText("Увеличить скорость")));

            speedDown = new Button(new LiteralText("-"), new FloatRectangle(this.x - 105, this.y + this.backgroundHeight + 24, 40, 18), (btn) -> {
                rotationSpeed = Math.max(0, rotationSpeed - 1.0f);
            }, Collections.singletonList(new LiteralText("Уменьшить скорость")));
        }

        if (this.title.getString().contains("Поиск") || this.title.getString().contains("Аукционы")) {
            if (afterInit.hasPasses(50)) {
                if (minPrice == null) minPrice = calculateMinPriceSlot();
            }
        }

        if (minPrice != null) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) this.x, (float) this.y, 0.0F);
            this.fillGradient(matrices, minPrice.x, minPrice.y, minPrice.x + 16, minPrice.y + 16, new Color(0x9907DF43, true).getRGB(), new Color(0x9907DF43, true).getRGB());
            RenderSystem.popMatrix();
        }

        float historyX = this.x - 148;
        Blur.register(() -> Rounds.drawRound(this.x - 150, this.y, 145, this.backgroundHeight, 6, Color.BLACK));
        Blur.draw(8, ColorUtility.getColorComps(new Color(0x717171)));

        mc.textRenderer.draw(matrices, "История покупок", historyX + (145 - mc.textRenderer.getWidth("История покупок")) / 2f, y + 3, -1);
        button.render(matrices);
        speedUp.render(matrices);
        speedDown.render(matrices);
        mc.textRenderer.draw(matrices, "V: " + (int)rotationSpeed, this.x - 60, this.y + this.backgroundHeight + 29, -1);

        historyManager.render(matrices, historyX, this.y + 16, 145, this.backgroundHeight);

        long now = System.currentTimeMillis();
        currentAngle += rotationSpeed * (now - lastMs) / 10f;
        lastMs = now;

        matrices.push();

        float scale = 25.0f;
        String logo = "卐";

        float centerX = this.x + this.backgroundWidth + 100;
        float centerY = this.y + (this.backgroundHeight / 2f);

        matrices.translate(centerX, centerY, 0);
        matrices.multiply(net.minecraft.util.math.Vec3f.POSITIVE_Z.getDegreesQuaternion(currentAngle % 360));
        matrices.scale(scale, scale, 1.0f);

        float tw = mc.textRenderer.getWidth(logo);
        float th = 8;

        mc.textRenderer.draw(matrices, logo, -tw / 2f, -th / 2f, 0xFF00FF00);

        matrices.pop();
    }

    @Override
    public void renderBackground(MatrixStack matrices) {}

    public Slot calculateMinPriceSlot() {
        Slot minPriceSlot = null;
        int minPrice = Integer.MAX_VALUE;
        for (int i = 0; i < this.getScreenHandler().slots.size(); i++) {
            Slot slot = this.getScreenHandler().getSlot(i);
            if (!slot.getStack().isEmpty()) {
                int price = Utils.getPrice(slot.getStack());
                if (price == -1 || Utils.getSeller(slot.getStack()).equalsIgnoreCase(mc.getSession().getUsername())) continue;
                price = price / slot.getStack().getCount();
                CollectItem item = SpookyBuy.getInstance().getAutoBuy().getItem(slot.getStack());
                if (item != null && price < minPrice) {
                    minPrice = price;
                    minPriceSlot = slot;
                }
            }
        }
        return minPriceSlot;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.button != null) {
            this.button.mouseClicked(mouseX, mouseY, button);
            this.speedUp.mouseClicked(mouseX, mouseY, button);
            this.speedDown.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        HistoryManager.getInstance().scroll += (float) (amount * 16);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}