package io.github.task.vertx.api.service;

import java.util.List;

import io.github.task.vertx.api.entity.Assign;
import io.github.task.vertx.api.entity.Task;
import io.github.task.vertx.api.repository.TaskDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class TaskService {
	

	private TaskDao taskDao = TaskDao.getInstance();
	
	public void getTaskDetails(RoutingContext context, String authorization, Handler<AsyncResult<List<Task>>> handler) {
        
		Future<List<Task>> future = Future.future();
        future.setHandler(handler);
        String rolename = null;
        
        try
        {
        	rolename = getName(context, authorization, "rolename");
			
			if(rolename.equals("admin"))
			{
				List<Task> list = taskDao.getAllTasks();
				future.complete(list);
			}
			else 
	       	{
	       	    sendError("Unauthorized - Only Admin can view the Task table", context.response(),401);
	       	}   
        }
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
        }
    }
	
	public void save(RoutingContext context, String authorization, Task newTask, Handler<AsyncResult<Task>> handler) {
        
		Future<Task> future = Future.future();
        future.setHandler(handler);
        String rolename = null;
        
        try
        {
        	rolename = getName(context, authorization, "rolename");
			
			if(rolename.equals("admin"))
			{
				Task task = taskDao.getByTitle(newTask.getTitle());
				
				if(task != null) 
				{
					sendError("Task exist", context.response(),400);
				}
				else 
				{
					taskDao.persist(newTask);
					sendSuccess("Task created successfully", context.response(), 200);
					future.complete();
				}
			}
			else 
	       	{
	       	    sendError("Unauthorized - Only Admin can create the Task", context.response(),401);
	       	}   
        }
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
        }
    }
	
	public void update(RoutingContext context, String authorization, Task newTask, Handler<AsyncResult<Task>> handler) {
        Future<Task> future = Future.future();
        future.setHandler(handler);
        String rolename = null;

        try 
        {
        	rolename = getName(context, authorization, "rolename");
        	
        	if(rolename.equals("admin"))
        	{
        		Task task = taskDao.getById(newTask.getTaskid());
				
				if(task == null) 
				{
					sendError("Task doesn't exist", context.response(),400);
				}
				else
				{
	        		taskDao.update(newTask, newTask.getTaskid());
	                sendSuccess("Task updated successfully", context.response(),200);
	                future.complete();
				}
        	}
        	else 
	       	{
	       	    sendError("Unauthorized - Only Admin can update the Task", context.response(),401);
	       	}  
        	
        } 
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
        }
    }
	
	public void remove(RoutingContext context, String authorization, String taskid, Handler<AsyncResult<Task>> handler) {
        Future<Task> future = Future.future();
        future.setHandler(handler);
        String rolename = null;

        try 
        {
        	rolename = getName(context, authorization, "rolename");
        	
        	if(rolename.equals("admin"))
        	{
        		taskDao.removeById(Integer.parseInt(taskid));
                sendSuccess("Task removed successfully", context.response(),200);
                future.complete();
        	}
        	else 
	       	{
	       	    sendError("Unauthorized - Only Admin can delete the Task", context.response(),401);
	       	}  
        	
        } 
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
        }
    }

	public void assign(RoutingContext context, String authorization, Assign newTask, Handler<AsyncResult<Assign>> handler) {
        
		Future<Assign> future = Future.future();
        future.setHandler(handler);
        String rolename = null;
        
        try
        {
        	rolename = getName(context, authorization, "rolename");
			
			if(rolename.equals("admin"))
			{
				Task task = taskDao.getById(newTask.getTask().getTaskid());
				
				if(task == null) 
				{
					sendError("Task doesn't exist", context.response(),400);
				}
				else 
				{
					taskDao.assignTask(newTask);
					sendSuccess("User assigned successfully", context.response(), 200);
					future.complete();
				}
			}
			else 
	       	{
	       	    sendError("Unauthorized - Only Admin can assign the Task", context.response(),401);
	       	}   
        }
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
        }
    }
	
	public void getByName(RoutingContext context, String authorization, String name, Handler<AsyncResult<List<Assign>>> handler) {
        
		Future<List<Assign>> future = Future.future();
        future.setHandler(handler);
        String username = null, rolename = null;
        
        try
        {
        	
        	List<Assign> task = taskDao.getByName(name);
        	username = getName(context, authorization, "username");
        	rolename = getName(context, authorization, "rolename");
			
			if(task != null && (username.equals(task.get(0).getAssignto()) || rolename.equals("admin")))
			{
				future.complete(task);
			}
			else if(!username.equals(task.get(0).getAssignto()))
			{
				sendError("User mismatch", context.response(),401);
			}
			else 
	       	{
	       	    sendError("User not found", context.response(),401);
	       	}   
        }
        catch (Throwable ex) 
        {
          	sendError("Fail to fetch task details", context.response(),401);
            future.fail(ex);
        }
    }
	
	public void updateStatus(RoutingContext context,String authorization, int id, Assign newTask, Handler<AsyncResult<Assign>> handler) {
        Future<Assign> future = Future.future();
        future.setHandler(handler);
        String username = null, rolename = null;
        Assign task = null;

        try {

        	 username = getName(context, authorization, "username");
         	 rolename = getName(context, authorization, "rolename");
         	 
        	 task = taskDao.getByAssignId(id);
        	 
        	 if(task != null && ( username.equals(task.getAssignto()) || rolename.equals("admin")))
        	 {
        		 taskDao.updateStatus(task.getAssignto(), newTask.getStatus());
                 sendSuccess("Status updated successfully", context.response(),200);
                 future.complete();
        	 }
        	 else if(!username.equals(task.getAssignto()))
 			 {
 				 sendError("User mismatch", context.response(),401);
 			 }
        	 else
        	 {
 	       	     sendError("User not found", context.response(),401);
        	 }
        	
        } catch (Throwable ex) {
        	 sendError("fail to update status", context.response(),400);
            future.fail(ex);
      
        }
    }
	
	public static String getName(RoutingContext context, String value, String type) {
    	
		String name = null;
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        
        if(value.isEmpty())
        {
        	sendError("Unauthorized", context.response(),401);
        }
        
        try(Jedis jedis = jedisPool.getResource())
        {
        	String result =  jedis.get(value);
			JsonObject jsonObject = new JsonObject(result);
			
			//System.out.print(jsonObject);
			
			if(type.equals("rolename"))
			{
				name = jsonObject.getString("rolename");
			}
			else
			{
				name = jsonObject.getString("name");
			}
			jedisPool.close();
	  		
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	return name;
  		
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

    private void sendSuccess(String successMessage, HttpServerResponse response,int code) {
        JsonObject jObj = new JsonObject();
        jObj.put("successMessage", successMessage);

        response
                .setStatusCode(200)
                .setStatusCode(code)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(jObj));
    }
}
