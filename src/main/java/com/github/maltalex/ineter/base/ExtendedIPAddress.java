package com.github.maltalex.ineter.base;

/**
 * Class for the base {@link IPAddress} extensions, that need access to exact type of {@code this}.
 * @param <C> exact type of {@code this}
 */
public abstract class ExtendedIPAddress<C extends IPAddress> extends IPAddress {
	/**
	 * Checks whether this and other addresses are adjacent to each other
	 *
	 * @param other
	 * 		another address to compare
	 * @return true - are adjacent, false - not adjacent
	 */
	public abstract boolean isAdjacentTo(C other);
}
