package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import io.Mongo;
import io.RESTController;
import model.POI;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class FacebookQuery {



	public static void main(String[] args) throws Exception  {
		FacebookQuery fbq = new FacebookQuery();
		Mongo dao = new Mongo();
		fbq.run(dao);
	}


	private Logger logger = Logger.getLogger(RESTController.class);



	public void run(Mongo dao) {
		MongoCollection<Document> collection = dao.retrieveFBData();
		FindIterable<Document> cursor = collection.find();

		/*
		{ "_id" : { "$oid" : "59bfeb1008f3712e10ad9bd6" },
		"id" : "0.618256116400304", "generalInfo" : { "birthday" : "01/26/1976", "gender" : "male", "first_name" : "Marco", "last_name" : "Mamei", "id" : "10212747782318816",
		"likes" : { "data" : [
		{ "about" : "Corso roma 34 CARPI", "category" : "Personal Blog", "name" : "Aleluna Carpi", "id" : "664538570412556" },
		{ "about" : "vi aspettiamo per cena...\nTutto fatto in casa\nVia Bagnoli n.4 - Reggio nell'Emilia\n0522-343324", "category" : "Artist", "name" : "Gian Franco Valenti", "id" : "769127319955550" },
		{ "about" : "\"PiÃ¹ Scienza per TUTTI\" Ã¨ la pagina di comunicazione della conoscenza scientifica e tecnologica curata da Renato Sartini. Informazione, eventi, formazione.", "category" : "Public Figure", "name" : "Renato Sartini - Scienza X TUTTI", "id" : "652485711606869" },
		{ "about" : "Strumenti per la pianificazione di viaggi sostenibili presso LUoghi, Musei, Eventi artistici e culturali dell'Emilia Romagna", "category" : "Media", "name" : "LUMEPlanner", "id" : "1203946292994354" },
		{ "category" : "Local Business", "name" : "Lauro Acetaia Pagani Pagani", "id" : "249797272097798" },
		{ "about" : "Official page of TEDxModena, an independently organized TED event in Modena.\nPrimo evento 27 Maggio 2017, Teatro Storchi.", "category" : "Society & Culture Website", "name" : "TEDxModena", "id" : "692080500954506" }, { "about" : "The Reggio Emilia Behavioural and Experimental Laboratory (Rebel) carries on research activities related to behavioral and social aspects of human decision", "category" : "Community", "name" : "Rebel", "id" : "186662835127717" }, { "about" : "You can have data without information, but you cannot have information without data.  --Daniel Keys Moran", "category" : "Corporate Office", "name" : "Facebook Data Science", "id" : "8394258414" }, { "about" : "Official DQuid page. News about DQuid products, events and Internet of Things evolution. ", "category" : "Internet Company", "name" : "DQuid", "id" : "332408833479120" }, { "about" : "La vita Ã¨ piena di brevi storie tristi, condividetele con noi scrivendoci un messaggio in privato. Non siete soli! \n#brevistorietristi\n#bst\nSito ufficiale Brevi storie Tristi\nhttp://www.brevistorietristi.it\n", "category" : "Fictional Character", "name" : "Brevi Storie Tristi", "id" : "137585733269520" }, { "about" : "La pagina facebook di Nostro Tempo, il settimanale della diocesi di Modena-Nonantola: notizie, immagini e tanti contenuti extra. ", "category" : "News & Media Website", "name" : "Nostro Tempo", "id" : "1418951868331472" }, { "about" : "TIM Ã¨ su Facebook perchÃ© amiamo parlare con voi. CuriositÃ , novitÃ  e assistenza per condividere insieme a voi la nostra idea di futuro", "category" : "Telecommunication Company", "name" : "TIM", "id" : "198011288410" }, { "about" : "Bottega vegana e vegetariana. Panini caldi, estratti di frutta e verdura bio, tramezzini, zuppe, focacce e tanto altro.", "category" : "Specialty Grocery Store", "name" : "Loma via Canalino 61", "id" : "1504358723212521" }, { "about" : "Uno dei problemi piÃ¹ delicati e piÃ¹ difficili oggi Ã¨ lâ\u0080\u0099energia. Un gruppo di ricercatori degli Enti di Bologna propone una discussione pubblica sul tema. ", "category" : "Community", "name" : "Energia Perlitalia", "id" : "432780086906806" }, { "about" : "waitbutwhy.com\n\nstore.waitbutwhy.com\n\nA long-form, stick-figure-illustrated blog about almost everything.", "category" : "Website", "name" : "Wait But Why", "id" : "480646095317630" },
		{ "about" : "Oculus makes it possible to experience anything, anywhere through the power of virtual reality.", "category" : "Video Game", "name" : "Oculus", "id" : "270208243080697" }, { "about" : "PARRUCCHIERI TRUCCATORI", "category" : "Hair Salon", "name" : "Peluqueria Parrucchieri", "id" : "695729297217009" }, { "category" : "Hotel", "name" : "Hotel Fiorenza", "id" : "867241573345872" }, { "about" : "www.matteorenzi.it", "category" : "Politician", "name" : "Matteo Renzi", "id" : "113335124914" }, { "about" : "Innamorata della cucina in ogni sua sfumatura. Da creatrice di Giallozafferano, ora trovi tutte le mie ricette su www.soniaperonaci.it", "category" : "Chef", "name" : "Sonia Peronaci", "id" : "220003562646" }, { "about" : "La seduzione Ã¨ di casa al ristorante Don Giovanni, proponiamo una cucina curata in ogni dettaglio, sia di pesce che tradizionale, in un ambiente raffinato.", "category" : "Restaurant", "name" : "DonGiovanni Di Soliera MO", "id" : "1540739482839688" }, { "about" : "Facebook AI Research", "category" : "Private Investigator", "name" : "Facebook AI Research", "id" : "352917404885219" }, { "about" : "Compravendita e assistenza su immobili a Modena e nelle migliori localitÃ  turistiche", "category" : "Real Estate Investment Firm", "name" : "IMMOBILIARE LA CONTRADA", "id" : "244738675584668" }, { "about" : "A Small Charming Inn", "category" : "Hotel", "name" : "Ca' Bragadin Carabba", "id" : "106048405457" }, { "about" : "Se hai unâ\u0080\u0099attivitÃ , Groupon ti aiuta a far crescere il tuo business http://works.groupon.it. \nScopri tutte le nostre offerte qui https://finder.groupon.it", "category" : "Shopping & Retail", "name" : "Groupon", "id" : "373380217520" }, { "category" : "Public Figure", "name" : "Popi Art", "id" : "164036633796820" }, { "about" : "Established in 2010", "category" : "College & University", "name" : "Design Thinking Reggio Emilia", "id" : "128124184003962" }, { "about" : "I live on Earth at present, and I don't know what I am. I know that I am not a category. I am not a thing â\u0080\u0094 a noun. I seem to be a verb, an evolutionary process â\u0080\u0094 an integral function of the universe   _b.fuller", "category" : "Interior Design Studio", "name" : "THC_  architecture /// visual communication", "id" : "513736495340945" }, { "about" : "Molecules of Knowledge (MoK) is a model for knowledge self-organisation.", "category" : "Software", "name" : "Molecules of Knowledge", "id" : "647292785297708" }, { "about" : "Prodotti e soluzioni per la gestione, la distribuzione e la visualizzazione di contenuti multimediali in ambito professionale", "category" : "Company", "name" : "AlhenaCom", "id" : "423307957687687" },
		{ "about" : "Madison Avenue Comunicazione - Agenzia di grafica e web design a Modena - www.madisonav.it\nComunicazione, graphic design, branding, copywriting, web design, fotoritocco, impaginazione, montaggio video, dvd authoring, immagine coordinata.", "category" : "Consulting Agency", "name" : "madisonav.it", "id" : "130231140475578" }, { "about" : "Consulenza di comunicazione, organizzazione eventi, pubbliche relazioni, pubblicitÃ¡", "category" : "Public Relations Agency", "name" : "Fujiko Events", "id" : "470916542967370" }, { "about" : "Blazing fast chemical based meta-simulator", "category" : "Software", "name" : "Alchemist", "id" : "106914089464784" }, { "about" : "La prima App di Facebook per generare una fiaba con protagonisti tu e i tuoi amici. ", "category" : "Community", "name" : "C'eraunavoltapp", "id" : "431866250198652" }, { "about" : "www.stileaggressive.it", "category" : "Clothing (Brand)", "name" : "Aggressive", "id" : "136993346435572" },
		{ "about" : "DISMI - Dipartimento di Scienze e Metodi dell'Ingegneria | UniversitÃ  degli studi di Modena e Reggio Emilia", "category" : "College & University", "name" : "Ingegneria Reggio Emilia", "id" : "342425415808717" }, { "about" : "Produzione e commercializzazione pannolini, detergenza e cosmesi per neonati e bambini nei canali on line, farmacie, sanitarie e negozi specializzati", "category" : "E-commerce Website", "name" : "MISSKAPPA il pannolino che non smette mai di respirare", "id" : "339785449368153" }, { "about" : "La pagina UFFICIALE di Servizio Pubblico, la factory multimediale di Michele Santoro.", "category" : "Media/News Company", "name" : "Servizio Pubblico", "id" : "281125725235741" }, { "about" : "Siamo il corporate accelerator di TIM che seleziona, finanzia e accelera le migliori startup in ambito digitale.", "category" : "Community", "name" : "TIM #Wcap Accelerator", "id" : "116106065189" }, { "about" : "The official page for AMC's The Walking Dead. For more info, go to www.amc.com", "category" : "TV Show", "name" : "The Walking Dead", "id" : "110475388978628" }, { "category" : "TV Show", "name" : "The Walking Dead", "id" : "136662573036500" }, { "about" : "PAGINE UFFICIALE di UNIMORE - UniversitÃ  degli studi di Modena e Reggio Emilia - www.unimore.it", "category" : "College & University", "name" : "UniversitÃ  di Modena e Reggio Emilia", "id" : "138033796218052" }, { "about" : "We build Facebook.", "category" : "Other", "name" : "Facebook Engineering", "id" : "9445547199" }, { "about" : "Photobucket image", "category" : "Website", "name" : "geotagmap2.jpg image by emsana on Photobucket", "id" : "125775740790923" }, { "about" : "BICINCITTÃ\u0080 OFFICIAL PAGE.\nIl bike sharing nella tua cittÃ . PerchÃ© andare in bicicletta nelle nostre cittÃ  Ã¨ salutare, divertente e si fa prima!", "category" : "Cargo & Freight Company", "name" : "BicincittÃ ", "id" : "370127262128" }, { "about" : "SOUL & FASHION", "category" : "Dance & Night Club", "name" : "SO:FAâ\u0080\u0099 â\u0080¢ VENERDI NOTTE â\u0080¢ PHOTOS' PAGE", "id" : "150609408977" }, { "about" : "il cd MONDOMOPLEN Ã¨ in vendita da max record shop a modena in via del voltone 11 e on line nel portale CD BABY dal nostro sito oltre che hai nostri live!.per info www.myspace.com/moplen69.i MOPLEN piacciono anche al piano di sotto!Scrivi qualcosa su", "category" : "Company", "name" : "MOPLEN", "id" : "64923565699" },
		{ "about" : "VISITA IL SITO http://www.expresidenti.com", "category" : "Musician/Band", "name" : "Ex Presidenti", "id" : "80040171324" },
		{ "about" : "250,000 miles from home.  The hardest thing to face...is yourself.  Now available on Blu-ray & DVD!", "category" : "Movie", "name" : "Moon", "id" : "85942297472" },{ "category" : "Public Figure", "name" : "Ingegner Cane", "id" : "22480843898" }
		],
		"paging" : { "cursors" : { "before" : "NjY0NTM4NTcwNDEyNTU2", "after" : "MjI0ODA4NDM4OTgZD" }, "next" : "https://graph.facebook.com/v2.8/10212747782318816/likes?access_token" }
		}
		},
		"eventsInfo" : [
		{ "name" : "The Walking Dead at New York Comic Con", "place" : { "name" : "The Theater at Madison Square Garden", "location" : { "city" : "New York", "country" : "United States", "latitude" : 40.75063, "longitude" : -73.993744, "state" : "NY", "street" : "2 Penn Plz", "zip" : "10001" }, "id" : "247010618784553" }, "start_time" : "2017-10-07T19:45:00-0400", "end_time" : "2017-10-07T20:45:00-0400", "id" : "117398282303839" },
		{ "name" : "La Notte dei Ricercatori 2017", "place" : { "name" : "UniversitÃ  di Modena e Reggio Emilia", "location" : { "city" : "Modena", "country" : "Italy", "latitude" : 44.64502, "longitude" : 10.92786, "street" : "Via UniversitÃ , 4", "zip" : "41121" }, "id" : "138033796218052" }, "start_time" : "2017-09-29T18:00:00+0200", "end_time" : "2017-09-29T23:30:00+0200", "id" : "727088337476157" }
		]}
		*/

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);



		for (Iterator<Document> iter = cursor.iterator(); iter.hasNext();) {
			Document doc = iter.next();
			JSONObject obj = new JSONObject(doc.toJson());
			String id = obj.getString("id");
			String first_name = obj.getJSONObject("generalInfo").getString("first_name");
			String last_name = obj.getJSONObject("generalInfo").getString("last_name");
			String birthday = obj.getJSONObject("generalInfo").getString("birthday");
			String gender = obj.getJSONObject("generalInfo").getString("gender");
			System.out.println("ID: "+id+" => "+first_name+" "+last_name+" ("+gender+") nato il "+birthday);
			System.out.println("LIKES: ");

			JSONArray likes = obj.getJSONObject("generalInfo").getJSONObject("likes").getJSONArray("data");
			for (int i = 0; i < likes.length(); i++) {
				String like = likes.getJSONObject(i).getString("name");
				System.out.println("\t"+(i+1)+". "+like);
			}
			System.out.println("EVENTS: ");
			JSONArray events = obj.getJSONArray("eventsInfo");
			for (int i = 0; i < events.length(); i++) {
				String event = events.getJSONObject(i).getString("name");
				System.out.println("\t"+(i+1)+". "+event);
			}


		}

	}
}