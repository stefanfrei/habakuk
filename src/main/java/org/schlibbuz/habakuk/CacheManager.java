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

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Stefan Frei <stefan.a.frei@gmail.com>
 */
public class CacheManager {

    private static final Logger w = LogManager.getLogger(CacheManager.class);
    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    private final Path dir;
    private final DirectoryObserver dirObs;
    private final Queue<String> eq;

    CacheManager(Path dir) throws IOException {
        this.dir = dir;
        eq = new LinkedList<>();
        dirObs = new DirectoryObserver(dir, eq);
        initDirObs();
    }

    private void initDirObs() {
        new Thread(dirObs).start();
    }

    void work() {

        w.info("i am managing directory [ " + dir + " ]");

        AtomicLong waitTime = new AtomicLong(2000);

        try {
            for (;;) {
                w.info("iteration wait -> " + waitTime.get() + "ms");
                ScheduledFuture<String> future = exec.schedule(() -> {
                    return processQueue();
                }, waitTime.get(), TimeUnit.MILLISECONDS);
                String task = future.get();
                if (task != null) {
                    w.info("file [ " + task + " ] needs refresh");
                } else {
                    w.info("no work :D");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            w.warn(e.getMessage());
        }

    }

    /**
     *
     * @return task (path to changed file)
     */
    private String processQueue() {

        return eq.poll();

    }

}
