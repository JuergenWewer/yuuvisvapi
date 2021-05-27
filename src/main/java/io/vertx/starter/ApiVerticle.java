package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.RequestParameters;

import java.io.*;
import java.util.*;

public class ApiVerticle extends AbstractVerticle {

  private HttpServer server;
  public static final String BASIC_USERNAME = "username";
  public static final String BASIC_PASSWORD = "password";

  public static final String USER = "yuuvis";
  public static final String PASSWORD = "optimalsystem";
  public static final String UPLOADDIR = "uploads";

  public static String yuuvisuri = null;
  public static int yuuvisport = 0;


  @Override
  public void start(Promise<Void> startPromise) {

    yuuvisuri = System.getenv("AUTHENTICATION_SERVICE_HOST");
    if (System.getenv("AUTHENTICATION_SERVICE_PORT") != null) {
      yuuvisport = Integer.valueOf(System.getenv("AUTHENTICATION_SERVICE_PORT"));
//      yuuvisport = 30080;
    }
    System.out.println("yuuvisuri: " + yuuvisuri);
    System.out.println("yuuvisport: " + yuuvisport);
    WebClientOptions options = new WebClientOptions()
      .setUserAgent("otto");
    options.setKeepAlive(false);
    System.out.println("start the webclient");
    WebClient client = WebClient.create(this.vertx, options);

    RouterBuilder.create(this.vertx, "dist/openapi3.yaml")
      .onSuccess(routerBuilder -> {
        // Add routes handlers
        System.out.println("routerbuilder succeed, build the routs");

        // tag::Dokument_Get[]
        routerBuilder.operation("Dokument_Get")
          .handler(BodyHandler.create().setUploadsDirectory(UPLOADDIR).setBodyLimit(-1L))
          .handler(routingContext -> {
          List<String> eDokumentenID = routingContext.queryParam("eDokumentenID");
          if (eDokumentenID.isEmpty()) {
            routingContext
              .response() // <1>
              .setStatusCode(204)
              .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
              .end("eDokumentenID is Empty"); // <3>
          }
          System.out.println("entered handler by id Dokument_Get eDokumentenID: " + eDokumentenID.get(0));
          JsonObject yuuvisQuery = new JsonObject();
          yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneodokument WHERE tenYuuvistest:edokumentenid = '" + eDokumentenID.get(0) + "'");
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("root", "optimalsystem")
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(sr -> {
              HttpResponse<Buffer> responseQuery = sr;
              System.out.println("Received yuuvis Query response with statuscode: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results >= 1) {
                JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                String objectId = searchProperties.getJsonObject("system:objectId").getString("value");
                System.out.println("objectId: " + objectId);

                client
                  .get(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId + "/contents/file")
                  .timeout(20000)
                  .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                  .basicAuthentication("root", "optimalsystem")
                  .send()
                  .onSuccess(ar -> {
                    HttpResponse<Buffer> responseQueryDokument = ar;
                    System.out.println("Received yuuvis Query response with status code: " + responseQueryDokument.statusCode() + " " + responseQueryDokument.statusMessage());
                    System.out.println("Received yuuvis Query response with body: " + responseQueryDokument.body());
                    routingContext
                      .response() // <1>
                      .setStatusCode(200)
                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                      .send(responseQueryDokument.body());
                  })
                  .onFailure(err -> {
                    System.out.println("Something went wrong " + err.getMessage());
                    routingContext
                      .end(err.getMessage());

                  });
              } else {
//                check if klientakte
                JsonObject yuuvisQuery2 = new JsonObject();
                yuuvisQuery2.put("statement", "SELECT * FROM tenYuuvistest:klientakteneodokument WHERE tenYuuvistest:edokumentenid = '" + eDokumentenID.get(0) + "'");
                yuuvisQuery2.put("skipCount", 0);
                yuuvisQuery2.put("maxItems", 50);
                JsonObject yuuvisQueryObject2 = new JsonObject();
                yuuvisQueryObject2.put("query", yuuvisQuery2);
                client
                  .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
                  .timeout(20000)
                  .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                  .basicAuthentication("root", "optimalsystem")
                  .sendJsonObject(yuuvisQueryObject2)
                  .onSuccess(sr2 -> {
                    HttpResponse<Buffer> responseQuery2 = sr2;
                    System.out.println("Received yuuvis Query response with status code: " + responseQuery2.statusCode() + " " + responseQuery.statusMessage());
                    System.out.println("Received yuuvis Query response with body: " + responseQuery2.bodyAsString());
                    JsonObject yuuvisSearch2 = new JsonObject(responseQuery2.bodyAsString());
                    int results2 = yuuvisSearch2.getInteger("numItems");
                    if (responseQuery2.statusCode() == 200 && results2 >= 1) {
                      JsonArray yuuvisSearchObjects2 = yuuvisSearch2.getJsonArray("objects");
                      JsonObject yuuvisSearchObject2 = yuuvisSearchObjects2.getJsonObject(0);
                      JsonObject searchProperties2 = yuuvisSearchObject2.getJsonObject("properties");
                      String objectId2 = searchProperties2.getJsonObject("system:objectId").getString("value");
                      System.out.println("objectId: " + objectId2);

                      client
                        .get(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId2 + "/contents/file")
                        .timeout(20000)
                        .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                        .basicAuthentication("root", "optimalsystem")
                        .send()
                        .onSuccess(ar2 -> {
                          HttpResponse<Buffer> responseQueryDokument2 = ar2;
                          System.out.println("Received yuuvis Query response with status code: " + responseQueryDokument2.statusCode() + " " + responseQueryDokument2.statusMessage());
                          System.out.println("Received yuuvis Query response with body: " + responseQueryDokument2.body());
                          routingContext
                            .response() // <1>
                            .setStatusCode(200)
                            .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                            .send(responseQueryDokument2.body());
                        })
                        .onFailure(err -> {
                          System.out.println("Something went wrong " + err.getMessage());
                          routingContext
                            .end(err.getMessage());

                        });
                    } else {
                      routingContext
                        .response() // <1>
                        .setStatusCode(204)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                        .end("Dokument mit der eDokumentenID existiert nicht, oder es wurde noch keine Dokumentdatei angeheftet."); // <3>
                    }
                  })
                  .onFailure(err -> {
                    System.out.println("Something went wrong " + err.getMessage());
                    routingContext
                      .end(err.getMessage());
                  });
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong " + err.getMessage());
              routingContext
                .end(err.getMessage());

            });

        });
        // end::Dokument_Get[]

        // tag::Dokument_Post[]
        routerBuilder.operation("Dokument_Post")
          .handler(BodyHandler.create().setUploadsDirectory(UPLOADDIR).setBodyLimit(-1L))
          .handler(routingContext -> {
          System.out.println("entered handler by id Dokument_Post");

          String nameValue="";
          String fileNameValue ="";
          String filePathValue ="";
          String fileTypeValue="";

//          routingContext.response().setChunked(true);
//          JsonObject requestBodyValue = null;
          for (FileUpload f : routingContext.fileUploads()) {
            System.out.println("Filename: " + f.fileName());
            System.out.println("Size: " + f.size());
            if (f.name().equals("File")) {
              nameValue = f.name();
              fileNameValue = f.fileName();
              filePathValue = f.uploadedFileName();
              fileTypeValue = f.contentType();
            } else {
              System.out.println("Unkonwn Upload Element: " + f.name());
              routingContext
                .response()
                .setStatusCode(204)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end("Unkonwn Upload Element: " + f.name()); // <4>
            }
          }
          if (routingContext.fileUploads().size() != 1) {
            System.out.println("To much/less elements in upload, should be 2 but is: " + routingContext.fileUploads().size());
            routingContext
              .response()
              .setStatusCode(204)
              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
              .end("To much/less elements in upload, should be 2 but is: " + routingContext.fileUploads().size()); // <4>
          }
          final String name=nameValue;
          final String fileName = fileNameValue;
          final String filePath = filePathValue;
          final String fileType=fileTypeValue;

          RequestParameters params = routingContext.get("parsedParameters"); // (1)
          JsonObject data = params.body().getJsonObject();
          JsonObject requestBody = new JsonObject(data.getString("data"));
          System.out.println("requestBody: " + requestBody);

          //check if document for fallakte or klientakte
          final boolean fallakte;
          if (!requestBody.getString("Klientakte.Klient.ID").isEmpty()) {
            fallakte = false;
          } else {
            fallakte = true;
          }
          JsonObject yuuvisQuery = new JsonObject();
          if (fallakte) {
            if (requestBody.getString("Fallakte.Vorgang.Register").isEmpty()) {
              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'");
            } else {
              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'" +
                " AND tenYuuvistest:register = '" + requestBody.getString("Fallakte.Vorgang.Register") + "'");
            }
          } else {
            yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:klientakteneo WHERE tenYuuvistest:id = '" + requestBody.getString("Klientakte.Klient.ID") + "'");
          }
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("root", "optimalsystem")
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(ar -> {
              HttpResponse<Buffer> responseQuery = ar;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              if (responseQuery.statusCode() == 200) {
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                int yuuvisSearchIndex = -1;
                if (results == 1) {
                  yuuvisSearchIndex = 0;
                } else if (results >= 1 && requestBody.getString("Fallakte.Vorgang.Register").isEmpty()) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  for (int i = 0; i < results; i++) {
                    JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(i);
                    JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                    if (searchProperties.getString("tenYuuvistest:register") == null) {
                      yuuvisSearchIndex = i;
                    }
                  }
                }

                if (yuuvisSearchIndex >= 0) {
                  JsonObject yuuvisDocuments;
                  if (fallakte) {
                    yuuvisDocuments = getFallakteDokument(name, fileName, fileType, requestBody, yuuvisSearch, yuuvisSearchIndex);
                  } else {
                    yuuvisDocuments = getKlientakteDokument(name, fileName, fileType, requestBody, yuuvisSearch, yuuvisSearchIndex);
                  }


                  final String nameJson = "data";
                  final String fileNameJson = "document";
                  UUID uuid = UUID.randomUUID();
                  String uuidAsString = uuid.toString();

                  final String filePathJson = "file-" + UPLOADDIR + "/" + uuid.toString();
                  final String fileTypeJson = "application/json";

                  try (FileWriter file = new FileWriter(filePathJson)) {
                    file.write(yuuvisDocuments.toString());
                    file.flush();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }

                  MultipartForm form = MultipartForm.create()
//                  .attribute("data", yuuvisDocuments.encode())
                    .textFileUpload(nameJson, fileNameJson, filePathJson, fileTypeJson)
                    .binaryFileUpload(name, fileName, filePath, fileType);
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects")
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                    .putHeader("content-type", "multipart/form-data")
                    .basicAuthentication("root", "optimalsystem")
                    .sendMultipartForm(form)
//                  .sendJsonObject(yuuvisDocuments)
                    .onSuccess(arDocument -> {
                      HttpResponse<Buffer> responseDocument = arDocument;
                      System.out.println("Received yuuvis response with status code: " + responseDocument.statusCode() + " " + responseDocument.statusMessage());
                      System.out.println("Received yuuvis response with body: " + responseDocument.bodyAsString());
                      //delete the uploaded file
                      for (FileUpload f : routingContext.fileUploads()) {
                        File myObj = new File(f.uploadedFileName());
                        if (myObj.delete()) {
                          System.out.println("Deleted the file: " + myObj.getName());
                        } else {
                          System.out.println("Failed to delete the file.");
                        }
                      }
                      File myObj = new File(filePathJson);
                      if (myObj.delete()) {
                        System.out.println("Deleted the file: " + myObj.getName());
                      } else {
                        System.out.println("Failed to delete the file.");
                      }

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

                } else {
                  routingContext
                    .response()
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end("Fallakte mit der Fallakte.Vorgang.ID existiert nicht"); // <4>
                }
              } else {
                routingContext
                  .response()
                  .setStatusCode(204)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                  .end("Yuuvisabfrage fehlgeschlagen: status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage() ); // <4>
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong during the query: " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });

//          RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY); // <1>
        });
        // end::Dokument_Post[]

        // tag::Dokument_GetDokumente[]
        routerBuilder.operation("Dokument_GetDokumente")
          .handler(routingContext -> {
            List<String> klientID = routingContext.queryParam("klientID");
            List<String> vorgangID = routingContext.queryParam("vorgangID");
            List<String> vorgangRegister = routingContext.queryParam("vorgangRegister");
            final boolean fallakte;
            if (!klientID.isEmpty()) {
              fallakte = false;
              System.out.println("entered handler by id Dokument_GetDokumente" +
              " klientID: " +klientID.get(0));
            } else {
              fallakte = true;
              if (vorgangRegister.isEmpty()) {
                System.out.println("entered handler by id Dokument_GetDokumente" +
                  " vorgangID: " + vorgangID.get(0));
              } else {
                System.out.println("entered handler by id Dokument_GetDokumente" +
                  " vorgangID: " + vorgangID.get(0) + " vorgangRegister: " + vorgangRegister.get(0));
              }
            }

            JsonObject yuuvisQuery = new JsonObject();
            if (fallakte) {
              if (vorgangRegister.isEmpty()) {
                yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + vorgangID.get(0) + "'");
              } else {
                yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + vorgangID.get(0) + "'" +
                  " AND tenYuuvistest:register = '" + vorgangRegister.get(0) + "'");
              }
            } else {
              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:klientakteneo WHERE tenYuuvistest:id = '" + klientID.get(0) + "'");
            }
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", "yuuvistest")
              .basicAuthentication("root", "optimalsystem")
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results >= 1) {
                  routingContext
                    .response()
                    .setStatusCode(200)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(yuuvisSearch.encode());
                } else {
                  routingContext
                    .response() // <1>
                    .setStatusCode(409)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("Die VorgangsID oder das Register/der Unterordner existiert im enaio® nicht."); // <3>
                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong " + err.getMessage());
                routingContext
                  .end(err.getMessage());

              });
          });
        // end::Dokument_GetDokumente[]

        // tag::Fallakte_Post[]
        routerBuilder.operation("Fallakte_Post")
          .handler(routingContext -> {
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
          clienttitle.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("vorname")+", "+requestBody.getJsonObject("leistungsempfaenger").getString("nachname"));
          properties.put("clienttitle",clienttitle);
          //gebdatum
          JsonObject clientdescription = new JsonObject();
          clientdescription.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("geburtsdatum"));
          properties.put("clientdescription",clientdescription);

          JsonObject aktenzeichen = new JsonObject();
          aktenzeichen.put("value",requestBody.getJsonObject("vorgang").getString("aktenzeichen"));
          properties.put("aktenzeichen",aktenzeichen);
          JsonObject eaktenid = new JsonObject();
          eaktenid.put("value",requestBody.getString("eAktenID"));
          properties.put("eaktenid",eaktenid);
          JsonObject sachbearbeiter = new JsonObject();
          sachbearbeiter.put("value",requestBody.getJsonObject("vorgang").getString("zustaendigerSachbearbeiter"));
          properties.put("sachbearbeiter",sachbearbeiter);
          JsonObject vorgangsid = new JsonObject();
          vorgangsid.put("value",requestBody.getJsonObject("vorgang").getString("id"));
          properties.put("vorgangsid",vorgangsid);
          if (!requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
            JsonObject register = new JsonObject();
            register.put("value", requestBody.getJsonObject("vorgang").getString("register"));
            properties.put("register", register);
          }
          JsonObject rechtsgebiet = new JsonObject();
          rechtsgebiet.put("value",requestBody.getJsonObject("vorgang").getString("rechtsgebiet"));
          properties.put("rechtsgebiet",rechtsgebiet);
          JsonObject vorname = new JsonObject();
          vorname.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("vorname"));
          properties.put("vorname",vorname);
          JsonObject nachname = new JsonObject();
          nachname.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("nachname"));
          properties.put("nachname",nachname);
          JsonObject geburtsdatum = new JsonObject();
          geburtsdatum.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("geburtsdatum"));
          properties.put("geburtsdatum",geburtsdatum);
          JsonObject id = new JsonObject();
          id.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("id"));
          properties.put("id",id);
          JsonObject vornameAntragsteller = new JsonObject();
          vornameAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("vorname"));
          properties.put("vornameAntragsteller",vornameAntragsteller);
          JsonObject nachnameAntragsteller = new JsonObject();
          nachnameAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("nachname"));
          properties.put("nachnameAntragsteller",nachnameAntragsteller);
          JsonObject geburtsdatumAntragsteller = new JsonObject();
          geburtsdatumAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("geburtsdatum"));
          properties.put("geburtsdatumAntragsteller",geburtsdatumAntragsteller);
          JsonObject idAntragsteller = new JsonObject();
          idAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("id"));
          properties.put("idAntragsteller",idAntragsteller);
          JsonObject vornameUnterhaltspflichtiger = new JsonObject();
          vornameUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("vorname"));
          properties.put("vornameUnterhaltspflichtiger",vornameUnterhaltspflichtiger);
          JsonObject nachnameUnterhaltspflichtiger = new JsonObject();
          nachnameUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("nachname"));
          properties.put("nachnameUnterhaltspflichtiger",nachnameUnterhaltspflichtiger);
          JsonObject geburtsdatumUnterhaltspflichtiger = new JsonObject();
          geburtsdatumUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("geburtsdatum"));
          properties.put("geburtsdatumUnterhaltspflichtiger",geburtsdatumUnterhaltspflichtiger);
          JsonObject idUnterhaltspflichtiger = new JsonObject();
          idUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("id"));
          properties.put("idUnterhaltspflichtiger",idUnterhaltspflichtiger);
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

          JsonObject yuuvisQuery = new JsonObject();
          if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
            yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'");
          } else {
            yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
              " AND tenYuuvistest:register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'");
          }
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("root", "optimalsystem")
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(sr -> {
              HttpResponse<Buffer> responseQuery = sr;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results == 0) {
                client
                  .post(yuuvisport, yuuvisuri, "/api/dms/objects")
                  .timeout(10000)
                  .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                  .basicAuthentication("root", "optimalsystem")
                  .sendJsonObject(yuuvisDocuments)
                  .onSuccess(ar -> {
                    HttpResponse<Buffer> response = ar;
                    System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
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
              } else {
                String answer ="";
                if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
                  answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'";
                } else {
                  answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
                    " AND register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'";
                }

                routingContext
                  .response() // <1>
                  .setStatusCode(409)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                  .end("fallakte allready exists: " + answer); // <3>
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });
        });
        // end::Fallakte_Post[]

        // tag::Fallakte_Put[]
        routerBuilder.operation("Fallakte_Put")
          .handler(routingContext -> {
            System.out.println("entered handler by id Fallakte_Put");
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
            clienttitle.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("vorname")+", "+requestBody.getJsonObject("leistungsempfaenger").getString("nachname"));
            properties.put("clienttitle",clienttitle);
            //gebdatum
            JsonObject clientdescription = new JsonObject();
            clientdescription.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("geburtsdatum"));
            properties.put("clientdescription",clientdescription);

            JsonObject aktenzeichen = new JsonObject();
            aktenzeichen.put("value",requestBody.getJsonObject("vorgang").getString("aktenzeichen"));
            properties.put("aktenzeichen",aktenzeichen);
            JsonObject eaktenid = new JsonObject();
            eaktenid.put("value",requestBody.getString("eAktenID"));
            properties.put("eaktenid",eaktenid);
            JsonObject sachbearbeiter = new JsonObject();
            sachbearbeiter.put("value",requestBody.getJsonObject("vorgang").getString("zustaendigerSachbearbeiter"));
            properties.put("sachbearbeiter",sachbearbeiter);
            JsonObject vorgangsid = new JsonObject();
            vorgangsid.put("value",requestBody.getJsonObject("vorgang").getString("id"));
            properties.put("vorgangsid",vorgangsid);
            if (!requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
              JsonObject register = new JsonObject();
              register.put("value", requestBody.getJsonObject("vorgang").getString("register"));
              properties.put("register", register);
            }
            JsonObject rechtsgebiet = new JsonObject();
            rechtsgebiet.put("value",requestBody.getJsonObject("vorgang").getString("rechtsgebiet"));
            properties.put("rechtsgebiet",rechtsgebiet);
            JsonObject vorname = new JsonObject();
            vorname.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("vorname"));
            properties.put("vorname",vorname);
            JsonObject nachname = new JsonObject();
            nachname.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("nachname"));
            properties.put("nachname",nachname);
            JsonObject geburtsdatum = new JsonObject();
            geburtsdatum.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("geburtsdatum"));
            properties.put("geburtsdatum",geburtsdatum);
            JsonObject id = new JsonObject();
            id.put("value",requestBody.getJsonObject("leistungsempfaenger").getString("id"));
            properties.put("id",id);
            JsonObject vornameAntragsteller = new JsonObject();
            vornameAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("vorname"));
            properties.put("vornameAntragsteller",vornameAntragsteller);
            JsonObject nachnameAntragsteller = new JsonObject();
            nachnameAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("nachname"));
            properties.put("nachnameAntragsteller",nachnameAntragsteller);
            JsonObject geburtsdatumAntragsteller = new JsonObject();
            geburtsdatumAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("geburtsdatum"));
            properties.put("geburtsdatumAntragsteller",geburtsdatumAntragsteller);
            JsonObject idAntragsteller = new JsonObject();
            idAntragsteller.put("value",requestBody.getJsonObject("antragssteller").getString("id"));
            properties.put("idAntragsteller",idAntragsteller);
            JsonObject vornameUnterhaltspflichtiger = new JsonObject();
            vornameUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("vorname"));
            properties.put("vornameUnterhaltspflichtiger",vornameUnterhaltspflichtiger);
            JsonObject nachnameUnterhaltspflichtiger = new JsonObject();
            nachnameUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("nachname"));
            properties.put("nachnameUnterhaltspflichtiger",nachnameUnterhaltspflichtiger);
            JsonObject geburtsdatumUnterhaltspflichtiger = new JsonObject();
            geburtsdatumUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("geburtsdatum"));
            properties.put("geburtsdatumUnterhaltspflichtiger",geburtsdatumUnterhaltspflichtiger);
            JsonObject idUnterhaltspflichtiger = new JsonObject();
            idUnterhaltspflichtiger.put("value",requestBody.getJsonObject("unterhaltspflichtiger").getString("id"));
            properties.put("idUnterhaltspflichtiger",idUnterhaltspflichtiger);
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

            JsonObject yuuvisQuery = new JsonObject();
            if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'");
            } else {
              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
                " AND tenYuuvistest:register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'");
            }
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", "yuuvistest")
              .basicAuthentication("root", "optimalsystem")
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results >= 1) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                  JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                  String objectId = searchProperties.getJsonObject("system:objectId").getString("value");
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId)
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                    .basicAuthentication("root", "optimalsystem")
                    .sendJsonObject(yuuvisDocuments)
                    .onSuccess(ar -> {
                      HttpResponse<Buffer> response = ar;
                      System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
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
                } else {
                  String answer ="";
                  if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
                    answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'";
                  } else {
                    answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
                      " AND register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'";
                  }
                  routingContext
                    .response() // <1>
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("fallakte not found: " + answer); // <3>
                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong " + err.getMessage());
                routingContext
                  .end(err.getMessage()); // <4>
              });
          });
        // end::Fallakte_Put[]

        // tag::Fallakte_Get[]
        routerBuilder.operation("Fallakte_Get")
          .handler(routingContext -> {
          String param = routingContext.pathParam("vorgangsId");
          System.out.println("entered handler by id Fallakte_Get vorgangsId: " + param);
          JsonObject yuuvisQuery = new JsonObject();
          yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + param + "'");
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("root", "optimalsystem")
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(ar -> {
              HttpResponse<Buffer> responseQuery = ar;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results >= 1) {
                JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");

//                JsonObject klientAkte = new JsonObject();
//                klientAkte.put("eAktenID","");
//                JsonObject klient = new JsonObject();
//                klient.put("vorname",searchProperties.getJsonObject("tenYuuvistest:vorname").getString("value"));
//                klient.put("nachname",searchProperties.getJsonObject("tenYuuvistest:nachname").getString("value"));
//                klient.put("id",searchProperties.getJsonObject("tenYuuvistest:id").getString("value"));
//                klient.put("geburtsdatum",searchProperties.getJsonObject("tenYuuvistest:geburtsdatum").getString("value"));
//                klient.put("adresse",searchProperties.getJsonObject("tenYuuvistest:adresse").getString("value"));
//                klientAkte.put("klient", klient);
//                klientAkte.put("archivieren",searchProperties.getJsonObject("tenYuuvistest:archivieren").getString("value"));
//                klientAkte.put("archivierenDatum",searchProperties.getJsonObject("tenYuuvistest:archivdatum").getString("value"));
//                klientAkte.put("loeschen",searchProperties.getJsonObject("tenYuuvistest:loeschen").getString("value"));
//                klientAkte.put("loeschenDatum",searchProperties.getJsonObject("tenYuuvistest:loeschdatum").getString("value"));

                JsonObject fallAkte = new JsonObject();
                fallAkte.put("eAktenID","");
                JsonObject vorgang = new JsonObject();
                vorgang.put("aktenzeichen",searchProperties.getJsonObject("tenYuuvistest:aktenzeichen").getString("value"));
                vorgang.put("archivieren",searchProperties.getJsonObject("tenYuuvistest:archivieren").getString("value"));
                vorgang.put("archivierenDatum",searchProperties.getJsonObject("tenYuuvistest:archivdatum").getString("value"));
                vorgang.put("bemerkung",searchProperties.getJsonObject("tenYuuvistest:Bemerkung").getString("value"));
                vorgang.put("id",searchProperties.getJsonObject("tenYuuvistest:vorgangsid").getString("value"));
                vorgang.put("loeschen",searchProperties.getJsonObject("tenYuuvistest:loeschen").getString("value"));
                vorgang.put("loeschenDatum",searchProperties.getJsonObject("tenYuuvistest:loeschdatum").getString("value"));
                vorgang.put("rechtsgebiet",searchProperties.getJsonObject("tenYuuvistest:rechtsgebiet").getString("value"));
                vorgang.put("register",searchProperties.getJsonObject("tenYuuvistest:register").getString("value"));
                vorgang.put("zustaendigerSachbearbeiter",searchProperties.getJsonObject("tenYuuvistest:sachbearbeiter").getString("value"));
                fallAkte.put("vorgang", vorgang);
                JsonObject personBaseExtended1 = new JsonObject();
                personBaseExtended1.put("vorname",searchProperties.getJsonObject("tenYuuvistest:vornameAntragsteller").getString("value"));
                personBaseExtended1.put("nachname",searchProperties.getJsonObject("tenYuuvistest:nachnameAntragsteller").getString("value"));
                personBaseExtended1.put("id",searchProperties.getJsonObject("tenYuuvistest:idAntragsteller").getString("value"));
                personBaseExtended1.put("geburtsdatum",searchProperties.getJsonObject("tenYuuvistest:geburtsdatumAntragsteller").getString("value"));
                fallAkte.put("antragssteller", personBaseExtended1);
                JsonObject personBaseExtended2 = new JsonObject();
                personBaseExtended2.put("vorname",searchProperties.getJsonObject("tenYuuvistest:vorname").getString("value"));
                personBaseExtended2.put("nachname",searchProperties.getJsonObject("tenYuuvistest:nachname").getString("value"));
                personBaseExtended2.put("id",searchProperties.getJsonObject("tenYuuvistest:id").getString("value"));
                personBaseExtended2.put("geburtsdatum",searchProperties.getJsonObject("tenYuuvistest:geburtsdatum").getString("value"));
                fallAkte.put("leistungsempfaenger", personBaseExtended2);
                JsonObject personBaseExtended3 = new JsonObject();
                personBaseExtended3.put("vorname",searchProperties.getJsonObject("tenYuuvistest:vornameUnterhaltspflichtiger").getString("value"));
                personBaseExtended3.put("nachname",searchProperties.getJsonObject("tenYuuvistest:nachnameUnterhaltspflichtiger").getString("value"));
                personBaseExtended3.put("id",searchProperties.getJsonObject("tenYuuvistest:idUnterhaltspflichtiger").getString("value"));
                personBaseExtended3.put("geburtsdatum",searchProperties.getJsonObject("tenYuuvistest:geburtsdatumUnterhaltspflichtiger").getString("value"));
                fallAkte.put("unterhaltspflichtiger", personBaseExtended3);

                routingContext
                  .response() // <1>
                  .setStatusCode(200)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                  .end(fallAkte.toString()); // <3>
              } else {
                routingContext
                  .response() // <1>
                  .setStatusCode(204)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                  .end("search result not unique found: " + results); // <3>
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong during the query: " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });

        });
        // end::Fallakte_Get[]

        // tag::Klientakte_Post[]
        routerBuilder.operation("Klientakte_Post")
          .handler(routingContext -> {
            System.out.println("entered handler by id Klientakte_Post");
            JsonObject requestBody = routingContext.getBodyAsJson();
            System.out.println("requestBody: " + requestBody);
            JsonObject yuuvisDocuments = new JsonObject();
            JsonArray yuuvisObjects = new JsonArray();
            JsonObject yuuvisObject = new JsonObject();
            JsonObject properties = new JsonObject();

            JsonObject systemObject = new JsonObject();
            systemObject.put("value","tenYuuvistest:klientakteneo");
            properties.put("system:objectTypeId",systemObject);
            //vorname, nachname
            JsonObject clienttitle = new JsonObject();
            clienttitle.put("value",requestBody.getJsonObject("klient").getString("vorname")+", "+requestBody.getJsonObject("klient").getString("nachname"));
            properties.put("clienttitle",clienttitle);
            //gebdatum
            JsonObject clientdescription = new JsonObject();
            clientdescription.put("value",requestBody.getJsonObject("klient").getString("geburtsdatum"));
            properties.put("clientdescription",clientdescription);

            JsonObject vorname = new JsonObject();
            vorname.put("value",requestBody.getJsonObject("klient").getString("vorname"));
            properties.put("vorname",vorname);
            JsonObject nachname = new JsonObject();
            nachname.put("value",requestBody.getJsonObject("klient").getString("nachname"));
            properties.put("nachname",nachname);
            JsonObject id = new JsonObject();
            id.put("value",requestBody.getJsonObject("klient").getString("id"));
            properties.put("id",id);
            JsonObject geburtsdatum = new JsonObject();
            geburtsdatum.put("value",requestBody.getJsonObject("klient").getString("geburtsdatum"));
            properties.put("geburtsdatum",geburtsdatum);
            JsonObject adresse = new JsonObject();
            adresse.put("value",requestBody.getJsonObject("klient").getString("adresse"));
            properties.put("adresse",adresse);
            JsonObject vornameUnterhaltspflichtiger = new JsonObject();
            vornameUnterhaltspflichtiger.put("value","???");
            properties.put("vornameUnterhaltspflichtiger",vornameUnterhaltspflichtiger);
            JsonObject archivdatum = new JsonObject();
            archivdatum.put("value",requestBody.getString("archivierenDatum"));
            properties.put("archivdatum",archivdatum);
            JsonObject loeschdatum = new JsonObject();
            loeschdatum.put("value",requestBody.getString("loeschenDatum"));
            properties.put("loeschdatum",loeschdatum);
            JsonObject archivieren = new JsonObject();
            archivieren.put("value",requestBody.getString("archivieren"));
            properties.put("archivieren",archivieren);
            JsonObject loeschen = new JsonObject();
            loeschen.put("value",requestBody.getString("loeschen"));
            properties.put("loeschen",loeschen);

            yuuvisObject.put("properties",properties);
            yuuvisObjects.add(yuuvisObject);
            yuuvisDocuments.put("objects",yuuvisObjects);

            JsonObject yuuvisQuery = new JsonObject();
            yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:klientakteneo WHERE tenYuuvistest:id = '" + requestBody.getJsonObject("klient").getString("id") + "'");
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", "yuuvistest")
              .basicAuthentication("root", "optimalsystem")
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results == 0) {
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects")
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                    .basicAuthentication("root", "optimalsystem")
                    .sendJsonObject(yuuvisDocuments)
                    .onSuccess(ar -> {
                      HttpResponse<Buffer> response = ar;
                      System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
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
                        .end(err.getMessage());
                    });
                } else {
                  String answer = " id = '" + requestBody.getJsonObject("klient").getString("id") + "'";
                  routingContext
                    .response() // <1>
                    .setStatusCode(409)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("klientakte allready exists for: " + answer); // <3>
                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong " + err.getMessage());
                routingContext
                  .end(err.getMessage());

              });
          });
        // end::Klientakte_Post[]

        // tag::Klientakte_Put[]
        routerBuilder.operation("Klientakte_Put")
          .handler(routingContext -> {
            System.out.println("entered handler by id Klientakte_Put");
            JsonObject requestBody = routingContext.getBodyAsJson();
            System.out.println("requestBody: " + requestBody);
            JsonObject yuuvisDocuments = new JsonObject();
            JsonArray yuuvisObjects = new JsonArray();
            JsonObject yuuvisObject = new JsonObject();
            JsonObject properties = new JsonObject();

            JsonObject systemObject = new JsonObject();
            systemObject.put("value","tenYuuvistest:klientakteneo");
            properties.put("system:objectTypeId",systemObject);
            //vorname, nachname
            JsonObject clienttitle = new JsonObject();
            clienttitle.put("value",requestBody.getJsonObject("klient").getString("vorname")+", "+requestBody.getJsonObject("klient").getString("nachname"));
            properties.put("clienttitle",clienttitle);
            //gebdatum
            JsonObject clientdescription = new JsonObject();
            clientdescription.put("value",requestBody.getJsonObject("klient").getString("geburtsdatum"));
            properties.put("clientdescription",clientdescription);

            JsonObject vorname = new JsonObject();
            vorname.put("value",requestBody.getJsonObject("klient").getString("vorname"));
            properties.put("vorname",vorname);
            JsonObject nachname = new JsonObject();
            nachname.put("value",requestBody.getJsonObject("klient").getString("nachname"));
            properties.put("nachname",nachname);
            JsonObject id = new JsonObject();
            id.put("value",requestBody.getJsonObject("klient").getString("id"));
            properties.put("id",id);
            JsonObject geburtsdatum = new JsonObject();
            geburtsdatum.put("value",requestBody.getJsonObject("klient").getString("geburtsdatum"));
            properties.put("geburtsdatum",geburtsdatum);
            JsonObject adresse = new JsonObject();
            adresse.put("value",requestBody.getJsonObject("klient").getString("adresse"));
            properties.put("adresse",adresse);
            JsonObject vornameUnterhaltspflichtiger = new JsonObject();
            vornameUnterhaltspflichtiger.put("value","Fehlt bei Swaggerdaten");
            properties.put("vornameUnterhaltspflichtiger",vornameUnterhaltspflichtiger);
            JsonObject archivdatum = new JsonObject();
            archivdatum.put("value",requestBody.getString("archivierenDatum"));
            properties.put("archivdatum",archivdatum);
            JsonObject loeschdatum = new JsonObject();
            loeschdatum.put("value",requestBody.getString("loeschenDatum"));
            properties.put("loeschdatum",loeschdatum);
            JsonObject archivieren = new JsonObject();
            archivieren.put("value",requestBody.getString("archivieren"));
            properties.put("archivieren",archivieren);
            JsonObject loeschen = new JsonObject();
            loeschen.put("value",requestBody.getString("loeschen"));
            properties.put("loeschen",loeschen);

            yuuvisObject.put("properties",properties);
            yuuvisObjects.add(yuuvisObject);
            yuuvisDocuments.put("objects",yuuvisObjects);

            String param = requestBody.getJsonObject("klient").getString("id");
            System.out.println("entered handler by id Dokument_Get eDokumentenID: " + param);
            JsonObject yuuvisQuery = new JsonObject();
            yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:klientakteneo WHERE tenYuuvistest:id = '" + param + "'");
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);


            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", "yuuvistest")
              .basicAuthentication("root", "optimalsystem")
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results >= 1) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                  JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                  String objectId = searchProperties.getJsonObject("system:objectId").getString("value");
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId)
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", "yuuvistest")
                    .basicAuthentication("root", "optimalsystem")
                    .sendJsonObject(yuuvisDocuments)
                    .onSuccess(ar -> {
                      HttpResponse<Buffer> response = ar;
                      System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
                      System.out.println("Received yuuvis response with body: " + response.bodyAsString());
                      routingContext
                        .response()
                        .setStatusCode(200)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(response.bodyAsString()); // <4>
                    })
                    .onFailure(err -> {
                      System.out.println("Something went wrong " + err.getMessage());
                      routingContext
                        .end(err.getMessage()); // <4>
                    });
                } else {
                  routingContext
                    .response() // <1>
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("klientakte not found: " + requestBody.getJsonObject("klient").getString("id")); // <3>

                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong during the query: " + err.getMessage());
                routingContext
                  .end(err.getMessage()); // <4>
              });
            });
        // end::Klientakte_Put[]

        // tag::Klientakte_Get[]
        routerBuilder.operation("Klientakte_Get").handler(routingContext -> {
          String param = routingContext.pathParam("klientId");
          System.out.println("entered handler by id Dokument_Get eDokumentenID: " + param);
          JsonObject yuuvisQuery = new JsonObject();
//          if (fallakte) {
//            if (requestBody.getString("Fallakte.Vorgang.Register").isEmpty()) {
//              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'");
//            } else {
//              yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'" +
//                " AND tenYuuvistest:register = '" + requestBody.getString("Fallakte.Vorgang.Register") + "'");
//            }
//          } else {
          yuuvisQuery.put("statement", "SELECT * FROM tenYuuvistest:klientakteneo WHERE tenYuuvistest:id = '" + param + "'");
//          }
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("root", "optimalsystem")
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(ar -> {
              HttpResponse<Buffer> responseQuery = ar;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results >= 1) {
                JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");

                JsonObject klientAkte = new JsonObject();
                klientAkte.put("eAktenID","");
                JsonObject klient = new JsonObject();
                klient.put("vorname",searchProperties.getJsonObject("tenYuuvistest:vorname").getString("value"));
                klient.put("nachname",searchProperties.getJsonObject("tenYuuvistest:nachname").getString("value"));
                klient.put("id",searchProperties.getJsonObject("tenYuuvistest:id").getString("value"));
                klient.put("geburtsdatum",searchProperties.getJsonObject("tenYuuvistest:geburtsdatum").getString("value"));
                klient.put("adresse",searchProperties.getJsonObject("tenYuuvistest:adresse").getString("value"));
                klientAkte.put("klient", klient);
                klientAkte.put("archivieren",searchProperties.getJsonObject("tenYuuvistest:archivieren").getString("value"));
                klientAkte.put("archivierenDatum",searchProperties.getJsonObject("tenYuuvistest:archivdatum").getString("value"));
                klientAkte.put("loeschen",searchProperties.getJsonObject("tenYuuvistest:loeschen").getString("value"));
                klientAkte.put("loeschenDatum",searchProperties.getJsonObject("tenYuuvistest:loeschdatum").getString("value"));

                routingContext
                  .response() // <1>
                  .setStatusCode(200)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                  .end(klientAkte.toString()); // <3>
              } else {
                routingContext
                  .response() // <1>
                  .setStatusCode(204)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                  .end("search result not unique found: " + results); // <3>
              }
              })
            .onFailure(err -> {
                System.out.println("Something went wrong during the query: " + err.getMessage());
                routingContext
                  .end(err.getMessage()); // <4>
              });

        });
        // end::Klientakte_Get[]

        AuthProvider authProvider = createBasicAuthProvider();
        routerBuilder.securityHandler("httpBasicAuth", BasicAuthHandler.create(authProvider));

        // Generate the router
        // tag::routerGen[]
        routerBuilder.bodyHandler(BodyHandler.create());
        Router router = routerBuilder.createRouter(); // <1>

        // REGISTER OPENAPI3 DEFINITION YAML FROM MOUNT AT SAME ROUTE AS WEBROOT
        router
          .route("/swagger/ui/*")
          .method(HttpMethod.GET)
          .handler(routingContext -> {
            System.out.println("GET " + routingContext.normalizedPath());
//            System.out.println("GET " + routingContext.normalizedPath().substring(12));
//            System.out.println("GET " + routingContext.normalizedPath().substring(routingContext.normalizedPath().indexOf(".")+1));
            String fileEnding = routingContext.normalizedPath().substring(routingContext.normalizedPath().indexOf(".")+1);
            String contentType = null;
            String fileName = null;
            if (routingContext.normalizedPath().length() <= 11) {
              fileName = "index.html";
            } else {
              fileName = routingContext.normalizedPath().substring(12);
            }
            switch (fileEnding) {
              case "html":
                contentType = "text/html";
                break;
              case "css":
                contentType = "text/css";
                break;
              case "js":
                contentType = "text/javascript";
                break;
              case "png":
                contentType = "image/png";
                break;
              case "jpeg":
                contentType = "image/jpeg";
                break;
              case "jpg":
                contentType = "image/jpeg";
                break;
              default:
                contentType = "text/html";
                break;
            }
//            StaticHandler.create()
//              .setAllowRootFileSystemAccess(true)
//              .setWebRoot("dist");
            if (fileName.isEmpty()) {
              fileName = "index.html";
            }
            FileReader fileReader = null;
            String fileContent = null;
            try {
              fileReader = new FileReader("src/main/resources/dist/"  + fileName);
              if (fileReader != null) {
                int data;
                StringBuilder content = new StringBuilder();
                while ((data = fileReader.read()) != -1) {
                  content.append((char) data);
                }
                fileContent = content.toString();
              }
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }

            System.out.println("Deliver: " + fileName + " " + contentType);
              routingContext
                .response()
                .setChunked(true)
                .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
                .putHeader("Access-Control-Allow-Origin", "*")
                .setStatusMessage("OK")
                .end(fileContent);

//                .sendFile("dist/"  + fileName );
          });


        // Enable multipart form data parsing
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));


