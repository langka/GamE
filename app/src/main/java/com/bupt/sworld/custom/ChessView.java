package com.bupt.sworld.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xusong on 2018/1/14.
 * About:象棋棋盘view
 */

public class ChessView extends View {
    //实现移动动画
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what <= 9) {
                setEnabled(false);
                Message m = Message.obtain();
                holder.progress = msg.what;
                m.what = msg.what + 1;
                sendMessageDelayed(m, 20);
                invalidate();
            } else {
                board[holder.tx][holder.ty] = holder.p;
                holder = null;
                selectedLocationInboard = null;
                setEnabled(true);
                invalidate();
            }
        }
    }

    private Handler handler = new MyHandler();

    //作为在move过程中记录的那个piece
    private volatile DataHolder holder = null;

    private static class DataHolder {
        Piece p;
        int fx;
        int fy;
        int tx;
        int ty;
        int progress;

        public DataHolder(Piece p, int fx, int fy, int tx, int ty) {
            this.p = p;
            this.fx = fx;
            this.fy = fy;
            this.tx = tx;
            this.ty = ty;
        }
    }


    private volatile boolean isOk = true;

    public void setOk(boolean ok) {
        this.isOk = ok;
    }

    public interface OnStepMoveListener {
        void onMove(int fx, int fy, int tx, int ty);
    }

    public void setStepMoveListener(OnStepMoveListener stepMoveListener) {
        this.stepMoveListener = stepMoveListener;
    }

    private OnStepMoveListener stepMoveListener;

    private static class Location {
        int x;
        int y;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            if (x != location.x) return false;
            return y == location.y;

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static List<Location> crosses = new ArrayList<>();

    static {
        crosses.add(new Location(1, 2));
        crosses.add(new Location(1, 7));
        crosses.add(new Location(7, 2));
        crosses.add(new Location(7, 7));
        int[] line = new int[]{0, 2, 4, 6, 8};
        int[] row = new int[]{3, 6};
        for (int i : line)
            for (int j : row)
                crosses.add(new Location(i, j));
    }

    //棋盘上的布局
    Piece[][] board = new Piece[9][10];
    //吃了的东西
    private List<Piece> redDead = new ArrayList<>(16);
    private List<Piece> blackDead = new ArrayList<>(16);

    public List<Piece> getRedDead() {
        return redDead;
    }

    public List<Piece> getBlackDead() {
        return blackDead;
    }

    private static final int[] soldiers = new int[]{0, 2, 4, 6, 8};
    private static final int[] cannon = new int[]{1, 7};
    private static final int[] vehicles = new int[]{0, 8};
    private static final int[] horses = new int[]{1, 7};
    private static final int[] elephants = new int[]{2, 6};
    private static final int[] guards = new int[]{3, 5};
    private static final int[] general = new int[]{4};

    private boolean isRed = true;
    private boolean redTurn = true;

    Paint framePaint = new Paint();
    Paint textPaint = new Paint();
    Paint tipPaint = new Paint();


    private float gap = -1;
    private float startX = -1;
    private float startY = -1;


    private Location selectedLocationInboard;

    //返回board数组中被选中的棋子，而非view中
    private Location computeLocation(MotionEvent event) {
        float x = event.getX() - startX;
        float y = event.getY() - startY;
        int px = (int) (x / gap);
        if (x - (px * gap) >= (gap / 2))
            px += 1;
        int py = (int) (y / gap);
        if (y - (py * gap) >= (gap / 2))
            py += 1;
        if (px >= 8)
            px = 8;
        if (py >= 9)
            py = 9;
        return getBySide(px, py);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isOk)
            return true;
        if (holder == null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (selectedLocationInboard == null) {//第一次点击
                        selectedLocationInboard = computeLocation(event);
                        Piece p = board[selectedLocationInboard.x][selectedLocationInboard.y];
                        if (p == null) {//点击的位置没有东西
                            selectedLocationInboard = null;
                        } else {
                            if (p.side == 1 && !isRed)
                                selectedLocationInboard = null;
                            else if (p.side == 2 && isRed)
                                selectedLocationInboard = null;
                            else {//点击了自己的棋子
                                Log.d("chesspiec ", "piece :" + p.getName() + " selected");
                                postInvalidate();
                            }
                        }
                    } else {//意味着已经有了一个待选的棋子，现在要么走要么重选
                        Location loc = computeLocation(event);
                        Piece pie = board[loc.x][loc.y];
                        if (pie != null && pie.side == board[selectedLocationInboard.x][selectedLocationInboard.y].side) {
                            selectedLocationInboard = loc;//选了个自己的新棋子
                        } else {
                            if (redTurn == isRed) {//我的回合
                                if (computeReachableLocations(selectedLocationInboard).contains(loc)) {
                                    stepMoveListener.onMove(selectedLocationInboard.x, selectedLocationInboard.y, loc.x, loc.y);
                                    //confirmNextStep(selectedLocationInboard.x, selectedLocationInboard.y, loc.x, loc.y);
                                } else {
                                    Toast.makeText(getContext(), "非法移动", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "请等待", Toast.LENGTH_SHORT).show();

                            }
                        }
                        postInvalidate();
                    }
                    break;

            }
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                Toast.makeText(getContext(), "不是你的回合", Toast.LENGTH_SHORT).show();

        }

        return true;
    }


    public ChessView(Context context) {
        super(context);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textPaint.setStyle(Paint.Style.STROKE);
        tipPaint.setColor(Color.BLUE);
        tipPaint.setAlpha(128);
    }


    private Location getBySide(int x, int y) {
        if (isRed)
            return new Location(x, y);
        else return new Location(8 - x, 9 - y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpec = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpec = MeasureSpec.getMode(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpec == MeasureSpec.AT_MOST || heightSpec == MeasureSpec.AT_MOST) {
            int z = Math.min(w, h);
            float zh = (float) (z * 1.2);
            setMeasuredDimension(z, (int) zh);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        // TODO: 2018/1/14 中间一通计算 ,获得起点坐标，小方块边长
        if (startX == -1) {
            startX = width / 10;
            startY = startX / 2;
            gap = startX;
            textPaint.setTextSize(gap / 2);
        }
        float endY = startY + gap * 9;
        float endX = startX + gap * 8;
        drawBoard(canvas, startX, startY, endX, endY, gap);
        drawPieces(canvas, startX, startY, endX, endY, gap);
        drawSelectedAndTips(canvas, startX, startY, endX, endY, gap);
        drawMovingPiece(canvas, startX, startY, gap);

    }

    private void drawMovingPiece(Canvas canvas, float startX, float startY, float gap) {
        DataHolder h = holder;
        if (h != null) {//说明有处于moving状态的piece
            float p = h.progress / 10f;
            if (p >= 1)
                p = 1;
            float mx = ((h.tx - h.fx) * p + h.fx);
            float my = ((h.ty - h.fy) * p + h.fy);
            if (!isRed) {
                mx = 8 - mx;
                my = 9 - my;
            }
            drawPieceAt(holder.p, canvas, startX + mx * gap, startY + my * gap);
        }
    }

    private void drawSelectedAndTips(Canvas canvas, float startX, float startY, float endX, float endY, float gap) {
        if (selectedLocationInboard != null) {//选中
            Location viewLocation = getBySide(selectedLocationInboard.x, selectedLocationInboard.y);
            float x = startX + viewLocation.x * gap;
            float y = startY + viewLocation.y * gap;
            float distance = gap / 2;
            float length = gap / 3;
            canvas.drawLine(x - distance, y - distance, x - distance + length, y - distance, tipPaint);
            canvas.drawLine(x - distance, y - distance, x - distance, y - distance + length, tipPaint);
            canvas.drawLine(x - distance, y + distance, x - distance + length, y + distance, tipPaint);
            canvas.drawLine(x - distance, y + distance, x - distance, y + distance - length, tipPaint);
            canvas.drawLine(x + distance, y - distance, x + distance - length, y - distance, tipPaint);
            canvas.drawLine(x + distance, y - distance, x + distance, y - distance + length, tipPaint);
            canvas.drawLine(x + distance, y + distance, x + distance - length, y + distance, tipPaint);
            canvas.drawLine(x + distance, y + distance, x + distance, y + distance - length, tipPaint);
            List<Location> locations = computeReachableLocations(selectedLocationInboard);
            if (locations.size() != 0) {
                for (Location a : locations) {
                    Location actual = getBySide(a.x, a.y);
                    float centerX = startX + actual.x * gap;
                    float centerY = startY + actual.y * gap;
                    float radius = gap / 4;
                    canvas.drawCircle(centerX, centerY, radius, tipPaint);
                }
            }
        }
    }

    private void drawPieces(Canvas canvas, float startX, float startY, float endX, float endY, float gap) {
        for (int i = 0; i <= 8; i++)
            for (int j = 0; j <= 9; j++) {
                Location l = getBySide(i, j);
                Piece pie = board[i][j];
                if (pie != null) {
                    float x = startX + l.x * gap;
                    float y = startY + l.y * gap;
                    drawPieceAt(pie, canvas, x, y);
                }
            }
    }


    private List<Location> computeReachableLocations(Location l) {
        Piece p = board[l.x][l.y];
        if (p == null)
            return new ArrayList<>();
        switch (p.state) {
            case 0:
                List<Location> locations = new ArrayList<>(4);
                if (p.side == 1) {//红色
                    int leftX = l.x - 1;
                    int rightX = l.x + 1;
                    int forward = l.y - 1;
                    if (l.y <= 4) {
                        Piece left = null;
                        if (leftX >= 0)
                            left = board[leftX][l.y];
                        if (leftX >= 0 && (left == null || left.side != p.side))
                            locations.add(new Location(leftX, l.y));
                        Piece right = null;
                        if (rightX <= 8)
                            right = board[rightX][l.y];
                        if (rightX <= 8 && (right == null || right.side != p.side))
                            locations.add(new Location(rightX, l.y));
                    }
                    Piece fward = null;
                    if (forward >= 0)
                        fward = board[l.x][forward];
                    if (forward >= 0 && (fward == null || fward.side != p.side))
                        locations.add(new Location(l.x, forward));
                } else {
                    int leftX = l.x - 1;
                    int rightX = l.x + 1;
                    int forward = l.y + 1;
                    if (l.y >= 5) {
                        Piece left = null;
                        if (leftX >= 0)
                            left = board[leftX][l.y];
                        if (leftX >= 0 && (left == null || left.side != p.side))
                            locations.add(new Location(leftX, l.y));
                        Piece right = null;
                        if (rightX <= 8)
                            right = board[rightX][l.y];
                        if (rightX <= 8 && (right == null || right.side != p.side))
                            locations.add(new Location(rightX, l.y));
                    }
                    Piece fward = null;
                    if (forward >= 0)
                        fward = board[l.x][forward];
                    if (forward >= 0 && (fward == null || fward.side != p.side))
                        locations.add(new Location(l.x, forward));
                }
                return locations;
            case 1:
                return getCannonTips(l);
            case 2:
                return getVehiclesTips(l);
            case 3:
                return getHorseTips(l);
            case 4:
                return getElephantTips(l);
            case 5:
                return getGuardTips(l);
            case 6:
                return getGeneralTips(l);
        }
        return new ArrayList<>();
    }

    private List<Location> getCannonTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>(20);
        for (int j = l.x - 1; j >= 0; j--) {
            Piece piece = board[j][l.y];
            if (piece == null)
                locations.add(new Location(j, l.y));
            else {
                for (int i = j - 1; i >= 0; i--) {
                    Piece p = board[i][l.y];
                    if (p != null) {
                        if (p.side != current.side)
                            locations.add(new Location(i, l.y));
                        break;
                    }
                }
                break;
            }
        }
        for (int j = l.x + 1; j <= 8; j++) {
            Piece piece = board[j][l.y];
            if (piece == null)
                locations.add(new Location(j, l.y));
            else {
                for (int i = j + 1; i <= 8; i++) {
                    Piece p = board[i][l.y];
                    if (p != null) {
                        if (p.side != current.side)
                            locations.add(new Location(i, l.y));
                        break;
                    }
                }
                break;
            }
        }
        for (int j = l.y + 1; j <= 9; j++) {
            Piece piece = board[l.x][j];
            if (piece == null)
                locations.add(new Location(l.x, j));
            else {
                for (int i = j + 1; i <= 9; i++) {
                    Piece p = board[l.x][i];
                    if (p != null) {
                        if (p.side != current.side)
                            locations.add(new Location(l.x, i));
                        break;
                    }
                }
                break;
            }
        }
        for (int j = l.y - 1; j >= 0; j--) {
            Piece piece = board[l.x][j];
            if (piece == null)
                locations.add(new Location(l.x, j));
            else {
                for (int i = j - 1; i >= 0; i--) {
                    Piece p = board[l.x][i];
                    if (p != null) {
                        if (p.side != current.side)
                            locations.add(new Location(l.x, i));
                        break;
                    }
                }
                break;
            }
        }

        return locations;
    }

    private List<Location> getGeneralTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>();
        int top = l.y - 1;
        int bottom = l.y + 1;
        int left = l.x - 1;
        int right = l.x + 1;
        if (left >= 3) {
            if (board[left][l.y] == null || board[left][l.y].side != current.side)
                locations.add(new Location(left, l.y));
        }
        if (right <= 5) {
            if (board[right][l.y] == null || board[right][l.y].side != current.side)
                locations.add(new Location(right, l.y));
        }
        if (current.side == 1) {
            if (top >= 7) {
                if (board[l.x][top] == null || board[l.x][top].side != current.side)
                    locations.add(new Location(l.x, top));
            }
            if (bottom <= 9) {
                if (board[l.x][bottom] == null || board[l.x][bottom].side != current.side)
                    locations.add(new Location(l.x, bottom));
            }
        } else {
            if (top >= 0) {
                if (board[l.x][top] == null || board[l.x][top].side != current.side)
                    locations.add(new Location(l.x, top));
            }
            if (bottom <= 2) {
                if (board[l.x][bottom] == null || board[l.x][bottom].side != current.side)
                    locations.add(new Location(l.x, bottom));
            }
        }
        return locations;
    }

    private List<Location> getGuardTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>();
        int[] px = new int[]{l.x - 1, l.x + 1};
        int[] py = new int[]{l.y - 1, l.y + 1};
        for (int x : px)
            for (int y : py) {
                if (x >= 3 && x <= 5) {
                    if (current.side == 1) {//红色
                        if (y >= 7 && y <= 9) {
                            if (board[x][y] == null || board[x][y].side != current.side) {
                                locations.add(new Location(x, y));
                            }
                        }
                    } else {
                        if (y >= 0 && y <= 2) {
                            if (board[x][y] == null || board[x][y].side != current.side) {
                                locations.add(new Location(x, y));
                            }
                        }
                    }
                }
            }

        return locations;

    }

    private List<Location> getElephantTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>(4);
        int[] px = new int[]{l.x - 2, l.x + 2};
        int[] py = new int[]{l.y - 2, l.y + 2};
        for (int x : px)
            for (int y : py) {
                if (x >= 0 && x <= 8) {//左右未越界
                    if (current.side == 1) {//红色
                        if (y <= 9 && y >= 5) {//上下未越界
                            if (board[x][y] == null || board[x][y].side != current.side) {
                                int mx = (x + l.x) / 2;
                                int my = (y + l.y) / 2;
                                if (board[mx][my] == null)
                                    locations.add(new Location(x, y));
                            }
                        }
                    } else {
                        if (y <= 4 && y >= 0) {
                            if (board[x][y] == null || board[x][y].side != current.side) {
                                int mx = (x + l.x) / 2;
                                int my = (y + l.y) / 2;
                                if (board[mx][my] == null)
                                    locations.add(new Location(x, y));
                            }
                        }
                    }
                }
            }
        return locations;
    }

    private List<Location> getHorseTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>(8);
        int movex = l.x - 2;
        if (movex >= 0 && board[l.x - 1][l.y] == null) {//向左走,无遮挡
            if (l.y + 1 <= 9)
                if (board[movex][l.y + 1] == null || board[movex][l.y + 1].side != current.side)
                    locations.add(new Location(movex, l.y + 1));
            if (l.y - 1 >= 0)
                if (board[movex][l.y - 1] == null || board[movex][l.y - 1].side != current.side)
                    locations.add(new Location(movex, l.y - 1));
        }
        movex = l.x + 2;
        if (movex <= 8 && board[l.x + 1][l.y] == null) {
            if (l.y + 1 <= 9)
                if (board[movex][l.y + 1] == null || board[movex][l.y + 1].side != current.side)
                    locations.add(new Location(movex, l.y + 1));
            if (l.y - 1 >= 0)
                if (board[movex][l.y - 1] == null || board[movex][l.y - 1].side != current.side)
                    locations.add(new Location(movex, l.y - 1));
        }
        int moveY = l.y - 2;//向上
        if (moveY >= 0 && board[l.x][l.y - 1] == null) {
            if (l.x - 1 >= 0)
                if (board[l.x - 1][moveY] == null || board[l.x - 1][moveY].side != current.side)
                    locations.add(new Location(l.x - 1, moveY));
            if (l.x + 1 <= 8) {
                if (board[l.x + 1][moveY] == null || board[l.x + 1][moveY].side != current.side)
                    locations.add(new Location(l.x + 1, moveY));
            }
        }
        moveY = l.y + 2;
        if (moveY <= 9 && board[l.x][l.y + 1] == null) {
            if (l.x - 1 >= 0)
                if (board[l.x - 1][moveY] == null || board[l.x - 1][moveY].side != current.side)
                    locations.add(new Location(l.x - 1, moveY));
            if (l.x + 1 <= 8) {
                if (board[l.x + 1][moveY] == null || board[l.x + 1][moveY].side != current.side)
                    locations.add(new Location(l.x + 1, moveY));
            }
        }
        return locations;
    }


    private List<Location> getVehiclesTips(Location l) {
        Piece current = board[l.x][l.y];
        List<Location> locations = new ArrayList<>();
        for (int j = l.x - 1; j >= 0; j--) {
            Piece piece = board[j][l.y];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(j, l.y));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }
        for (int j = l.x + 1; j <= 8; j++) {
            Piece piece = board[j][l.y];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(j, l.y));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }
        for (int j = l.y + 1; j <= 9; j++) {
            Piece piece = board[l.x][j];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(l.x, j));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }
        for (int j = l.y - 1; j >= 0; j--) {
            Piece piece = board[l.x][j];
            if (piece == null || piece.side != current.side)
                locations.add(new Location(l.x, j));
            if (piece != null)//只要有单位，車一定不能穿越
                break;
        }

        return locations;
    }

    /**
     * @param p
     * @param canvas
     * @param x      具体的坐标了, 中心
     * @param y
     */
    private void drawPieceAt(Piece p, Canvas canvas, float x, float y) {
        if (p.side == 1)
            textPaint.setColor(Color.RED);
        else textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float top = textPaint.getFontMetrics().top;
        float bot = textPaint.getFontMetrics().bottom;
        float base = y - top / 2 - bot / 2;
        canvas.drawCircle(x, y, gap / 3, textPaint);
        canvas.drawText(p.getName(), x, base, textPaint);
    }


    private void drawBoard(Canvas canvas, float startX, float startY, float endX, float endY, float gap) {
        for (int i = 0; i <= 9; i++) {
            canvas.drawLine(startX, startY + i * gap, endX, startY + i * gap, framePaint);
        }
        canvas.drawLine(startX, startY, startX, endY, framePaint);
        canvas.drawLine(endX, startY, endX, endY, framePaint);
        for (int i = 0; i <= 8; i++) {
            canvas.drawLine(startX + i * gap, startY, startX + i * gap, startY + 4 * gap, framePaint);
            canvas.drawLine(startX + i * gap, startY + 5 * gap, startX + i * gap, endY, framePaint);
        }
        canvas.drawLine(startX + 3 * gap, startY, startX + 5 * gap, startY + 2 * gap, framePaint);
        canvas.drawLine(startX + 5 * gap, startY, startX + 3 * gap, startY + 2 * gap, framePaint);
        canvas.drawLine(startX + 3 * gap, startY + 7 * gap, startX + 5 * gap, startY + 9 * gap, framePaint);
        canvas.drawLine(startX + 5 * gap, startY + 7 * gap, startX + 3 * gap, startY + 9 * gap, framePaint);
        float crosslength = gap / 5;
        float distance = gap / 10;
        for (Location e : crosses) {
            float posX = startX + (e.x) * gap;
            float posY = startY + (e.y) * gap;
            if (e.x != 0) {//不是最左边
                canvas.drawLine(posX - distance, posY + distance, posX - distance - crosslength, posY + distance, framePaint);
                canvas.drawLine(posX - distance, posY + distance, posX - distance, posY + distance + crosslength, framePaint);
                canvas.drawLine(posX - distance, posY - distance, posX - distance - crosslength, posY - distance, framePaint);
                canvas.drawLine(posX - distance, posY - distance, posX - distance, posY - distance - crosslength, framePaint);
            }
            if (e.x != 8) {
                canvas.drawLine(posX + distance, posY - distance, posX + distance + crosslength, posY - distance, framePaint);
                canvas.drawLine(posX + distance, posY - distance, posX + distance, posY - distance - crosslength, framePaint);
                canvas.drawLine(posX + distance, posY + distance, posX + distance, posY + distance + crosslength, framePaint);
                canvas.drawLine(posX + distance, posY + distance, posX + distance + crosslength, posY + distance, framePaint);

            }
        }


    }


    public void init(boolean red) {
        isRed = red;
        resetPieces();
        postInvalidate();
    }

    private void resetPieces() {
        for (int i = 0; i <= 8; i++)
            for (int j = 0; j <= 9; j++)
                board[i][j] = null;
        reset(0, soldiers, 3, 6);
        reset(1, cannon, 2, 7);
        reset(2, vehicles, 0, 9);
        reset(3, horses, 0, 9);
        reset(4, elephants, 0, 9);
        reset(5, guards, 0, 9);
        reset(6, general, 0, 9);

    }

    private void reset(int state, int[] vertical, int black, int red) {
        for (int i : vertical) {
            Piece rp = new Piece(state, 1);
            Piece bp = new Piece(state, 2);
            board[i][red] = rp;
            board[i][black] = bp;
        }
    }


    //走一步引起界面刷新
    public void confirmNextStep(int fx, int fy, int tx, int ty) {
        holder = new DataHolder(board[fx][fy], fx, fy, tx, ty);
        board[fx][fy] = null;
        Message message = Message.obtain();
        message.what = 0;
        handler.sendMessage(message);
        redTurn = !redTurn;
    }

}

