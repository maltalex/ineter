/**
 * Copyright (c) 2018, Ineter Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.bit48.ineter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public abstract class IPRange<T extends IPAddress & Comparable<T>> implements Iterable<T>, Serializable {

	private static final long serialVersionUID = 1L;

	public abstract T getFirst();

	public abstract T getLast();

	public boolean overlaps(IPRange<T> range) {
		// Either one of the ends of the other range is within this one
		// Or this range is completely inside the other range. In that case,
		// it's enough to check just one of the edges of this range
		return this.contains(range.getFirst()) || this.contains(range.getLast()) || range.contains(this.getFirst());
	}

	public boolean contains(T ip) {
		return this.getFirst().compareTo(ip) <= 0 && this.getLast().compareTo(ip) >= 0;
	}

	public boolean contains(IPRange<T> range) {
		return this.contains(range.getFirst()) && this.contains(range.getLast());
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + this.getFirst().hashCode();
		result = prime * result + this.getLast().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		IPRange<T> other = (IPRange<T>) obj;
		return this.getFirst().equals(other.getFirst()) && this.getLast().equals(other.getLast());
	}

	@Override
	public String toString() {
		return String.format("%s - %s", this.getFirst().toString(), this.getLast().toString());
	}

	public abstract Number length();

	@Override
	public Iterator<T> iterator() {
		return iterator(false);
	}

	public Iterator<T> iterator(boolean trim) {
		return iterator(trim, trim);
	}

	public abstract Iterator<T> iterator(boolean skipFirst, boolean skipLast);

	public abstract List<? extends IPSubnet<? extends T>> toSubnets();
}
