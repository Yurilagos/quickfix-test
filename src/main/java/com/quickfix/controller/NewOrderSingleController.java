package com.quickfix.controller;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.quickfix.QuickfixTestApplication;
import com.quickfix.banzai.Order;
import com.quickfix.banzai.OrderSide;
import com.quickfix.banzai.OrderTIF;
import com.quickfix.banzai.OrderType;
import com.quickfix.service.NewOrderSingleService;

import lombok.AllArgsConstructor;
import quickfix.Initiator;
import quickfix.SessionID;

@RestController
@AllArgsConstructor
public class NewOrderSingleController {

	private final Initiator clientInitiator;

	@Autowired
	private NewOrderSingleService service;

	@PostMapping("/buy-order")
	@ResponseStatus(OK)
	private void createBuyOrder(@RequestParam String symbol, @RequestParam int quantity, @RequestParam String orderType,
			@RequestParam double limitPrice, @RequestParam double stopPrice) {
		Order order = new Order();
		order.setSide(OrderSide.BUY);
		order.setType(OrderType.parse(orderType));
		order.setTIF(OrderTIF.DAY);

		order.setSymbol(symbol);
		order.setQuantity(quantity);
		order.setOpen(quantity);

		OrderType type = order.getType();
		if (type == OrderType.LIMIT || type == OrderType.STOP_LIMIT)
			order.setLimit(limitPrice);
		if (type == OrderType.STOP || type == OrderType.STOP_LIMIT)
			order.setStop(stopPrice);
		// Apenas para 1 session, caso use mais de 1 ao mesmo tempo é neessário fazer um
		// filter
		SessionID sessionID = clientInitiator.getSessions().stream().findFirst().orElseThrow(RuntimeException::new);
		order.setSessionID(sessionID);
		service.send42(order);
	}
	
	@PostMapping("/cancell-order")
	@ResponseStatus(OK)
	private void createCancelOrder(@RequestParam String orderId) {
		Order order = QuickfixTestApplication.orderMapId.get(orderId);
		SessionID sessionID = clientInitiator.getSessions().stream().findFirst().orElseThrow(RuntimeException::new);
		order.setSessionID(sessionID);
		service.cancel42(order);
	}

}
