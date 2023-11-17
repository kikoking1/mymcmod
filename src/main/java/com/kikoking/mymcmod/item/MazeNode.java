package com.kikoking.mymcmod.item;

import net.minecraft.util.Tuple;

public class MazeNode {
        public boolean visited = false;
        public MazeNode up = null;
        public MazeNode right =  null;
        public MazeNode down = null;
        public MazeNode left = null;
        public Integer xCoordinate;
        public Integer zCoordinate;

    public MazeNode(Integer xCoordinate, Integer zCoordinate) {
        this.xCoordinate = xCoordinate;
        this.zCoordinate = zCoordinate;
    }
}
