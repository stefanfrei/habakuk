/*
 * The MIT License
 *
 * Copyright 2021 Stefan Frei <stefan.a.frei@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.schlibbuz.habakuk;

/**
 *
 * @author Stefan Frei <stefan.a.frei@gmail.com>
 */
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.nio.file.attribute.BasicFileAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 *
 * @author Stefan
 */
class DirectoryObserver implements Runnable {

    private static final Logger w = LogManager.getLogger(DirectoryObserver.class);

    private final Queue eq;

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private boolean shouldRun = false;

    /**
     * Creates a DirectoryObserver and registers the given directory
     */
    DirectoryObserver(Path dir, Queue<String> eq) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        recursive = true;

        this.eq = eq;

        w.info("Scanning " + dir + " ...");
        registerAll(dir);
        w.info("Done.");

        // enable trace after initial registration
        this.trace = true;
        this.shouldRun = true;
    }

    @Override
    public void run() {

        // Listen for file-changes.
        while (shouldRun) {

            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                w.warn(e.getMessage());
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                w.warn("unsupported file-event");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                w.info(event.kind().name() + ": " + child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException e) {
                        w.error(e.getMessage());
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }


    // Register dir to be observed.
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                w.info("register: " + dir);
            } else {
                if (!dir.equals(prev)) {
                    w.info("update: " + prev + " -> " + dir);
                }
            }
        }
        keys.put(key, dir);
    }


    // Register dir to be observed (recursive variant).
    private void registerAll(final Path start) throws IOException {

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // this is only here for consistence, rarely used maybe.
    void enableProcessEvents() {
        this.shouldRun = true;
    }

    // Tomcat doesnt like infinite loops, control from outside is needed.
    void disableProcessEvents() {
        this.shouldRun = false;
    }

}
