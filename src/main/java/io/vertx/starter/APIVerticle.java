package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIVerticle extends AbstractVerticle {

  private HttpServer server;
  public static final String BASIC_USERNAME = "username";
  public static final String BASIC_PASSWORD = "password";

  public static final String USER = "yuuvis";
  public static final String PASSWORD = "optimalsystem";

  final List<JsonObject> pets = new ArrayList<>(Arrays.asList(
    new JsonObject().put("id", 1).put("name", "Fufi").put("tag", "ABC"),
    new JsonObject().put("id", 2).put("name", "Garfield").put("tag", "XYZ"),
    new JsonObject().put("id", 3).put("name", "Puffa")
  ));

  @Override
  public void start(Promise<Void> startPromise) {

    WebClientOptions options = new WebClientOptions()
      .setUserAgent("otto");
    options.setKeepAlive(false);
    System.out.println("start the webclient");
    WebClient client = WebClient.create(this.vertx, options);

    RouterBuilder.create(this.vertx, "openapi3.yaml")
      .onSuccess(routerBuilder -> {
        // Add routes handlers
        // tag::listPetsHandler[]
        routerBuilder.operation("Dokument_Get").handler(routingContext -> {
          List<String> params = routingContext.queryParam("eDokumentenID");
          System.out.println("entered handler by id Dokument_Get eDokumentenID: " + params.get(0));
          routingContext
            .response() // <1>
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
            .end("alles ok"); // <3>
        });
        // end::listPetsHandler[]
        // tag::createDokument_PostHandler[]
        routerBuilder.operation("Dokument_Post").handler(routingContext -> {
          System.out.println("entered handler by id Dokument_Post");
          RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY); // <1>
          JsonObject pet = params.body().getJsonObject(); // <2>
          addPet(pet);
          routingContext
            .response()
            .setStatusCode(200)
            .end(); // <3>
        });
        // end::createDokument_PostHandler[]

        // tag::Fallakte_PostHandler[]
        routerBuilder.operation("Fallakte_Post").handler(routingContext -> {
          System.out.println("entered handler by id Fallakte_Post");
          JsonObject requestBody = routingContext.getBodyAsJson();
          System.out.println("requestBody: " + requestBody);
          JsonObject yuuvisDocuments = new JsonObject();
          JsonArray yuuvisObjects = new JsonArray();
          JsonObject yuuvisObject = new JsonObject();
          JsonObject properties = new JsonObject();

          JsonObject systemObject = new JsonObject();
          systemObject.put("value","tenYuuvistest:fallakteneo");
          properties.put("system:objectTypeId",systemObject);
          //vorname, nachname
          JsonObject clienttitle = new JsonObject();
          clienttitle.put("value","montagmorgen");
          properties.put("clienttitle",clienttitle);
          //gebdatum
          JsonObject clientdescription = new JsonObject();
          clientdescription.put("value","clientdescription");
          properties.put("clientdescription",clientdescription);
          JsonObject aktenzeichen = new JsonObject();
          aktenzeichen.put("value",requestBody.getJsonObject("vorgang").getString("aktenzeichen"));
          properties.put("aktenzeichen",aktenzeichen);
          JsonObject sachbearbeiter = new JsonObject();
          sachbearbeiter.put("value",requestBody.getJsonObject("vorgang").getString("zustaendigerSachbearbeiter"));
          properties.put("sachbearbeiter",sachbearbeiter);
          JsonObject vorgangsid = new JsonObject();
          vorgangsid.put("value",requestBody.getJsonObject("vorgang").getString("id"));
          properties.put("vorgangsid",vorgangsid);
          JsonObject rechtsgebiet = new JsonObject();
          rechtsgebiet.put("value",requestBody.getJsonObject("vorgang").getString("rechtsgebiet"));
          properties.put("rechtsgebiet",rechtsgebiet);
//          antragsteller, leistungsempfaenger oder unterhaltspflichtiger
          JsonObject vorname = new JsonObject();
          vorname.put("value",requestBody.getJsonObject("antragssteller").getString("vorname"));
          properties.put("vorname",vorname);
          JsonObject nachname = new JsonObject();
          nachname.put("value",requestBody.getJsonObject("antragssteller").getString("nachname"));
          properties.put("nachname",nachname);
          JsonObject geburtsdatum = new JsonObject();
          geburtsdatum.put("value",requestBody.getJsonObject("antragssteller").getString("geburtsdatum"));
          properties.put("geburtsdatum",geburtsdatum);
          JsonObject id = new JsonObject();
          id.put("value",requestBody.getString("eAktenID"));
          properties.put("id",id);
          JsonObject archivdatum = new JsonObject();
          archivdatum.put("value",requestBody.getJsonObject("vorgang").getString("archivierenDatum"));
          properties.put("archivdatum",archivdatum);
          JsonObject loeschdatum = new JsonObject();
          loeschdatum.put("value",requestBody.getJsonObject("vorgang").getString("loeschenDatum"));
          properties.put("loeschdatum",loeschdatum);
          JsonObject archivieren = new JsonObject();
          archivieren.put("value",requestBody.getJsonObject("vorgang").getString("archivieren"));
          properties.put("archivieren",archivieren);
          JsonObject loeschen = new JsonObject();
          loeschen.put("value",requestBody.getJsonObject("vorgang").getString("loeschen"));
          properties.put("loeschen",loeschen);
          JsonObject Bemerkung = new JsonObject();
          Bemerkung.put("value",requestBody.getJsonObject("vorgang").getString("bemerkung"));
          properties.put("Bemerkung",Bemerkung);

          yuuvisObject.put("properties",properties);
          yuuvisObjects.add(yuuvisObject);
          yuuvisDocuments.put("objects",yuuvisObjects);

          client
            .post(30080, "10.211.55.4", "/api/dms/objects")
            .timeout(10000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("jwewer", "admin123")
            .sendJsonObject(yuuvisDocuments)
            .onSuccess(ar -> {
              HttpResponse<Buffer> response = ar;
              System.out.println("Received yuuvis response with status code: " + response.statusCode() +" "+ response.statusMessage());
              System.out.println("Received yuuvis response with body: " + response.bodyAsString());
              routingContext
                .response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(requestBody.encode()); // <4>
            })
            .onFailure(err -> {
              System.out.println("Something went wrong " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });


        });
        // end::Fallakte_PostHandler[]

        AuthProvider authProvider = createBasicAuthProvider();
        routerBuilder.securityHandler("httpBasicAuth", BasicAuthHandler.create(authProvider));

        // Generate the router
        // tag::routerGen[]
        Router router = routerBuilder.createRouter(); // <1>

        router.route().handler(BodyHandler.create());

        router.errorHandler(404, routingContext -> { // <2>
          JsonObject errorObject = new JsonObject() // <3>
            .put("code", 404)
            .put("message",
              (routingContext.failure() != null) ?
                routingContext.failure().getMessage() :
                "Not Found"
            );
          routingContext
            .response()
            .setStatusCode(404)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(errorObject.encode()); // <4>
        });

        router.errorHandler(401, routingContext -> {
          JsonObject errorObject = new JsonObject()
            .put("code", 401)
            .put("message",
              (routingContext.failure() != null) ?
                routingContext.failure().getMessage() :
                "Unauthorized"
            );
          routingContext
            .response()
            .setStatusCode(401)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(errorObject.encode());
        });

        router.errorHandler(400, routingContext -> {
          JsonObject errorObject = new JsonObject()
            .put("code", 400)
            .put("message",
              (routingContext.failure() != null) ?
                routingContext.failure().getMessage() :
                "Bad Request"
            );
          routingContext
            .response()
            .setStatusCode(400)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(errorObject.encode());
        });

        server = vertx.createHttpServer(new HttpServerOptions().setPort(8080)); // <5>
        server
          .requestHandler(router)
          .listen(
            (ar) -> {
              if (ar.succeeded()) {
                System.out.println("HTTP-OPENAPI3-SERVER STARTED ON PORT 8080");
                startPromise.complete();
              } else {
                System.out.println("ERROR starting HTTP-OPENAPI3-SERVER ON PORT 8080");
                startPromise.fail("start failed");
              }
            });

//        server.requestHandler(router).listen(); // <6>
//        System.out.println("SERVER listen on port 8080");
//        // end::routerGen[]
//        startPromise.complete(); // Complete the verticle start
      })
      .onFailure(startPromise::fail);
  }

  @Override
  public void stop(){
    this.server.close();
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new APIVerticle());
  }

  private AuthProvider createBasicAuthProvider() {
    AuthProvider authProvider =
      (authInfo, resultHandler) -> {
        if (authInfo.getString(BASIC_USERNAME).equals(USER)
          && authInfo.getString(BASIC_PASSWORD).equals(PASSWORD)) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture("INVALID HTTPBASICAUTH CREDENTIALS"));
        }
      };
    System.out.println("auth provider");
    return authProvider;
  }

  private List<JsonObject> getAllPets() {
    return this.pets;
  }

  private void addPet(JsonObject pet) {
    this.pets.add(pet);
  }

  // tag::loadSpecSampleMethod[]
  // For documentation purpose
  private void loadSpecSample(Promise<Void> startPromise) {
    // tag::loadSpec[]
    RouterBuilder.create(this.vertx, "openapi3.yaml")
      .onSuccess(routerBuilder -> { // <1>
        // You can start building the router using routerBuilder
      }).onFailure(cause -> { // <2>
      // Something went wrong during router factory initialization
      startPromise.fail(cause);
    });
    // end::loadSpec[]
  }
  // end::loadSpecSampleMethod[]
}
