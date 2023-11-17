package com.kikoking.mymcmod.item;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGeneratorService {

    public MazeNode generateMazeLinkedList(int MAZE_SIZE, int xPos, int zPos) {
        MazeNode mazeRootNode = new MazeNode(xPos, zPos);
        MazeNode cellPrev = mazeRootNode;
        MazeNode rowStart = mazeRootNode;

        var y = 0;
        while(true) {
            var x = 0;
            while(x < MAZE_SIZE){
                MazeNode cellNext = new MazeNode(cellPrev.xCoordinate+2, cellPrev.zCoordinate);
                cellPrev.right = cellNext;
                cellNext.left = cellPrev;
                if(cellPrev.up != null) {
                    MazeNode cellAboveNext = cellPrev.up.right;
                    cellNext.up = cellAboveNext;
                    cellAboveNext.down = cellNext;
                }

                cellPrev = cellNext;
                x++;
            }

            // putting the stop here, so the below lines don't create an extra row with only one node
            if(y >= MAZE_SIZE){
                break;
            }

            MazeNode rowStartNext = new MazeNode(rowStart.xCoordinate, rowStart.zCoordinate+2);
            rowStart.down = rowStartNext;
            rowStartNext.up = rowStart;
            rowStart = rowStartNext;
            cellPrev = rowStartNext;
            y++;
        }

        return mazeRootNode;
    }

    public void printList(MazeNode head) {
        MazeNode current = head;
        while (current != null) {
            MazeNode right = current;
            MazeNode down = current;
            while (right != null) {
                System.out.print(right.xCoordinate +":"+ right.zCoordinate + " ");
                right = right.right;
            }
            System.out.println();
            current = down.down;
        }
    }
}

