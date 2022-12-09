package io.github.task.vertx.api.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import io.github.task.vertx.api.entity.User;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public class UserDao {
    private static UserDao instance;
    protected EntityManager entityManager;

    public static UserDao getInstance()
    {
        if (instance == null){
            instance = new UserDao();
        }
        return instance;
    }

    private UserDao()
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

	 public User getByUsername(String name) {
		 User user = null;
	    	try {
	    		List<User> users = entityManager.createQuery(
			    		"FROM User WHERE username = :name", User.class)
		          .setParameter("name", name)
		          .getResultList();
	    		
	    		user=users.get(0);
	    		
	    	}
	    	catch(Exception ex) {
	    	ex.printStackTrace();
	    	}
	    	return user;
	  }

     public User getByName(RoutingContext context, String name)
	  {
	      try{Object result = entityManager.createQuery( "SELECT s FROM User s WHERE s.name LIKE :user_name")
	    	        .setParameter("user_name", name)
	    	        .getSingleResult();

	      if (result != null) {
	          return (User) result;
	      }
	      }
	      catch(NoResultException nre){

	    	  sendError("Login failed", context.response(),400);
	      }
	     return null;
	  }

      public void persist(User user)
    	{
	        try {
	            entityManager.getTransaction().begin();
	            entityManager.persist(user);
	            entityManager.getTransaction().commit();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            entityManager.getTransaction().rollback();
	        }
	    }
   
      public User getByEmail(RoutingContext context, String email) {
 		 
 	    	try {
 	    		Object result = entityManager.createQuery(
 			    		"FROM User WHERE email = :email", User.class)
 		          .setParameter("email", email)
 		          .getSingleResult();
 	    		if (result != null) {
 	 	          return (User) result;
 	 	      }
 	    	}
 	    	catch(Exception ex) {
 	    		ex.printStackTrace();
 	    	}
 	    	return null;
 	    	}
      
      public void forgotPassword(RoutingContext context,String email,String password)
  		{   
    	  try {
    		  
	            entityManager.getTransaction().begin();
	            Query update = entityManager.createQuery("UPDATE User set password='"+password+"'  WHERE email='"+email+"'");
	            update.executeUpdate();
        		entityManager.getTransaction().commit();
        		sendSuccess(" Password Updated", context.response(),200);   		 
    	  }  catch (Exception ex) {
		            ex.printStackTrace();
		            entityManager.getTransaction().rollback();
		            
		        }
	    }

      private static void sendError(String errorMessage, HttpServerResponse response,int code) {
          JsonObject jObj = new JsonObject();
          jObj.put("errorMessage", errorMessage);

          response
                  .setStatusCode(400)
                  .setStatusCode(code)
                  .putHeader("content-type", "application/json; charset=utf-8")
                  .end(Json.encodePrettily(jObj));
      }
      
      private void sendSuccess(String successMessage,HttpServerResponse response,int code) {
    	  JsonObject jObj = new JsonObject();
          jObj.put("successMessage", successMessage);
    	  response
	              .setStatusCode(200)
	              .setStatusCode(code)
	              .putHeader("content-type", "application/json; charset=utf-8")
	              .end(Json.encodePrettily(jObj));
	  }
}