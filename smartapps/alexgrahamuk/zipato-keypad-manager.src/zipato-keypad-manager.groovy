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

	page(name: "page1", title: "Select keypads and maximum users", uninstall: true)
    page(name: "page2", title: "User Setup", uninstall: false, install: false)
    page(name: "page3", title: "Edit User", uninstall: false, install: false)
}

def page1() {

	dynamicPage(name: "page1") {
    
        section {
            input(name: "keypads", type: "device.zipatoKeypad", title: "Zipato Keypads", required: true, multiple: true)
            input(name: "maxUsers", type: "number", title: "Number of Users", range: "0..255", required: true, default: 0)
        }

		if (maxUsers > 0) {
            section("Manage Users") {
                href(name: "linkToPage2", title: "Manage Users", required: false, page: "page2")
            }        
        }
    
    }

}

def page2() {
    dynamicPage(name: "page2") {
    
            section("Users") {
            
            	for (int x=0; x<maxUsers; x++) {
                	def p = [userID: x as String, smecker: "ih2"]
         	   		href(name: "linkToPage3${x}", title: "Edit User $x (Bob)", required: false, page: "page3", params: p)
               }
        	}
    }
}	

def page3(params) {

	log.debug("Params Page 3: $params")

	if (params.smecker) {
    	atomicState.params = params
    } else {
    	params = atomicState.params
    }

   	def uid = "userCode${params.userID}"
        
	log.debug("UID is: $uid")
    log.debug("Current Settings: $settings")

    dynamicPage(name: "page3", title: "Editing User $params.userID X (Bob)") {
    
        section {
        	label(title: "${uid}")
            input(name: "${uid}", type: "text", title: "User Code", required: true, default: "1234", submitOnChange: true)
        }
        
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    //unsubscribe()
    initialize()
    //keypad.getUsers()
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