package io.github.task.vertx.api;

import java.util.HashSet;
import java.util.Set;

import com.hazelcast.config.Config;

import io.github.task.vertx.api.entity.User;
import io.github.task.vertx.api.entity.Assign;
import io.github.task.vertx.api.entity.Role;
import io.github.task.vertx.api.entity.Task;
import io.github.task.vertx.api.service.UserService;
import io.github.task.vertx.api.service.RoleService;
import io.github.task.vertx.api.service.TaskService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Verticle extends AbstractVerticle {


	  @SuppressWarnings("deprecation")
	  @Override
	  public void start(Future<Void> future) {
	      Router router = Router.router(vertx); // <1>
	      // CORS support
	      Set<String> allowHeaders = new HashSet<>();
	      allowHeaders.add("x-requested-with");
	      allowHeaders.add("Access-Control-Allow-Origin");
	      allowHeaders.add("origin");
	      allowHeaders.add("Content-Type");
	      allowHeaders.add("accept");
	      allowHeaders.add("Authorization");
	      Set<HttpMethod> allowMethods = new HashSet<>();
	      allowMethods.add(HttpMethod.GET);
	      allowMethods.add(HttpMethod.POST);
	      allowMethods.add(HttpMethod.DELETE);
	      allowMethods.add(HttpMethod.PUT);

	      router.route().handler(CorsHandler.create("*") // <2>
	              .allowedHeaders(allowHeaders)
	              .allowedMethods(allowMethods));
	      router.route().handler(BodyHandler.create()); // <3>

	      // routes

	      router.post("/signup").handler(this::signup);
	      router.put("/forgotpassword").handler(this::passwordUpdate);
	      router.post("/login").handler(this::login);
	      
	      router.post("/role").handler(this::saveRole);
	      router.get("/role/:token").handler(this::getRole);
	      router.get("/user/:token").handler(this::getUser);

	      router.get("/task/list").handler(this::getTask);
	      router.post("/task").handler(this::saveTask);
	      router.put("/task/update").handler(this::updateTask);
	      router.delete("/task/:id").handler(this::removeTask);


	      router.post("/task/assign").handler(this::assignTask);
	      router.get("/task/list/:name").handler(this::getTaskByName);
	      router.put("/task/status/:id").handler(this::updateStatus);

	      vertx.createHttpServer() // <4>
	              .requestHandler(router::accept)
	              .listen(8080, "0.0.0.0", result -> {
	                  if (result.succeeded())
	                      future.complete();
	                  else
	                      future.fail(result.cause());
	              });
	  }
	  
	  

	  UserService userService = new UserService();
	  RoleService roleService = new RoleService();
	  TaskService taskService = new TaskService();


	  private void signup(RoutingContext context) {
		  	userService.signup(context, Json.decodeValue(context.getBodyAsString(), User.class), ar -> {
		          if (ar.succeeded()) {
		              sendSuccess(context.response());
		          } else {
		              sendError(ar.cause().getMessage(), context.response());
		          }
		      });
	  }
	  
	  private void login(RoutingContext context) {
		  	userService.validate(context, Json.decodeValue(context.getBodyAsString(), User.class), ar -> {
		          if (ar.succeeded()) {
		              sendSuccess(context.response());
		          } else {
		              sendError(ar.cause().getMessage(), context.response());
		          }
		      });
	  }
	  
	  private void passwordUpdate(RoutingContext context) {
		  userService.passwordUpdate(context, Json.decodeValue(context.getBodyAsString(), User.class), ar -> {
	            if (ar.succeeded()) {
	                sendSuccess(context.response());
	                
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  
	  
	  private void getUser(RoutingContext context) {
		  userService.getUserDetails(context, context.request().getHeader("Authorization"), context.request().getParam("token"), ar -> {
			  if (ar.succeeded()) {
	                if (ar.result() != null){
	                    sendSuccess(Json.encodePrettily(ar.result()), context.response());
	                } else {
	                    sendSuccess(context.response());
	                }
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  private void getRole(RoutingContext context) {
		  roleService.getRoleDetails(context, context.request().getHeader("Authorization"), context.request().getParam("token"), ar -> {
			  if (ar.succeeded()) {
	                if (ar.result() != null){
	                    sendSuccess(Json.encodePrettily(ar.result()), context.response());
	                } else {
	                    sendSuccess(context.response());
	                }
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  private void saveRole(RoutingContext context) {
		  roleService.saveRole(context, context.request().getHeader("Authorization"), Json.decodeValue(context.getBodyAsString(), Role.class), ar -> {
	            if (ar.succeeded()) {
	                sendSuccess(context.response());
	                
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	    
	  
	  
	  private void getTask(RoutingContext context) {
		  taskService.getTaskDetails(context, context.request().getHeader("Authorization"), ar -> {
			  if (ar.succeeded()) {
	                if (ar.result() != null){
	                    sendSuccess(Json.encodePrettily(ar.result()), context.response());
	                } else {
	                    sendSuccess(context.response());
	                }
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  private void saveTask(RoutingContext context) {
		  taskService.save(context, context.request().getHeader("Authorization"), Json.decodeValue(context.getBodyAsString(), Task.class), ar -> {
	            if (ar.succeeded()) {
	                sendSuccess(context.response());
	                
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  private void updateTask(RoutingContext context) {
		  taskService.update(context, context.request().getHeader("Authorization"), Json.decodeValue(context.getBodyAsString(), Task.class), ar -> {
	            if (ar.succeeded()) {
	                sendSuccess(context.response());
	                
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  private void removeTask(RoutingContext context) {
	        taskService.remove(context, context.request().getHeader("Authorization"), context.request().getParam("id"), ar -> {
	            if (ar.succeeded()) {
	                sendSuccess(context.response());
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }

	  private void assignTask(RoutingContext context) {
		taskService.assign(context, context.request().getHeader("Authorization"), Json.decodeValue(context.getBodyAsString(), Assign.class), ar -> {
			  if (ar.succeeded()) {
				  sendSuccess(context.response());
				  
			  } else {
				  sendError(ar.cause().getMessage(), context.response());
			  }
		  });
	}
	  
	  private void getTaskByName(RoutingContext context) {
	        taskService.getByName(context, context.request().getHeader("Authorization"), context.request().getParam("name"), ar -> {
	            if (ar.succeeded()) {
	                if (ar.result() != null){
	                    sendSuccess(Json.encodePrettily(ar.result()), context.response());
	                } else {
	                    sendSuccess(context.response());
	                }
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  private void updateStatus(RoutingContext context) {
		  taskService.updateStatus(context, context.request().getHeader("Authorization"), Integer.parseInt(context.request().getParam("id")), Json.decodeValue(context.getBodyAsString(), Assign.class), ar -> {
	            if (ar.succeeded()) {
	                sendSuccess(context.response());
	                
	            } else {
	                sendError(ar.cause().getMessage(), context.response());
	            }
	        });
	  }
	  
	  
	  
	  
	  private void sendError(String errorMessage, HttpServerResponse response) {
	      JsonObject jObj = new JsonObject();
	      jObj.put("errorMessage", errorMessage);

	      response
	              .setStatusCode(400)
	              .putHeader("content-type", "application/json; charset=utf-8")
	              .end(Json.encodePrettily(jObj));
	  }

	  private void sendSuccess(HttpServerResponse response) {
	      response
	              .setStatusCode(200)
	              .putHeader("content-type", "application/json; charset=utf-8")
	              .end();
	  }
	  
	  private void sendSuccess(String responseBody, HttpServerResponse response) {
	        response
	                .setStatusCode(200)
	                .putHeader("content-type", "application/json; charset=utf-8")
	                .end(responseBody);
	  } 


	  public static void main(String[] args){

		Config hazelcastConfig = new Config();
		ClusterManager manager = new HazelcastClusterManager(hazelcastConfig);
		VertxOptions options = new VertxOptions().setClusterManager(manager);
		Vertx.clusteredVertx(options, cluster -> {
			if (cluster.succeeded()) {
				cluster.result().deployVerticle(new Verticle(), res -> {
					if(res.succeeded()){
						System.out.println("Deployment id is: " + res.result());
					} else {
						System.out.println("Deployment failed!");
					}
				});
			} else {
				System.out.println("Cluster up failed: " + cluster.cause());
			}
		});
	 }
}

