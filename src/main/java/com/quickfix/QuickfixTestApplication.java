package com.quickfix;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.quickfix.banzai.Order;
import com.quickfix.fixcomponents.ApplicationMessageCracker;
import com.quickfix.fixcomponents.ClientApplicationFix;

import io.allune.quickfixj.spring.boot.starter.EnableQuickFixJClient;
import lombok.extern.slf4j.Slf4j;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.FileLogFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketInitiator;
import quickfix.fix42.MessageCracker;

@Slf4j
@EnableQuickFixJClient
@SpringBootApplication
public class QuickfixTestApplication {

	public static HashMap<String, Order> orderMapId = new HashMap<String, Order>();

	public static void main(String[] args) throws Exception {
		SpringApplication.run(QuickfixTestApplication.class, args);
	}

	@Bean
	public Application clientApplication(MessageCracker messageCracker) {
		return new ClientApplicationFix(messageCracker);
	}

	@Bean
	public MessageCracker messageCracker() {
		return new ApplicationMessageCracker();
	}

	@Bean
	public Initiator clientInitiator(quickfix.Application clientApplication,
			MessageStoreFactory clientMessageStoreFactory, SessionSettings clientSessionSettings,
			LogFactory clientLogFactory, MessageFactory clientMessageFactory) throws ConfigError {

		return new ThreadedSocketInitiator(clientApplication, clientMessageStoreFactory, clientSessionSettings,
				clientLogFactory, clientMessageFactory);
	}

	@Bean
	public LogFactory clientLogFactory(SessionSettings clientSessionSettings) throws Exception {
		return new FileLogFactory(clientSessionSettings);
	}

}
