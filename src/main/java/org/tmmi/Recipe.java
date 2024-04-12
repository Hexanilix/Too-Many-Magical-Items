package org.tmmi;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Recipe {
    public static List<Recipe> recipies = new ArrayList<>();

    public static boolean isValidRecipe(@NotNull List<ItemStack> list) {
        List<ItemStack> norList = new ArrayList<>();
        for (ItemStack i : list) {
            for (int j = 0; j < i.getAmount(); j++) {
                ItemStack k = i.clone();
                k.setAmount(1);
                norList.add(k);
            }
        }
        return !norList.isEmpty() && !recipies.isEmpty() && recipies.stream()
                .anyMatch(recipe -> new HashSet<>(recipe.getIngredients()).containsAll(norList));
    }

    public static @Nullable Recipe getRecipe(List<ItemStack> list) {
        for (Recipe r : recipies)
            if (new HashSet<>(r.getIngredients()).containsAll(list)) return r;
        return null;
    }

    private final List<ItemStack> ingredients;
    private final ItemStack outcome;

    Recipe(List<ItemStack> ingredients, ItemStack outcome) {
        this.ingredients = ingredients;
        this.outcome = outcome;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }

    public ItemStack getOutcome() {
        return outcome;
    }
}
