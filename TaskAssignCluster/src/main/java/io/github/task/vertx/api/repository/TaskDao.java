package io.github.task.vertx.api.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import io.github.task.vertx.api.entity.Assign;
import io.github.task.vertx.api.entity.Task;

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
    
    @SuppressWarnings("unchecked")
	public List<Task> getAllTasks() {
        return entityManager.createQuery("FROM " + Task.class.getName()).getResultList();
    }
    
    public Task getById(int taskid) {
		 Task task = null;
	    	try {
	    		List<Task> tasks = entityManager.createQuery(
			    		"FROM Task WHERE task_id = :taskid", Task.class)
		          .setParameter("taskid", taskid)
		          .getResultList();
	    		
	    		task=tasks.get(0);
	    		
	    		
	    	}
	    	catch(Exception ex) {
	    	ex.printStackTrace();
	    	}
	    	return task;
	}
    
    public void removeById(int taskid) {
        try {
            Task task = getById(taskid);
            System.out.println(taskid + " - " );
            if(task != null) {
            	entityManager.getTransaction().begin();
                Query update = entityManager.createQuery("DELETE FROM Task where task_id='"+taskid+"'");
                update.executeUpdate();
        		entityManager.getTransaction().commit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            entityManager.getTransaction().rollback();
        }
    }
    
    public List<Assign> getByName(String name)
	  {
	      try{
	    	  return entityManager.createQuery( "FROM Assign WHERE assign_to = :assignto", Assign.class)
	    	        .setParameter("assignto", name)
	    	        .getResultList();
	      }
	      catch(Exception ex){
	            ex.printStackTrace();
	      }
	     return null;
	}

	public Task getByTitle(String title)
	  {
	      try{
	    	  Object result = entityManager.createQuery( "FROM Task WHERE title = :title", Task.class)
	    	        .setParameter("title", title)
	    	        .getSingleResult();

	      if (result != null) {
	          return (Task) result;
	      }
	      }
	      catch(Exception ex){
	            ex.printStackTrace();
	      }
	     return null;
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
    
    public void updateStatus(String name, String status)
	{   
		  try {
		        entityManager.getTransaction().begin();
		        Query update = entityManager.createQuery("UPDATE Assign set status='" + status + "'  WHERE assign_to='" + name + "'");
		        update.executeUpdate();
				entityManager.getTransaction().commit();  		 
		  }  catch (Exception ex) {
		            ex.printStackTrace();
		            entityManager.getTransaction().rollback();
		  }
    }

	public void assignTask(Assign task)
	{   
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(task);
			entityManager.getTransaction().commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			entityManager.getTransaction().rollback();
		}
    }
	
    
    public void persist(Task task)
	{
			try {
			    entityManager.getTransaction().begin();
			    entityManager.persist(task);
			    entityManager.getTransaction().commit();
			} catch (Exception ex) {
			    ex.printStackTrace();
			    entityManager.getTransaction().rollback();
			}
    }
    
    public void update(Task task, int taskid) {
			try {
				  
			    entityManager.getTransaction().begin();
			    Query update = entityManager.createQuery("UPDATE Task set title='" + task.getTitle() + "', description='" + task.getDescription() + "'  WHERE task_id='" + taskid + "'");
			        update.executeUpdate();
					entityManager.getTransaction().commit();  		 
			  }  catch (Exception ex) {
			            ex.printStackTrace();
			            entityManager.getTransaction().rollback();
			  }
    }
}
