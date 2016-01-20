/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.event.spi.jpa;

/**
 * Factory for building instances user-specified event listeners.
 *
 * @author Steve Ebersole
 */
public interface ListenerFactory {
	public <T> T buildListener(Class<T>  listenerClass);

	public void release();
	public void setup();
}
