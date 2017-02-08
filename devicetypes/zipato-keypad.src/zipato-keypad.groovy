/**
 *  zipato-keypad
 *
 *  Copyright 2017 `
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
    definition(name: "Zipato Keypad", namespace: "alexgrahamuk", author: "Alex Graham") {
        capability "Polling"
        capability "Sensor"

        command "executeCommand"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {

    }
}

preferences {
    input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
    input("gatewayPort", "text", title: "Gateway Port", required: true, displayDuringSetup: true)
}

def message(msg) {
    log.debug(msg)
}

// parse events into attributes
def parse(String description) {

    def ping = createEvent(name:"ping", value: description, isStateChange: true)
    return ping
}


def hubActionResponse(response) {

    message("Got 'hubActionResponse'")
    sendEvent(name: "ping", value: response, isStateChange: true)
}


def poll() {
    message("Executing 'poll'")
}


def executeCommand(command, ip=null, dnid=null) {

    device.deviceNetworkId = "000C290D21CC"

    def headers = [:]
    headers.put("HOST", "$gatewayIP:$gatewayPort")
    headers.put("x-hs110-ip", ip)
    headers.put("x-hs110-command", command)
    headers.put("x-hs110-dnid", dnid)
    headers.put("callback", getCallBackAddress())

    try {
        sendHubCommand(new physicalgraph.device.HubAction([
                method : "POST",
                path   : "/",
                headers: headers],
                device.deviceNetworkId,
                [callback: "hubActionResponse"]
        ))
    } catch (e) {
        message(e.message)
    }
}

//Callback Stuff
private getCallBackAddress() {
    device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

