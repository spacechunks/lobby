package space.chunks.explorer.lobby.grpc

import io.grpc.CallCredentials
import io.grpc.Metadata
import java.util.concurrent.Executor

class AuthCredentials(val token: String) : CallCredentials() {
    override fun applyRequestMetadata(
        requestInfo: RequestInfo?,
        appExecutor: Executor?,
        applier: MetadataApplier?
    ) {
        val m = Metadata()
        val k = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
        m.put(k, this.token)
        applier?.apply(m)
    }
}