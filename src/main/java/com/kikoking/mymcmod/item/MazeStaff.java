package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
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
    private static final int MAZE_SIZE = 50; // must be even number
    private static final int MAZE_HEIGHT = 10;
    private static final int MAX_MONSTER_PER_FLOOR = 4;
    private static final Random random = new Random();
    private static final Tuple<Block, EntityType>[] blockTypeByTowerLevel = new Tuple[]{
            new Tuple<>(Blocks.BEDROCK, EntityType.ZOMBIE),
            new Tuple<>(Blocks.POLISHED_DEEPSLATE, EntityType.SPIDER),
            new Tuple<>(Blocks.BEDROCK, EntityType.SKELETON),
            new Tuple<>(Blocks.POLISHED_DEEPSLATE, EntityType.ZOMBIE_VILLAGER),
            new Tuple<>(Blocks.BEDROCK, EntityType.PILLAGER),
            new Tuple<>(Blocks.POLISHED_DEEPSLATE, EntityType.HUSK),
            new Tuple<>(Blocks.BEDROCK, EntityType.VINDICATOR),
            new Tuple<>(Blocks.POLISHED_DEEPSLATE, EntityType.WITCH),
            new Tuple<>(Blocks.BEDROCK, EntityType.ILLUSIONER),
    };

    public MazeStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        BlockHitResult ray = rayTrace(world, player, ClipContext.Fluid.NONE);
        BlockPos lookPos = ray.getBlockPos().relative(ray.getDirection());

        for(int floorLevel = 0; floorLevel < MAZE_HEIGHT; floorLevel++){
            int blockTypeIdx = floorLevel >= blockTypeByTowerLevel.length ? blockTypeByTowerLevel.length -1 : floorLevel;
            Block blockType = blockTypeByTowerLevel[blockTypeIdx].getA();

            fillFloor(world, floorLevel, lookPos, blockType);
            generateMaze(world, floorLevel, lookPos, blockTypeIdx);
        }

        return super.use(world, player, hand);
    }

    public static void fillFloor(Level world, int floorLevel, BlockPos lookPos, Block blockType) {
        int floorLevelOffset = getFloorLevelOffset(floorLevel);
        for(int z = 0; z < MAZE_SIZE; z++){
            for(int x = 0; x < MAZE_SIZE; x++){
                int xPos = lookPos.getX()+x;
                int yPos = lookPos.getY()+floorLevelOffset;
                int zPos = lookPos.getZ()+z;

                if(floorLevel == 0){
                    BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
                    world.setBlockAndUpdate(blockPos, blockType.defaultBlockState());
                }

                BlockPos blockPos = new BlockPos(xPos, yPos+1, zPos);
                world.setBlockAndUpdate(blockPos, blockType.defaultBlockState());

                BlockPos blockAbovePos = new BlockPos(xPos, yPos+2, zPos);
                world.setBlockAndUpdate(blockAbovePos, blockType.defaultBlockState());

                BlockPos ceilingBlockPos = new BlockPos(xPos, yPos+3, zPos);
                world.setBlockAndUpdate(ceilingBlockPos, blockType.defaultBlockState());
            }
        }
    }

    private static MazeNode carveMazePath(Level world, MazeNode mazeNode, Stack<MazeNode> backTrackStack, EntityType monsterEntityType, int yPos) {

        List<MazeNode> visitableDirections = getVisitableDirections(mazeNode);

        if(visitableDirections.isEmpty()){
            if(backTrackStack.isEmpty()){
                return null;
            }
            MazeNode backTrackDirection = backTrackStack.pop();
            return carveMazePath(world, backTrackDirection, backTrackStack, monsterEntityType, yPos);
        }

        backTrackStack.push(mazeNode);

        Collections.shuffle(visitableDirections);
        MazeNode forwardDirection = visitableDirections.get(0);


        Integer coordDiff = getPrevNextCoordinateDiff(mazeNode.xCoordinate, forwardDirection.xCoordinate);

        if(coordDiff != null){
            // Carve between nodes horizontally
            BlockPos blockMid = new BlockPos(coordDiff, yPos + 1, mazeNode.zCoordinate);
            world.setBlockAndUpdate(blockMid, Blocks.VOID_AIR.defaultBlockState());
            BlockPos blockMidAbove = new BlockPos(coordDiff, yPos + 2, mazeNode.zCoordinate);
            world.setBlockAndUpdate(blockMidAbove, Blocks.VOID_AIR.defaultBlockState());
        } else {
            // Carve between nodes vertically
            coordDiff = getPrevNextCoordinateDiff(mazeNode.zCoordinate, forwardDirection.zCoordinate);

            BlockPos blockMid = new BlockPos(mazeNode.xCoordinate, yPos + 1, coordDiff);
            world.setBlockAndUpdate(blockMid, Blocks.VOID_AIR.defaultBlockState());
            BlockPos blockMidAbove = new BlockPos(mazeNode.xCoordinate, yPos + 2, coordDiff);
            world.setBlockAndUpdate(blockMidAbove, Blocks.VOID_AIR.defaultBlockState());
        }

        BlockPos blockCurr = new BlockPos(mazeNode.xCoordinate, yPos + 1, mazeNode.zCoordinate);
        world.setBlockAndUpdate(blockCurr, Blocks.VOID_AIR.defaultBlockState());
        BlockPos blockCurrAbove = new BlockPos(mazeNode.xCoordinate, yPos + 2, mazeNode.zCoordinate);
        world.setBlockAndUpdate(blockCurrAbove, Blocks.VOID_AIR.defaultBlockState());

        BlockPos blockNext = new BlockPos(forwardDirection.xCoordinate, yPos + 1, forwardDirection.zCoordinate);
        world.setBlockAndUpdate(blockNext, Blocks.VOID_AIR.defaultBlockState());
        BlockPos blockNextAbove = new BlockPos(forwardDirection.xCoordinate, yPos + 2, forwardDirection.zCoordinate);
        world.setBlockAndUpdate(blockNextAbove, Blocks.VOID_AIR.defaultBlockState());

        // TODO: place monster randomly
//        if (mazeArr[z][x] == 0 && (z + x + random.nextInt(1, MAZE_SIZE - 1)) % (random.nextInt(1, MAZE_SIZE - 1)) == 0 && monsterPlaceCounter <= MAX_MONSTER_PER_FLOOR) {
//            Entity monster = monsterEntityType.create(world);
//            if (monster != null) {
//                monster.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
//                world.addFreshEntity(monster);
//                monsterPlaceCounter++;
//            }
//        }

        return carveMazePath(world, forwardDirection, backTrackStack, monsterEntityType, yPos);
    }

    private static Integer getPrevNextCoordinateDiff(int prevCoord, int nextCoord){

        if(prevCoord == nextCoord){
            return null;
        }

        return prevCoord < nextCoord ? prevCoord +1 : nextCoord +1;
    }

    private static List<MazeNode> getVisitableDirections(MazeNode mazeNode) {
        List<MazeNode> directionList = new ArrayList<>();
        if(mazeNode.up != null){
            if(!mazeNode.up.visited){
                directionList.add(mazeNode.up);
            }
        }
        if(mazeNode.right != null){
            if(!mazeNode.right.visited) {
                directionList.add(mazeNode.right);
            }
        }
        if(mazeNode.down != null){
            if(!mazeNode.down.visited) {
                directionList.add(mazeNode.down);
            }
        }
        if(mazeNode.left != null){
            if(!mazeNode.left.visited) {
                directionList.add(mazeNode.left);
            }
        }
        return directionList;
    }

    private static void generateMaze(Level world, int floorLevel, BlockPos lookPos, int blockTypeIdx){

        int floorLevelOffset = getFloorLevelOffset(floorLevel);
        int yPos = lookPos.getY() + floorLevelOffset;
        EntityType monsterEntityType = blockTypeByTowerLevel[blockTypeIdx].getB();

        // TODO: Create Start and Finish

        MazeGeneratorService mazeGeneratorService = new MazeGeneratorService();
        MazeNode rootMazeNode = mazeGeneratorService.generateMazeLinkedList(MAZE_SIZE, lookPos.getX(), lookPos.getZ());
        Stack<MazeNode> backTrackStack = new Stack<>();

        carveMazePath(world, rootMazeNode, backTrackStack, monsterEntityType, yPos);
    }

    private static int getFloorLevelOffset(int floorLevel){
        return floorLevel*3;
    }

    private static BlockHitResult rayTrace(Level world, Player player, ClipContext.Fluid fluidMode) {
        double range = 200;

        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 vec31 = vec3.add((double) f6 * range, (double) f5 * range, (double) f7 * range);
        return world.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, fluidMode, player));
    }
}