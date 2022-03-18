package com.example.mazegame.ui.screens;

import android.app.AlertDialog;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.mazegame.R;
import com.example.mazegame.databinding.GameFragmentBinding;
import com.example.mazegame.helpers.ViewHelper;
import com.example.mazegame.interfaces.IDialogListener;
import com.example.mazegame.interfaces.IInteractorListener;
import com.example.mazegame.logic.GameInteractor;
import java.util.List;

public class GameFragment extends Fragment {

    private static final int COLS_COUNT = 4;
    private static final int ROWS_COUNT = 8;

    private GameFragmentBinding binding;
    private GameInteractor interactor;

    //region ******************** OVERRIDE *********************************************************

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = GameFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    //endregion OVERRIDE

    //region ********************** INIT ***********************************************************

    private void init() {
        initInteractor();
        initViews();
    }

    private void initInteractor() {
        interactor = new GameInteractor();
        interactor.setListener(new IInteractorListener() {
            @Override
            public void onSizesReady(int w, int h, int marginStart, int marginTop) {
                ViewHelper.setLpMargin(binding.image, w, h, ViewHelper.Offsets.left(marginStart).setTop(marginTop));
                createMaze();
            }
            @Override
            public void onMazeReady(int cols, int rows, int cellSize, List<RectF> walls) {
                binding.maze.start(cols, rows, cellSize, walls);
            }
            @Override
            public void onWallTouch() {
                binding.maze.stop();
                interactor.finish();
                showDialog(() -> {
                    binding.maze.restart();
                    interactor.restart();
                });
            }
            @Override
            public void onFinish() {
                binding.maze.stop();
                showDialog(() -> {
                    interactor.restart();
                    createMaze();
                });
            }
        });
    }

    private void initViews() {
        binding.maze
            .enableTrail()
            .setDrawables(R.drawable.maze_player, R.drawable.maze_finish)
            .setMoveListener((playerRect, finishRect) -> interactor.onMove(playerRect, finishRect));

        View root = binding.getRoot();
        root.post(() ->
            interactor.prepareScale(root.getHeight(), root.getWidth(), binding.image.getHeight(), binding.image.getWidth()));
    }

    //endregion INIT

    //region ********************** HELPERS ********************************************************

    private void createMaze() {
        binding.maze.post(() -> interactor.createMaze(COLS_COUNT, ROWS_COUNT,
            binding.maze.getWidth(), binding.maze.getHeight(),
            getResources().getDimensionPixelSize(R.dimen.maze_wall_thickness)));
    }

    private void showDialog(IDialogListener listener) {
        new AlertDialog.Builder(getContext())
            .setTitle(getString(R.string.dialog_title))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.dialog_button), (dialog, which) -> {
                if(listener != null) listener.onAccept();
            })
            .show();
    }

    //endregion HELPERS
}
