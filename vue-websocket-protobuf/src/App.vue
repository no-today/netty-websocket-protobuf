<template>
  <div id="app">
    <div class="text-center">
      WebSocket Protobuf Client
    </div>
    <Row>
      <i-col offset="1" span="7">
        <Card id="config">
          <p slot="title">
            Config
          </p>
          <Form>
            <FormItem label="Authentication Token">
              <Input size="large" v-model="form.token" re/>
            </FormItem>
            <FormItem label="Subscribe Topic">
              <Select size="large" v-model="form.subscribes" multiple>
                <Option v-for="subscribe in subscribes" :value="subscribe" :key="subscribe">{{ subscribe }}</Option>
              </Select>
            </FormItem>
            <FormItem label="Socket Endpoint">
              <Input size="large" v-model="form.endpoint"/>
            </FormItem>
            <FormItem>
              <Button type="primary" @click="connect" :disabled="connected">Connect</Button>
              <Button style="margin-left: 8px" @click="disconnect" :disabled="!connected">DiConnect</Button>
            </FormItem>

            <FormItem label="Send Data">
              <Input size="large" v-model="form.data"/>
            </FormItem>
            <FormItem>
              <Button type="primary" @click="send" :disabled="!connected">Send</Button>
            </FormItem>
          </Form>
        </Card>
      </i-col>
      <i-col id="message" offset="1" span="14">
        <Card>
          <p slot="title">
            Message ({{table.messages.length}})
          </p>
          <a href="#" slot="extra" @click.prevent="table.messages = []">
            <Icon type="ios-loop-strong"></Icon>
            Clear
          </a>
          <Table :max-height="height - 52" :row-class-name="rowClassName" :columns="table.headers"
                 :data="table.messages"/>
        </Card>
      </i-col>
    </Row>
  </div>
</template>

<script>
  import proto from "@/protocol/messages_pb.js"

  const google_protobuf_wrappers_pb = require('google-protobuf/google/protobuf/wrappers_pb.js')
  const google_protobuf_any_pb = require('google-protobuf/google/protobuf/any_pb.js')

  export default {
    name: 'app',
    data() {
      return {
        height: 0,
        subscribes: ['/topic/tracker', '/topic/notice'],
        form: {
          token: '',
          endpoint: 'ws://127.0.0.1:7879',
          subscribes: ['/topic/tracker', '/topic/notice'],
          command: 1,
          data: ''
        },
        table: {
          headers: [
            // {title: 'Subscribe', key: 'subscribe', width: 150},
            {title: 'Time', key: 'time', width: 120},
            {title: 'Content', key: 'content', tooltip: true},
          ],
          messages: []
        },
        connected: false,
        client: WebSocket
      }
    },
    methods: {
      send() {
        let message = this.buildMessage()

        this.client.send(message.serializeBinary())

        this.table.messages.push({
          time: new Date().toLocaleTimeString(),
          content: this.form.data,
          className: 'table-sender-row'
        })
      },
      connect() {
        if (!this.connected) {
          this.client = new WebSocket(this.form.endpoint + '?' + this.buildSubscribes())

          this.client.onmessage = this.doOnMessage
          this.client.onopen = this.doOnOpen
          this.client.onclose = this.doOnClose
          this.client.onerror = this.onError
        }
      },
      disconnect() {
        this.client.close()
      },
      doOnOpen() {
        console.log("Connected")
        this.connected = true
      },
      doOnClose(event) {
        let msg = codes[event.code]
        if (msg === undefined) msg = '0000 UNKNOWN_ERROR'
        console.log("Connection closed: " + msg)
        this.connected = false
      },
      doOnError(err) {
        console.log("Connection error: " + err)
      },
      doOnMessage(event) {
        let reader = new FileReader()
        reader.readAsArrayBuffer(event.data)
        reader.onload = () => {
          let array = new Uint8Array(reader.result)
          let message = proto.Message.deserializeBinary(array)

          let data
          let any = google_protobuf_any_pb.Any.deserializeBinary(message.getData().serializeBinary())
          switch (message.getData().getTypeName()) {
            case 'google.protobuf.StringValue':
              data = any.unpack(google_protobuf_wrappers_pb.StringValue.deserializeBinary, any.getTypeName()).toObject().value
              break
            default:
              data = ''
          }

          this.table.messages.push({
            time: new Date(message.getHeader().getTimestamp()).toLocaleTimeString(),
            content: JSON.stringify({
              command: message.getCommand(),
              data: data
            }),
            className: 'table-receive-row'
          })
        }
      },
      buildSubscribes() {
        return this.form.subscribes.map(e => 'subscribes=' + e).reduce((a, b) => a + '&' + b)
      },
      buildMessage() {
        let data = new google_protobuf_wrappers_pb.StringValue()
        data.setValue(this.form.data)

        // https://developers.google.com/protocol-buffers/docs/reference/javascript-generated#any
        // https://github.com/protocolbuffers/protobuf/issues/4891
        let any = new google_protobuf_any_pb.Any()
        any.pack(data.serializeBinary(), 'google.protobuf.StringValue')

        let header = new proto.Header()
        header.setVersion('1.0')
        header.setTimestamp(new Date().getTime())
        header.setRequestid('' + new Date().getTime())
        header.setSequenceid('' + new Date().getTime())

        let message = new proto.Message()
        message.setHeader(header)
        message.setCommand(parseInt(this.form.command))
        message.setData(any)

        return message
      },
      rowClassName(row) {
        return row.className
      }
    },
    mounted() {
      this.height = document.getElementById("config").offsetHeight
      document.getElementById('message').style['height'] = this.height + 'px'
      document.getElementById('message').style['max-height'] = this.height + 'px'

      document.getElementsByTagName('table')[0].style['height'] = this.height + 'px'
      document.getElementsByTagName('table')[0].style['max-height'] = this.height + 'px'
    }
  }

  const codes = {
    1000: '1000 CLOSE_NORMAL',
    1001: '1001 CLOSE_GOING_AWAY',
    1002: '1002 CLOSE_PROTOCOL_ERROR',
    1003: '1003 CLOSE_UNSUPPORTED',
    1004: '1004 CLOSE_RETAIN',
    1005: '1005 CLOSE_NO_STATUS',
    1006: '1006 CLOSE_ABNORMAL',
    1007: '1007 UNSUPPORTED_DATA',
    1008: '1008 POLICY_VIOLATION',
    1009: '1009 CLOSE_TOO_LARGE',
    1010: '1010 MISSING_EXTENSION',
    1011: '1011 INTERNAL_ERROR',
    1012: '1012 SERVICE_RESTART',
    1013: '1013 TRY_AGAIN_LATER',
    1014: '1014 CLOSE_RETAIN',
    1015: '1015 TLS_HANDSHAKE'
  }
</script>

<style>
  #app {
    font-size: 25px;
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    margin-top: 10px;
    margin-bottom: 10px;
  }

  .text-center {
    text-align: center;
    margin: 30px;
    display: block;
  }

  #app #message /deep/ .ivu-card-body {
    padding: 0;
  }

  #app #message /deep/ .ivu-card.ivu-card-bordered {
    height: 100%;
  }

  .ivu-table .table-sender-row td {
    color: black;
    background-color: #dee6f8;
  }

  .ivu-table .table-receive-row td {
    color: black;
    background-color: #f3eaea;
  }
</style>
