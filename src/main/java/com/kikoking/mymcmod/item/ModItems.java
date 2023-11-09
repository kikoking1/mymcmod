package com.kikoking.mymcmod.item;

import com.kikoking.mymcmod.MyMcMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MyMcMod.MOD_ID);

    public static final RegistryObject<Item> SAPPHIRE = ITEMS.register("sapphire",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_SAPPHIRE = ITEMS.register("raw_sapphire",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ENDER_DRAGON_SPAWN_EGG = ITEMS.register("ender_dragon_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    () -> EntityType.ENDER_DRAGON, 0x000000, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> WITHER_SPAWN_EGG = ITEMS.register("wither_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    () -> EntityType.WITHER, 0x000000, 0xFF0000, new Item.Properties()));


    public static final RegistryObject<ForgeSpawnEggItem> GIANT_SPAWN_EGG = ITEMS.register("giant_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    () -> EntityType.GIANT, 0x00FF00, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> ILLUSIONER_SPAWN_EGG = ITEMS.register("illusioner_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    () -> EntityType.ILLUSIONER, 0x0000FF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> MAZE_STAFF = ITEMS.register("maze_staff",
            () -> new MazeStaff(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
