///*
// * Copyright 2016-2017 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.github.ibole.microservice.rpc.server.grpc;
//
//import com.google.rpc.Status;
//
//import io.grpc.CallOptions;
//import io.grpc.ClientCall;
//import io.grpc.ManagedChannel;
//import io.grpc.Metadata;
//import io.grpc.ServerCall;
//import io.grpc.ServerCallHandler;
//
//import java.util.Optional;
//
///*********************************************************************************************.
// * 
// * 
// * <p>Copyright 2016, iBole Inc. All rights reserved.
// * 
// * <p></p>
// *********************************************************************************************/
//
//
///**
// * @author bwang
// *
// */
//public class ProxyExample {
//
//  //https://github.com/grpc/grpc-java/issues/3017#issuecomment-302492675
//
//    
//       public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall,
//                                                                    final Metadata headers,
//                                                                    final ServerCallHandler<ReqT, RespT> next) {
//           final Optional<String> targetAlias = resolveRedirectTarget();
//           if (!targetAlias.isPresent()) {
//               return next.startCall(serverCall, headers);
//           }
//
//           final ManagedChannel channel;
//           final ClientCall<ReqT, RespT> clientCall;
//           try {
//               channel = channelFactory.get(targetAlias.get());
//               clientCall = channel.newCall(serverCall.getMethodDescriptor(), CallOptions.DEFAULT);
//               clientCall.start(new ClientCall.Listener<RespT>() {
//                   @Override
//                   public void onHeaders(final Metadata headers) {
//                       // Issue is here
//                       serverCall.sendHeaders(headers);
//                   }
//
//                   @Override
//                   public void onMessage(final RespT message) {
//                       serverCall.sendMessage(message);
//                   }
//
//                   @Override
//                   public void onClose(final Status status, final Metadata trailers) {
//                       serverCall.close(status, trailers);
//                   }
//
//                   @Override
//                   public void onReady() {
//                       serverCall.request(1);
//                   }
//
//               }, headers);
//               clientCall.request(2);
//               serverCall.request(1);
//           } catch (final Exception ignored) {
//               return next.startCall(serverCall, headers);
//           }
//
//           return new ServerCall.Listener<ReqT>(){
//               public void onMessage(final ReqT message) {
//                   clientCall.sendMessage(message);
//               }
//
//               public void onHalfClose() {
//                   clientCall.halfClose();
//               }
//
//               public void onCancel() {
//                   clientCall.cancel("Redirect call was cancelled upstream.", null);
//               }
//
//                public void onReady() {
//                   clientCall.request(1);
//               }
//           };
//       }
//
//}
