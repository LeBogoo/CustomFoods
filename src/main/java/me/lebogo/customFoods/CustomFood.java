package me.lebogo.customFoods;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("CustomFood")
public class CustomFood implements ConfigurationSerializable {
    private final String name;
    private int hunger;
    private int saturation;
    private ItemStack itemStack;
    private CustomCraftingRecipe customRecipe;

    public CustomFood(String name) {
        this(name, 0, 0, null, new CustomCraftingRecipe(new NamespacedKey(CustomFoods.instance, FoodManager.getEscapedName(name)), "shapeless", new ItemStack[9], null));
    }

    public CustomFood(String name, int hunger, int saturation, ItemStack itemStack, CustomCraftingRecipe customRecipe) {
        this.name = name;
        this.hunger = hunger;
        this.saturation = saturation;
        this.itemStack = itemStack;
        this.customRecipe = customRecipe;

        registerRecipe();
    }

    public static CustomFood deserialize(Map<String, Object> args) {
        String name = (String) args.getOrDefault("name", null);
        int hunger = (int) args.getOrDefault("hunger", 0);
        int saturation = (int) args.getOrDefault("saturation", 0);
        ItemStack itemStack = (ItemStack) args.getOrDefault("itemStack", null);
        CustomCraftingRecipe customRecipe = (CustomCraftingRecipe) args.getOrDefault("recipe", null);

        return new CustomFood(name, hunger, saturation, itemStack, customRecipe);
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

    public CustomCraftingRecipe getCustomRecipe() {
        return customRecipe;
    }

    public void setCustomRecipe(CustomCraftingRecipe customRecipe) {
        this.customRecipe = customRecipe;
    }

    public void registerRecipe() {
        customRecipe.register();
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
        result.put("recipe", customRecipe);

        return result;
    }
}
