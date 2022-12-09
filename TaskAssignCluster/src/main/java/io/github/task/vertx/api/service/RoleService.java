package io.github.task.vertx.api.service;

import java.util.List;

import io.github.task.vertx.api.entity.Role;
import io.github.task.vertx.api.repository.RoleDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RoleService {
	private RoleDao roleDao = RoleDao.getInstance();
	
	public void getRoleDetails(RoutingContext context, String authorization, String token, Handler<AsyncResult<List<Role>>> handler) {
        
		Future<List<Role>> future = Future.future();
        future.setHandler(handler);
        
        String value=context.request().getHeader("authorization");
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        
        try(Jedis jedis = jedisPool.getResource())
        {
        	String result =  jedis.get(token);
			JsonObject jsonObject = new JsonObject(result);
			
			System.out.println(jsonObject);
			
			String rolename = jsonObject.getString("rolename");
			jedisPool.close();
			System.out.println(rolename);
			
			if(value .equals(token) && rolename.equals("admin"))
			{
				List<Role> list = roleDao.getAllRoles();
				future.complete(list);
			}
			else 
	       	{
	       	    sendError("Unauthorized - Only Admin can view the Role table", context.response(),401);
	       	}   
        }
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
        }
    }
	
	public void saveRole(RoutingContext context, String authorization, Role newRole, Handler<AsyncResult<Role>> handler) {
        
		Future<Role> future = Future.future();
        future.setHandler(handler);
        
        String value = context.request().getHeader("authorization");
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        
        if(value.isEmpty())
        {
        	sendError("Unauthorized", context.response(),401);
        }
        
        try(Jedis jedis = jedisPool.getResource())
        {
        	String result =  jedis.get(value);
			JsonObject jsonObject = new JsonObject(result);
			
			System.out.print(jsonObject);
			
			String rolename = jsonObject.getString("rolename");
			jedisPool.close();
			
			if(rolename.equals("admin"))
			{
				Role role = roleDao.getById(newRole.getRoleid());
				
				if(role!=null) 
				{
					sendError("Role exist", context.response(),400);
				}
				else 
				{
					roleDao.persist(newRole);
					sendSuccess("Role created successfully", context.response(), 200);
					future.complete();
				}
			}
			else 
	       	{
	       	    sendError("Unauthorized - Only Admin can create the Role", context.response(),401);
	       	}   
        }
        catch (Throwable ex) 
        {
          	sendError("Unauthorized", context.response(),401);
            future.fail(ex);
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
