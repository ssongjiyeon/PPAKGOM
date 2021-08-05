package com.ppakgom.db.entity;

import javax.persistence.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Interest extends BaseEntity{

	String name;
	
	public Interest(String n) {
		this.name = n;
	}

	public Interest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}