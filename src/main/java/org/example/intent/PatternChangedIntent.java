package org.example.intent;

public record PatternChangedIntent(String newPattern) implements FileRenameIntent {

    @Override
    public IntentType getIntentType() {
        return IntentType.PATTERN_CHANGED;
    }
}
