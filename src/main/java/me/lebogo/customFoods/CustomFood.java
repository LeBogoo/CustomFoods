package me.lebogo.customFoods;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SerializableAs("CustomFood")
public class CustomFood implements ConfigurationSerializable {
    private final String name;
    private final NamespacedKey namespacedKey;
    private int hunger;
    private int saturation;
    private ItemStack itemStack;
    private CraftingRecipe recipe;

    public CustomFood(String name) {
        this(name, 0, 0, null, null);
    }

    public CustomFood(String name, int hunger, int saturation, ItemStack itemStack, CraftingRecipe recipe) {
        this.name = name;
        this.hunger = hunger;
        this.saturation = saturation;
        this.itemStack = itemStack;
        this.recipe = recipe;

        this.namespacedKey = new NamespacedKey(CustomFoods.instance, getEscapedName());

        registerRecipe();
    }

    public static CustomFood deserialize(Map<String, Object> args) {
        String name = (String) args.getOrDefault("name", null);
        int hunger = (int) args.getOrDefault("hunger", 0);
        int saturation = (int) args.getOrDefault("saturation", 0);
        ItemStack itemStack = (ItemStack) args.getOrDefault("itemStack", null);
        CraftingRecipe recipe = null;

        if (args.containsKey("recipeType")) {
            String recipeType = (String) args.get("recipeType");
            if (recipeType.equals("shapeless")) {
                List<ItemStack> ingredientList = (List<ItemStack>) args.get("recipe");
                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(CustomFoods.instance, FoodManager.getEscapedName(name)), itemStack);
                for (ItemStack ingredient : ingredientList) {
                    shapelessRecipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
                }
                recipe = shapelessRecipe;
            }

            // TODO - Add support for shaped recipes
        }

        return new CustomFood(name, hunger, saturation, itemStack, recipe);
    }

    public String getName() {
        return name;
    }

    public String getEscapedName() {
        return FoodManager.getEscapedName(name);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;

        updateItemStack();
    }

    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(CraftingRecipe recipe) {
        this.recipe = recipe;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
        updateItemStack();
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
        updateItemStack();
    }

    public void registerRecipe() {
        if (recipe != null) {
            if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                List<RecipeChoice> choiceList = shapelessRecipe.getChoiceList();
                shapelessRecipe = new ShapelessRecipe(namespacedKey, itemStack);
                for (RecipeChoice recipeChoice : choiceList) {
                    shapelessRecipe.addIngredient(recipeChoice);
                }

                recipe = shapelessRecipe;
            }

            CustomFoods.instance.getServer().removeRecipe(namespacedKey);
            CustomFoods.instance.getServer().addRecipe(recipe);
        }
    }

    private void updateItemStack() {
        ItemStack itemStack = getItemStack();
        itemStack.editMeta(itemMeta -> {
            FoodComponent food = itemMeta.getFood();
            food.setNutrition(hunger);
            food.setSaturation(saturation);
            itemMeta.setFood(food);

            itemMeta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        });

        // Don't use the setter here! It will cause infinite recursion.
        this.itemStack = itemStack;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("name", name);
        result.put("hunger", hunger);
        result.put("saturation", saturation);
        result.put("itemStack", itemStack);
        result.put("recipeType", null);

        if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            List<RecipeChoice> choiceList = shapelessRecipe.getChoiceList();
            List<ItemStack> ingredientList = new ArrayList<>();
            for (RecipeChoice recipeChoice : choiceList) {
                if (recipeChoice instanceof RecipeChoice.ExactChoice exactChoice) {
                    ingredientList.add(exactChoice.getItemStack());
                }
            }
            result.put("recipe", ingredientList);
            result.put("recipeType", "shapeless");
        }

        return result;
    }
}
