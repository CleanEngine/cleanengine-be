package com.cleanengine.coin.mypage.controller;

import com.cleanengine.coin.common.response.ApiResponse;
import com.cleanengine.coin.mypage.dto.PagedCompletedOrdersDto;
import com.cleanengine.coin.mypage.service.CompletedOrderService;
import com.cleanengine.coin.user.login.infra.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userinfo")
public class CompletedOrderController {
    private final CompletedOrderService completedOrderService;
    @GetMapping("/trades")
//    public ResponseEntity<ApiResponse<List<CompletedOrderDto>>> getCompletedOrder(@AuthenticationPrincipal CustomOAuth2User user) {
    public ResponseEntity<ApiResponse<PagedCompletedOrdersDto>> getCompletedOrder(@RequestParam Integer userId,
                                                                                  @RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "10") int size,
                                                                                  @RequestParam(defaultValue = "false") boolean settled) {
//        List<CompletedOrderDto> completedOrders = completedOrderService.getCompletedOrders(user.getUserId());
        System.out.println("===============controller : "+userId+"==============");
        PagedCompletedOrdersDto completedOrders = completedOrderService.getCompletedOrders(userId,page,size,settled);
        return ApiResponse.success(completedOrders, HttpStatus.OK).toResponseEntity();
    }
}
