package org.example.intent;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileRenameIntentTest {

    @Test
    void addFilesIntent_shouldReturnCorrectTypeAndFiles() {
        List<File> files = List.of(new File("a"), new File("b"));
        AddFilesIntent intent = new AddFilesIntent(files);
        assertEquals(FileRenameIntent.IntentType.ADD, intent.getIntentType());
        assertEquals(files, intent.files());
    }

    @Test
    void patternChangedIntent_shouldReturnCorrectTypeAndPattern() {
        PatternChangedIntent intent = new PatternChangedIntent("patt");
        assertEquals(FileRenameIntent.IntentType.PATTERN_CHANGED, intent.getIntentType());
        assertEquals("patt", intent.newPattern());
    }

    @Test
    void startNumberChangedIntent_shouldReturnCorrectTypeAndNumber() {
        StartNumberChangedIntent intent = new StartNumberChangedIntent(7);
        assertEquals(FileRenameIntent.IntentType.START_NUMBER_CHANGED, intent.getIntentType());
        assertEquals(7, intent.newStartNumber());
    }

    @Test
    void renameFilesIntent_shouldReturnCorrectTypePatternAndNumber() {
        RenameFilesIntent intent = new RenameFilesIntent("p", 3);
        assertEquals(FileRenameIntent.IntentType.RENAME, intent.getIntentType());
        assertEquals("p", intent.newPattern());
        assertEquals(3, intent.startNumber());
    }
}
