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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Stefan Frei <stefan.a.frei@gmail.com>
 */
public class CacheManager implements FilechangeListener {

    private static final Logger w = LogManager.getLogger(CacheManager.class);

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
        for(;;) {
            try {
                w.info("i am managing directory [ " + dir + " ]");
                Thread.currentThread().sleep(2000);
            } catch(InterruptedException e) {
                w.error(e.getMessage());
            }
        }
    }

    @Override
    public void fileChanged() {
        w.info("file has changed");
    }

}
