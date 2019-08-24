package com.ericsantanna.filemanager.models

class ClipboardState {
    enum ActionType {COPY, CUT}
    Integer hash
    ActionType actionType

    static Integer generateHash(Object[] array) {
        Arrays.hashCode(array)
    }
}
