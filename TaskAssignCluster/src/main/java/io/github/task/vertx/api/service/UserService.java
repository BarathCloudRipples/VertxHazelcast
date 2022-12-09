package io.github.task.vertx.api.service;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import io.github.task.vertx.api.entity.User;
import io.github.task.vertx.api.repository.UserDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class UserService {
	private UserDao userDao = UserDao.getInstance();

	public void signup(RoutingContext context,User newUser, Handler<AsyncResult<User>> handler) {
		     Future<User> future = Future.future();
		     future.setHandler(handler);
		  try {
				if(newUser.getName().isEmpty() || newUser.getPassword().isEmpty()||newUser.getEmail().isEmpty()||newUser.getDesignation().isEmpty()) {
		     		 System.out.print("Please filled the manditatory Fields");
		             sendError("Please filled the manditatory Fields", context.response(),400);
		     	}
				else
				{
				  User user = userDao.getByUsername(newUser.getName());
				  if(user!=null) {
					  sendError("User exist", context.response(),400);}
				  else 
				  {
						  
				    	 String passwordRegex = "^(?=.*[0-9])"
			                     + "(?=.*[a-z])(?=.*[A-Z])"
			                     + "(?=.*[@#$%^&+=])"
			                     + "(?=\\S+$).{8,20}$";
				    	 
				    	 Pattern p = Pattern.compile(passwordRegex);
				    	 Matcher m = p.matcher(newUser.getPassword());
				    	  if(m.matches()){	
				    		  
				    		  String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" 
				    			        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";  
				    		  Pattern pattern = Pattern.compile(emailRegex);
						    	 Matcher matcher = pattern.matcher(newUser.getEmail());
						    	 if(matcher.matches()){ 
						    		  System.out.print(newUser.toJsonString());
						    		  userDao.persist(newUser);	
						    		  sendSuccess("User created successfully", context.response(),200);
						    		 
				    		 }
						    	 else {
						    		 sendError("Please give a valid Email", context.response(),400);}
						    	 
						  }else{
							  sendError("Password must have length 8 characters,one Uppercase,"
							  		+ "one special character and one digit", context.response(),400);}
						  }
			  future.complete();
			 	 } }catch (Throwable ex) {
				            future.fail(ex);
				 }
	 }
	
	
	public void validate(RoutingContext context,User newUser, Handler<AsyncResult<User>> handler) {
	     Future<User> future = Future.future();
	     future.setHandler(handler);
	     try {
	    	 
	    	 String jString = null, token = null, date = "Token expires after 24 hours";
	    	 User user = null;
	    	 
	     	if((newUser.getName().isEmpty() && newUser.getEmail().isEmpty()) || newUser.getPassword().isEmpty()) {
	     		 
	             sendError("Invalid username and password", context.response(),400);

	     	}
	     	else {
	     		
	     		if(!newUser.getEmail().isEmpty() && 
	     				newUser.getEmail().equals(userDao.getByEmail(context, newUser.getEmail()).getEmail()) && 
     						newUser.getPassword().equals(userDao.getByEmail(context, newUser.getEmail()).getPassword()))
	     		{
	     			user = userDao.getByEmail(context, newUser.getEmail());
	     			jString = getJsonAttributes(user);
	     			token = createToken(jString);
     				sendSuccess("Login successfully", date, token, context.response(),200);
	     			
	     		}
	     		
	     		else if(newUser.getName().equals(userDao.getByName(context,newUser.getName()).getName()) &&
	     				 	newUser.getPassword().equals(userDao.getByName(context,newUser.getName()).getPassword()))
	     		{
	     			user = userDao.getByName(context, newUser.getName());
	     			jString = getJsonAttributes(user);
	     			token = createToken(jString);
	     			sendSuccess("Login successfully", date, token, context.response(),200);
	     		}
	     		
	     		 else 
	     		 {
	     			sendError("Login failed", context.response(),400);
	     		 }

	     		  future.complete();
	     	   }
		   } catch (Throwable ex) {
		            future.fail(ex);
		        }
	 		}

    public void passwordUpdate(RoutingContext context,User signup, Handler<AsyncResult<User>> handler) {
        Future<User> future = Future.future();
        future.setHandler(handler);

        try {
        	 User user = userDao.getByEmail(context,signup.getEmail());
        	 
			  if(user == null)
			  {
				  sendError("Invalid email", context.response(),400);
			  }
			  else if(signup.getEmail().equals(userDao.getByEmail(context, signup.getEmail()).getEmail()))
			  {
				  String passwordRegex = "^(?=.*[0-9])"
		                     + "(?=.*[a-z])(?=.*[A-Z])"
		                     + "(?=.*[@#$%^&+=])"
		                     + "(?=\\S+$).{8,20}$";
			    	 
			    	 Pattern p = Pattern.compile(passwordRegex);
			    	 Matcher m = p.matcher(signup.getPassword());
			    	  if(m.matches()){	    		 
			    		  System.out.print("success");
				  
			userDao.forgotPassword(context,signup.getEmail(), signup.getPassword());
			sendSuccess("Password Updated", context.response(),200);
            future.complete();
			    	  }else {
			    		  sendError("Password must have length 8 characters,one Uppercase,one special character and one digit", context.response(),400);} 
			    	  }
			 
			  else {
				  sendError("Email not Exist", context.response(),400);
			  }
        } catch (Throwable ex) {
        	 sendError("fail ", context.response(),400);
            future.fail(ex);
      
        }
    }
    
    public void getUserDetails(RoutingContext context, String authorization, String token, Handler<AsyncResult<User>> handler) {
        Future<User> future = Future.future();
        future.setHandler(handler);
        
        String value=context.request().getHeader("authorization");
        
        try 
        {
        	 
        	 if(value .equals(token)) 
        	 {
		          JedisPool jedisPool = new JedisPool("localhost", 6379);
				  
		          try (Jedis jedis = jedisPool.getResource()) 
		          {
					  String result =  jedis.get(token);
					  JsonObject jsonObject = new JsonObject(result);
					  
					  System.out.print(jsonObject);
					  
					  sendMessage(token,jsonObject, context.response());
					  future.complete();
				  }
				 jedisPool.close();	
				 future.complete();
        	  } 
        	 else 
        	 {
        			 sendError("Unauthorized", context.response(),401);
        	 }    	 
         } 
         catch (Throwable ex) 
         {
           	 sendError("Unauthorized", context.response(),401);
             future.fail(ex);
         }
    }
    
public static String getJsonAttributes(User user) {
    	
		JSONObject jObj = new JSONObject();
		jObj.put("id", user.getId());
		jObj.put("firstname", user.getFirstname());
		jObj.put("lastname", user.getLastname());
		jObj.put("designation", user.getDesignation());
		jObj.put("name", user.getName());
		jObj.put("email", user.getEmail());
		jObj.put("password", user.getPassword());
		jObj.put("roleid", user.getRole().getRoleid());
		jObj.put("rolename", user.getRole().getRolename());
		
    	
    	return jObj.toString();
  		
    }

    public static String createToken(String jsonObj) {
    	
    	String token = null;
    	JedisPool jedisPool = new JedisPool("localhost", 6379);
    	try(Jedis jedis = jedisPool.getResource())
    	{
	    	UUID uuid = UUID.randomUUID(); 
			  
			token = uuid.toString();
			System.out.println(token + "\n" + jsonObj);
			
			jedis.set(token, jsonObj);
			jedis.expire(token, 24 * 60 * 60);
	  		//String value = jedis.get(token);
			jedisPool.close();
	  		
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	return token;
  		
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
     
     private void sendSuccess(String successMessage, String date, String token, HttpServerResponse response,int code) {
    	 JsonObject jObj = new JsonObject();
         jObj.put("token", token);
         jObj.put("expiryDate", date);
         jObj.put("successMessage", successMessage);
         response
                 .setStatusCode(200)
                 .setStatusCode(code)
                 .putHeader("content-type", "application/json; charset=utf-8")
                 .putHeader("Authorization", token)
                 .end(Json.encodePrettily(jObj));
     	}
     
     private void sendMessage( String token, Object object, HttpServerResponse response) {
         JsonObject jObj = new JsonObject();
         jObj.put(token, object);
         response
                 .setStatusCode(200)
                 .putHeader("content-type", "application/json; charset=utf-8")              
                 .end(Json.encodePrettily(jObj));
     	}

}
