definition(
        name: "Zipato Keypad Manager",
        namespace: "alexgrahamuk",
        author: "Alex Graham",
        description: "Allows you to manager users on your Zipato Keypad.",
        category: "My Apps",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png",
        singleInstance: true
)

preferences {
    section("Devices") {
        input(name: "keypad", type: "device.zipatoKeypad", title: "Zipato Keypad", required: true, displayDuringSetup: true, multiple: false)
    }
}


def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
    keypad.getUsers()
}

def initialize() {

//    subscribe(devices, "switch.on", "switchOnHandler")
//    subscribe(devices, "switch.off", "switchOffHandler")
//    subscribe(devices, "refresh.refresh", "switchRefreshHandler")
//    subscribe(gateway, "ping", "switchStatusHandler")


}

def switchOnHandler(evt)
{
//    log.debug("A switch turned on")
//    log.debug(evt.getDevice().deviceNetworkId)
//    gateway.poll()
//    gateway.executeCommand("on", evt.getDevice().currentValue('outletIP'), evt.getDevice().deviceNetworkId)
}

def switchOffHandler(evt)
{
//    log.debug("A switch turned off")
//    gateway.executeCommand("off", evt.getDevice().currentValue('outletIP'), evt.getDevice().deviceNetworkId)
}

def switchRefreshHandler(evt)
{
//    log.debug("A switch was refreshed")
//    gateway.executeCommand("status", evt.getDevice().currentValue('outletIP'), evt.getDevice().deviceNetworkId)
}

def switchStatusHandler(evt) {

  /*  log.debug("A switch was queried for status")

    def description = evt.value
    message("Parsing: $description")

    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data

    //def uuid = UUID.randomUUID().toString()
    //device.deviceNetworkId = "tp_link_${uuid}"

    //sendEvent(name: "power", value: "123", isStateChange: true)*/
}