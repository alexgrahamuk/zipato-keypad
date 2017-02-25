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

	dynamicPage(name: "page1", install: true, uninstall: true) {
    
        section {
            input(name: "keypads", type: "capability.lockCodes", title: "Zipato Keypads", required: true, multiple: true)
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
            input(name: "${uid}", type: "text", title: "User Code", defaultValue: settings."userCode${params.userID}", required: true, submitOnChange: true)
        }
        
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
}

def initialize() {

	log.debug("init called")
    subscribe(keypads, "switch.on", switchOnHandler)
    subscribe(keypads, "alarm.armed", armedHandler)
    subscribe(keypads, "alarm.reallyArmed", reallyHandler)
}


def reallyHandler(evt) {
	log.debug("It really is armed")
    location.setMode("Away")
    location.setMode("Home")
}

def armedHandler(evt) {
	log.debug("Alarm Handler")
    runIn(Integer.valueOf(String.valueOf(evt.value)), armAlarm)
}

def armAlarm() {
	keypads.armAlarm()
}

def switchOnHandler(evt)
{
	log.debug("Users Number Call")
	keypads.reloadAllCodes()
}