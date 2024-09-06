package se.coolcode.locknload.api;

public interface Lock extends AutoCloseable {

    boolean lock();
    void unlock();

    @Override
    default void close() {
        unlock();
    }
}
