/*
 * Copyright (c) 2015 AsyncHttpClient Project. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at
 *     http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.resolver;

import io.netty.resolver.NameResolver;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.asynchttpclient.handler.AsyncHandlerExtensions;
import org.asynchttpclient.netty.SimpleFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RequestHostnameResolver {

    INSTANCE;

    public Future<List<InetSocketAddress>> resolve(NameResolver<InetAddress> nameResolver, InetSocketAddress unresolvedAddress, AsyncHandlerExtensions asyncHandlerExtensions) {

        final String hostname = unresolvedAddress.getHostName();
        final int port = unresolvedAddress.getPort();
        final Promise<List<InetSocketAddress>> promise = ImmediateEventExecutor.INSTANCE.newPromise();

        if (asyncHandlerExtensions != null) {
            try {
                asyncHandlerExtensions.onHostnameResolutionAttempt(hostname);
            } catch (Exception e) {
                LOGGER.error("onHostnameResolutionAttempt crashed", e);
                promise.tryFailure(e);
                return promise;
            }
        }

        final Future<List<InetAddress>> whenResolved = nameResolver.resolveAll(hostname);
        final AsyncHandlerExtensions a1 = asyncHandlerExtensions;
        
        whenResolved.addListener(new SimpleFutureListener<List<InetAddress>>() {

        	final AsyncHandlerExtensions a = a1;
        	
            @Override
            protected void onSuccess(List<InetAddress> value) throws Exception {
                ArrayList<InetSocketAddress> socketAddresses = new ArrayList<>(value.size());
                for (InetAddress a : value) {
                    socketAddresses.add(new InetSocketAddress(a, port));
                }
                if (a != null) {
                    try {
                        a.onHostnameResolutionSuccess(hostname, socketAddresses);
                    } catch (Exception e) {
                        LOGGER.error("onHostnameResolutionSuccess crashed", e);
                        promise.tryFailure(e);
                        return;
                    }
                }
                promise.trySuccess(socketAddresses);
            }

            @Override
            protected void onFailure(Throwable t) throws Exception {
                if (a != null) {
                    try {
                        a.onHostnameResolutionFailure(hostname, t);
                    } catch (Exception e) {
                        LOGGER.error("onHostnameResolutionFailure crashed", e);
                        promise.tryFailure(e);
                        return;
                    }
                }
                promise.tryFailure(t);
            }
        });

        return promise;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHostnameResolver.class);
}
