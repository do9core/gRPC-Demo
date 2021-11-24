# gRPC Demo Project

## Introduction

This is a demo project for testing the [Protobuf](https://developers.google.com/protocol-buffers/docs/overview) and [gRPC](https://www.grpc.io).

## Features

This project includes server and client applications.

### Server

A gRPC server written in [Go](https://go.dev/).

### Client

An Android application written in [Kotlin](https://kotlinlang.org) with [Jetpack Compose](https://developer.android.com/jetpack/compose) for UI layer.

## How to use this project

### Server

> If Go is not installed, please check the [installation guide](https://go.dev/doc/install) first.

1. Download `protoc`'s latest version from [here](https://github.com/protocolbuffers/protobuf/releases)

2. Download `protoc-gen-go` plugin and `grpc` with Go package manager
    ```
    go get -u google.golang.org/grpc
    go get -u github.com/golang/protobuf/protoc-gen-go
    ```
    You could find `protoc-gen-go.exe` under `GOPATH/bin`
    
3. Use `protoc` to generate `*.pb.go` files and put them under `Server/api` directory

    ```
    mkdir ../Server/api
    protoc --go_out=plugins=grpc:../Server/api *.proto
    ```

4. Use `go run main.go` to quickly start a server or `go build` to build a server binary

### Client

> Note: This project has only been built on MacOS(12.0.1) and Windows 11(22000.348).

1. Copy or link protocol buffer files to `android/src/main/proto` directory
2. Import project to [Android Studio](https://developer.android.com/studio/projects/android-studio) and sync project with gradle
3. Build the project with gradle and install on your device(or emulator)

## References

* [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/overview)
* [gRPC Document](https://www.grpc.io/docs/)
* [Go 中的 gRPC 入门详解 - 痴者工良 - 博客园 (cnblogs.com)](https://www.cnblogs.com/whuanle/p/14588031.html)

## License

>    Copyright 2021 do9core
>
>   Licensed under the Apache License, Version 2.0 (the "License");  
>   you may not use this file except in compliance with the License.  
>   You may obtain a copy of the License at  
>
>       http://www.apache.org/licenses/LICENSE-2.0
>
>   Unless required by applicable law or agreed to in writing, software  
>   distributed under the License is distributed on an "AS IS" BASIS,  
>   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
>   See the License for the specific language governing permissions and  
>   limitations under the License.  
