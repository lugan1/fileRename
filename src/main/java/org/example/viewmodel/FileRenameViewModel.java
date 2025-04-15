package org.example.viewmodel;

import org.example.domain.FileRenameService;
import org.example.intent.*;
import org.example.state.FileRenameState;

import java.io.File;
import java.util.List;

public class FileRenameViewModel {
    private final FileRenameState state = new FileRenameState();
    private final FileRenameService renameService;
    private StateListener listener;

    public FileRenameViewModel(FileRenameService renameService) {
        this.renameService = renameService;
    }

    public void setListener(StateListener listener) {
        this.listener = listener;
    }

    public void processIntent(FileRenameIntent intent) {
        switch (intent.getIntentType()) {
            case ADD: {
                AddFilesIntent addIntent = (AddFilesIntent) intent;
                for (File file : addIntent.files()) {
                    state.addFile(file);
                    state.addLog("파일 추가됨: " + file.getAbsolutePath());
                }
                notifyStateChanged();
                break;
            }
            case RENAME: {
                RenameFilesIntent renameIntent = (RenameFilesIntent) intent;
                List<File> files = state.getFileList();
                renameService.renameFiles(files, renameIntent.newPattern(), renameIntent.startNumber(), new FileRenameService.RenamingCallback() {
                    @Override
                    public void onFileRenamed(File oldFile, File newFile) {
                        int index = files.indexOf(oldFile);
                        if (index != -1) {
                            state.setFileAt(index, newFile);
                        }
                        state.addLog("변경됨: " + oldFile.getAbsolutePath() + " -> " + newFile.getAbsolutePath());
                        notifyStateChanged();
                    }
                    @Override
                    public void onError(File file, Exception e) {
                        state.addLog("에러 발생: " + file.getAbsolutePath() + " - " + e.getMessage());
                        notifyStateChanged();
                    }
                    @Override
                    public void onLog(String message) {
                        state.addLog(message);
                        notifyStateChanged();
                    }
                });
                break;
            }
            case PATTERN_CHANGED: {
                PatternChangedIntent patternIntent = (PatternChangedIntent) intent;
                String newPattern = patternIntent.newPattern();
                if (!state.getCurrentPattern().equals(newPattern)) {
                    state.setCurrentPattern(newPattern);
                    //자동으로 그려지기 때문에 notifyStateChanged() 할필요 없음
                }
                break;
            }
            case START_NUMBER_CHANGED: {
                StartNumberChangedIntent startNumberIntent = (StartNumberChangedIntent) intent;
                int newNumber = startNumberIntent.newStartNumber();
                if (state.getCurrentStartNumber() != newNumber) {
                    state.setCurrentStartNumber(newNumber);
                }
                break;
            }
            default:
                break;
        }
    }

    private void notifyStateChanged() {
        if (listener != null) {
            listener.onStateChanged(state);
        }
    }

    public interface StateListener {
        void onStateChanged(FileRenameState state);
    }
}
