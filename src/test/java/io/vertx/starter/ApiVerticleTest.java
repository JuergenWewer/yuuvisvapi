package io.vertx.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ApiVerticleTest {



  private CountDownLatch producerReadyLatch = new CountDownLatch(1);
  private static Vertx vertx = Vertx.vertx();

//  private String apiurl = "localhost";
//  private int apiport = 8080;

  private String apiurl = "10.211.55.4";
  private int apiport = 31367;


  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) throws Exception {
    Map<String,String> newEnv = new HashMap<>();
    newEnv.put("AUTHENTICATION_SERVICE_HOST", "10.211.55.4");
    newEnv.put("AUTHENTICATION_SERVICE_PORT", "30080");
    setEnv(newEnv);
    vertx.deployVerticle(new ApiVerticle(), testContext.succeeding(id -> {
      System.out.println("server startet");
      producerReadyLatch.countDown();
      testContext.completeNow();
    }));
  }

  @Test
  void testGET(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testGET");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Dokument")
        .basicAuthentication("yuuvis", "optimalsystem")
        .addQueryParam("eDokumentenID", "4711")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          testContext.completeNow();
          })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

  @Test
  void testFallaktePOST(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testFallaktePOST");
      JsonObject fallAkte = new JsonObject("{\n" +
        "  \"eAktenID\": \"string\",\n" +
        "  \"vorgang\": {\n" +
        "    \"aktenzeichen\": \"string\",\n" +
        "    \"archivieren\": \"string\",\n" +
        "    \"archivierenDatum\": \"string\",\n" +
        "    \"bemerkung\": \"string\",\n" +
        "    \"id\": \"string\",\n" +
        "    \"loeschen\": \"string\",\n" +
        "    \"loeschenDatum\": \"string\",\n" +
        "    \"rechtsgebiet\": \"string\",\n" +
        "    \"register\": \"string\",\n" +
        "    \"zustaendigerSachbearbeiter\": \"string\"\n" +
        "  },\n" +
        "  \"antragssteller\": {\n" +
        "    \"vorname\": \"asstring\",\n" +
        "    \"nachname\": \"asstring\",\n" +
        "    \"id\": \"asstring\",\n" +
        "    \"geburtsdatum\": \"asstring\"\n" +
        "  },\n" +
        "  \"leistungsempfaenger\": {\n" +
        "    \"vorname\": \"lestring\",\n" +
        "    \"nachname\": \"lestring\",\n" +
        "    \"id\": \"lestring\",\n" +
        "    \"geburtsdatum\": \"lestring\"\n" +
        "  },\n" +
        "  \"unterhaltspflichtiger\": {\n" +
        "    \"vorname\": \"upstring\",\n" +
        "    \"nachname\": \"upstring\",\n" +
        "    \"id\": \"upstring\",\n" +
        "    \"geburtsdatum\": \"upstring\"\n" +
        "  }\n" +
        "}");

      JsonObject fallAkte1 = new JsonObject();
      fallAkte1.put("eAktenID","eAktenID1234");
      JsonObject vorgang = new JsonObject();
      vorgang.put("aktenzeichen","aktenzeichenValue");
      vorgang.put("archivieren","true");
      vorgang.put("archivierenDatum","2021-02-18");
      vorgang.put("bemerkung","bemerkungValue");
      vorgang.put("id","id1");
      vorgang.put("loeschen","false");
      vorgang.put("loeschenDatum","2024-02-18");
      vorgang.put("rechtsgebiet","rechtsgebietValue");
      vorgang.put("register","registerValue");
      vorgang.put("zustaendigerSachbearbeiter","zustaendigerSachbearbeiterValue");
      fallAkte1.put("vorgang", vorgang);
      JsonObject personBaseExtended1 = new JsonObject();
      personBaseExtended1.put("vorname","vornameantragssteller");
      personBaseExtended1.put("nachname","nachnameantragssteller");
      personBaseExtended1.put("id","idantragssteller");
      personBaseExtended1.put("geburtsdatum","2002-03-21");
      fallAkte1.put("antragssteller", personBaseExtended1);
      JsonObject personBaseExtended2 = new JsonObject();
      personBaseExtended2.put("vorname","vornameleistungsempfaenger");
      personBaseExtended2.put("nachname","nachnameleistungsempfaenger");
      personBaseExtended2.put("id","idleistungsempfaenger");
      personBaseExtended2.put("geburtsdatum","2002-03-21");
      fallAkte1.put("leistungsempfaenger", personBaseExtended2);
      JsonObject personBaseExtended3 = new JsonObject();
      personBaseExtended3.put("vorname","vornameunterhaltspflichtiger");
      personBaseExtended3.put("nachname","nachnameunterhaltspflichtiger");
      personBaseExtended3.put("id","idunterhaltspflichtiger");
      personBaseExtended3.put("geburtsdatum","2002-03-21");
      fallAkte1.put("unterhaltspflichtiger", personBaseExtended3);

      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(apiport, apiurl, "/api/Fallakte")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendJsonObject(fallAkte1)
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received POST response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received POST response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("POST Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

  @Test
  void testDokumentPOST(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentPOST");

      JsonObject dokumentBsp = new JsonObject("{\n" +
        "  \"eDokumentenID\": \"string\",\n" +
        "  \"erstellungZeitpunkt\": \"string\",\n" +
        "  \"vorlage\": \"string\",\n" +
        "  \"sachbearbeiter\": {\n" +
        "    \"vorname\": \"asstring\",\n" +
        "    \"nachname\": \"asstring\"\n" +
        "  },\n" +
        "  \"empfaenger\": {\n" +
        "    \"vorname\": \"lestring\",\n" +
        "    \"nachname\": \"lestring\"\n" +
        "  },\n" +
        "  \"prosozDateiname\": \"string\",\n" +
        "  \"contentUrl\": \"string\"\n" +
        "}");

      JsonObject dokument = new JsonObject();
      dokument.put("eDokumentenID","eAktenID1234");
      dokument.put("erstellungZeitpunkt","2021-02-18");
      dokument.put("vorlage","vorlage");
      JsonObject personBaseExtended1 = new JsonObject();
      personBaseExtended1.put("vorname","vornamesachbearbeiter");
      personBaseExtended1.put("nachname","nachnamesachbearbeiter");
      dokument.put("sachbearbeiter", personBaseExtended1);
      JsonObject personBaseExtended2 = new JsonObject();
      personBaseExtended2.put("vorname","vornameempfaenger");
      personBaseExtended2.put("nachname","nachnameempfaenger");
      dokument.put("empfaenger", personBaseExtended2);
      dokument.put("prosozDateiname","prosozDateiname");
      dokument.put("contentUrl","contentUrl");

      try (FileWriter file = new FileWriter("src/test/resources/document.json")) {
        file.write(dokument.toString());
        file.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }

      MultipartForm form = MultipartForm.create()
        .textFileUpload("data","document","src/test/resources/document.json","application/json")
//        .attribute("data", dokument.encode())
//        .binaryFileUpload(
//          "file",
//          "Foto",
//          "src/test/resources/DSC_8113.jpg",
//          "image/jpg")
        .binaryFileUpload(
        "file",
        "Lizenz",
        "src/test/resources/LicenseCertificate-R5292742.pdf",
        "application/pdf");


      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(apiport, apiurl, "/api/Dokument")
//        .post(30314, "10.211.55.4", "/api/Dokument")
//        .putHeader("content-type", "multipart/form-data")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendMultipartForm(form)
//        .sendJsonObject(dokument)
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received POST response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received POST response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("POST Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

  protected static void setEnv(Map<String, String> newenv) throws Exception {
    try {
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      env.putAll(newenv);
      Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
      cienv.putAll(newenv);
    } catch (NoSuchFieldException e) {
      Class[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();
      for(Class cl : classes) {
        if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          Map<String, String> map = (Map<String, String>) obj;
          map.clear();
          map.putAll(newenv);
        }
      }
    }
  }

}
