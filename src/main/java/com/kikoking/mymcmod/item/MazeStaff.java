package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.kikoking.mymcmod.block.ModBlocks.SAPPHIRE_BLOCK;

public class MazeStaff extends Item {
    private static final int MAZE_SIZE = 24; // must be even number, divisible by 4
    private static final int ATTACK_DAMAGE = 3;
    private static final int MAZE_HEIGHT = 11;
    private static final Tuple<Block, EntityType>[] blockTypeByTowerLevel = new Tuple[]{
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.PILLAGER),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.PILLAGER),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.PILLAGER),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.VINDICATOR),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.VINDICATOR),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.WITHER_SKELETON),
            new Tuple<>(Blocks.DIAMOND_BLOCK, EntityType.WITCH),
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
            Block blockType = MAZE_HEIGHT == 1 ? Blocks.GRASS_BLOCK : blockTypeByTowerLevel[blockTypeIdx].getA();
            EntityType monsterEntityType = blockTypeByTowerLevel[blockTypeIdx].getB();

            fillFloor(world, floorLevel, lookPos, blockType, floorLevel + 1 == MAZE_HEIGHT);
            generateMaze(world, floorLevel, lookPos, monsterEntityType, floorLevel + 1 == MAZE_HEIGHT);
        }

        return super.use(world, player, hand);
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity enemy, LivingEntity player) {
        enemy.setHealth(enemy.getHealth() - ATTACK_DAMAGE);
        super.hurtEnemy(itemStack, enemy, player);
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        // Set the duration for how long the player can use the item (in ticks)
        return 200; // 10 seconds (20 ticks per second)
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public void fillFloor(Level world, int floorLevel, BlockPos lookPos, Block blockType, boolean isLastFloor) {
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

                // ceiling
                // don't create ceiling if it's the last floor
                if (!isLastFloor) {
                    setMCBlockByCoordinates(world, blockType.defaultBlockState(), xPos, yPos+3, zPos);
                }

            }
        }
    }

    private void generateMaze(Level world, int floorLevel, BlockPos lookPos, EntityType monsterEntityType, boolean isLastFloor){

        int floorLevelOffset = getFloorLevelOffset(floorLevel);
        int yPos = lookPos.getY() + floorLevelOffset;

        MazeGeneratorService mazeGeneratorService = new MazeGeneratorService();

        Tuple<MazeNode, MazeNode> mazeStartEndNodes = mazeGeneratorService.generateMazeLinkedList(MAZE_SIZE, lookPos.getX(), lookPos.getZ());
        MazeNode rootMazeNode = mazeStartEndNodes.getA();
        MazeNode tailMazeNode = mazeStartEndNodes.getB();

        Stack<MazeNode> backTrackStack = new Stack<>();

        rootMazeNode.visited = true;

        if(floorLevel == 0) {
            // Carve Bottom Floor Entrance
            setMCBlockByCoordinates(world, Blocks.OAK_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_90), rootMazeNode.xCoordinate-3, yPos-1, rootMazeNode.zCoordinate);
            setMCBlockByCoordinates(world, Blocks.OAK_STAIRS.defaultBlockState().rotate(Rotation.CLOCKWISE_90), rootMazeNode.xCoordinate-2, yPos, rootMazeNode.zCoordinate);
            setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate-1, yPos + 1, rootMazeNode.zCoordinate);
            setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate-1, yPos + 2, rootMazeNode.zCoordinate);
        }

        // carve root node, starting point
        setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate, yPos + 1, rootMazeNode.zCoordinate);
        setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), rootMazeNode.xCoordinate, yPos + 2, rootMazeNode.zCoordinate);

        // Run maze carve(generation) algorithm
        carveMazePath(world, rootMazeNode, backTrackStack, monsterEntityType, yPos);

        // carve finish point
        MazeNode finishPointNode = tailMazeNode;

        boolean isOddFloor = floorLevel % 2 != 0;

        // Alternate finish points on either side of the maze, for each floor
        if(isOddFloor) {
            finishPointNode = rootMazeNode;
        }

        setMazeFloorExit(world, isOddFloor, isLastFloor, finishPointNode.xCoordinate, yPos, finishPointNode.zCoordinate);
    }

    private void setMazeFloorExit(Level world, boolean isOddFloor, boolean isLastFloor, int xPos, int yPos, int zPoz) {
        setMCBlockByCoordinates(world, SAPPHIRE_BLOCK.get().defaultBlockState(), xPos, yPos, zPoz);
        setMCBlockByCoordinates(world, Blocks.VOID_AIR.defaultBlockState(), xPos, yPos + 3, zPoz);
        if(isOddFloor) {
            setMCBlockByCoordinates(world, Blocks.LADDER.defaultBlockState().rotate(Rotation.CLOCKWISE_180), xPos, yPos + 1, zPoz);
            setMCBlockByCoordinates(world, Blocks.LADDER.defaultBlockState().rotate(Rotation.CLOCKWISE_180), xPos, yPos + 2, zPoz);
            if(!isLastFloor){
                setMCBlockByCoordinates(world, Blocks.LADDER.defaultBlockState().rotate(Rotation.CLOCKWISE_180), xPos, yPos + 3, zPoz);
            } else {
                setMazeChest(world, xPos - 1, yPos + 3, zPoz - 1, Rotation.CLOCKWISE_180);
            }
        } else {
            setMCBlockByCoordinates(world, Blocks.LADDER.defaultBlockState(), xPos, yPos + 1, zPoz);
            setMCBlockByCoordinates(world, Blocks.LADDER.defaultBlockState(), xPos, yPos + 2, zPoz);
            if(!isLastFloor) {
                setMCBlockByCoordinates(world, Blocks.LADDER.defaultBlockState(), xPos, yPos + 3, zPoz);
            } else {
                setMazeChest(world, xPos + 1, yPos + 3, zPoz + 1, null);
            }
        }
    }

    private void setMazeChest(Level world, int xPos, int yPos, int zPoz, Rotation rotation) {
        BlockPos chestBlockPos;
        if(rotation != null){
            chestBlockPos = setMCBlockByCoordinates(world, Blocks.CHEST.defaultBlockState().rotate(rotation), xPos, yPos, zPoz);
        } else {
            chestBlockPos = setMCBlockByCoordinates(world, Blocks.CHEST.defaultBlockState(), xPos, yPos, zPoz);
        }

        // Get the TileEntity at the specified position
        ChestBlockEntity chestBlockEntity = (ChestBlockEntity) world.getBlockEntity(chestBlockPos);

        // Populate the chest with items (replace with your desired items)
        if (chestBlockEntity != null) {
            ItemStack itemStack1 = new ItemStack(Blocks.DIAMOND_BLOCK);
            ItemStack itemStack2 = new ItemStack(Blocks.GOLD_BLOCK);
            ItemStack itemStack3 = new ItemStack(Blocks.IRON_BLOCK);

            chestBlockEntity.setItem(0, itemStack1);
            chestBlockEntity.setItem(1, itemStack2);
            chestBlockEntity.setItem(2, itemStack3);
        }
    }

    private void carveMazePath(Level world, MazeNode mazeNode, Stack<MazeNode> backTrackStack, EntityType monsterEntityType, int yPos) {

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

            if (loopCount > MAZE_SIZE * 2 && loopCount % 7 == 0 && monsterPlacedCounter < MAZE_SIZE / 4) {
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

    private BlockPos setMCBlockByCoordinates(Level world, BlockState blockState, int xCoordinate, int yCoordinate, int zCoordinate){
        BlockPos blockPos = new BlockPos(xCoordinate, yCoordinate, zCoordinate);
        world.setBlockAndUpdate(new BlockPos(xCoordinate, yCoordinate, zCoordinate), blockState);

        return blockPos;
    }

    private Integer getPrevNextCoordinateDiff(int prevCoord, int nextCoord){

        if(prevCoord == nextCoord){
            return null;
        }

        return prevCoord < nextCoord ? prevCoord +1 : nextCoord +1;
    }

    private List<MazeNode> getVisitableDirections(MazeNode mazeNode) {
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

    private int getFloorLevelOffset(int floorLevel){
        return floorLevel*3;
    }

    private BlockHitResult rayTrace(Level world, Player player, ClipContext.Fluid fluidMode) {
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