package io.github.task.vertx.api.entity;

import org.hibernate.annotations.GenericGenerator;
import io.vertx.core.json.JsonObject;
import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "user_details")

public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "user_id")
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")	  
	private String id;
	
	@Column(name = "first_name", nullable=false)
    private String firstname;
    
    @Column(name = "last_name", nullable=false)
    private String lastname;
    
    @Column(name = "designation", nullable=false)
    private String designation;
	
    @Column(name = "username",unique = true, nullable=false)
    private String name;
    
    @Column(name = "email",unique = true, nullable=false)
    private String email;
    
    @Column(name = "password", nullable=false)
    private String password;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name="role_id")
    private Role role;
    
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    
    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    
    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
            this.password = password;
    }
       
    public String toJsonString(){
         return String.valueOf(JsonObject.mapFrom(this));
    }


}
