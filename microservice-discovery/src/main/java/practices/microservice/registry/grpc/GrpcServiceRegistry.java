package practices.microservice.registry.grpc;

import io.grpc.ServerServiceDefinition;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.ClassHelper;
import practices.microservice.common.ConcurrentSet;
import practices.microservice.common.IOUtils;
import practices.microservice.registry.RegisterEntry;
import practices.microservice.registry.ServiceDefinition;
import practices.microservice.registry.ServiceInstanceProvider;
import practices.microservice.registry.ServiceRegistry;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;

public final class GrpcServiceRegistry implements ServiceRegistry {

	private final static Logger LOG = LoggerFactory.getLogger(GrpcServiceRegistry.class);
	private final static String PROTO_DESC_FILENAME = "protos.desc";
	private final static String SERVICE_CLAZZ_SUFFIX = "Grpc";
	private final static String SERVICE_BIND_METHOD = "bindService";

	private final static ConcurrentSet<RegisterEntry> services = new ConcurrentSet<RegisterEntry>();
   
	@Override
	public List<RegisterEntry> getServiceList() {

		return ImmutableList.copyOf(services);
	}

	@Override
	public void addService(RegisterEntry entry) {

		services.add(entry);
	}

	public void loadService() {

		LOG.info("Load Service is starting...");
		InputStream in = null;
		FileDescriptorSet descriptorSet;
		try {
			in = ClassHelper.getClassLoader()
					.getResourceAsStream(PROTO_DESC_FILENAME);
			descriptorSet = FileDescriptorSet.parseFrom(in);
			RegisterEntry entry;
			for (FileDescriptorProto fdp : descriptorSet.getFileList()) {
				FileDescriptor fd = FileDescriptor.buildFrom(fdp,
						new FileDescriptor[] {});
				for (com.google.protobuf.Descriptors.ServiceDescriptor service : fd.getServices()) {
					entry = new RegisterEntry();
					entry.setLastUpdated(Calendar.getInstance().getTime());
					entry.setServiceDefinition(new ServiceDefinition(service
							.getFullName(), callBindService(service.getName(), service.getFullName())));
					addService(entry);
				}
			}
			LOG.info("Load Service is finished, total {} Register Entry are found.", services.size());
		} catch (Exception ex) {
			LOG.error("Load service error happened.", ex);
			throw new RuntimeException(ex);

		} finally {
			IOUtils.closeInputStream(in);
		}
	}

	private io.grpc.ServerServiceDefinition callBindService(final String serviceInterfaceName,
			final String fullServiceClazzName) throws Exception {

		Class<?> rpcClazz = Class.forName(fullServiceClazzName + SERVICE_CLAZZ_SUFFIX,
				true, ClassHelper.getClassLoader());
		
		Class<?> serviceInterface = Class.forName(fullServiceClazzName + SERVICE_CLAZZ_SUFFIX+"$"+serviceInterfaceName, true,
				ClassHelper.getClassLoader());

		Method serviceBinder = rpcClazz.getMethod(SERVICE_BIND_METHOD, serviceInterface);
		return (ServerServiceDefinition) serviceBinder.invoke(
				null,
				ServiceInstanceProvider.provider().getServiceBean(
						serviceInterface));

	}
}
