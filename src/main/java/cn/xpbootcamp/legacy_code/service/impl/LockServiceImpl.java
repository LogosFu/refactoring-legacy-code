package cn.xpbootcamp.legacy_code.service.impl;

import cn.xpbootcamp.legacy_code.service.LockService;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

public class LockServiceImpl implements LockService {

    @Override
    public void unlock(String id) {
        RedisDistributedLock.getSingletonInstance().unlock(id);
    }

    @Override
    public boolean lock(String id) {
        return RedisDistributedLock.getSingletonInstance().lock(id);
    }

}