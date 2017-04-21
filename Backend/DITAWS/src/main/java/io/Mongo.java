package io;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import model.*;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.geojson.Point;

import util.PointCodec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;


public class Mongo {

    private static final String MONGO_URL = "127.0.0.1:27017";
	private static final String MONGO_USER = "dita";
    private static final String MONGO_PASSWORD = "mames1976";
	private static final String MONGO_DB = "lume";
	private Logger logger = Logger.getLogger(Mongo.class);

	private ObjectMapper mapper;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private java.util.logging.Logger mongoLogger;


	public Mongo () {

		CodecRegistry codecRegistry =
				CodecRegistries.fromRegistries(
						CodecRegistries.fromCodecs(new PointCodec()),
						MongoClient.getDefaultCodecRegistry());


		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		mongoLogger = java.util.logging.Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE);
        mongoClient = new MongoClient(new ServerAddress(MONGO_URL), Arrays.asList(
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

	public void insertRestaurant(String city, POI poi) {
		try {
			if (db.getCollection(city+"restaurants").find(new Document("place_id", poi.getPlace_id())).first() == null)
				db.getCollection(city+"restaurants").insertOne(Document.parse(poi.toJSONString()));
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


	public List<POI> retrieveRestaurants(String city) {
		List<POI> result = new ArrayList<POI>();
		try {
			for (Iterator<Document> iter = db.getCollection(city+"restaurants").find().iterator(); iter.hasNext();) {
				result.add(mapper.readValue(iter.next().toJson(), POI.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
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

	public POI retrieveClosestActivity(String city,POI custom) {
		POI result = new POI();
		try {
			result = mapper.readValue(db.getCollection(city+"activities").find(new Document("geometry", new Document("$near",
					new Document("$geometry", custom.getGeometry())))).first().toJson(), POI.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<POI> retrieveClosestRestaurants(String city,Point location) {
		List<POI> result = new ArrayList<>();
		try {
			FindIterable<Document> top15 = db.getCollection(city+"activities").find(new Document("geometry", new Document("$near",
					new Document("$geometry", location)))).limit(15);
			for (Document document : top15) {
				result.add(mapper.readValue(document.toJson(), POI.class));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
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


	public boolean signup(User user) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(user.getEmail().getBytes()).toString())).first();
		if (null == userRecord) {
			logger.info("Creating new user account for "+user.getEmail());
			db.getCollection("users").insertOne(Document.parse(user.toJSONString()));
			return true;
		} else {
			logger.info("User "+user.getEmail()+" already exists");
			return false;
		}
	}

	public Integer login(User user) {
		Document userRecord = db.getCollection("users").find(new Document("id", UUID.nameUUIDFromBytes(user.getEmail().getBytes()).toString())).first();
		if (null != userRecord) {
			if (userRecord.get("password").equals(user.getPassword())) {
				logger.info("User "+user.getEmail()+" successfully logged in");
				if (null == db.getCollection("plans").find(new Document("crowd.user", user.getEmail())).first()) {
					return 1;
				} else {
					return 2;
				}

			} else {
				logger.info("Wrong password for "+user.getEmail());
				return 0; //wrong password
			}
		}
		logger.info("User "+user.getEmail()+" not found");
		return -1; //user not found
	}


	public boolean insertPlan(VisitPlanAlternatives plans) {
		VisitPlan plan_accepted = plans.getShortest();
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("crowd.user", plan_accepted.getUser())).first();
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

	public VisitPlanAlternatives retrievePlan(String user_email) {
		try{
			Document userPlanRecord = db.getCollection("plans").find(new Document("crowd.user", user_email)).first();
			if (null == userPlanRecord) {
				return null;
			} else {
				logger.info("Getting visit plan for user "+user_email);
				return mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/*
	 * *************************************** USER-SERVICES ***************************************
	 */

	public VisitPlanAlternatives updatePlan(Visit new_visited) {
		try{
			logger.info("Trying to find the plan for user "+new_visited.getUser());
			Document userPlanRecord = db.getCollection("plans").find(new Document("crowd.user", new_visited.getUser())).first();
			if (null != userPlanRecord) {
				VisitPlanAlternatives current = mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);

				/***** UPDATE SHORTEST PATH *****/
				VisitPlan plan = current.getShortest();
				Activity to_swap = null;
				for (Activity activity : plan.getTo_visit()) {
					//logger.info("Short check:"+activity.getVisit().getPlace_id());
					if (activity.getVisit().getPlace_id().equals(new_visited.getVisited().getPlace_id())) {
						to_swap = activity;
						break;
					}
				}
				if (null == to_swap) {
					logger.error("to_swap is null on shortest: " + new_visited.getVisited().getPlace_id() + " plan: " + plan.toString());
					throw new RuntimeException();
				}
				plan.getTo_visit().remove(to_swap);
				if (plan.getVisited() == null) {
					plan.setVisited(new ArrayList<Activity>());
				}
				plan.getVisited().add(to_swap);

				//logger.info("short swap:"+to_swap);
				/***** UPDATE GREEDY *****/
				to_swap = null;
				plan = current.getAsis();
				for (Activity activity : plan.getTo_visit()) {
					//logger.info("Greedy check:"+activity.getVisit().getPlace_id());
					if (activity.getVisit().getPlace_id().equals(new_visited.getVisited().getPlace_id())) {
						to_swap = activity;
						break;
					}
				}
				if (null == to_swap) {
					logger.error("to_swap is null on greedy: " + new_visited.getVisited().getPlace_id() + " plan: " + plan.toString());
					throw new RuntimeException();
				}

				plan.getTo_visit().remove(to_swap);
				if (plan.getVisited() == null) {
					plan.setVisited(new ArrayList<Activity>());
				}
				plan.getVisited().add(to_swap);
				//logger.info("greedy swap:"+to_swap);
				/***** UPDATE LESS CROWDED *****/

				plan = current.getCrowd();
				to_swap = null;
				for (Activity activity : plan.getTo_visit()) {
					//logger.info("Crowd check:"+activity.getVisit().getPlace_id());
					if (activity.getVisit().getPlace_id().equals(new_visited.getVisited().getPlace_id())) {
						to_swap = activity;
						break;
					}
				}
				if (null == to_swap) {
					logger.error("to_swap is null on crow_related: " + new_visited.getVisited().getPlace_id() + " plan: " + plan.toString());
					throw new RuntimeException();
				}


				plan.getTo_visit().remove(to_swap);
				if (plan.getVisited() == null) {
					plan.setVisited(new ArrayList<Activity>());
				}
				plan.getVisited().add(to_swap);
				db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(current.toJSONString()));

				//logger.info("crowd swap:"+to_swap);
				return current;
			}
		} catch (Exception e) {
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
