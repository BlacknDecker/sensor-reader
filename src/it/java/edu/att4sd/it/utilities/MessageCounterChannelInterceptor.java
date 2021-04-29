package edu.att4sd.it.utilities;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

public class MessageCounterChannelInterceptor implements ChannelInterceptor {
	public final AtomicInteger sendCount = new AtomicInteger();
	private Logger logger = LoggerFactory.getLogger(MessageCounterChannelInterceptor.class);
	
	@Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        sendCount.incrementAndGet();
        logger.info("Message n°"+ sendCount.get() +" Transiting Channel: " + message.getPayload());
        return message;
    }
	
	public void resetCount() {
		sendCount.set(0);
	}
}