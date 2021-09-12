package me.hhhaiai.adbs.interfaces;

import java.io.IOException;

/**
 * Created by qiaoruikai on 2019-04-19 16:13.
 */
public interface AbstCmdLine {
    void writeCommand(String cmd);
    String readOutput();
    void close() throws IOException;
    boolean isClosed();
}
