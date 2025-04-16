package org.example.domain;

import java.io.File;
import java.util.List;

public class FileRenameServiceImpl implements FileRenameService {

    @Override
    public void renameFiles(List<File> files, String newPattern, int startNumber, RenamingCallback callback) {
        int count = 0;
        int nextNumber = startNumber;
        for (File oldFile : files) {
            String newFileName = newPattern.replaceFirst("\\[0-9]", Integer.toString(nextNumber));
            File newFile = new File(oldFile.getParent(), newFileName);

            if (newFile.exists()) {
                callback.onExists(newFile);
                continue;
            }

            if (oldFile.renameTo(newFile)) {
                // 파일이 정상적으로 변경된 경우: onFileRenamed만 호출합니다.
                count++;
                nextNumber++;
                callback.onRenamed(oldFile, newFile);
            } else {
                callback.onError(oldFile, new Exception("에러 발생: " + oldFile.getAbsolutePath() + " -> " + newFile.getAbsolutePath()));
            }
        }

        callback.onComplete(count);
    }
}
