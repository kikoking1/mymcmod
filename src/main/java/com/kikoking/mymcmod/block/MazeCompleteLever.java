package com.kikoking.mymcmod.block;

import com.kikoking.mymcmod.item.MazeStaff;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MazeCompleteLever extends LeverBlock {
    public MazeCompleteLever(Properties properties) {
        super(properties);
    }

    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        var xPos = blockPos.getX()-10;
        var yPos = blockPos.getY();
        var zPos = blockPos.getZ()-10;

        for (var x = 0; x < 20; x++) {
            for (var z = 0; z < 20; z++) {
                MazeStaff.setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), x + xPos, yPos, z + zPos);
                MazeStaff.setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), x + xPos, yPos + 1, z + zPos);
            }
        }

       return super.use(blockState, world, blockPos, player, interactionHand, blockHitResult);
    }
}
