/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
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
package org.hibernate.jpa.test.transaction;

import org.hibernate.HibernateException;
import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;
import org.hibernate.testing.jta.TestingJtaBootstrap;
import org.hibernate.testing.jta.TestingJtaPlatformImpl;
import org.jboss.logging.Logger;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Recreate test failure that occurs when two threads share the same entity manager and transaction.
 * This simulates a remote EJB that is invoked multiple times under the same JTA transaction,
 * Each EJB invocation may use a different thread which currently causes a
 * "org.hibernate.HibernateException: Transaction was rolled back in a different thread!" .
 *
 * @author Scott Marlow
 */
public class TransactionJoinedToMultipleThreadsTest extends BaseEntityManagerFunctionalTestCase {

	private static final Logger log = Logger.getLogger( BaseEntityManagerFunctionalTestCase.class );


	@Override
	protected void addConfigOptions(Map options) {
		super.addConfigOptions(options);
		TestingJtaBootstrap.prepare(options);
		log.debugf("addConfigOptions complete options=%s", options);
	}

	@Test
	public void testTransactionRolledBackInDifferentThreadFailure() throws Exception {

		// main test thread
		TestingJtaPlatformImpl.INSTANCE.getTransactionManager().begin();
		final EntityManager em = entityManagerFactory().createEntityManager();
		em.joinTransaction();
		assertTrue("entity manager should be joined to JTA transaction",em.isJoinedToTransaction());

		// suspend the JTA transaction from the main test thread
		final Transaction transaction = TestingJtaPlatformImpl.INSTANCE.getTransactionManager().suspend();
		log.debugf( "suspended transaction %s on unit test thread %s",  transaction, Thread.currentThread().getName());

		// will be set to the failing exception
		final HibernateException[] transactionRolledBackInDifferentThreadException = new HibernateException[1];
		transactionRolledBackInDifferentThreadException[0] = null;

		// background test thread 1
		final Runnable run1 = new Runnable() {
			@Override
			public void run() {
				try {
					log.debugf( "resumed transaction %s on background thread %s", transaction, Thread.currentThread().getName() );
					// associate the JTA transaction with the background thread
					TestingJtaPlatformImpl.INSTANCE.getTransactionManager().resume(transaction);
					em.joinTransaction();
					log.debugf( "joined transaction on %s? answer=%b", Thread.currentThread().getName(), em.isJoinedToTransaction() );
					assertTrue("entity manager should be joined to JTA transaction on background thread",em.isJoinedToTransaction());
					// roll the transaction back, which shouldn't cause a "Transaction was rolled back in a different thread!"
					// but does.
					TestingJtaPlatformImpl.INSTANCE.getTransactionManager().rollback();
					log.debugf("rolled back transaction on %s", Thread.currentThread().getName());
				} catch (javax.persistence.PersistenceException e) {
					if (e.getCause() instanceof HibernateException &&
							e.getCause().getMessage().equals("Transaction was rolled back in a different thread!")) {
						log.debugf( "transaction was rolled back in a different thread from %s", Thread.currentThread().getName() );
						/**
						 * Save the exception for the main test thread to fail with
						 */
						transactionRolledBackInDifferentThreadException[0] = (HibernateException) e.getCause();
					}
					log.debugf( e, "javax.persistence.PersistenceException logged" );
				} catch (Throwable throwable) {
					log.debugf( throwable, "Throwable logged" );
					throwable.printStackTrace();
					fail("unexpected error " + throwable.getMessage());
				} finally {
					try {
						if (TestingJtaPlatformImpl.INSTANCE.getTransactionManager().getStatus() != Status.STATUS_NO_TRANSACTION)
							TestingJtaPlatformImpl.INSTANCE.getTransactionManager().suspend();
					} catch (SystemException ignore) {

					}
				}

			}
		};

		Thread thread = new Thread(run1, "test thread 1");
		thread.start();
		thread.join();
		log.debugf( "back to main thread, transactionRolledBackInDifferentThreadException[0]=%s" ,transactionRolledBackInDifferentThreadException[0]);
		// show failure for exception caught in run1.run()                                       ,
		if (transactionRolledBackInDifferentThreadException[0] != null)
		{
			// show the "Transaction was rolled back in a different thread!" error that was incorrectly thrown.
			transactionRolledBackInDifferentThreadException[0].printStackTrace();
			fail("We caught a 'Transaction was rolled back in a different thread!' error in the background test thread 1 " +
					  "which  should not happen as we rolled back from the thread that was actively using the entity manager.");
		}

		em.close();
	}
}
