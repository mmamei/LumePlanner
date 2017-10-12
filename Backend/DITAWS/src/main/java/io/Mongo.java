package io;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

import com.mongodb.*;
import model.*;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import util.PointCodec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import static io.LoginInfo.MONGO_DB;
import static io.LoginInfo.MONGO_PASSWORD;
import static io.LoginInfo.MONGO_USER;
import static services.CategoriesDictionary.CAT;


public class Mongo {


	private Logger logger = Logger.getLogger(Mongo.class);

	private ObjectMapper mapper;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private java.util.logging.Logger mongoLogger;


	public static void main(String[] args) {
		Mongo dao = new Mongo();
		Map<String,Double> prefs = dao.getPrefs("marco");
		System.out.println(prefs);
		dao.updatePrefs("marco","attractions",0.05);
		dao.updatePrefs("marco","attractions",0.05);
		dao.updatePrefs("marco","attractions",0.05);
		prefs = dao.getPrefs("marco");
		System.out.println(prefs);
	}


	public Mongo () {

		CodecRegistry codecRegistry =
				CodecRegistries.fromRegistries(
						CodecRegistries.fromCodecs(new PointCodec()),
						MongoClient.getDefaultCodecRegistry());


		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		mongoLogger = java.util.logging.Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE);
        mongoClient = new MongoClient(new ServerAddress(LoginInfo.MONGO_URL), Arrays.asList(
							MongoCredential.createCredential(
                                    MONGO_USER,
									MONGO_DB,
                                    MONGO_PASSWORD.toCharArray())),
									MongoClientOptions.builder().codecRegistry(codecRegistry).build());
        db = mongoClient.getDatabase(MONGO_DB);
        logger.info("loading new db "+MONGO_DB+" .....");
	}


	//	public void closeMongoConnection(){
	//		this.mongoClient.close();
	//	}


	/*
	 * *************************************** INSERT ***************************************
	 */


	public void testInsertion() throws ParseException{
		db.getCollection("testCollection").insertOne(
				new Document("id",new Document("id1", "key1").append("id2", "key2")));
		List<Document> docList = new ArrayList<Document>();
		docList.add(new Document("key1", "value1"));
		docList.add(new Document("key2", "value2"));
		docList.add(new Document("key3", "value3"));
		db.getCollection("testCollection").insertMany(docList);
	}


	public void insertActivity(String city, POI poi) {
		try {
			if (db.getCollection(city+"activities").find(new Document("place_id", poi.getPlace_id())).first() == null)
				db.getCollection(city+"activities").insertOne(Document.parse(poi.toJSONString()));
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}

	public void insertItinerary(String city, Itinerary itinerary) {
		try {
			if (db.getCollection(city+"itineraries").find(new Document("itinerary_id", itinerary.getItinerary_id())).first() == null)
				db.getCollection(city+"itineraries").insertOne(Document.parse(itinerary.toJSONString()));
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}


	public void insertFBData(String fbdata) {
		db.getCollection("fbdata").insertOne(Document.parse(fbdata));

	}

	public MongoCollection<Document> retrieveFBData() {
		return db.getCollection("fbdata");
	}



	/*
	 * *************************************** RETRIEVE ***************************************
	 */


	public void testQuery() {
		MongoCollection<Document> collection = db.getCollection("testCollection");
		FindIterable<Document> cursor = collection.find();
		for (Iterator<Document> iter = cursor.iterator(); iter.hasNext();) {
			System.out.println(iter.next());
		}
	}

	public List<POI> retrieveActivities(String city) {
		List<POI> result = new ArrayList<>();
		try {
			for (Iterator<Document> iter = db.getCollection(city+"activities").find().iterator(); iter.hasNext();) {
				POI p = mapper.readValue(iter.next().toJson(), POI.class);
				p.setDisplay_name(Normalizer.normalize(p.getDisplay_name(), Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").replaceAll("''", "'"));
				result.add(p);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean checkActivities(String city){
		return db.getCollection(city+"activities").count() != 0l;
    }

	public POI retrieveActivity(String city,String place_id) {
		try {
			//logger.info("Retrieve activity:"+place_id);
			return mapper.readValue(db.getCollection(city+"activities").find(new Document("place_id", place_id)).iterator().next().toJson(), POI.class);
		} catch(Exception e) {
			logger.info(e.getMessage());
		}
		return null;
	}


	public List<Itinerary> retrieveItineraries(String city) {
		List<Itinerary> result = new ArrayList<>();
		try {
			for (Iterator<Document> iter = db.getCollection(city+"itineraries").find().iterator(); iter.hasNext();) {
				Itinerary i = mapper.readValue(iter.next().toJson(), Itinerary.class);
				i.setDisplay_name(Normalizer.normalize(i.getDisplay_name(), Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").replaceAll("''", "'"));
				result.add(i);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}



	/*
	 * *************************************** USER-SERVICES ***************************************
	 */


	private Document signup(String user) {
		Document userRecord = db.getCollection("users").find(new Document("user", user)).first();
		if (null == userRecord) {
			logger.info("Creating new user account for "+user);
			userRecord = new Document();
			userRecord.append("user",user);

			Map<String,Double> prefs = new HashMap<>();
			for(String cat: CAT)
				prefs.put(cat, 1.0 / CAT.size());

			userRecord.append("prefs",prefs);

			db.getCollection("users").insertOne(userRecord);
			return userRecord;
		} else {
			logger.info("User "+user+" already exists");
			return userRecord;
		}
	}

	public Map<String,Double> getPrefs(String user) {
		Document userRecord = db.getCollection("users").find(new Document("user", user)).first();
		if(userRecord == null) userRecord = signup(user);
		return (Map<String,Double>)userRecord.get("prefs");
	}

	public void updatePrefs(String user,Map<String,Double> newprefs) {
		Document userRecord = db.getCollection("users").find(new Document("user", user)).first();
		if(userRecord == null) userRecord = signup(user);
		userRecord.put("prefs",newprefs);
		db.getCollection("users").replaceOne(new BasicDBObject().append("user", user),userRecord);
	}

	public Map<String,Double> updatePrefs(String user, String cat, double delta) {
		Document userRecord = db.getCollection("users").find(new Document("user", user)).first();
		if(userRecord == null) userRecord = signup(user);
		Map<String,Double> prefs = (Map<String,Double>)userRecord.get("prefs");

		if(prefs.get(cat)+delta > 1) return prefs;

		for(String c: prefs.keySet()) {
			double v = prefs.get(c);
			if(cat.equals(c)) prefs.put(c,v+delta);
			else prefs.put(c,v - (delta/(prefs.size()-1)));
		}

		db.getCollection("users").replaceOne(new BasicDBObject().append("user", user),userRecord);
		return prefs;
	}




	/*
	 * *************************************** PLAN-SERVICES ***************************************
	 */

	public boolean insertPlan(VisitPlanAlternatives plans) {
		VisitPlan plan_accepted = plans.get(plans.getSelected());
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("user", plan_accepted.getUser())).first();
			if (null == userPlanRecord) {
				logger.info("Creating new visit plan for user "+plan_accepted.getUser());
				db.getCollection("plans").insertOne(Document.parse(plans.toJSONString()));
				return true;
			} else {
				logger.info("Updating visit plan for user "+plan_accepted.getUser());
				db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(plans.toJSONString()));
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public VisitPlanAlternatives retrievePlan(String user) {
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("user", user)).first();
			if (null == userPlanRecord) {
				return null;
			} else {
				logger.info("Getting visit plan for user "+user);
				return mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public VisitPlanAlternatives updatePlan(Visit new_visited) {
		try{
			logger.info("Trying to find the plan for user "+new_visited.getUser());
			Document userPlanRecord = db.getCollection("plans").find(new Document("user", new_visited.getUser())).first();
			if (null != userPlanRecord) {
				VisitPlanAlternatives current = mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);
				for(VisitPlan p : current.getPlans().values())
					p.updatePlan(new_visited);
				db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(current.toJSONString()));
				return current;
			}
		}catch(Exception e) {
			logger.info(e.getMessage());
		}
		logger.info("user plan not found or exception");
		return null;
	}


	public boolean deletePlan(String user_mail) {
		try{
			DeleteResult userPlanRecord = db.getCollection("plans").deleteOne(new Document("crowd.user", user_mail));
			if (0 == userPlanRecord.getDeletedCount()) {
				logger.info("Visiting Plan not found for removal "+user_mail);
				return false;
			} else {
				logger.info("Deleting Visiting Plan for user "+user_mail);
				return true;
			}
		} catch (Exception e) {
			logger.info("Deleting Visiting Plan for user "+user_mail+" thrown an exception: "+e.getMessage());
			return false;
		}
	}
}
