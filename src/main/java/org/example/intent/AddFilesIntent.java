package org.example.intent;

import java.io.File;
import java.util.List;

public record AddFilesIntent(List<File> files) implements FileRenameIntent {

    @Override
    public IntentType getIntentType() {
        return IntentType.ADD;
    }
}
