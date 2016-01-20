package org.hibernate.jpa.event.internal.jpa;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

/**
 * BeanManagerAfterDeploymentValidation
 *
 * @author Scott Marlow
 */
public class BeanManagerAfterDeploymentValidation implements Extension {

	public BeanManagerAfterDeploymentValidation() {
		System.out.println("xxx BeanManagerAfterDeploymentValidation ctor called");
	}
	void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
		System.out.println("xxx BeanManagerAfterDeploymentValidation.afterDeploymentValidation reached, amazing, the beanmanager=" + manager);

	}

}
