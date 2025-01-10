package com.example.poraproject

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.xbill.DNS.Lookup
import org.xbill.DNS.Record
import org.xbill.DNS.Type

object DBUtil {

    private const val CONNECTION_STRING = ""



    private val mongoClient = MongoClients.create(CONNECTION_STRING)
    private val database: MongoDatabase = mongoClient.getDatabase("RentRideApp")

    fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }

    fun fetchAll(collectionName: String): List<Document> {
        return getCollection(collectionName).find().toList()
    }

    fun testMongoConnection() {
        try {
            println("Attempting to connect to MongoDB...")
            val client = MongoClients.create(CONNECTION_STRING)
            val databases = client.listDatabaseNames()

            println("Databases available:")

            if (databases.iterator().hasNext()) {
                databases.forEach { dbName ->
                    println(dbName)
                }
            } else {
                println("No databases found.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Connection failed: ${e.message}")
        }
    }



    /*private const val SRV_HOSTNAME = "projektnipraktikum.epnifwl.mongodb.net"
    private const val DATABASE_NAME = "RentRideApp"

    private lateinit var mongoClient: com.mongodb.client.MongoClient
    private lateinit var database: MongoDatabase

    // Initialization function to be called from a coroutine
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            val connectionString = resolveSrvAndBuildConnectionString(SRV_HOSTNAME)
            mongoClient = MongoClients.create(connectionString)
            database = mongoClient.getDatabase(DATABASE_NAME)
        }
    }

    fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }

    suspend fun fetchAll(collectionName: String): List<Document> {
        return withContext(Dispatchers.IO) {
            getCollection(collectionName).find().toList()
        }
    }

    private fun resolveSrvAndBuildConnectionString(hostname: String): String {
        val lookup = Lookup("_mongodb._tcp.$hostname", Type.SRV)
        val records = lookup.run()
        if (lookup.result != Lookup.SUCCESSFUL || records == null) {
            throw RuntimeException("Failed to resolve SRV records for $hostname")
        }

        val hosts = records.map { record ->
            val srv = record as org.xbill.DNS.SRVRecord
            "${srv.target}:${srv.port}"
        }

        // Construct a standard connection string
        return "mongodb://${hosts.joinToString(",")}/$DATABASE_NAME?retryWrites=true&w=majority"
    }
*/

}