//$Id$
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.jpa.test;

import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;
import org.hibernate.jpa.AvailableSettings;
import org.hibernate.testing.jta.TestingJtaBootstrap;
import org.hibernate.testing.jta.TestingJtaPlatformImpl;
import org.junit.Test;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.Map;

import static org.junit.Assert.*;


/**
 * EntityManagerFactoryClosedTest
 *
 * @author Scott Marlow
 */
public class EntityManagerFactoryClosedTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected void addConfigOptions(Map options) {
		super.addConfigOptions( options );
		TestingJtaBootstrap.prepare(options);
		options.put( AvailableSettings.TRANSACTION_TYPE, "JTA" );
	}

	@Override
	public Class[] getAnnotatedClasses() {
		return new Class[] {
				Item.class,
				Distributor.class,
				Wallet.class
		};
	}


	/**
	 * Test that using a closed EntityManagerFactory throws an IllegalStateException
	 * Also ensure that HHH-8586 doesn't regress.
	 * @throws Exception
	 */
	@Test
	public void testWithTransactionalEnvironment() throws Exception {

		assertFalse( JtaStatusHelper.isActive(TestingJtaPlatformImpl.INSTANCE.getTransactionManager()) );
		TestingJtaPlatformImpl.INSTANCE.getTransactionManager().begin();
		assertTrue( JtaStatusHelper.isActive(TestingJtaPlatformImpl.INSTANCE.getTransactionManager()) );
		EntityManagerFactory entityManagerFactory = entityManagerFactory();

		entityManagerFactory.close();	// close the underlying entity manager factory

		try {
			entityManagerFactory.createEntityManager();
			fail( "expected IllegalStateException when calling emf.createEntityManager with closed emf" );
		} catch( IllegalStateException expected ) {
			// success

		}

		try {
			entityManagerFactory.getCriteriaBuilder();
			fail( "expected IllegalStateException when calling emf.getCriteriaBuilder with closed emf" );
		} catch( IllegalStateException expected ) {
			// success
		}

		try {
			entityManagerFactory.getCache();
			fail( "expected IllegalStateException when calling emf.getCache with closed emf" );
		} catch( IllegalStateException expected ) {
			// success
		}

		assertFalse( entityManagerFactory.isOpen() );

		TestingJtaPlatformImpl.INSTANCE.getTransactionManager().commit();
	}
	@Test
	public void cts180() throws Exception {

		assertFalse( JtaStatusHelper.isActive(TestingJtaPlatformImpl.INSTANCE.getTransactionManager()) );
		TestingJtaPlatformImpl.INSTANCE.getTransactionManager().begin();
		assertTrue( JtaStatusHelper.isActive(TestingJtaPlatformImpl.INSTANCE.getTransactionManager()) );
		EntityManager entityManager = getOrCreateEntityManager();

		entityManager.joinTransaction();

		int count = entityManager.createNativeQuery("DELETE FROM Item").executeUpdate();
		assertTrue("deleted row count should be zero", count == 0);

		Item w = new Item("name1","description");
		entityManager.persist(w);
		entityManager.flush();

		w = entityManager.find(Item.class, "name1");
		assertNotNull("couldn't find name1", w);
		w.setDescr("description3");

		count = entityManager.createNativeQuery("DELETE FROM Item").executeUpdate();
		assertTrue("deleted row count should be one", count == 1);

		TestingJtaPlatformImpl.INSTANCE.getTransactionManager().commit();
		entityManager.close();
	}
}
