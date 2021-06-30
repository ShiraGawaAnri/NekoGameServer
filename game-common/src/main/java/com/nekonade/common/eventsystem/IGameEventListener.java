package com.nekonade.common.eventsystem;

public interface IGameEventListener {

    void update(Object origin, IGameEventMessage event);
}
