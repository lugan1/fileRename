package org.example.viewmodel;

import org.example.domain.FileRenameService;
import org.example.intent.AddFilesIntent;
import org.example.intent.PatternChangedIntent;
import org.example.intent.RenameFilesIntent;
import org.example.intent.StartNumberChangedIntent;
import org.example.state.FileRenameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRenameViewModelTest {

    private FileRenameViewModel vm;
    private TestListener listener;

    @BeforeEach
    void setUp() {
        // Stub 서비스: 콜백을 즉시 호출
        FileRenameService stubService = (files, newPattern, startNumber, callback) -> {
            File oldFile = files.get(0);
            File newFile = new File(oldFile.getParent(), newPattern.replaceFirst("\\[0-9]", String.valueOf(startNumber)));
            callback.onRenamed(oldFile, newFile);
            callback.onComplete(1);
        };
        vm = new FileRenameViewModel(stubService);
        listener = new TestListener();
        vm.setListener(listener);
    }

    @Test
    void processPatternChangedIntent_shouldUpdatePatternFieldViaReflection() throws Exception {
        vm.processIntent(new PatternChangedIntent("XYZ"));
        // private state 필드에 리플렉션으로 접근
        Field f = FileRenameViewModel.class.getDeclaredField("state");
        f.setAccessible(true);
        FileRenameState state = (FileRenameState) f.get(vm);
        assertEquals("XYZ", state.getCurrentPattern());
    }

    @Test
    void processStartNumberChangedIntent_shouldUpdateStartNumberViaReflection() throws Exception {
        vm.processIntent(new StartNumberChangedIntent(99));
        Field f = FileRenameViewModel.class.getDeclaredField("state");
        f.setAccessible(true);
        FileRenameState state = (FileRenameState) f.get(vm);
        assertEquals(99, state.getCurrentStartNumber());
    }

    @Test
    void processAddFilesIntent_shouldNotifyListReloadAndLogMessage() {
        File f = new File("a.txt");
        vm.processIntent(new AddFilesIntent(List.of(f)));

        // ADD 처리 후, 첫 번째는 LIST_RELOAD, 두 번째는 LOG_MESSAGE
        assertEquals(2, listener.count.get(), "콜백 횟수");
        assertEquals(FileRenameViewModel.ResultType.LIST_RELOAD, listener.types.get(0));
        assertEquals(FileRenameViewModel.ResultType.LOG_MESSAGE, listener.types.get(1));
    }

    @Test
    void processRenameFilesIntent_shouldUpdateStateAndNotify() throws NoSuchFieldException, IllegalAccessException {
        // 먼저 파일 추가
        File f = new File("d", "x.txt");
        vm.processIntent(new AddFilesIntent(List.of(f)));
        listener.reset();

        vm.processIntent(new RenameFilesIntent("N[0-9].ext", 7));

        // onRenamed → LIST_RELOAD, onComplete → LOG_MESSAGE
        assertEquals(2, listener.count.get());
        assertEquals(FileRenameViewModel.ResultType.LIST_RELOAD, listener.types.get(0));
        assertEquals(FileRenameViewModel.ResultType.LOG_MESSAGE, listener.types.get(1));

        // log 메시지 확인
        Field fState = FileRenameViewModel.class.getDeclaredField("state");
        fState.setAccessible(true);
        FileRenameState state = (FileRenameState) fState.get(vm);
        assertTrue(state.getLogMessages().stream()
                .anyMatch(msg -> msg.contains("총 1개의 파일명이 변경되었습니다.")));
    }

    static class TestListener implements FileRenameViewModel.StateListener {
        final AtomicInteger count = new AtomicInteger(0);
        final java.util.List<FileRenameViewModel.ResultType> types = new java.util.ArrayList<>();

        @Override
        public void onStateChanged(FileRenameViewModel.ResultType type, FileRenameState state) {
            types.add(type);
            count.incrementAndGet();
        }

        void reset() {
            count.set(0);
            types.clear();
        }
    }
}
