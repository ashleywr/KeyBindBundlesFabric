package com.matyrobbrt.keybindbundles.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class SearchTreeManager {
    private static SearchTree<ItemStack> basicSearch;

    public static void onPlayerJoin() {
        if (basicSearch != null) basicSearch = null;
    }

    public static SearchTree<ItemStack> getSearchTree() {
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            return conn.searchTrees().creativeNameSearch();
        } else {
            if (basicSearch == null) basicSearch = new MappedSearchTree<>(new IdSearchTree<>(i -> i.builtInRegistryHolder().unwrapKey().map(key -> Stream.of(key.location())).orElseGet(Stream::empty), new RegistryBackedList<>(BuiltInRegistries.ITEM, Item.class)), Item::getDefaultInstance);
            return basicSearch;
        }
    }
}
