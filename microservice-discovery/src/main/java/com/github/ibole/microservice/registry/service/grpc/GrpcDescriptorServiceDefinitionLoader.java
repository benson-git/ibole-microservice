package com.github.ibole.microservice.registry.service.grpc;

import com.github.ibole.microservice.common.utils.ClassHelper;
import com.github.ibole.microservice.common.utils.ConcurrentSet;
import com.github.ibole.microservice.registry.service.BuzzServiceInstanceProvider;
import com.github.ibole.microservice.registry.service.ServiceDefinitionLoader;
import com.github.ibole.microservice.registry.service.ServiceImplementationException;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;

import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerServiceDefinition;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Build the list of instance of GrpcServiceDefinition by parsing the generated proto description
 * file in the specified path.
 * 
 * @author bwang
 *
 */
public final class GrpcDescriptorServiceDefinitionLoader extends ServiceDefinitionLoader<GrpcServiceDefinition> {

  private static final Logger LOG = LoggerFactory.getLogger(GrpcDescriptorServiceDefinitionLoader.class.getName());
  private static final ConcurrentSet<GrpcServiceDefinition> services = new ConcurrentSet<GrpcServiceDefinition>();
  private final AtomicBoolean initialized = new AtomicBoolean(false);

  @Override
  public List<GrpcServiceDefinition> getServiceList() {
    if (initialized.compareAndSet(false, true)) {
      loadService();
    }
    return ImmutableList.copyOf(services);
  }

  private void loadService() {

    LOG.info("Load service definition is starting...");
    InputStream in = null;
    FileDescriptorSet descriptorSet;
    try {
      in = ClassHelper.getClassLoader().getResourceAsStream(GrpcConstants.PROTO_DESC_FILENAME);
      descriptorSet = FileDescriptorSet.parseFrom(in);
      for (FileDescriptorProto fdp : descriptorSet.getFileList()) {
        FileDescriptor fd = FileDescriptor.buildFrom(fdp, new FileDescriptor[] {}, true);
        for (com.google.protobuf.Descriptors.ServiceDescriptor service : fd.getServices()) {
          addServiceDenifition(service.getName(),
              fd.getOptions().getJavaPackage() + '.' + service.getFullName());
        }
      }
      LOG.info("Load service denifition is finished, total {} service are found.", services.size());
    } catch (Exception ex) {
      LOG.error("Load service denifition error happened.", ex);
      throw new RuntimeException(ex);

    } finally {
      CloseableUtils.closeQuietly(in);
    }
  }

  private void addServiceDenifition(final String serviceShortName,
      final String fullGprcClassNamePrefix) throws Exception {

    // service interface is a inner static class in generated gRPC class.
    Class<?> serviceInterface =
        ClassHelper.forName(fullGprcClassNamePrefix + GrpcConstants.SERVICE_CLAZZ_SUFFIX + "$"
            + serviceShortName + GrpcConstants.SERVICE_IMPL_CLAZZ_SUFFIX);

    try {
      Object serviceImplBean =
          BuzzServiceInstanceProvider.provider().getServiceBean(serviceInterface);

      if (serviceImplBean != null) {
        Method serviceBinder =
            serviceInterface.getMethod(GrpcConstants.SERVICE_BIND_METHOD);
        services.add(new GrpcServiceDefinition((ServerServiceDefinition) serviceBinder.invoke(serviceImplBean)));
      }
    } catch (ServiceImplementationException e) {
      LOG.warn("Exception happened when loading the instance of service definition '{}'",
          serviceInterface, e);
    }
  }

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.registry.ServiceDefinitionLoader#isAvailable()
   */
  @Override
  protected boolean isAvailable() {   
    return true;
  }

  /* (non-Javadoc)
   * @see com.github.ibole.microservice.registry.ServiceDefinitionLoader#priority()
   */
  @Override
  protected int priority() {
    return 4;
  }
}
