package se.coolcode.locknload.api.templates;

import se.coolcode.locknload.api.Lock;

public interface Template {

    Object get(String user, String resource);
    void put(String user, String resource, Object data);
    Lock getLock(String user, String resource);
}
