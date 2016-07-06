package practices.microservice.registry.instance.grpc;

import io.grpc.ServerServiceDefinition;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import practices.microservice.common.io.IOUtils;
import practices.microservice.common.utils.ClassHelper;
import practices.microservice.common.utils.ConcurrentSet;
import practices.microservice.registry.instance.BuzzServiceInstanceProvider;

import com.google.api.client.util.Strings;
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
    private final static ConcurrentSet<String> serviceStubs = new ConcurrentSet<String>();
	private GrpcServiceDefinitionLoader(){
		loadService();
	}
	
	public List<ServerServiceDefinition> getServiceList() {

		return ImmutableList.copyOf(services);
	}
	
	public List<String> getServiceStubList() {

		return ImmutableList.copyOf(serviceStubs);
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
					services.add(callBindService(service.getName(), fd.getOptions().getJavaPackage()+'.'+service.getFullName()));
					addServiceStub(service.getName(), fd.getOptions().getJavaPackage()+'.'+service.getName());
				}
			}
			LOG.info("Load service denifition is finished, total {} service are found.", services.size());
			LOG.info("Load service Stub is finished, total {} service stub are found.", serviceStubs.size());
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
     * 
     * @param serviceInterfaceName the short name of grpc service.
     * @param fullServiceClazzName
     * @param stubSuffix
     * @return the full class name for the service stub.
     */
	private String vefiryServiceStub(String serviceInterfaceName, String fullServiceClazzName, String stubSuffix)
	{
		boolean validated = true;
		String serviceStub = fullServiceClazzName + GrpcConstants.SERVICE_CLAZZ_SUFFIX + "$" + serviceInterfaceName
				+ stubSuffix;
		try {
			ClassHelper.forName(serviceStub);
		} catch (ClassNotFoundException e) {
			validated = false;
			LOG.error("Build Service Stub error happened, no '{serviceStubName}' class found! Skip it!!!", e);
		}
	    return validated? serviceStub : null;
	}
	
	private void addServiceStub(String serviceInterfaceName, String fullServiceClazzName)
	{
		String asynStub = vefiryServiceStub(serviceInterfaceName, fullServiceClazzName, GrpcConstants.CLIENT_STUB_SUFFIX_ASYN);
		String blockingStub = vefiryServiceStub(serviceInterfaceName, fullServiceClazzName, GrpcConstants.CLIENT_STUB_SUFFIX_BLOCKING);
		String futureStub = vefiryServiceStub(serviceInterfaceName, fullServiceClazzName, GrpcConstants.CLIENT_STUB_SUFFIX_FUTURE);
		if(!Strings.isNullOrEmpty(asynStub)){
			serviceStubs.add(asynStub);
		}
		if(!Strings.isNullOrEmpty(blockingStub)){
			serviceStubs.add(blockingStub);
		}
		if(!Strings.isNullOrEmpty(futureStub)){
			serviceStubs.add(futureStub);
		}
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
