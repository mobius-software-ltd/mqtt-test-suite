package com.mobius.software.mqtt.performance.commons.data;

public class Counter
{
	protected Integer count;
	protected Direction direction;

	public Counter()
	{
		super();
	}

	public Counter(Integer count, Direction direction)
	{
		super();
		this.count = count;
		this.direction = direction;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}

	public boolean messageIsIncoming()
	{
		return this.direction == Direction.INCOMING;
	}
}
