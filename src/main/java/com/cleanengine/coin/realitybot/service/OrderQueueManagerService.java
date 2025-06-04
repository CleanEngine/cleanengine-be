package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.TestOrder;
import lombok.Getter;

import java.util.Comparator;
import java.util.PriorityQueue;

@Getter
//@Component
public class OrderQueueManagerService {
    /*해당 코드는 초기 개별 모듈로 작업할 때 가상의 체결을 만드는 코드였습니다.
    * 이젠 쓰이지 않는 코드이나 어떤 에러가 발생할 때 재사용하기 위한 용도로 삭제하지 않았습니다.
    * */

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
