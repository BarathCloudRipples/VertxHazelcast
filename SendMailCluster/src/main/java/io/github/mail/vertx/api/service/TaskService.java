package io.github.mail.vertx.api.service;

import io.github.mail.vertx.api.entity.Assign;
import io.github.mail.vertx.api.repository.TaskDao;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

public class TaskService {
	

	private TaskDao taskDao = TaskDao.getInstance();

	
	public void sendMail(RoutingContext context,String authorization, int id, Handler<AsyncResult<Assign>> handler) {
        Future<Assign> future = Future.future();
        future.setHandler(handler);
        String rolename = null, fromAddress = null, toAddress = null;
        Assign task = null;

        try {

         	 rolename = getName(context, authorization, "rolename");

             fromAddress = getName(context, authorization, "email");
         	 
        	 task = taskDao.getByAssignId(id);

             toAddress = taskDao.getEmail(task.getAssignto()).getEmail();
        	 
        	 if(task != null && rolename.equals("admin"))
        	 {
        		 sendGmail(fromAddress, toAddress, task.getTask().getTitle(), task.getTask().getDescription());
                 sendSuccess("Mail send to " + task.getAssignto() + " successfully", context.response(),200);
                 future.complete();
        	 }
        	 else
        	 {
 	       	     sendError("User not found", context.response(),401);
        	 }
        	
        } catch (Throwable ex) {
        	sendError("fail to send mail", context.response(),400);
			ex.printStackTrace();
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
			
			name = jsonObject.getString(type);
			jedisPool.close();
	  		
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	return name;
  		
    }

	public static void sendGmail(String from, String to, String subject, String text) {

        Dotenv dotenv = Dotenv.load();
		final String username = dotenv.get("MY_USERNAME");
        final String password = dotenv.get("MY_PASSWORD");

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
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
