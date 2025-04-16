package org.example.state;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileRenameState {
    // 파일 목록과 로그 메시지는 내부에서 변경 가능한 리스트로 관리하지만, 외부에는 불변 리스트로 노출
    // ViewModel 에서 FileList를 관리하기 위해 사용. ViewModel 은 UI 프레임워크를 몰라야하며 종속되면 안된다.
    private final List<File> fileList = new ArrayList<>();
    private final List<String> logMessages = new ArrayList<>();

    // 입력 관련 상태: 새 파일명 패턴과 시작 번호
    private String currentPattern = "";
    private int currentStartNumber = 1;


    // 입력 상태 관련 getter / setter
    public String getCurrentPattern() {
        return currentPattern;
    }

    public void setCurrentPattern(String currentPattern) {
        if (currentPattern == null) {
            throw new IllegalArgumentException("currentPattern cannot be null");
        }
        this.currentPattern = currentPattern;
    }

    public int getCurrentStartNumber() {
        return currentStartNumber;
    }

    public void setCurrentStartNumber(int currentStartNumber) {
        this.currentStartNumber = currentStartNumber;
    }

    // 파일 목록 관련 메서드
    public List<File> getFileList() {
        // 외부에서는 내부 리스트의 복사본을 불변 리스트로 반환하여 직접 수정할 수 없도록 함
        return List.copyOf(fileList);
    }

    public void addFile(File file) {
        if (file != null) {
            fileList.add(file);
        }
    }

    public void setFileAt(int index, File file) {
        if (index < 0 || index >= fileList.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        fileList.set(index, file);
    }

    // 로그 메시지 관련 메서드
    public List<String> getLogMessages() {
        // 로그 리스트도 복사본을 반환하여 불변으로 처리
        return List.copyOf(logMessages);
    }

    public void addLog(String message) {
        if (message != null) {
            logMessages.add(message);
        }
    }
}
