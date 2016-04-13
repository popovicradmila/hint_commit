
/*
 *      Copyright (C) 2012-2016 DataStax Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package hintcommit.driver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Internal implementation of ResultSetFuture.
 */
public class DefaultStringFuture implements Future<String>  {


    /**
     * Waits for the query to return and return its result.
     *
     * This method is usually more convenient than {@link #get} because it:
     * <ul>
     *   <li>Waits for the result uninterruptibly, and so doesn't throw
     *   {@link InterruptedException}.</li>
     *   <li>Returns meaningful exceptions, instead of having to deal
     *   with ExecutionException.</li>
     * </ul>
     * As such, it is the preferred way to get the future result.
     *
     * @throws NoHostAvailableException if no host in the cluster can be
     * contacted successfully to execute this query.
     * @throws QueryExecutionException if the query triggered an execution
     * exception, that is an exception thrown by Cassandra when it cannot execute
     * the query with the requested consistency level successfully.
     * @throws QueryValidationException if the query is invalid (syntax error,
     * unauthorized or any other validation problem).
     */
    public String getUninterruptibly() {
        try {
            return Uninterruptibles.getUninterruptibly(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Waits for the provided time for the query to return and return its
     * result if available.
     *
     * This method is usually more convenient than {@link #get} because it:
     * <ul>
     *   <li>Waits for the result uninterruptibly, and so doesn't throw
     *   {@link InterruptedException}.</li>
     *   <li>Returns meaningful exceptions, instead of having to deal
     *   with ExecutionException.</li>
     * </ul>
     * As such, it is the preferred way to get the future result.
     *
     * @throws NoHostAvailableException if no host in the cluster can be
     * contacted successfully to execute this query.
     * @throws QueryExecutionException if the query triggered an execution
     * exception, that is an exception thrown by Cassandra when it cannot execute
     * the query with the requested consistency level successfully.
     * @throws QueryValidationException if the query if invalid (syntax error,
     * unauthorized or any other validation problem).
     * @throws TimeoutException if the wait timed out (Note that this is
     * different from a Cassandra timeout, which is a {@code
     * QueryExecutionException}).
     */
    public String getUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
        try {
            return Uninterruptibles.getUninterruptibly(this, timeout, unit);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

	@Override
	public String get() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get(long arg0, TimeUnit arg1) throws InterruptedException,
			ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO Auto-generated method stub
		return false;
	}
}

