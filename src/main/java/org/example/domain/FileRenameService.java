package org.example.domain;

import java.io.File;
import java.util.List;

public interface FileRenameService {
    /**
     * 파일 목록에 대해 새 파일명 패턴을 적용하여 이름을 변경합니다.
     *
     * @param files       변경 대상 파일 목록
     * @param newPattern  새 파일명 패턴 (예: "A0[0-9].smi")
     * @param startNumber 번호 시작값
     * @param callback    작업 결과를 전달하는 콜백
     */
    void renameFiles(List<File> files, String newPattern, int startNumber, RenamingCallback callback);

    interface RenamingCallback {
        void onRenamed(File oldFile, File newFile);
        void onExists(File newFile);
        void onComplete(int count);
        void onError(File file, Exception e);
    }
}
