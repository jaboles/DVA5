package jb.common.ui;

public interface IExceptionSink {
    void store(String message) throws Exception;
}
