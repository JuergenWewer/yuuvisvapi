package io.vertx.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
public class ApiVerticleTest {



  private CountDownLatch producerReadyLatch = new CountDownLatch(1);
  private static Vertx vertx = Vertx.vertx();


  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) {
    vertx.deployVerticle(new APIVerticle(), testContext.succeeding(id -> {
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
      System.out.println("start the webclient");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(8080, "localhost", "/api/Dokument")
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
  void testPOST(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient");
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
      vorgang.put("aktenzeichen","aktenzeichen");
      vorgang.put("archivieren","true");
      vorgang.put("archivierenDatum","2021-02-18");
      vorgang.put("bemerkung","bemerkung");
      vorgang.put("id","id1");
      vorgang.put("loeschen","false");
      vorgang.put("loeschenDatum","2024-02-18");
      vorgang.put("rechtsgebiet","rechtsgebiet");
      vorgang.put("register","register");
      vorgang.put("zustaendigerSachbearbeiter","zustaendigerSachbearbeiter");
      fallAkte1.put("vorgang", vorgang);
      JsonObject personBaseExtended = new JsonObject();
      personBaseExtended.put("vorname","vorname");
      personBaseExtended.put("nachname","nachname");
      personBaseExtended.put("id","id");
      personBaseExtended.put("geburtsdatum","2002-03-21");
      fallAkte1.put("antragssteller", personBaseExtended);
      fallAkte1.put("leistungsempfaenger", personBaseExtended);
      fallAkte1.put("unterhaltspflichtiger", personBaseExtended);

      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(8080, "localhost", "/api/Fallakte")
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

}
