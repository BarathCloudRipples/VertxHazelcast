package io.github.mail.vertx.api;

import java.util.HashSet;
import java.util.Set;

import com.hazelcast.config.Config;

import io.github.mail.vertx.api.service.TaskService;
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

	      router.post("/email/send/:id").handler(this::sendMail);

	      vertx.createHttpServer() // <4>
	              .requestHandler(router::accept)
	              .listen(8081, "0.0.0.0", result -> {
	                  if (result.succeeded())
	                      future.complete();
	                  else
	                      future.fail(result.cause());
	              });
	  }
	  
	  
	  TaskService taskService = new TaskService();

	  
	  private void sendMail(RoutingContext context) {
		  taskService.sendMail(context, context.request().getHeader("Authorization"), Integer.parseInt(context.request().getParam("id")), ar -> {
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

