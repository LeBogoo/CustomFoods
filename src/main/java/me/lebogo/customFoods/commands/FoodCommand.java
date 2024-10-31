package me.lebogo.customFoods.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.lebogo.customFoods.CustomFoods;
import me.lebogo.customFoods.CustomFood;
import me.lebogo.customFoods.FoodManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FoodCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (args.length < 1) {
            CustomFoods.sendErrorMessage(sender, "Usage: /food <create|edit|get> [food name]");
            return;
        }

        switch (args[0]) {
            case "create":
                createFood(sender, args);
                break;
            case "edit":
                editFood(sender, args);
                break;
            case "delete":
                deleteFood(sender, args);
                break;
            case "get":
                getFood(sender, args);
                break;
        }
    }


    private void createFood(CommandSender sender, String[] args) {
        // /food create <name>
        if (args.length < 2) {
            CustomFoods.sendErrorMessage(sender, "Usage: /food create <food name>");
            return;
        }

        // all args joined by space
        String foodName = String.join(" ", args).substring(args[0].length() + 1);
        String escapedName = foodName.replace(" ", "_").toLowerCase();

        FoodManager foodManager = CustomFoods.instance.foodManager;
        if (foodManager.getFood(escapedName) != null) {
            CustomFoods.sendErrorMessage(sender, "Food \"" + foodName + "\" already exists.");
            return;
        }

        CustomFood food = new CustomFood(foodName);
        foodManager.createFood(food);
        CustomFoods.sendSuccessMessage(sender, "Food \"" + foodName + "\" was created. It still needs to be configured using \"/food edit " + escapedName + "\".");
    }

    private void editFood(CommandSender sender, String[] args) {
        if (args.length < 3) {
            // TODO - Add support for status effects
            CustomFoods.sendErrorMessage(sender, "Usage: /food edit <food name> <crafting|item|saturation|hunger>");
            return;
        }

        FoodManager foodManager = CustomFoods.instance.foodManager;
        CustomFood food = foodManager.getFood(args[1]);
        if (food == null) {
            CustomFoods.sendErrorMessage(sender, "Food \"" + args[1] + "\" does not exist.");
            return;
        }

        switch (args[2]) {
            case "crafting":
                if (!(sender instanceof Player player)) {
                    CustomFoods.sendErrorMessage(sender, "Only players can edit crafting recipes.");
                    return;
                }

                if (food.getItemStack() == null) {
                    CustomFoods.sendErrorMessage(sender, "Food \"" + args[1] + "\" does not have an item set. Configure it using \"/food edit " + args[1] + " item\".");
                    return;
                }

                // check next arg if it is shapeless or shaped
                if (args.length < 4) {
                    // TODO - Add support for shaped recipes
                    CustomFoods.sendErrorMessage(sender, "Usage: /food edit " + args[1] + " crafting shapeless");
                    return;
                }

                // TODO - Add support for shaped recipes
                if (!args[3].equalsIgnoreCase("shapeless") /*&& !args[3].equalsIgnoreCase("shaped")*/) {
                    CustomFoods.sendErrorMessage(sender, "Invalid crafting type: " + args[3]);
                    return;
                }

                Component recipeTypeText = CustomFoods.SHAPED_RECIPE_TEXT;
                if (args[3].equalsIgnoreCase("shapeless")) {
                    recipeTypeText = CustomFoods.SHAPELESS_RECIPE_TEXT;
                }

                Inventory recipeInventory = player.getServer().createInventory(player, InventoryType.DISPENSER, Component.textOfChildren(recipeTypeText, Component.text(" for "), Component.text(food.getName())));
                if (args[3].equalsIgnoreCase("shapeless") && food.getRecipe() instanceof ShapelessRecipe shapelessRecipe) {
                    int index = 0;
                    for (RecipeChoice recipeChoice : shapelessRecipe.getChoiceList()) {
                        if (!(recipeChoice instanceof RecipeChoice.ExactChoice exactChoice)) continue;
                        recipeInventory.setItem(index++, exactChoice.getItemStack());
                        System.out.println(index);
                    }
                }

                player.openInventory(recipeInventory);

                break;
            case "item":
                if (!(sender instanceof Player player)) {
                    CustomFoods.sendErrorMessage(sender, "Only players can set a food item.");
                    return;
                }

                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (itemInMainHand.getType() == Material.AIR) {
                    CustomFoods.sendErrorMessage(sender, "You must be holding an item to set it as a food item.");
                    return;
                }

                food.setItemStack(itemInMainHand.clone());
                foodManager.saveFoods();

                food.registerRecipe();

                String itemText = itemInMainHand.getType().toString();
                ItemMeta itemMeta = itemInMainHand.getItemMeta();
                if (itemMeta.hasCustomModelData()) {
                    itemText += " with custom model data " + itemMeta.getCustomModelData();
                }

                CustomFoods.sendSuccessMessage(sender, "Item for " + food.getName() + " set to " + itemText);
                break;
            case "saturation":
                if (args.length < 4) {
                    CustomFoods.sendErrorMessage(sender, "Usage: /food edit " + args[1] + " saturation <saturation>");
                    return;
                }

                try {
                    int saturation = Integer.parseInt(args[3]);

                    if (saturation < 0) {
                        CustomFoods.sendErrorMessage(sender, "Saturation value must be greater than or equal to 0.");
                        return;
                    }

                    food.setSaturation(saturation);
                    foodManager.saveFoods();
                    CustomFoods.sendSuccessMessage(sender, "Saturation for " + food.getName() + " set to " + saturation);
                } catch (NumberFormatException e) {
                    CustomFoods.sendErrorMessage(sender, "Invalid saturation value: " + args[3]);
                    return;
                }
                break;
            case "hunger":
                if (args.length < 4) {
                    CustomFoods.sendErrorMessage(sender, "Usage: /food edit " + args[1] + " hunger <hunger>");
                    return;
                }

                try {
                    int hunger = Integer.parseInt(args[3]);

                    if (hunger < 0) {
                        CustomFoods.sendErrorMessage(sender, "Hunger value must be greater than or equal to 0.");
                        return;
                    }

                    food.setHunger(hunger);
                    foodManager.saveFoods();
                    CustomFoods.sendSuccessMessage(sender, "Hunger for " + food.getName() + " set to " + hunger);
                } catch (NumberFormatException e) {
                    CustomFoods.sendErrorMessage(sender, "Invalid hunger value: " + args[3]);
                    return;
                }
                break;
            // TODO - Add support for status effects
            //case "effects":
            //    break;
            default:
                CustomFoods.sendErrorMessage(sender, "Invalid edit option: " + args[2]);
                break;
        }
    }

    private void deleteFood(CommandSender sender, String[] args) {
        if (args.length < 2) {
            CustomFoods.sendErrorMessage(sender, "Usage: /food delete <food name>");
            return;
        }

        FoodManager foodManager = CustomFoods.instance.foodManager;
        if (!foodManager.deleteFood(args[1])) {
            CustomFoods.sendErrorMessage(sender, "Food \"" + args[1] + "\" does not exist.");
            return;
        }

        foodManager.saveFoods();

        CustomFoods.sendSuccessMessage(sender, "Food \"" + args[1] + "\" was deleted.");
    }

    private void getFood(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            CustomFoods.sendErrorMessage(sender, "Only players can use this command.");
            return;
        }

        if (args.length < 2) {
            CustomFoods.sendErrorMessage(sender, "Usage: /food get <food name>");
            return;
        }

        FoodManager foodManager = CustomFoods.instance.foodManager;
        CustomFood food = foodManager.getFood(args[1]);
        if (food == null) {
            CustomFoods.sendErrorMessage(sender, "Food \"" + args[1] + "\" does not exist.");
            return;
        }

        ItemStack itemStack = food.getItemStack();

        if (itemStack == null) {
            CustomFoods.sendErrorMessage(sender, "Food \"" + args[1] + "\" does not have an item set. Configure it using \"/food edit " + args[1] + " item\".");
            return;
        }

        player.getInventory().addItem(itemStack);
        CustomFoods.sendSuccessMessage(sender, "You received " + food.getName() + ".");
    }


    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        List<String> finalSuggestions = new ArrayList<>();

        if (args.length <= 1) {
            List<String> suggestions = List.of("create", "edit", "delete", "get");
            if (args.length == 0) {
                return suggestions;
            }

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(args[0])) {
                    finalSuggestions.add(suggestion);
                }
            }
        }

        if (args.length == 2) {
            FoodManager foodManager = CustomFoods.instance.foodManager;
            List<String> suggestions = switch (args[0]) {
                case "edit", "delete", "get" -> foodManager.getEscapedFoodNames();
                default -> List.of();
            };

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(args[1])) {
                    finalSuggestions.add(suggestion);
                }
            }
        }

        if (args.length == 3 && args[0].equals("edit")) {
            // TODO - Add support for status effects
            List<String> suggestions = List.of("crafting", "item", "saturation", "hunger");

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(args[2])) {
                    finalSuggestions.add(suggestion);
                }
            }
        }

        if (args.length == 4 && args[0].equals("edit") && args[2].equals("crafting")) {
            // TODO - Add support for shaped recipes
            List<String> suggestions = List.of("shapeless");

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(args[3])) {
                    finalSuggestions.add(suggestion);
                }
            }
        }

        return finalSuggestions;
    }

    @Override
    public @Nullable String permission() {
        return "customfoods.command.food";
    }
}
