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
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
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
        routerBuilder.operation("Dokument_Get").handler(routingContext -> {
          List<String> params = routingContext.queryParam("eDokumentenID");
          System.out.println("entered handler by id Dokument_Get eDokumentenID: " + params.get(0));
          routingContext
            .response() // <1>
            .setStatusCode(200)
            .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
            .end("alles ok"); // <3>
        });
        // end::Dokument_Get[]

        // tag::Dokument_Post[]
        routerBuilder.operation("Dokument_Post")
          .handler(BodyHandler.create().setUploadsDirectory(UPLOADDIR))
          .handler(routingContext -> {
          System.out.println("entered handler by id Dokument_Post");

          String nameValue="";
          String fileNameValue ="";
          String filePathValue ="";
          String fileTypeValue="";

//          routingContext.response().setChunked(true);
          JsonObject requestBodyValue = null;
          for (FileUpload f : routingContext.fileUploads()) {
            System.out.println("Filename: " + f.fileName());
            System.out.println("Size: " + f.size());
            if (f.name().equals("data")) {
              FileReader fileReader = null;
              try {
                fileReader = new FileReader(f.uploadedFileName());
                if (fileReader != null) {
                  int data;
                  StringBuilder content = new StringBuilder();
                  while ((data = fileReader.read()) != -1) {
                    content.append((char) data);
                  }
                  requestBodyValue = new JsonObject(String.valueOf(content));
                }
              } catch (FileNotFoundException e) {
                e.printStackTrace();
              } catch (IOException e) {
                e.printStackTrace();
              }
            } else if (f.name().equals("file")) {
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
          if (routingContext.fileUploads().size() != 2) {
            System.out.println("To much/less elements in upload, should be 2 but is: " + routingContext.fileUploads().size());
            routingContext
              .response()
              .setStatusCode(204)
              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
              .end("To much/less elements in upload, should be 2 but is: " + routingContext.fileUploads().size()); // <4>
          }
          final String name=nameValue;
//          final String fileName = "0f3174cd-59f7-44b6-ae3f-57f97539a4bf";
//          final String filePath = "file-uploads";
          final String fileName = fileNameValue;
          final String filePath = filePathValue;
          final String fileType=fileTypeValue;
          final JsonObject requestBody = requestBodyValue;

          RequestParameters params = routingContext.get("parsedParameters"); // (1)
          JsonObject data = params.body().getJsonObject();

//          JsonObject requestBody = new JsonObject(data.getString("data"));
          System.out.println("requestBody: " + requestBody);

          JsonObject yuuvisQuery = new JsonObject();
          yuuvisQuery.put("statement","SELECT * FROM tenYuuvistest:fallakteneo WHERE tenYuuvistest:eaktenid = '" + requestBody.getString("eDokumentenID") +"'");
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
                if (results >= 1) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
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
                  clienttitle.put("value", requestBody.getJsonObject("empfaenger").getString("vorname") + ", " + requestBody.getJsonObject("empfaenger").getString("nachname"));
                  properties.put("clienttitle", clienttitle);
                  //prosozDateiname
                  JsonObject clientdescription = new JsonObject();
                  clientdescription.put("value", requestBody.getString("prosozDateiname"));
                  properties.put("clientdescription", clientdescription);

//                registertyp ?
                  JsonObject registertyp = new JsonObject();
                  registertyp.put("value", "fallakte-register");
                  properties.put("registertyp", registertyp);
//                dokumententyp ?
                  JsonObject dokumenttyp = new JsonObject();
                  dokumenttyp.put("value", "fallakte-dokument");
                  properties.put("dokumenttyp", dokumenttyp);

                  JsonObject vorlage = new JsonObject();
                  vorlage.put("value", requestBody.getString("vorlage"));
                  properties.put("vorlage", vorlage);
//                current system time - erstellungZeitpunkt+ ?
                  JsonObject erstelldatum = new JsonObject();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
//                String erstelldatumTime = sdf.format(System.currentTimeMillis());
                  erstelldatum.put("value", requestBody.getString("erstellungZeitpunkt"));
                  properties.put("erstelldatum", erstelldatum);

                  JsonObject vornamesachbearbeiter = new JsonObject();
                  vornamesachbearbeiter.put("value", requestBody.getJsonObject("sachbearbeiter").getString("vorname"));
                  properties.put("vornamesachbearbeiter", vornamesachbearbeiter);
                  JsonObject nachnamesachbearbeiter = new JsonObject();
                  nachnamesachbearbeiter.put("value", requestBody.getJsonObject("sachbearbeiter").getString("nachname"));
                  properties.put("nachnamesachbearbeiter", nachnamesachbearbeiter);
                  JsonObject kennungsachbearbeiter = new JsonObject();
                  kennungsachbearbeiter.put("value", "sachbearbeiterKennung");
                  properties.put("kennungsachbearbeiter", kennungsachbearbeiter);
                  JsonObject vornameempfaenger = new JsonObject();
                  vornameempfaenger.put("value", requestBody.getJsonObject("empfaenger").getString("vorname"));
                  properties.put("vornameempfaenger", vornameempfaenger);
                  JsonObject nachnameempfaenger = new JsonObject();
                  nachnameempfaenger.put("value", requestBody.getJsonObject("empfaenger").getString("nachname"));
                  properties.put("nachnameempfaenger", nachnameempfaenger);
                  JsonObject kennungempfaenger = new JsonObject();
                  kennungempfaenger.put("value", "empfaengerKennung");
                  properties.put("kennungempfaenger", kennungempfaenger);


//                      kennung ?
//                JsonObject kennung = new JsonObject();
//                kennung.put("value","kennungValue");
//                properties.put("kennung",kennung);
//                id = eDokumentenID
//                JsonObject id = new JsonObject();
//                id.put("value",requestBody.getString("eDokumentenID"));
//                properties.put("id",id);

                  //dateiname = prosozdateiname + ?
                  JsonObject dateiname = new JsonObject();
                  dateiname.put("value", requestBody.getString("prosozDateiname"));
                  properties.put("dateiname", dateiname);
//                adresse?
//                JsonObject adresse = new JsonObject();
//                adresse.put("value","");
//                properties.put("adresse",adresse);

                  yuuvisObject.put("properties", properties);
//                "contentStreams": [{
//                  "mimeType": "message/rfc822",
//                    "fileName": "upload.eml",
//                    "cid": "cid_63apple"
//                }],
                  JsonArray contentStreams = new JsonArray();
                  JsonObject contentStreamsElement = new JsonObject();
                  contentStreamsElement.put("mimeType", fileType);
                  contentStreamsElement.put("fileName", fileName);
                  contentStreamsElement.put("cid", name);
                  contentStreams.add(contentStreamsElement);
                  yuuvisObject.put("contentStreams", contentStreams);

                  yuuvisObjects.add(yuuvisObject);
                  yuuvisDocuments.put("objects", yuuvisObjects);


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
                    .end("Dokument mit der eDokumentenID existiert nicht oder ist nicht eindeutig"); // <4>
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
        // end::createDokument_PostHandler[]

        // tag::Fallakte_PostHandler[]
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

          //unfilled:
//          JsonObject registertyp = new JsonObject();
//          registertyp.put("value","");
//          properties.put("registertyp",registertyp);
//          JsonObject dokumenttyp = new JsonObject();
//          dokumenttyp.put("value","");
//          properties.put("dokumenttyp",dokumenttyp);
//          JsonObject vorlage = new JsonObject();
//          vorlage.put("value","");
//          properties.put("vorlage",vorlage);

          //current system time
//          JsonObject erstelldatum = new JsonObject();
//          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
//          String erstelldatumTime = sdf.format(System.currentTimeMillis());
//          erstelldatum.put("value",erstelldatumTime);
//          properties.put("erstelldatum",erstelldatum);

          //unfilled:
//          JsonObject kennung = new JsonObject();
//          kennung.put("value","");
//          properties.put("kennung",kennung);

          //klient - unfilled
//          JsonObject vornameKlient = new JsonObject();
//          vornameKlient.put("value","");
//          properties.put("vornameKlient",vornameKlient);
//          JsonObject nachnameKlient = new JsonObject();
//          nachnameKlient.put("value","");
//          properties.put("nachnameKlient",nachnameKlient);
//          JsonObject geburtsdatumKlient = new JsonObject();
//          geburtsdatumKlient.put("value","");
//          properties.put("vorlage",geburtsdatumKlient);
//          JsonObject idKlient = new JsonObject();
//          idKlient.put("value","");
//          properties.put("idKlient",idKlient);

          //unfilled
//          JsonObject dateiname = new JsonObject();
//          dateiname.put("value","");
//          properties.put("dateiname",dateiname);
//          JsonObject adresse = new JsonObject();
//          adresse.put("value","");
//          properties.put("adresse",adresse);


          yuuvisObject.put("properties",properties);
          yuuvisObjects.add(yuuvisObject);
          yuuvisDocuments.put("objects",yuuvisObjects);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects")
            .timeout(10000)
            .putHeader("X-ID-TENANT-NAME", "yuuvistest")
            .basicAuthentication("root", "optimalsystem")
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
        routerBuilder.bodyHandler(BodyHandler.create());
        Router router = routerBuilder.createRouter(); // <1>

        // REGISTER OPENAPI3 DEFINITION YAML FROM MOUNT AT SAME ROUTE AS WEBROOT
        router
          .route("/swagger/ui/*")
          .method(HttpMethod.GET)
//          .path("/swagger/ui/*")
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

//        // REGISTER OPENAPI3 DEFINITION YAML FROM MOUNT AT SAME ROUTE AS WEBROOT
//        router
//          .route()
//          .method(HttpMethod.GET)
//          .path("/openapi")
//          .handler(routingContext -> {
//            System.out.println("GET " + routingContext.normalizedPath());
//            StaticHandler.create()
//              .setAllowRootFileSystemAccess(true)
//              .setWebRoot("openapi3.yaml");
//          });

        // 'PING' ENDPOINT TO SEE IF SERVER IS UP AND RUNNING
//        router
//          .trace("/*")
//          .handler(routingContext -> {
//            System.out.println("trace");
//            routingContext.response().setStatusMessage("OK").end();});

        // 'PING' Get ENDPOINT
//        router
//          .route()
//          .method(HttpMethod.GET)
//          .path("/*")
//          .handler(
//            routingContext -> {
//              System.out.println("PING Get" + routingContext.normalizedPath());
//              routingContext
//                .response()
//                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
//                .setStatusMessage("OK")
//                .end(new JsonObject().put("status", "ok").encodePrettily());});

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
