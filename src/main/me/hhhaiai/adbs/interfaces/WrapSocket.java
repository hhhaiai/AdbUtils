package me.hhhaiai.adbs.interfaces;

import java.io.InputStream;

/**
 * 包装用Socket，包装通过其他方式实现的Socket
 */
public interface WrapSocket {

    void writeBytes(byte[] content);

    InputStream getInputStream();

    void disconnect() throws Exception;
}
