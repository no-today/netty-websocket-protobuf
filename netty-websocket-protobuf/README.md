## protoc

```shell script
protoc --java_out=./src/main/java ./src/main/protobuf/messages.proto
protoc --js_out=import_style=commonjs,binary:. ./src/main/protobuf/messages.proto
```

## 核心组件

![UML](pic/uml.jpg)