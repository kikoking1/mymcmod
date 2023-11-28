package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MazeCompleteLever extends LeverBlock {
    public MazeCompleteLever(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState pull(BlockState blockState, Level world, BlockPos blockPos) {
        super.pull(blockState, world, blockPos);

        System.out.println("pulled");

        return blockState;
    }
}
