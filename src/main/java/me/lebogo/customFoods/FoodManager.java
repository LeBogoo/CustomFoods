package me.lebogo.customFoods;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodManager {

    private final Plugin plugin;
    Map<String, CustomFood> foods = new HashMap<>();

    public FoodManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void loadFoods() {
        FileConfiguration config = this.plugin.getConfig();
        for (String key : config.getKeys(false)) {
            CustomFood food = (CustomFood) config.get(key);
            foods.put(key, food);
        }
    }

    public void saveFoods() {
        FileConfiguration config = this.plugin.getConfig();

        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        for (Map.Entry<String, CustomFood> entry : foods.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        this.plugin.saveConfig();
    }

    public boolean createFood(CustomFood food) {
        if (foods.containsKey(food.getEscapedName())) {
            return false;
        }

        foods.put(food.getEscapedName(), food);
        saveFoods();
        return true;
    }

    public boolean editFood(CustomFood food) {
        if (!foods.containsKey(food.getEscapedName())) {
            return false;
        }

        foods.put(food.getEscapedName(), food);
        saveFoods();

        return true;
    }

    public boolean deleteFood(String name) {
        if (!foods.containsKey(name)) {
            return false;
        }

        foods.remove(name);
        saveFoods();

        return true;
    }

    public CustomFood getFood(String name) {
        return foods.get(name);
    }

    public List<String> getEscapedFoodNames() {
        return List.of(foods.keySet().toArray(new String[0]));
    }

    public static String getEscapedName(String name) {
        return name.replace(" ", "_").toLowerCase();
    }
}
