package main.java.hintcommit.driver;

/**
 * Copyright (c) 2013-2015 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License. See accompanying LICENSE file.
 *
 */


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.Notification;

import main.java.hintcommit.cache.CacheStore;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.gson.Gson;
import com.yahoo.ycsb.ByteArrayByteIterator;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

/**
 * Cassandra 2.x CQL client.
 * 
 * See {@code cassandra2/README.md} for details.
 * 
 * @author cmatser
 */
public class YcsbAdapter extends DB {

	static final boolean SSL = System.getProperty("ssl") != null;
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static int CACHE_CAPACITY = 2;
	static int PORT;
	
	public static enum Version {JUST_HINT, HINT_COMMIT_FROM_SERVER, HINT_CACHE}
	private static Version vers = Version.HINT_CACHE;
	
	ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

	public NettyClient nc;
	public RequestExecution re;
	public CacheStore cache; 
	public static Gson gson = new Gson();
	private static boolean debug = false;
	

	/**
	 * Initialize any state for this DB. Called once per DB instance; there is
	 * one DB instance per client thread.
	 */
	@Override
	public void init() throws DBException {
		System.out.println("\nInitializing the YCSB adapter.\n");
		CountDownLatch nettyStartupLatch = new CountDownLatch(1);
			
		nc = new NettyClient(nettyStartupLatch, Integer.parseInt("8007"));
		cache = new CacheStore(CACHE_CAPACITY);
		nc.start();
		re = new RequestExecution(nc, cache, vers);
			
		try {
			nettyStartupLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Cleanup any state for this DB. Called once per DB instance; there is one
	 * DB instance per client thread.
	 */
	@Override
	public void cleanup() throws DBException {
		System.out.println("Cleaning up YCSB adapter.");
	}

	/**
	 * Read a record from the database. Each field/value pair from the result
	 * will be stored in a HashMap.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to read.
	 * @param fields
	 *            The list of fields to read, or null for all of them
	 * @param result
	 *            A HashMap of field/value pairs for the result
	 * @return Zero on success, a non-zero error code on error
	 */
	@Override
	public Status read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		String readCommand = "get," + key + "\r\n";
		System.out.println("Reading " + key);
		
		return read_and_compare(readCommand, result);
	}

	public Status read_and_compare(String command,
			HashMap<String, ByteIterator> result) {
		boolean divergent = false;
		try {
			
			ArrayList<Future<String>> futures = executeCommand(command);

			Future<String> hintRSF = futures.get(0);
			Future<String> commitRSF = null;
			
			if (!vers.equals(Version.JUST_HINT))
				commitRSF = futures.get(1);

			String hintRS;
			String commitRS = null;
			long hintTS;
			try {
				hintRS = Uninterruptibles.getUninterruptibly(hintRSF,300L, TimeUnit.MILLISECONDS);
				//hintRS = hintRSF.getUninterruptibly(300L, TimeUnit.MILLISECONDS);
				hintTS = System.nanoTime();
			} catch (TimeoutException e) {
				System.out.println("  ... hint timed out!");
				return Status.NOT_FOUND;
			}
			try{
				if (!vers.equals(Version.JUST_HINT))
					commitRS = Uninterruptibles.getUninterruptibly(commitRSF,300L, TimeUnit.MILLISECONDS); 
				//commitRSF.getUninterruptibly(300L,TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				System.out.println("  ... commit timed out!");
				return Status.NOT_FOUND;
			}

			if (hintRS==null) {
				//return Status.NOT_FOUND;
			}
			
			//Notification hintNotification = new Notification("value",hintRS, hintTS);

				if (hintRS != null) {
					ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
				    buffer.putLong(hintTS);
					result.put("timestamp", new ByteArrayByteIterator(buffer.array()));
					result.put("content", new StringByteIterator(hintRS));
				} else {
					result.put("timestamp",null);
					result.put("content", null);
					//return Status.NOT_FOUND;
				}

			if (!vers.equals(Version.JUST_HINT)){
				if (commitRS!=null) {
					// There was both a HINT and a COMMIT..
					// Let's compare the HINT with the COMMIT
	
					//Notification commitNotification = new Notification("value",commitRS, hintTS);
	
					//divergent = !commitNotification.equals(hintNotification);
				}
			}
			System.out.println("hint: "+hintRS+",commit: "+commitRS);
			return new TimestampedStatus("", "", hintTS, divergent);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error reading..");
			return Status.ERROR;
		}
	}

	/**
	 * Perform a range scan for a set of records in the database. Each
	 * field/value pair from the result will be stored in a HashMap.
	 * 
	 * Cassandra CQL uses "token" method for range scan which doesn't always
	 * yield intuitive results.
	 * 
	 * @param table
	 *            The name of the table
	 * @param startkey
	 *            The record key of the first record to read.
	 * @param recordcount
	 *            The number of records to read
	 * @param fields
	 *            The list of fields to read, or null for all of them
	 * @param result
	 *            A Vector of HashMaps, where each HashMap is a set field/value
	 *            pairs for one record
	 * @return Zero on success, a non-zero error code on error
	 */
	@Override
	public Status scan(String table, String startkey, int recordcount,
			Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
		System.out.println("Scanning from " + startkey);
		System.out.println("Error: scan not implemented!");

		return Status.ERROR;
	}

	/**
	 * Update a record in the database. Any field/value pairs in the specified
	 * values HashMap will be written into the record with the specified record
	 * key, overwriting any existing values with the same field name.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to write.
	 * @param values
	 *            A HashMap of field/value pairs to update in the record
	 * @return Zero on success, a non-zero error code on error
	 */
	@Override
	public Status update(String table, String key,
			HashMap<String, ByteIterator> values) {
		System.out.println("Updating " + key);
		// Insert and updates provide the same functionality
		return insert(table, key, values);
	}

	/**
	 * Insert a record in the database. Any field/value pairs in the specified
	 * values HashMap will be written into the record with the specified record
	 * key.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to insert.
	 * @param values
	 *            A HashMap of field/value pairs to insert in the record
	 * @return Zero on success, a non-zero error code on error
	 */
	@Override
	public Status insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		System.out.println("Inserting " + key);

		try {
			String value = "";
			// Add fields
			for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
				ByteIterator byteIterator = entry.getValue();
				value = byteIterator.toString();
			}

			String updateCommand = "put,"+key+","+value+"\r\n";
			nc.serverChannel.writeAndFlush(updateCommand);

			return Status.OK;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Status.ERROR;
	}

	/**
	 * Delete a record from the database.
	 * 
	 * @param table
	 *            The name of the table
	 * @param key
	 *            The record key of the record to delete.
	 * @return Zero on success, a non-zero error code on error
	 */
	@Override
	public Status delete(String table, String key) {
		System.out.println("Deleting key: " + key);
		System.out.println("Error: delete not implemented!");

		return Status.ERROR;
	}
	
	public ArrayList<Future<String>> executeCommand(String c){
		ArrayList<Future<String>> ret = new ArrayList<>();
		re.setCommand(c);
		
		if (vers.equals(Version.JUST_HINT)){
			ret.add(pool.submit(re.hintRequest));
		}
		if (vers.equals(Version.HINT_COMMIT_FROM_SERVER)){
			ret.add(pool.submit(re.hintRequest));
			ret.add(pool.submit(re.commitRequest));
		}
		if (vers.equals(Version.HINT_CACHE)){
			ret.add(pool.submit(re.cacheRequest));
			//hre for just current value in server's map, we consider it a commit
			ret.add(pool.submit(re.hintRequest));
		}
		
		return ret;
	}
}