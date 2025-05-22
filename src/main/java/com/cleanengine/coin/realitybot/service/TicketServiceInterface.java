package com.cleanengine.coin.realitybot.service;


import com.cleanengine.coin.realitybot.dto.Ticks;

import java.util.Queue;

public interface TicketServiceInterface {
    double calculateVWAP(Queue<Ticks> ticksQueue);
}
