package com.ht.rcs.blackberry.threadpool;

public interface QueueObject {
    void setEnqueued(boolean enqueued);
    boolean isEnqueued();
}
