# vue-websocket-protobuf

protobufjs 库不兼容 Any 类型, 服务端拿到的数据里 Any 类型的值永远为空(客户端压根没带过去), 所以我选择使用 google-protobuf 库。

- https://stackoverflow.com/questions/62699640/protobufjs-any-usage
- https://github.com/protobufjs/protobuf.js/issues/435

## Project setup
```
yarn install
```

### Compiles and hot-reloads for development
```
yarn serve
```

### Compiles and minifies for production
```
yarn build
```

### Lints and fixes files
```
yarn lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
