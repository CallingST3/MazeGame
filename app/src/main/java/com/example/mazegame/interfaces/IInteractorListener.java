package com.example.mazegame.interfaces;

import android.graphics.RectF;
import java.util.List;

public interface IInteractorListener {
    void onSizesReady(int w, int h, int marginStart, int marginTop);
    void onMazeReady(int cols, int rows, int cellSize, List<RectF> walls);
    void onWallTouch();
    void onFinish();
}
