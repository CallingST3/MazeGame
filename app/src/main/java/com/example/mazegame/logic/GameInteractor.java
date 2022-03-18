package com.example.mazegame.logic;

import android.graphics.RectF;
import com.example.mazegame.interfaces.IInteractorListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GameInteractor {

    private IInteractorListener listener;

    private final Random random = new Random();
    private boolean gameFinished;
    private List<RectF> walls;


    //region ******************* OPTIONS ***********************************************************

    public void setListener(IInteractorListener listener) {
        this.listener = listener;
    }

    //endregion OPTIONS

    //region ******************* HANDLERS **********************************************************

    public void prepareScale(float hScr, float wScr, float hPic, float wPic) {
        float screenRatio = hScr / wScr;
        float picRatio = hPic / wPic;
        int wResult = (int) wScr;
        int hResult = (int) hScr;
        int marginStart, marginTop;

        if(screenRatio > picRatio) wResult = (int) (hScr / picRatio);
        else hResult = (int) (wScr * picRatio);
        marginStart = -Math.abs((int) (wResult - wScr)/2);
        marginTop = -Math.abs((int) (hResult - hScr)/2);

        listener.onSizesReady(wResult, hResult, marginStart, marginTop);
    }

    public void createMaze(int cols, int rows, int playgroundWidth, int playgroundHeight, int wallThickness) {
        int cellWidth = playgroundWidth / cols;
        int cellHeight = playgroundHeight / rows;
        int cellSize = Math.min(cellWidth, cellHeight);

        walls = generateMaze(cols, rows, wallThickness, cellSize);
        listener.onMazeReady(cols, rows, cellSize, walls);
    }

    public void onMove(RectF playerRect, RectF finishRect) {
        if(gameFinished) return;

        boolean touchedWall = isWallTouched(playerRect);
        boolean reachedFinish = !touchedWall && isFinishReached(playerRect, finishRect);

        if(touchedWall || reachedFinish) {
            gameFinished = true;
            if(reachedFinish) listener.onFinish();
            else listener.onWallTouch();
        }
    }

    public void restart() {
        gameFinished = false;
    }

    public void finish() {
        gameFinished = true;
    }

    //endregion HANDLERS

    //region ******************* HELPERS ***********************************************************

    private LinkedList<RectF> generateMaze(int cols, int rows, int wallThickness, int cellSize) {
        // создаем матрицу ячеек
        Cell[][] cells = new Cell[cols][rows];
        for(int i = 0; i < cols; i++)
            for(int j = 0; j < rows; j++)
                cells[i][j] = new Cell(i, j);

        Stack<Cell> stack = new Stack<>();
        Cell currentCell = cells[0][0];
        // проходим по всем ячейкам и генерируем лабиринт. Для каждой ячейки берем случайного
        // "соседа" и убираем стену между этими ячейками. Каждая ячейка может быть "соседом" только
        // один раз, чтобы не было лишних ветвлений. Если зашли в тупик (у ячейки нет необработанных
        // соседей), возвращаемся обратно по стеку. Как только найдем ячейку, у которой есть
        // необработанный сосед, продолжим обработку ячеек по тому же принципу и таким образом
        // начнем создавать очередную ветку лабиринта. Эта логика будет повторяться, пока не
        // останется необработанных ячеек (из очереднего тупика вернемся к первой ячейке,
        // и стек полностью очистится).
        do {
            currentCell.visited = true;
            // получаем соседнюю ячейку, которую еще не успели обработать
            Cell nextCell = getPendingNeighbour(cells, currentCell, cols, rows);
            if(nextCell != null) {
                // удаляем стены между соседними ячейками
                removeWall(currentCell, nextCell);
                stack.push(currentCell);
                currentCell = nextCell;
            }
            else currentCell = stack.pop();
        } while(!stack.empty());

        // превращаем карту стен в rect'ы
        return createWalls(cells, cols, rows, wallThickness, cellSize);
    }

    private Cell getPendingNeighbour(Cell[][] cells, Cell cell, int cols, int rows) {
        Cell leftCell = cell.col > 0 ? cells[cell.col - 1][cell.row] : null;
        Cell topCell = cell.row > 0 ? cells[cell.col][cell.row - 1] : null;
        Cell rightCell = cell.col < cols - 1 ? cells[cell.col + 1][cell.row] : null;
        Cell bottomCell = cell.row < rows - 1 ? cells[cell.col][cell.row + 1] : null;

        List<Cell> pendingNeighbours = new ArrayList<>();
        if(leftCell != null && !leftCell.visited) pendingNeighbours.add(leftCell);
        if(topCell != null && !topCell.visited) pendingNeighbours.add(topCell);
        if(rightCell != null && !rightCell.visited) pendingNeighbours.add(rightCell);
        if(bottomCell != null && !bottomCell.visited) pendingNeighbours.add(bottomCell);

        if(pendingNeighbours.size() > 0) {
            int index = random.nextInt(pendingNeighbours.size());
            return pendingNeighbours.get(index);
        }

        return null;
    }

    private void removeWall(Cell current, Cell next) {
        if(current.col == next.col) {
            if(current.row == next.row + 1) {
                // next сверху
                current.topWall = false;
                next.bottomWall = false;
            }
            else if(current.row == next.row - 1) {
                // next снизу
                current.bottomWall = false;
                next.topWall = false;
            }
        }
        else if(current.row == next.row) {
            if(current.col == next.col + 1) {
                // next слева
                current.leftWall = false;
                next.rightWall = false;
            }
            else if(current.col == next.col - 1) {
                // next справа
                current.rightWall = false;
                next.leftWall = false;
            }
        }
    }

    private LinkedList<RectF> createWalls(Cell[][] cells, int cols, int rows, int wallThickness, int cellSize) {
        LinkedList<RectF> walls = new LinkedList<>();
        for(int i = 0; i < cols; i++)
            for(int j = 0; j < rows; j++) {
                Cell cell = cells[i][j];
                float left = i * cellSize - wallThickness / 2f;
                float top = j * cellSize - wallThickness / 2f;
                float right = (i + 1) * cellSize + wallThickness / 2f;
                float bottom = (j + 1) * cellSize + wallThickness / 2f;
                if(cell.leftWall) walls.add(new RectF(left, top, left + wallThickness, bottom));
                if(cell.topWall) walls.add(new RectF(left, top, right, top + wallThickness));
                if(cell.rightWall) walls.add(new RectF(right - wallThickness, top, right, bottom));
                if(cell.bottomWall) walls.add(new RectF(left, bottom - wallThickness, right, bottom));
            }
        return walls;
    }

    private boolean isWallTouched(RectF rect) {
        for(RectF wall : walls)
            if(RectF.intersects(rect, wall)) return true;
        return false;
    }

    private boolean isFinishReached(RectF playerRect, RectF finishRect) {
        return finishRect.contains(playerRect.centerX(), playerRect.centerY());
    }

    //endregion HELPERS

    public static class Cell {
        private final int col, row;
        private boolean topWall = true;
        private boolean leftWall = true;
        private boolean bottomWall = true;
        private boolean rightWall = true;
        private boolean visited;

        private Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
