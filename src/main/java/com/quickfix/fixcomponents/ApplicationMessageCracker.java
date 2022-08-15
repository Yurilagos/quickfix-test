/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quickfix.fixcomponents;

import java.math.BigDecimal;

import com.quickfix.QuickfixTestApplication;
import com.quickfix.banzai.Order;
import com.quickfix.utils.CommonsUtils;

import lombok.extern.slf4j.Slf4j;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrigClOrdID;
import quickfix.field.Text;
import quickfix.fix42.MessageCracker;

@Slf4j
public class ApplicationMessageCracker extends MessageCracker {

	@Override
	public void onMessage(quickfix.fix42.OrderCancelRequest orderCancelRequest, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		// Handle the message here
		log.info("*****************");
		log.info("OrderCancelRequest Message received for sessionID={}: {}", sessionID, orderCancelRequest);
		log.info("*****************");
	}
	
	@Override
	public void onMessage(quickfix.fix42.NewOrderSingle newOrderSingle, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("NewOrderSingle Message received for sessionID={}: {}", sessionID, newOrderSingle);
		log.info("*****************");
	}
	@Override
	public void onMessage(quickfix.fix42.IndicationofInterest indicationofInterest, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("indicationofInterest Message received for sessionID={}: {}", sessionID, indicationofInterest);
		log.info("*****************");
	}

	@Override
	public void onMessage(quickfix.fix42.ExecutionReport executionReport, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("ExecutionReport Message received for sessionID={}: {}", sessionID, executionReport);
		log.info("*****************");

		ExecID execID = (ExecID) executionReport.getField(new ExecID());
		if (CommonsUtils.alreadyProcessed(execID, sessionID))
			return;

		Order order = QuickfixTestApplication.orderMapId.get(executionReport.getField(new ClOrdID()).getValue());
		if (order == null) {
			return;
		}

		BigDecimal fillSize;

		if (executionReport.isSetField(LastShares.FIELD)) {
			LastShares lastShares = new LastShares();
			executionReport.getField(lastShares);
			fillSize = new BigDecimal("" + lastShares.getValue());
		} else {
			// > FIX 4.1
			LeavesQty leavesQty = new LeavesQty();
			executionReport.getField(leavesQty);
			fillSize = new BigDecimal(order.getQuantity()).subtract(new BigDecimal("" + leavesQty.getValue()));
		}

		if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
			order.setOpen(order.getOpen() - (int) Double.parseDouble(fillSize.toPlainString()));
			order.setExecuted(Double.parseDouble(executionReport.getString(CumQty.FIELD)));
			order.setAvgPx(Double.parseDouble(executionReport.getString(AvgPx.FIELD)));
		}

		OrdStatus ordStatus = (OrdStatus) executionReport.getField(new OrdStatus());

		if (ordStatus.valueEquals(OrdStatus.REJECTED)) {
			order.setRejected(true);
			order.setOpen(0);
		} else if (ordStatus.valueEquals(OrdStatus.CANCELED) || ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY)) {
			order.setCanceled(true);
			order.setOpen(0);
		} else if (ordStatus.valueEquals(OrdStatus.NEW)) {
			if (order.isNew()) {
				order.setNew(false);
			}
		}

		try {
			order.setMessage(executionReport.getField(new Text()).getValue());
		} catch (FieldNotFound e) {
		}

//		orderTableModel.updateOrder(order, executionReport.getField(new ClOrdID()).getValue());
//		observableOrder.update(order);

	}

	@Override
	public void onMessage(quickfix.fix42.OrderCancelReject orderCancelReject, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("OrderCancelReject Message received for sessionID={}: {}", sessionID, orderCancelReject);
		log.info("*****************");
		
		String id = orderCancelReject.getField(new ClOrdID()).getValue();
		Order order =  QuickfixTestApplication.orderMapId.get(id);
		if (order == null)
			return;

		try {
			order.setMessage(orderCancelReject.getField(new Text()).getValue());
		} catch (FieldNotFound e) {
		}
	}

	@Override
	public void onMessage(quickfix.fix42.OrderCancelReplaceRequest orderCancelReplaceRequest, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("OrderCancelReplaceRequest Message received for sessionID={}: {}", sessionID, orderCancelReplaceRequest);
		log.info("*****************");
	}

	@Override
	public void onMessage(quickfix.fix42.NewOrderList newOrderList, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

		// Handle the message here
		log.info("*****************");
		log.info("NewOrderList Message received for sessionID={}: {}", sessionID, newOrderList);
		log.info("*****************");
	}

}