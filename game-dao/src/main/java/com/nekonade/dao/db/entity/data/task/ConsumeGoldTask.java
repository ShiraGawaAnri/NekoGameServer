package com.nekonade.dao.db.entity.data.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumeGoldTask extends BasicTask {

    private int quota;

    @Override
    public boolean finishCheck() {
        return false;
    }

    @Override
    public boolean rewriteCheckFinish() {
        return false;
    }

    @Override
    public boolean checkParam() {
        return false;
    }

    @Override
    public Object taskQuota() {
        return quota;
    }
}
