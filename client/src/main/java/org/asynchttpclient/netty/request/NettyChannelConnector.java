/*
 * Copyright (c) 2015 AsyncHttpClient Project. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient.netty.request;

import static org.asynchttpclient.handler.AsyncHandlerExtensionsUtils.toAsyncHandlerExtensions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.AsyncHttpClientState;
import org.asynchttpclient.handler.AsyncHandlerExtensions;
import org.asynchttpclient.netty.SimpleChannelFutureListener;
import org.asynchttpclient.netty.channel.NettyConnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyChannelConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyChannelConnector.class);

    private final AsyncHandlerExtensions asyncHandlerExtensions;
    private final InetSocketAddress localAddress;
    private final List<InetSocketAddress> remoteAddresses;
    private final AsyncHttpClientState clientState;
    private volatile int i = 0;

    public NettyChannelConnector(InetAddress localAddress,//
            List<InetSocketAddress> remoteAddresses,//
            AsyncHandler<?> asyncHandler,//
            AsyncHttpClientState clientState,//
            AsyncHttpClientConfig config) {
        this.localAddress = localAddress != null ? new InetSocketAddress(localAddress, 0) : null;
        this.remoteAddresses = remoteAddresses;
        this.asyncHandlerExtensions = toAsyncHandlerExtensions(asyncHandler);
        this.clientState = clientState;
    }

    private boolean pickNextRemoteAddress() {
        i++;
        return i < remoteAddresses.size();
    }

    public void connect(final Bootstrap bootstrap, final NettyConnectListener<?> connectListener) {
        final InetSocketAddress remoteAddress = remoteAddresses.get(i);

        if (asyncHandlerExtensions != null) {
            try {
                asyncHandlerExtensions.onTcpConnectAttempt(remoteAddress);
            } catch (Exception e) {
                LOGGER.error("onTcpConnectAttempt crashed", e);
                connectListener.onFailure(null, e);
                return;
            }
        }

        try {
            connect0(bootstrap, connectListener, remoteAddress);
        } catch (RejectedExecutionException e) {
            if (clientState.isClosed()) {
                LOGGER.info("Connect crash but engine is shutting down");
            } else {
                connectListener.onFailure(null, e);
            }
        }
    }

    private void connect0(Bootstrap bootstrap, final NettyConnectListener<?> connectListener, InetSocketAddress remoteAddress) {
    	
    	final Bootstrap b = bootstrap;
    	final InetSocketAddress r = remoteAddress;

        bootstrap.connect(remoteAddress, localAddress)//
                .addListener(new SimpleChannelFutureListener() {
                    @Override
                    public void onSuccess(Channel channel) {
                        if (asyncHandlerExtensions != null) {
                            try {
                                asyncHandlerExtensions.onTcpConnectSuccess(r, channel);
                            } catch (Exception e) {
                                LOGGER.error("onTcpConnectSuccess crashed", e);
                                connectListener.onFailure(channel, e);
                                return;
                            }
                        }
                        connectListener.onSuccess(channel, r);
                    }

                    @Override
                    public void onFailure(Channel channel, Throwable t) {
                        if (asyncHandlerExtensions != null) {
                            try {
                                asyncHandlerExtensions.onTcpConnectFailure(r, t);
                            } catch (Exception e) {
                                LOGGER.error("onTcpConnectFailure crashed", e);
                                connectListener.onFailure(channel, e);
                                return;
                            }
                        }
                        boolean retry = pickNextRemoteAddress();
                        if (retry) {
                            NettyChannelConnector.this.connect(b, connectListener);
                        } else {
                            connectListener.onFailure(channel, t);
                        }
                    }
                });
    }
}
