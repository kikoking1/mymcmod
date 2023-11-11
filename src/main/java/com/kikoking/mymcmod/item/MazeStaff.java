package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

//player.setPos(lookPos.getX(), lookPos.getY(), lookPos.getZ());

public class MazeStaff extends Item {

    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final int MAZE_SIZE = 50;
    private static final int MAZE_HEIGHT = 10;
    private static final int MAX_MONSTER_PER_FLOOR = 4;
    private static final Random random = new Random();
    private static final Tuple<Block, EntityType>[] blockTypeByTowerLevel = new Tuple[]{
            new Tuple<>(Blocks.BEDROCK, EntityType.ZOMBIE),
            new Tuple<>(Blocks.BEDROCK, EntityType.SPIDER),
            new Tuple<>(Blocks.BEDROCK, EntityType.SKELETON),
            new Tuple<>(Blocks.BEDROCK, EntityType.ZOMBIE_VILLAGER),
            new Tuple<>(Blocks.BEDROCK, EntityType.PILLAGER),
            new Tuple<>(Blocks.BEDROCK, EntityType.HUSK),
            new Tuple<>(Blocks.BEDROCK, EntityType.VINDICATOR),
            new Tuple<>(Blocks.BEDROCK, EntityType.WITCH),
            new Tuple<>(Blocks.BEDROCK, EntityType.ILLUSIONER),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.WARDEN),
    };

    public MazeStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        BlockHitResult ray = rayTrace(world, player, ClipContext.Fluid.NONE);
        BlockPos lookPos = ray.getBlockPos().relative(ray.getDirection());

        for(int x = 0; x < MAZE_HEIGHT; x++){
            setMazeFloorLevel(world, x, lookPos, player.getDirection());
        }

        return super.use(world, player, hand);
    }

    private static BlockHitResult rayTrace(Level world, Player player, ClipContext.Fluid fluidMode) {
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

    private static void setMazeFloorLevel(Level world, int floorLevel, BlockPos lookPos, Direction playerDirection){

        int floorLevelOffset = floorLevel*3;
        int blockTypeIdx = floorLevel >= blockTypeByTowerLevel.length ? blockTypeByTowerLevel.length -1 : floorLevel;
        Block blockType = blockTypeByTowerLevel[blockTypeIdx].getA();
        EntityType monsterEntityType = blockTypeByTowerLevel[blockTypeIdx].getB();
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

        int monsterPlaceCounter = 1;
        for(int z = 0; z < mazeArr.length; z++){
            for(int x = 0; x < mazeArr[z].length; x++){
                int xPos = lookPos.getX()+x;
                int yPos = lookPos.getY()+floorLevelOffset;
                int zPos = lookPos.getZ()+z;

                if(floorLevel == 0){
                    BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
                    world.setBlockAndUpdate(blockPos, blockType.defaultBlockState());
                }

                if(mazeArr[z][x] == 1){
                    BlockPos blockPos = new BlockPos(xPos, yPos+1, zPos);
                    world.setBlockAndUpdate(blockPos, blockType.defaultBlockState());
                    BlockPos blockAbovePos = new BlockPos(xPos, yPos+2, zPos);
                    world.setBlockAndUpdate(blockAbovePos, blockType.defaultBlockState());
                } else {
                    BlockPos blockPos = new BlockPos(xPos, yPos+1, zPos);
                    world.setBlockAndUpdate(blockPos, Blocks.VOID_AIR.defaultBlockState());
                    BlockPos blockAbovePos = new BlockPos(xPos, yPos+2, zPos);
                    world.setBlockAndUpdate(blockAbovePos, Blocks.VOID_AIR.defaultBlockState());
                    if(mazeArr[z][x] == 0 && (z+x+random.nextInt(1, MAZE_SIZE-1)) % (random.nextInt(1, MAZE_SIZE-1)) == 0 && monsterPlaceCounter <= MAX_MONSTER_PER_FLOOR){
                        Entity monster = monsterEntityType.create(world);
                        if(monster != null){
                            monster.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                            world.addFreshEntity(monster);
                            monsterPlaceCounter++;
                        }
                    }
                }
                BlockPos ceilingBlockPos = new BlockPos(xPos, yPos+3, zPos);
                world.setBlockAndUpdate(ceilingBlockPos, blockType.defaultBlockState());
            }
        }
    }

    // Place a spawner block with a zombie entity in it at the specified position
    public static void placeSpawnerBlock(Level world, BlockPos pos, EntityType entityType) {
        // Set the block state to mob spawner
        world.setBlockAndUpdate(pos, Blocks.SPAWNER.defaultBlockState());

        // Get the tile entity for the mob spawner
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof SpawnerBlockEntity spawnerTileEntity) {
            // Set the spawner entity type to a zombie
            spawnerTileEntity.getSpawner().setEntityId(entityType, world, RandomSource.create(), pos);
            spawnerTileEntity.setChanged();
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
}
