package cn.xpbootcamp.legacy_code.service;

public interface LockService {
    void unlock(String id);

    boolean lock(String id);
}
