package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.TestOrder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;

@Getter
@Component
public class OrderQueueManagerService {

    //체결용 출력
    private final PriorityQueue<TestOrder> buyqueue = new PriorityQueue<>(new Comparator<TestOrder>() {
        @Override
        public int compare(TestOrder o1, TestOrder o2) {
            return Double.compare(o2.getPrice(), o1.getPrice());//가격이 높은 순
        }
    });
    private final PriorityQueue<TestOrder> sellqueue = new PriorityQueue<>(new Comparator<TestOrder>() {
        @Override
        public int compare(TestOrder o1, TestOrder o2) {
            return Double.compare(o1.getPrice(), o2.getPrice());//가격이 낮은 순
        }
    });

    //generator로부터 입력
    public void addBuyOrder(double price, double volume){
        buyqueue.offer(new TestOrder(TestOrder.Type.BUY,price,volume,System.currentTimeMillis()));
    }
    public void addSellOrder(double price, double volume){
        sellqueue.offer(new TestOrder(TestOrder.Type.SELL,price,volume,System.currentTimeMillis()));
    }

    //큐 로그 확인용
    public void logAllOrders(){
        System.out.println("== BUY QUEUE ==");
        buyqueue.forEach(System.out::println);
        System.out.println("== SELL QUEUE ==");
        sellqueue.forEach(System.out::println);
    }

}
