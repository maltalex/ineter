package com.github.maltalex.ineter.base;

/**
 * Class for the base {@link IPAddress} extensions, that need access to exact
 * type of {@code this}.
 * 
 * @param <C>
 *            exact type of {@code this}
 */
public abstract class ExtendedIPAddress<C extends IPAddress> extends IPAddress implements Comparable<C> {

	/**
	 * Checks whether this and other addresses are adjacent to each other
	 * without wrap-around. So 0.0.0.0 (or ::) is not adjacent to
	 * 255.255.255.255 (or ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff) But 0.0.0.0
	 * (or ::) is adjacent to 0.0.0.1 (or ::1)
	 *
	 * @param other
	 *            another address to compare
	 * @return true - are adjacent, false - not adjacent
	 */
	public abstract boolean isAdjacentTo(C other);

}
