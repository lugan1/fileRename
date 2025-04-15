package org.example.intent;

public record StartNumberChangedIntent(int newStartNumber) implements FileRenameIntent {

    @Override
    public IntentType getIntentType() {
        return IntentType.START_NUMBER_CHANGED;
    }
}
