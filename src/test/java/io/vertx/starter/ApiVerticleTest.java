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

  private String apiurl = "localhost";
  private int apiport = 8080;

//  private String apiurl = "10.211.55.4";
//  private int apiport = 30036;


  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) throws Exception {
    Map<String,String> newEnv = new HashMap<>();
    newEnv.put("AUTHENTICATION_SERVICE_HOST", "10.211.55.4");
    newEnv.put("AUTHENTICATION_SERVICE_PORT", "30080");
    newEnv.put("TENANT", "yuuvistest");
    newEnv.put("SERVERURL", apiurl + ":" + apiport);
    newEnv.put("USER", "yuuvis");
    newEnv.put("PASSWORD", "optimalsystem");
    newEnv.put("YUUVISUSER", "root");
    newEnv.put("YUUVISPASSWORD", "optimalsystem");
    setEnv(newEnv);
    vertx.deployVerticle(new ApiVerticle(), testContext.succeeding(id -> {
      System.out.println("server startet");
      producerReadyLatch.countDown();
      testContext.completeNow();
    }));
  }

///////////////////  Klientakte /////////////////////////

  @Test
  void testKlientakteGET(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testGET");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Klientakte/Klientid")
        .basicAuthentication("yuuvis", "optimalsystem")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }


  @Test
  void testHealth(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testHealth");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/Health")
        .basicAuthentication("yuuvis", "optimalsystem")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

  @Test
  void testKlientaktePOST(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testKlientaktePOST");
      JsonObject klientAkte = new JsonObject();
      klientAkte.put("eAktenID","eAktenID1234");
      JsonObject klient = new JsonObject();
      klient.put("vorname","vorname");
      klient.put("nachname","nachname");
      klient.put("id","Klientid");
      klient.put("geburtsdatum","2003-02-18");
      klient.put("adresse","adresse");
      klientAkte.put("klient", klient);
      klientAkte.put("archivieren","true");
      klientAkte.put("archivierenDatum","2024-02-18");
      klientAkte.put("loeschen","false");
      klientAkte.put("loeschenDatum","2024-02-18");

      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(apiport, apiurl, "/api/Klientakte")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendJsonObject(klientAkte)
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
  void testKlientaktePUT(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testKlientaktePUT");
      JsonObject klientAkte = new JsonObject();
      klientAkte.put("eAktenID","eAktenID1234");
      JsonObject klient = new JsonObject();
      klient.put("vorname","Jürgen");
      klient.put("nachname","Schulz");
      klient.put("id","Klientid");
      klient.put("geburtsdatum","2003-02-18");
      klient.put("adresse","adresse");
      klientAkte.put("klient", klient);
      klientAkte.put("archivieren","true");
      klientAkte.put("archivierenDatum","2024-02-18");
      klientAkte.put("loeschen","false");
      klientAkte.put("loeschenDatum","2024-02-18");

      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .put(apiport, apiurl, "/api/Klientakte")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendJsonObject(klientAkte)
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

///////////////////  Fallakte  /////////////////////////

  @Test
  void testFallakteGET(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testGET");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Fallakte/Akte1")
        .basicAuthentication("yuuvis", "optimalsystem")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

  @Test
  void testFallaktePOSTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testFallaktePOST");

      JsonObject fallAkte1 = new JsonObject();
      fallAkte1.put("eAktenID","eAktenID1234");
      JsonObject vorgang = new JsonObject();
      vorgang.put("aktenzeichen","aktenzeichenValue");
      vorgang.put("archivieren","true");
      vorgang.put("archivierenDatum","2021-02-18");
      vorgang.put("bemerkung","bemerkungValue");
      vorgang.put("id","Akte2");
      vorgang.put("loeschen","false");
      vorgang.put("loeschenDatum","2024-02-18");
      vorgang.put("rechtsgebiet","rechtsgebietValue");
      vorgang.put("register","");
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
  void testFallaktePOSTVorgangIDRegister(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testFallaktePOST");

      JsonObject fallAkte1 = new JsonObject();
      fallAkte1.put("eAktenID","eAktenID1234");
      JsonObject vorgang = new JsonObject();
      vorgang.put("aktenzeichen","aktenzeichenValue");
      vorgang.put("archivieren","true");
      vorgang.put("archivierenDatum","2021-02-18");
      vorgang.put("bemerkung","bemerkungValue");
      vorgang.put("id","Akte1");
      vorgang.put("loeschen","false");
      vorgang.put("loeschenDatum","2024-02-18");
      vorgang.put("rechtsgebiet","rechtsgebietValue");
      vorgang.put("register","Register1");
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
      personBaseExtended3.put("vorname","Fritz");
      personBaseExtended3.put("nachname","Walter");
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
  void testFallaktePUTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testFallaktePUT");

      JsonObject fallAkte1 = new JsonObject();
      fallAkte1.put("eAktenID","eAktenID1234");
      JsonObject vorgang = new JsonObject();
      vorgang.put("aktenzeichen","aktenzeichenXYungelöst");
      vorgang.put("archivieren","true");
      vorgang.put("archivierenDatum","2021-02-18");
      vorgang.put("bemerkung","bemerkungValue");
      vorgang.put("id","Akte2");
      vorgang.put("loeschen","false");
      vorgang.put("loeschenDatum","2024-02-18");
      vorgang.put("rechtsgebiet","rechtsgebietValue");
      vorgang.put("register","");
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
        .put(apiport, apiurl, "/api/Fallakte")
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

///////////////    Get Dokumente Fallakte /////////////////

  @Test
  void testDokumentGETGetDokumeteeDokumentenIDFallakte(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testGET");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Dokument")
        .basicAuthentication("yuuvis", "optimalsystem")
        .addQueryParam("eDokumentenID", "12345678901")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.body());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

///////////////    Get Dokumente Klientakte  /////////////

  @Test
  void testDokumentGETGetDokumeteeDokumentenIDKlientakte(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testGET");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Dokument")
        .basicAuthentication("yuuvis", "optimalsystem")
        .addQueryParam("eDokumentenID", "012345678900")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.body());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

///////////////    POST Dokument Fallakte

  @Test
  void testDokumentFallaktePOSTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentPOST");

      JsonObject dokument = new JsonObject();
      dokument.put("Fallakte.EAktenID","Fallakte.EAktenID");
      dokument.put("Fallakte.Vorgang.Aktenzeichen","Fallakte.Vorgang.Aktenzeichen");
      dokument.put("Fallakte.Vorgang.Archivieren","Fallakte.Vorgang.Archivieren");
      dokument.put("Fallakte.Vorgang.ArchivierenDatum","Fallakte.Vorgang.ArchivierenDatum");
      dokument.put("Fallakte.Vorgang.Bemerkung","Fallakte.Vorgang.Bemerkung");
      dokument.put("Fallakte.Vorgang.ID","Akte2");
      dokument.put("Fallakte.Vorgang.Loeschen","Fallakte.Vorgang.Loeschen");
      dokument.put("Fallakte.Vorgang.LoeschenDatum","Fallakte.Vorgang.LoeschenDatum");
      dokument.put("Fallakte.Vorgang.Rechtsgebiet","Fallakte.Vorgang.Rechtsgebiet");
      dokument.put("Fallakte.Vorgang.Register","");
      dokument.put("Fallakte.Vorgang.ZustaendigerSachbearbeiter","Fallakte.Vorgang.ZustaendigerSachbearbeiter");
      dokument.put("Fallakte.Antragssteller.ID","Fallakte.Antragssteller.ID");
      dokument.put("Fallakte.Antragssteller.Geburtsdatum","Fallakte.Antragssteller.Geburtsdatum");
      dokument.put("Fallakte.Antragssteller.Vorname","Fallakte.Antragssteller.Vorname");
      dokument.put("Fallakte.Antragssteller.Nachname","Fallakte.Antragssteller.Nachname");
      dokument.put("Fallakte.Leistungsempfaenger.ID","Fallakte.Leistungsempfaenger.ID");
      dokument.put("Fallakte.Leistungsempfaenger.Geburtsdatum","Fallakte.Leistungsempfaenger.Geburtsdatum");
      dokument.put("Fallakte.Leistungsempfaenger.Vorname","Fallakte.Leistungsempfaenger.Vorname");
      dokument.put("Fallakte.Leistungsempfaenger.Nachname","Fallakte.Leistungsempfaenger.Nachname");
      dokument.put("Fallakte.Unterhaltspflichtiger.ID","Fallakte.Unterhaltspflichtiger.ID");
      dokument.put("Fallakte.Unterhaltspflichtiger.Geburtsdatum","Fallakte.Unterhaltspflichtiger.Geburtsdatum");
      dokument.put("Fallakte.Unterhaltspflichtiger.Vorname","Fallakte.Unterhaltspflichtiger.Vorname");
      dokument.put("Fallakte.Unterhaltspflichtiger.Nachname","Fallakte.Unterhaltspflichtiger.Nachname");
      dokument.put("Fallakte.OrdnerObjektTypName","Fallakte.OrdnerObjektTypName");
      dokument.put("Fallakte.RegisterObjektTypName","Fallakte.RegisterObjektTypName");
      dokument.put("Klientakte.EAktenID","");
      dokument.put("Klientakte.Klient.Adresse","");
      dokument.put("Klientakte.Klient.ID","");
      dokument.put("Klientakte.Klient.Geburtsdatum","");
      dokument.put("Klientakte.Klient.Vorname","");
      dokument.put("Klientakte.Klient.Nachname","Klientakte.Klient.Nachname");
      dokument.put("Klientakte.Archivieren","Klientakte.Archivieren");
      dokument.put("Klientakte.ArchivierenDatum","Klientakte.ArchivierenDatum");
      dokument.put("Klientakte.Loeschen","Klientakte.Loeschen");
      dokument.put("Klientakte.LoeschenDatum","Klientakte.LoeschenDatum");
      dokument.put("Klientakte.OrdnerObjektTypName","Klientakte.OrdnerObjektTypName");
      dokument.put("Dokument.EDokumentenID","012345678901");
      dokument.put("Dokument.ErstellungZeitpunkt","2021-02-18");
      dokument.put("Dokument.Typ","Dokument.Typ");
      dokument.put("Dokument.Vorlage","Dokument.Vorlage");
      dokument.put("Dokument.Sachbearbeiter.Kennung","Dokument.Sachbearbeiter.Kennung");
      dokument.put("Dokument.Sachbearbeiter.Vorname","Dokument.Sachbearbeiter.Vorname");
      dokument.put("Dokument.Sachbearbeiter.Nachname","Dokument.Sachbearbeiter.Nachname");
      dokument.put("Dokument.Empfaenger.Adresse","Dokument.Empfaenger.Adresse");
      dokument.put("Dokument.Empfaenger.Vorname","Dokument.Empfaenger.Vorname");
      dokument.put("Dokument.Empfaenger.Nachname","Dokument.Empfaenger.Nachname");
      dokument.put("Dokument.ProsozDateiname","Dokument.ProsozDateiname");
      dokument.put("Dokument.DokumentObjektTypName","Dokument.DokumentObjektTypName");
      dokument.put("Dokument.ContentUrl","Dokument.ContentUrl");

      MultipartForm form = MultipartForm.create()
        .attribute("data", dokument.encode())
        .binaryFileUpload(
        "File",
        "Lizenz",
        "src/test/resources/LicenseCertificate-R5292742.pdf",
        "application/pdf");


      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(apiport, apiurl, "/api/Dokument")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendMultipartForm(form)
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
  void testDokumentFallaktePOSTVorgangIDRegister(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentPOST");

      JsonObject dokument = new JsonObject();
      dokument.put("Fallakte.EAktenID","Fallakte.EAktenID");
      dokument.put("Fallakte.Vorgang.Aktenzeichen","Fallakte.Vorgang.Aktenzeichen");
      dokument.put("Fallakte.Vorgang.Archivieren","Fallakte.Vorgang.Archivieren");
      dokument.put("Fallakte.Vorgang.ArchivierenDatum","Fallakte.Vorgang.ArchivierenDatum");
      dokument.put("Fallakte.Vorgang.Bemerkung","Fallakte.Vorgang.Bemerkung");
      dokument.put("Fallakte.Vorgang.ID","Akte1");
      dokument.put("Fallakte.Vorgang.Loeschen","Fallakte.Vorgang.Loeschen");
      dokument.put("Fallakte.Vorgang.LoeschenDatum","Fallakte.Vorgang.LoeschenDatum");
      dokument.put("Fallakte.Vorgang.Rechtsgebiet","Fallakte.Vorgang.Rechtsgebiet");
      dokument.put("Fallakte.Vorgang.Register","Register1");
      dokument.put("Fallakte.Vorgang.ZustaendigerSachbearbeiter","Fallakte.Vorgang.ZustaendigerSachbearbeiter");
      dokument.put("Fallakte.Antragssteller.ID","Fallakte.Antragssteller.ID");
      dokument.put("Fallakte.Antragssteller.Geburtsdatum","Fallakte.Antragssteller.Geburtsdatum");
      dokument.put("Fallakte.Antragssteller.Vorname","Fallakte.Antragssteller.Vorname");
      dokument.put("Fallakte.Antragssteller.Nachname","Fallakte.Antragssteller.Nachname");
      dokument.put("Fallakte.Leistungsempfaenger.ID","Fallakte.Leistungsempfaenger.ID");
      dokument.put("Fallakte.Leistungsempfaenger.Geburtsdatum","Fallakte.Leistungsempfaenger.Geburtsdatum");
      dokument.put("Fallakte.Leistungsempfaenger.Vorname","Fallakte.Leistungsempfaenger.Vorname");
      dokument.put("Fallakte.Leistungsempfaenger.Nachname","Fallakte.Leistungsempfaenger.Nachname");
      dokument.put("Fallakte.Unterhaltspflichtiger.ID","Fallakte.Unterhaltspflichtiger.ID");
      dokument.put("Fallakte.Unterhaltspflichtiger.Geburtsdatum","Fallakte.Unterhaltspflichtiger.Geburtsdatum");
      dokument.put("Fallakte.Unterhaltspflichtiger.Vorname","Fallakte.Unterhaltspflichtiger.Vorname");
      dokument.put("Fallakte.Unterhaltspflichtiger.Nachname","Fallakte.Unterhaltspflichtiger.Nachname");
      dokument.put("Fallakte.OrdnerObjektTypName","Fallakte.OrdnerObjektTypName");
      dokument.put("Fallakte.RegisterObjektTypName","Fallakte.RegisterObjektTypName");
      dokument.put("Klientakte.EAktenID","");
      dokument.put("Klientakte.Klient.Adresse","");
      dokument.put("Klientakte.Klient.ID","");
      dokument.put("Klientakte.Klient.Geburtsdatum","");
      dokument.put("Klientakte.Klient.Vorname","");
      dokument.put("Klientakte.Klient.Nachname","Klientakte.Klient.Nachname");
      dokument.put("Klientakte.Archivieren","Klientakte.Archivieren");
      dokument.put("Klientakte.ArchivierenDatum","Klientakte.ArchivierenDatum");
      dokument.put("Klientakte.Loeschen","Klientakte.Loeschen");
      dokument.put("Klientakte.LoeschenDatum","Klientakte.LoeschenDatum");
      dokument.put("Klientakte.OrdnerObjektTypName","Klientakte.OrdnerObjektTypName");
      dokument.put("Dokument.EDokumentenID","012345678901");
      dokument.put("Dokument.ErstellungZeitpunkt","2021-02-18");
      dokument.put("Dokument.Typ","Dokument.Typ");
      dokument.put("Dokument.Vorlage","Dokument.Vorlage");
      dokument.put("Dokument.Sachbearbeiter.Kennung","Dokument.Sachbearbeiter.Kennung");
      dokument.put("Dokument.Sachbearbeiter.Vorname","Dokument.Sachbearbeiter.Vorname");
      dokument.put("Dokument.Sachbearbeiter.Nachname","Dokument.Sachbearbeiter.Nachname");
      dokument.put("Dokument.Empfaenger.Adresse","Dokument.Empfaenger.Adresse");
      dokument.put("Dokument.Empfaenger.Vorname","Dokument.Empfaenger.Vorname");
      dokument.put("Dokument.Empfaenger.Nachname","Dokument.Empfaenger.Nachname");
      dokument.put("Dokument.ProsozDateiname","Dokument.ProsozDateiname");
      dokument.put("Dokument.DokumentObjektTypName","Dokument.DokumentObjektTypName");
      dokument.put("Dokument.ContentUrl","Dokument.ContentUrl");

      MultipartForm form = MultipartForm.create()
        .attribute("data", dokument.encode())
        .binaryFileUpload(
          "File",
          "Lizenz",
          "src/test/resources/LicenseCertificate-R5292742.pdf",
          "application/pdf");


      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(apiport, apiurl, "/api/Dokument")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendMultipartForm(form)
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

////////////////   POST Dokument Klientakte

  @Test
  void testDokumentKlientaktePOSTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentPOST");

      JsonObject dokument = new JsonObject();
      dokument.put("Fallakte.EAktenID","Fallakte.EAktenID");
      dokument.put("Fallakte.Vorgang.Aktenzeichen","Fallakte.Vorgang.Aktenzeichen");
      dokument.put("Fallakte.Vorgang.Archivieren","Fallakte.Vorgang.Archivieren");
      dokument.put("Fallakte.Vorgang.ArchivierenDatum","Fallakte.Vorgang.ArchivierenDatum");
      dokument.put("Fallakte.Vorgang.Bemerkung","Fallakte.Vorgang.Bemerkung");
      dokument.put("Fallakte.Vorgang.ID","Akte2");
      dokument.put("Fallakte.Vorgang.Loeschen","Fallakte.Vorgang.Loeschen");
      dokument.put("Fallakte.Vorgang.LoeschenDatum","Fallakte.Vorgang.LoeschenDatum");
      dokument.put("Fallakte.Vorgang.Rechtsgebiet","Fallakte.Vorgang.Rechtsgebiet");
      dokument.put("Fallakte.Vorgang.Register","");
      dokument.put("Fallakte.Vorgang.ZustaendigerSachbearbeiter","Fallakte.Vorgang.ZustaendigerSachbearbeiter");
      dokument.put("Fallakte.Antragssteller.ID","Fallakte.Antragssteller.ID");
      dokument.put("Fallakte.Antragssteller.Geburtsdatum","Fallakte.Antragssteller.Geburtsdatum");
      dokument.put("Fallakte.Antragssteller.Vorname","Fallakte.Antragssteller.Vorname");
      dokument.put("Fallakte.Antragssteller.Nachname","Fallakte.Antragssteller.Nachname");
      dokument.put("Fallakte.Leistungsempfaenger.ID","Fallakte.Leistungsempfaenger.ID");
      dokument.put("Fallakte.Leistungsempfaenger.Geburtsdatum","Fallakte.Leistungsempfaenger.Geburtsdatum");
      dokument.put("Fallakte.Leistungsempfaenger.Vorname","Fallakte.Leistungsempfaenger.Vorname");
      dokument.put("Fallakte.Leistungsempfaenger.Nachname","Fallakte.Leistungsempfaenger.Nachname");
      dokument.put("Fallakte.Unterhaltspflichtiger.ID","Fallakte.Unterhaltspflichtiger.ID");
      dokument.put("Fallakte.Unterhaltspflichtiger.Geburtsdatum","Fallakte.Unterhaltspflichtiger.Geburtsdatum");
      dokument.put("Fallakte.Unterhaltspflichtiger.Vorname","Fallakte.Unterhaltspflichtiger.Vorname");
      dokument.put("Fallakte.Unterhaltspflichtiger.Nachname","Fallakte.Unterhaltspflichtiger.Nachname");
      dokument.put("Fallakte.OrdnerObjektTypName","Fallakte.OrdnerObjektTypName");
      dokument.put("Fallakte.RegisterObjektTypName","Fallakte.RegisterObjektTypName");
      dokument.put("Klientakte.EAktenID","");
      dokument.put("Klientakte.Klient.Adresse","");
      dokument.put("Klientakte.Klient.ID","Klientid");
      dokument.put("Klientakte.Klient.Geburtsdatum","");
      dokument.put("Klientakte.Klient.Vorname","");
      dokument.put("Klientakte.Klient.Nachname","Klientakte.Klient.Nachname");
      dokument.put("Klientakte.Archivieren","Klientakte.Archivieren");
      dokument.put("Klientakte.ArchivierenDatum","Klientakte.ArchivierenDatum");
      dokument.put("Klientakte.Loeschen","Klientakte.Loeschen");
      dokument.put("Klientakte.LoeschenDatum","Klientakte.LoeschenDatum");
      dokument.put("Klientakte.OrdnerObjektTypName","Klientakte.OrdnerObjektTypName");
      dokument.put("Dokument.EDokumentenID","012345678900");
      dokument.put("Dokument.ErstellungZeitpunkt","2021-02-18");
      dokument.put("Dokument.Typ","Dokument.Typ");
      dokument.put("Dokument.Vorlage","Dokument.Vorlage");
      dokument.put("Dokument.Sachbearbeiter.Kennung","Dokument.Sachbearbeiter.Kennung");
      dokument.put("Dokument.Sachbearbeiter.Vorname","Dokument.Sachbearbeiter.Vorname");
      dokument.put("Dokument.Sachbearbeiter.Nachname","Dokument.Sachbearbeiter.Nachname");
      dokument.put("Dokument.Empfaenger.Adresse","Dokument.Empfaenger.Adresse");
      dokument.put("Dokument.Empfaenger.Vorname","Dokument.Empfaenger.Vorname");
      dokument.put("Dokument.Empfaenger.Nachname","Dokument.Empfaenger.Nachname");
      dokument.put("Dokument.ProsozDateiname","Dokument.ProsozDateiname");
      dokument.put("Dokument.DokumentObjektTypName","Dokument.DokumentObjektTypName");
      dokument.put("Dokument.ContentUrl","Dokument.ContentUrl");

      MultipartForm form = MultipartForm.create()
        .attribute("data", dokument.encode())
        .binaryFileUpload(
          "File",
          "Lizenz",
          "src/test/resources/LicenseCertificate-R5292742.pdf",
          "application/octet-stream");


      WebClient clientFallakte = WebClient.create(this.vertx, options);
      clientFallakte
        .post(apiport, apiurl, "/api/Dokument")
        .basicAuthentication("yuuvis", "optimalsystem")
        .sendMultipartForm(form)
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

///////////////    Get GetDokumente  KlientID
  @Test
  void testDokumentGETGetDokumeteKlientID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentGETGetDokumeteKlientID");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Dokument/GetDokumente")
        .addQueryParam("klientID", "Klientid")
        .basicAuthentication("yuuvis", "optimalsystem")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

///////////////     Get GetDokumente Fallakte
  @Test
  void testDokumentGETGetDokumeteVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentGETGetDokumeteKlientID");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Dokument/GetDokumente")
        .addQueryParam("vorgangID", "Akte2")
        .basicAuthentication("yuuvis", "optimalsystem")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
          testContext.completeNow();
        });
    }
  }

  @Test
  void testDokumentGETGetDokumeteVorgangIDRegister(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      WebClientOptions options = new WebClientOptions()
        .setUserAgent("otto");
      options.setKeepAlive(false);
      System.out.println("start the webclient: testDokumentGETGetDokumeteKlientID");
      WebClient client = WebClient.create(this.vertx, options);
      client
        .get(apiport, apiurl, "/api/Dokument/GetDokumente")
        .addQueryParam("vorgangID", "Akte1")
        .addQueryParam("vorgangRegister", "Register1")
        .basicAuthentication("yuuvis", "optimalsystem")
        .send()
        .onSuccess(ar -> {
          HttpResponse<Buffer> response = ar;
          System.out.println("Received GET response with status code: " + response.statusCode() +" "+ response.statusMessage());
          System.out.println("Received GET response body: " + response.bodyAsString());
          testContext.completeNow();
        })
        .onFailure(err -> {
          System.out.println("GET Something went wrong " + err.getMessage());
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
