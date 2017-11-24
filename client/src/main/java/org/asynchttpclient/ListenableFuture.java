/*
 * Copyright 2010 Ning, Inc.
 *
 * This program is licensed to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asynchttpclient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import javatest.added.CompletableFuture;

/**
 * Extended {@link Future}
 *
 * @param <V> Type of the value that will be returned.
 */
public interface ListenableFuture<V> extends Future<V> {

    /**
     * Terminate and if there is no exception, mark this Future as done and release the internal lock.
     */
    void done();

    /**
     * Abort the current processing, and propagate the {@link Throwable} to the {@link AsyncHandler} or {@link Future}
     *
     * @param t the exception
     */
    void abort(Throwable t);

    /**
     * Touch the current instance to prevent external service to times out.
     */
    void touch();

    /**
     * Adds a listener and executor to the ListenableFuture.
     * The listener will be {@linkplain java.util.concurrent.Executor#execute(Runnable) passed
     * to the executor} for execution when the {@code Future}'s computation is
     * {@linkplain Future#isDone() complete}.
     * <br>
     * Executor can be <code>null</code>, in that case executor will be executed
     * in the thread where completion happens.
     * <br>
     * There is no guaranteed ordering of execution of listeners, they may get
     * called in the order they were added and they may get called out of order,
     * but any listener added through this method is guaranteed to be called once
     * the computation is complete.
     *
     * @param listener the listener to run when the computation is complete.
     * @param exec     the executor to run the listener in.
     * @return this Future
     */
    public ListenableFuture<V> addListener(Runnable listener, Executor exec);

    public CompletableFuture<V> toCompletableFuture();
    
    class CompletedFailure<T> implements ListenableFuture<T>{

        private final ExecutionException e;

        public CompletedFailure(Throwable t) {
            e = new ExecutionException(t);
        }

        public CompletedFailure(String message, Throwable t) {
            e = new ExecutionException(message, t);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            throw e;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw e;
        }

        @Override
        public void done() {
        }

        @Override
        public void abort(Throwable t) {
        }

        @Override
        public void touch() {
        }

        @Override
        public ListenableFuture<T> addListener(Runnable listener, Executor exec) {
            if (exec != null) {
                exec.execute(listener);
            } else {
                listener.run();
            }
            return this;
        }
        
        @Override
        public CompletableFuture<T> toCompletableFuture() {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }

		@Override
		public boolean isSuccess() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isCancellable() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Throwable cause() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> addListener(GenericFutureListener<? extends Future<? super T>> listener) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> addListeners(GenericFutureListener<? extends Future<? super T>>... listeners) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> removeListener(GenericFutureListener<? extends Future<? super T>> listener) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> removeListeners(GenericFutureListener<? extends Future<? super T>>... listeners) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> sync() throws InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> syncUninterruptibly() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> await() throws InterruptedException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<T> awaitUninterruptibly() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean await(long timeoutMillis) throws InterruptedException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean awaitUninterruptibly(long timeoutMillis) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public T getNow() {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
