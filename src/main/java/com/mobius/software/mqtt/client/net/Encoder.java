package com.mobius.software.mqtt.client.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class Encoder extends MessageToByteEncoder<MQMessage>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, MQMessage msg, ByteBuf out) throws Exception
	{
		if (msg.getType() == MessageType.DISCONNECT)
			ctx.pipeline().remove("handler");
		out.writeBytes(MQParser.encode(msg));
	}
}
