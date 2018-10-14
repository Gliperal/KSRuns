package com.ksruns;

import static org.junit.Assert.*;

public class RunTest
{
	@org.junit.Test
	public void testRunCmp()
	{
		Run a = new Run(58, null, null, null, null);
		Run b = new Run(59, null, null, null, null);
		Run c = new Run(-1, null, null, null, null);
		assertFalse(a.betterTimeThan(a));
		assertFalse(b.betterTimeThan(b));
		assertFalse(c.betterTimeThan(c));
		assertTrue(a.betterTimeThan(b));
		assertTrue(b.betterTimeThan(c));
		assertFalse(c.betterTimeThan(a));
		assertTrue(a.betterTimeThan(c));
		assertFalse(c.betterTimeThan(b));
		assertFalse(b.betterTimeThan(a));
	}
}
