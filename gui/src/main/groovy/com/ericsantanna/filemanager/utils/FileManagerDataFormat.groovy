package com.ericsantanna.filemanager.utils

import javafx.scene.input.DataFormat

class FileManagerDataFormat {
    public static final DataFormat FILES = DataFormat.FILES
    public static final DataFormat PLAIN_TEXT = DataFormat.PLAIN_TEXT
    public static final DataFormat IMAGE = DataFormat.IMAGE
    public static final DataFormat GNOME_COPIED_FILES = new DataFormat('x-special/gnome-copied-files')
    public static final DataFormat COMPOUND_TEXT = new DataFormat('COMPOUND_TEXT')
    public static final DataFormat UTF8_PLAIN_TEXT = new DataFormat('text/plain;charset=utf-8')
    public static final DataFormat TARGETS = new DataFormat('TARGETS')
    public static final DataFormat UTF8_STRING = new DataFormat('UTF8_STRING')
    public static final DataFormat TEXT = new DataFormat('TEXT')
    public static final DataFormat STRING = new DataFormat('STRING')
    public static final DataFormat MULTIPLE = new DataFormat('MULTIPLE')
    public static final DataFormat TIMESTAMP = new DataFormat('TIMESTAMP')
}
