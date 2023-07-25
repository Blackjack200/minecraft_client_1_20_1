package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JUnitLikeTestReporter implements TestReporter {
   private final Document document;
   private final Element testSuite;
   private final Stopwatch stopwatch;
   private final File destination;

   public JUnitLikeTestReporter(File file) throws ParserConfigurationException {
      this.destination = file;
      this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      this.testSuite = this.document.createElement("testsuite");
      Element element = this.document.createElement("testsuite");
      element.appendChild(this.testSuite);
      this.document.appendChild(element);
      this.testSuite.setAttribute("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
      this.stopwatch = Stopwatch.createStarted();
   }

   private Element createTestCase(GameTestInfo gametestinfo, String s) {
      Element element = this.document.createElement("testcase");
      element.setAttribute("name", s);
      element.setAttribute("classname", gametestinfo.getStructureName());
      element.setAttribute("time", String.valueOf((double)gametestinfo.getRunTime() / 1000.0D));
      this.testSuite.appendChild(element);
      return element;
   }

   public void onTestFailed(GameTestInfo gametestinfo) {
      String s = gametestinfo.getTestName();
      String s1 = gametestinfo.getError().getMessage();
      Element element;
      if (gametestinfo.isRequired()) {
         element = this.document.createElement("failure");
         element.setAttribute("message", s1);
      } else {
         element = this.document.createElement("skipped");
         element.setAttribute("message", s1);
      }

      Element element2 = this.createTestCase(gametestinfo, s);
      element2.appendChild(element);
   }

   public void onTestSuccess(GameTestInfo gametestinfo) {
      String s = gametestinfo.getTestName();
      this.createTestCase(gametestinfo, s);
   }

   public void finish() {
      this.stopwatch.stop();
      this.testSuite.setAttribute("time", String.valueOf((double)this.stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000.0D));

      try {
         this.save(this.destination);
      } catch (TransformerException var2) {
         throw new Error("Couldn't save test report", var2);
      }
   }

   public void save(File file) throws TransformerException {
      TransformerFactory transformerfactory = TransformerFactory.newInstance();
      Transformer transformer = transformerfactory.newTransformer();
      DOMSource domsource = new DOMSource(this.document);
      StreamResult streamresult = new StreamResult(file);
      transformer.transform(domsource, streamresult);
   }
}
