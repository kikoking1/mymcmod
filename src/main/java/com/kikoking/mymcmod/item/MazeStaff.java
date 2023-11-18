package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.kikoking.mymcmod.block.ModBlocks.SAPPHIRE_BLOCK;

public class MazeStaff extends Item {
    private static final int MAZE_SIZE = 50; // must be even number
    private static final int MAZE_HEIGHT = 7;
    private static final int MAX_MONSTER_PER_FLOOR = 4;
    private static final Tuple<Block, EntityType>[] blockTypeByTowerLevel = new Tuple[]{
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.HUSK),
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.SPIDER),
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.SKELETON),
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.ZOMBIE_VILLAGER),
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.PILLAGER),
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.VINDICATOR),
            new Tuple<>(Blocks.GOLD_BLOCK, EntityType.WITCH),
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
        // Offset -1 and +2 here so that the perimeter is a solid border.
        for(int z = -1; z < MAZE_SIZE+2; z++){
            for(int x = -1; x < MAZE_SIZE+2; x++){
                int xPos = lookPos.getX()+x;
                int yPos = lookPos.getY()+floorLevelOffset;
                int zPos = lookPos.getZ()+z;

                if(floorLevel == 0){
                    setMCBlockByCoordinates(world, blockType.defaultBlockState(), xPos, yPos, zPos);
                }

                setMCBlockByCoordinates(world, blockType.defaultBlockState(), xPos, yPos+1, zPos);
                setMCBlockByCoordinates(world, blockType.defaultBlockState(), xPos, yPos+2, zPos);
                setMCBlockByCoordinates(world, blockType.defaultBlockState(), xPos, yPos+3, zPos);
            }
        }
    }

    private static void generateMaze(Level world, int floorLevel, BlockPos lookPos, int blockTypeIdx){

        int floorLevelOffset = getFloorLevelOffset(floorLevel);
        int yPos = lookPos.getY() + floorLevelOffset;
        EntityType monsterEntityType = blockTypeByTowerLevel[blockTypeIdx].getB();

        MazeGeneratorService mazeGeneratorService = new MazeGeneratorService();

        Tuple<MazeNode, MazeNode> mazeStartEndNodes = mazeGeneratorService.generateMazeLinkedList(MAZE_SIZE, lookPos.getX(), lookPos.getZ());
        MazeNode rootMazeNode = mazeStartEndNodes.getA();
        MazeNode tailMazeNode = mazeStartEndNodes.getB();

        Stack<MazeNode> backTrackStack = new Stack<>();

        rootMazeNode.visited = true;

        if(floorLevel == 0) {
            // Carve Bottom Floor Entrance
            setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate-1, yPos + 1, rootMazeNode.zCoordinate);
            setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate-1, yPos + 2, rootMazeNode.zCoordinate);
        }

        // carve root node, starting point
        setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate, yPos + 1, rootMazeNode.zCoordinate);
        setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate, yPos + 2, rootMazeNode.zCoordinate);

        carveMazePath(world, rootMazeNode, backTrackStack, monsterEntityType, yPos);

        // carve tail node, finish point
        MazeNode finishPointNode = tailMazeNode;

        if(floorLevel % 2 != 0){
            finishPointNode = rootMazeNode;
        }

        setMCBlockByCoordinates(world, SAPPHIRE_BLOCK.get().defaultBlockState(), finishPointNode.xCoordinate, yPos, finishPointNode.zCoordinate);
        setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), finishPointNode.xCoordinate, yPos + 3, finishPointNode.zCoordinate);
    }

    private static void carveMazePath(Level world, MazeNode mazeNode, Stack<MazeNode> backTrackStack, EntityType monsterEntityType, int yPos) {

        var loopCount = 0;
        var monsterPlacedCounter = 0;
        while(true) {
            List<MazeNode> visitableDirections = getVisitableDirections(mazeNode);

            if (visitableDirections.isEmpty()) {
                if (backTrackStack.isEmpty()) {
                    break;
                }
                MazeNode backTrackDirection = backTrackStack.pop();
                mazeNode = backTrackDirection;
                continue;
            }

            backTrackStack.push(mazeNode);

            Collections.shuffle(visitableDirections);
            MazeNode forwardDirection = visitableDirections.get(0);

            // carve between nodes
            Integer coordBetween = getPrevNextCoordinateDiff(mazeNode.xCoordinate, forwardDirection.xCoordinate);
            if (coordBetween != null) {
                // Carve between nodes x axis
                setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), coordBetween, yPos + 1, mazeNode.zCoordinate);
                setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), coordBetween, yPos + 2, mazeNode.zCoordinate);
            } else {
                // Carve between nodes z axis
                coordBetween = getPrevNextCoordinateDiff(mazeNode.zCoordinate, forwardDirection.zCoordinate);

                setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), mazeNode.xCoordinate, yPos + 1, coordBetween);
                setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), mazeNode.xCoordinate, yPos + 2, coordBetween);
            }

            // carve next
            setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), forwardDirection.xCoordinate, yPos + 1, forwardDirection.zCoordinate);
            setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), forwardDirection.xCoordinate, yPos + 2, forwardDirection.zCoordinate);

            if (loopCount % 3 == 0 && monsterPlacedCounter < MAX_MONSTER_PER_FLOOR) {
                Entity monster = monsterEntityType.create(world);
                if (monster != null) {
                    monster.setPos(mazeNode.xCoordinate, yPos + 1, mazeNode.zCoordinate);
                    world.addFreshEntity(monster);
                    monsterPlacedCounter++;
                }
            }
            forwardDirection.visited = true;

            mazeNode = forwardDirection;
            loopCount++;
        }
    }

    private static void setMCBlockByCoordinates(Level world, BlockState blockState, int xCoordinate, int yCoordinate, int zCoordinate){
        world.setBlockAndUpdate(new BlockPos(xCoordinate, yCoordinate, zCoordinate), blockState);
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