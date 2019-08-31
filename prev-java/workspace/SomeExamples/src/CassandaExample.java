// package com.examples.cassandra;
 
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
 
public class CassandraExample {
 
 //The string serializer translates the byte[] to and from String using utf-8 encoding
    private static StringSerializer stringSerializer = StringSerializer.get();
  
    public static void insertData() {
     try {
       //Create a cluster object from your existing Cassandra cluster
            Cluster cluster = HFactory.getOrCreateCluster("Test Cluster", "localhost:9160");
             
            //Create a keyspace object from the existing keyspace we created using CLI
            Keyspace keyspace = HFactory.createKeyspace("AuthDB", cluster);
             
            //Create a mutator object for this keyspace using utf-8 encoding
            Mutator<string> mutator = HFactory.createMutator(keyspace, stringSerializer);
       
            //Use the mutator object to insert a column and value pair to an existing key
            mutator.insert("sample", "authCollection", HFactory.createStringColumn("username", "admin"));
            mutator.insert("sample", "authCollection", HFactory.createStringColumn("password", "admin"));
             
            System.out.println("Data Inserted");
            System.out.println();
     } catch (Exception ex) {
      System.out.println("Error encountered while inserting data!!");
      ex.printStackTrace() ;
     }
    }
     
    public static void retrieveData() {
     try {
      //Create a cluster object from your existing Cassandra cluster
            Cluster cluster = HFactory.getOrCreateCluster("Test Cluster", "localhost:9160");
             
            //Create a keyspace object from the existing keyspace we created using CLI
            Keyspace keyspace = HFactory.createKeyspace("AuthDB", cluster);
      SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
            sliceQuery.setColumnFamily("authCollection").setKey("sample");
            sliceQuery.setRange("", "", false, 4);
             
            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute(); 
            System.out.println("\nInserted data is as follows:\n" + result.get());
      System.out.println();
     } catch (Exception ex) {
      System.out.println("Error encountered while retrieving data!!");
      ex.printStackTrace() ;
     }
    }
     
    public static void updateData() {
     try {
 
      //Create a cluster object from your existing Cassandra cluster
            Cluster cluster = HFactory.getOrCreateCluster("Test Sample", "localhost:9160");
             
            //Create a keyspace object from the existing keyspace we created using CLI
            Keyspace keyspace = HFactory.createKeyspace("AuthDB", cluster);
             
          //Create a mutator object for this keyspace using utf-8 encoding
            Mutator<string> mutator = HFactory.createMutator(keyspace, stringSerializer);
             
            //Use the mutator object to update a column and value pair to an existing key
            mutator.insert("sample", "authCollection", HFactory.createStringColumn("username", "administrator"));
             
            //Check if data is updated
            MultigetSliceQuery<String, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
            multigetSliceQuery.setColumnFamily("authCollection");
            multigetSliceQuery.setKeys("sample");
         
            //The 3rd parameter returns the columns in reverse order if true
            //The 4th parameter in setRange determines the maximum number of columns returned per key
            multigetSliceQuery.setRange("username", "", false, 1);
            QueryResult<Rows<String, String, String>> result = multigetSliceQuery.execute();
            System.out.println("Updated data..." +result.get());
       
     } catch (Exception ex) {
      System.out.println("Error encountered while updating data!!");
      ex.printStackTrace() ;
     }
    }
 
    public static void deleteData() {
     try {
     
      //Create a cluster object from your existing Cassandra cluster
            Cluster cluster = HFactory.getOrCreateCluster("Test Cluster", "localhost:9160");
             
            //Create a keyspace object from the existing keyspace we created using CLI
            Keyspace keyspace = HFactory.createKeyspace("AuthDB", cluster);
             
          //Create a mutator object for this keyspace using utf-8 encoding
            Mutator<string> mutator = HFactory.createMutator(keyspace, stringSerializer);
             
            //Use the mutator object to delete row
            mutator.delete("sample", "authCollection",null, stringSerializer);
             
            System.out.println("Data Deleted!!");
             
            //try to retrieve data after deleting
            SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
            sliceQuery.setColumnFamily("authCollection").setKey("sample");
            sliceQuery.setRange("", "", false, 4);
             
            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute(); 
            System.out.println("\nTrying to Retrieve data after deleting the key 'sample':\n" + result.get());
             
            //close connection
            cluster.getConnectionManager().shutdown();
     
  } catch (Exception ex) {
   System.out.println("Error encountered while deleting data!!");
   ex.printStackTrace() ;
  }
 }
     
     
 public static void main(String[] args) {
   
  insertData() ;
  retrieveData() ;
  updateData() ;
  deleteData() ;
         
 }
}