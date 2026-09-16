package demo.usercart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.usercart.model.JwtUtility;
import demo.usercart.model.Order;
import demo.usercart.service.OrderItemService;
import demo.usercart.service.OrderService;

@RestController
@RequestMapping("/api/items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtility jwtUtility;

    @GetMapping("/{orderid}")
    public ResponseEntity<?> getItemsById(
            @PathVariable Integer orderid,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization
    ) {

        // 1. 驗證 Access Token，取得登入會員帳號
        String loginUsername =
                jwtUtility.extractUsernameFromAuthorization(
                        authorization
                );

        // 2. 查詢明細所屬的訂單
        Order order = orderService.getOrdersById(orderid);

        // 3. 訂單不存在，或不屬於目前會員，回傳 404
        if (order == null
                || order.getUser() == null
                || !loginUsername.equals(
                        order.getUser().getUsername()
                )) {

            return ResponseEntity.notFound().build();
        }

        // 4. 確認是自己的訂單後，才查詢商品明細
        return ResponseEntity.ok(
                orderItemService.getItemsById(orderid)
        );
    }
}