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

    private void notifyStateChanged(ResultType type) {
        if (listener != null) {
            listener.onStateChanged(type, state);
        }
    }

    public void processIntent(FileRenameIntent intent) {
        switch (intent.getIntentType()) {
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

            case ADD: {
                AddFilesIntent addIntent = (AddFilesIntent) intent;
                for (File file : addIntent.files()) {
                    state.addFile(file);
                    state.addLog("파일 추가됨: " + file.getAbsolutePath());
                }
                notifyStateChanged(ResultType.LIST_RELOAD);
                notifyStateChanged(ResultType.LOG_MESSAGE);
                break;
            }

            case RENAME: {
                RenameFilesIntent renameIntent = (RenameFilesIntent) intent;
                List<File> files = state.getFileList();
                renameService.renameFiles(files, renameIntent.newPattern(), renameIntent.startNumber(), new FileRenameService.RenamingCallback() {
                    @Override
                    public void onRenamed(File oldFile, File newFile) {
                        int index = files.indexOf(oldFile);
                        if (index != -1) {
                            state.setFileAt(index, newFile);
                        }
                        notifyStateChanged(ResultType.LIST_RELOAD);
                        state.addLog("변경됨: " + oldFile.getAbsolutePath() + " -> " + newFile.getAbsolutePath());
                    }

                    @Override
                    public void onExists(File newFile) {
                        state.addLog("오류: " + newFile.getAbsolutePath() + " 파일이 이미 존재합니다. 건너뜀.");
                        notifyStateChanged(ResultType.LOG_MESSAGE);
                    }

                    @Override
                    public void onComplete(int count) {
                        state.addLog("총 " + count + "개의 파일명이 변경되었습니다.");
                        notifyStateChanged(ResultType.LOG_MESSAGE);
                    }

                    @Override
                    public void onError(File file, Exception e) {
                        state.addLog(e.getMessage());
                        notifyStateChanged(ResultType.LOG_MESSAGE);
                    }
                });
                break;
            }
            default:
                break;
        }
    }

    public enum ResultType {
        LIST_RELOAD,
        ON_FILE_RENAMED,
        LOG_MESSAGE
    }

    public void setListener(StateListener listener) {
        this.listener = listener;
    }

    public interface StateListener {
        void onStateChanged(ResultType type, FileRenameState state);
    }
}
