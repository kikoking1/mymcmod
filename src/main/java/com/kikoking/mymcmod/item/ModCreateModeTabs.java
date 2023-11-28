package com.kikoking.mymcmod.item;

import com.kikoking.mymcmod.MyMcMod;
import com.kikoking.mymcmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreateModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyMcMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MYMC_TAB = CREATIVE_MODE_TABS.register("mymc_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SAPPHIRE.get()))
                    .title(Component.translatable("creativetab.mymc_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.SAPPHIRE.get());
                        pOutput.accept(ModItems.RAW_SAPPHIRE.get());
                        pOutput.accept(ModItems.ENDER_DRAGON_SPAWN_EGG.get());
                        pOutput.accept(ModItems.GIANT_SPAWN_EGG.get());
                        pOutput.accept(ModItems.ILLUSIONER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.MAZE_STAFF.get());
                        pOutput.accept(ModItems.WITHER_SPAWN_EGG.get());

                        pOutput.accept(ModBlocks.SAPPHIRE_BLOCK.get());
                        pOutput.accept(ModBlocks.MAZE_COMPLETE_LEVER.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
