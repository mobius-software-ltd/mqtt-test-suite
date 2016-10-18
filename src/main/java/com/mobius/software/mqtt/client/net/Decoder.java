package com.mobius.software.mqtt.client.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;

public class Decoder extends ByteToMessageDecoder
{
	static AtomicInteger count = new AtomicInteger();
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
	{
		ByteBuf nextHeader = null;
		do
		{
			if (buf.readableBytes() > 1)
				nextHeader = MQParser.next(buf);

			if (nextHeader != null)
			{
				buf.readBytes(nextHeader, nextHeader.capacity());
				try
				{
					MQMessage header = MQParser.decode(nextHeader);
					out.add(header);
				}
				catch (Exception e)
				{
					buf.resetReaderIndex();
					e.printStackTrace();
				}
				finally
				{
					nextHeader.release();
				}
			}
		}
		while (buf.readableBytes() > 1 && nextHeader != null);
	}
}
