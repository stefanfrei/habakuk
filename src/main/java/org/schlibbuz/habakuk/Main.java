/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.schlibbuz.habakuk;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Stefan
 */
public class Main {
    private static final Logger q = LogManager.getLogger(Main.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        q.error(System.class.getClassLoader());
        q.error(new File("").getAbsolutePath());
        q.trace(new File("").getAbsolutePath());
        q.error("üüüüüüüü");
    }

}
