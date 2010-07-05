package se.skl.tp.recmedcertquestion.transformers;

import iso.v21090.dt.v1.II;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.wsaddressing10.AttributedURIType;

import se.fk.vardgivare.sjukvard.taemotfragaresponder.v1.TaEmotFragaType;
import se.fk.vardgivare.sjukvard.v1.Amne;
import se.fk.vardgivare.sjukvard.v1.Enhet;
import se.fk.vardgivare.sjukvard.v1.Falt;
import se.fk.vardgivare.sjukvard.v1.Lakarintygsreferens;
import se.fk.vardgivare.sjukvard.v1.Organisation;
import se.fk.vardgivare.sjukvard.v1.Patient;
import se.fk.vardgivare.sjukvard.v1.Person;
import se.fk.vardgivare.sjukvard.v1.Adressering.Avsandare;
import se.fk.vardgivare.sjukvard.v1.Adressering.Mottagare;
import se.skl.riv.insuranceprocess.healthreporting.qa.v1.AdresseringsType;
import se.skl.riv.insuranceprocess.healthreporting.qa.v1.Amnetyp;
import se.skl.riv.insuranceprocess.healthreporting.qa.v1.KompletteringType;
import se.skl.riv.insuranceprocess.healthreporting.qa.v1.LakarutlatandeEnkelType;
import se.skl.riv.insuranceprocess.healthreporting.qa.v1.MeddelandeType;
import se.skl.riv.insuranceprocess.healthreporting.qa.v1.Meddelandetyp;
import se.skl.riv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.ReceiveMedicalCertificateQuestionType;
import se.skl.riv.insuranceprocess.healthreporting.v1.EnhetType;
import se.skl.riv.insuranceprocess.healthreporting.v1.HosPersonalType;
import se.skl.riv.insuranceprocess.healthreporting.v1.OrganisationType;
import se.skl.riv.insuranceprocess.healthreporting.v1.PatientType;
import se.skl.riv.insuranceprocess.healthreporting.v1.VardgivareType;

