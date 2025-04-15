package org.example;

import org.example.domain.FileRenameService;
import org.example.domain.FileRenameServiceImpl;
import org.example.presentation.FileRenameView;
import org.example.viewmodel.FileRenameViewModel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileRenameService renameService = new FileRenameServiceImpl();
            FileRenameViewModel viewModel = new FileRenameViewModel(renameService);
            FileRenameView view = new FileRenameView(viewModel);
            view.setVisible(true);
        });
    }
}
