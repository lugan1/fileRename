package org.example.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileRenameStateTest {

    private FileRenameState state;

    @BeforeEach
    void setUp() {
        state = new FileRenameState();
    }

    @Test
    void initialState_shouldBeEmptyAndDefaults() {
        assertTrue(state.getFileList().isEmpty(), "초기 파일리스트는 비어야 합니다");
        assertTrue(state.getLogMessages().isEmpty(), "초기 로그는 비어야 합니다");
        assertEquals("", state.getCurrentPattern(), "기본 패턴");
        assertEquals(1, state.getCurrentStartNumber(), "기본 시작 번호");
    }

    @Test
    void setCurrentPattern_andGetCurrentPattern() {
        state.setCurrentPattern("myPattern");
        assertEquals("myPattern", state.getCurrentPattern());
    }

    @Test
    void setCurrentPattern_null_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> state.setCurrentPattern(null));
    }

    @Test
    void setCurrentStartNumber_andGet() {
        state.setCurrentStartNumber(42);
        assertEquals(42, state.getCurrentStartNumber());
    }

    @Test
    void addFile_andGetFileList_returnsUnmodifiableCopy() {
        File f = new File("test.txt");
        state.addFile(f);
        List<File> list = state.getFileList();
        assertEquals(1, list.size());
        assertEquals(f, list.get(0));
        // 불변 복사본 확인
        assertThrows(UnsupportedOperationException.class, () -> list.add(new File("x")));
    }

    @Test
    void addLog_andGetLogMessages_returnsUnmodifiableCopy() {
        state.addLog("hello");
        List<String> logs = state.getLogMessages();
        assertEquals(1, logs.size());
        assertEquals("hello", logs.get(0));
        // 불변 복사본 확인
        assertThrows(UnsupportedOperationException.class, () -> logs.add("x"));
    }
}
