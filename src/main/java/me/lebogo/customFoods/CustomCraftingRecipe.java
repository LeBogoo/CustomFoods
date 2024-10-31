package me.lebogo.customFoods;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SerializableAs("CustomCraftingRecipe")
public class CustomCraftingRecipe implements ConfigurationSerializable {
    private NamespacedKey namespacedKey;
    private String recipeType;
    private ItemStack[] ingredients;
    private ItemStack output;


    public CustomCraftingRecipe(NamespacedKey namespacedKey, String recipeType, ItemStack[] ingredients, ItemStack output) {
        this.namespacedKey = namespacedKey;
        this.recipeType = recipeType;
        this.ingredients = ingredients;
        this.output = output;
    }

    public static CustomCraftingRecipe deserialize(Map<String, Object> args) {
        String namespace = (String) args.getOrDefault("namespace", null);
        String key = (String) args.getOrDefault("key", null);
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);

        String recipeType = (String) args.getOrDefault("recipeType", "shapeless");
        List<ItemStack> ingredients = (List<ItemStack>) args.getOrDefault("ingredients", new ArrayList<ItemStack>());
        ItemStack output = (ItemStack) args.getOrDefault("output", null);

        return new CustomCraftingRecipe(namespacedKey, recipeType, ingredients.toArray(new ItemStack[0]), output);
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public void setNamespacedKey(NamespacedKey namespacedKey) {
        this.namespacedKey = namespacedKey;
    }

    public String getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(String recipeType) {
        this.recipeType = recipeType;
    }

    public ItemStack[] getIngredients() {
        return ingredients;
    }

    public void setIngredients(ItemStack[] ingredients) {
        this.ingredients = ingredients;
    }

    public ItemStack getOutput() {
        return output;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }

    private ShapelessRecipe constructShapelessRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, output);

        boolean isEmpty = true;
        for (ItemStack ingredient : ingredients) {
            if (ingredient == null) continue;
            recipe.addIngredient(ingredient);
            isEmpty = false;
        }

        if (isEmpty) {
            return null;
        }

        return recipe;
    }

    private ShapedRecipe constructShapedRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, output);

        // Define the recipe shape (3 rows of 3 columns)
        recipe.shape("ABC", "DEF", "GHI");

        // Define mapping for each slot in the ItemStack array to characters
        char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};

        boolean isEmpty = true;
        for (int i = 0; i < 9; i++) {
            if (ingredients[i] != null && ingredients[i].getType() != Material.AIR) {
                // Map the character to the ItemStack in the slot
                recipe.setIngredient(chars[i], ingredients[i]);
                isEmpty = false;
            }
        }

        if (isEmpty) {
            return null;
        }

        return recipe;
    }


    public CraftingRecipe getRecipe() {
        if (output == null) return null;

        if (recipeType.equals("shapeless")) {
            return constructShapelessRecipe();
        } else if (recipeType.equals("shaped")) {
            return constructShapedRecipe();
        }
        return null;
    }


    public void register() {
        Server server = CustomFoods.instance.getServer();

        server.removeRecipe(namespacedKey);
        CraftingRecipe recipe = getRecipe();
        if (recipe == null) return;

        CustomFoods.instance.getServer().addRecipe(getRecipe());
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("recipeType", recipeType);
        result.put("ingredients", ingredients);
        result.put("namespace", namespacedKey.getNamespace());
        result.put("key", namespacedKey.getKey());
        result.put("output", output);

        return result;
    }
}