//        router.route("/*").handler(BodyHandler.create());
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

        server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setMaxFormAttributeSize(10256)); // <5>
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
      })
      .onFailure(startPromise::fail);
  }

  private JsonObject getFallakteDokument(String name, String fileName, String fileType, JsonObject requestBody, JsonObject yuuvisSearch, int yuuvisSearchIndex) {
    JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
    JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(yuuvisSearchIndex);
    JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
    JsonObject searchSystemobjectId = searchProperties.getJsonObject("system:objectId");
    String folderID = searchSystemobjectId.getString("value");

    JsonObject yuuvisDocuments = new JsonObject();
    JsonArray yuuvisObjects = new JsonArray();
    JsonObject yuuvisObject = new JsonObject();
    JsonObject properties = new JsonObject();

    JsonObject systemObject = new JsonObject();
    systemObject.put("value", "tenYuuvistest:fallakteneodokument");
    properties.put("system:objectTypeId", systemObject);
    JsonObject systemparentId = new JsonObject();
    systemparentId.put("value", folderID);
    properties.put("system:parentId", systemparentId);
    //vorname, nachname
    JsonObject clienttitle = new JsonObject();
    clienttitle.put("value", requestBody.getString("Dokument.Sachbearbeiter.Vorname") + ", " + requestBody.getString("Dokument.Sachbearbeiter.Nachname"));
    properties.put("clienttitle", clienttitle);
    //prosozDateiname
    JsonObject clientdescription = new JsonObject();
    clientdescription.put("value", requestBody.getString("Dokument.ProsozDateiname"));
    properties.put("clientdescription", clientdescription);

    JsonObject edokumentenid = new JsonObject();
    edokumentenid.put("value",  requestBody.getString("Dokument.EDokumentenID"));
    properties.put("edokumentenid", edokumentenid);

//                registertyp ?
    JsonObject registertyp = new JsonObject();
    registertyp.put("value",  requestBody.getString("Fallakte.RegisterObjektTypName"));
    properties.put("registertyp", registertyp);
//                dokumententyp ?
    JsonObject dokumenttyp = new JsonObject();
    dokumenttyp.put("value",  requestBody.getString("Dokument.Typ"));
    properties.put("dokumenttyp", dokumenttyp);

    JsonObject vorlage = new JsonObject();
    vorlage.put("value", requestBody.getString("Dokument.Vorlage"));
    properties.put("vorlage", vorlage);
//                current system time - erstellungZeitpunkt+ ?
    JsonObject erstelldatum = new JsonObject();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
//                String erstelldatumTime = sdf.format(System.currentTimeMillis());
    erstelldatum.put("value", requestBody.getString("Dokument.ErstellungZeitpunkt"));
    properties.put("erstelldatum", erstelldatum);

    JsonObject vornamesachbearbeiter = new JsonObject();
    vornamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Vorname"));
    properties.put("vornamesachbearbeiter", vornamesachbearbeiter);
    JsonObject nachnamesachbearbeiter = new JsonObject();
    nachnamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Nachname"));
    properties.put("nachnamesachbearbeiter", nachnamesachbearbeiter);
    JsonObject kennungsachbearbeiter = new JsonObject();
    kennungsachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Kennung"));
    properties.put("kennungsachbearbeiter", kennungsachbearbeiter);
    JsonObject vornameempfaenger = new JsonObject();
    vornameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Vorname"));
    properties.put("vornameempfaenger", vornameempfaenger);
    JsonObject nachnameempfaenger = new JsonObject();
    nachnameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Nachname"));
    properties.put("nachnameempfaenger", nachnameempfaenger);
    JsonObject kennungempfaenger = new JsonObject();
    kennungempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Adresse"));
    properties.put("kennungempfaenger", kennungempfaenger);


    JsonObject dateiname = new JsonObject();
    dateiname.put("value", requestBody.getString("Dokument.ProsozDateiname"));
    properties.put("dateiname", dateiname);

    yuuvisObject.put("properties", properties);
    JsonArray contentStreams = new JsonArray();
    JsonObject contentStreamsElement = new JsonObject();
    contentStreamsElement.put("mimeType", fileType);
    contentStreamsElement.put("fileName", fileName);
    contentStreamsElement.put("cid", name);
    contentStreams.add(contentStreamsElement);
    yuuvisObject.put("contentStreams", contentStreams);

    yuuvisObjects.add(yuuvisObject);
    yuuvisDocuments.put("objects", yuuvisObjects);
    return yuuvisDocuments;
  }

  private JsonObject getKlientakteDokument(String name, String fileName, String fileType, JsonObject requestBody, JsonObject yuuvisSearch, int yuuvisSearchIndex) {
    JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
    JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(yuuvisSearchIndex);
    JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
    JsonObject searchSystemobjectId = searchProperties.getJsonObject("system:objectId");
    String folderID = searchSystemobjectId.getString("value");

    JsonObject yuuvisDocuments = new JsonObject();
    JsonArray yuuvisObjects = new JsonArray();
    JsonObject yuuvisObject = new JsonObject();
    JsonObject properties = new JsonObject();

    JsonObject systemObject = new JsonObject();
    systemObject.put("value", "tenYuuvistest:klientakteneodokument");
    properties.put("system:objectTypeId", systemObject);
    JsonObject systemparentId = new JsonObject();
    systemparentId.put("value", folderID);
    properties.put("system:parentId", systemparentId);
    //vorname, nachname
    JsonObject clienttitle = new JsonObject();
    clienttitle.put("value", requestBody.getString("Dokument.Sachbearbeiter.Vorname") + ", " + requestBody.getString("Dokument.Sachbearbeiter.Nachname"));
    properties.put("clienttitle", clienttitle);
    //prosozDateiname
    JsonObject clientdescription = new JsonObject();
    clientdescription.put("value", requestBody.getString("Dokument.ProsozDateiname"));
    properties.put("clientdescription", clientdescription);

    JsonObject edokumentenid = new JsonObject();
    edokumentenid.put("value",  requestBody.getString("Dokument.EDokumentenID"));
    properties.put("edokumentenid", edokumentenid);

//                registertyp ?
    JsonObject registertyp = new JsonObject();
    registertyp.put("value",  requestBody.getString("Klientakte.OrdnerObjektTypName"));
    properties.put("registertyp", registertyp);
//                dokumententyp ?
    JsonObject dokumenttyp = new JsonObject();
    dokumenttyp.put("value",  requestBody.getString("Dokument.Typ"));
    properties.put("dokumenttyp", dokumenttyp);

    JsonObject vorlage = new JsonObject();
    vorlage.put("value", requestBody.getString("Dokument.Vorlage"));
    properties.put("vorlage", vorlage);
//                current system time - erstellungZeitpunkt+ ?
    JsonObject erstelldatum = new JsonObject();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
//                String erstelldatumTime = sdf.format(System.currentTimeMillis());
    erstelldatum.put("value", requestBody.getString("Dokument.ErstellungZeitpunkt"));
    properties.put("erstelldatum", erstelldatum);

    JsonObject vornamesachbearbeiter = new JsonObject();
    vornamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Vorname"));
    properties.put("vornamesachbearbeiter", vornamesachbearbeiter);
    JsonObject nachnamesachbearbeiter = new JsonObject();
    nachnamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Nachname"));
    properties.put("nachnamesachbearbeiter", nachnamesachbearbeiter);
    JsonObject kennungsachbearbeiter = new JsonObject();
    kennungsachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Kennung"));
    properties.put("kennungsachbearbeiter", kennungsachbearbeiter);
    JsonObject vornameempfaenger = new JsonObject();
    vornameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Vorname"));
    properties.put("vornameempfaenger", vornameempfaenger);
    JsonObject nachnameempfaenger = new JsonObject();
    nachnameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Nachname"));
    properties.put("nachnameempfaenger", nachnameempfaenger);
    JsonObject kennungempfaenger = new JsonObject();
    kennungempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Adresse"));
    properties.put("kennungempfaenger", kennungempfaenger);
// idempfaenger - redundant, da kennungempfaenger
//    die folgenden felder ebenfalls redundant, da sie im folder bereits sind
//  <propertyReference>vornameKlient</propertyReference>
//  <propertyReference>nachnameKlient</propertyReference>
//  <propertyReference>geburtsdatumKlient</propertyReference>
//  <propertyReference>idKlient</propertyReference>
//  <propertyReference>dateiname</propertyReference>
    JsonObject dateiname = new JsonObject();
    dateiname.put("value", requestBody.getString("Dokument.ProsozDateiname"));
    properties.put("dateiname", dateiname);
//  <propertyReference>adresseKlient</propertyReference>


    yuuvisObject.put("properties", properties);
    JsonArray contentStreams = new JsonArray();
    JsonObject contentStreamsElement = new JsonObject();
    contentStreamsElement.put("mimeType", fileType);
    contentStreamsElement.put("fileName", fileName);
    contentStreamsElement.put("cid", name);
    contentStreams.add(contentStreamsElement);
    yuuvisObject.put("contentStreams", contentStreams);

    yuuvisObjects.add(yuuvisObject);
    yuuvisDocuments.put("objects", yuuvisObjects);
    return yuuvisDocuments;
  }

  @Override
  public void stop(){
    this.server.close();
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ApiVerticle());
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

}
