package org.example.intent;

public record RenameFilesIntent(String newPattern, int startNumber) implements FileRenameIntent {

    @Override
    public IntentType getIntentType() {
        return IntentType.RENAME;
    }
}
