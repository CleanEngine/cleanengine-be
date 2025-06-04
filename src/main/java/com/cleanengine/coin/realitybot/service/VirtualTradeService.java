package com.cleanengine.coin.realitybot.service;

import com.cleanengine.coin.realitybot.dto.TestOrder;

import java.util.*;

//@Service
public class VirtualTradeService {
    private final OrderQueueManagerService queueManager;
    private final PlatformVWAPService platformVWAPService;

    public VirtualTradeService(OrderQueueManagerService queueManager, PlatformVWAPService platformVWAPService) {
        this.platformVWAPService = platformVWAPService;
        this.queueManager = queueManager;
    }
    /*해당 코드는 초기 개별 모듈로 작업할 때 가상의 체결을 만드는 코드였습니다.
     * 이젠 쓰이지 않는 코드이나 어떤 에러가 발생할 때 재사용하기 위한 용도로 삭제하지 않았습니다.
     * */

    //가상 주문 매칭 및 체결 처리를 담당하는 서비스
    public void matchOrder(){
        //매수, 매도 주문 큐 관리
        PriorityQueue<TestOrder> buyQueue = queueManager.getBuyqueue();
        PriorityQueue<TestOrder> sellQueue = queueManager.getSellqueue();

        while(!buyQueue.isEmpty() && !sellQueue.isEmpty()){
            //주문 추출
            TestOrder buyOrder = buyQueue.peek(); //가장 높은 매수 주문
            TestOrder sellOrder = sellQueue.peek(); // 가장 낮은 매도 주문

            //체결 조건 부여 : 현재 느슨한 체결 (1:1은 문제 발생/어짜피 매서드 호출 힘너무 쓰면 안됨)
            //매수 희망가 >= 매도 희망가
            if ((long)buyOrder.getPrice() == (long)sellOrder.getPrice()){ //매도벽이 크게 세워짐..

            //체결 가격을 중간값으로 설정
//            double matchedPrice = (buyOrder.getPrice() + sellOrder.getPrice())/2; // 느슨한 체결 조건 쓰니깐 문제 발생

            //현재 매도가 기준
            double matchedPrice = sellOrder.getPrice();
            double matchedVolume = Math.min(buyOrder.getVolume(), sellOrder.getVolume()); //적은쪽으로 물량 설정
//            System.out.println("=== 체결 진행 - 가격 :"+matchedPrice+", 수량 : "+matchedVolume);

            //잔량 처리
            buyOrder.setVolume(buyOrder.getVolume() - matchedVolume);
            sellOrder.setVolume(sellOrder.getVolume() - matchedVolume);

            //잔량 0 이하 주문 제거
            if (buyOrder.getVolume() <= 0) buyQueue.poll();
            if (sellOrder.getVolume() <= 0) sellQueue.poll();

//            platformVWAPService.recordTrade(matchedPrice,matchedVolume);
            }
            else {
                break;
            }

        }
    }
    public void matchOrderbyIterator(){
        //queue 를 list로 변환
        List<TestOrder> buyOrders = new ArrayList<>(queueManager.getBuyqueue());
        List<TestOrder> sellOrders = new ArrayList<>(queueManager.getSellqueue());

        //
        buyOrders.sort(Comparator.comparing(TestOrder::getPrice).reversed());
        sellOrders.sort(Comparator.comparing(TestOrder::getPrice));

        List<TestOrder> excutedBuy = new ArrayList<>();
        List<TestOrder> excutedSell = new ArrayList<>();

        for (TestOrder buyOrder : buyOrders){
            for (TestOrder sellOrder : sellOrders){
                if ((int)buyOrder.getPrice() >= (int)sellOrder.getPrice()){
                    double matchVolume = Math.min(buyOrder.getVolume(), sellOrder.getVolume());
                    if (matchVolume <=0) continue;
                    buyOrder.setVolume(buyOrder.getVolume() - matchVolume);
                    sellOrder.setVolume(sellOrder.getVolume() - matchVolume);

                    if (buyOrder.getVolume() <= 0){
                        excutedBuy.add(buyOrder);
                        break;
                    }
                    if (sellOrder.getVolume() <= 0){
                        excutedSell.add(sellOrder);
//                        break;
                    }
                }

            }
        }
        queueManager.getBuyqueue().removeAll(excutedBuy);
        queueManager.getSellqueue().removeAll(excutedSell);

    }

    //매서드 종료 시 호가창 요약
    private void printSummary(PriorityQueue<TestOrder> queue, Comparator<Integer> sortOrder) {
        Map<Integer, Double> summary = new TreeMap<>(sortOrder);

        for (TestOrder order : queue) {
            int price = (int) order.getPrice(); // 호가 기준
            double volume = order.getVolume();
            summary.put(price, summary.getOrDefault(price, 0.0) + volume);
        }

        for (Map.Entry<Integer, Double> entry : summary.entrySet()) {
            System.out.printf("호가 %d원 : %.4f 개%n", entry.getKey(), entry.getValue());
        }
    }

    //전체 호가창 콘솔 출력
    public void printOrderSummary() {
        System.out.println("=== SELL ORDER SUMMARY ===");
        printSummary(queueManager.getSellqueue(), Comparator.reverseOrder()); // ⬇ 고가 → 저가
        System.out.println("=== BUY ORDER SUMMARY ===");
        printSummary(queueManager.getBuyqueue(), Comparator.reverseOrder()); // ⬆ 저가 → 고가
    }
}
