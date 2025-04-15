package org.example.intent;

public interface FileRenameIntent {
    IntentType getIntentType();

    enum IntentType {
        ADD,
        RENAME,
        PATTERN_CHANGED,
        START_NUMBER_CHANGED
    }
}

