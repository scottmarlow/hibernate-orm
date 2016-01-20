/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.event.internal.jpa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.hibernate.jpa.event.spi.jpa.ListenerFactory;

/**
 * CID-based implementation of the ListenerFactory contract.  Listener instances are kept in a map keyed by Class
 * to achieve singleton-ness.
 *
 * @author Steve Ebersole
 */
public class BeanManagerListenerFactory implements ListenerFactory {
	private final BeanManager beanManager;
	private final Map<Class,BeanMetaData> listeners = new ConcurrentHashMap<Class, BeanMetaData>();

	public static BeanManagerListenerFactory fromBeanManagerReference(Object beanManagerReference) {
		return new BeanManagerListenerFactory( ( BeanManager ) beanManagerReference );
	}

	public BeanManagerListenerFactory(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	@Override
	public <T> T buildListener(Class<T> listenerClass) {
		BeanMetaData<T> beanMetaData = listeners.get( listenerClass );
		if ( beanMetaData == null ) {
			beanMetaData = new BeanMetaData<T>( listenerClass );
			listeners.put( listenerClass, beanMetaData );
			beanMetaData.setup(); // TODO: hack alert, move this call to the Extension callback
		}
		return beanMetaData.instance;
	}

	@Override
	public void setup() {
		for ( BeanMetaData beanMetaData : listeners.values() ) {
			beanMetaData.setup();
		}
	}

	@Override
	public void release() {
		for ( BeanMetaData beanMetaData : listeners.values() ) {
			beanMetaData.release();
		}
		listeners.clear();
	}

	private class BeanMetaData<T> {
		private final InjectionTarget<T> injectionTarget;
		private volatile CreationalContext<T> creationalContext;
		private volatile T instance;

		private BeanMetaData(Class<T> listenerClass) {
			AnnotatedType<T> annotatedType = beanManager.createAnnotatedType( listenerClass );
			this.injectionTarget = beanManager.createInjectionTarget( annotatedType );
		}

		private void setup() {
			this.creationalContext = beanManager.createCreationalContext( null );

			this.instance = injectionTarget.produce( creationalContext );
			injectionTarget.inject( this.instance, creationalContext );

			injectionTarget.postConstruct( this.instance );
		}

		private void release() {
			injectionTarget.preDestroy( instance );
			injectionTarget.dispose( instance );
			creationalContext.release();
		}
	}
}
