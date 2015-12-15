/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id.enhanced;

import org.hibernate.HibernateException;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Variation of {@link PooledOptimizer} which interprets the incoming database value as the lo value, rather than
 * the hi value, as well as using thread local to cache the generation state.
 *
 * @author Steve Ebersole
 * @author Stuart Douglas
 * @author Scott Marlow
 *
 * @see PooledOptimizer
 */
public class PooledThreadLocalLoOptimizer extends AbstractOptimizer {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			PooledThreadLocalLoOptimizer.class.getName()
	);

	private static int THREAD_LOCAL_BLOCK_SIZE = Integer.getInteger("org.hibernate.thread-local-block-size", 5000);

	private static class GenerationState {
		private GenerationState(final AccessCallback callback, final int incrementSize) {
			lastSourceValue = callback.getNextValue();
			upperLimitValue = lastSourceValue.copy().add(incrementSize);
			value = lastSourceValue.copy();
		}
		// last value read from db source
		private IntegralDataTypeHolder lastSourceValue;
		// the current generator value
		private IntegralDataTypeHolder value;
		// the value at which we'll hit the db again
		private IntegralDataTypeHolder upperLimitValue;
	}

	/**
	 * Constructs a PooledThreadLocalLoOptimizer.
	 *
	 * @param returnClass The Java type of the values to be generated
	 * @param incrementSize The increment size.
	 */
	public PooledThreadLocalLoOptimizer(Class returnClass, int incrementSize) {
		super( returnClass, incrementSize );
		if ( incrementSize < 1 ) {
			throw new HibernateException( "increment size cannot be less than 1" );
		}
		LOG.creatingPooledLoOptimizer( incrementSize, returnClass.getName() );
	}

	@Override
	public Serializable generate(AccessCallback callback) {

		GenerationState local = localAssignedIds.get();
		if ( local != null && local.value.lt( local.upperLimitValue ) ) {
			return local.value.makeValueThenAdd(incrementSize);
		}

		synchronized (this) {
			final GenerationState generationState = locateGenerationState(callback);

			if ( local == null ) {
				localAssignedIds.set( generationState );
			}
			// if we reached the upper limit value, increment to next block of sequences
			if (!generationState.value.lt(generationState.upperLimitValue)) {
				generationState.lastSourceValue = callback.getNextValue();
				// generationState.upperLimitValue = generationState.lastSourceValue.copy().add(incrementSize);
				generationState.upperLimitValue = generationState.lastSourceValue.copy().add(THREAD_LOCAL_BLOCK_SIZE);
				generationState.value = generationState.lastSourceValue.copy();
				// handle cases where initial-value is less that one (hsqldb for instance).
				while (generationState.value.lt(1)) {
					generationState.value.increment();
				}
			}
			return generationState.value.makeValueThenAdd(incrementSize);
		}
	}

	private GenerationState generationState;
	private final ThreadLocal<GenerationState> localAssignedIds = new ThreadLocal<GenerationState>();

	private GenerationState locateGenerationState(AccessCallback callback) {
		if ( generationState == null ) {
			generationState = new GenerationState(callback, THREAD_LOCAL_BLOCK_SIZE);
		}
		return generationState;
	}

	private GenerationState generationState() {
		if ( generationState == null ) {
			throw new IllegalStateException( "Could not locate previous generation state" );
		}
		return generationState;
	}

	@Override
	public IntegralDataTypeHolder getLastSourceValue() {
		return generationState().lastSourceValue;
	}

	@Override
	public boolean applyIncrementSizeToSourceValues() {
		return true;
	}
}
