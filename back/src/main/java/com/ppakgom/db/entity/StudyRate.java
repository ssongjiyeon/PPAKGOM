package com.ppakgom.db.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(StudyRateId.class)
public class StudyRate{
   
   boolean checked;
   
   @Id
   @ManyToOne
   @JoinColumn(name = "user_id")
   User user;
   
   @Id
   @ManyToOne
   @JoinColumn(name = "study_id")
   Study study;
   
   @Id
   @ManyToOne
   @JoinColumn(name = "study_member_id")
   User studyMember;
   
   
   public StudyRate(Study study, User user, User studyMember, boolean check) {
	   this.setChecked(check);
	   this.setStudy(study);
	   this.setUser(user);
	   this.setStudyMember(studyMember);
   }
}