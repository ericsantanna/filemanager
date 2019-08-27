package com.ericsantanna.filemanager.services

import com.ericsantanna.filemanager.models.ClipboardState
import com.ericsantanna.filemanager.models.PathItem
import com.ericsantanna.filemanager.utils.FileManagerDataFormat
import com.sun.javafx.PlatformUtil
import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ClipboardService {
    private static ClipboardState clipboardState

    void copy(Collection<Path> pathsToCopy) {
        def hash = ClipboardState.generateHash(pathsToCopy.toArray())
        clipboardState = new ClipboardState(hash: hash, actionType: ClipboardState.ActionType.COPY)
        setClipboardContent(pathsToCopy, clipboardState)
    }

    void cut(Collection<Path> pathsToCut) {
        def hash = ClipboardState.generateHash(pathsToCut.toArray())
        clipboardState = new ClipboardState(hash: hash, actionType: ClipboardState.ActionType.CUT)
        setClipboardContent(pathsToCut, clipboardState)
    }

    void paste(Path targetFolder) {
        if(!Files.isWritable(targetFolder)) {
            throw new Exception("$targetFolder is not a valid folder or we don't have enough privileges")
        }
        def contentImages = Clipboard.systemClipboard.getContent(DataFormat.IMAGE)
        def contentPlainText = Clipboard.systemClipboard.getContent(DataFormat.PLAIN_TEXT)
        def contentString = Clipboard.systemClipboard.getString()
        def contentGnome = Clipboard.systemClipboard.getContent(FileManagerDataFormat.GNOME_COPIED_FILES) as ByteBuffer
//        def contentPlainTimestamp = Clipboard.systemClipboard.getContent(FileManagerDataFormat.TIMESTAMP) as ByteBuffer
//        def contentPlainTargets = Clipboard.systemClipboard.getContent(FileManagerDataFormat.TARGETS) as ByteBuffer
//        def contentPlainMultiple = Clipboard.systemClipboard.getContent(FileManagerDataFormat.MULTIPLE)
//        def contentPlainCompound = Clipboard.systemClipboard.getContent(FileManagerDataFormat.COMPOUND_TEXT) as ByteBuffer

        // TODO Check System.getenv('XDG_CURRENT_DESKTOP')?
        if(PlatformUtil.isLinux()) {
        }

        if(Clipboard.systemClipboard.hasContent(FileManagerDataFormat.GNOME_COPIED_FILES)) {
            def content = Clipboard.systemClipboard.getContent(FileManagerDataFormat.GNOME_COPIED_FILES) as ByteBuffer
            def contentAsString = new String(content.array(), StandardCharsets.UTF_8)
            def parts = contentAsString.split(/\n/)

            if(parts.length < 2) {
                throw new Exception('Mimetype not supported!')
            }

            def isCopy = 'copy' == parts[0]
            def isCut = 'cut' == parts[0]

            if(!isCopy && !isCut) {
                throw new Exception('Mimetype not supported!')
            }

            def paths = []
            parts.eachWithIndex { String url, int i ->
                if(i == 0) return
                def path = Paths.get(new URL(url).toURI())
                if(!Files.isReadable(path)) {
                    throw new Exception("$path is not readable!")
                }
                paths << path
            }

            if(isCopy) {
                pasteCopiedPaths(paths, targetFolder)
            } else {
                pasteCutPaths(paths, targetFolder)
            }
        } else if(Clipboard.systemClipboard.hasFiles()) {
            def content = Clipboard.systemClipboard.getContent(DataFormat.FILES) as List<File>
            def currentClipboardHash = ClipboardState.generateHash(content.toArray())
            if(currentClipboardHash == clipboardState.hash && clipboardState.actionType == ClipboardState.ActionType.CUT) {

            } else {

            }
        }

//        println "Types": Clipboard.systemClipboard.getContentTypes()
//        println "gnome": new String( contentGnome.array(), StandardCharsets.UTF_8 )
//        println "gnome contentPlainTimestamp": new String( contentPlainTimestamp.array() )
//        println "gnome contentPlainTargets": new String( contentPlainTargets.array() )
//        println "gnome contentPlainCompound": new String( contentPlainCompound.array(), StandardCharsets.UTF_8 )
//        println "gnome hash": Arrays.toString( contentGnome.array() )
//        println "-----------------------------"
//        content.each {
//            println "content: $it"
//        }
//        println "-----------------------------"
//        println "contentImages: $contentImages"
//        println "-----------------------------"
//        println "contentPlainText: $contentPlainText"
//        println "-----------------------------"
//        println "contentString: $contentString"
//        println "-----------------------------"
//        println "contentGnome: $contentGnome"
//        println "-----------------------------"
//        println "contentPlainTimestamp: $contentPlainTimestamp"
//        println "-----------------------------"
//        println "contentPlainTargets: $contentPlainTargets"
//        println "-----------------------------"
//        println "contentPlainMultiple: $contentPlainMultiple"
//        println "-----------------------------"
//        println "contentPlainCompound: $contentPlainCompound"
//        println "-----------------------------"
    }

    private void pasteCopiedPaths(List<Path> paths, Path targetFolder) {
        paths.each { path ->
            Files.copy(path, targetFolder.resolve(path.getFileName()))
        }
    }

    private void pasteCutPaths(List<Path> paths, Path targetFolder) {
        paths.each { path ->
            Files.move(path, targetFolder.resolve(path.getFileName()))
        }
    }

    private void setClipboardContent(Collection<Path> paths, ClipboardState clipboardState) {
        ClipboardContent clipboardContent = new ClipboardContent()
        def files = paths.collect { path -> path.toFile() } as List<File>
        def filesAsString = files.collect { it.toString() }.join("\n")
        clipboardContent.clear()
        clipboardContent.putFiles(files)
        clipboardContent.putString(filesAsString)
        clipboardContent.put(FileManagerDataFormat.UTF8_PLAIN_TEXT, filesAsString)
//        clipboardContent.put(FileManagerDataFormat.MULTIPLE, filesAsString)
        clipboardContent.put(FileManagerDataFormat.UTF8_STRING, filesAsString)
//        clipboardContent.put(FileManagerDataFormat.STRING, filesAsString)
//        clipboardContent.put(FileManagerDataFormat.TARGETS, filesAsString)
//        clipboardContent.put(FileManagerDataFormat.TIMESTAMP, filesAsString)
//        clipboardContent.put(FileManagerDataFormat.COMPOUND_TEXT, filesAsString)

        // TODO Check System.getenv('XDG_CURRENT_DESKTOP')?
        if(PlatformUtil.isLinux()) {
            final StringBuilder builder = new StringBuilder(clipboardState.actionType.name().toLowerCase() + "\n")
            paths.each { Path path ->
                builder.append(path.toUri().toASCIIString()).append('\n')
            }

            builder.delete(builder.length() - 1, builder.length());

            final ByteBuffer buffer = ByteBuffer.allocate(builder.length())

            int i = 0
            for (int length = builder.length(); i < length; i++) {
                buffer.put((byte) builder.charAt(i))
            }

            buffer.flip()

            clipboardContent.put(FileManagerDataFormat.GNOME_COPIED_FILES, buffer)
        }

        Clipboard.systemClipboard.content = clipboardContent
    }

    boolean hasContentPastable() {
        Clipboard.systemClipboard.hasContent(FileManagerDataFormat.GNOME_COPIED_FILES) || Clipboard.systemClipboard.hasFiles()
    }
}
