/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.service;

import java.util.Map;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cache.internal.RegionFactoryInitiator;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.service.spi.ServiceContributor;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import org.hibernate.testing.cache.CachingRegionFactory;

/**
 * MyRegionFactoryInitiator
 *
 */
public class MyRegionFactoryInitiator extends RegionFactoryInitiator implements ServiceContributor, Service {
	protected static int getFallbackCalledCount = 0;
	protected static int getContributeCalledCount = 0;
	protected static int getResolveRegionFactoryCalledCount = 0;

	public static void cleanupState() {
		getContributeCalledCount = 0;
		getFallbackCalledCount = 0;
		getResolveRegionFactoryCalledCount = 0;
	}

	@Override
	protected RegionFactory getFallback(
			Map configurationValues,
			ServiceRegistryImplementor registry) {
		getFallbackCalledCount++;
		return new MyRegionFactory();
	}
	@Override
    protected RegionFactory resolveRegionFactory(Map configurationValues, ServiceRegistryImplementor registry) {
		getResolveRegionFactoryCalledCount++;
		return new MyRegionFactory();
	}

	@Override
	public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
		getContributeCalledCount++;
		serviceRegistryBuilder.addInitiator( this );
	}
	
	public static class MyRegionFactory extends CachingRegionFactory {
	}

}

