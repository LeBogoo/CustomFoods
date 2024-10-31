package me.lebogo.customFoods.listeners;

import me.lebogo.customFoods.CustomFood;
import me.lebogo.customFoods.CustomFoods;
import me.lebogo.customFoods.FoodManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.List;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Component title = event.getView().title();
        List<Component> children = title.children();
        if (children.isEmpty()) {
            return;
        }

        if (!(children.getFirst() instanceof TextComponent firstChild)) {
            return;
        }

        if (!(children.getLast() instanceof TextComponent lastChild)) {
            return;
        }

        FoodManager foodManager = CustomFoods.instance.foodManager;
        CustomFood food = foodManager.getFood(FoodManager.getEscapedName(lastChild.content()));
        NamespacedKey namespacedKey = new NamespacedKey(CustomFoods.instance, food.getEscapedName());

        if (firstChild.equals(CustomFoods.SHAPED_RECIPE_TEXT)) {
            // Handle shaped recipe
            // TODO - get shape from inventory
        } else if (firstChild.equals(CustomFoods.SHAPELESS_RECIPE_TEXT)) {
            ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, food.getItemStack());
            for (ItemStack content : event.getInventory().getContents()) {
                if (content != null) {
                    System.out.println("Adding ingredient: " + content.getType());
                    recipe.addIngredient(new RecipeChoice.ExactChoice(content));
                }
            }

            food.setRecipe(recipe);
            foodManager.saveFoods();

            food.registerRecipe();

            CustomFoods.sendSuccessMessage(event.getPlayer(), "Recipe saved!");



            return;
        }

        CustomFoods.sendErrorMessage(event.getPlayer(), "Failed to save recipe!");

    }
}
