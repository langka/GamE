package com.bupt.sworld.custom;

public class Piece {
    int state;//身份 0-6 兵 炮 车 马 象 士 将
    int side;//属于哪一边1 红色 2黑色

    public Piece(int state, int side) {
        this.state = state;
        this.side = side;
    }

    public String getName() {
        switch (state) {
            case 0:
                if (side == 1) return "兵";
                return "卒";
            case 1:
                return "炮";
            case 2:
                if (side == 1) return "车";
                return "車";
            case 3:
                if (side == 1) return "马";
                return "馬";
            case 4:
                if (side == 1) return "相";
                return "象";
            case 5:
                if (side == 1) return "仕";
                return "士";
            case 6:
                if (side == 1) return "帅";
                return "将";
        }
        return "无法识别的棋子 ";
    }


}