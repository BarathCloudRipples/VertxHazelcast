package io.github.task.vertx.api.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import io.github.task.vertx.api.entity.Role;


public class RoleDao {
    private static RoleDao instance;
    protected EntityManager entityManager;

    public static RoleDao getInstance()
	{
	    if (instance == null){
	        instance = new RoleDao();
	    }
	    return instance;
	}

    private RoleDao()
    {
        entityManager = getEntityManager();
    }

    private EntityManager getEntityManager()
    {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("crudHibernatePU");
        if (entityManager == null) {
            entityManager = factory.createEntityManager();
        }
        return entityManager;
    }
    
    @SuppressWarnings("unchecked")
	public List<Role> getAllRoles() {
        return entityManager.createQuery("FROM " + Role.class.getName()).getResultList();
    }
    
    public Role getById(int roleId) {
		 Role role = null;
	    	try {
	    		List<Role> roles = entityManager.createQuery(
			    		"FROM Role WHERE role_id = :roleid", Role.class)
		          .setParameter("roleid", roleId)
		          .getResultList();
	    		
	    		role=roles.get(0);
	    		
	    	}
	    	catch(Exception ex) {
	    	ex.printStackTrace();
	    	}
	    	return role;
	}
    
    public void persist(Role role)
	{
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(role);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
}
