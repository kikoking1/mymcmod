package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

//player.setPos(lookPos.getX(), lookPos.getY(), lookPos.getZ());

public class MazeStaff extends Item {

    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final int MAZE_SIZE = 50;
    private static final int MAZE_HEIGHT = 10;
    private static final Random random = new Random();

    public MazeStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        BlockHitResult ray = rayTrace(world, player, ClipContext.Fluid.NONE);
        BlockPos lookPos = ray.getBlockPos().relative(ray.getDirection());

        for(int x = 0; x < MAZE_HEIGHT; x++){
            setMazeFloorLevel(world, x, lookPos);
        }

        return super.use(world, player, hand);
    }

    private static void setMazeFloorLevel(Level world, int floorLevel, BlockPos lookPos){

        floorLevel *= 3;
        int[][] mazeArr = generateMaze(MAZE_SIZE);

        // close the walls
        for(int z = 0; z < mazeArr.length; z++){
            mazeArr[z][0] = 1;
            mazeArr[z][mazeArr.length-1] = 1;
            mazeArr[0][z] = 1;
            mazeArr[mazeArr.length-1][z] = 1;
        }

        // Create Start
        for(int z = 0; z < mazeArr.length; z++){
            if(mazeArr[z][1] == 0){
                mazeArr[z][0] = 0;
                break;
            }
        }

        // Create Finish
        for(int z = 0; z < mazeArr.length; z++){
            if(mazeArr[z][mazeArr.length-2] == 0){
                mazeArr[z][mazeArr.length-1] = 0;
                break;
            }
        }

        for(int z = 0; z < mazeArr.length; z++){
            for(int x = 0; x < mazeArr[z].length; x++){
                if(mazeArr[z][x] == 1){
                    BlockPos block = new BlockPos(lookPos.getX()+x, (lookPos.getY()+1)+floorLevel, lookPos.getZ()+z);
                    world.setBlockAndUpdate(block, Blocks.BEDROCK.defaultBlockState());
                    BlockPos blockAbove = new BlockPos(lookPos.getX()+x, (lookPos.getY()+2)+floorLevel, lookPos.getZ()+z);
                    world.setBlockAndUpdate(blockAbove, Blocks.BEDROCK.defaultBlockState());
                } else {
                    BlockPos block = new BlockPos(lookPos.getX()+x, (lookPos.getY()+1)+floorLevel, lookPos.getZ()+z);
                    world.setBlockAndUpdate(block, Blocks.VOID_AIR.defaultBlockState());
                    BlockPos blockAbove = new BlockPos(lookPos.getX()+x, (lookPos.getY()+2)+floorLevel, lookPos.getZ()+z);
                    world.setBlockAndUpdate(blockAbove, Blocks.VOID_AIR.defaultBlockState());
                }
                BlockPos ceilingBlock = new BlockPos(lookPos.getX()+x, (lookPos.getY()+3)+floorLevel, lookPos.getZ()+z);
                world.setBlockAndUpdate(ceilingBlock, Blocks.BEDROCK.defaultBlockState());
            }
        }
    }

    private static int[][] generateMaze(int size) {
        int[][] maze = new int[size][size];

        // Initialize the maze with walls.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                maze[i][j] = WALL;
            }
        }

        // Create a random starting point.
        int startX = random.nextInt(size);
        int startY = random.nextInt(size);
        maze[startX][startY] = PATH;

        // Create a list of walls.
        List<int[]> walls = new ArrayList<>();
        walls.add(new int[]{startX, startY});

        while (!walls.isEmpty()) {
            int[] currentCell = walls.remove(random.nextInt(walls.size()));
            int x = currentCell[0];
            int y = currentCell[1];

            // Check the four neighbors.
            int[][] neighbors = {{x - 2, y}, {x + 2, y}, {x, y - 2}, {x, y + 2}};
            Collections.shuffle(Arrays.asList(neighbors));

            for (int[] neighbor : neighbors) {
                int nx = neighbor[0];
                int ny = neighbor[1];

                if (nx >= 0 && nx < size && ny >= 0 && ny < size) {
                    if (maze[nx][ny] == WALL) {
                        maze[nx][ny] = PATH;
                        maze[(x + nx) / 2][(y + ny) / 2] = PATH;
                        walls.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return maze;
    }

    protected static BlockHitResult rayTrace(Level world, Player player, ClipContext.Fluid fluidMode) {
        double range = 200;

        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 vec31 = vec3.add((double)f6 * range, (double)f5 * range, (double)f7 * range);
        return world.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, fluidMode, player));
    }


}
