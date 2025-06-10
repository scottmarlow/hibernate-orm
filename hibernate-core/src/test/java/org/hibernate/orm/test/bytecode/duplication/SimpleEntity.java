/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.bytecode.duplication;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;


/**
 * @author Steve Ebersole
 */
@Entity
@Access(AccessType.PROPERTY)
public class SimpleEntity {

	private java.util.Date id;

	private java.sql.Time timeData;

	private java.sql.Timestamp tsData;


	private String name;

	public SimpleEntity() {
	}

	public SimpleEntity(java.util.Date id, String name) {
		this.id = id;
		this.name = name;
	}

	@Id
	@Column(name = "DATATYPES2_ID")
	@Temporal(TemporalType.DATE)
	public java.util.Date getId() {
		return id;
	}

	public void setId(Date id) {
		this.id = id;
	}

	@Column(name = "TSDATA")
	public java.sql.Timestamp getTsData() {
		return tsData;
	}

public void setTsData(java.sql.Timestamp tsData) {
this.tsData = tsData;
}

}
