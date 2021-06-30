package com.nekonade.common.gameMessage;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class DataManager {

    public AtomicInteger seqId = new AtomicInteger();

    public void seqIncr(){
        seqId.incrementAndGet();
    }
}
