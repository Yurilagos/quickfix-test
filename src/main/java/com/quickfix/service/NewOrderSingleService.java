package com.quickfix.service;

import org.springframework.stereotype.Service;

import com.quickfix.QuickfixTestApplication;
import com.quickfix.banzai.Order;
import com.quickfix.banzai.OrderSide;
import com.quickfix.banzai.OrderType;
import com.quickfix.utils.CommonsUtils;

import io.allune.quickfixj.spring.boot.starter.template.QuickFixJTemplate;
import lombok.AllArgsConstructor;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

@Service
@AllArgsConstructor
public class NewOrderSingleService {

	private final QuickFixJTemplate clientQuickFixJTemplate;

	public void send42(Order order) {
		quickfix.fix42.NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(new ClOrdID(order.getID()),
				new HandlInst('1'), new Symbol(order.getSymbol()), CommonsUtils.sideToFIXSide(order.getSide()),
				new TransactTime(), CommonsUtils.typeToFIXType(order.getType()));
		newOrderSingle.set(new OrderQty(order.getQuantity()));
		QuickfixTestApplication.orderMapId.put(order.getID(), order);
		clientQuickFixJTemplate.send(populateOrder(order, newOrderSingle), order.getSessionID());
	}
	
	public void cancel42(Order order) {
		String id = order.generateID();
		quickfix.fix42.OrderCancelRequest message = new quickfix.fix42.OrderCancelRequest(
				new OrigClOrdID(order.getID()), new ClOrdID(id), new Symbol(order.getSymbol()),
				CommonsUtils.sideToFIXSide(order.getSide()), new TransactTime());
		message.setField(new OrderQty(order.getQuantity()));

		clientQuickFixJTemplate.send(message, order.getSessionID());
	}

	public quickfix.Message populateOrder(Order order, quickfix.Message newOrderSingle) {

		OrderType type = order.getType();

		if (type == OrderType.LIMIT)
			newOrderSingle.setField(new Price(order.getLimit()));
		else if (type == OrderType.STOP) {
			newOrderSingle.setField(new StopPx(order.getStop()));
		} else if (type == OrderType.STOP_LIMIT) {
			newOrderSingle.setField(new Price(order.getLimit()));
			newOrderSingle.setField(new StopPx(order.getStop()));
		}

		if (order.getSide() == OrderSide.SHORT_SELL || order.getSide() == OrderSide.SHORT_SELL_EXEMPT) {
			newOrderSingle.setField(new LocateReqd(false));
		}

		newOrderSingle.setField(CommonsUtils.tifToFIXTif(order.getTIF()));
		return newOrderSingle;
	}

}
