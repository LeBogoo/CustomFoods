package me.lebogo.customFoods.listeners;

import me.lebogo.customFoods.CustomFood;
import me.lebogo.customFoods.CustomFoods;
import me.lebogo.customFoods.FoodManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity player = event.getPlayer();
        Inventory inventory = event.getInventory();
        Component title = event.getView().title();
        List<Component> children = title.children();

        if (children.isEmpty()) {
            return;
        }

        if (!(children.getLast() instanceof TextComponent lastChild)) {
            return;
        }

        FoodManager foodManager = CustomFoods.instance.foodManager;
        CustomFood food = foodManager.getFood(FoodManager.getEscapedName(lastChild.content()));

        food.getCustomRecipe().setIngredients(inventory.getContents());
        foodManager.saveFoods();

        food.registerRecipe();

        CustomFoods.sendSuccessMessage(player, "Recipe saved!");
    }
}
