package hintcommit.driver;

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


import hintcommit.futures.DefaultStringFuture;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.Notification;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
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

	public static final String HOSTS_PROPERTY = "hosts";
	public static final String PORT_PROPERTY = "port";

	static final boolean SSL = System.getProperty("ssl") != null;
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static int PORT;

	ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

	private static boolean debug = false;
	

	/**
	 * Initialize any state for this DB. Called once per DB instance; there is
	 * one DB instance per client thread.
	 */
	@Override
	public void init() throws DBException {
		System.out.println("\nInitializing the YCSB adapter.\n");
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
			Future<String> commitRSF = futures.get(1);

			String hintRS;
			String commitRS;
			long hintTS;
			try {
				hintRS = Uninterruptibles.getUninterruptibly(hintRSF,300L, TimeUnit.MILLISECONDS);
				//hintRS = hintRSF.getUninterruptibly(300L, TimeUnit.MILLISECONDS);
				hintTS = System.nanoTime();

				commitRS = Uninterruptibles.getUninterruptibly(commitRSF,300L, TimeUnit.MILLISECONDS); 
				//commitRSF.getUninterruptibly(300L,TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				System.out.println("  ... timed out!");
				return Status.NOT_FOUND;
			}

			if (hintRS==null) {
				//return Status.NOT_FOUND;
			}
			
			Notification hintNotification = new Notification("value",hintRS, hintTS);

				if (hintRS != null) {
					ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
				    buffer.putLong(hintTS);
					result.put("timestamp", new ByteArrayByteIterator(buffer.array()));
					result.put("content", new StringByteIterator(hintRS));
				} else {
					result.put("timestamp",null);
					result.put("content", null);
					return Status.NOT_FOUND;
				}

			if (commitRS!=null) {
				// There was both a HINT and a COMMIT..
				// Let's compare the HINT with the COMMIT

				Notification commitNotification = new Notification("value",commitRS, hintTS);

				divergent = !commitNotification.equals(hintNotification);
			}
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
			App.nc.serverChannel.writeAndFlush(updateCommand);

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
		App.re.setCommand(c);
		
		ret.add(pool.submit(App.re.hre));
		ret.add(pool.submit(App.re.cre));
		
		return ret;
	}
}