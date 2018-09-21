/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.service;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MultipleInitiatorTest extends BaseUnitTestCase {

	@After
	public void cleanupState() {
		MyRegionFactoryInitiator.cleanupState();
	}

	@Test
	public void testContributeTwoSessionFactories() {
		SessionFactory sf1 = null, sf2 = null;
		try {
			// NOTE : Assumes that the initiator is discovered through a ServiceContributor...
			// NOTE : Also assumes that the fallback RegionFactory is triggered

			assertEquals( 0, MyRegionFactoryInitiator.getContributeCalledCount );

			sf1 = new Configuration().buildSessionFactory();
			assertEquals( 1, MyRegionFactoryInitiator.getContributeCalledCount );

			sf2 = new Configuration().buildSessionFactory();
			assertEquals( 2, MyRegionFactoryInitiator.getContributeCalledCount );

/*
			// additionally:
			final RegionFactory rf1 = ( (SessionFactoryImplementor) sf1 ).getServiceRegistry();
			final RegionFactory rf2 = ( (SessionFactoryImplementor) sf2 ).getServiceRegistry();

			// we should have hit the fallback (apparently)
			assert rf1 instanceof MyRegionFactory;
			assert rf2 instanceof MyRegionFactory;

			// we should have 2 distinct RegionFactory instances
			assert rf1 != rf2;
*/
		}
		finally {
			if ( sf1 != null ) {
				sf1.close();
			}
			if ( sf2 != null ) {
				sf2.close();
			}
		}
	}

	@Test
	public void testFallbackTwoSessionFactories() {
		SessionFactory sf1 = null, sf2 = null;
		try {
			// NOTE : Assumes that the initiator is discovered through a ServiceContributor...
			// NOTE : Also assumes that the fallback RegionFactory is triggered

			assertEquals( 0, MyRegionFactoryInitiator.getFallbackCalledCount );

			sf1 = new Configuration().buildSessionFactory();
			assertEquals( 1, MyRegionFactoryInitiator.getFallbackCalledCount );

			sf2 = new Configuration().buildSessionFactory();
			assertEquals( 2, MyRegionFactoryInitiator.getFallbackCalledCount );
		}
		finally {
			if ( sf1 != null ) {
				sf1.close();
			}
			if ( sf2 != null ) {
				sf2.close();
			}
		}
	}

	@Test
	public void testResolveRegionFactoryTwoSessionFactories() {
		SessionFactory sf1 = null, sf2 = null;
		try {
			// NOTE : Assumes that the initiator is discovered through a ServiceContributor...
			// NOTE : Also assumes that the fallback RegionFactory is triggered

			assertEquals( 0, MyRegionFactoryInitiator.getResolveRegionFactoryCalledCount );

			sf1 = new Configuration().buildSessionFactory();
			assertEquals( 1, MyRegionFactoryInitiator.getResolveRegionFactoryCalledCount );

			sf2 = new Configuration().buildSessionFactory();
			assertEquals( 2, MyRegionFactoryInitiator.getResolveRegionFactoryCalledCount );
		}
		finally {
			if ( sf1 != null ) {
				sf1.close();
			}
			if ( sf2 != null ) {
				sf2.close();
			}
		}
	}


}
