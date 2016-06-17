package practices.microservice.registry.grpc;
/**
 * Some naming Conventions for the generated Grpc class.
 * @author bwang
 *
 */
public final class GrpcConstants {

	public static final String PROTO_DESC_FILENAME = "protos.desc";
	public static final String SERVICE_CLAZZ_SUFFIX = "Grpc";
	public static final String SERVICE_BIND_METHOD = "bindService";
	public static final String CLIENT_STUB_CLAZZ_PATTERN = "{service short name}([Blocking|Future])?Stub";

}