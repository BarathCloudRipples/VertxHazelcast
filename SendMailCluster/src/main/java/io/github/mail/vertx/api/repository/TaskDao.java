package io.github.mail.vertx.api.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import io.github.mail.vertx.api.entity.Assign;
import io.github.mail.vertx.api.entity.User;

public class TaskDao {
	
	private static TaskDao instance;
    protected EntityManager entityManager;

    public static TaskDao getInstance()
	{
	    if (instance == null){
	        instance = new TaskDao();
	    }
	    return instance;
	}

    private TaskDao()
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

	public Assign getByAssignId(int id) {
        try{
			Object result = entityManager.createQuery( "FROM Assign WHERE assign_id = :id", Assign.class)
				  .setParameter("id", id)
				  .getSingleResult();

		if (result != null) {
			return (Assign) result;
		}
		}
		catch(Exception ex){
			  ex.printStackTrace();
		}
	   return null;
    }

	public User getEmail(String name) {
        try{
			Object result = entityManager.createQuery( "FROM User WHERE username = :name", User.class)
				  .setParameter("name", name)
				  .getSingleResult();

		if (result != null) {
			return (User) result;
		}
		}
		catch(Exception ex){
			  ex.printStackTrace();
		}
	   return null;
    }
}
