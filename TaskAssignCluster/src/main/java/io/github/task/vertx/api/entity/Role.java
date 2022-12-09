package io.github.task.vertx.api.entity;

import io.vertx.core.json.JsonObject;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "role_details")

public class Role implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "role_id")
	private int roleid;	 
	
	@Column(name = "role_name", unique=true)
    private String rolename;
	
	public Integer getRoleid() {
        return roleid;
    }

    public void setRoleid(Integer roleid) {
        this.roleid = roleid;
    }
    
    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }
    
    public String toJsonString(){
        return String.valueOf(JsonObject.mapFrom(this));
   }

}
