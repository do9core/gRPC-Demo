package xyz.do9core.proto_todo

import io.grpc.ManagedChannelBuilder
import xyz.do9core.proto_todo.service.TodoServiceGrpcKt

object TodoRepository {

    val stub: TodoServiceGrpcKt.TodoServiceCoroutineStub

    init {
        val channel = ManagedChannelBuilder
            .forAddress("192.168.50.128", 6672)
            .usePlaintext()
            .build()
        stub = TodoServiceGrpcKt.TodoServiceCoroutineStub(channel)
    }
}
