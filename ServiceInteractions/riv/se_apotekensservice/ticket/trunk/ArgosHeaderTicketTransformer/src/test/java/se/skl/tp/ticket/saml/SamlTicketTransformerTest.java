package se.skl.tp.ticket.saml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;

import se.skl.tp.ticket.argos.ArgosHeader;
import se.skl.tp.ticket.exception.TicketMachineException;

public class SamlTicketTransformerTest {

    @Test
    public void testTransformMuleMessage() throws TransformerException, UnsupportedEncodingException,
	    XMLStreamException {
	String encoding = "UTF-8";
	MuleMessage muleMessageWithArgosHeader = createCompleteMockedMuleMessage();
	MuleMessage muleMessageWithSamlTicket = (MuleMessage) new SamlTicketTransformer().transform(
		muleMessageWithArgosHeader, encoding);

	assert muleMessageWithSamlTicket != null;
    }

    @Test
    public void testExtractSamlTicketFromMuleMessage() throws XMLStreamException, FactoryConfigurationError,
	    UnsupportedEncodingException, TicketMachineException {
	MuleMessage muleMessageWithArgosHeader = createCompleteMockedMuleMessage();
	XMLEventReader samlTicket = new SamlTicketTransformer().extractSamlTicket(muleMessageWithArgosHeader);

	assert samlTicket.hasNext();
    }

    private MuleMessage createCompleteMockedMuleMessage() throws UnsupportedEncodingException, XMLStreamException {

	String muleMessage = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:inera.se.apotekensservice:argos:1\" xmlns:add=\"http://www.w3.org/2005/08/addressing\" xmlns:urn1=\"urn:riv:se.apotekensservice:or:HamtaAktuellaOrdinationerResponder:1\">"
		+ "<soapenv:Header>"
		+ "<urn:ArgosHeader>"
		+ "<urn:forskrivarkod>1111129</urn:forskrivarkod>"
		+ "<urn:legitimationskod>KOD</urn:legitimationskod>"
		+ "<urn:fornamn>Lars</urn:fornamn>"
		+ "<urn:efternamn>L�kare</urn:efternamn>"
		+ "<urn:befattningskod>123456</urn:befattningskod>"
		+ "<urn:arbetsplatskod>1234567890</urn:arbetsplatskod>"
		+ "<urn:arbetsplatsnamn>Sjukhuset</urn:arbetsplatsnamn>"
		+ "<urn:postort>Staden</urn:postort>"
		+ "<urn:postadress>V�gen 1</urn:postadress>"
		+ "<urn:postnummer>11111</urn:postnummer>"
		+ "<urn:telefonnummer>08-1234567</urn:telefonnummer>"
		+ "<urn:requestId>123456</urn:requestId>"
		+ "<urn:rollnamn>FORSKRIVARE</urn:rollnamn>"
		+ "<urn:directoryID>TSE6565656565-1003</urn:directoryID>"
		+ "<urn:hsaID>TSE6565656565-1003</urn:hsaID>"
		+ "<urn:katalog>HSA</urn:katalog>"
		+ "<urn:organisationsnummer>1234567890</urn:organisationsnummer>"
		+ "<urn:systemnamn>Melior</urn:systemnamn>"
		+ "<urn:systemversion>1.0</urn:systemversion>"
		+ "<urn:systemIp>192.0.0.1</urn:systemIp>"
		+ "<urn:yrkesgrupp>L�kare</urn:yrkesgrupp>"
		+ "</urn:ArgosHeader>"
		+ "<add:To>1234567</add:To>"
		+ "</soapenv:Header>"
		+ "<soapenv:Body>"
		+ "<urn1:HamtaAktuellaOrdinationer>"
		+ "<urn1:personnummer>196308212817</urn1:personnummer>"
		+ "</urn1:HamtaAktuellaOrdinationer>" + "</soapenv:Body>" + "</soapenv:Envelope>";

	InputStream is = new ByteArrayInputStream(muleMessage.getBytes("UTF-8"));
	XMLInputFactory factory = XMLInputFactory.newInstance();
	XMLStreamReader parser = factory.createXMLStreamReader(is, "UTF-8");
	ReversibleXMLStreamReader reversibleXMLStreamReader = new ReversibleXMLStreamReader(parser);
	MuleMessage msg = new DefaultMuleMessage((Object) reversibleXMLStreamReader);
	msg.setProperty("ArgosHeader", getArgosHeader());

	return msg;
    }

    private ArgosHeader getArgosHeader() {

	String forskrivarkod = "1111129";
	String legitimationskod = "KOD";
	String fornamn = "Lars";
	String efternamn = "L�kare";
	String yrkesgrupp = "L�kare";
	String befattningskod = "123456";
	String arbetsplatskod = "1234567890";
	String arbetsplatsnamn = "Sjukhuset";
	String postort = "Staden";
	String postadress = "V�gen 1";
	String postnummer = "11111";
	String telefonnummer = "08-1234567";
	String requestId = "123456";
	String rollnamn = "FORSKRIVARE";
	String directoryID = "TSE6565656565-1003";
	String hsaID = "TSE6565656565-1003";
	String katalog = "HSA";
	String organisationsnummer = "1234567890";
	String systemnamn = "Melior";
	String systemversion = "1.0";
	String systemIp = "192.0.0.1";

	return new ArgosHeader(forskrivarkod, legitimationskod, fornamn, efternamn, yrkesgrupp, befattningskod,
		arbetsplatskod, arbetsplatsnamn, postort, postadress, postnummer, telefonnummer, requestId, rollnamn,
		directoryID, hsaID, katalog, organisationsnummer, systemnamn, systemversion, systemIp);
    }
}