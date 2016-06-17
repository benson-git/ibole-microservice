package practices.microservice.registry.grpc;

import io.grpc.ServerServiceDefinition;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.io.IOUtils;
import practices.microservice.common.utils.ClassHelper;
import practices.microservice.common.utils.ConcurrentSet;
import practices.microservice.registry.BuzzServiceInstanceProvider;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
/**
 * Build the list of instance of ServerServiceDefinition 
 * by parsing the generated proto description file in the specified path.
 * 
 * @author bwang
 *
 */
public final class GrpcServiceDefinitionLoader {

	private final static Logger LOG = LoggerFactory.getLogger(GrpcServiceDefinitionLoader.class);
	private final static ConcurrentSet<ServerServiceDefinition> services = new ConcurrentSet<ServerServiceDefinition>();
   
	private GrpcServiceDefinitionLoader(){
		loadService();
	}
	
	public List<ServerServiceDefinition> getServiceList() {

		return ImmutableList.copyOf(services);
	}
	
	private void loadService() {

		LOG.info("Load service definition is starting...");
		InputStream in = null;
		FileDescriptorSet descriptorSet;
		try {
			in = ClassHelper.getClassLoader()
					.getResourceAsStream(GrpcConstants.PROTO_DESC_FILENAME);
			descriptorSet = FileDescriptorSet.parseFrom(in);
			for (FileDescriptorProto fdp : descriptorSet.getFileList()) {
				FileDescriptor fd = FileDescriptor.buildFrom(fdp,
						new FileDescriptor[] {});
				for (com.google.protobuf.Descriptors.ServiceDescriptor service : fd.getServices()) {
					services.add(callBindService(service.getName(), service.getFullName()));
				}
			}
			LOG.info("Load service denifition is finished, total {} service are found.", services.size());
		} catch (Exception ex) {
			LOG.error("Load service denifition error happened.", ex);
			throw new RuntimeException(ex);

		} finally {
			IOUtils.closeInputStream(in);
		}
	}

	private ServerServiceDefinition callBindService(final String serviceInterfaceName,
			final String fullServiceClazzName) throws Exception {

		Class<?> generatedGrpc = ClassHelper.forName(fullServiceClazzName + GrpcConstants.SERVICE_CLAZZ_SUFFIX);
		//service interface is a inner static class in generated gRPC class.
		Class<?> serviceInterface = ClassHelper.forName(fullServiceClazzName + GrpcConstants.SERVICE_CLAZZ_SUFFIX+"$"+serviceInterfaceName);

		Method serviceBinder = generatedGrpc.getMethod(GrpcConstants.SERVICE_BIND_METHOD, serviceInterface);
		return (ServerServiceDefinition) serviceBinder.invoke(
				null,
				BuzzServiceInstanceProvider.provider().getServiceBean(
						serviceInterface));

	}
	/**
	 * Load the service with lazy load style.
	 * @return the instance of GrpcServiceDefinitionLoader
	 */
	public static GrpcServiceDefinitionLoader load()
	{
		return Loader.INSTANCE;
	}
	
	private static class Loader {
		private final static GrpcServiceDefinitionLoader INSTANCE = new GrpcServiceDefinitionLoader();
		
	}
}