public class FkRequest2VardTransformer extends AbstractMessageAwareTransformer
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public FkRequest2VardTransformer()
    {
        super();
        registerSourceType(Object.class);
        setReturnClass(Object.class);
    }
    
	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {
		ResourceBundle rb = ResourceBundle.getBundle("fkdata");	    

		try {			
			// Transform the XML payload into a JAXB object
            Unmarshaller unmarshaller = JAXBContext.newInstance(TaEmotFragaType.class).createUnmarshaller();
            XMLStreamReader streamPayload = (XMLStreamReader)((Object[])message.getPayload())[1];
            TaEmotFragaType inRequest = (TaEmotFragaType)((JAXBElement)unmarshaller.unmarshal(streamPayload)).getValue();
		
			// Get receiver to adress from Mule property
			String receiverId = (String)message.getProperty("receiverid");			

			// Create new JAXB object for the outgoing data
			ReceiveMedicalCertificateQuestionType outRequest = new ReceiveMedicalCertificateQuestionType();
    		MeddelandeType outMeddelande = new MeddelandeType();
    		outRequest.setQuestion(outMeddelande);

			// Transform between incoming and outgoing objects
    		// Avsändare - FK
    		Avsandare inAvsandare = inRequest.getFKSKLTaEmotFragaAnrop().getAdressering().getAvsandare();
    		Organisation inOrganisationAvsandare = inAvsandare.getOrganisation();
    		    		
    		AdresseringsType outAvsandare = new AdresseringsType();
    		outMeddelande.setAvsandare(outAvsandare);    		
    		OrganisationType outOrganisationAvsandare = new OrganisationType();
    		outAvsandare.setOrganisation(outOrganisationAvsandare);

    		outOrganisationAvsandare.setOrganisationsId(inOrganisationAvsandare.getId().getValue());
    		outOrganisationAvsandare.setOrganisationsnamn(inOrganisationAvsandare.getNamn().getValue());
         	
    		// Mottagare - Vården
    		Mottagare inMottagare = inRequest.getFKSKLTaEmotFragaAnrop().getAdressering().getMottagare();
    		Organisation inOrganisationMottagare = inMottagare.getOrganisation();
    		Enhet inEnhetMottagare = inOrganisationMottagare.getEnhet();
    		Person inPersonMottagare = inEnhetMottagare.getPerson();
    		    		
    		AdresseringsType outMottagare = new AdresseringsType();
    		outMeddelande.setMottagare(outMottagare);    		
    		HosPersonalType outHosPersonalMottagare = new HosPersonalType();
    		outAvsandare.setHosPersonal(outHosPersonalMottagare);

    		if (inPersonMottagare.getNamn() != null && inPersonMottagare.getNamn().length() > 0) {
        		outHosPersonalMottagare.setFullstandigtNamn(inPersonMottagare.getNamn());    			
    		} else {
        		outHosPersonalMottagare.setFullstandigtNamn(inPersonMottagare.getFornamn() + " " + inPersonMottagare.getEfternamn() );
    		}
    		II outPersonalIdMottagare = new II();
    		outPersonalIdMottagare.setRoot("1.2.752.129.2.1.4.1");
    		outPersonalIdMottagare.setExtension(inPersonMottagare.getId().getValue());
    		outHosPersonalMottagare.setPersonalId(outPersonalIdMottagare);

    		EnhetType outEnhetMottagare = new EnhetType();	
    		II outEnhetsIdMottagare = new II();
    		outEnhetsIdMottagare.setRoot("1.2.752.129.2.1.4.1");
    		outEnhetsIdMottagare.setExtension(inEnhetMottagare.getId().getValue());
    		outEnhetMottagare.setEnhetsId(outEnhetsIdMottagare);
    		outEnhetMottagare.setTelefonnummer(inEnhetMottagare.getKontaktuppgifter().getTelefon().getValue());
    		outEnhetMottagare.setPostadress(inEnhetMottagare.getKontaktuppgifter().getAdress().getPostadress().getValue());
    		outEnhetMottagare.setPostnummer(inEnhetMottagare.getKontaktuppgifter().getAdress().getPostnummer().getValue());
    		outEnhetMottagare.setPostort(inEnhetMottagare.getKontaktuppgifter().getAdress().getPostort().getValue());
    		outEnhetMottagare.setEnhetsnamn(inEnhetMottagare.getNamn().getValue());
    		outHosPersonalMottagare.setEnhet(outEnhetMottagare);
    		
    		VardgivareType outVardgivareMottagare = new VardgivareType();
    		outVardgivareMottagare.setVardgivarnamn(inOrganisationMottagare.getNamn().getValue());
    		II outVardgivareIdMottagare = new II();
    		outVardgivareIdMottagare.setRoot("1.2.752.129.2.1.4.1");
    		outVardgivareIdMottagare.setExtension(inOrganisationMottagare.getId().getValue());
    		outVardgivareMottagare.setVardgivareId(outVardgivareIdMottagare);
    		outEnhetMottagare.setVardgivare(outVardgivareMottagare);
    		 		
    		// Avsänt tidpunkt
    		XMLGregorianCalendar inSkickades = inRequest.getFKSKLTaEmotFragaAnrop().getAdressering().getSkickades();
    		outMeddelande.setAvsantTidpunkt(inSkickades);
    		
    		// Set läkarutlåtande enkel från vården
    		Lakarintygsreferens inLakarutlatande = inRequest.getFKSKLTaEmotFragaAnrop().getLakarintyg();
    		Patient inPatient = inRequest.getFKSKLTaEmotFragaAnrop().getPatient();
    		
    		LakarutlatandeEnkelType outLakarutlatandeEnkel = new LakarutlatandeEnkelType();
    		PatientType outPatient = new PatientType();
    		II outPersonId = new II();
    		outPersonId.setRoot("1.2.752.129.2.1.3.1"); // OID för samordningsnummer är 1.2.752.129.2.1.3.3.
    		outPersonId.setExtension(inPatient.getIdentifierare());
    		outPatient.setPersonId(outPersonId);
    		// Hur göra med fullständigt namn?
    		outPatient.setFornamn(inPatient.getFornamn()); 
    		outPatient.setEfternamn(inPatient.getEfternamn());
    		outLakarutlatandeEnkel.setPatient(outPatient);
    		outLakarutlatandeEnkel.setLakarutlatandeId(inLakarutlatande.getReferens());
    		// Skall det vara avsänt tidpunkt eller signerades tidpunkt?
    		outLakarutlatandeEnkel.setAvsantTidpunkt(inLakarutlatande.getSignerades());
    		outMeddelande.setLakarutlatande(outLakarutlatandeEnkel);
    	
    		// Set Försäkringskassans id
    		outMeddelande.setForsakringskassansArendeId(inAvsandare.getReferens().getValue());
    		
    		// Set meddelandetyp
    		outMeddelande.setMeddelandetyp(Meddelandetyp.FRAGA_FRAN_FK);

    		// Set ämne
    		Amne inAmne = inRequest.getFKSKLTaEmotFragaAnrop().getAmne();
    		outMeddelande.setAmne(transformAmneFromFK(inAmne));
    		
    		// Set meddelande rubrik och text
    		outMeddelande.setMeddelanderubrik(inAmne.getFritext());
    		outMeddelande.setMeddelandetext(inRequest.getFKSKLTaEmotFragaAnrop().getFraga().getText());

    		// Komplettering - enbart om ämne är komplettering
    		if (outMeddelande.getAmne().compareTo(Amnetyp.KOMPLETTERING_AV_LAKARINTYG) == 0) {
    			List<Falt> inKompletteringar = inLakarutlatande.getFalt();
    			for(int i = 0 ; i < inKompletteringar.size() ; i++) {
    				Falt inTempFalt = inKompletteringar.get(i);
    				KompletteringType outKomplettering = new KompletteringType();
    				outKomplettering.setFalt(inTempFalt.getNamn());
    				outKomplettering.setText(inTempFalt.getKommentar());
    				outMeddelande.getKomplettering().add(outKomplettering);
    			}
    		}

    		// Sista datum för komplettering
    		outMeddelande.setSistaDatumForKomplettering(inRequest.getFKSKLTaEmotFragaAnrop().getBesvaras());	
    		
    		// Meddelande id???
    		outMeddelande.setMeddelandeId("Referens till meddelande instansen");
    		
    		AttributedURIType logicalAddressHeader = new AttributedURIType();
    		logicalAddressHeader.setValue(receiverId);

    		Object[] payloadOut = new Object[] {logicalAddressHeader, outRequest};
            
	        if (logger.isDebugEnabled()) {
	            logger.debug("transformed payload to: " + payloadOut);
	        }
	        return payloadOut;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
		
	private Amnetyp transformAmneFromFK(Amne inAmne) {
		if (inAmne.getBeskrivning().equalsIgnoreCase("Arbetstidsförläggning")) {
			return Amnetyp.ARBETSTIDSFORLAGGNING;
		} else if (inAmne.getBeskrivning().equalsIgnoreCase("Avstämningsmöte")) {
			return Amnetyp.AVSTAMNINGSMOTE;
		} else if (inAmne.getBeskrivning().equalsIgnoreCase("Komplettering")) {
			return Amnetyp.KOMPLETTERING_AV_LAKARINTYG;
		} else if (inAmne.getBeskrivning().equalsIgnoreCase("Kontakt")) {
			return Amnetyp.KONTAKT;
// Skall detta vara en egen enumeration på vårdsidan?
		} else if (inAmne.getBeskrivning().equalsIgnoreCase("Påminnelse")) {
			return Amnetyp.OVRIGT;
		} else if (inAmne.getBeskrivning().equalsIgnoreCase("Övrigt")) {
			return Amnetyp.OVRIGT;
		} else {
			return Amnetyp.OVRIGT;
		}
	}
}