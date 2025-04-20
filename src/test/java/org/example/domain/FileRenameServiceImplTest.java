package org.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileRenameServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void renameFiles_shouldRenameAndCallbackOnRenamedAndComplete() throws Exception {
        // 준비: 임시 디렉터리에 두 개의 파일 생성
        File oldFile1 = tempDir.resolve("old1.txt").toFile();
        File oldFile2 = tempDir.resolve("old2.txt").toFile();
        assertTrue(oldFile1.createNewFile(), "old1.txt 파일 생성 실패");
        assertTrue(oldFile2.createNewFile(), "old2.txt 파일 생성 실패");
        List<File> files = List.of(oldFile1, oldFile2);

        FileRenameServiceImpl service = new FileRenameServiceImpl();

        // 콜백 결과 저장용
        var renamed = new java.util.ArrayList<File>();
        var completedCount = new int[1];

        service.renameFiles(files, "renamed[0-9].txt", 5, new FileRenameService.RenamingCallback() {
            @Override
            public void onRenamed(File oldFile, File newFile) {
                renamed.add(newFile);
            }

            @Override
            public void onExists(File newFile) {
                fail("onExists는 호출되지 않아야 합니다");
            }

            @Override
            public void onComplete(int count) {
                completedCount[0] = count;
            }

            @Override
            public void onError(File file, Exception e) {
                fail("onError는 호출되지 않아야 합니다: " + e.getMessage());
            }
        });

        // 검증: 콜백 호출 횟수 및 파일 시스템 상태
        assertEquals(2, renamed.size(), "onRenamed 호출 횟수");
        assertEquals("renamed5.txt", renamed.get(0).getName());
        assertEquals("renamed6.txt", renamed.get(1).getName());
        assertEquals(2, completedCount[0], "onComplete에 전달된 count");

        assertFalse(oldFile1.exists(), "oldFile1은 삭제되어야 합니다");
        assertFalse(oldFile2.exists(), "oldFile2는 삭제되어야 합니다");
        assertTrue(tempDir.resolve("renamed5.txt").toFile().exists(), "renamed5.txt 존재 여부");
        assertTrue(tempDir.resolve("renamed6.txt").toFile().exists(), "renamed6.txt 존재 여부");
    }
}
