package io.github.mail.vertx.api.entity;

import java.io.Serializable;

import javax.persistence.*;

import io.vertx.core.json.JsonObject;

@Entity
@Table(name = "task_assign")

public class Assign implements Serializable {
    
    @Id
	@Column(name = "assign_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_gen")
    @SequenceGenerator(name = "article_gen", allocationSize = 1, sequenceName = "task_assign_id_seq")
    private int assignid;
    
    @Column(name = "assign_to")
    private String assignto;
    
    @Column(name = "status", nullable=false)
    private String status;
	
    @Column(name = "timeline", nullable=false)
    private String timeline;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name="task_id")
    private Task task;


    public int getAssignid() {
        return this.assignid;
    }

    public void setAssignid(int assignid) {
        this.assignid = assignid;
    }

    public String getAssignto() {
        return this.assignto;
    }

    public void setAssignto(String assignto) {
        this.assignto = assignto;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeline() {
        return this.timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String toJsonString(){
        return String.valueOf(JsonObject.mapFrom(this));
    }
}
