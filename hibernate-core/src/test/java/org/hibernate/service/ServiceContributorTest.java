/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.service;

import java.util.Map;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cache.internal.RegionFactoryInitiator;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import org.hibernate.testing.cache.CachingRegionFactory;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.Test;

import static org.hibernate.testing.junit4.ExtraAssertions.assertTyping;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Ebersole
 */
public class ServiceContributorTest extends BaseUnitTestCase {
	@Test
	public void overrideCachingInitiator() {
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
		ssrb.clearSettings();

		final MyRegionFactoryInitiator initiator = new MyRegionFactoryInitiator();
		ssrb.addInitiator( initiator );

		final ServiceRegistryImplementor registry = (ServiceRegistryImplementor) ssrb.build();
		try {
			final RegionFactory regionFactory = registry.getService( RegionFactory.class );
			assertTrue( initiator.called );
			assertTyping( MyRegionFactory.class, regionFactory );
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}
	@Test
	public void CallCachingInitiatorTwice() {
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
		ssrb.clearSettings();

		final MyRegionFactoryInitiator initiator = new MyRegionFactoryInitiator();
		ssrb.addInitiator( initiator );

		final ServiceRegistryImplementor registry = (ServiceRegistryImplementor) ssrb.build();
		try {
			final RegionFactory regionFactory = registry.getService( RegionFactory.class );
			assertEquals( initiator.calledCount, 1 );
			final RegionFactory regionFactory2 = registry.getService( RegionFactory.class );
			assertEquals( "initiator.calledCount should be two but was " + initiator.calledCount
					,initiator.calledCount, 2 );
			assertTyping( MyRegionFactory.class, regionFactory );
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	@Test
	public void overrideCachingInitiatorExplicitSet() {
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();

		final MyRegionFactoryInitiator initiator = new MyRegionFactoryInitiator();
		ssrb.addInitiator( initiator );
		ssrb.applySetting( AvailableSettings.CACHE_REGION_FACTORY, new MyRegionFactory() );

		final ServiceRegistryImplementor registry = (ServiceRegistryImplementor) ssrb.build();
		try {
			registry.getService( RegionFactory.class );
			assertFalse( initiator.called );
		}
		finally {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}

	class MyRegionFactoryInitiator extends RegionFactoryInitiator {
		private boolean called = false;
		private int calledCount = 0;

		@Override
		protected RegionFactory getFallback(
				Map configurationValues,
				ServiceRegistryImplementor registry) {
			called = true;
			calledCount++;
			return new MyRegionFactory();
		}

//			@Override
//			public RegionFactory initiateService(
//					Map configurationValues,
//					ServiceRegistryImplementor registry) {
//				called = true;
//				return super.initiateService( configurationValues, registry );
//			}
	}

	class MyRegionFactory extends CachingRegionFactory {
	}

}
