package me.lebogo.customFoods;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.lebogo.customFoods.commands.FoodCommand;
import me.lebogo.customFoods.listeners.InventoryCloseListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomFoods extends JavaPlugin {
    public static final Component EDIT_RECIPE_TEXT = Component.text("Edit Recipe");


    public static CustomFoods instance;

    static {
        ConfigurationSerialization.registerClass(CustomCraftingRecipe.class, "CustomCraftingRecipe");
        ConfigurationSerialization.registerClass(CustomFood.class, "CustomFood");
    }

    public FoodManager foodManager;

    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message).color(TextColor.color(0xFB5454)));
    }

    public static void sendSuccessMessage(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message).color(TextColor.color(0x54FB54)));
    }

    @Override
    public void onEnable() {
        instance = this;
        foodManager = new FoodManager(this);
        foodManager.loadFoods();

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("food", new FoodCommand());
        });

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
